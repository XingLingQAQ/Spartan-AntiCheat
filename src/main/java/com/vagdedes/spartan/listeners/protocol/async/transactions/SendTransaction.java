package com.vagdedes.spartan.listeners.protocol.async.transactions;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.vagdedes.spartan.Register;
import com.vagdedes.spartan.abstraction.protocol.SpartanProtocol;
import com.vagdedes.spartan.functionality.server.SpartanBukkit;
import org.bukkit.entity.Player;

import java.util.Objects;

public class SendTransaction extends PacketAdapter {

    public SendTransaction() {
        super(
                Register.plugin,
                ListenerPriority.HIGHEST,
                PacketType.Play.Client.TRANSACTION
        );
    }

    @Override
    public void onPacketReceiving(PacketEvent event) {
        PacketContainer packet = event.getPacket();

        if (packet.getShorts().getValues().isEmpty()) {
            return;
        }
        Player player = event.getPlayer();
        SpartanProtocol protocol = SpartanBukkit.getProtocol(player);

        if (protocol.timePassed() >= BackgroundTransaction.refreshRate
                && Objects.equals(packet.getShorts().read(0), protocol.getTransactionID())) {
            protocol.setLastTransaction();
            PacketContainer transaction = new PacketContainer(PacketType.Play.Server.TRANSACTION);
            transaction.getShorts().write(0, protocol.increaseTransactionID());
            SpartanBukkit.transferTask(
                    player,
                    () -> ProtocolLibrary.getProtocolManager().sendServerPacket(player, transaction)
            );
        }
    }

}
