package ai.idealistic.vacan.listeners.bukkit;

import ai.idealistic.vacan.abstraction.protocol.PlayerProtocol;
import ai.idealistic.vacan.functionality.server.PluginBase;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class TeleportEvent implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    private void Teleport(PlayerTeleportEvent e) {
        teleport(e.getPlayer(), false, e);
    }

    public static void teleport(Player player, boolean packets, Object object) {
        PlayerProtocol protocol = PluginBase.getProtocol(player, true);

        if (protocol.packetsEnabled() == packets
                || object instanceof PlayerTeleportEvent) {
            protocol.profile().executeRunners(null, object);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void Respawn(PlayerRespawnEvent e) {
        respawn(e.getPlayer(), false, e);
    }

    public static void respawn(Player player, boolean packets, Object object) {
        PlayerProtocol protocol = PluginBase.getProtocol(player, true);

        if (protocol.packetsEnabled() == packets) {
            protocol.profile().executeRunners(null, object);
        }
    }

}
