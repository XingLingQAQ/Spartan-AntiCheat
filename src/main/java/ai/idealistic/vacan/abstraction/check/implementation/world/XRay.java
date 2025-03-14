package ai.idealistic.vacan.abstraction.check.implementation.world;

import ai.idealistic.vacan.abstraction.check.CheckDetection;
import ai.idealistic.vacan.abstraction.check.CheckEnums;
import ai.idealistic.vacan.abstraction.check.CheckRunner;
import ai.idealistic.vacan.abstraction.check.definition.ImplementedDetection;
import ai.idealistic.vacan.abstraction.profiling.MiningHistory;
import ai.idealistic.vacan.abstraction.profiling.PlayerProfile;
import ai.idealistic.vacan.abstraction.protocol.PlayerProtocol;
import ai.idealistic.vacan.functionality.tracking.ResearchEngine;
import ai.idealistic.vacan.utils.math.AlgebraUtils;
import ai.idealistic.vacan.utils.math.statistics.StatisticsMath;
import org.bukkit.World;
import org.bukkit.event.block.BlockBreakEvent;

import java.util.Collection;

public class XRay extends CheckRunner {

    private static final double threshold = 0.9999;

    private static double[] getStatistics(World.Environment environment, MiningHistory.MiningOre ore) {
        Collection<PlayerProfile> profiles = ResearchEngine.getPlayerProfiles();
        int factorRequirement = 10;

        if (profiles.size() < factorRequirement) {
            return new double[]{};
        } else {
            int profileCount = 0;
            double mean = 0.0,
                    variance = 0.0;

            for (PlayerProfile profile : profiles) {
                double minesToTimeRatio = profile.getMiningHistory(ore).getMinesToTimeRatio(environment);

                if (minesToTimeRatio > 0.0) {
                    profileCount++;
                    mean += minesToTimeRatio;
                }
            }
            mean /= profileCount;

            for (PlayerProfile profile : profiles) {
                double minesToTimeRatio = profile.getMiningHistory(ore).getMinesToTimeRatio(environment);

                if (minesToTimeRatio > 0.0) {
                    double difference = minesToTimeRatio - mean;
                    variance += difference * difference;
                }
            }
            double stdDev = Math.sqrt(variance / (double) profileCount);

            if (profileCount >= factorRequirement) {
                return new double[]{
                        mean / (double) profileCount,
                        Math.sqrt(stdDev / (double) profileCount)
                };
            } else {
                return new double[]{};
            }
        }
    }

    private final ImplementedDetection[] detections;

    public XRay(CheckEnums.HackType hackType, PlayerProtocol protocol) {
        super(hackType, protocol);
        this.detections = new ImplementedDetection[World.Environment.values().length];

        for (World.Environment environment : World.Environment.values()) {
            this.detections[environment.ordinal()] = new ImplementedDetection(
                    this,
                    null,
                    null,
                    environment.name().toLowerCase(),
                    true,
                    1L,
                    1L,
                    1L,
                    1L
            );
        }
    }

    @Override
    protected void handleInternal(boolean cancelled, Object object) {
        if (object instanceof Object[]) {
            Object[] objects = (Object[]) object;
            World.Environment environment = World.Environment.valueOf(objects[0].toString());
            CheckDetection detection = this.detections[environment.ordinal()];

            detection.call(() -> {
                double[] statistics = getStatistics(environment, (MiningHistory.MiningOre) objects[2]);

                if (statistics.length > 0) {
                    double individual = ((MiningHistory) objects[1]).getMinesToTimeRatio(environment);

                    if (individual > 0.0) {
                        double probability = StatisticsMath.getCumulativeProbability(
                                (individual - statistics[0]) / statistics[1]
                        );

                        if (probability >= threshold) {
                            detection.cancel(
                                    "Unfair gathering of " + objects[3]
                                            + " in " + environment.name().toLowerCase() + " world"
                                            + ", surpassed " + AlgebraUtils.cut(probability * 100.0, 2)
                                            + "% of players"
                            );
                        }
                    }
                }
            });
        } else if (object instanceof BlockBreakEvent) {
            BlockBreakEvent event = (BlockBreakEvent) object;

            if (this.prevent()) {
                event.setCancelled(true);
            }
        }
    }

}
