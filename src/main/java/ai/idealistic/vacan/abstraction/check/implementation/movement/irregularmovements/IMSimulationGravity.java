package ai.idealistic.vacan.abstraction.check.implementation.movement.irregularmovements;

import ai.idealistic.vacan.abstraction.check.Check;
import ai.idealistic.vacan.abstraction.check.CheckDetection;
import ai.idealistic.vacan.abstraction.check.CheckRunner;
import ai.idealistic.vacan.abstraction.event.CPlayerVelocityEvent;
import ai.idealistic.vacan.abstraction.protocol.ExtendedPotionEffect;
import ai.idealistic.vacan.abstraction.protocol.PlayerProtocol;
import ai.idealistic.vacan.abstraction.world.ServerLocation;
import ai.idealistic.vacan.functionality.server.MultiVersion;
import ai.idealistic.vacan.functionality.server.MultiVersion.MCVersion;
import ai.idealistic.vacan.functionality.server.TPS;
import ai.idealistic.vacan.utils.math.AlgebraUtils;
import ai.idealistic.vacan.utils.math.RayUtils;
import ai.idealistic.vacan.utils.minecraft.entity.PotionEffectUtils;
import ai.idealistic.vacan.utils.minecraft.world.BlockUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Entity;
import org.bukkit.potion.PotionEffectType;

public class IMSimulationGravity extends CheckDetection {

    private int air = 0, ground = 0;
    private int top = 0, topReset = 0;
    private int levitation = 0, glideBlocks = 0, climbBlocks = 0, vhact = 0,
            slowFalling = 0, levitationA = 0, slowFallingA = 0, swimming = 0,
            boatTicks = 0;
    private boolean slime = false, slime0Tick = false,
            trident = false, cringeBlockClimb = false, allowLowJump = false,
            allowMinorGravityChange = false, allowLowMinorAccel = false;
    private int slimePunch = 0, slimeTicks = 0, pistonTicks = 0, sub = 0;
    private double vl = 0.0, vlCount = 0.0;
    private int semi = 0;
    private double fallDistance = 0.0;
    private double lastGroundY = 0;
    private double teleportExcept = 0;
    private double motionY = 0;

    public IMSimulationGravity(CheckRunner executor) {
        super(
                executor,
                null,
                Check.DetectionType.PACKETS,
                "gravity_packets",
                true
        );
    }

    private boolean patches(PlayerProtocol p) {
        for (Entity entity : p.getNearbyEntities(2.0)) { // Boats
            if (entity instanceof Boat) {
                this.boatTicks = AlgebraUtils.integerCeil(TPS.maximum);
                return true;
            }
        }
        if (this.boatTicks > 0) {
            this.boatTicks--;
            return true;
        }
        if (p.wasGliding() || p.getVehicle() != null) {
            return true;
        }
        if (p.getTimePassed(p.magmaCubeWater) <= 1_000L
                || p.getTimePassed(p.soulSandWater) <= 1_000L) { // Bubble columns
            return true;
        }
        for (int i = 0; i < AlgebraUtils.integerCeil(p.getEyeHeight()); i++) {
            Material m = new ServerLocation(p.getLocation().clone().add(0, i, 0)).getBlock().getType();

            if (BlockUtils.areInteractiveBushes(m)
                    || BlockUtils.areWebs(m)) {
                return true;
            }
        }
        return false;
    }

