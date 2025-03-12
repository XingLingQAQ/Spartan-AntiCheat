package ai.idealistic.vacan.listeners.protocol;

import ai.idealistic.vacan.Register;
import ai.idealistic.vacan.abstraction.protocol.PlayerProtocol;
import ai.idealistic.vacan.functionality.concurrent.CheckThread;
import ai.idealistic.vacan.functionality.server.PluginBase;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.vehicle.VehicleEnterEvent;

public class UseEntityListener extends PacketAdapter {

    public UseEntityListener() {
        super(Register.plugin, ListenerPriority.NORMAL, PacketType.Play.Client.USE_ENTITY);
    }

    @Override
    public void onPacketReceiving(PacketEvent event) {
        Player player = event.getPlayer();
        PlayerProtocol protocol = PluginBase.getProtocol(player);
        PacketContainer packet = event.getPacket();
        int entityId = packet.getIntegers().read(0);

        CheckThread.run(() -> {
            if ((!packet.getEntityUseActions().getValues().isEmpty())
                    ? !packet.getEntityUseActions().read(0).equals(EnumWrappers.EntityUseAction.ATTACK)
                    : !packet.getEnumEntityUseActions().read(0).getAction().equals(
                    EnumWrappers.EntityUseAction.ATTACK)) {
                Entity t = null;
                for (Entity entity : protocol.getNearbyEntities(5)) {
                    if (entity.getEntityId() == entityId) {
                        t = entity;
                        break;
                    }
                }
                if (t instanceof Vehicle) {
                    protocol.profile().executeRunners(
                            false,
                            new VehicleEnterEvent(null, player)
                    );
                }
            }
        });
    }

}
