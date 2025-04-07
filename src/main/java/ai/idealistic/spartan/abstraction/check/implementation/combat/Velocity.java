package ai.idealistic.spartan.abstraction.check.implementation.combat;

import ai.idealistic.spartan.abstraction.check.Check;
import ai.idealistic.spartan.abstraction.check.CheckDetection;
import ai.idealistic.spartan.abstraction.check.CheckEnums;
import ai.idealistic.spartan.abstraction.check.CheckRunner;
import ai.idealistic.spartan.abstraction.check.definition.ImplementedDetection;
import ai.idealistic.spartan.abstraction.event.PlayerTransactionEvent;
import ai.idealistic.spartan.abstraction.protocol.PlayerProtocol;
import ai.idealistic.spartan.abstraction.world.ServerLocation;
import ai.idealistic.spartan.utils.math.RayUtils;
import ai.idealistic.spartan.utils.minecraft.entity.CombatUtils;
import ai.idealistic.spartan.utils.minecraft.world.BlockUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerVelocityEvent;
import org.bukkit.util.Vector;

public class Velocity extends CheckRunner {

    private final CheckDetection vertical, horizontal;

    public Velocity(CheckEnums.HackType hackType, PlayerProtocol protocol) {
        super(hackType, protocol);
        this.vertical = new ImplementedDetection(this, null, Check.DetectionType.PACKETS, "vertical", true,
                CheckDetection.DEFAULT_AVERAGE_TIME * 4L,
                CheckDetection.TIME_TO_NOTIFY,
                CheckDetection.TIME_TO_PREVENT,
                CheckDetection.TIME_TO_PUNISH);
        this.horizontal = new ImplementedDetection(this, null, Check.DetectionType.PACKETS, "horizontal", true,
                CheckDetection.DEFAULT_AVERAGE_TIME * 4L,
                CheckDetection.TIME_TO_NOTIFY,
                CheckDetection.TIME_TO_PREVENT,
                CheckDetection.TIME_TO_PUNISH);
    }

    private final double[] jumpReset = new double[]{0.248136, 0.3332};

    private float vl = 0;
    private long oldTime = System.currentTimeMillis();
    private double mostCloseYMotion = 1.0;
    private int timing = 0;
    private boolean transactionLock = false;
    private Vector velocity = null;

    @Override
    protected void handleInternal(boolean cancelled, Object object) {
        if (object instanceof PlayerTransactionEvent) {
            transactionLock = false;
        } else if (object instanceof PlayerVelocityEvent) {
            PlayerVelocityEvent event = (PlayerVelocityEvent) object;
            transactionLock = true;
            Location[] locationsToCheck = {
                    this.protocol.getLocation().clone().add(event.getVelocity()),
                    this.protocol.getLocation().clone().add(event.getVelocity()).add(0, 1, 0)
            };
            boolean allClear = true;
            for (Location loc : locationsToCheck) {
                if (isPointWall(loc)) {
                    allClear = false;
                    break;
                }
            }
            if (allClear) {
                this.velocity = event.getVelocity();
                this.mostCloseYMotion = 1.0;
                this.timing = 0;
            }

            if (vl > 0) vl -= 5;
        } else if (object instanceof PlayerMoveEvent) {
            if (this.horizontal.canCall()
                    || this.vertical.canCall()) {
                long delay = System.currentTimeMillis() - oldTime;
                PlayerMoveEvent event = (PlayerMoveEvent) object;
                Location from = event.getFrom();
                Location to = event.getTo();

                double x = to.getX() - from.getX();
                double y = to.getY() - from.getY();
                double z = to.getZ() - from.getZ();

                //this.protocol.bukkit().sendMessage("m: " + y);
                if (velocity != null) {
                    if (abs(abs(y) - abs(velocity.getY())) < mostCloseYMotion)
                        mostCloseYMotion = y;
                    // time for vertical
                    if (abs(abs(y) - abs(velocity.getY())) < 0.0001) {
                        { // time for horizontal
                            double xDiff = x - velocity.getX();
                            double zDiff = z - velocity.getZ();
                            double total = abs(xDiff) + abs(zDiff);
                            double multi = 1.0;
                            if ((!this.protocol.isOnGround() && !this.protocol.isOnGroundFrom())) {
                                // time for horizontal flag 1
                                if (total > 0.05 * multi) {
                                    this.flag("velocity manipulation [xz] (air deviation: " + total + ")", 14, horizontal);
                                }
                            } else if (total > 0.2 * multi) {
                                this.flag("velocity manipulation [xz] (ground deviation: " + total + ")", 30, horizontal);
                            }
                        }
                        //this.protocol.bukkit().sendMessage(x + " " + velocity.getX() + " | " + z + " " + velocity.getZ());
                        this.velocity = null;
                        this.timing = 0;
                    } else if (delay > 25) {
                        if (timing < 2) {
                            if (!transactionLock) timing++;
                        } else {
                            // time for vertical flag
                            if (this.velocity.getY() != 0.003) {
                                this.flag("velocity manipulation [y] (motion: " + r(this.mostCloseYMotion)
                                        + ", velocity: " + r(this.velocity.getY()) + ")", this.isJumpReset(this.mostCloseYMotion) ? 12 : 25, vertical);
                            }
                            this.velocity = null;
                        }
                    }
                }
                this.oldTime = System.currentTimeMillis();
            }
        }
    }

    private void flag(String reason, double vl, CheckDetection e) {
        e.call(() -> {
            this.vl += (float) vl;
            if (this.vl > 60) {
                //this.protocol.bukkit().sendMessage("flag: " + reason);
                e.cancel(reason);
                this.vl = 50;
            }
        });
    }

    private boolean isJumpReset(double v) {
        for (double d : this.jumpReset) {
            if (d == r(v)) return true;
        }
        return false;
    }

    private static double abs(double v) {
        return Math.abs(v);
    }

    private static double r(double v) {
        return RayUtils.scaleVal(v, 6);
    }

    private boolean isPointWall(Location location) {
        double x = location.getX();
        double y = location.getY() + 0.1;
        double z = location.getZ();
        for (int dx = -1; dx <= 1; ++dx) {
            for (int dy = -1; dy <= 1; ++dy) {
                for (int dz = -1; dz <= 1; ++dz) {
                    Material material = new ServerLocation(
                            new Location(this.protocol.getWorld(), x + (double) dx * 0.3, y + (double) dy * 0.3, z + (double) dz * 0.3)
                    ).getBlock().getTypeOrNull();

                    if (material != null &&
                            (BlockUtils.isSemiSolid(material)
                                    || BlockUtils.isSolid(material)
                                    || material.toString().contains("GRASS"))
                            || BlockUtils.isLiquid(material)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    protected boolean canRun() {
        return this.protocol.getVehicle() == null
                && !this.protocol.getEnvironment().isSlime()
                && !this.protocol.getEnvironment().isSlimeWide()
                && !this.protocol.getEnvironment().isSlimeHeight()
                && CombatUtils.canCheck(this.protocol)
                && !this.protocol.isOutsideOfTheBorder(-1.0);
    }

}
