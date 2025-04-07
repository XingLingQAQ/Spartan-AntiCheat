package ai.idealistic.spartan.compatibility.manual.enchants;

import ai.idealistic.spartan.abstraction.protocol.PlayerProtocol;
import ai.idealistic.spartan.compatibility.Compatibility;
import ai.idealistic.spartan.functionality.server.MultiVersion;
import me.darrionat.CustomEnchantsAPI;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class CustomEnchantsPlus {

    public static boolean has(PlayerProtocol protocol) {
        if (Compatibility.CompatibilityType.CUSTOM_ENCHANTS_PLUS.isFunctional()) {
            PlayerInventory inventory = protocol.getInventory();

            for (ItemStack armor : inventory.getArmorContents()) {
                if (armor != null && has(armor)) {
                    return true;
                }
            }
            return has(inventory.getItemInHand())
                    || MultiVersion.isOrGreater(MultiVersion.MCVersion.V1_9)
                    && has(inventory.getItemInOffHand());
        }
        return false;
    }

    private static boolean has(ItemStack item) {
        if (item.hasItemMeta()) {
            for (Enchantment enchantment : item.getItemMeta().getEnchants().keySet()) {
                if (CustomEnchantsAPI.isCustomEnchantment(enchantment)) {
                    return true;
                }
            }
        }
        return false;
    }
}
