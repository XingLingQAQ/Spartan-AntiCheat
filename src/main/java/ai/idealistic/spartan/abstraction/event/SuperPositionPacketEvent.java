package ai.idealistic.spartan.abstraction.event;

import ai.idealistic.spartan.abstraction.protocol.PlayerProtocol;
import com.comphenix.protocol.events.PacketEvent;

public class SuperPositionPacketEvent {

    public final PlayerProtocol protocol;
    public final PacketEvent packetEvent;

    public SuperPositionPacketEvent(PlayerProtocol protocol, PacketEvent packetEvent) {
        this.protocol = protocol;
        this.packetEvent = packetEvent;
    }

}
