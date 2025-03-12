package ai.idealistic.vacan.abstraction.check.implementation.movement.irregularmovements;

import ai.idealistic.vacan.abstraction.check.Check;
import ai.idealistic.vacan.abstraction.check.CheckDetection;
import ai.idealistic.vacan.abstraction.check.CheckRunner;
import ai.idealistic.vacan.abstraction.world.ServerLocation;
import ai.idealistic.vacan.utils.minecraft.world.BlockUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Entity;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.List;

public class IMGroundSpoof extends CheckDetection {

    private int vl = 0;
    private int tick0Value = 0;
    private long disabledUntil;

    IMGroundSpoof(CheckRunner executor) {
        super(
                executor,
                Check.DataType.JAVA,
                null,
                "ground_spoof",
                true,
                DEFAULT_AVERAGE_TIME,
                TIME_TO_NOTIFY * 2L,
                TIME_TO_PREVENT * 2L,
                TIME_TO_PUNISH * 2L
        );
    }

    void run(PlayerMoveEvent event) {
        this.call(() -> {
            if (disabledUntil <= System.currentTimeMillis()
                    && !this.protocol.getEnvironment().isSlime()
                    && !this.protocol.getEnvironment().isSlimeWide()
                    && !this.protocol.getEnvironment().isSlimeHeight()
                    && this.protocol.getVehicle() == null
                    && !this.protocol.isFlying()
                    && !this.protocol.isGliding()
                    && !this.protocol.isLowEyeHeight()) { // Covers swimming & gliding
                boolean onGround = this.protocol.isOnGround(),
                        onGroundCheck = isOnSolidGround(event.getTo().clone())
                                || isOnSolidGround(event.getFrom().clone());

                boolean boat = false;
                List<Entity> nearbyEntities = this.protocol.getNearbyEntities(2);
                for (Entity vehicle : nearbyEntities) {
                    if (vehicle instanceof Boat) {
                        boat = true;
                        break;
                    }
                }

                if (this.protocol.pistonTick) {
                    this.protocol.pistonTick = false;
                    this.tick0Value = 4;
                }

                if (this.tick0Value > 0) {
                    this.tick0Value--;
                    return;
                }

                if (onGround && !onGroundCheck && !boat) {
                    int threshold = 30;
                    this.vl += threshold;

                    if (this.vl >= threshold) {
                        this.cancel(
                                "Invalid ground value: " + onGround
                        );
                        //this.player.protocol.startSimulationFlag();
                    }
                } else if (onGround) {
                    this.decreaseVl(3);
                } else {
                    this.decreaseVl(1);
                }
            } else {
                this.vl = 0;
            }
        });
    }

    private void decreaseVl(int v) {
        this.vl -= v;
        if (this.vl < 0) {
            this.vl = 0;
        }
    }

    private boolean isOnSolidGround(Location location) {
        double x = location.getX(),
                y = location.getY() - 0.1,
                z = location.getZ();

        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dz = -1; dz <= 1; dz++) {
                    Material material = this.protocol.packetWorld.getBlock(
                            new Location(
                                    this.protocol.getWorld(),
                                    x + (dx * 0.3),
                                    y + (dy * 0.5),
                                    z + (dz * 0.3)
                            )
                    );
                    Material material2 = new ServerLocation(new Location(
                            this.protocol.getWorld(),
                            x + (dx * 0.3),
                            y + (dy * 0.5),
                            z + (dz * 0.3))).getBlock().getTypeOrNull();

                    if (material == null || material2 == null) {
                        continue;
                    }
                    if (BlockUtils.isSolid(material)
                            || BlockUtils.isSolid(material2)
                            || material.name().contains("GRASS")) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    void onBreak() {
        this.call(() -> this.disabledUntil = System.currentTimeMillis() + 1_000L);
    }

    void onVehicleExit() {
        this.call(() -> this.disabledUntil = System.currentTimeMillis() + 1_000L);
    }

}