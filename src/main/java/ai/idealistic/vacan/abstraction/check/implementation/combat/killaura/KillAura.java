package ai.idealistic.vacan.abstraction.check.implementation.combat.killaura;

import ai.idealistic.vacan.abstraction.Enums;
import ai.idealistic.vacan.abstraction.check.CheckRunner;
import ai.idealistic.vacan.abstraction.check.implementation.combat.killaura.movedirection.MoveDirection;
import ai.idealistic.vacan.abstraction.event.EntityAttackPlayerEvent;
import ai.idealistic.vacan.abstraction.event.PlayerAttackEvent;
import ai.idealistic.vacan.abstraction.protocol.PlayerProtocol;
import ai.idealistic.vacan.compatibility.necessary.protocollib.ProtocolLib;
import ai.idealistic.vacan.functionality.server.MultiVersion;
import ai.idealistic.vacan.utils.minecraft.entity.CombatUtils;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;

public class KillAura extends CheckRunner {

    private final Irregular irregular;
    private final HitTime hitTime;
    private final MoveDirection moveDirection;
    private final MoveLength moveLength;

    public KillAura(Enums.HackType hackType, PlayerProtocol protocol) {
        super(hackType, protocol);
        irregular = new Irregular(this);
        hitTime = new HitTime(this);
        moveDirection = new MoveDirection(this);
        moveLength = new MoveLength(this);
    }

    boolean canRunEntity(LivingEntity entity) {
        if (ProtocolLib.getVehicle(entity) == null
                && CombatUtils.canCheck(this.protocol, entity)) {
            boolean isPlayer = entity instanceof Player;
            return (isPlayer || !MultiVersion.isOrGreater(MultiVersion.MCVersion.V1_10) || entity.hasAI())
                    && !this.protocol.bukkit().equals(entity)
                    && hackType.getCheck().getBooleanOption("detection." + (isPlayer ? "players" : "entities"), true);
        }
        return false;
    }

    @Override
    protected void handleInternal(boolean cancelled, Object object) {
        if (object instanceof PlayerMoveEvent) {
            moveDirection.run();
            moveLength.run();
        } else if (object instanceof PlayerAttackEvent) {
            PlayerAttackEvent event = (PlayerAttackEvent) object;
            LivingEntity target = event.target;

            if (canRunEntity(target)) {
                irregular.run();
                moveDirection.run(target);
                hitTime.run();
                moveLength.runHit();
            }
        } else if (object instanceof EntityAttackPlayerEvent) {
            moveLength.runHitBy();
        }
    }

    @Override
    protected boolean canRun() {
        return CombatUtils.canCheck(this.protocol);
    }
}
