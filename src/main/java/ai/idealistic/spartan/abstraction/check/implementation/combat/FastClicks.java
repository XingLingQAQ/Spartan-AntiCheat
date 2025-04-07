package ai.idealistic.spartan.abstraction.check.implementation.combat;

import ai.idealistic.spartan.abstraction.check.Check;
import ai.idealistic.spartan.abstraction.check.CheckDetection;
import ai.idealistic.spartan.abstraction.check.CheckEnums;
import ai.idealistic.spartan.abstraction.check.CheckRunner;
import ai.idealistic.spartan.abstraction.check.definition.ImplementedDetection;
import ai.idealistic.spartan.abstraction.data.Buffer;
import ai.idealistic.spartan.abstraction.event.PlayerLeftClickEvent;
import ai.idealistic.spartan.abstraction.event.PlayerTickEvent;
import ai.idealistic.spartan.abstraction.protocol.PlayerProtocol;
import ai.idealistic.spartan.functionality.server.TPS;
import ai.idealistic.spartan.utils.java.ConcurrentList;
import ai.idealistic.spartan.utils.math.AlgebraUtils;
import ai.idealistic.spartan.utils.math.statistics.StatisticsMath;
import ai.idealistic.spartan.utils.minecraft.entity.CombatUtils;
import org.bukkit.Material;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.PlayerInventory;

import java.util.List;

public class FastClicks extends CheckRunner {

    private final CheckDetection
            bowForceDetection,
            bowShotsDetection;
    private long interactTime;
    private final Buffer.IndividualBuffer bowForce, bowShots;

    private long lastTickTime = System.currentTimeMillis();
    private final List<Integer> buffer = new ConcurrentList<>();

    {
        for (int i = 0; i < 7; i++) buffer.add(0);
    }

    private final CheckDetection
            cps,
            floorTempo,
            deviation,
            skewness,
            kurtosis,
            variance;
    private final List<Long>
            samples = new ConcurrentList<>(),
            samplesMassive = new ConcurrentList<>();

    public FastClicks(CheckEnums.HackType hackType, PlayerProtocol protocol) {
        super(hackType, protocol);
        this.cps = new ImplementedDetection(this, null, Check.DetectionType.PACKETS, "cps", true);
        this.floorTempo = new ImplementedDetection(this, null, Check.DetectionType.PACKETS, "floor_tempo", true,
                CheckDetection.DEFAULT_AVERAGE_TIME * 4L,
                CheckDetection.TIME_TO_NOTIFY,
                CheckDetection.TIME_TO_PREVENT,
                CheckDetection.TIME_TO_PUNISH);
        this.deviation = new ImplementedDetection(this, null, Check.DetectionType.PACKETS, "deviation", true,
                CheckDetection.DEFAULT_AVERAGE_TIME * 4L,
                CheckDetection.TIME_TO_NOTIFY,
                CheckDetection.TIME_TO_PREVENT,
                CheckDetection.TIME_TO_PUNISH);
        this.skewness = new ImplementedDetection(this, null, Check.DetectionType.PACKETS, "skewness", true,
                CheckDetection.DEFAULT_AVERAGE_TIME * 4L,
                CheckDetection.TIME_TO_NOTIFY,
                CheckDetection.TIME_TO_PREVENT,
                CheckDetection.TIME_TO_PUNISH);
        this.kurtosis = new ImplementedDetection(this, null, Check.DetectionType.PACKETS, "kurtosis", true,
                CheckDetection.DEFAULT_AVERAGE_TIME * 4L,
                CheckDetection.TIME_TO_NOTIFY,
                CheckDetection.TIME_TO_PREVENT,
                CheckDetection.TIME_TO_PUNISH);
        this.variance = new ImplementedDetection(this, null, Check.DetectionType.PACKETS, "variance", true,
                CheckDetection.DEFAULT_AVERAGE_TIME * 4L,
                CheckDetection.TIME_TO_NOTIFY,
                CheckDetection.TIME_TO_PREVENT,
                CheckDetection.TIME_TO_PUNISH);
        this.bowForceDetection = new ImplementedDetection(this, null, null, "bow_force", true,
                CheckDetection.DEFAULT_AVERAGE_TIME * 4L,
                CheckDetection.TIME_TO_NOTIFY,
                CheckDetection.TIME_TO_PREVENT,
                CheckDetection.TIME_TO_PUNISH);
        this.bowShotsDetection = new ImplementedDetection(this, null, null, "bow_shots", true,
                CheckDetection.DEFAULT_AVERAGE_TIME * 4L,
                CheckDetection.TIME_TO_NOTIFY,
                CheckDetection.TIME_TO_PREVENT,
                CheckDetection.TIME_TO_PUNISH);
        this.bowForce = new Buffer.IndividualBuffer();
        this.bowShots = new Buffer.IndividualBuffer();
    }

