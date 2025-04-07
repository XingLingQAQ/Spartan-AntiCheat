package ai.idealistic.spartan.listeners.bukkit.standalone;

import ai.idealistic.spartan.abstraction.protocol.PlayerProtocol;
import ai.idealistic.spartan.functionality.server.PluginBase;
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
            p.executeRunners(e.isCancelled(), e);
        }
    }

}
