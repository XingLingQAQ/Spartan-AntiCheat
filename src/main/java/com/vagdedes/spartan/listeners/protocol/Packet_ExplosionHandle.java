package com.vagdedes.spartan.listeners.protocol;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.vagdedes.spartan.Register;
import com.vagdedes.spartan.abstraction.protocol.SpartanProtocol;
import com.vagdedes.spartan.abstraction.world.SpartanLocation;
import com.vagdedes.spartan.functionality.server.SpartanBukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.List;

public class Packet_ExplosionHandle extends PacketAdapter {

    public Packet_ExplosionHandle() {
        super(Register.plugin, ListenerPriority.NORMAL,
                PacketType.Play.Server.EXPLOSION);
    }

    @Override
    public void onPacketSending(PacketEvent event) {
        PacketContainer packet = event.getPacket();
        List<Double> d = packet.getDoubles().getValues();

        if (d.size() >= 3) {
            Player player = event.getPlayer();
            SpartanProtocol protocol = SpartanBukkit.getProtocol(player);
            Location l = new Location(player.getWorld(), d.get(0), d.get(1), d.get(2));

            if (SpartanLocation.distanceSquared(l, protocol.getLocation()) < 10) {
                protocol.getComponentY().explosionTick = true;
            }
        }
    }

}