package ai.idealistic.spartan.abstraction.check.implementation.combat.killaura;

import ai.idealistic.spartan.abstraction.check.CheckEnums;
import ai.idealistic.spartan.abstraction.check.CheckRunner;
import ai.idealistic.spartan.abstraction.check.implementation.combat.killaura.movedirection.KAMoveDirection;
import ai.idealistic.spartan.abstraction.event.EntityAttackPlayerEvent;
import ai.idealistic.spartan.abstraction.event.PlayerAttackEvent;
import ai.idealistic.spartan.abstraction.protocol.PlayerProtocol;
import ai.idealistic.spartan.compatibility.necessary.protocollib.ProtocolLib;
import ai.idealistic.spartan.utils.minecraft.entity.CombatUtils;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;

public class KillAura extends CheckRunner {

    private final KAIrregular irregular;
    private final KAHitTime hitTime;
    private final KAMoveDirection moveDirection;
    private final KAMoveLength moveLength;
    private final KABackTrack backTrack;

    public KillAura(CheckEnums.HackType hackType, PlayerProtocol protocol) {
        super(hackType, protocol);
        irregular = new KAIrregular(this);
        hitTime = new KAHitTime(this);
        moveDirection = new KAMoveDirection(this);
        moveLength = new KAMoveLength(this);
        backTrack = new KABackTrack(this);
    }

    boolean canRunEntity(LivingEntity entity) {
        return ProtocolLib.getVehicle(entity) == null
                && CombatUtils.canCheck(this.protocol, entity)
                && !this.protocol.bukkit().equals(entity)
                && hackType.getCheck().getBooleanOption(
                "detection." + (entity instanceof Player ? "players" : "entities"),
                true
        );
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
                backTrack.run(event);
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
