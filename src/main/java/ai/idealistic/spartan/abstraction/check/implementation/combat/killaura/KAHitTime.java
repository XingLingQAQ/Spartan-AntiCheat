package ai.idealistic.spartan.abstraction.check.implementation.combat.killaura;

import ai.idealistic.spartan.abstraction.check.Check;
import ai.idealistic.spartan.abstraction.check.CheckDetection;
import ai.idealistic.spartan.abstraction.check.CheckRunner;
import ai.idealistic.spartan.listeners.protocol.standalone.LegacyLagCompensationListener;

public class KAHitTime extends CheckDetection {

    KAHitTime(CheckRunner executor) {
        super(executor, null, Check.DetectionType.PACKETS, "hit_time", true,
                DEFAULT_AVERAGE_TIME * 4L,
                TIME_TO_NOTIFY,
                TIME_TO_PREVENT,
                TIME_TO_PUNISH);
    }

    private int
            vl = 0,
            vlAnalysis = 0,
            nullPacket = 0,
            devianceTotal = 0,
            packets = 0,
            lowPackets = 0;

    private long
            lastHitTime = 0,
            oldHitTiming = 0;

    void run() {
        this.call(() -> {
            long hitTime = System.currentTimeMillis() - lastHitTime;
            int deviance = (int) Math.abs(hitTime - oldHitTiming);
            devianceTotal += deviance;
            packets += 1;

            if (this.protocol.getPing() > 1_000) {
                return;
            }
            if (deviance < 5 && deviance != 0) {
                lowPackets++;
            }
            if (hitTime == 0) {
                nullPacket = 2;
            }
            if (nullPacket > 1) {
                vl += 55;
                if (vl >= 300) {
                    cancel("switch", this.protocol.getFromLocation());
                }
            }
            if (packets >= 5 && LegacyLagCompensationListener.getPlayerTicksDelay(this.protocol.getEntityId()) < 2) {
                if (devianceTotal < 35) {
                    vlAnalysis += (devianceTotal < 10) ? 150 : 75;
                    if (vlAnalysis >= 300) {
                        cancel("post(linear-analysis)", this.protocol.getFromLocation());
                    }
                } else if (devianceTotal > 45 && devianceTotal < 55) {
                    vlAnalysis += 150;
                    if (vlAnalysis >= 300) {
                        cancel("post(default)", this.protocol.getFromLocation());
                    }
                } else if (lowPackets >= 3) {
                    vlAnalysis += 100;
                    if (vlAnalysis >= 300) {
                        cancel("post(low)", this.protocol.getFromLocation());
                    }
                } else {
                    if (vlAnalysis > 0) vlAnalysis -= 50;
                }
                packets = 0;
                devianceTotal = 0;
                lowPackets = 0;
            }
            if (vl > 0) {
                vl -= 5;
            }
            if (nullPacket > 0) {
                nullPacket -= 1;
            }
            lastHitTime = System.currentTimeMillis();
            oldHitTiming = hitTime;
        });
    }

}
