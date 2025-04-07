package ai.idealistic.spartan.listeners.protocol;

import ai.idealistic.spartan.Register;
import ai.idealistic.spartan.abstraction.event.CPlayerVelocityEvent;
import ai.idealistic.spartan.abstraction.protocol.PlayerProtocol;
import ai.idealistic.spartan.functionality.concurrent.CheckThread;
import ai.idealistic.spartan.functionality.server.PluginBase;
import ai.idealistic.spartan.listeners.bukkit.VelocityEvent;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class VelocityListener extends PacketAdapter {


    public VelocityListener() {
        super(
                Register.plugin,
                ListenerPriority.MONITOR,
                PacketType.Play.Server.ENTITY_VELOCITY
        );
    }

    @Override
    public void onPacketSending(PacketEvent event) {
        Player player = event.getPlayer();
        PlayerProtocol protocol = PluginBase.getProtocol(player);

        if (protocol.isBedrockPlayer()) {
            return;
        }
        PacketContainer packet = event.getPacket();

        if (!packet.getIntegers().getValues().isEmpty()) {
            int id = packet.getIntegers().getValues().get(0);
            if (protocol.getEntityId() == id) {
                CheckThread.run(() -> {
                    double x = packet.getIntegers().read(1).doubleValue() / 8000.0D,
                                    y = packet.getIntegers().read(2).doubleValue() / 8000.0D,
                                    z = packet.getIntegers().read(3).doubleValue() / 8000.0D;
                    CPlayerVelocityEvent velocityEvent = new CPlayerVelocityEvent(player, new Vector(x, y, z));
                    velocityEvent.setCancelled(event.isCancelled());
                    VelocityEvent.event(velocityEvent, true);
                });
            }
        }
    }

}