    void run() {
        this.call(() -> {
            PlayerProtocol p = this.protocol;

            if (patches(p)) {
                this.air = 0;
                this.ground = 0;
                this.vl = 0.0;
                this.vlCount = 0.0;
                this.setPredict(0.0);
                this.levitation = 0;
                this.levitationA = 0;
                this.slime = false;
                this.slime0Tick = false;
                this.trident = false;
                this.cringeBlockClimb = false;
                this.allowLowJump = false;
                this.allowMinorGravityChange = false;
                this.allowLowMinorAccel = false;
                this.slimePunch = 0;
                this.slimeTicks = 0;
                this.pistonTicks = 0;
                this.sub = 0;
                this.semi = 0;
                this.fallDistance = 0.0;
                this.lastGroundY = 0;
                this.teleportExcept = 0;
                this.motionY = 0;
                return;
            }
            boolean ground = (this.protocol.getVehicle() != null)
                    ? this.protocol.getVehicle().isOnGround() : this.protocol.isOnGround();
            Location to = p.getLocation();
            Location from = this.protocol.getFromLocation();
            double dx = to.getX() - from.getX();
            double dz = to.getZ() - from.getZ();
            double speed = Math.sqrt(dx * dx + dz * dz);

            //this.protocol.bukkit().sendMessage(protocol().pistonHandle + " " + protocol().pistonTick + " " + protocol().explosionHandle);
            if (to.getX() != from.getX() || to.getY() != from.getY() || to.getZ() != from.getZ()) {
                double motion = to.getY() - from.getY();
                double gravity = 0.08;
                double jumpStartMotionY =
                        (atJumpModifyBlock()
                                && this.jumpMotion() * 1.5 > motion)
                                ? motion : this.jumpMotion();
                if (BlockUtils.areWebs(new ServerLocation(to).getBlock().getType())) {
                    jumpStartMotionY /= 10;
                }
                /*
                this.air = isOnGround ? 0 : this.air + 1;
                this.ground = isOnGround ? this.ground + 1 : 0;
                 */
                boolean isOnWater = this.isOnWater();
                double pr2;
                if (this.isOnSlime()) this.allowMinorGravityChange = true;
                if (ground) {
                    if (this.protocol.getLocation().getY() > this.lastGroundY)
                        allowLowJump = true;
                    if (this.ground != 1) {
                        this.setPredict(0.0);
                        if (this.abs(motion) < 0.08 && this.abs(motion) > 0.0
                                && (this.isBlocksOnTop(to)
                                || this.isOnSlime()
                                || isNearGlideBlocks())) {
                            this.setPredict(motion);
                        }
                    } else {
                        pr2 = this.motionY -= gravity;
                        pr2 *= 0.9800000190734863;
                        this.setPredict(0.0);
                        if (motion > pr2 && motion < 0.0) {
                            this.setPredict(motion);
                        } else if (cringeBlockClimb && motion < 0.5) {
                            // skull
                            this.setPredict(motion);
                        }
                        if (this.isOnSlime()) {
                            this.slime = true;
                        }
                    }

                    if (this.isOnSemi() || this.isOnSemi()) {
                        this.semi = 2;
                    }
                    if (this.semi > 0 && motion != 0.0 && motion < 0.785) {
                        this.setPredict(motion);
                        this.semi--;
                    }
                    this.lastGroundY = this.protocol.getLocation().getY();
                } else if (this.air == 1) {
                    if (motion > 0.0) {
                        this.setPredict((float) jumpStartMotionY);
                    } else {
                        this.setPredict(-gravity * 0.9800000190734863);
                    }

                    if (this.slime) {
                        this.slimeTicks = 3;
                    }
                    if (this.slimeTicks > 0) {
                        pr2 = this.jumpMotion() + this.abs(this.fallDistance) * 0.09;
                        if (motion == 0.0) {
                            this.setPredict(0.0);
                        } else if (pr2 > motion) {
                            this.setPredict(motion);
                        }
                        this.slimeTicks--;
                    }
                } else {
                    this.cringeBlockClimb = (this.air == 14);
                    this.motionY -= gravity;
                    this.motionY *= 0.9800000190734863;
                    if (this.motionY < -3.92) {
                        this.motionY = -3.92;
                    }
                    this.slime = false;
                }
                if (p.getComponentY().pistonTick)
                    this.pistonTicks = 2;
                if (this.getDeviantity(motion) > 0.01
                        && p.getComponentY().pistonTick
                        && motion <= 0.25) {
                    this.setPredict(motion);
                    p.getComponentY().pistonTick = false;
                }
                if (pistonTicks > 0) pistonTicks--;
                if (this.getDeviantity(motion) > 0.01
                        && p.getComponentY().pistonTick
                        && motion <= 1.0) {
                    this.setPredict(motion);
                    p.getComponentY().pistonTick = false;
                }
                boolean slimeEvent = (this.getDeviantity(motion) > 0.001)
                        && (p.getComponentY().pistonHandle
                        && this.slime0Tick);
                if (slimeEvent) {
                    p.getComponentY().pistonHandle = false;
                    slimePunch = 4;
                }
                if ((this.isOnSemi() || this.isOnSemi() || this.isOnSlimeHeight()) && motion != 0.0 && motion < 0.51) {
                    this.setPredict(motion);
                }
                pr2 = this.abs(this.abs(motion) - this.abs(this.predictY()));
                if (!this.protocol.claimedVeloGravity.isEmpty() && pr2 > 0.001) {
                    this.velocity(motion);
                }

                double devPreTotal2 = this.abs(this.abs(motion) - this.abs(this.predictY()));

                if (this.climbBlocks > 0 && devPreTotal2 > 0.001 && motion < 0.118 && motion > -0.16) {
                    this.setPredict(motion);
                    this.climbBlocks--;
                } else if (devPreTotal2 > 0.001 && motion < 0.118 && motion > -0.16 && isNearClimbBlocks()) {
                    this.setPredict(motion);
                    this.climbBlocks = 2;
                }

                double dev1 = this.abs(this.abs(motion) - this.abs(this.predictY()));
                if (dev1 > 0.008 && this.isBlocksOnTop(to)) {
                    if (motion < this.predictY() && motion > -0.08) {
                        this.setPredict(motion);
                        this.top = 3;
                        this.topReset = 0;
                    }
                } else if (dev1 > 0.008 && this.top > 0) {
                    if (motion < this.predictY() && motion > -0.08) {
                        this.setPredict(motion);
                        --this.top;
                        this.topReset = 0;
                    }
                } else if (this.topReset > 18 && this.top > 0) {
                    --this.top;
                    this.topReset = 0;
                }
                boolean onWaterBubble = this.isOnWaterBubble();
                if (MultiVersion.isOrGreater(MCVersion.V1_13)
                        && this.protocol.bukkit().isSwimming()) {
                    this.swimming = 2;
                } else if (this.swimming > 0) {
                    this.swimming--;
                }
                double waterLimClimb = (this.isOnSolidGround(to)) ? 0.4 : 0.2;
                double waterLim = (MultiVersion.isOrGreater(MCVersion.V1_13))
                        ? (this.swimming > 0) ? 0.4 : waterLimClimb : waterLimClimb;
                if (dev1 > 0.01 && isOnWater && motion > -3.92
                        && motion < (onWaterBubble ? 0.8 : waterLim)) {
                    this.setPredict(motion);
                    this.sub = (onWaterBubble) ? 8 : 5;
                }

                if (this.abs(this.abs(motion)
                        - this.abs(this.predictY())) > 0.01
                        && this.trident && motion < 4.2) {
                    this.setPredict(motion);
                }

                if (this.sub > 0 && this.abs(this.abs(motion) - this.abs(this.predictY())) < 0.14) {
                    this.sub--;
                    this.setPredict(motion);
                }

                ExtendedPotionEffect slowFalling;
                double pr;
                if (MultiVersion.isOrGreater(MCVersion.V1_9)) {
                    slowFalling = this.protocol.getPotionEffect(PotionEffectType.LEVITATION, 0L);
                    if (slowFalling != null || this.levitation > 0) {
                        if (slowFalling != null) {
                            this.levitation = 2;
                            pr = 0.05 * (double) slowFalling.bukkitEffect.getAmplifier() + 1.0;
                            this.levitationA = slowFalling.bukkitEffect.getAmplifier() + 1;
                        } else {
                            pr = 0.05 * (double) this.levitationA;
                        }
                        if (motion < pr && motion >= 0.0) {
                            this.setPredict(motion);
                            this.fallDistance = 0.0;
                        }

                        if (this.levitation > 0) {
                            --this.levitationA;
                        }
                    }
                }

                if (MultiVersion.isOrGreater(MCVersion.V1_13)) {
                    slowFalling = this.protocol.getPotionEffect(PotionEffectType.SLOW_FALLING, 0L);
                    if (slowFalling != null || this.slowFalling > 0) {
                        if (slowFalling != null) {
                            this.slowFalling = 2;
                            pr = 0.02 * (double) slowFalling.bukkitEffect.getAmplifier() + 0.5;
                            this.slowFallingA = slowFalling.bukkitEffect.getAmplifier() + 1;
                        } else {
                            pr = 0.02 * (double) this.slowFallingA + 0.5;
                        }
                        if (motion < pr && motion < 0.0) {
                            this.setPredict(motion);
                        }

                        if (this.slowFalling > 0) {
                            --this.slowFallingA;
                        }
                    }
                }

                if (this.protocol.isFlying()
                        || this.protocol.isGliding()
                        || this.protocol.flyingTicks > 0) {
                    this.setPredict(motion);
                    this.levitation = 3;
                    this.levitationA = 3;
                }
                if (!ground) {
                    if (this.air == 1) {
                        if (motion < 0.0) {
                            this.fallDistance = motion;
                        }
                    } else if (motion < 0.0) {
                        this.fallDistance += motion;
                    } else {
                        this.fallDistance = 0.0;
                    }
                }
                double devPreTotal = this.abs(this.abs(motion) - this.abs(this.predictY()));

                if (this.glideBlocks > 0 && devPreTotal > 0.001 && motion < 0 && motion > -0.16) {
                    this.setPredict(motion);
                    this.glideBlocks--;
                } else if (devPreTotal > 0.001 && motion < 0 && motion > -0.16 && isNearGlideBlocks()) {
                    this.setPredict(motion);
                    this.glideBlocks = 2;
                }

                if (this.getDeviantity(motion) > 0.001
                        && this.slimePunch > 0
                        && motion > 0 && motion < 1.52) {
                    this.setPredict(motion);
                    this.allowLowMinorAccel = true;
                    this.slimePunch--;
                }
                if (this.getDeviantity(motion) > 0.001 &&
                        this.teleportExcept > 0 && this.getDeviantity(motion) <= this.teleportExcept) {
                    this.setPredict(motion);
                    this.teleportExcept = 0;
                }

                // ignore
                if (this.protocol.getVehicle() != null) {
                    this.setPredict(motion);
                }

                /*
                Brainrot 1.18< version cringe fix...
                 */
                if (!this.protocol.isUsingVersionOrGreater(MCVersion.V1_19)
                        && (air > 5 && air < 10)
                        && (motion < this.predictY() + 0.04)
                        && speed < 0.1) {
                    double allowedDeviantity = 0.076;
                    ExtendedPotionEffect jumpEffect = this.protocol.getPotionEffect(PotionEffectUtils.JUMP, 1L);

                    if (jumpEffect != null) {
                        allowedDeviantity += 0.04 * (double) (jumpEffect.bukkitEffect.getAmplifier() + 1);
                        if (allowedDeviantity > 0.16) allowedDeviantity = 0.16;
                    }

                    if (this.getDeviantity(motion) < allowedDeviantity)
                        this.setPredict(motion);
                }

                /*
                Sometimes after the explosion comes first
                the recoil from the explosion,
                and then with in the next tick already from the recoil,
                let's take into account this nonsense
                 */
                if (this.protocol.getComponentY().explosionTick && this.getDeviantity(motion) < 1.2) {
                    this.setPredict(motion);
                    this.protocol.getComponentY().explosionTick = false;
                }
                if (this.allowMinorGravityChange && this.predictY() > motion
                        && getDeviantity(motion) <= 0.08) {
                    this.setPredict(motion);
                    this.allowMinorGravityChange = false;
                }
                if (this.allowLowMinorAccel && this.predictY() > motion && getDeviantity(motion) <= 0.1) {
                    this.setPredict(motion);
                    this.allowLowMinorAccel = false;
                }
                //protocol.bukkit().sendMessage("t: " + this.protocol.predictedSlimeTicks + " " + getDeviantity(motion));
                if (this.protocol.predictedSlimeTicks > 0 && getDeviantity(motion) < 2.5 && getDeviantity(motion) > 0.01) {
                    this.setPredict(motion);
                    this.allowMinorGravityChange = true;
                    this.protocol.predictedSlimeTicks--;
                }

                double devTotal = this.abs(this.abs(motion) - this.abs(this.predictY()));
                //this.protocol.bukkit().sendMessage("m: " + motion + " p: " + this.predictY() + " air: " + air);
                if (devTotal > ((this.protocol.getVehicle()
                        != null && motion < 0)
                        ? 0.36 : (this.protocol.getVehicle()
                        != null) ? 0.09 : 0.009)) {
                    if (devTotal < 1.9 && this.vhact > 0) {
                        this.setPredict(0.0);
                    } else if (this.protocol.getVehicle() == null) {
                        if (allowLowJump && air == 1
                                && this.predictY() > motion
                                && getDeviantity(motion) <= 0.03) {
                            return;
                        }
                        if (this.isSolidBlock(from.clone().add(0, 0.5, 0))
                                && to.distance(from) < 0.3) {
                            return;
                        }

                        this.vl += devTotal;
                        this.vlCount += 1;

                        //protocol.bukkit().sendMessage("vl: " + vl + " " + devTotal + " (gravity)");
                        if (this.vl > ((this.protocol.getVehicle() != null) ? 0.6 : 0.05)) {
                            StringBuilder alertBuilder = new StringBuilder(String.valueOf(RayUtils.scaleVal(devTotal, 6.0)));
                            if (devTotal < 1.0) {
                                alertBuilder.deleteCharAt(0);
                            }
                            if (this.vlCount > ((this.air == 1) ? 0 : (this.vl > 0.3) ? 0 : (this.vl > 0.16) ? 1 : 3)) {
                                this.cancel("simulation[gravity], y: " + alertBuilder, this.protocol.getFromLocation());
                            }
                            //this.protocol.bukkit().sendMessage("hacks: " + devTotal);
                            /*
                            Bukkit.getScheduler().runTask(Register.plugin, () -> {
                                this.protocol.bukkit().teleport(from);
                            });
                             */
                        }

                        this.setPredict(motion);
                        if (this.vl > 0) this.vl -= 0.004;
                        if (this.vlCount > 0) this.vlCount -= 0.3;
                        this.vlCount /= 1.08;
                        this.vl /= (this.vlCount < 4) ? 1.17 : 1.04;
                    }
                } else {
                    ++this.topReset;
                    this.vl /= 1.01;
                    if (this.topReset == 48) {
                        this.topReset = 0;
                    }
                }
            }
            slime0Tick = (this.isOnSlimeWide() || this.isOnSlimeWide());
            if (this.vhact > 0) this.vhact--;
        });
    }

