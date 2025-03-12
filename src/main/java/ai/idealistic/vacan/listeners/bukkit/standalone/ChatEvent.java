package ai.idealistic.vacan.listeners.bukkit.standalone;

import ai.idealistic.vacan.abstraction.protocol.PlayerProtocol;
import ai.idealistic.vacan.functionality.server.PluginBase;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatEvent implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    private void Event(AsyncPlayerChatEvent e) {
        PlayerProtocol p = PluginBase.getProtocol(e.getPlayer());
        p.profile().executeRunners(e.isCancelled(), e);
    }

}
