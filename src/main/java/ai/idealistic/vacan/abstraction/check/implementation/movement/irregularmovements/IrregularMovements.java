package ai.idealistic.vacan.abstraction.check.implementation.movement.irregularmovements;

import ai.idealistic.vacan.abstraction.Enums.HackType;
import ai.idealistic.vacan.abstraction.check.CheckRunner;
import ai.idealistic.vacan.abstraction.event.CPlayerRiptideEvent;
import ai.idealistic.vacan.abstraction.event.PlayerTickEvent;
import ai.idealistic.vacan.abstraction.event.SuperPositionPacketEvent;
import ai.idealistic.vacan.abstraction.protocol.PlayerProtocol;
import ai.idealistic.vacan.functionality.server.PluginBase;
import ai.idealistic.vacan.functionality.tracking.MovementProcessing;
import ai.idealistic.vacan.listeners.protocol.TeleportListener;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketEvent;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;

public class IrregularMovements extends CheckRunner {

    private final IMSimulationGravity gravity;
    private final IMSimulationSpeed speed;
    private final IMFoodSprint foodSprint;
    private final IMBaritone baritone;
    private final IMHeadPosition headPosition;
    private final IMGroundSpoof groundSpoof;
    private final IMSimulationElytra elytra;
    private final IMSimulationVehicle vehicle;

    public IrregularMovements(HackType hackType, PlayerProtocol protocol) {
        super(hackType, protocol);
        gravity = new IMSimulationGravity(this);
        foodSprint = new IMFoodSprint(this);
        baritone = new IMBaritone(this);
        speed = new IMSimulationSpeed(this);
        headPosition = new IMHeadPosition(this);
        groundSpoof = new IMGroundSpoof(this);
        elytra = new IMSimulationElytra(this);
        vehicle = new IMSimulationVehicle(this);
    }

    @Override
    protected void handleInternal(boolean cancelled, Object object) {
        if (object instanceof PlayerTeleportEvent) {
            this.teleport();
        } else if (object instanceof CPlayerRiptideEvent) {
            gravity.trident();
            speed.trident();
        } else if (object instanceof SuperPositionPacketEvent) {
            SuperPositionPacketEvent e = (SuperPositionPacketEvent) object;
            speed.ground(e.packetEvent.getPacket().getBooleans().read(0));
            groundSpoof.run(
                    new PlayerMoveEvent(
                            e.protocol.bukkit(),
                            e.protocol.getLocation(),
                            e.protocol.getFromLocation()
                    )
            );
        } else if (object instanceof PlayerMoveEvent) {
            foodSprint.run();
            baritone.run();
            gravity.run();
            speed.run();
            headPosition.run();
            elytra.run();
            vehicle.run();
            groundSpoof.run((PlayerMoveEvent) object);
        } else if (object instanceof BlockBreakEvent) {
            if (((BlockBreakEvent) object).isCancelled()) {
                groundSpoof.onBreak();
            }
        } else if (object instanceof VehicleExitEvent) {
            groundSpoof.onVehicleExit();
        } else if (object instanceof PlayerTickEvent) {
            PlayerTickEvent e = (PlayerTickEvent) object;
            gravity.groundSp(e.onGround);
        } else if (object instanceof VehicleEnterEvent) {
            speed.vhAct();
            gravity.vhAct();
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
        gravity.teleport();
        speed.teleport();
        baritone.teleport();
        elytra.teleport();
        vehicle.teleport();
    }

    @Override
    protected boolean canRun() {
        return MovementProcessing.canCheck(
                this.protocol,
                true,
                true,
                false,
                true,
                false
        );
    }

}