    void vhAct() {
        this.call(() -> this.vhact = 2);
    }

    private double getDeviantity(double motion) {
        return this.abs(this.abs(motion) - this.abs(this.predictY()));
    }

    private void velocity(double motion) {
        CPlayerVelocityEvent claimed = null;

        for (CPlayerVelocityEvent velo : this.protocol.claimedVeloGravity) {
            if (this.veloIsValid(motion, velo.getVelocity().getY())) {
                this.setPredict(motion);
                this.allowMinorGravityChange = true;
                claimed = velo;
                break;
            }
        }
        if (claimed != null) this.protocol.claimedVeloGravity.remove(claimed);
    }

    private boolean veloIsValid(double motion, double velo) {
        return abs(this.abs(motion) - this.abs(velo)) < 0.08;
    }

    void teleport() {
        this.call(() -> {
            this.air = 0;
            this.ground = 0;
            this.teleportExcept = 0.35;
            this.vl /= 1.5;
            this.setPredict(0.0);
        });
    }

    private void ground(boolean isOnGround) {
        //if (limToUpdate > 1) return;
        boolean ground = (this.protocol.getVehicle() != null)
                ? this.protocol.getVehicle().isOnGround() : isOnGround;
        this.ground = ground ? this.ground + 1 : 0;
        this.air = ground ? 0 : this.air + 1;
    }

