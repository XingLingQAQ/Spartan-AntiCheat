package ai.idealistic.spartan.abstraction.check.implementation.movement.irregularmovements;

import ai.idealistic.spartan.abstraction.check.Check;
import ai.idealistic.spartan.abstraction.check.CheckDetection;
import ai.idealistic.spartan.abstraction.check.CheckRunner;
import ai.idealistic.spartan.abstraction.data.Buffer;
import ai.idealistic.spartan.abstraction.data.EnvironmentData;
import ai.idealistic.spartan.abstraction.protocol.PlayerProtocol;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class IMSimulationElytra extends CheckDetection {

    private double vl = 0, oldY = 0, oldSpeed = 0, oldAccel = 0;
    private int ground = 0, air = 0, ticks = 0, postSpeed = 0;
    private Location from = null;
    private final Buffer.IndividualBuffer buffer;

    public IMSimulationElytra(CheckRunner executor) {
        super(
                executor,
                null,
                Check.DetectionType.PACKETS,
                "elytra",
                false
        );
        this.buffer = new Buffer.IndividualBuffer();
    }

    void runExploit() {
        this.call(() -> {
            if (protocol.getVehicle() == null) {
                PlayerInventory inventory = protocol.getInventory();
                ItemStack i = inventory.getChestplate();

                if (i != null) {
                    if (i.getType() == Material.ELYTRA) {
                        if (i.getDurability() < 432) {
                            double buffer = this.buffer.count(1, 20),
                                    threshold = 15;

                            if (buffer >= threshold) {
                                cancel(
                                        "elytra(glide-spam)",
                                        this.protocol.getFromLocation(),
                                        0,
                                        true
                                );

                                if (this.prevent()) {
                                    protocol.bukkit().setGliding(false);
                                }
                            }
                        } else {
                            cancel(
                                    "elytra(no-durability-glide)",
                                    this.protocol.getFromLocation(),
                                    0,
                                    true
                            );

                            if (this.prevent()) {
                                protocol.bukkit().setGliding(false);
                            }
                        }
                    } else {
                        cancel(
                                "elytra(no-elytra-glide)",
                                this.protocol.getFromLocation(),
                                0,
                                true
                        );

                        if (this.prevent()) {
                            protocol.bukkit().setGliding(false);
                        }
                    }
                } else {
                    cancel(
                            "elytra(no-chestplate-item-glide)",
                            this.protocol.getFromLocation(),
                            0,
                            true
                    );

                    if (this.prevent()) {
                        protocol.bukkit().setGliding(false);
                    }
                }
            } else {
                cancel(
                        "elytra(vehicle-glide)",
                        this.protocol.getFromLocation(),
                        0,
                        true
                );

                if (this.prevent()) {
                    protocol.bukkit().setGliding(false);
                }
            }
        });
    }

    void runSimulation() {
        this.call(() -> {
            PlayerProtocol p = this.protocol;

            if (!p.isGliding()) {
                this.ticks = 0;
                this.air = 0;
                this.ground = 0;
                this.vl = 0;
                this.postSpeed = 0;
                this.from = null;
                this.oldY = 0;
                this.oldSpeed = 0;
                this.oldAccel = 0;
                return;
            }
            this.ticks++;
            boolean isOnGround = p.isOnGround();
            this.ground(isOnGround);
            Location to = p.getLocation();
            if (from == null) from = to.clone();
            double dx = to.getX() - from.getX();
            double dz = to.getZ() - from.getZ();
            double speed = Math.sqrt(dx * dx + dz * dz);
            double accel = getDeviance(speed, (oldSpeed * 0.91));
            double diffAccel = getDeviance(accel, oldAccel);
            double y = to.getY() - from.getY();
            final EnvironmentData ec = protocol.getEnvironment();
            double pr = Math.abs(Math.abs(y) - Math.abs(oldY));
            //protocol.bukkit().sendMessage("p: " + speed + " " + y);
            if (ticks > 1) {
                if (speed > 0.05 && ec.isAllFalse() && diffAccel < 1e-8) {
                    //this.protocol.bukkit().sendMessage("d: " + postSpeed);
                    this.postSpeed += (diffAccel == 0) ? 3 : 1;
                    if (this.postSpeed > 12) {
                        this.cancel("invalid elytra speed: " + diffAccel, this.protocol.getFromLocation(), 0, true);
                        this.postSpeed = 8;
                    }
                } else if (this.postSpeed > 0) this.postSpeed -= 2;
                if (pr == 0.0 && ec.isAllFalse()) {
                    this.vl += 2.0;
                    if (this.vl >= 5.0) {
                        this.cancel("invalid elytra gravity: " + pr, this.protocol.getFromLocation(), 0, true);
                        this.vl /= 1.3;
                    }
                }
                if (speed > 2.4 || y > 1.7) {
                    this.cancel("elytra limit", this.protocol.getFromLocation(), 0, true);
                }
                this.vl /= 1.15;
            }
            from = to.clone();
            oldY = y;
            oldSpeed = speed;
            oldAccel = accel;
        });
    }

    private static double getDeviance(double a, double b) {
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

}