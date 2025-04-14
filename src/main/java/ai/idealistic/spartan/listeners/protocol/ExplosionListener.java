package ai.idealistic.spartan.listeners.protocol;

import ai.idealistic.spartan.Register;
import ai.idealistic.spartan.abstraction.protocol.PlayerProtocol;
import ai.idealistic.spartan.abstraction.world.ServerLocation;
import ai.idealistic.spartan.functionality.server.PluginBase;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.List;

public class ExplosionListener extends PacketAdapter {

    public ExplosionListener() {
        super(Register.plugin, ListenerPriority.NORMAL,
                PacketType.Play.Server.EXPLOSION);
    }

    @Override
    public void onPacketSending(PacketEvent event) {
        PacketContainer packet = event.getPacket();
        List<Double> d = packet.getDoubles().getValues();

        if (d.size() >= 3) {
            Player player = event.getPlayer();
            PlayerProtocol protocol = PluginBase.getProtocol(player);
            Location l = new Location(player.getWorld(), d.get(0), d.get(1), d.get(2));

            if (ServerLocation.distanceSquared(l, protocol.getLocation()) < 10) {
                protocol.getComponentY().explosionTick = true;
                protocol.getComponentXZ().explosionTick = true;
            }
        }
    }

}