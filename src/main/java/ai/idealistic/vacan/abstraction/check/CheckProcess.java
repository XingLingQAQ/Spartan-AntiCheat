package ai.idealistic.vacan.abstraction.check;

import ai.idealistic.vacan.abstraction.Enums;
import ai.idealistic.vacan.abstraction.protocol.PlayerProtocol;

public abstract class CheckProcess {

    public final Enums.HackType hackType;
    public final PlayerProtocol protocol;

    protected CheckProcess(Enums.HackType hackType, PlayerProtocol protocol) {
        this.hackType = hackType;
        this.protocol = protocol;
    }

}
