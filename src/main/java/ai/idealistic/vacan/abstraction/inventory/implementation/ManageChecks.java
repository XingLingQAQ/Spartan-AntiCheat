package ai.idealistic.vacan.abstraction.inventory.implementation;

import ai.idealistic.vacan.abstraction.check.Check;
import ai.idealistic.vacan.abstraction.check.CheckEnums;
import ai.idealistic.vacan.abstraction.check.CheckEnums.HackType;
import ai.idealistic.vacan.abstraction.inventory.InventoryMenu;
import ai.idealistic.vacan.abstraction.protocol.PlayerProtocol;
import ai.idealistic.vacan.api.Permission;
import ai.idealistic.vacan.functionality.command.CommandExecution;
import ai.idealistic.vacan.functionality.connection.DiscordServer;
import ai.idealistic.vacan.functionality.moderation.clickable.ClickableMessage;
import ai.idealistic.vacan.functionality.server.Config;
import ai.idealistic.vacan.functionality.server.MultiVersion;
import ai.idealistic.vacan.functionality.server.Permissions;
import ai.idealistic.vacan.functionality.server.PluginBase;
import ai.idealistic.vacan.functionality.tracking.ResearchEngine;
import ai.idealistic.vacan.utils.minecraft.inventory.EnchantmentUtils;
import ai.idealistic.vacan.utils.minecraft.inventory.MaterialUtils;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class ManageChecks extends InventoryMenu {

    public ManageChecks() {
        super("Manage Checks", 54, Permission.MANAGE);
    }

    @Override
    public boolean internalOpen(PlayerProtocol protocol, boolean permissionMessage, Object object) {
        for (HackType check : CheckEnums.HackType.values()) {
            addCheck(protocol, check);
        }
        add("§cDisable silent checking for all checks", null, new ItemStack(MaterialUtils.get("lead")), 46);
        add("§cDisable all checks", null, new ItemStack(MultiVersion.isOrGreater(MultiVersion.MCVersion.V1_13) ? Material.RED_TERRACOTTA : Material.getMaterial("STAINED_CLAY"), 1, (short) 14), 47);

        add("§4Back", null, new ItemStack(Material.ARROW), 49);

        add("§aEnable all checks", null, new ItemStack(MultiVersion.isOrGreater(MultiVersion.MCVersion.V1_13) ? Material.LIME_TERRACOTTA : Material.getMaterial("STAINED_CLAY"), 1, (short) 5), 51);
        add("§aEnable silent checking for all checks", null, new ItemStack(Material.FEATHER), 52);
        return true;
    }

    @Override
    public boolean internalHandle(PlayerProtocol protocol) {
        String item = itemStack.getItemMeta().getDisplayName();
        item = item.startsWith("§") ? item.substring(2) : item;

        if (item.equals("Back")) {
            PluginBase.playerInfo.open(protocol, protocol.bukkit().getName());
        } else if (item.equals("Disable all checks")) {
            Config.disableChecks();
            open(protocol);
        } else if (item.equals("Enable all checks")) {
            Config.enableChecks();
            open(protocol);
        } else if (item.equals("Disable silent checking for all checks")) {
            Config.disableSilentChecking();
            open(protocol);
        } else if (item.equals("Enable silent checking for all checks")) {
            Config.enableSilentChecking();
            open(protocol);
        } else {
            item = item.split(" ")[0];

            if (clickType == ClickType.LEFT) {
                Check check = Config.getCheckByName(item);

                if (check != null) {
                    check.setEnabled(null, !check.isEnabled(null, null));
                }
                open(protocol);
            } else if (clickType == ClickType.RIGHT) {
                Check check = Config.getCheckByName(item);

                if (check != null) {
                    check.setSilent(null, !check.isSilent(null, null));
                }
                open(protocol);
            } else if (clickType.isShiftClick()) {
                Check check = Config.getCheckByName(item);

                if (check != null) {
                    ResearchEngine.resetData(check.hackType);
                    protocol.bukkit().closeInventory();
                    ClickableMessage.sendURL(
                            protocol.bukkit(),
                            Config.messages.getColorfulString("check_stored_data_delete_message").replace(
                                    "{check}",
                                    check.getName()
                            ),
                            CommandExecution.support,
                            DiscordServer.url
                    );
                }
            } else if (clickType.isKeyboardClick()) {
                Check check = Config.getCheckByName(item);

                if (check != null) {
                    check.setPunish(null, !check.canPunish(null));
                }
                open(protocol);
            }
        }
        return true;
    }

    private void addCheck(PlayerProtocol protocol, HackType hackType) {
        Check check = hackType.getCheck();
        boolean enabled = check.isEnabled(null, null),
                silent = check.isSilent(null, null),
                bypassing = Permissions.isBypassing(protocol.bukkit(), hackType),
                punish = check.canPunish(null);
        String enabledOption, silentOption, punishOption, colour, secondColour;
        ItemStack item;

        if (silent) {
            silentOption = "§7Right click to §cdisable §7silent checking.";
        } else {
            silentOption = "§7Right click to §aenable §7silent checking.";
        }

        if (punish) {
            punishOption = "§7Keyboard click to §cdisable §7punishments.";
        } else {
            punishOption = "§7Keyboard click to §aenable §7punishments.";
        }

        if (enabled) {
            item = new ItemStack(MultiVersion.isOrGreater(MultiVersion.MCVersion.V1_13) ? Material.LIME_DYE : Material.getMaterial("INK_SACK"), 1, (short) 10);
            colour = "§2";
            secondColour = "§a";
            enabledOption = "§7Left click to §cdisable §7check.";
        } else {
            item = new ItemStack(MultiVersion.isOrGreater(MultiVersion.MCVersion.V1_13) ? Material.GRAY_DYE : Material.getMaterial("INK_SACK"), 1, (short) 8);
            colour = "§4";
            secondColour = "§c";
            enabledOption = "§7Left click to §aenable §7check.";
        }

        List<String> lore = new ArrayList<>(30);

        for (String s : hackType.description) {
            lore.add("§7" + s);
        }

        // Separator

        lore.add("");
        lore.add((enabled ? "§a" : "§c") + "Enabled §8/ "
                + (silent ? "§a" : "§c") + "Silent §8/ "
                + (punish ? "§a" : "§c") + "Punishments §8/ "
                + (bypassing ? "§a" : "§c") + "Bypassing");
        int counter = 0;

        for (String s : check.getPunishmentCommands()) {
            if (s != null) {
                counter++;
                String base = "§7" + counter + "§8:§f ";

                if (s.length() > 40) {
                    lore.add(base + s.substring(0, 40));
                } else {
                    lore.add(base + s);
                }
            }
        }

        // Separator
        lore.add("");
        lore.add(enabledOption);
        lore.add(silentOption);
        lore.add(punishOption);
        lore.add("§7Shift click to §edelete §7the check's data.");

        // Separator

        if (enabled && silent) {
            item.addUnsafeEnchantment(EnchantmentUtils.DURABILITY, 1);
        }
        add(colour + check.getName() + " " + secondColour + hackType.category.toString() + " Check", lore, item, -1);
    }
}
