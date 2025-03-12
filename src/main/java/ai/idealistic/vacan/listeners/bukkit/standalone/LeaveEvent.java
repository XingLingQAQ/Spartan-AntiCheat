package ai.idealistic.vacan.listeners.bukkit.standalone;

import ai.idealistic.vacan.abstraction.protocol.PlayerProtocol;
import ai.idealistic.vacan.functionality.moderation.DetectionNotifications;
import ai.idealistic.vacan.functionality.server.PluginBase;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class LeaveEvent implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    private void Event(PlayerQuitEvent e) {
        PlayerProtocol protocol = PluginBase.deleteProtocol(e.getPlayer());

        if (protocol == null) {
            return;
        }
        DetectionNotifications.runOnLeave(protocol);
    }

}
