package ai.idealistic.vacan.abstraction.check;

import ai.idealistic.vacan.abstraction.Enums;
import ai.idealistic.vacan.abstraction.profiling.PlayerProfile;
import ai.idealistic.vacan.abstraction.protocol.PlayerProtocol;
import ai.idealistic.vacan.api.API;
import ai.idealistic.vacan.api.CheckCancelEvent;
import ai.idealistic.vacan.api.PlayerViolationCommandEvent;
import ai.idealistic.vacan.api.PlayerViolationEvent;
import ai.idealistic.vacan.functionality.connection.CloudConnections;
import ai.idealistic.vacan.functionality.connection.PluginEdition;
import ai.idealistic.vacan.functionality.moderation.DetectionNotifications;
import ai.idealistic.vacan.functionality.moderation.clickable.ClickableMessage;
import ai.idealistic.vacan.functionality.server.Config;
import ai.idealistic.vacan.functionality.server.MultiVersion;
import ai.idealistic.vacan.functionality.server.PluginBase;
import ai.idealistic.vacan.functionality.server.TPS;
import ai.idealistic.vacan.functionality.tracking.AntiCheatLogs;
import ai.idealistic.vacan.functionality.tracking.ResearchEngine;
import ai.idealistic.vacan.utils.math.AlgebraUtils;
import ai.idealistic.vacan.utils.minecraft.server.ConfigUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public abstract class CheckDetection extends CheckProcess {

    public static final String
            javaPlayerIdentifier = "Java:",
            checkIdentifier = "Check:",
            detectionIdentifier = "Detection:",
            levelIdentifier = "Level:",
            notificationLevelIdentifier = "Notification-Level:",
            preventionLevelIdentifier = "Prevention-Level:",
            punishmentLevelIdentifier = "Punishment-Level:";

    public static final long
            DEFAULT_AVERAGE_TIME = 500L,
            TIME_TO_PUNISH = 60_000L,
            TIME_TO_PREVENT = (TIME_TO_PUNISH / 3L) * 2L,
            TIME_TO_NOTIFY = TIME_TO_PUNISH / 2L;

    // Separator

    CheckPrevention prevention;
    public final CheckRunner executor;
    public final String name;
    public final boolean hasName;
    private final Boolean def;
    public final Check.DataType forcedDataType;
    public final Check.DetectionType detectionType;

    public final long defaultAverageTime, timeToNotify, timeToPrevent, timeToPunish;
    private final long[] accumulatedTime, previousTime;
    private long notifications;
    private final List<Long>[] data;

    public CheckDetection(
            CheckRunner executor,
            Check.DataType forcedDataType,
            Check.DetectionType detectionType,
            String name,
            Boolean def,
            long defaultAverageTime,
            long timeToNotify,
            long timeToPrevent,
            long timeToPunish
    ) {
        super(executor.hackType, executor.protocol);
        this.executor = executor;
        this.name = name == null
                ? Integer.toString(this.getClass().getName().hashCode())
                : name;
        this.hasName = name != null;
        this.def = def;
        this.forcedDataType = forcedDataType;
        this.detectionType = detectionType;
        this.prevention = new CheckPrevention();
        this.notifications = 0L;
        this.defaultAverageTime = defaultAverageTime;
        this.timeToNotify = timeToNotify;
        this.timeToPrevent = timeToPrevent;
        this.timeToPunish = timeToPunish;
        this.accumulatedTime = new long[Check.DataType.values().length];
        this.previousTime = new long[Check.DataType.values().length];
        this.data = new List[Check.DataType.values().length];

        for (Check.DataType dataType : Check.DataType.values()) {
            this.data[dataType.ordinal()] = new CopyOnWriteArrayList<>();
            this.accumulatedTime[dataType.ordinal()] = 0L;
            this.previousTime[dataType.ordinal()] = -1L;
        }
        this.isEnabled();

        if (executor.addDetection(this.name, this) != null) {
            throw new IllegalArgumentException(
                    "Detection '" + this.name + "' already exists for enum '" + executor.hackType.toString() + "'."
            );
        }
    }

    public CheckDetection(
            CheckRunner executor,
            Check.DataType forcedDataType,
            Check.DetectionType detectionType,
            String name,
            Boolean def
    ) {
        this(
                executor,
                forcedDataType,
                detectionType,
                name,
                def,
                DEFAULT_AVERAGE_TIME,
                TIME_TO_NOTIFY,
                TIME_TO_PREVENT,
                TIME_TO_PUNISH
        );
    }

    public final int generalHashCode() {
        return (this.hackType.hashCode() * PluginBase.hashCodeMultiplier)
                + (this.name == null ? 0 : this.name.hashCode());
    }

    // Check

    public final boolean isEnabled() {
        return !this.hasName
                || this.def == null
                || this.hackType.getCheck().getBooleanOption("check_" + this.name, this.def);
    }

    public final boolean supportsDataType(Check.DataType dataType) {
        return this.forcedDataType == null
                || this.forcedDataType == dataType;
    }

    public final boolean supportsDetectionType(Check.DetectionType detectionType) {
        return this.detectionType == null
                || this.detectionType == detectionType;
    }

    public final boolean canCall() {
        return this.protocol != null
                && this.executor.canCall()
                && this.isEnabled()
                && this.supportsDetectionType(this.protocol.detectionType)
                && this.supportsDataType(this.protocol.getDataType())
                && PluginEdition.hasDetectionsPurchased(this.protocol.getDataType());
    }

    public final void call(Runnable runnable) {
        if (this.canCall()) {
            runnable.run();
        }
    }

    // Data

    public final void clearData(Check.DataType dataType) {
        this.data[dataType.ordinal()].clear();
        this.accumulatedTime[dataType.ordinal()] = 0L;
        this.previousTime[dataType.ordinal()] = -1L;
    }

    public final void storeData(Check.DataType dataType, long time) {
        Collection<Long> data = this.data[dataType.ordinal()];
        int size = data.size() - 2_048;

        if (size > 0) {
            Iterator<Long> iterator = data.iterator();

            while (iterator.hasNext() && size > 0) {
                if (data.remove(iterator.next())) {
                    size--;
                }
            }
        }
        data.add(time);
    }

    public final void sortData() {
        for (Check.DataType dataType : Check.DataType.values()) {
            List<Long> data = this.data[dataType.ordinal()];

            if (data != null) {
                Collections.sort(data);
            }
        }
    }

    public List<Long> getTimeDifferences(PlayerProfile profile, Check.DataType dataType) {
        List<Long> data = this.data[dataType.ordinal()];

        if (data != null) {
            Iterator<Long> iterator = data.iterator();

            if (iterator.hasNext()) {
                List<Long> differences = new ArrayList<>(data.size() - 1);
                long previous = iterator.next();

                while (iterator.hasNext()) {
                    long current = iterator.next();

                    if (profile.getContinuity().wasOnline(current, previous)) {
                        differences.add(current - previous);
                    }
                    previous = current;
                }
                return differences;
            }
        }
        return this.data[dataType.ordinal()];
    }

    // Accumulation

    private void increaseAccumulationTime(Check.DataType dataType, long time) {
        double average = ResearchEngine.getAverageViolationTime(this, dataType);
        long previousTime = this.previousTime[dataType.ordinal()],
                add;

        if (previousTime == -1L) {
            add = AlgebraUtils.integerRound(average);
        } else {
            double multiplier = Math.min(average / (double) (time - previousTime), 1.0);
            add = AlgebraUtils.integerRound(average * multiplier);
        }
        this.previousTime[dataType.ordinal()] = time;

        if (this.accumulatedTime[dataType.ordinal()] <= System.currentTimeMillis()) {
            this.accumulatedTime[dataType.ordinal()] = System.currentTimeMillis() + add;
        } else {
            this.accumulatedTime[dataType.ordinal()] += add;
        }
    }

    private long getAccumulatedTime(Check.DataType dataType) {
        return Math.max(this.accumulatedTime[dataType.ordinal()] - System.currentTimeMillis(), 0L);
    }

    public final int getLevel(Check.DataType dataType) {
        return AlgebraUtils.integerFloor( // Attention
                this.getAccumulatedTime(dataType) / ResearchEngine.getAverageViolationTime(this, dataType)
        );
    }

    public final int getNotificationLevel(Check.DataType dataType) {
        return AlgebraUtils.integerCeil(
                this.timeToNotify / ResearchEngine.getAverageViolationTime(this, dataType)
        );
    }

    public final int getPreventionLevel(Check.DataType dataType) {
        return AlgebraUtils.integerCeil(
                this.timeToPrevent / ResearchEngine.getAverageViolationTime(this, dataType)
        );
    }

    public final int getPunishmentLevel(Check.DataType dataType) {
        return AlgebraUtils.integerCeil(
                this.timeToPunish / ResearchEngine.getAverageViolationTime(this, dataType)
        );
    }

    // Notification

    public final boolean canSendNotification(long time, int level, int notification) {
        int ticks = this.executor.getNotificationTicksCooldown(protocol);
        boolean canSend = level >= notification
                || DetectionNotifications.isVerboseEnabled(this.protocol);

        if (ticks == 0) {
            return canSend;
        } else if (canSend && this.notifications <= time) {
            this.notifications = System.currentTimeMillis() + (ticks * TPS.tickTime);
            return true;
        } else {
            return false;
        }
    }

    private void notify(
            int level,
            int notification,
            int prevention,
            int punishment,
            long time,
            String information
    ) {
        String notificationMsg = ConfigUtils.replaceWithSyntax(
                this.protocol,
                Config.messages.getColorfulString("detection_notification")
                        .replace("{info}", information)
                        .replace("{detection:level}", (level >= punishment
                                ? "§4" :
                                level >= prevention ? "§6"
                                        : level >= notification ? "§3"
                                        : "§2") + AlgebraUtils.integerFloor(level / (double) punishment * 100.0) + "/100"),
                hackType
        );

        Location location = this.protocol.getLocation();
        information = "(" + AntiCheatLogs.playerIdentifier + " " + this.protocol.bukkit().getName() + "), "
                + "(" + checkIdentifier + " " + this.hackType + "), "
                + "(" + javaPlayerIdentifier + " " + (!this.protocol.isBedrockPlayer()) + ")" + ", "
                + "(" + detectionIdentifier + " " + this.name + ")" + ", "
                + "(" + levelIdentifier + " " + level + "),"
                + "(" + notificationLevelIdentifier + " " + notification + "),"
                + "(" + preventionLevelIdentifier + " " + prevention + "),"
                + "(" + punishmentLevelIdentifier + " " + punishment + "),"
                + "(Server-Version: " + MultiVersion.serverVersion.toString() + "), "
                + "(Plugin-Version: " + API.getVersion() + "), "
                + "(Silent: " + hackType.getCheck().isSilent(this.protocol.getDataType(), this.protocol.getWorld().getName()) + "), "
                + "(Punish: " + hackType.getCheck().canPunish(this.protocol.getDataType()) + "), "
                + "(Packets: " + this.protocol.packetsEnabled() + "), "
                + "(Ping: " + this.protocol.getPing() + "ms), "
                + "(W-XYZ: " + location.getWorld().getName() + " " + location.getBlockX() + " " + location.getBlockY() + " " + location.getBlockZ() + "), "
                + "(Data: " + information + ")";
        boolean canNotifyOutOfVerbose = level >= notification;
        AntiCheatLogs.logInfo(
                this.protocol,
                notificationMsg,
                information,
                canNotifyOutOfVerbose,
                null,
                this.hackType,
                time
        );

        if (canNotifyOutOfVerbose) {
            CloudConnections.executeDiscordWebhook(
                    "checks",
                    protocol.getUUID(),
                    protocol.bukkit().getName(),
                    location.getBlockX(),
                    location.getBlockY(),
                    location.getBlockZ(),
                    "Detection",
                    notificationMsg
            );
        }
        // Local Notifications
        String command = Config.settings.getString("Notifications.message_clickable_command")
                .replace("{player}", this.protocol.bukkit().getName());

        if (Config.settings.getBoolean("Notifications.individual_only_notifications")) {
            if (this.canSendNotification(time, level, notification)) {
                ClickableMessage.sendCommand(this.protocol.bukkit(), notificationMsg, command, command);
            }
        } else {
            List<PlayerProtocol> protocols = DetectionNotifications.getPlayers();

            if (!protocols.isEmpty()) {
                for (PlayerProtocol staff : protocols) {
                    CheckDetection detection = staff.profile().getRunner(this.hackType).getDetection(this.name);

                    if (detection != null
                            && detection.canSendNotification(time, level, notification)) {
                        ClickableMessage.sendCommand(
                                staff.bukkit(),
                                notificationMsg,
                                command,
                                command
                        );
                    }
                }
            }
        }
    }

    // Punishment

    private void punish(int level, int max) {
        Check check = hackType.getCheck();

        if (level >= max
                && check.canPunish(this.protocol.getDataType())) {
            List<String> commands = check.getPunishmentCommands();

            if (!commands.isEmpty()) {
                this.accumulatedTime[this.protocol.getDataType().ordinal()] = 0L;
                this.previousTime[this.protocol.getDataType().ordinal()] = -1L;
                int index = 0;
                boolean enabledDeveloperAPI = Config.settings.getBoolean("Important.enable_developer_api");

                for (String command : commands) {
                    String modifiedCommand = ConfigUtils.replaceWithSyntax(
                            this.protocol,
                            command.replaceAll("\\{detections}|\\{detection}", check.getName()),
                            null
                    );
                    commands.set(index++, modifiedCommand);

                    if (enabledDeveloperAPI) {
                        Runnable runnable = () -> {
                            PlayerViolationCommandEvent event = new PlayerViolationCommandEvent(
                                    this.protocol.bukkit(),
                                    hackType,
                                    modifiedCommand
                            );
                            Bukkit.getPluginManager().callEvent(event);

                            if (!event.isCancelled()) {
                                PluginBase.runCommand(modifiedCommand);
                            }
                        };

                        if (PluginBase.isSynchronised()) {
                            runnable.run();
                        } else {
                            PluginBase.transferTask(this.protocol, runnable);
                        }
                    } else {
                        PluginBase.runCommand(modifiedCommand);
                    }
                }
            }
        }
    }

    // Prevention

    public final boolean prevent() {
        if (this.prevention.complete()) {
            if (PluginBase.isSynchronised()
                    && Config.settings.getBoolean("Important.enable_developer_api")) {
                CheckCancelEvent checkCancelEvent = new CheckCancelEvent(this.protocol.bukkit(), hackType);
                Bukkit.getPluginManager().callEvent(checkCancelEvent);

                if (checkCancelEvent.isCancelled()) {
                    return false;
                } else {
                    this.prevention.handle(this);
                    return true;
                }
            } else {
                this.prevention.handle(this);
                return true;
            }
        } else {
            return false;
        }
    }

    protected Runnable prevention(Location location, boolean groundTeleport, double damage) {
        return () -> {
            if (location != null
                    || groundTeleport) {
                this.protocol.profile().getRunner(Enums.HackType.MorePackets).addDisableCause(
                        "Prevention from checks",
                        null,
                        5
                );
                if (location != null) {
                    protocol.teleport(location);
                }
                if (groundTeleport) {
                    protocol.groundTeleport();
                }
            }
            if (damage > 0.0) {
                protocol.bukkit().damage(damage);
            }
        };
    }

    // Cancel

    public final void cancel(String information, Location location,
                             int cancelTicks, boolean groundTeleport, double damage) {
        if (!this.executor.canCancel()
                || !this.executor.canRun()) {
            return;
        }
        CheckCancellation disableCause = this.executor.getDisableCause();

        if (disableCause != null
                && !disableCause.hasExpired()
                && disableCause.pointerMatches(information)) {
            return;
        }
        long time = System.currentTimeMillis();
        CheckPrevention newPrevention = new CheckPrevention(
                location,
                cancelTicks,
                groundTeleport,
                damage
        );
        boolean event = Config.settings.getBoolean("Important.enable_developer_api");
        CheckCancellation silentCause = this.executor.getSilentCause();

        Runnable runnable = () -> {
            if (event) {
                PlayerViolationEvent playerViolationEvent = new PlayerViolationEvent(
                        this.protocol.bukkit(),
                        hackType,
                        information
                );
                Bukkit.getPluginManager().callEvent(playerViolationEvent);

                if (playerViolationEvent.isCancelled()) {
                    return;
                }
            }
            // Store, potentially recalculate and check data
            this.storeData(
                    this.protocol.getDataType(),
                    time
            );
            this.increaseAccumulationTime(
                    this.protocol.getDataType(),
                    time
            );
            ResearchEngine.queueToCache(this.hackType, this.protocol.getDataType());
            int max = this.getPunishmentLevel(this.protocol.getDataType()),
                    notification = this.getNotificationLevel(this.protocol.getDataType()),
                    prevention = this.getPreventionLevel(this.protocol.getDataType()),
                    level = Math.min(this.getLevel(this.protocol.getDataType()), max);

            // Notification
            this.notify(level, notification, prevention, max, time, information);

            // Prevention & Punishment
            this.prevention = newPrevention;
            this.prevention.canPrevent =
                    level >= prevention
                            && !hackType.getCheck().isSilent(this.protocol.getDataType(), this.protocol.getWorld().getName())
                            && (silentCause == null
                            || silentCause.hasExpired()
                            || !silentCause.pointerMatches(information));

            if (this.prevention.hasDetails()) {
                this.prevent();
            }
            this.punish(level, max);
        };

        if (PluginBase.isSynchronised() || !event) {
            runnable.run();
        } else {
            PluginBase.transferTask(this.protocol, runnable);
        }
    }

    public final void cancel(String information, Location location,
                             int cancelTicks, boolean groundTeleport) {
        cancel(information, location, cancelTicks, groundTeleport, 0.0);
    }

    public final void cancel(String information, Location location,
                             int cancelTicks) {
        cancel(information, location, cancelTicks, false, 0.0);
    }

    public final void cancel(String information, Location location) {
        cancel(information, location, 0, false, 0.0);
    }

    public final void cancel(String information) {
        cancel(information, null, 0, false, 0.0);
    }

}
