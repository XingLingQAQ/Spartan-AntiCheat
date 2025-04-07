package ai.idealistic.spartan.functionality.tracking;

import ai.idealistic.spartan.Register;
import ai.idealistic.spartan.abstraction.check.Check;
import ai.idealistic.spartan.abstraction.check.CheckDetection;
import ai.idealistic.spartan.abstraction.check.CheckEnums;
import ai.idealistic.spartan.abstraction.check.CheckRunner;
import ai.idealistic.spartan.abstraction.profiling.MiningHistory;
import ai.idealistic.spartan.abstraction.profiling.PlayerProfile;
import ai.idealistic.spartan.abstraction.protocol.PlayerProtocol;
import ai.idealistic.spartan.functionality.concurrent.GeneralThread;
import ai.idealistic.spartan.functionality.connection.CloudBase;
import ai.idealistic.spartan.functionality.server.Config;
import ai.idealistic.spartan.functionality.server.PluginBase;
import ai.idealistic.spartan.utils.java.ConcurrentList;
import ai.idealistic.spartan.utils.math.AlgebraUtils;
import ai.idealistic.spartan.utils.minecraft.inventory.MaterialUtils;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ResearchEngine {

    private static boolean firstLoad = false;
    public static final Map<Integer, Double> averageViolationTime = new ConcurrentHashMap<>();
    public static Map<CheckEnums.HackType, Collection<Check.DataType>> violationFired = new ConcurrentHashMap<>();
    private static long schedulerTicks = 0L;
    private static final Map<String, PlayerProfile> playerProfiles = new ConcurrentHashMap<>();
    private static final GeneralThread.ThreadPool statisticsThread = new GeneralThread.ThreadPool(1L);

    static {
        PluginBase.runRepeatingTask(() -> {
            if (firstLoad) {
                if (schedulerTicks == 0) {
                    schedulerTicks = 1200L;

                    if (Config.sql.isEnabled()) {
                        refresh(Register.isPluginEnabled());
                    } else {
                        statisticsThread.executeIfFree(() -> updateCache(false));
                    }
                } else {
                    schedulerTicks -= 1;

                    statisticsThread.executeIfFree(() -> updateCache(false));
                }
            }
        }, 1L, 1L);
    }

    // Separator

    public static Collection<PlayerProfile> getPlayerProfiles() {
        return playerProfiles.values();
    }

    public static PlayerProfile getPlayerProfile(String name) {
        PlayerProfile playerProfile = playerProfiles.get(name);

        if (playerProfile == null) {
            playerProfile = new PlayerProfile(name);
            playerProfiles.put(name, playerProfile);
        }
        return playerProfile;
    }

    public static PlayerProfile getAnyCasePlayerProfile(String name) {
        if (!playerProfiles.isEmpty()) {
            for (Map.Entry<String, PlayerProfile> entry : playerProfiles.entrySet()) {
                if (entry.getKey().equalsIgnoreCase(name)) {
                    return entry.getValue();
                }
            }
        }
        return null;
    }

    public static PlayerProfile getPlayerProfile(PlayerProtocol protocol) {
        PlayerProfile playerProfile = playerProfiles.get(protocol.bukkit().getName());

        if (playerProfile == null) {
            playerProfile = new PlayerProfile(protocol);
            playerProfiles.put(protocol.bukkit().getName(), playerProfile);
        }
        return playerProfile;
    }

    private static void createPlayerProfile(PlayerProtocol protocol) {
        PlayerProfile profile = new PlayerProfile(protocol);
        profile.update(protocol);
        playerProfiles.put(protocol.bukkit().getName(), new PlayerProfile(protocol));
    }

    // Separator

    public static void resetData(CheckEnums.HackType hackType) {
        if (firstLoad) {
            statisticsThread.execute(() -> {
                String hackTypeString = hackType.toString();

                // Separator

                if (!playerProfiles.isEmpty()) {
                    for (PlayerProfile playerProfile : playerProfiles.values()) {
                        CheckRunner runner = playerProfile.getRunner(hackType);

                        if (runner != null) {
                            for (CheckDetection detection : runner.getDetections()) {
                                for (Check.DataType dataType : Check.DataType.values()) {
                                    detection.clearData(dataType);
                                }
                            }
                        } else {
                            for (String detection : hackType.getDetections()) {
                                for (Check.DataType dataType : Check.DataType.values()) {
                                    playerProfile.clearTimeDifferences(
                                            hackType,
                                            dataType,
                                            detection
                                    );
                                }
                            }
                        }
                        playerProfile.getContinuity().clear();
                    }
                    updateCache(true);
                }

                // Separator

                if (Config.sql.isEnabled()) {
                    Config.sql.update("DELETE FROM " + Config.sql.getTable() + " WHERE functionality = '" + hackTypeString + "';");
                }

                // Separator

                Collection<File> files = getFiles();

                if (!files.isEmpty()) {
                    for (File file : files) {
                        YamlConfiguration configuration = YamlConfiguration.loadConfiguration(file);

                        for (String key : configuration.getKeys(false)) {
                            String value = configuration.getString(key);

                            if (value != null && value.contains(hackTypeString)) {
                                configuration.set(key, null);
                            }
                        }
                        try {
                            configuration.save(file);
                        } catch (Exception ignored) {
                        }
                    }
                }
            });
        }
    }

    public static void resetData(String playerName) {
        if (firstLoad) {
            // Clear Violations
            PlayerProtocol player = PluginBase.getProtocol(playerName);

            if (isStorageMode()) {
                // Clear Files/Database
                statisticsThread.execute(() -> {
                    if (player == null) {
                        playerProfiles.remove(playerName);
                    } else {
                        createPlayerProfile(player);
                    }
                    if (Config.sql.isEnabled()) {
                        Config.sql.update("DELETE FROM " + Config.sql.getTable() + " WHERE information LIKE '%" + playerName + "%';");
                    }
                    Collection<File> files = getFiles();

                    if (!files.isEmpty()) {
                        for (File file : files) {
                            YamlConfiguration configuration = YamlConfiguration.loadConfiguration(file);

                            for (String key : configuration.getKeys(false)) {
                                String value = configuration.getString(key);

                                if (value != null && value.contains(playerName)) {
                                    configuration.set(key, null);
                                }
                            }
                            try {
                                configuration.save(file);
                            } catch (Exception ignored) {
                            }
                        }
                    }
                });
            } else if (player == null) {
                playerProfiles.remove(playerName);
            } else {
                createPlayerProfile(player);
            }
            PluginBase.playerInfo.refresh(playerName);
        }
    }

    // Separator

    public static String findInformation(String s, String pattern) {
        String find = "(" + pattern + " ";
        int index = s.indexOf(find);

        if (index > -1) {
            s = s.substring(index + find.length());
            index = s.indexOf(")");
            return s.substring(0, index);
        }
        return null;
    }

    private static Check.DataType findDataType(String s) {
        s = findInformation(s, CheckDetection.javaPlayerIdentifier);

        if (s != null) {
            for (Check.DataType dataType : Check.DataType.values()) {
                if (s.equals(dataType.toString())) {
                    return dataType;
                }
            }
        }
        return Check.DataType.JAVA;
    }

    private static boolean isStorageMode() {
        return Config.settings.getBoolean("Logs.log_file") || Config.sql.isEnabled();
    }

    private static Collection<File> getFiles() {
        File[] files = new File(AntiCheatLogs.folderPath).listFiles();

        if (files != null && files.length > 0) {
            TreeMap<Integer, File> map = new TreeMap<>();
            String start = "log", end = ".yml";
            int startLength = start.length(), endLength = end.length();

            for (File file : files) {
                if (file.isFile()) {
                    String name = file.getName();

                    if (name.startsWith(start) && name.endsWith(end)) {
                        Integer integer = AlgebraUtils.returnValidInteger(name.substring(startLength, name.length() - endLength));

                        if (integer != null) {
                            map.put(integer, file);
                        }
                    }
                }
            }
            List<File> list = new LinkedList<>(map.values());
            Collections.reverse(list);
            return list;
        }
        return new ArrayList<>(0);
    }

    private static Map<String, String> getLogs() {
        Map<String, String> cache = new LinkedHashMap<>();
        int byteSize = 0;
        boolean isFull = false,
                continueWithYAML = false;

        // Separator

        if (Config.sql.isEnabled()) {
            try {
                ResultSet rs = Config.sql.query("SELECT creation_date, information FROM " + Config.sql.getTable() + " ORDER BY id DESC LIMIT " + PluginBase.maxSQLRows + ";");

                if (rs != null) {
                    while (rs.next()) {
                        String data = rs.getString("information"),
                                date = rs.getString("creation_date");
                        cache.put(date, data);
                        byteSize += date.length() + data.length();

                        if (byteSize >= PluginBase.maxBytes) {
                            isFull = true;
                            break;
                        }
                    }
                    rs.close();

                    if (cache.isEmpty()) {
                        continueWithYAML = true;
                    }
                } else {
                    continueWithYAML = true;
                }
            } catch (Exception ex) {
                continueWithYAML = true;
            }
        } else {
            continueWithYAML = true;
        }

        // YAML Process

        if (!isFull && continueWithYAML) {
            Collection<File> files = getFiles();

            if (!files.isEmpty()) {
                for (File file : files) {
                    YamlConfiguration c = YamlConfiguration.loadConfiguration(file);

                    for (String key : c.getKeys(false)) {
                        String data = c.getString(key);

                        if (data != null) {
                            cache.put(key, data);
                            byteSize += key.length() + data.length();

                            if (byteSize >= PluginBase.maxBytes) {
                                isFull = true;
                                break;
                            }
                        }
                    }
                    if (isFull) {
                        break;
                    }
                }
            }
        }
        return cache;
    }

    // Separator

    public static void refresh(boolean enabledPlugin) {
        Runnable runnable = () -> {
            // Complete Storage
            Config.sql.refreshDatabase();

            if (enabledPlugin) {
                buildCache();
                CloudBase.refresh();
            }
        };

        if (firstLoad) {
            statisticsThread.executeIfFree(runnable);
        } else {
            statisticsThread.execute(runnable);
            firstLoad = true;
        }
    }

    private static void buildCache() {
        if (isStorageMode()) {
            Map<String, String> logs = getLogs();

            if (!logs.isEmpty()) {
                for (Map.Entry<String, String> entry : logs.entrySet()) {
                    String fullDate = entry.getKey(),
                            data = entry.getValue();
                    String detection = findInformation(
                            data,
                            CheckDetection.detectionIdentifier
                    );

                    if (detection != null) {
                        String hackTypeString = findInformation(
                                data,
                                CheckDetection.checkIdentifier
                        );

                        if (hackTypeString != null) {
                            String player = findInformation(
                                    data,
                                    AntiCheatLogs.playerIdentifier
                            );

                            if (player != null) {
                                for (CheckEnums.HackType hackType : CheckEnums.HackType.values()) {
                                    if (hackTypeString.equals(hackType.toString())) {
                                        Check.DataType dataType = findDataType(data);
                                        PlayerProfile profile = getPlayerProfile(player);
                                        profile.setLastDataType(dataType);
                                        SimpleDateFormat sdf = new SimpleDateFormat(AntiCheatLogs.dateFormat);

                                        try {
                                            hackType.addDetection(
                                                    detection,
                                                    CheckDetection.DEFAULT_AVERAGE_TIME
                                            );
                                            profile.addTimeDifference(
                                                    hackType,
                                                    dataType,
                                                    detection,
                                                    sdf.parse(fullDate).getTime()
                                            );
                                        } catch (Exception ignored) {
                                        }
                                        break;
                                    }
                                }
                            }
                        }
                    } else {
                        String oreString = findInformation(
                                data,
                                MiningHistory.oreIdentifier
                        );

                        if (oreString != null) {
                            String amount = findInformation(
                                    data,
                                    MiningHistory.amountIdentifier
                            );

                            if (amount != null
                                    && AlgebraUtils.validInteger(amount)) {
                                String player = findInformation(
                                        data,
                                        AntiCheatLogs.playerIdentifier
                                );

                                if (player != null) {
                                    String environmentString = findInformation(
                                            data,
                                            MiningHistory.environmentIdentifier
                                    );

                                    if (environmentString != null) {
                                        Material material = MaterialUtils.findMaterial(
                                                oreString.toUpperCase().replace("-", "_")
                                        );

                                        if (material != null) {
                                            MiningHistory.MiningOre ore = MiningHistory.getMiningOre(material);

                                            if (ore != null) {
                                                World.Environment environment = MaterialUtils.findEnvironment(
                                                        environmentString.toUpperCase().replace("-", "_")
                                                );

                                                if (environment != null) {
                                                    getPlayerProfile(
                                                            player
                                                    ).getMiningHistory(
                                                            ore
                                                    ).increaseMines(
                                                            environment,
                                                            Integer.parseInt(amount)
                                                    );
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            int index = data.indexOf(PlayerProfile.activeFor);

                            if (index != -1) {
                                SimpleDateFormat sdf = new SimpleDateFormat(AntiCheatLogs.dateFormat);

                                try {
                                    ResearchEngine.getPlayerProfile(
                                            data.split(" ", 2)[0]
                                    ).getContinuity().setActiveTime(
                                            sdf.parse(fullDate).getTime(),
                                            Long.parseLong(data.substring(index + PlayerProfile.activeFor.length())),
                                            false
                                    );
                                } catch (Exception ignored) {
                                }
                            }
                        }
                    }
                }
            }
        }
        for (PlayerProfile profile : playerProfiles.values()) {
            for (CheckEnums.HackType hackType : CheckEnums.HackType.values()) {
                for (String detection : hackType.getDetections()) {
                    profile.sortTimeDifferences(
                            hackType,
                            detection
                    );
                }
            }
        }
        updateCache(true);
    }

    private static void updateCache(boolean force) {
        if ((force || !violationFired.isEmpty()) && !playerProfiles.isEmpty()) {
            Collection<PlayerProfile> profiles = playerProfiles.values();

            for (CheckEnums.HackType hackType : (force
                    ? Arrays.asList(CheckEnums.HackType.values())
                    : violationFired.keySet())) {
                Collection<Check.DataType> dataTypes = violationFired.get(hackType);

                for (Check.DataType dataType : (force
                        ? Arrays.asList(Check.DataType.values())
                        : (dataTypes == null || dataTypes.isEmpty()
                        ? Arrays.asList(Check.DataType.values()) :
                        dataTypes))) {
                    Map<String, List<List<Long>>> statistics = new LinkedHashMap<>();

                    if (hackType.getCheck().isEnabled(dataType, null)) {
                        for (PlayerProfile profile : profiles) {
                            for (String detectionExecutor : hackType.getDetections()) {
                                statistics.computeIfAbsent(
                                        detectionExecutor,
                                        k -> new ArrayList<>()
                                ).add(
                                        profile.getTimeDifferences(hackType, dataType, detectionExecutor)
                                );
                            }
                        }

                        if (statistics.isEmpty()) {
                            for (PlayerProfile profile : profiles) {
                                for (String detection : hackType.getDetections()) {
                                    profile.clearTimeDifferences(
                                            hackType,
                                            dataType,
                                            detection
                                    );
                                    averageViolationTime.remove(
                                            (CheckDetection.generalHashCode(hackType, detection) * PluginBase.hashCodeMultiplier)
                                                    + dataType.hashCode()
                                    );
                                }
                            }
                        } else {
                            for (Map.Entry<String, List<List<Long>>> entry : statistics.entrySet()) {
                                List<List<Long>> lists = entry.getValue();
                                String detection = entry.getKey();
                                double mean = 0.0,
                                        realizedMean = 0.0,
                                        realizedTotal = 0.0,
                                        total = 0.0;

                                for (List<Long> list : lists) {
                                    if (!list.isEmpty()) {
                                        for (long time : list) {
                                            realizedMean += time;
                                        }
                                        realizedTotal += list.size();
                                    }
                                }

                                if (realizedTotal > 0) {
                                    realizedMean /= realizedTotal;

                                    for (List<Long> list : lists) {
                                        if (list.isEmpty()) {
                                            mean += realizedMean;
                                            total++;
                                        } else {
                                            for (long time : list) {
                                                mean += time;
                                            }
                                            total += list.size();
                                        }
                                    }
                                } else {
                                    realizedMean = hackType.getDefaultAverageTime(detection);
                                }

                                for (List<Long> list : lists) {
                                    for (long time : list) {
                                        mean += time;
                                    }
                                    total += list.size();
                                }

                                if (total > 0) {
                                    mean /= total;
                                } else {
                                    mean = realizedMean;
                                }
                                averageViolationTime.put(
                                        (CheckDetection.generalHashCode(hackType, detection) * PluginBase.hashCodeMultiplier)
                                                + dataType.hashCode(),
                                        Math.min(
                                                mean,
                                                hackType.getDefaultAverageTime(detection) * 3
                                        )
                                );
                            }
                        }
                    } else {
                        for (PlayerProfile profile : profiles) {
                            for (String detection : hackType.getDetections()) {
                                profile.clearTimeDifferences(
                                        hackType,
                                        dataType,
                                        detection
                                );
                                averageViolationTime.remove(
                                        (CheckDetection.generalHashCode(hackType, detection) * PluginBase.hashCodeMultiplier)
                                                + dataType.hashCode()
                                );
                            }
                        }
                    }
                }
            }
            violationFired.clear();
        }
    }

    public static void queueToCache(CheckEnums.HackType hackType, Check.DataType dataType) {
        violationFired.computeIfAbsent(
                hackType,
                k -> new ConcurrentList<>()
        ).add(dataType);
    }

    public static double getAverageViolationTime(CheckDetection detection, Check.DataType dataType) {
        double average = averageViolationTime.getOrDefault(
                (detection.generalHashCode() * PluginBase.hashCodeMultiplier)
                        + dataType.hashCode(),
                (double) detection.defaultAverageTime
        );
        return average > 0.0 ? average : (double) detection.defaultAverageTime;
    }

}
