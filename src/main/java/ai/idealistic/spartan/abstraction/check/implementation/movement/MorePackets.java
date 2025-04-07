package ai.idealistic.spartan.abstraction.check.implementation.movement;

import ai.idealistic.spartan.abstraction.check.Check;
import ai.idealistic.spartan.abstraction.check.CheckDetection;
import ai.idealistic.spartan.abstraction.check.CheckEnums.HackType;
import ai.idealistic.spartan.abstraction.check.CheckRunner;
import ai.idealistic.spartan.abstraction.check.definition.ImplementedDetection;
import ai.idealistic.spartan.abstraction.data.TimerBalancer;
import ai.idealistic.spartan.abstraction.event.PlayerTickEvent;
import ai.idealistic.spartan.abstraction.protocol.PlayerProtocol;
import ai.idealistic.spartan.functionality.server.PluginBase;
import ai.idealistic.spartan.functionality.server.TPS;
import ai.idealistic.spartan.functionality.tracking.MovementProcessing;
import ai.idealistic.spartan.listeners.protocol.TeleportListener;
import ai.idealistic.spartan.utils.math.RayUtils;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class MorePackets extends CheckRunner {

    private static final double minTimer = 1.085;

    private int packetVL;
    private final ImplementedDetection
            positive_net,
            positive_latency,
            positive_balance,
            negative;
    private long teleported;

    public MorePackets(HackType hackType, PlayerProtocol protocol) {
        super(hackType, protocol);
        this.packetVL = 0;
        this.positive_net = new ImplementedDetection(
                this,
                Check.DataType.JAVA,
                Check.DetectionType.PACKETS,
                "positive_net",
                true
        );
        this.positive_latency = new ImplementedDetection(
                this,
                Check.DataType.JAVA,
                Check.DetectionType.PACKETS,
                "positive_latency",
                true
        );
        this.positive_balance = new ImplementedDetection(
                this,
                Check.DataType.JAVA,
                Check.DetectionType.PACKETS,
                "positive_balance",
                true
        );
        this.negative = new ImplementedDetection(
                this,
                Check.DataType.JAVA,
                Check.DetectionType.PACKETS,
                "negative",
                true
        );
    }

    @Override
    protected void handleInternal(boolean cancelled, Object object) {
        if (object instanceof PlayerTickEvent) {
            if (this.positive_balance.canCall()
                    || this.positive_latency.canCall()
                    || this.positive_net.canCall()
                    || this.negative.canCall()) {
                PlayerTickEvent event = (PlayerTickEvent) object;
                long delay = event.getDelay();
                TimerBalancer timerBalancer = event.protocol.timerBalancer;
                double multiply = RayUtils.scaleVal((double) TPS.tickTime / delay, 2);

                if (timerBalancer.isNegativeTimer() && delay > 50) {
                    this.negative.call(() -> {
                        if (multiply < 1.0) {
                            punish(
                                    this.negative,
                                    "negative, multiply: " + multiply,
                                    1.0 / multiply
                            );
                        }
                    });
                } else if (delay < 50 && timerBalancer.getResult() > 100) {
                    this.positive_balance.call(() -> {
                        if (multiply >= minTimer) {
                            punish(
                                    this.positive_balance,
                                    "positive(balance), multiply: " + multiply,
                                    multiply
                            );
                        }
                    });
                } else if (delay < 50 && timerBalancer.getForced() > 8) {
                    this.positive_net.call(() -> {
                        if (multiply >= minTimer) {
                            punish(
                                    this.positive_net,
                                    "positive(NET), multiply: " + multiply,
                                    multiply
                            );
                        }
                    });
                } else if (delay < 50 && timerBalancer.getLatency() > 40) {
                    this.positive_latency.call(() -> {
                        if (multiply >= minTimer) {
                            punish(
                                    this.positive_latency,
                                    "positive(latency), multiply: " + multiply,
                                    multiply
                            );
                        }
                    });
                }
            }
        } else if (object instanceof PlayerTeleportEvent) {
            this.teleport();
        } else if (PluginBase.packetsEnabled()
                && object instanceof PacketEvent) {
            PacketType eventType = ((PacketEvent) object).getPacketType();

            for (PacketType type : TeleportListener.packetTypes) {
                if (type.equals(eventType)) {
                    this.teleport();
                    break;
                }
            }
        }
    }

    private void teleport() {
        this.teleported = System.currentTimeMillis() + (TPS.tickTime * 4L);
        this.packetVL = 0;
    }

    private void punish(
            CheckDetection executor,
            String info,
            double amplitude
    ) {
        packetVL += 70;

        if (packetVL > 100) {
            executor.cancel(
                    amplitude,
                    info,
                    this.protocol.getFromLocation(),
                    0,
                    true
            );
            packetVL -= 10;
        } else if (packetVL > 0) {
            packetVL -= 1;
        }
    }

    @Override
    protected boolean canRun() {
        return !this.protocol.isAFK()
                && this.teleported < System.currentTimeMillis()
                && MovementProcessing.canCheck(
                this.protocol,
                false,
                true,
                true,
                true,
                true
        );
    }

}