package com.vagdedes.spartan.abstraction.profiling;

import com.vagdedes.spartan.abstraction.replicates.SpartanPlayer;
import me.vagdedes.spartan.system.Enums;

public class PlayerViolation {

    public final Enums.HackType hackType;
    public final long time;
    public final String information, detection;
    public final int level, identity;
    public final boolean isOption;

    public PlayerViolation(long time,
                           Enums.HackType hackType,
                           String information,
                           int level,
                           InformationAnalysis analysis) {
        this.hackType = hackType;
        this.time = time;
        this.information = information;
        this.level = level;

        this.detection = analysis.detection;
        this.isOption = analysis.isOption;
        this.identity = analysis.identity;
    }

    public PlayerViolation(long time,
                           Enums.HackType hackType,
                           String information,
                           int level) {
        this(
                time,
                hackType,
                information,
                level,
                new InformationAnalysis(hackType, information)
        );
    }

    boolean isDetectionEnabled() {
        return !isOption || hackType.getCheck().getBooleanOption(detection, null);
    }

    public int getIgnoredViolations(SpartanPlayer player) {
        return hackType.getCheck().getIgnoredViolations(player.dataType, this.identity);
    }
}