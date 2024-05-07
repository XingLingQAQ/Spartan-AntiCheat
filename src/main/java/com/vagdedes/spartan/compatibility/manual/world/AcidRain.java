package com.vagdedes.spartan.compatibility.manual.world;

import com.vagdedes.spartan.abstraction.configuration.implementation.Compatibility;
import com.vagdedes.spartan.abstraction.replicates.SpartanPlayer;
import com.vagdedes.spartan.functionality.server.SpartanBukkit;
import com.wasteofplastic.acidisland.events.AcidEvent;
import com.wasteofplastic.acidisland.events.AcidRainEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

public class AcidRain implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    private void AcidRainEvent(AcidRainEvent e) {
        if (Compatibility.CompatibilityType.ACID_RAIN.isFunctional()) {
            Player n = e.getPlayer();
            SpartanPlayer p = SpartanBukkit.getPlayer(n.getUniqueId());

            if (p == null) {
                return;
            }
            p.addReceivedDamage(new EntityDamageEvent(
                    n,
                    EntityDamageEvent.DamageCause.FALL,
                    0.0
            ));
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void AcidEvent(AcidEvent e) {
        if (Compatibility.CompatibilityType.ACID_RAIN.isFunctional()) {
            Player n = e.getPlayer();
            SpartanPlayer p = SpartanBukkit.getPlayer(n.getUniqueId());

            if (p == null) {
                return;
            }
            p.addReceivedDamage(new EntityDamageEvent(
                    n,
                    EntityDamageEvent.DamageCause.FALL,
                    0.0
            ));
        }
    }
}