package com.vagdedes.spartan.compatibility.manual.abilities;

import com.snowgears.grapplinghook.api.HookAPI;
import com.vagdedes.spartan.abstraction.configuration.implementation.Compatibility;
import com.vagdedes.spartan.abstraction.replicates.SpartanPlayer;
import com.vagdedes.spartan.functionality.server.SpartanBukkit;
import com.vagdedes.spartan.functionality.tracking.CheckDelay;
import com.vagdedes.spartan.utils.server.PluginUtils;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;

public class GrapplingHook implements Listener {

    private static boolean isItem(ItemStack i) {
        if (Compatibility.CompatibilityType.GRAPPLING_HOOK.isFunctional()) {
            try {
                return PluginUtils.exists("grapplinghook") ? HookAPI.isGrapplingHook(i) : i.getType() == Material.FISHING_ROD;
            } catch (Exception ignored) {
            }
        }
        return false;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    private void Event(PlayerFishEvent e) {
        if (Compatibility.CompatibilityType.GRAPPLING_HOOK.isFunctional() && e.getState() == PlayerFishEvent.State.CAUGHT_ENTITY) {
            Entity caught = e.getCaught();

            if (caught instanceof Player) {
                SpartanPlayer p = SpartanBukkit.getPlayer((Player) caught),
                        t = SpartanBukkit.getPlayer(e.getPlayer().getUniqueId());

                if (p != null && t != null && !p.equals(t) && isItem(t.getItemInHand())) {
                    if (PluginUtils.exists("grapplinghook")) {
                        CheckDelay.evadeStandardCombatFPs(p, Compatibility.CompatibilityType.GRAPPLING_HOOK, 40);
                    } else {
                        CheckDelay.evadeStandardCombatFPs(p, Compatibility.CompatibilityType.GRAPPLING_HOOK, 10);
                    }
                }
            }
        }
    }
}