    void groundSp(boolean isOnGround) {
        this.call(() -> this.ground(isOnGround));
    }

    void trident() {
        this.call(() -> {
            this.trident = true;
            this.levitation = 13;
            this.levitationA = 4;
        });
    }

    private double jumpMotion() {
        double jumpPower = 0.42;
        ExtendedPotionEffect jumpEffect = this.protocol.getPotionEffect(PotionEffectUtils.JUMP, 1L);

        if (jumpEffect != null) {
            jumpPower += 0.1 * (double) (jumpEffect.bukkitEffect.getAmplifier() + 1);
        }

        return jumpPower;
    }

    private double abs(double v) {
        return Math.abs(v);
    }

    private double predictY() {
        return this.motionY;
    }

    private void setPredict(double v) {
        this.motionY = v;
    }

    private boolean isOnWater() {
        return this.protocol.getEnvironment().isLiquid();
    }

    private boolean isOnWaterBubble() {
        return this.protocol.getEnvironment().isBubble();
    }

    private boolean isOnSemi() {
        return this.protocol.getEnvironment().isSemi();
    }

    private boolean isNearGlideBlocks() {
        return this.protocol.getEnvironment().isSemi();
    }

    private boolean atJumpModifyBlock() {
        return this.protocol.getEnvironment().isJumpModify();
    }