    @Override
    protected void handleInternal(boolean cancelled, Object object) {
        if (object instanceof BlockBreakEvent
                || object instanceof PlayerDropItemEvent) {
            this.addDisableCause(
                    null,
                    null,
                    AlgebraUtils.integerCeil(TPS.maximum)
            );
        } else if (object instanceof PlayerLeftClickEvent) {
            this.run(((PlayerLeftClickEvent) object).delay);
        } else if (object instanceof PlayerTickEvent) {
            this.tick((PlayerTickEvent) object);
        } else if (object instanceof PlayerInteractEvent) {
            PlayerInteractEvent event = (PlayerInteractEvent) object;

            if (this.prevent()) {
                event.setCancelled(true);
            }
            if (event.getAction() == Action.RIGHT_CLICK_AIR) {
                PlayerInventory inventory = this.protocol.getInventory();

                if (inventory.getItemInHand().getType() == Material.BOW
                        && inventory.contains(Material.ARROW)) {
                    this.interactTime = System.currentTimeMillis();
                }
            }
        } else if (object instanceof EntityShootBowEvent) {
            EntityShootBowEvent event = (EntityShootBowEvent) object;

            if (this.protocol.getItemInHand().getType() == Material.BOW) {
                checkBowShots(event);
                checkBowForce(event);
            }
        }
    }

    private void checkBowForce(EntityShootBowEvent event) {
        this.bowForceDetection.call(() -> {
            if (event.getForce() != 1.0f) {
                return;
            }
            long timePassed = System.currentTimeMillis() - interactTime;

            if (timePassed > 500L) {
                return;
            }
            double buffer = bowForce.count(1, 20),
                    threshold = 2;

            if (buffer >= threshold) {
                bowForce.reset();
                this.bowForceDetection.cancel(
                        "bow-force, ms: " + timePassed
                );

                if (this.prevent()) {
                    event.setCancelled(true);
                }
            }
        });
    }

    private void checkBowShots(EntityShootBowEvent event) {
        this.bowShotsDetection.call(() -> {
            double buffer = bowShots.count(1, 10),
                    threshold = 7;

            if (buffer >= threshold) {
                this.bowShotsDetection.cancel(
                        "bow-shots"
                );

                if (this.prevent()) {
                    event.setCancelled(true);
                }
            }
        });
    }

    private void run(long delay) {
        if (this.cps.canCall()
                || this.floorTempo.canCall()
                || this.deviation.canCall()
                || this.skewness.canCall()
                || this.kurtosis.canCall()
                || this.variance.canCall()) {
            if (!tickingCorrect()) return;
            this.samples.add(delay);
            this.samplesMassive.add(delay);
            if (this.samples.size() >= 20) {
                { // logic
                    final double cps = StatisticsMath.getCps(this.samples) * 50;
                    final double limit = hackType.getCheck().getNumericalOption(
                            "clicks_per_second_limit",
                            15
                    );
                    final double variance = StatisticsMath.getVariance(this.samples);
                    final double kurtosis = StatisticsMath.getKurtosis(this.samples);
                    if (cps > limit) this.cps.cancel("limit ("
                            + (int) cps + "/" + (int) limit + ")");
                    { // floor
                        final double floor = Math.abs(Math.floor(cps) - cps);
                        if (floor < 0.07) {
                            if (addToBuffer(1, 2) > 3) {
                                this.floorTempo.call(() ->
                                        this.floorTempo.cancel("floor tempo (" + floor + ")")
                                );
                                addToBuffer(1, -1);
                            }
                        } else {
                            addToBuffer(1, -1);
                        }
                    }
                    { // kurtosis
                        if (kurtosis < 45000) {
                            if (addToBuffer(4, 3) > 6) {
                                this.kurtosis.call(() ->
                                        this.kurtosis.cancel("kurtosis (" + (int) kurtosis + ")")
                                );
                                addToBuffer(4, -1);
                            }
                        } else {
                            addToBuffer(4, -2);
                        }
                    }
                    { // variance
                        if (variance < 1100 && variance > 200 && cps > 7) {
                            if (addToBuffer(5, 3) > 6) {
                                this.variance.call(() ->
                                        this.variance.cancel("variance (" + (int) variance + ")")
                                );
                                addToBuffer(5, -1);
                            }
                        } else {
                            addToBuffer(5, -3);
                        }
                    }
                }
                this.samples.clear();
            }
            if (this.samplesMassive.size() >= 50) {
                { // logic
                    final double standardDev = StatisticsMath.getStandardDeviation(this.samplesMassive);
                    final double skewness = StatisticsMath.getSkewness(this.samplesMassive);
                    if (standardDev < 50) {
                        if (addToBuffer(2, 2) > 3) {
                            this.deviation.call(() ->
                                    this.deviation.cancel("low deviation (" + standardDev + ")")
                            );
                            addToBuffer(2, -1);
                        }
                    } else {
                        addToBuffer(2, -1);
                    }
                    if (skewness < -0.01) {
                        if (addToBuffer(3, 2) > 3) {
                            this.skewness.call(() ->
                                    this.skewness.cancel("invalid skewness (" + skewness + ")")
                            );
                            addToBuffer(3, -1);
                        }
                    } else {
                        addToBuffer(3, -2);
                    }
                }
                this.samplesMassive.clear();
            }
        }
    }

    private void tick(PlayerTickEvent event) {
        this.lastTickTime = event.time;
    }

    private int addToBuffer(int i, int v) {
        this.buffer.set(i, Math.max(this.buffer.get(i) + v, 0));
        return this.buffer.get(i);
    }

    private boolean tickingCorrect() {
        return (System.currentTimeMillis() - this.lastTickTime) < 70;
    }

    @Override
    protected boolean canRun() {
        return !this.protocol.getNearbyEntities(
                CombatUtils.maxHitDistance,
                CombatUtils.maxHitDistance,
                CombatUtils.maxHitDistance
        ).isEmpty();
    }

}
