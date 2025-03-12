package ai.idealistic.vacan.listeners.bukkit;

import ai.idealistic.vacan.abstraction.protocol.PlayerProtocol;
import ai.idealistic.vacan.functionality.server.PluginBase;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;

public class VehicleEvent implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public static void enter(VehicleEnterEvent e) {
        if (!e.isCancelled()) {
            Entity entity = e.getEntered();

            if (entity instanceof Player) {
                PlayerProtocol p = PluginBase.getProtocol((Player) entity, true);
                p.profile().executeRunners(e.isCancelled(), e);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public static void exit(VehicleExitEvent e) {
        if (!e.isCancelled()) {
            Entity en = e.getExited();

            if (en instanceof Player) {
                PlayerProtocol p = PluginBase.getProtocol((Player) en, true);
                p.profile().executeRunners(e.isCancelled(), e);
            }
        }
    }

}
