package ai.idealistic.vacan.abstraction.check.implementation.combat;

import ai.idealistic.vacan.abstraction.check.CheckDetection;
import ai.idealistic.vacan.abstraction.check.CheckEnums.HackType;
import ai.idealistic.vacan.abstraction.check.CheckRunner;
import ai.idealistic.vacan.abstraction.check.definition.ImplementedDetection;
import ai.idealistic.vacan.abstraction.event.PlayerAttackEvent;
import ai.idealistic.vacan.abstraction.protocol.PlayerProtocol;
import ai.idealistic.vacan.functionality.server.MultiVersion;
import ai.idealistic.vacan.functionality.server.PluginBase;
import ai.idealistic.vacan.utils.math.ClientMath;
import ai.idealistic.vacan.utils.math.OptifineMath;
import ai.idealistic.vacan.utils.math.RayUtils;
import ai.idealistic.vacan.utils.math.VanillaMath;
import ai.idealistic.vacan.utils.math.statistics.StatisticsMath;
import ai.idealistic.vacan.utils.minecraft.entity.AxisAlignedBB;
import ai.idealistic.vacan.utils.minecraft.entity.CombatUtils;
import ai.idealistic.vacan.utils.minecraft.entity.MovingObjectPosition;
import ai.idealistic.vacan.utils.minecraft.vector.Vec3;
import ai.idealistic.vacan.utils.minecraft.world.BoundingBoxUtil;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.utility.MinecraftVersion;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Pose;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;

public class HitReach extends CheckRunner {

    private final CheckDetection
            hitboxRaytrace, throughblocks, simple;

    private final static boolean allowedVer =
                    ProtocolLibrary.getProtocolManager().getMinecraftVersion()
                                    .compareTo(new MinecraftVersion("1.14")) >= 0;
    private final List<Double> rayTraceAnalysis = new CopyOnWriteArrayList<>();
    private int vl = 0;
    private static final boolean[] BOOLEANS = {true, false};
    private static final double REACH_DIST = 3.001, HIT_BOX_VAL = 10.0;
    private double hitBoxVL = -1;
    private double reachVl = 0;
    private double blocksVl = 0;
    private final OptifineMath optifineMath = new OptifineMath();
    private final VanillaMath vanillaMath = new VanillaMath();

    public HitReach(HackType hackType, PlayerProtocol protocol) {
        super(hackType, protocol);
        hitboxRaytrace = new ImplementedDetection(this, null, null, "hitbox_raytrace", true,
                CheckDetection.DEFAULT_AVERAGE_TIME * 2L,
                CheckDetection.TIME_TO_NOTIFY,
                CheckDetection.TIME_TO_PREVENT,
                CheckDetection.TIME_TO_PUNISH);
        simple = new ImplementedDetection(this, null, null, "simple", true,
                CheckDetection.DEFAULT_AVERAGE_TIME * 2L,
                CheckDetection.TIME_TO_NOTIFY,
                CheckDetection.TIME_TO_PREVENT,
                CheckDetection.TIME_TO_PUNISH);
        throughblocks = new ImplementedDetection(this, null, null, "throughblocks", true,
                CheckDetection.DEFAULT_AVERAGE_TIME * 2L,
                CheckDetection.TIME_TO_NOTIFY,
                CheckDetection.TIME_TO_PREVENT,
                CheckDetection.TIME_TO_PUNISH);
    }

