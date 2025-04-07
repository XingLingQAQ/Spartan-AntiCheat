package ai.idealistic.spartan.functionality.command;

import ai.idealistic.spartan.Register;
import ai.idealistic.spartan.abstraction.check.Check;
import ai.idealistic.spartan.abstraction.check.CheckEnums;
import ai.idealistic.spartan.abstraction.protocol.PlayerProtocol;
import ai.idealistic.spartan.api.Permission;
import ai.idealistic.spartan.functionality.connection.DiscordServer;
import ai.idealistic.spartan.functionality.moderation.DetectionNotifications;
import ai.idealistic.spartan.functionality.moderation.Wave;
import ai.idealistic.spartan.functionality.moderation.clickable.ClickableMessage;
import ai.idealistic.spartan.functionality.server.Config;
import ai.idealistic.spartan.functionality.server.Permissions;
import ai.idealistic.spartan.functionality.server.PluginBase;
import ai.idealistic.spartan.functionality.server.TPS;
import ai.idealistic.spartan.listeners.NPCManager;
import ai.idealistic.spartan.utils.java.StringUtils;
import ai.idealistic.spartan.utils.math.AlgebraUtils;
import ai.idealistic.spartan.utils.minecraft.server.ConfigUtils;
import ai.idealistic.spartan.utils.minecraft.server.ProxyUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CommandExecution implements CommandExecutor {

    public static final int maxConnectedArgumentLength = 4096;
    public static final String support = "Click to receive plugin support.";

    private static void buildCommand(CommandSender sender, ChatColor chatColor, String command, String description) {
        ClickableMessage.sendCommand(sender, chatColor + command, description, command);
    }

    public static boolean defaultMessage(CommandSender sender,
                                         boolean isPlayer,
                                         boolean documentation) {
        if (!isPlayer || Permissions.has((Player) sender)) {
            sender.sendMessage("");
            String command = "§2" + Register.pluginName + " AntiCheat";

            sender.sendMessage(command);
            if (documentation) {
                sender.sendMessage("§8§l<> §7Required command argument");
                sender.sendMessage("§8§l[] §7Optional command argument");
            }
            return true;
        }
        sender.sendMessage(Config.messages.getColorfulString("unknown_command"));
        return false;
    }

    public static void completeMessage(CommandSender sender, String list) {
        boolean isPlayer = sender instanceof Player;
        PlayerProtocol protocol = isPlayer ? PluginBase.getProtocol((Player) sender) : null;
        isPlayer &= protocol != null;

        boolean info = !isPlayer || Permissions.has(protocol.bukkit(), Permission.INFO),
                manage = !isPlayer || Permissions.has(protocol.bukkit(), Permission.MANAGE);

        switch (list) {
            case "default":
                if (defaultMessage(sender, isPlayer, !isPlayer)) {
                    if (isPlayer) {
                        if (manage) {
                            ClickableMessage.sendCommand(
                                    sender,
                                    "§cPanic Mode §7(Click)",
                                    "Click this command to toggle silent mode and disable punishments for all checks.",
                                    "/" + Register.command + " panic"
                            );
                        }
                        if (Permissions.has(protocol.bukkit(), Permission.RELOAD)) {
                            ClickableMessage.sendCommand(
                                    sender,
                                    "§cReload Plugin §7(Click)",
                                    "Click this command to reload the plugin's cache.",
                                    "/" + Register.command + " reload"
                            );
                        }
                        if (info) {
                            ClickableMessage.sendCommand(
                                    sender,
                                    "§cPlayer Info §7(Click)",
                                    "Click this command to view useful information yourself.",
                                    "/" + Register.command + " info"
                            );
                        }
                        if (manage) {
                            ClickableMessage.sendCommand(
                                    sender,
                                    "§cToggle Checks §7(Click)",
                                    "Click this command to toggle a check and its detections.",
                                    "/" + Register.command + " manage-checks"
                            );
                            ClickableMessage.sendCommand(
                                    sender,
                                    "§cToggle Preventions §7(Click)",
                                    "Click this command to toggle a check's preventions.",
                                    "/" + Register.command + " manage-checks"
                            );
                            ClickableMessage.sendCommand(
                                    sender,
                                    "§cToggle Punishments §7(Click)",
                                    "Click this command to toggle a check's punishments.",
                                    "/" + Register.command + " manage-checks"
                            );
                        }
                        if (Permissions.has(protocol.bukkit(), Permission.USE_BYPASS)) {
                            ClickableMessage.sendCommand(
                                    sender,
                                    "§cPlayer Bypass §7(Click)",
                                    "Click this command to give check bypass to a player.",
                                    "/" + Register.command + " bypass *"
                            );
                        }
                    } else {
                        ClickableMessage.sendCommand(
                                sender,
                                ChatColor.RED + "/" + Register.command + " panic",
                                "This command can be used to enable silent mode and disable punishments for all checks.",
                                null
                        );
                        ClickableMessage.sendCommand(
                                sender,
                                ChatColor.RED + "/" + Register.command + " toggle <check>",
                                "This command can be used to toggle a check and its detections.",
                                null
                        );
                        ClickableMessage.sendCommand(
                                sender,
                                ChatColor.RED + "/" + Register.command + " toggle-prevention <check>",
                                "This command can be used to toggle a check's preventions.",
                                null
                        );
                        ClickableMessage.sendCommand(
                                sender,
                                ChatColor.RED + "/" + Register.command + " toggle-punishment <check>",
                                "This command can be used to toggle a check's punishments.",
                                null
                        );
                        buildCommand(
                                sender,
                                ChatColor.RED, "/" + Register.command + " reload",
                                "Click this command to reload the plugin's cache."
                        );
                    }
                    if (!isPlayer
                            || info
                            || Permissions.has(protocol.bukkit(), Permission.KICK)
                            || Permissions.has(protocol.bukkit(), Permission.WARN)
                            || Permissions.has(protocol.bukkit(), Permission.USE_BYPASS)
                            || Permissions.has(protocol.bukkit(), Permission.WAVE)) {
                        buildCommand(
                                sender,
                                ChatColor.RED, "/" + Register.command + " moderation",
                                "Click this command to view a list of moderation commands."
                        );
                    }
                }
                break;
            case "moderation":
                if (defaultMessage(sender, isPlayer, true)) {
                    boolean permission = false;

                    if (isPlayer && DetectionNotifications.hasPermission(protocol)) {
                        ClickableMessage.sendCommand(
                                sender,
                                ChatColor.RED + "/" + Register.command + " notifications [ticks-frequency]",
                                "This command can be used to receive chat messages whenever a player is suspected of using hack modules.", null);
                        ClickableMessage.sendCommand(
                                sender,
                                ChatColor.RED + "/" + Register.command + " verbose",
                                "This command can be used to enable all notifications to go through instead of only important ones when disabled.", null);
                    }
                    if (isPlayer && info) {
                        ClickableMessage.sendCommand(
                                sender,
                                ChatColor.RED + "/" + Register.command + " info [player]",
                                "This command can be used to view useful information about a player.",
                                null
                        );
                    }
                    if (!isPlayer
                            || Permissions.has(protocol.bukkit(), Permission.USE_BYPASS)) {
                        permission = true;
                        ClickableMessage.sendCommand(
                                sender,
                                ChatColor.RED + "/" + Register.command + " bypass <player> <check> [seconds]",
                                "This command can be used to cause a player to temporarily bypass a check and its detections.",
                                null
                        );
                    }
                    if (!isPlayer
                            || Permissions.has(protocol.bukkit(), Permission.WARN)) {
                        permission = true;
                        ClickableMessage.sendCommand(
                                sender,
                                ChatColor.RED + "/" + Register.command + " warn <player> <reason>",
                                "This command can be used to individually warn a player about something important.",
                                null
                        );
                    }
                    if (!isPlayer
                            || Permissions.has(protocol.bukkit(), Permission.KICK)) {
                        permission = true;
                        ClickableMessage.sendCommand(
                                sender,
                                ChatColor.RED + "/" + Register.command + " kick <player> <reason>",
                                "This command can be used to kick players from the server for a specific reason.",
                                null
                        );
                    }
                    if (!isPlayer
                            || Permissions.has(protocol.bukkit(), Permission.WAVE)) {
                        permission = true;
                        ClickableMessage.sendCommand(
                                sender,
                                ChatColor.RED + "/" + Register.command + " wave <add/remove/clear/run/list> [player] [command]",
                                "This command can be used to add a player to a list with a command representing their punishment. " +
                                        "This list can be executed manually by a player or automatically based on the plugin's configuration, " +
                                        "and cause added players to punished all at once and in order."
                                        + "\n\n"
                                        + "Example: /" + Register.command + " wave add playerName ban {player} You have been banned for hacking!",
                                null
                        );
                    }
                    if (!isPlayer
                            || Permissions.has(protocol.bukkit(), Permission.ADMIN)) {
                        permission = true;
                        ClickableMessage.sendCommand(
                                sender,
                                ChatColor.RED + "/" + Register.command + " proxy-command <command>",
                                "This command can be used to transfer commands to the proxy/network of servers. (Example: BungeeCord)",
                                null
                        );
                    }

                    if (!permission) {
                        completeMessage(sender, "default");
                    }
                }
                break;
            case "conditions":
                if (defaultMessage(sender, isPlayer, true)) {
                    if (!isPlayer || Permissions.has(protocol.bukkit(), Permission.CONDITION)) {
                        sender.sendMessage(ChatColor.RED + "/" + Register.command + " <player> if <condition> equals <result> do <command>");
                        sender.sendMessage(ChatColor.RED + "/" + Register.command + " <player> if <condition> contains <result> do <command>");
                        sender.sendMessage(ChatColor.RED + "/" + Register.command + " <player> if <number> is-less-than <result> do <command>");
                        sender.sendMessage(ChatColor.RED + "/" + Register.command + " <player> if <number> is-greater-than <result> do <command>");
                    } else {
                        completeMessage(sender, "default");
                    }
                }
                break;
            default:
                break;
        }
    }

    public static int num(final String s) {
        return Integer.parseInt(s);
    }

    public static double dbl(final String s) {
        return Double.parseDouble(s);
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        boolean isPlayer = sender instanceof Player;

        if (label.equalsIgnoreCase(Register.command) && (isPlayer || sender instanceof ConsoleCommandSender)) {
            PlayerProtocol protocol = isPlayer ? PluginBase.getProtocol((Player) sender) : null;

            if (args.length == 0) {
                if (isPlayer) {
                    if (NPCManager.supported
                            && Permissions.isStaff(protocol.bukkit())
                            && Config.settings.getBoolean("Important.enable_npc")) {
                        NPCManager.create(protocol);
                    }
                }
                completeMessage(sender, "default");
            } else if (args.length == 1) {
                if (isPlayer && args[0].equalsIgnoreCase("Manage-Checks")) {
                    PluginBase.manageChecks.open(protocol);
                } else if (args[0].equalsIgnoreCase("Panic")) {
                    if (isPlayer && !Permissions.has(protocol.bukkit(), Permission.MANAGE)) {
                        ClickableMessage.sendURL(
                                sender,
                                Config.messages.getColorfulString("no_permission"),
                                support,
                                DiscordServer.url
                        );
                        return true;
                    }
                    Check.setPanic(!Check.getPanic());

                    if (Check.getPanic()) {
                        ClickableMessage.sendURL(
                                sender,
                                Config.messages.getColorfulString("panic_mode_enable"),
                                support,
                                DiscordServer.url
                        );
                    } else {
                        ClickableMessage.sendURL(
                                sender,
                                Config.messages.getColorfulString("panic_mode_disable"),
                                support,
                                DiscordServer.url
                        );
                    }
                } else if (args[0].equalsIgnoreCase("Moderation")) {
                    completeMessage(sender, args[0].toLowerCase());

                } else if (args[0].equalsIgnoreCase("Conditions")) {
                    completeMessage(sender, args[0].toLowerCase());

                } else if (args[0].equalsIgnoreCase("Reload") || args[0].equalsIgnoreCase("Rl")) {
                    if (isPlayer && !Permissions.has(protocol.bukkit(), Permission.RELOAD)) {
                        ClickableMessage.sendURL(
                                sender,
                                Config.messages.getColorfulString("no_permission"),
                                support,
                                DiscordServer.url
                        );
                        return true;
                    }
                    Config.reload(sender);

                } else if (isPlayer && args[0].equalsIgnoreCase("Info")) {
                    if (!Permissions.has(protocol.bukkit(), Permission.INFO)) {
                        ClickableMessage.sendURL(
                                sender,
                                Config.messages.getColorfulString("no_permission"),
                                support,
                                DiscordServer.url
                        );
                        return true;
                    }
                    PluginBase.playerInfo.open(protocol, sender.getName());

                } else if (isPlayer && args[0].equalsIgnoreCase("Notifications")) {
                    if (!DetectionNotifications.hasPermission(protocol)) {
                        ClickableMessage.sendURL(
                                sender,
                                Config.messages.getColorfulString("no_permission"),
                                support,
                                DiscordServer.url
                        );
                        return true;
                    }
                    DetectionNotifications.set(protocol, DetectionNotifications.defaultFrequency);

                } else if (isPlayer && args[0].equalsIgnoreCase("Verbose")) {
                    if (!DetectionNotifications.hasPermission(protocol)) {
                        ClickableMessage.sendURL(
                                sender,
                                Config.messages.getColorfulString("no_permission"),
                                support,
                                DiscordServer.url
                        );
                        return true;
                    }
                    if (DetectionNotifications.isVerboseEnabled(protocol)) {
                        DetectionNotifications.removeVerbose(protocol);
                    } else {
                        DetectionNotifications.addVerbose(protocol);
                    }

                } else {
                    completeMessage(sender, "default");
                }
            } else {
                if (args[0].equalsIgnoreCase("Proxy-Command")) {
                    if (isPlayer && !Permissions.has(protocol.bukkit(), Permission.ADMIN)) {
                        ClickableMessage.sendURL(
                                sender,
                                Config.messages.getColorfulString("no_permission"),
                                support,
                                DiscordServer.url
                        );
                        return true;
                    }
                    StringBuilder argumentsToStringBuilder = new StringBuilder();
                    for (int i = 1; i < args.length; i++) {
                        argumentsToStringBuilder.append(args[i]).append(" ");
                    }
                    String argumentsToString = argumentsToStringBuilder.substring(0, argumentsToStringBuilder.length() - 1);

                    if (isPlayer ? argumentsToString.length() > protocol.getMaxChatLength() : argumentsToString.length() > maxConnectedArgumentLength) {
                        ClickableMessage.sendURL(
                                sender,
                                Config.messages.getColorfulString("massive_command_reason"),
                                support,
                                DiscordServer.url
                        );
                        return true;
                    }
                    if (!ProxyUtils.executeCommand(isPlayer ? protocol.bukkit() : null, argumentsToString)) {
                        ClickableMessage.sendURL(
                                sender,
                                Config.messages.getColorfulString("failed_command").replace(
                                        "{command}",
                                        StringUtils.toString(args, " ")
                                ),
                                support,
                                DiscordServer.url
                        );
                        return true;
                    }
                    ClickableMessage.sendURL(
                            sender,
                            Config.messages.getColorfulString("successful_command"),
                            support,
                            DiscordServer.url
                    );
                } else {
                    if (args.length == 2) {
                        if (args[0].equalsIgnoreCase("Wave")) {
                            String command = args[1];

                            if (isPlayer && !Permissions.has(protocol.bukkit(), Permission.WAVE)) {
                                ClickableMessage.sendURL(
                                        sender,
                                        Config.messages.getColorfulString("no_permission"),
                                        support,
                                        DiscordServer.url
                                );
                                return true;
                            }
                            if (command.equalsIgnoreCase("Run")) {
                                if (Wave.getWaveList().length == 0) {
                                    ClickableMessage.sendURL(
                                            sender,
                                            Config.messages.getColorfulString("empty_wave_list"),
                                            support,
                                            DiscordServer.url
                                    );
                                    return true;
                                }
                                if (!Wave.start()) {
                                    ClickableMessage.sendURL(
                                            sender,
                                            Config.messages.getColorfulString("failed_command").replace(
                                                    "{command}",
                                                    StringUtils.toString(args, " ")
                                            ),
                                            support,
                                            DiscordServer.url
                                    );
                                }
                            } else if (command.equalsIgnoreCase("Clear")) {
                                Wave.clear();
                                ClickableMessage.sendURL(
                                        sender,
                                        Config.messages.getColorfulString("wave_clear_message"),
                                        support,
                                        DiscordServer.url
                                );
                            } else if (command.equalsIgnoreCase("List")) {
                                sender.sendMessage(ChatColor.GRAY + "Wave Queued Players" + ChatColor.DARK_GRAY + ":");
                                sender.sendMessage(Wave.getWaveListString());
                            } else {
                                completeMessage(sender, "moderation");
                            }

                        } else if (isPlayer && args[0].equalsIgnoreCase("Info")) {
                            if (!Permissions.has(protocol.bukkit(), Permission.INFO)) {
                                ClickableMessage.sendURL(
                                        sender,
                                        Config.messages.getColorfulString("no_permission"),
                                        support,
                                        DiscordServer.url
                                );
                                return true;
                            }
                            PluginBase.playerInfo.open(protocol, ConfigUtils.replaceWithSyntax(args[1], null));

                        } else if (args[0].equalsIgnoreCase("Toggle")) {
                            String check = args[1];

                            if (isPlayer && !Permissions.has(protocol.bukkit(), Permission.MANAGE)) {
                                ClickableMessage.sendURL(
                                        sender,
                                        Config.messages.getColorfulString("no_permission"),
                                        support,
                                        DiscordServer.url
                                );
                                return true;
                            }
                            boolean exists = false;

                            for (CheckEnums.HackType hackType : CheckEnums.HackType.values()) {
                                if (hackType.getCheck().getName().equalsIgnoreCase(check)) {
                                    check = hackType.toString();
                                    exists = true;
                                    break;
                                }
                            }
                            if (exists) {
                                CheckEnums.HackType type = CheckEnums.HackType.valueOf(check);
                                Check checkObj = type.getCheck();

                                if (checkObj.isEnabled(null, null)) {
                                    checkObj.setEnabled(null, false);
                                    String message = Config.messages.getColorfulString("check_disable_message");
                                    message = isPlayer
                                            ? ConfigUtils.replaceWithSyntax((Player) sender, message, type)
                                            : ConfigUtils.replaceWithSyntax(message, type);
                                    sender.sendMessage(message);
                                } else {
                                    checkObj.setEnabled(null, true);
                                    String message = Config.messages.getColorfulString("check_enable_message");
                                    message = isPlayer
                                            ? ConfigUtils.replaceWithSyntax((Player) sender, message, type)
                                            : ConfigUtils.replaceWithSyntax(message, type);
                                    sender.sendMessage(message);
                                }
                            } else {
                                ClickableMessage.sendURL(
                                        sender,
                                        Config.messages.getColorfulString("non_existing_check"),
                                        support,
                                        DiscordServer.url
                                );
                            }
                        } else if (args[0].equalsIgnoreCase("Toggle-Prevention")) {
                            String check = args[1];

                            if (isPlayer && !Permissions.has(protocol.bukkit(), Permission.MANAGE)) {
                                ClickableMessage.sendURL(
                                        sender,
                                        Config.messages.getColorfulString("no_permission"),
                                        support,
                                        DiscordServer.url
                                );
                                return true;
                            }
                            boolean exists = false;

                            for (CheckEnums.HackType hackType : CheckEnums.HackType.values()) {
                                if (hackType.getCheck().getName().equalsIgnoreCase(check)) {
                                    check = hackType.toString();
                                    exists = true;
                                    break;
                                }
                            }
                            if (exists) {
                                CheckEnums.HackType type = CheckEnums.HackType.valueOf(check);
                                Check checkObj = type.getCheck();

                                if (checkObj.isSilent(null, null)) {
                                    checkObj.setSilent(null, false);
                                    String message = Config.messages.getColorfulString("check_silent_disable_message");
                                    message = isPlayer
                                            ? ConfigUtils.replaceWithSyntax((Player) sender, message, type)
                                            : ConfigUtils.replaceWithSyntax(message, type);
                                    sender.sendMessage(message);
                                } else {
                                    checkObj.setSilent(null, true);
                                    String message = Config.messages.getColorfulString("check_silent_enable_message");
                                    message = isPlayer
                                            ? ConfigUtils.replaceWithSyntax((Player) sender, message, type)
                                            : ConfigUtils.replaceWithSyntax(message, type);
                                    sender.sendMessage(message);
                                }
                            } else {
                                ClickableMessage.sendURL(
                                        sender,
                                        Config.messages.getColorfulString("non_existing_check"),
                                        support,
                                        DiscordServer.url
                                );
                            }
                        } else if (args[0].equalsIgnoreCase("Toggle-Punishment")) {
                            String check = args[1];

                            if (isPlayer && !Permissions.has(protocol.bukkit(), Permission.MANAGE)) {
                                ClickableMessage.sendURL(
                                        sender,
                                        Config.messages.getColorfulString("no_permission"),
                                        support,
                                        DiscordServer.url
                                );
                                return true;
                            }
                            boolean exists = false;

                            for (CheckEnums.HackType hackType : CheckEnums.HackType.values()) {
                                if (hackType.getCheck().getName().equalsIgnoreCase(check)) {
                                    check = hackType.toString();
                                    exists = true;
                                    break;
                                }
                            }
                            if (exists) {
                                CheckEnums.HackType type = CheckEnums.HackType.valueOf(check);
                                Check checkObj = type.getCheck();

                                if (checkObj.canPunish(null)) {
                                    checkObj.setPunish(null, false);
                                    String message = Config.messages.getColorfulString("check_punishment_disable_message");
                                    message = isPlayer
                                            ? ConfigUtils.replaceWithSyntax((Player) sender, message, type)
                                            : ConfigUtils.replaceWithSyntax(message, type);
                                    sender.sendMessage(message);
                                } else {
                                    checkObj.setPunish(null, true);
                                    String message = Config.messages.getColorfulString("check_punishment_enable_message");
                                    message = isPlayer
                                            ? ConfigUtils.replaceWithSyntax((Player) sender, message, type)
                                            : ConfigUtils.replaceWithSyntax(message, type);
                                    sender.sendMessage(message);
                                }
                            } else {
                                ClickableMessage.sendURL(
                                        sender,
                                        Config.messages.getColorfulString("non_existing_check"),
                                        support,
                                        DiscordServer.url
                                );
                            }

                        } else if (isPlayer && args[0].equalsIgnoreCase("Notifications")) {
                            if (!DetectionNotifications.hasPermission(protocol)) {
                                ClickableMessage.sendURL(
                                        sender,
                                        Config.messages.getColorfulString("no_permission"),
                                        support,
                                        DiscordServer.url
                                );
                                return true;
                            }
                            String divisorString = args[1];

                            if (AlgebraUtils.validInteger(divisorString)) {
                                int frequency = Integer.parseInt(divisorString);

                                if (frequency >= 0) {
                                    DetectionNotifications.set(protocol, frequency);
                                } else {
                                    ClickableMessage.sendURL(
                                            sender,
                                            Config.messages.getColorfulString("failed_command").replace(
                                                    "{command}",
                                                    StringUtils.toString(args, " ")
                                            ),
                                            support,
                                            DiscordServer.url
                                    );
                                }
                            } else {
                                ClickableMessage.sendURL(
                                        sender,
                                        Config.messages.getColorfulString("failed_command").replace(
                                                "{command}",
                                                StringUtils.toString(args, " ")
                                        ),
                                        support,
                                        DiscordServer.url
                                );
                            }

                        } else {
                            completeMessage(sender, "default");
                        }
                    } else { // 3 or more arguments
                        StringBuilder argumentsToStringBuilder = new StringBuilder();
                        for (int i = 2; i < args.length; i++) {
                            argumentsToStringBuilder.append(args[i]).append(" ");
                        }
                        String argumentsToString = argumentsToStringBuilder.substring(0, argumentsToStringBuilder.length() - 1);

                        if (args[0].equalsIgnoreCase("Kick")) {
                            if (isPlayer && !Permissions.has(protocol.bukkit(), Permission.KICK)) {
                                ClickableMessage.sendURL(
                                        sender,
                                        Config.messages.getColorfulString("no_permission"),
                                        support,
                                        DiscordServer.url
                                );
                                return true;
                            }
                            if (isPlayer ? argumentsToString.length() > protocol.getMaxChatLength() : argumentsToString.length() > maxConnectedArgumentLength) {
                                ClickableMessage.sendURL(
                                        sender,
                                        Config.messages.getColorfulString("massive_command_reason"),
                                        support,
                                        DiscordServer.url
                                );
                                return true;
                            }
                            PlayerProtocol t = PluginBase.getAnyCaseProtocol(args[1]);

                            if (t == null) {
                                ClickableMessage.sendURL(
                                        sender,
                                        Config.messages.getColorfulString("player_not_found_message"),
                                        support,
                                        DiscordServer.url
                                );
                                return true;
                            }
                            if (!t.punishments.kick(sender, argumentsToString)) {
                                ClickableMessage.sendURL(
                                        sender,
                                        Config.messages.getColorfulString("failed_command").replace(
                                                "{command}",
                                                StringUtils.toString(args, " ")
                                        ),
                                        support,
                                        DiscordServer.url
                                );
                            }

                        } else if (args[0].equalsIgnoreCase("Warn")) {
                            if (isPlayer && !Permissions.has(protocol.bukkit(), Permission.WARN)) {
                                ClickableMessage.sendURL(
                                        sender,
                                        Config.messages.getColorfulString("no_permission"),
                                        support,
                                        DiscordServer.url
                                );
                                return true;
                            }
                            if (isPlayer ? argumentsToString.length() > protocol.getMaxChatLength() : argumentsToString.length() > maxConnectedArgumentLength) {
                                ClickableMessage.sendURL(
                                        sender,
                                        Config.messages.getColorfulString("massive_command_reason"),
                                        support,
                                        DiscordServer.url
                                );
                                return true;
                            }
                            PlayerProtocol t = PluginBase.getAnyCaseProtocol(args[1]);

                            if (t == null) {
                                ClickableMessage.sendURL(
                                        sender,
                                        Config.messages.getColorfulString("player_not_found_message"),
                                        support,
                                        DiscordServer.url
                                );
                                return true;
                            }
                            if (!t.punishments.warn(sender, argumentsToString)) {
                                ClickableMessage.sendURL(
                                        sender,
                                        Config.messages.getColorfulString("failed_command").replace(
                                                "{command}",
                                                StringUtils.toString(args, " ")
                                        ),
                                        support,
                                        DiscordServer.url
                                );
                            }

                        } else if (args[0].equalsIgnoreCase("Bypass")) {
                            boolean noSeconds = args.length == 3;

                            if (noSeconds || args.length == 4) {
                                CheckEnums.HackType[] hackTypes = CheckEnums.HackType.values();
                                int maxHackTypes = hackTypes.length;
                                String[] checks = args[2].split(",", maxHackTypes);
                                String sec = noSeconds ? null : args[3];

                                if (isPlayer && !Permissions.has(protocol.bukkit(), Permission.USE_BYPASS)) {
                                    ClickableMessage.sendURL(
                                            sender,
                                            Config.messages.getColorfulString("no_permission"),
                                            support,
                                            DiscordServer.url
                                    );
                                    return true;
                                }
                                PlayerProtocol t = PluginBase.getAnyCaseProtocol(args[1]);

                                if (t == null) {
                                    ClickableMessage.sendURL(
                                            sender,
                                            Config.messages.getColorfulString("player_not_found_message"),
                                            support,
                                            DiscordServer.url
                                    );
                                    return true;
                                }
                                List<CheckEnums.HackType> found = new ArrayList<>(maxHackTypes);

                                for (String check : checks) {
                                    for (CheckEnums.HackType hackType : hackTypes) {
                                        if (hackType.getCheck().getName().equalsIgnoreCase(check)) {
                                            found.add(hackType);
                                            break;
                                        }
                                    }
                                }
                                if (!found.isEmpty()) {
                                    for (CheckEnums.HackType hackType : found) {
                                        int seconds = noSeconds ? 0 : Integer.parseInt(sec);

                                        if (noSeconds) {
                                            t.getRunner(hackType).addDisableCause("Command-" + sender.getName(), null, 0);
                                        } else {
                                            if (seconds < 1 || seconds > 3600) {
                                                sender.sendMessage(ChatColor.RED + "Seconds must be between 1 and 3600.");
                                                return true;
                                            }
                                            t.getRunner(hackType).addDisableCause("Command-" + sender.getName(), null, seconds * (AlgebraUtils.integerCeil(TPS.maximum)));
                                        }
                                        String message = ConfigUtils.replaceWithSyntax(t, Config.messages.getColorfulString("bypass_message"), hackType)
                                                .replace("{time}", noSeconds ? "infinite" : String.valueOf(seconds));
                                        sender.sendMessage(message);
                                    }
                                } else {
                                    ClickableMessage.sendURL(
                                            sender,
                                            Config.messages.getColorfulString("non_existing_check"),
                                            support,
                                            DiscordServer.url
                                    );
                                }
                            } else {
                                completeMessage(sender, "moderation");
                            }
                        } else if (args[0].equalsIgnoreCase("Wave")) {
                            String command = args[1];
                            OfflinePlayer t = Bukkit.getOfflinePlayer(args[2]);

                            if (isPlayer && !Permissions.has(protocol.bukkit(), Permission.WAVE)) {
                                ClickableMessage.sendURL(
                                        sender,
                                        Config.messages.getColorfulString("no_permission"),
                                        support,
                                        DiscordServer.url
                                );
                                return true;
                            }
                            if (command.equalsIgnoreCase("add") && args.length >= 4) {
                                if (Wave.getWaveList().length >= 100) {
                                    ClickableMessage.sendURL(
                                            sender,
                                            Config.messages.getColorfulString("full_wave_list"),
                                            support,
                                            DiscordServer.url
                                    );
                                    return true;
                                }
                                argumentsToStringBuilder = new StringBuilder();
                                for (int i = 3; i < args.length; i++) {
                                    argumentsToStringBuilder.append(args[i]).append(" ");
                                }
                                argumentsToString = argumentsToStringBuilder.substring(0, argumentsToStringBuilder.length() - 1);

                                if (isPlayer ? argumentsToString.length() > protocol.getMaxChatLength() : argumentsToString.length() > maxConnectedArgumentLength) {
                                    ClickableMessage.sendURL(
                                            sender,
                                            Config.messages.getColorfulString("massive_command_reason"),
                                            support,
                                            DiscordServer.url
                                    );
                                    return true;
                                }
                                String message = Config.messages.getColorfulString("wave_add_message");
                                message = ConfigUtils.replaceWithSyntax(t, message, null);
                                sender.sendMessage(message);
                                Wave.add(t.getUniqueId(), argumentsToString); // After to allow for further messages to take palce
                            } else if (command.equalsIgnoreCase("remove")) {
                                UUID uuid = t.getUniqueId();

                                if (Wave.getCommand(uuid) == null) {
                                    String message = Config.messages.getColorfulString("wave_not_added_message");
                                    message = ConfigUtils.replaceWithSyntax(t, message, null);
                                    sender.sendMessage(message);
                                    return true;
                                }
                                Wave.remove(uuid);
                                String message = Config.messages.getColorfulString("wave_remove_message");
                                message = ConfigUtils.replaceWithSyntax(t, message, null);
                                sender.sendMessage(message);
                            } else {
                                completeMessage(sender, "moderation");
                            }

                        } else if (args.length >= 4) {
                            argumentsToStringBuilder = new StringBuilder();
                            for (int i = 3; i < args.length; i++) {
                                argumentsToStringBuilder.append(args[i]).append(" ");
                            }
                            argumentsToString = argumentsToStringBuilder.substring(0, argumentsToStringBuilder.length() - 1);

                            if (args.length >= 7) {
                                if (isPlayer && !Permissions.has(protocol.bukkit(), Permission.CONDITION)) {
                                    ClickableMessage.sendURL(
                                            sender,
                                            Config.messages.getColorfulString("no_permission"),
                                            support,
                                            DiscordServer.url
                                    );
                                    return true;
                                }
                                PlayerProtocol t = PluginBase.getAnyCaseProtocol(args[0]);

                                if (t == null) {
                                    ClickableMessage.sendURL(
                                            sender,
                                            Config.messages.getColorfulString("player_not_found_message"),
                                            support,
                                            DiscordServer.url
                                    );
                                    return true;
                                }
                                if (args[1].equalsIgnoreCase("if") && args[5].equalsIgnoreCase("do")) {
                                    final String condition = ConfigUtils.replaceWithSyntax(t, args[2], null);
                                    final String result = ConfigUtils.replaceWithSyntax(t, args[4], null);

                                    argumentsToStringBuilder = new StringBuilder();
                                    for (int i = 6; i < args.length; i++) {
                                        argumentsToStringBuilder.append(args[i]).append(" ");
                                    }
                                    final String command = ConfigUtils.replaceWithSyntax(t, argumentsToStringBuilder.substring(0, argumentsToStringBuilder.length() - 1), null);

                                    switch (args[3].toLowerCase()) {
                                        case "equals":
                                        case "=":
                                            if (condition.equalsIgnoreCase(result)) {
                                                PluginBase.runCommand(command);
                                            }
                                            break;
                                        case "not-equals":
                                        case "/=":
                                            if (!condition.equalsIgnoreCase(result)) {
                                                PluginBase.runCommand(command);
                                            }
                                            break;
                                        case "contains":
                                            if (condition.contains(result)) {
                                                PluginBase.runCommand(command);
                                            }
                                            break;
                                        case "is-less-than":
                                        case "<":
                                            if (AlgebraUtils.validInteger(condition) && AlgebraUtils.validInteger(result) && num(condition) < num(result)
                                                    || AlgebraUtils.validDecimal(condition) && AlgebraUtils.validDecimal(result) && dbl(condition) < dbl(result)
                                                    || AlgebraUtils.validInteger(condition) && AlgebraUtils.validDecimal(result) && num(condition) < dbl(result)
                                                    || AlgebraUtils.validDecimal(condition) && AlgebraUtils.validInteger(result) && dbl(condition) < num(result)) {
                                                PluginBase.runCommand(command);
                                            }
                                            break;
                                        case "is-greater-than":
                                        case ">":
                                            if (AlgebraUtils.validInteger(condition) && AlgebraUtils.validInteger(result) && num(condition) > num(result)
                                                    || AlgebraUtils.validDecimal(condition) && AlgebraUtils.validDecimal(result) && dbl(condition) > dbl(result)
                                                    || AlgebraUtils.validInteger(condition) && AlgebraUtils.validDecimal(result) && num(condition) > dbl(result)
                                                    || AlgebraUtils.validDecimal(condition) && AlgebraUtils.validInteger(result) && dbl(condition) > num(result)) {
                                                PluginBase.runCommand(command);
                                            }
                                            break;
                                        default:
                                            break;
                                    }
                                }
                            } else {
                                completeMessage(sender, "default");
                            }
                        } else {
                            completeMessage(sender, "default");
                        }
                    }
                }
            }
        }
        return false;
    }
}
