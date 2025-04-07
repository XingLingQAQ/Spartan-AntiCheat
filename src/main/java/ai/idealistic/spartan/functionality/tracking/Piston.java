package ai.idealistic.spartan.functionality.tracking;

import ai.idealistic.spartan.abstraction.protocol.PlayerProtocol;
import ai.idealistic.spartan.functionality.server.PluginBase;
import ai.idealistic.spartan.utils.math.AlgebraUtils;
import ai.idealistic.spartan.utils.minecraft.entity.PlayerUtils;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.Collection;
import java.util.List;

public class Piston {

    private static final double
            horizontalDistance = 3.0,
            verticalDistance = 2.0;

    public static void run(Block block, List<Block> blocks) {
        Collection<PlayerProtocol> protocols = PluginBase.getProtocols();

        if (!protocols.isEmpty()) {
            boolean runBlocks = !blocks.isEmpty();
            World world = block.getWorld();

            for (PlayerProtocol protocol : protocols) {
                if (protocol.getWorld().equals(world)) {
                    Location location = protocol.getLocationOrVehicle();
                    double preX = AlgebraUtils.getSquare(location.getX(), block.getX()),
                            diffY = location.getY() - block.getY(),
                            preZ = AlgebraUtils.getSquare(location.getZ(), block.getZ());

                    if (!run(protocol, preX, diffY, preZ) // Check if the player is nearby to the piston
                            && runBlocks
                            && Math.sqrt(preX + (diffY * diffY) + preZ) <= PlayerUtils.chunk) { // Check if the player is nearby to the piston affected blocks
                        for (Block affected : blocks) {
                            preX = AlgebraUtils.getSquare(location.getX(), affected.getX());
                            diffY = location.getY() - block.getY();
                            preZ = AlgebraUtils.getSquare(location.getZ(), affected.getZ());

                            if (run(protocol, preX, diffY, preZ)) { // Check if the player is nearby to the piston affected block
                                break;
                            }
                        }
                    }
                }
            }
        }
    }

    private static boolean run(PlayerProtocol protocol, double preX, double diffY, double preZ) {
        if (Math.sqrt(preX + preZ) <= horizontalDistance
                && Math.abs(diffY) <= verticalDistance) {
            protocol.lastVelocity = System.currentTimeMillis();
            return true;
        }
        return false;
    }
}
