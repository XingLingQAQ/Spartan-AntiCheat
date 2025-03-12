package ai.idealistic.vacan.abstraction.check.implementation.movement.irregularmovements;

import ai.idealistic.vacan.abstraction.check.Check;
import ai.idealistic.vacan.abstraction.check.CheckDetection;
import ai.idealistic.vacan.abstraction.check.CheckRunner;
import ai.idealistic.vacan.abstraction.data.EnvironmentData;
import ai.idealistic.vacan.abstraction.protocol.ExtendedPotionEffect;
import ai.idealistic.vacan.abstraction.protocol.PlayerProtocol;
import ai.idealistic.vacan.utils.minecraft.entity.PlayerUtils;
import org.bukkit.Location;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.potion.PotionEffectType;

public class IMSimulationVehicle extends CheckDetection {

    private double vl = 0, oldY = 0, oldSpeed = 0, oldAccel = 0;
    private int ground = 0, air = 0, ticks = 0, postFly = 0, postSpeed = 0;
    private Location from = null;

    public IMSimulationVehicle(CheckRunner executor) {
        super(
                executor,
                null,
                Check.DetectionType.PACKETS,
                "vehicle",
                true,
                DEFAULT_AVERAGE_TIME,
                TIME_TO_NOTIFY * 2L,
                TIME_TO_PREVENT * 2L,
                TIME_TO_PUNISH * 2L
        );
    }

    void run() {
        this.call(() -> {
            PlayerProtocol p = this.protocol;
            Entity vehicleEntity = p.getVehicle();

            if (vehicleEntity == null) {
                this.ticks = 0;
                this.vl = 0;
                this.postFly = 0;
                this.postSpeed = 0;
                this.air = 0;
                this.ground = 0;
                this.from = null;
                this.oldY = 0;
                this.oldSpeed = 0;
                this.oldAccel = 0;
                return;
            }
            if (!(vehicleEntity instanceof Vehicle)) {
                return;
            }
            Vehicle vehicle = (Vehicle) vehicleEntity;
            this.ticks++;
            boolean isOnGround = p.getVehicle().isOnGround();
            this.ground(isOnGround);
            Location to = p.getVehicle().getLocation();
            if (from == null) from = to.clone();
            double dx = to.getX() - from.getX();
            double dz = to.getZ() - from.getZ();
            double speed = Math.sqrt(dx * dx + dz * dz);
            double accel = getDeviantity(speed, (oldSpeed * 0.91));
            double diffAccel = getDeviantity(accel, oldAccel);
            double y = to.getY() - from.getY();
            final EnvironmentData ec = protocol.getEnvironment();
            double prV = (vehicle instanceof Boat) ? 0.04 : 0.08 * 0.98f;
            double pr = Math.abs(Math.abs(y) - Math.abs(oldY - prV));
            //protocol.bukkit().sendMessage("p: " + y + " " + (oldY - prV) + " " + air);
            if (pr > 1e-4 && air > 4 && (vehicle instanceof Boat)) {
                if (postFly < 0) postFly = 0;
                this.postFly++;
                //this.protocol.bukkit().sendMessage("p: " + postFly);
                if (this.postFly > 10) {
                    this.cancel("prediction vehicle gravity: " + pr, this.protocol.getFromLocation(), 0, true);
                    this.postFly = 9;
                }
            } else if (!(vehicle instanceof Minecart) && getDeviantity(oldY, prV) < 1e-10) {
                this.postFly++;
                if (this.postFly > 7) {
                    this.cancel("prediction vehicle gravity: " + pr, this.protocol.getFromLocation(), 0, true);
                    this.postFly = 6;
                }
            } else if (this.postFly > 0) {
                this.postFly -= 2;
            }
            if (diffAccel < 1e-10 && speed > 0.1 && !(vehicle instanceof Minecart)) {
                if (postSpeed < 0) postSpeed = 0;
                this.postSpeed++;
                if (this.postSpeed > 8) {
                    this.cancel("prediction vehicle speed: " + diffAccel, this.protocol.getFromLocation());
                    this.postSpeed = 7;
                }
            } else if (this.postSpeed > 0) this.postSpeed -= 2;
            if (!ec.isAllFalse()) this.postFly -= 2;
            if (ticks > 1) { // check
                double allowedSpeed = 1.0;
                double allowedVertical = 1.0;
                if (vehicle instanceof Horse) {
                    if (air == 0) allowedSpeed = 1.05;
                    else if (air < 4) allowedSpeed = 1.7;
                    else if (air < 14) allowedSpeed = 1.2;
                    else allowedSpeed = 0.4;

                    if (air < 2) allowedVertical = 1.5;
                    else if (air < 4) allowedVertical = 1.2 + (oldY - (0.98f * 0.08) + 0.03);
                    else if (air < 18) allowedVertical = oldY - (0.98f * 0.08) + 0.6;
                    else allowedVertical = oldY - (0.98f * 0.08);
                }
                allowedSpeed *= this.multiplex();
                if (ec.isSemi()) {
                    allowedSpeed += 1.0;
                    allowedVertical += 0.75;
                }
                if (ec.isGlide() || ec.isLiquid()) {
                    allowedSpeed += 0.2;
                    if (allowedVertical < 0) allowedVertical = 0;
                    allowedVertical += 0.3;
                }
                if (ec.isSlimeHeight()) {
                    allowedSpeed += 1.5;
                    allowedVertical += 1.5;
                }
                if (speed > allowedSpeed) {
                    this.vl += speed / allowedSpeed;
                    if (this.vl > 12.0) {
                        this.cancel("vehicle limit speed: " + speed, this.protocol.getFromLocation());
                        this.vl /= 1.3;
                    }
                }
                /*
                protocol.getBukkit().sendMessage(
                                "s: " + y + " y: "
                                                + allowedVertical + " " + isOnGround);
                 */

                if (y > allowedVertical) {
                    this.vl += Math.abs(y / allowedVertical);
                    if (this.vl > 15.0) {
                        this.cancel("vehicle limit gravity: " + y, this.protocol.getFromLocation());
                        this.vl /= 1.3;
                    }
                }
                this.vl /= 1.2;
            }
            from = to.clone();
            oldY = y;
            oldSpeed = speed;
            oldAccel = accel;
        });
    }

