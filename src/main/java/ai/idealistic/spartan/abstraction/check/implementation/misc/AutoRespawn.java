package ai.idealistic.spartan.abstraction.check.implementation.misc;

import ai.idealistic.spartan.abstraction.check.CheckDetection;
import ai.idealistic.spartan.abstraction.check.CheckEnums.HackType;
import ai.idealistic.spartan.abstraction.check.CheckRunner;
import ai.idealistic.spartan.abstraction.check.definition.ImplementedDetection;
import ai.idealistic.spartan.abstraction.protocol.PlayerProtocol;
import ai.idealistic.spartan.functionality.server.MultiVersion;
import ai.idealistic.spartan.functionality.server.PluginBase;
import ai.idealistic.spartan.functionality.server.TPS;
import ai.idealistic.spartan.listeners.protocol.DeathListener;
import ai.idealistic.spartan.utils.math.AlgebraUtils;
import ai.idealistic.spartan.utils.minecraft.server.PluginUtils;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketEvent;
import org.bukkit.GameRule;
import org.bukkit.event.entity.PlayerDeathEvent;

public class AutoRespawn extends CheckRunner {

    private final CheckDetection detection;
    private long memory;

    public AutoRespawn(HackType hackType, PlayerProtocol protocol) {
        super(hackType, protocol);
        this.detection = new ImplementedDetection(this, null, null, null, true);
    }

    @Override
    protected void handleInternal(boolean cancelled, Object object) {
        if (object == null) {
            this.detection.call(() -> {
                long time = System.currentTimeMillis() - this.memory,
                        limit = 750L;

                if (time <= limit && !this.protocol.bukkit().isDead()) {
                    memory = 0L;
                    this.detection.cancel(
                            "default, ms: " + time,
                            this.protocol.getFromLocation(),
                            AlgebraUtils.integerCeil(TPS.maximum)
                    );
                }
            });
        } else if (object instanceof PlayerDeathEvent) {
            this.death();
        } else if (PluginBase.packetsEnabled()
                && object instanceof PacketEvent) {
            PacketType eventType = ((PacketEvent) object).getPacketType();

            for (PacketType type : DeathListener.packetTypes) {
                if (type.equals(eventType)) {
                    this.death();
                    break;
                }
            }
        }
    }

    private void death() {
        this.memory = System.currentTimeMillis();
    }

    @Override
    protected boolean canRun() {
        return !PluginUtils.contains("respawn")
                && (!MultiVersion.isOrGreater(MultiVersion.MCVersion.V1_15)
                || !(this.protocol.getWorld().getGameRuleValue(GameRule.DO_IMMEDIATE_RESPAWN) == true));
    }
}
