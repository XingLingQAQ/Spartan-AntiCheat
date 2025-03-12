package ai.idealistic.vacan.listeners.bukkit;

import ai.idealistic.vacan.abstraction.event.CPlayerRiptideEvent;
import ai.idealistic.vacan.abstraction.protocol.PlayerProtocol;
import ai.idealistic.vacan.functionality.server.PluginBase;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRiptideEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class TridentEvent implements Listener {

    private static final double riptideMaxSafeLevel = 3.0;

    @EventHandler
    private void Event(PlayerRiptideEvent e) {
        event(
                new CPlayerRiptideEvent(
                        e.getPlayer(),
                        e.getItem(),
                        e.getPlayer().getVelocity()
                ), false);
    }

    public static void event(CPlayerRiptideEvent e, boolean packets) {
        PlayerProtocol p = PluginBase.getProtocol(e.player, true);

        if (p.packetsEnabled() == packets) {
            PlayerInventory inventory = e.player.getInventory();

            for (ItemStack item : new ItemStack[]{inventory.getItemInHand(), inventory.getItemInOffHand()}) {
                if (item.getType() == Material.TRIDENT) {
                    int level = item.getEnchantmentLevel(Enchantment.RIPTIDE);

                    if (level > 0) {
                        p.lastVelocity = System.currentTimeMillis();
                        break;
                    }
                }
            }
        }
    }

}