    private double multiplex() {
        double m = 1.0;
        ExtendedPotionEffect speedEffect = this.protocol.getPotionEffect(PotionEffectType.SPEED, 5);
        Location l = this.protocol.getLocation().clone().add(0, -0.5, 0);
        Location lF = this.protocol.getFromLocation().clone().add(0, -0.5, 0);
        if (PlayerUtils.soulSpeed && this.protocol.bukkit().getInventory().getBoots() != null) {
            int e = this.protocol.bukkit().getInventory().getBoots().getEnchantmentLevel(Enchantment.SOUL_SPEED);
            double v = (e + 1) * 0.4;
            m += v;
        }
        if (speedEffect != null)
            m += (double) (speedEffect.bukkitEffect.getAmplifier() + 1) * 0.2;
        if (this.isOnIce(l) || this.isOnSlime(l) || this.isOnIce(lF) || this.isOnSlime(lF))
            m *= 1.6;
        if (this.isOnWater() || this.isOnWater()) {
            ExtendedPotionEffect dolphinEffect = PlayerUtils.dolphinsGrace
                    ? this.protocol.getPotionEffect(PotionEffectType.DOLPHINS_GRACE, 0)
                    : null;
            if (dolphinEffect != null) m *= 6.2;
        }
        if (this.isOnSemi(l)) m *= 1.4;
        if (this.protocol.isGliding()) m *= 18.0;
        if (this.protocol.isFlying() || this.protocol.flyingTicks > 0) m *= 25.0;
        m *= (this.protocol.bukkit().getWalkSpeed() * 5);
        return m;
    }

    private static double getDeviantity(double a, double b) {
        return abs(abs(a) - abs(b));
    }

    void teleport() {
        this.call(() -> {
            this.air = 0;
            this.ticks = 0;
        });
    }

    private void ground(boolean isOnGround) {
        this.call(() -> {
            this.air = isOnGround ? 0 : this.air + 1;
            this.ground = isOnGround ? this.ground + 1 : 0;
        });
    }

    private static double abs(double v) {
        return Math.abs(v);
    }

    private boolean isOnWater() {
        return this.protocol.getEnvironment().isLiquid();
    }

    private boolean isOnIce(Location location) {
        return this.protocol.getEnvironment().isIce();
    }

    private boolean isOnSemi(Location location) {
        return this.protocol.getEnvironment().isSemi();
    }

    private boolean isOnSlime(Location location) {
        return this.protocol.getEnvironment().isSlime();
    }

}