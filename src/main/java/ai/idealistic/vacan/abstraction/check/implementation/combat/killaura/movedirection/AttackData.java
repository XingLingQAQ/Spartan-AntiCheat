package ai.idealistic.vacan.abstraction.check.implementation.combat.killaura.movedirection;

import java.util.UUID;

public class AttackData {

    final double accuracy;
    final float yaw, pitch;
    final long time;
    final UUID target;

    AttackData(double accuracy, float yaw, float pitch, long time, UUID target) {
        this.accuracy = accuracy;
        this.yaw = yaw;
        this.pitch = pitch;
        this.time = time;
        this.target = target;
    }

}
