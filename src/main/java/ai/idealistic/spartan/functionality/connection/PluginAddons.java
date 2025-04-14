package ai.idealistic.spartan.functionality.connection;

import ai.idealistic.spartan.Register;
import ai.idealistic.spartan.abstraction.check.Check;
import ai.idealistic.spartan.abstraction.check.CheckEnums;
import ai.idealistic.spartan.functionality.server.Config;
import ai.idealistic.spartan.functionality.server.PluginBase;
import ai.idealistic.spartan.utils.java.ReflectionUtils;
import ai.idealistic.spartan.utils.math.AlgebraUtils;
import lombok.Getter;
import org.bukkit.Bukkit;

import java.io.File;

public class PluginAddons {

    public static final String
            patreonURL = "https:/www.patreon.com/SpartanAntiCheat",
            disabledInFreeEdition = "Cannot be modified in free edition";
    private static final boolean[]
            ownedChecks = new boolean[CheckEnums.HackType.values().length],
            ownsEditions = new boolean[Check.DataType.values().length];
    @Getter
    private static boolean freeEdition = true;

    public static void refresh() {
        if (false) { // Addons
            disableFreeEdition();

            for (Check.DataType dataType : Check.DataType.values()) {
                ownsEditions[dataType.ordinal()] = true;
            }
            for (CheckEnums.HackType hackType : CheckEnums.HackType.values()) {
                if (hackType.category == CheckEnums.HackCategoryType.MISCELLANEOUS) {
                    ownedChecks[hackType.ordinal()] = true;
                }
            }
            PluginBase.dataThread.executeIfUnknownThreadElseHere(() -> {
                File directory = new File(Register.plugin.getDataFolder() + "/addons");

                if (directory.isFile()) {
                    directory.delete();
                }
                directory.mkdirs();
                File[] files = directory.listFiles();

                if (files != null
                        && files.length > 0) {
                    for (CheckEnums.HackType hackType : CheckEnums.HackType.values()) {
                        if (ownedChecks[hackType.ordinal()]) {
                            continue;
                        }
                        for (File file : files) {
                            if (file.isFile()
                                    && file.getName().endsWith(".jar")) {
                                Object object = ReflectionUtils.getFieldFromJar(
                                        file.getAbsolutePath(),
                                        Register.mainPackage + ".addon." + hackType.name(),
                                        "user"
                                );

                                if (object != null
                                        && (object.toString().equals(IDs.user)
                                        || !AlgebraUtils.validInteger(object.toString()))) {
                                    ownedChecks[hackType.ordinal()] = true;
                                    break;
                                }
                            }
                        }
                    }
                }
            });
        } else if (false) { // Multi jars
            disableFreeEdition();

            for (Check.DataType dataType : Check.DataType.values()) {
                ownsEditions[dataType.ordinal()] = true;
            }
            String combat = IDs.builtByBit ? "11196" : "350",
                    movement = IDs.builtByBit ? "12832" : "3600",
                    world = IDs.builtByBit ? "20142" : "984";

            if (IDs.resource.equals(combat)) {
                for (CheckEnums.HackType hackType : CheckEnums.HackType.values()) {
                    if (hackType.category == CheckEnums.HackCategoryType.COMBAT
                            || hackType.category == CheckEnums.HackCategoryType.MISCELLANEOUS) {
                        ownedChecks[hackType.ordinal()] = true;
                    }
                }
            } else if (IDs.resource.equals(movement)) {
                for (CheckEnums.HackType hackType : CheckEnums.HackType.values()) {
                    if (hackType.category == CheckEnums.HackCategoryType.MOVEMENT
                            || hackType.category == CheckEnums.HackCategoryType.MISCELLANEOUS) {
                        ownedChecks[hackType.ordinal()] = true;
                    }
                }
            } else if (IDs.resource.equals(world)) {
                for (CheckEnums.HackType hackType : CheckEnums.HackType.values()) {
                    if (hackType.category == CheckEnums.HackCategoryType.WORLD
                            || hackType.category == CheckEnums.HackCategoryType.MISCELLANEOUS) {
                        ownedChecks[hackType.ordinal()] = true;
                    }
                }
            }
            PluginBase.dataThread.executeIfUnknownThreadElseHere(() -> {
                String pluginsFolder = Register.plugin.getDataFolder().toString().replace(
                        Register.pluginName,
                        ""
                );

                for (File file : new File(pluginsFolder).listFiles()) {
                    if (file.isFile()
                            && file.getName().endsWith(".jar")) {
                        String clazz = IDs.class.getCanonicalName();
                        Object object = ReflectionUtils.getFieldFromJar(
                                file.getAbsolutePath(),
                                clazz,
                                "user"
                        );

                        if (object != null
                                && object.toString().equals(IDs.user)) {
                            object = ReflectionUtils.getFieldFromJar(
                                    file.getAbsolutePath(),
                                    clazz,
                                    "resource"
                            );

                            if (object != null
                                    && !object.toString().equals(IDs.resource)) {
                                if (object.toString().equals(combat)) {
                                    for (CheckEnums.HackType hackType : CheckEnums.HackType.values()) {
                                        if (hackType.category == CheckEnums.HackCategoryType.COMBAT) {
                                            ownedChecks[hackType.ordinal()] = true;
                                        }
                                    }
                                } else if (object.toString().equals(movement)) {
                                    for (CheckEnums.HackType hackType : CheckEnums.HackType.values()) {
                                        if (hackType.category == CheckEnums.HackCategoryType.MOVEMENT) {
                                            ownedChecks[hackType.ordinal()] = true;
                                        }
                                    }
                                } else if (object.toString().equals(world)) {
                                    for (CheckEnums.HackType hackType : CheckEnums.HackType.values()) {
                                        if (hackType.category == CheckEnums.HackCategoryType.WORLD) {
                                            ownedChecks[hackType.ordinal()] = true;
                                        }
                                    }
                                }
                            }
                            break;
                        }
                    }
                }
            });
        } else if (IDs.builtByBit
                || IDs.polymart
                || IDs.spigot) { // Premium Editions
            disableFreeEdition();

            for (CheckEnums.HackType hackType : CheckEnums.HackType.values()) {
                ownedChecks[hackType.ordinal()] = true;
            }
            String java = IDs.builtByBit ? "11196" : "350",
                    bedrock = IDs.builtByBit ? "12832" : "3600";

            if (IDs.resource.equals(java)) {
                ownsEditions[Check.DataType.JAVA.ordinal()] = true;
            } else if (IDs.resource.equals(bedrock)) {
                ownsEditions[Check.DataType.BEDROCK.ordinal()] = true;
            }
            PluginBase.dataThread.executeIfUnknownThreadElseHere(() -> {
                String pluginsFolder = Register.plugin.getDataFolder().toString().replace(
                        Register.pluginName,
                        ""
                );

                for (File file : new File(pluginsFolder).listFiles()) {
                    if (file.isFile()
                            && file.getName().endsWith(".jar")) {
                        String clazz = IDs.class.getCanonicalName();
                        Object object = ReflectionUtils.getFieldFromJar(
                                file.getAbsolutePath(),
                                clazz,
                                "user"
                        );

                        if (object != null
                                && object.toString().equals(IDs.user)) {
                            object = ReflectionUtils.getFieldFromJar(
                                    file.getAbsolutePath(),
                                    clazz,
                                    "resource"
                            );

                            if (object != null
                                    && !object.toString().equals(IDs.resource)) {
                                if (object.toString().equals(java)) {
                                    ownsEditions[Check.DataType.JAVA.ordinal()] = true;
                                } else if (object.toString().equals(bedrock)) {
                                    ownsEditions[Check.DataType.BEDROCK.ordinal()] = true;
                                }
                            }
                            break;
                        }
                    }
                }
            });
        } else if (!IDs.enabled) { // No DRM
            if (Bukkit.getMotd().contains(Register.pluginName)) { // Test Server
                freeEdition = false;

                for (CheckEnums.HackType hackType : CheckEnums.HackType.values()) {
                    ownedChecks[hackType.ordinal()] = true;
                }
                for (Check.DataType dataType : Check.DataType.values()) {
                    ownsEditions[dataType.ordinal()] = true;
                }
            } else {
                if (!ownsEdition(Check.DataType.JAVA)
                        || !ownsEdition(Check.DataType.BEDROCK)) { // Cloud
                    String[] editions = CloudConnections.getOwnedEditions();

                    if (editions != null) {
                        for (String edition : editions) {
                            for (Check.DataType dataType : Check.DataType.values()) {
                                if (edition.equals(dataType.toString())) {
                                    ownsEditions[dataType.ordinal()] = true;
                                    disableFreeEdition();
                                    break;
                                }
                            }
                        }
                    }
                }

                if (!freeEdition) { // Cloud success
                    for (CheckEnums.HackType hackType : CheckEnums.HackType.values()) {
                        ownedChecks[hackType.ordinal()] = true;
                    }
                } else { // Cloud failure
                    ownsEditions[Check.DataType.JAVA.ordinal()] = true;

                    for (CheckEnums.HackType hackType : CheckEnums.HackType.values()) {
                        switch (hackType) {
                            // Combat
                            case KILL_AURA:
                            case HIT_REACH:
                                // Movement
                            case SPEED_SIMULATION:
                            case GRAVITY_SIMULATION:
                                // World
                            case BLOCK_REACH:
                            case IMPOSSIBLE_ACTIONS:
                                // Misc
                            case AUTO_RESPAWN:
                            case FAST_HEAL:
                            case NO_SWING:
                                ownedChecks[hackType.ordinal()] = true;
                                break;
                            default:
                                ownedChecks[hackType.ordinal()] = false;
                                break;
                        }
                    }
                }
            }
        }
    }

    public static boolean ownsCheck(CheckEnums.HackType hackType) {
        return ownedChecks[hackType.ordinal()];
    }

    public static boolean ownsEdition(Check.DataType dataType) {
        return ownsEditions[dataType.ordinal()];
    }

    private static void disableFreeEdition() {
        freeEdition = false;
        Config.settings.create();
    }

}
