package ai.idealistic.vacan.abstraction.check.implementation.combat.killaura.movedirection;

import org.bukkit.entity.Entity;

public class AttackHandle {

    final Entity target;
    final long time;

    AttackHandle(Entity target, long timeMillis) {
        this.time = timeMillis;
        this.target = target;
    }

}
