package ai.idealistic.vacan.listeners.bukkit.standalone;

import ai.idealistic.vacan.abstraction.profiling.MiningHistory;
import ai.idealistic.vacan.abstraction.protocol.PlayerProtocol;
import ai.idealistic.vacan.functionality.server.PluginBase;
import ai.idealistic.vacan.functionality.tracking.Piston;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class WorldEvent implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    private void BlockBreak(BlockBreakEvent e) {
        PlayerProtocol protocol = PluginBase.getProtocol(e.getPlayer(), true);
        Block nb = e.getBlock();
        boolean cancelled = e.isCancelled();
        protocol.profile().executeRunners(e.isCancelled(), e);
        MiningHistory.log(protocol, nb, cancelled);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void Sign(SignChangeEvent e) {
        PlayerProtocol p = PluginBase.getProtocol(e.getPlayer(), true);
        p.profile().executeRunners(e.isCancelled(), e);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void Animation(PlayerAnimationEvent e) {
        PlayerProtocol protocol = PluginBase.getProtocol(e.getPlayer(), true);
        protocol.profile().executeRunners(e.isCancelled(), e);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void Interact(PlayerInteractEvent e) {
        PlayerProtocol protocol = PluginBase.getProtocol(e.getPlayer(), true);
        protocol.profile().executeRunners(false, e); // False because is cancelled is deprecated
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void Piston(BlockPistonExtendEvent e) {
        if (!e.isCancelled()) {
            Piston.run(e.getBlock(), e.getBlocks());
        }
    }

}