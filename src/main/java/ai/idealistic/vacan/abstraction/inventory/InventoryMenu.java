package ai.idealistic.vacan.abstraction.inventory;

import ai.idealistic.vacan.abstraction.protocol.PlayerProtocol;
import ai.idealistic.vacan.api.Permission;
import ai.idealistic.vacan.functionality.server.Config;
import ai.idealistic.vacan.functionality.server.Permissions;
import ai.idealistic.vacan.functionality.server.PluginBase;
import ai.idealistic.vacan.utils.minecraft.inventory.InventoryUtils;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public abstract class InventoryMenu {

    protected String title;

    protected final Permission[] permissions;
    protected ItemStack itemStack;
    protected ClickType clickType;
    protected int slot, size;
    protected Inventory inventory;

    public InventoryMenu(String title, int size, Permission[] permissions) {
        this.title = title;
        this.itemStack = null;
        this.clickType = null;
        this.inventory = null;
        this.slot = 0;
        this.size = size;
        this.permissions = permissions;
    }

    public InventoryMenu(String title, int size, Permission permissions) {
        this(title, size, new Permission[]{permissions});
    }

    protected Inventory setInventory(PlayerProtocol protocol, String title, int size) {
        this.title = title;
        this.size = size;
        return inventory = protocol.createInventory(size, title);
    }

    protected Inventory setSize(PlayerProtocol protocol, int size) {
        this.size = size;
        return inventory = protocol.createInventory(size, title);
    }

    protected Inventory setTitle(PlayerProtocol protocol, String title) {
        this.title = title;
        return inventory = protocol.createInventory(size, title);
    }

    protected void add(String name, List<String> lore, ItemStack item, int slot) {
        InventoryUtils.add(inventory, name, lore, item, slot);
    }

    public boolean open(PlayerProtocol protocol, boolean permissionMessage) {
        return open(protocol, permissionMessage, null);
    }

    public boolean open(PlayerProtocol protocol, Object object) {
        return open(protocol, true, object);
    }

    public boolean open(PlayerProtocol protocol) {
        return open(protocol, true, null);
    }

    public boolean open(PlayerProtocol protocol, boolean permissionMessage, Object object) {
        boolean access;

        if (permissions.length == 0) {
            access = true;
        } else {
            boolean check = false;

            for (Permission permission : permissions) {
                if (Permissions.has(protocol.bukkit(), permission)) {
                    check = true;
                    break;
                }
            }
            access = check;
        }

        if (access) {
            inventory = protocol.createInventory(size, title);
            if (internalOpen(protocol, permissionMessage, object)) {
                PluginBase.transferTask(
                        protocol,
                        () -> protocol.bukkit().openInventory(inventory)
                );
                return true;
            } else {
                return false;
            }
        } else {
            if (permissionMessage) {
                protocol.bukkit().sendMessage(Config.messages.getColorfulString("no_permission"));
            }
            PluginBase.transferTask(protocol, protocol.bukkit()::closeInventory);
            return false;
        }
    }

    protected abstract boolean internalOpen(PlayerProtocol protocol, boolean permissionMessage, Object object);

    public boolean handle(PlayerProtocol protocol, String title, ItemStack itemStack, ClickType clickType, int slot) {
        if (title.equals(this.title)) {
            boolean access;

            if (permissions.length == 0) {
                access = true;
            } else {
                boolean check = false;

                for (Permission permission : permissions) {
                    if (Permissions.has(protocol.bukkit(), permission)) {
                        check = true;
                        break;
                    }
                }
                access = check;

                if (!access) {
                    protocol.bukkit().closeInventory();
                }
            }

            if (access) {
                this.itemStack = itemStack;
                this.clickType = clickType;
                this.slot = slot;
                return internalHandle(protocol);
            }
        }
        return false;
    }

    protected abstract boolean internalHandle(PlayerProtocol protocol);
}
