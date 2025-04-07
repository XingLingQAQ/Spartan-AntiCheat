package ai.idealistic.spartan.abstraction.check.implementation.combat;

import ai.idealistic.spartan.abstraction.check.CheckDetection;
import ai.idealistic.spartan.abstraction.check.CheckEnums.HackType;
import ai.idealistic.spartan.abstraction.check.CheckRunner;
import ai.idealistic.spartan.abstraction.check.definition.ImplementedDetection;
import ai.idealistic.spartan.abstraction.event.PlayerAttackEvent;
import ai.idealistic.spartan.abstraction.protocol.PlayerProtocol;
import ai.idealistic.spartan.abstraction.world.ServerLocation;
import ai.idealistic.spartan.utils.math.RayUtils;
import ai.idealistic.spartan.utils.minecraft.entity.CombatUtils;
import ai.idealistic.spartan.utils.minecraft.world.BlockUtils;
import org.bukkit.potion.PotionEffectType;

public class Criticals extends CheckRunner {

    private final CheckDetection detection;

    public Criticals(HackType hackType, PlayerProtocol protocol) {
        super(hackType, protocol);
        this.detection = new ImplementedDetection(this, null, null, null, true,
                CheckDetection.DEFAULT_AVERAGE_TIME * 4L,
                CheckDetection.TIME_TO_NOTIFY,
                CheckDetection.TIME_TO_PREVENT,
                CheckDetection.TIME_TO_PUNISH);
    }

    @Override
    protected void handleInternal(boolean cancelled, Object object) {
        if (object instanceof PlayerAttackEvent) {
            this.detection.call(() -> {
                double verticalMaxAccuracy = this.protocol.getLocation().getY() - this.protocol.getFromLocation().getY();

                if (RayUtils.onBlock(
                        this.protocol,
                        this.protocol.getLocationOrVehicle(),
                        BlockUtils.web
                )
                ) {
                    return;
                }

                if ((verticalMaxAccuracy < 0.079 || Math.abs(verticalMaxAccuracy) == 0.0625)
                        && verticalMaxAccuracy > 0
                        && !this.protocol.isOnGround()
                        && this.protocol.isOnGroundFrom()
                        && !new ServerLocation(this.protocol.getLocation()).getBlock().getType().isBlock()
                        && !RayUtils.onSolidBlock(this.protocol, this.protocol.getLocation().clone().add(0, 2, 0))) {
                    this.detection.cancel("packet");
                }
            });
        }
    }

    @Override
    protected boolean canRun() {
        return CombatUtils.canCheck(this.protocol)
                && !this.protocol.getEnvironment().isLiquid()
                && !this.protocol.hasPotionEffect(PotionEffectType.BLINDNESS, 0);
    }
}
