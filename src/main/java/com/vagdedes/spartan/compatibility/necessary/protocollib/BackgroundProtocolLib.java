package com.vagdedes.spartan.compatibility.necessary.protocollib;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.vagdedes.spartan.listeners.protocol.*;
import com.vagdedes.spartan.listeners.protocol.async.transactions.SendTransaction;
import com.vagdedes.spartan.listeners.protocol.move.Move;
import com.vagdedes.spartan.listeners.protocol.move.Move_Deprecated;

public class BackgroundProtocolLib {

    static void run() {
        ProtocolManager p = ProtocolLibrary.getProtocolManager();
        p.addPacketListener(new Join());
        p.addPacketListener(new EntityAction());
        p.addPacketListener(new Velocity());
        p.addPacketListener(new Attack());
        p.addPacketListener(new Move());
        p.addPacketListener(new Move_Deprecated());
        p.addPacketListener(new ServerPosition());
        p.addPacketListener(new SendTransaction());
        p.addPacketListener(new WorldListener());
        //p.addPacketListener(new Debug());
    }

}