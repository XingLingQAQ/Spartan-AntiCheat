package ai.idealistic.vacan.listeners.bukkit;

import ai.idealistic.vacan.abstraction.event.PlayerTickEvent;
import ai.idealistic.vacan.abstraction.event.PlayerTransactionEvent;
import ai.idealistic.vacan.abstraction.protocol.PlayerProtocol;
import ai.idealistic.vacan.abstraction.world.ServerLocation;
import ai.idealistic.vacan.functionality.server.PluginBase;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class MovementEvent implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    private void MoveEvent(PlayerMoveEvent e) {
        event(e, false);
    }

    public static void event(PlayerMoveEvent e, boolean packets) {
        PlayerProtocol protocol = PluginBase.getProtocol(e.getPlayer());

        if (protocol.packetsEnabled() == packets) {
            Location nto = e.getTo();

            if (nto == null) {
                return;
            }
            Location vehicle = protocol.getVehicleLocation();
            ServerLocation to = vehicle != null
                    ? new ServerLocation(vehicle)
                    : new ServerLocation(nto);

            if (!protocol.processLastMoveEvent(
                    nto,
                    vehicle,
                    to,
                    e.getFrom(),
                    packets
            )) {
                return;
            }
            protocol.profile().executeRunners(e.isCancelled(), e);
        }
    }

    public static void tick(PlayerTickEvent tickEvent) {
        PlayerProtocol protocol = tickEvent.protocol;
        protocol.lastTickEvent = tickEvent;
        protocol.packetWorld.tick(tickEvent);
        protocol.profile().executeRunners(false, tickEvent);
    }

    public static void transaction(PlayerTransactionEvent event) {
        PlayerProtocol protocol = event.protocol;
        protocol.profile().executeRunners(false, event);
    }

}
