package ai.idealistic.vacan.listeners.bukkit;

import ai.idealistic.vacan.abstraction.Enums;
import ai.idealistic.vacan.abstraction.event.CBlockPlaceEvent;
import ai.idealistic.vacan.abstraction.protocol.PlayerProtocol;
import ai.idealistic.vacan.compatibility.manual.abilities.ItemsAdder;
import ai.idealistic.vacan.functionality.server.PluginBase;
import org.bukkit.block.Block;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class BlockPlaceEvent implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public static void event(org.bukkit.event.block.BlockPlaceEvent e) {
        PlayerProtocol protocol = PluginBase.getProtocol(e.getPlayer(), true);

        if (event(protocol, e.getBlock(), e.getBlockAgainst(), e)) {
            e.setCancelled(true);
        }
    }

    public static boolean event(
            PlayerProtocol protocol,
            Block block,
            Block blockAgainst,
            Object event
    ) {
        if (protocol.getWorld() != block.getWorld()) {
            return false;
        }
        protocol.profile().executeRunners(null, event);

        // Detections
        if (!ItemsAdder.is(block)) {
            protocol.profile().executeRunners(
                    event,
                    new CBlockPlaceEvent(
                            protocol.bukkit(),
                            block,
                            blockAgainst,
                            event instanceof Cancellable && ((Cancellable) event).isCancelled()
                    )
            );
        }
        return protocol.profile().getRunner(Enums.HackType.FastPlace).prevent()
                || protocol.profile().getRunner(Enums.HackType.BlockReach).prevent()
                || protocol.profile().getRunner(Enums.HackType.ImpossibleActions).prevent();
    }

}