    private boolean isNearClimbBlocks() {
        return this.protocol.getEnvironment().isClimb();
    }

    private boolean isOnSlime() {
        return this.protocol.getEnvironment().isSlime();
    }

    private boolean isOnSlimeWide() {
        return this.protocol.getEnvironment().isSlimeWide();
    }

    private boolean isOnSlimeHeight() {
        return this.protocol.getEnvironment().isSlimeHeight();
    }

    private boolean isBlocksOnTop(Location location) {
        double x = location.getX();
        double y = location.getY() - 0.1;
        double z = location.getZ();

        for (int dx = -2; dx <= 2; ++dx) {
            for (double dy = 1.0; dy <= 2.0; dy += 0.5) {
                for (int dz = -2; dz <= 2; ++dz) {
                    Material material = this.protocol.packetWorld.getBlock(
                            new Location(this.protocol.getWorld(), x + (double) dx * 0.5, y + dy, z + (double) dz * 0.5)
                    );

                    if (material != null && BlockUtils.isSolid(material)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private boolean isSolidBlock(Location location) {
        double x = location.getX(),
                y = location.getY(),
                z = location.getZ();

        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dz = -1; dz <= 1; dz++) {
                    Material material = this.protocol.packetWorld.getBlock(
                            new Location(
                                    this.protocol.getWorld(),
                                    x + dx * 0.3,
                                    y + dy * 0.3,
                                    z + dz * 0.3)
                    );

                    if (material == null) {
                        continue;
                    }
                    if (BlockUtils.isSolid(material)) {
                        return true;
                    }
                }
            }
        }
        return false;
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
                                    x + dx,
                                    y + dy,
                                    z + dz)
                    );

                    if (material == null) {
                        continue;
                    }
                    if (BlockUtils.isSolid(material)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

}