    @Override
    protected void handleInternal(boolean cancelled, Object object) {
        if (object instanceof PlayerAttackEvent) {
            PlayerAttackEvent event = (PlayerAttackEvent) object;
            if (!(event.target instanceof Player)) return;
            if (event.target.getVehicle() != null || this.protocol.getVehicle() != null) return;
            if (this.protocol.packetsEnabled()) {
                if (CombatUtils.canCheck(this.protocol, PluginBase.getProtocol((Player) event.target).bukkit())
                        && !this.protocol.getGameMode().equals(GameMode.CREATIVE)) {
                    PlayerProtocol target = PluginBase.getProtocol((Player) event.target);
                    List<Location> h = target.getPositionHistory();
                    Set<AxisAlignedBB> cachedBlocks = new HashSet<>();
                    Set<Material> ignored = new HashSet<>();
                    int nulls = 0;
                    Location hardcodedTargetLoc = target.getLocation().clone();
                    Location hardcodedPlayerLoc = this.protocol.getLocation().clone();
                    if (getDistance(hardcodedPlayerLoc, hardcodedTargetLoc) > 0.5) {
                        final Location t = target.getLocation();
                        for (int dx = -3; dx <= 3; ++dx) {
                            for (int dy = -3; dy <= 3; ++dy) {
                                for (int dz = -3; dz <= 3; ++dz) {
                                    final Location l = new Location(
                                                    target.bukkit().getWorld(),
                                                    t.getX() + (double) dx,
                                                    t.getY() + (double) dy,
                                                    t.getZ() + (double) dz
                                    );
                                    final Block block = getBlockAsync(l);
                                    if (block == null) {
                                        nulls++;
                                        continue;
                                    }
                                    final Location b = block.getLocation().clone().add(0.5, 0.5, 0.5);
                                    final Material material = block.getType();
                                    final List<AxisAlignedBB> bb = ProtocolLibrary.getProtocolManager().getMinecraftVersion()
                                                    .compareTo(new MinecraftVersion("1.13")) >= 0
                                                    ? BoundingBoxUtil.getBoundingBoxes(block)
                                                    : BoundingBoxUtil.getBoundingBoxesLegacy(block);
                                    if (material.isSolid() && bb != null) {
                                        cachedBlocks.addAll(bb);
                                    } else ignored.add(material);
                                }
                            }
                        }
                    }
                    final boolean is1_8 = this.protocol.isUsingVersion(MultiVersion.MCVersion.V1_8);
                    final float hitbox = (is1_8) ? 0.4F : 0.3F;
                    AtomicReference<Double> minDistance = new AtomicReference<>(Double.MAX_VALUE);
                    Set<Double> minBlockDistanceSet = new HashSet<>();
                    {
                        for (boolean fastMath : BOOLEANS) {
                            final ClientMath buildSpeed = fastMath ? optifineMath : vanillaMath;
                            final Location l = protocol.getLocation();
                            final Location lf = protocol.getFromLocation();
                            Vec3[] possibleEyeRotation = {
                                            getVectorForRotation(l.getYaw(), l.getPitch(), buildSpeed),
                                            getVectorForRotation(lf.getYaw(), lf.getPitch(), buildSpeed),
                                            getVectorForRotation(lf.getYaw(), lf.getPitch(), buildSpeed),
                            };

                            for (boolean sneaking : BOOLEANS) {
                                for (Vec3 eyeRotation : possibleEyeRotation) {
                                    Vec3 eyePos = new Vec3(
                                                    l.getX(),
                                                    l.getY() + getEyeHeight(sneaking),
                                                    l.getZ()
                                    );
                                    Vec3 endReachRay = eyePos.addVector(
                                                    eyeRotation.xCoord * 6.0D,
                                                    eyeRotation.yCoord * 6.0D,
                                                    eyeRotation.zCoord * 6.0D
                                    );
                                    List<Location> positions = new LinkedList<>(h);
                                    positions.add(target.getLocation());
                                    positions.add(target.getFrom());
                                    positions.add(target.bukkit().getLocation().clone());
                                    for (Location position : positions) {
                                        AxisAlignedBB axisAlignedBB = new AxisAlignedBB(
                                                        position.getX() - hitbox, position.getY() - 0.1f, position.getZ() - hitbox,
                                                        position.getX() + hitbox, position.getY() + 1.9f, position.getZ() + hitbox
                                        );

                                        MovingObjectPosition intercept = axisAlignedBB.calculateIntercept(eyePos, endReachRay);
                                        if (intercept != null) {
                                            double range = intercept.hitVec.distanceTo(eyePos);
                                            if (range < minDistance.get()) {
                                                minDistance.set(range);
                                            }
                                        }
                                    }
                                    double minBlockDist = Double.MAX_VALUE;
                                    for (AxisAlignedBB block : cachedBlocks) {
                                        Vec3 s = new Vec3(
                                                        l.getX(),
                                                        l.getY() + getBlockEyeHeight(sneaking),
                                                        l.getZ()
                                        );
                                        Vec3 e = s.addVector(
                                                        eyeRotation.xCoord * 6.0,
                                                        eyeRotation.yCoord * 6.0,
                                                        eyeRotation.zCoord * 6.0
                                        );
                                    /*
                                    profile.getPlayer().getWorld().spawnParticle(
                                                    Particle.DRIP_LAVA,
                                                    new Location(profile.getPlayer().getWorld(),
                                                                    e.xCoord, e.yCoord, e.zCoord), 0);
                                     */
                                        MovingObjectPosition intercept = block.calculateIntercept(s, e);
                                        double v = (intercept != null) ? intercept.hitVec.distanceTo(eyePos) : 0;
                                        if (v < (minDistance.get()) && v < minBlockDist
                                                        && intercept != null && v < 6) {
                                            minBlockDist = v;
                                        }
                                    }
                                    minBlockDistanceSet.add(minBlockDist);
                                }
                            }
                        }

                        //profile.getPlayer().sendMessage("i: " + minDistance.get() + " " + (minDistance.get() > 999));
                        boolean flagged = false;
                        if (minDistance.get() > HIT_BOX_VAL) {
                            flagged = true;
                            if (++hitBoxVL > 3D) {
                                this.hitboxRaytrace.cancel("Missing HitBox of " + target.bukkit().getName());
                                event.setCancelled(true);
                            }
                        } else {
                            hitBoxVL = Math.max(hitBoxVL - 5D, 0);
                        }
                        if (minDistance.get() > REACH_DIST && minDistance.get() < HIT_BOX_VAL) {
                            flagged = true;
                            if (reachVl++ > 2.5D) {
                                hitboxRaytrace.cancel("Distance " + minDistance.get());
                                event.setCancelled(true);
                            }
                        } else if (minDistance.get() < REACH_DIST && reachVl > 0) {
                            reachVl -= 0.05;
                        }
                        //profile.getPlayer().sendMessage("d: " + flagged);
                        { // result?
                            double dev = (StatisticsMath.getMax(minBlockDistanceSet) + 0.03 + 3e-3) - minDistance.get();
                            //profile.getPlayer().sendMessage("d: " + dev);
                            if (dev < -0.669) {
                                blocksVl++;
                                throughblocks.cancel("Hitting through block (dev: "
                                                + RayUtils.scaleVal(Math.abs(dev), 6) + ")");
                                event.setCancelled(true);
                            } else if (blocksVl > 0) {
                                blocksVl -= 0.5;
                            }
                        }
                    }
                }
            } else {
                LivingEntity target = event.target;
                if (CombatUtils.canCheck(this.protocol, target)
                        && !this.protocol.getGameMode().equals(GameMode.CREATIVE)) {
                    double bruteforceRayTrace = RayUtils.bruteforceRayTrace(this.protocol, target),
                            diff = bruteforceRayTrace - 0.4;
                    rayTraceAnalysis.add(bruteforceRayTrace);
                    if (rayTraceAnalysis.size() >= 10) {
                        analyze(this.protocol.getLocation(), bruteforceRayTrace);
                    }
                }
            }
        }
    }

