package ai.idealistic.spartan.abstraction.check;

import ai.idealistic.spartan.Register;
import ai.idealistic.spartan.api.CheckPunishmentToggleEvent;
import ai.idealistic.spartan.api.CheckSilentToggleEvent;
import ai.idealistic.spartan.api.CheckToggleEvent;
import ai.idealistic.spartan.api.ToggleAction;
import ai.idealistic.spartan.functionality.moderation.AwarenessNotifications;
import ai.idealistic.spartan.functionality.server.Config;
import ai.idealistic.spartan.functionality.tracking.ResearchEngine;
import ai.idealistic.spartan.utils.math.AlgebraUtils;
import ai.idealistic.spartan.utils.minecraft.server.ConfigUtils;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Check {

    // Static

    public enum DataType {
        JAVA, BEDROCK;

        private final String string;

        DataType() {
            switch (this.ordinal()) {
                case 0:
                    this.string = "Java";
                    break;
                default:
                    this.string = "Bedrock";
                    break;
            }
        }

        @Override
        public String toString() {
            return string;
        }
    }

    public enum DetectionType {
        BUKKIT, PACKETS;

        private final String string;

        DetectionType() {
            if (this.ordinal() == 0) {
                this.string = "Bukkit";
            } else {
                this.string = "Packets";
            }
        }

        @Override
        public String toString() {
            return string;
        }
    }

    private static boolean panic = false;
    public static final int maxCommands = 10;
    private static final File file = new File(
            Register.plugin.getDataFolder() + "/checks.yml"
    );

    private static final List<String> defaultPunishments = new ArrayList<>(Check.maxCommands);

    public static void setPanic(boolean panic) {
        Check.panic = panic;
        Config.create();
    }

    public static boolean getPanic() {
        return Check.panic;
    }

    static {
        defaultPunishments.add(Register.command + " kick {player} {detections}");

        for (int position = (defaultPunishments.size() + 1); position <= Check.maxCommands; position++) {
            defaultPunishments.add("");
        }
    }

    // Object

    public final CheckEnums.HackType hackType;
    @Getter
    private String name;
    private final Map<String, Object> options;
    public final boolean handleCancelledEvents;
    private final boolean[] enabled, silent, punish;
    private final Set<String>
            disabledWorlds,
            silentWorlds;
    private final Collection<String> punishments;

    // Object Methods

    public Check(CheckEnums.HackType hackType) {
        this.options = new ConcurrentHashMap<>();
        this.hackType = hackType;

        // Separator

        Object handleCancelledEvents = getOption("cancelled_event", false, false);

        String name = getOption("name", this.hackType.toString(), false).toString(),
                worlds_config = getOption("disabled_worlds", "exampleDisabledWorld1, exampleDisabledWorld2", false).toString(),
                silents_config = getOption("silent_worlds", "exampleSilentWorld1, exampleSilentWorld2", false).toString();

        // Separator
        this.enabled = new boolean[DataType.values().length];
        this.silent = new boolean[DataType.values().length];
        this.punish = new boolean[DataType.values().length];

        for (Check.DataType dataType : DataType.values()) {
            Object optionValue = getOption(
                    "enabled." + dataType.toString().toLowerCase(),
                    true,
                    false
            );
            boolean enabled = optionValue instanceof Boolean ? (boolean) optionValue :
                    optionValue instanceof Long || optionValue instanceof Integer || optionValue instanceof Short ? ((long) optionValue) > 0L :
                            optionValue instanceof Double || optionValue instanceof Float ? ((double) optionValue) > 0.0 :
                                    Boolean.parseBoolean(optionValue.toString().toLowerCase());
            this.enabled[dataType.ordinal()] = enabled;

            optionValue = getOption(
                    "silent." + dataType.toString().toLowerCase(),
                    false,
                    false
            );
            this.silent[dataType.ordinal()] = optionValue instanceof Boolean ? (boolean) optionValue :
                    optionValue instanceof Long || optionValue instanceof Integer || optionValue instanceof Short ? ((long) optionValue) > 0L :
                            optionValue instanceof Double || optionValue instanceof Float ? ((double) optionValue) > 0.0 :
                                    Boolean.parseBoolean(optionValue.toString().toLowerCase());
            optionValue = getOption(
                    "punishments.enabled." + dataType.toString().toLowerCase(),
                    true,
                    false
            );
            this.punish[dataType.ordinal()] = optionValue instanceof Boolean ? (boolean) optionValue :
                    optionValue instanceof Long || optionValue instanceof Integer || optionValue instanceof Short ? ((long) optionValue) > 0L :
                            optionValue instanceof Double || optionValue instanceof Float ? ((double) optionValue) > 0.0 :
                                    Boolean.parseBoolean(optionValue.toString().toLowerCase());
        }

        // Separator

        if (name != null) {
            if (name.length() > 32) {
                name = name.substring(0, 32);
            }
            this.name = name;
        } else {
            this.name = hackType.toString();
        }

        // Separator

        if (handleCancelledEvents != null) {
            if (handleCancelledEvents instanceof Boolean) {
                this.handleCancelledEvents = (boolean) handleCancelledEvents;
            } else if (handleCancelledEvents instanceof Long || handleCancelledEvents instanceof Integer || handleCancelledEvents instanceof Short) {
                this.handleCancelledEvents = ((long) handleCancelledEvents) > 0L;
            } else if (handleCancelledEvents instanceof Double || handleCancelledEvents instanceof Float) {
                this.handleCancelledEvents = ((double) handleCancelledEvents) > 0.0;
            } else {
                this.handleCancelledEvents = Boolean.parseBoolean(handleCancelledEvents.toString().toLowerCase());
            }
        } else {
            this.handleCancelledEvents = false;
        }

        // Separator

        if (worlds_config != null) {
            String[] worldsSplit = worlds_config.split(",");
            int size = worldsSplit.length;

            if (size > 0) {
                Set<String> set = new HashSet<>(size);

                for (String world : worldsSplit) {
                    world = world.replace(" ", "");

                    if (!world.isEmpty()) {
                        set.add(world.toLowerCase());
                    }
                }
                if (!set.isEmpty()) {
                    this.disabledWorlds = set;
                } else {
                    this.disabledWorlds = new HashSet<>(0);
                }
            } else {
                this.disabledWorlds = new HashSet<>(0);
            }
        } else {
            this.disabledWorlds = new HashSet<>(0);
        }

        // Separator

        if (silents_config != null) {
            String[] worldsSplit = silents_config.split(",");
            int size = worldsSplit.length;

            if (size > 0) {
                Set<String> set = new HashSet<>(size);

                for (String world : worldsSplit) {
                    world = world.replace(" ", "");

                    if (!world.isEmpty()) {
                        set.add(world.toLowerCase());
                    }
                }
                if (!set.isEmpty()) {
                    this.silentWorlds = set;
                } else {
                    this.silentWorlds = new HashSet<>(0);
                }
            } else {
                this.silentWorlds = new HashSet<>(0);
            }
        } else {
            this.silentWorlds = new HashSet<>(0);
        }

        // Separator

        String sectionString = hackType + ".punishments.commands";
        YamlConfiguration configuration = YamlConfiguration.loadConfiguration(file);

        if (!configuration.contains(sectionString)) {
            for (int position = 0; position < defaultPunishments.size(); position++) {
                ConfigUtils.add(file, sectionString + "." + (position + 1), defaultPunishments.get(position));
            }
        }
        ConfigurationSection section = configuration.getConfigurationSection(sectionString);

        if (section != null) {
            Collection<String> list = new ArrayList<>();
            Set<String> keys = section.getKeys(true);

            if (!keys.isEmpty()) {
                sectionString += ".";

                for (String key : keys) {
                    if (AlgebraUtils.validInteger(key)) {
                        int number = Integer.parseInt(key);

                        if (number >= 1 && number <= Check.maxCommands) {
                            String command = configuration.getString(sectionString + number);

                            if (command != null && !command.isEmpty()) {
                                list.add(command);
                            }
                        }
                    }
                }
            }
            this.punishments = list;
        } else {
            this.punishments = new ArrayList<>(0);
        }
    }

    // Separator

    public boolean isEnabled(Check.DataType dataType, String world) {
        if (dataType == null) {
            boolean enabled = false;

            for (Check.DataType type : DataType.values()) {
                if (this.enabled[type.ordinal()]) {
                    enabled = true;
                    break;
                }
            }
            if (!enabled) {
                return false;
            }
        } else if (!this.enabled[dataType.ordinal()]) {
            return false;
        }
        return world == null
                || !disabledWorlds.contains(world.toLowerCase());
    }

    public void setEnabled(Check.DataType dataType, boolean b) {
        Check.DataType[] dataTypes;

        if (dataType == null) {
            dataTypes = DataType.values();
        } else {
            dataTypes = null;

            for (Check.DataType type : DataType.values()) {
                if (type == dataType) {
                    dataTypes = new Check.DataType[]{dataType};
                    break;
                }
            }

            if (dataTypes == null) {
                return;
            }
        }
        for (Check.DataType type : dataTypes) {
            CheckToggleEvent event;

            if (Config.settings.getBoolean("Important.enable_developer_api")) {
                event = new CheckToggleEvent(this.hackType, b ? ToggleAction.ENABLE : ToggleAction.DISABLE);
                Bukkit.getPluginManager().callEvent(event);
            } else {
                event = null;
            }

            if (event == null || !event.isCancelled()) {
                this.enabled[type.ordinal()] = b;
                ResearchEngine.queueToCache(this.hackType, type);
                setOption("enabled." + type.toString().toLowerCase(), b);
                options.clear();
            }
        }
    }

    // Separator

    public void setName(String name) {
        if (name.length() > 32) {
            name = name.substring(0, 32);
        }
        for (CheckEnums.HackType hackType : CheckEnums.HackType.values()) {
            if (!hackType.equals(this.hackType)
                    && (name.equalsIgnoreCase(hackType.toString()))
                    || name.equalsIgnoreCase(hackType.getCheck().getName())) {
                return;
            }
        }
        this.name = name;
        setOption("name", name);
    }

    // Separator

    private void setOption(String option, Object value) {
        try {
            String key = this.hackType + "." + option;
            YamlConfiguration configuration = YamlConfiguration.loadConfiguration(file);
            configuration.set(key, value);

            try {
                configuration.save(file);
                options.remove(key); // Remove instead of modifying to be on demand and have the chance to catch changes by the user
            } catch (Exception ex) {
                AwarenessNotifications.forcefullySend("Failed to store '" + key + "' option in '" + file.getName() + "' file.");
                ex.printStackTrace();
            }
            return;
        } catch (Exception ex) {
            AwarenessNotifications.forcefullySend("Failed to find/create the '" + file.getName() + "' file.");
            ex.printStackTrace();
        }
        AwarenessNotifications.forcefullySend("Failed to find/create the '" + file.getName() + "' file.");
    }

    public Object getOption(String option, Object def, boolean cache) {
        if (cache) {
            Object cached = options.get(option);

            if (cached != null) {
                return cached;
            }
        }
        try {
            String key = this.hackType + "." + option;
            boolean isDefaultNull = def == null;
            YamlConfiguration configuration = YamlConfiguration.loadConfiguration(file);

            if (cache) {
                if (configuration.contains(key)) {
                    Object value = configuration.get(key, def);

                    if (!isDefaultNull) {
                        options.put(option, value);
                    }
                    return value;
                }
                if (!isDefaultNull) {
                    configuration.set(key, def);

                    try {
                        configuration.save(file);
                        options.put(option, def);
                    } catch (Exception ex) {
                        AwarenessNotifications.forcefullySend("Failed to store '" + key + "' option in '" + file.getName() + "' file.");
                        ex.printStackTrace();
                    }
                }
            } else {
                if (configuration.contains(key)) {
                    return configuration.get(key, def);
                } else if (!isDefaultNull) {
                    configuration.set(key, def);

                    try {
                        configuration.save(file);
                    } catch (Exception ex) {
                        AwarenessNotifications.forcefullySend("Failed to store '" + key + "' option in '" + file.getName() + "' file.");
                        ex.printStackTrace();
                    }
                }
            }
        } catch (Exception ex) {
            AwarenessNotifications.forcefullySend("Failed to find/create the '" + file.getName() + "' file.");
            ex.printStackTrace();
        }
        return def;
    }

    public boolean getBooleanOption(String option, Boolean def) {
        Object object = getOption(option, def, true);
        return object instanceof Boolean ? (boolean) object :
                object instanceof String ? Boolean.parseBoolean(object.toString().toLowerCase()) :
                        object instanceof Long || object instanceof Integer || object instanceof Short ? ((long) object) > 0L :
                                object instanceof Double || object instanceof Float ? ((double) object) > 0.0 :
                                        def != null && def;
    }

    public int getNumericalOption(String option, int def) {
        Object object = getOption(option, def, true);

        if (object instanceof Integer || object instanceof Short) {
            return (int) object;
        } else if (object instanceof String || object instanceof Long) {
            try {
                return Integer.parseInt(object.toString());
            } catch (Exception ex) {
                return def;
            }
        } else if (object instanceof Double || object instanceof Float) {
            try {
                return AlgebraUtils.integerRound(Double.parseDouble(object.toString()));
            } catch (Exception ex) {
                return def;
            }
        } else {
            return def;
        }
    }

    public double getDecimalOption(String option, double def) {
        Object object = getOption(option, def, true);

        if (object instanceof Double || object instanceof Float) {
            return (double) object;
        } else if (object instanceof Long) {
            return ((Long) object).doubleValue();
        } else if (object instanceof Integer) {
            return ((Integer) object).doubleValue();
        } else if (object instanceof Short) {
            return ((Short) object).doubleValue();
        } else if (object instanceof String) {
            try {
                return Double.parseDouble(object.toString());
            } catch (Exception ex) {
                return def;
            }
        } else {
            return def;
        }
    }

    // Separator

    public boolean canPunish(Check.DataType dataType) {
        if (panic) {
            return false;
        }
        if (dataType == null) {
            for (Check.DataType type : DataType.values()) {
                if (this.punish[type.ordinal()]) {
                    return true;
                }
            }
            return false;
        } else {
            return this.punish[dataType.ordinal()];
        }
    }

    // Separator

    public boolean isSilent(Check.DataType dataType, String world) {
        if (panic
                || world != null && silentWorlds.contains(world.toLowerCase())) {
            return true;
        }
        if (dataType == null) {
            for (Check.DataType type : DataType.values()) {
                if (this.silent[type.ordinal()]) {
                    return true;
                }
            }
            return false;
        } else return this.silent[dataType.ordinal()];
    }

    public void setSilent(Check.DataType dataType, boolean b) {
        Check.DataType[] dataTypes;

        if (dataType == null) {
            dataTypes = DataType.values();
        } else {
            dataTypes = null;

            for (Check.DataType type : DataType.values()) {
                if (type == dataType) {
                    dataTypes = new Check.DataType[]{dataType};
                    break;
                }
            }

            if (dataTypes == null) {
                return;
            }
        }
        for (Check.DataType type : dataTypes) {
            CheckSilentToggleEvent event;

            if (Config.settings.getBoolean("Important.enable_developer_api")) {
                event = new CheckSilentToggleEvent(this.hackType, b ? ToggleAction.ENABLE : ToggleAction.DISABLE);
                Bukkit.getPluginManager().callEvent(event);
            } else {
                event = null;
            }

            if (event == null || !event.isCancelled()) {
                this.silent[type.ordinal()] = b;
                setOption("silent." + type.toString().toLowerCase(), b);
                options.clear();
            }
        }
    }

    public List<String> getPunishmentCommands() {
        return new ArrayList<>(this.punishments);
    }

    public void setPunish(Check.DataType dataType, boolean b) {
        Check.DataType[] dataTypes;

        if (dataType == null) {
            dataTypes = DataType.values();
        } else {
            dataTypes = new Check.DataType[]{dataType};
        }
        for (Check.DataType type : dataTypes) {
            CheckPunishmentToggleEvent event;

            if (Config.settings.getBoolean("Important.enable_developer_api")) {
                event = new CheckPunishmentToggleEvent(this.hackType, b ? ToggleAction.ENABLE : ToggleAction.DISABLE);
                Bukkit.getPluginManager().callEvent(event);
            } else {
                event = null;
            }

            if (event == null || !event.isCancelled()) {
                this.punish[type.ordinal()] = b;
                setOption("punishments.enabled." + type.toString().toLowerCase(), b);
                options.clear();
            }
        }
    }

}
