package ai.idealistic.vacan.abstraction.check;

import ai.idealistic.vacan.abstraction.protocol.PlayerProtocol;

public abstract class CheckProcess {

    public final CheckEnums.HackType hackType;
    public final PlayerProtocol protocol;

    protected CheckProcess(CheckEnums.HackType hackType, PlayerProtocol protocol) {
        this.hackType = hackType;
        this.protocol = protocol;
    }

}