    private static Vec3 getVectorForRotation(float yaw, float pitch, ClientMath clientMath) {
        float f = clientMath.cos(-yaw * 0.017453292F - (float) Math.PI);
        float f1 = clientMath.sin(-yaw * 0.017453292F - (float) Math.PI);
        float f2 = -clientMath.cos(-pitch * 0.017453292F);
        float f3 = clientMath.sin(-pitch * 0.017453292F);

        return new Vec3(f1 * f2, f3, f * f2);
    }

    private float getEyeHeight(boolean sneaking) {
        float eyeHeight = 1.62F;
        if (sneaking) {
            eyeHeight -= 0.08F;
        }
        if (allowedVer && this.protocol.bukkit().getPose().equals(Pose.SWIMMING)) {
            eyeHeight -= 1.0f;
        }
        return eyeHeight;
    }
    private float getBlockEyeHeight(boolean sneaking) {
        float eyeHeight;
        if (allowedVer && this.protocol.bukkit().getPlayer().getPose().equals(Pose.SWIMMING)) {
            eyeHeight = 0.4f;
        } else {
            eyeHeight = 1.62F;
            if (sneaking) {
                eyeHeight -= 0.35F;
            }
        }
        return eyeHeight;
    }
    private static Block getBlockAsync(final Location location) {
        if (location.getWorld().isChunkLoaded(location.getBlockX() >> 4, location.getBlockZ() >> 4)) {
            return location.getWorld().getBlockAt(location);
        } else {
            return null;
        }
    }
    private static double getDistance(Location loc1, Location loc2) {
        double deltaX = loc1.getX() - loc2.getX();
        double deltaY = loc1.getY() - loc2.getY();
        double deltaZ = loc1.getZ() - loc2.getZ();
        return Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ);
    }

    private void analyze(Location to, double bruteForce) {
        String punish = null;
        double globalValue = 0;

        for (Double value : rayTraceAnalysis) {
            globalValue += value;
        }
        double result = globalValue / rayTraceAnalysis.size();

        if (result > (this.protocol.packetsEnabled() ? 0.43 : 0.5)) punish = "simple";
        int percent = (int) ((result / 0.4) * 100);
        if (punish != null) {
            vl += 100;
            this.simple.cancel( // ATTENTION: Add more detections to the check if you write more definitions for the 'punish' String field
                    punish + ", hitbox-size-analysis: "
                            + result + ", hitbox-size-last: " + bruteForce + ", percent-of-deviation: "
                            + percent + "%, reach: " + RayUtils.scaleVal((result / 0.4) * 3, 2) + " blocks",
                    to
            );
        } else {
            if (vl > 0) vl -= 50;
        }
        rayTraceAnalysis.clear();

    }

    @Override
    protected boolean canRun() {
        return CombatUtils.canCheck(this.protocol);
    }
}