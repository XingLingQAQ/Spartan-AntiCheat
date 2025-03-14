package ai.idealistic.vacan.abstraction.check.implementation.combat;

import ai.idealistic.vacan.abstraction.check.Check;
import ai.idealistic.vacan.abstraction.check.CheckDetection;
import ai.idealistic.vacan.abstraction.check.CheckEnums;
import ai.idealistic.vacan.abstraction.check.CheckRunner;
import ai.idealistic.vacan.abstraction.check.definition.ImplementedDetection;
import ai.idealistic.vacan.abstraction.event.PlayerLeftClickEvent;
import ai.idealistic.vacan.abstraction.event.PlayerTickEvent;
import ai.idealistic.vacan.abstraction.protocol.PlayerProtocol;
import ai.idealistic.vacan.functionality.server.TPS;
import ai.idealistic.vacan.utils.math.AlgebraUtils;
import ai.idealistic.vacan.utils.math.statistics.StatisticsMath;
import ai.idealistic.vacan.utils.minecraft.entity.CombatUtils;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class FastClicks extends CheckRunner {

    private long lastTickTime = System.currentTimeMillis();
    private final List<Integer> buffer = new CopyOnWriteArrayList<>();

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
            samples = new CopyOnWriteArrayList<>(),
            samplesMassive = new CopyOnWriteArrayList<>();

    public FastClicks(CheckEnums.HackType hackType, PlayerProtocol protocol) {
        super(hackType, protocol);
        this.cps = new ImplementedDetection(this, null, Check.DetectionType.PACKETS, "cps", true);
        this.floorTempo = new ImplementedDetection(this, null, Check.DetectionType.PACKETS, "floor_tempo", true,
                CheckDetection.DEFAULT_AVERAGE_TIME * 2L,
                CheckDetection.TIME_TO_NOTIFY,
                CheckDetection.TIME_TO_PREVENT,
                CheckDetection.TIME_TO_PUNISH);
        this.deviation = new ImplementedDetection(this, null, Check.DetectionType.PACKETS, "deviation", true,
                CheckDetection.DEFAULT_AVERAGE_TIME * 2L,
                CheckDetection.TIME_TO_NOTIFY,
                CheckDetection.TIME_TO_PREVENT,
                CheckDetection.TIME_TO_PUNISH);
        this.skewness = new ImplementedDetection(this, null, Check.DetectionType.PACKETS, "skewness", true,
                CheckDetection.DEFAULT_AVERAGE_TIME * 2L,
                CheckDetection.TIME_TO_NOTIFY,
                CheckDetection.TIME_TO_PREVENT,
                CheckDetection.TIME_TO_PUNISH);
        this.kurtosis = new ImplementedDetection(this, null, Check.DetectionType.PACKETS, "kurtosis", true,
                CheckDetection.DEFAULT_AVERAGE_TIME * 2L,
                CheckDetection.TIME_TO_NOTIFY,
                CheckDetection.TIME_TO_PREVENT,
                CheckDetection.TIME_TO_PUNISH);
        this.variance = new ImplementedDetection(this, null, Check.DetectionType.PACKETS, "variance", true,
                CheckDetection.DEFAULT_AVERAGE_TIME * 2L,
                CheckDetection.TIME_TO_NOTIFY,
                CheckDetection.TIME_TO_PREVENT,
                CheckDetection.TIME_TO_PUNISH);
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
            if (this.prevent()) {
                ((PlayerInteractEvent) object).setCancelled(true);
            }
        }
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
