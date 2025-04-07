package ai.idealistic.spartan.abstraction.check.implementation.world.impossibleactions;

import ai.idealistic.spartan.abstraction.check.CheckDetection;
import ai.idealistic.spartan.abstraction.check.CheckRunner;
import ai.idealistic.spartan.abstraction.data.Buffer;
import ai.idealistic.spartan.abstraction.protocol.PlayerProtocol;
import ai.idealistic.spartan.abstraction.world.ServerLocation;
import ai.idealistic.spartan.functionality.server.MultiVersion;
import ai.idealistic.spartan.utils.minecraft.world.BlockUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;

import java.util.Arrays;
import java.util.List;

public class ScaffoldAnalysis extends CheckDetection {

    private Block oldBlock;
    private long
            placeTime,
            placeHash;
    private final Buffer.IndividualSimpleBuffer buffer;

    ScaffoldAnalysis(CheckRunner executor) {
        super(executor, null, null, "scaffold_analysis", true);
        this.placeTime = System.currentTimeMillis();
        this.placeHash = 0;
        this.buffer = new Buffer.IndividualSimpleBuffer();
    }

    void run(Block currentBlock) {
        this.call(() -> {
            PlayerProtocol p = this.protocol;

            if (this.oldBlock == null) {
                this.oldBlock = currentBlock;
                return;
            }
            Location l = p.getLocation();
            Location lB = new Location(l.getWorld(), (long) l.getX(), (long) l.getY(), (long) l.getZ());
            long placeHash = (long) ((lB.getX() * 31) + lB.getZ());
            Location bL = currentBlock.getLocation();
            Block oldB = this.oldBlock;
            Material downBType = new ServerLocation(
                    bL.clone().add(0, -1, 0)
            ).getBlock().getTypeOrNull();
            {
                long delay = System.currentTimeMillis() - this.placeTime;
                boolean checkHash = false;
                boolean flag = true;
                {
                    List<Long> hashList = Arrays.asList(
                            (long) ((bL.getX() * 31) + bL.getZ()),
                            (long) (((bL.getX() + 0.3) * 31) + bL.getZ()),      // +0.3 X, 0 Z
                            (long) (((bL.getX() - 0.3) * 31) + bL.getZ()),      // -0.3 X, 0 Z
                            (long) ((bL.getX() * 31) + (bL.getZ() + 0.3)),      // 0 X, +0.3 Z
                            (long) ((bL.getX() * 31) + (bL.getZ() - 0.3)),      // 0 X, -0.3 Z
                            (long) (((bL.getX() + 0.3) * 31) + (bL.getZ() + 0.3)),  // +0.3 X, +0.3 Z
                            (long) (((bL.getX() + 0.3) * 31) + (bL.getZ() - 0.3)),  // +0.3 X, -0.3 Z
                            (long) (((bL.getX() - 0.3) * 31) + (bL.getZ() + 0.3)),  // -0.3 X, +0.3 Z
                            (long) (((bL.getX() - 0.3) * 31) + (bL.getZ() - 0.3))   // -0.3 X, -0.3 Z
                    );
                    for (Long v : hashList) {
                        if (p.isOnGround()) {
                            if (v == placeHash || ServerLocation.distance(bL, l) < 1.2) {
                                checkHash = true;
                                break;
                            } else if (!MultiVersion.isOrGreater(MultiVersion.MCVersion.V1_13)) {
                                Vector v2 = lB.toVector().clone();
                                Vector v1 = bL.toVector().clone();
                                if (v1.getX() == v2.getX() - 1 && v1.getZ() == v2.getZ()) {
                                    checkHash = true;
                                    break;
                                }
                            }
                        } else if (ServerLocation.distance(bL, l) < 2.35) {
                            checkHash = true;
                            break;
                        }
                    }
                }
                if (delay < 5
                        || placeHash == this.placeHash
                        || (long) p.getLocation().getY() <= bL.getY()
                        || p.bukkit().isFlying() || !checkHash) {
                    p.rightClickCounter = 0;
                    this.placeTime = System.currentTimeMillis();
                    this.oldBlock = currentBlock;
                    return;
                } else if (bL.getY() != oldB.getLocation().getY()
                        || !BlockUtils.areAir(downBType)) {
                    p.rightClickCounter = 0;
                    this.oldBlock = currentBlock;
                    if (buffer.count > 5) buffer.count -= 5;
                    return;
                }

                if (delay < 280) {
                    buffer.count += 4;
                } else if (delay < (p.isSneaking() ? 345 : 385) && p.rightClickCounter < 3) {
                    buffer.count += 2;
                } else {
                    flag = false;
                }

                if (buffer.count > 12 && flag) {
                    this.cancel("scaffold analysis ("
                            + (long) Math.abs((((double) 400 / (double) delay) * 100) - 100)
                            + "% faster than possible) [VL: " + buffer.count + "]");
                    buffer.count = 10;
                } else if (buffer.count > 0) {
                    buffer.count -= 2;
                }

            }

            this.oldBlock = currentBlock;
            this.placeHash = placeHash;
            this.placeTime = System.currentTimeMillis();
            p.rightClickCounter = 0;
        });
    }

}
