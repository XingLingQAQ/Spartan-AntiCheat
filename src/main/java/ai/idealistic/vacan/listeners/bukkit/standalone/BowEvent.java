package ai.idealistic.vacan.listeners.bukkit.standalone;

import ai.idealistic.vacan.abstraction.protocol.PlayerProtocol;
import ai.idealistic.vacan.functionality.server.PluginBase;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityShootBowEvent;

public class BowEvent implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    private void Event(EntityShootBowEvent e) {
        Entity entity = e.getEntity();

        if (entity instanceof Player) {
            PlayerProtocol p = PluginBase.getProtocol((Player) entity, true);
            p.profile().executeRunners(e.isCancelled(), e);
        }
    }

}
