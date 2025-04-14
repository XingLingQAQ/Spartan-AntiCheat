package ai.idealistic.spartan.abstraction.event;

import ai.idealistic.spartan.abstraction.protocol.PlayerProtocol;

public class PlayerTransactionEvent {

    public final PlayerProtocol protocol;
    public final long time, delay;

    public PlayerTransactionEvent(PlayerProtocol protocol) {
        this.time = System.currentTimeMillis();
        this.protocol = protocol;
        this.delay = protocol.transactionPing;
    }
}
