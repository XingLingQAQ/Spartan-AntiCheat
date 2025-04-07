package ai.idealistic.spartan.functionality.connection;

import ai.idealistic.spartan.Register;
import ai.idealistic.spartan.abstraction.check.Check;
import ai.idealistic.spartan.abstraction.check.CheckEnums;
import ai.idealistic.spartan.functionality.server.PluginBase;
import ai.idealistic.spartan.utils.java.ReflectionUtils;
import ai.idealistic.spartan.utils.math.AlgebraUtils;
import org.bukkit.Bukkit;

import java.io.File;

public class PluginAddons {

    private static final boolean[] ownedChecks = new boolean[CheckEnums.HackType.values().length];
    private static final boolean[] ownsEditions = new boolean[Check.DataType.values().length];

    public static void refresh() {
        if (false) { // Addons
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
                || IDs.spigot) { // Editions
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
        } else if (Bukkit.getMotd().contains(Register.pluginName)) {
            for (CheckEnums.HackType hackType : CheckEnums.HackType.values()) {
                ownedChecks[hackType.ordinal()] = true;
            }
            for (Check.DataType dataType : Check.DataType.values()) {
                ownsEditions[dataType.ordinal()] = true;
            }
        }
    }

    public static boolean ownsCheck(CheckEnums.HackType hackType) {
        return ownedChecks[hackType.ordinal()];
    }

    public static boolean ownsEdition(Check.DataType dataType) {
        return ownsEditions[dataType.ordinal()];
    }

}
