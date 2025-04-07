package ai.idealistic.spartan.abstraction.check.implementation.movement.irregularmovements;

import ai.idealistic.spartan.abstraction.check.CheckDetection;
import ai.idealistic.spartan.abstraction.check.CheckRunner;

public class IMHeadPosition extends CheckDetection {

    IMHeadPosition(CheckRunner executor) {
        super(executor, null, null, "head_position", true);
    }

    void run() {
        this.call(() -> {
            if (this.protocol.isLowEyeHeight()
                    || this.protocol.getVehicle() != null
                    || this.protocol.wasGliding()) {
                return;
            }
            double pitch = this.protocol.getLocation().getPitch();

            if (Math.abs(pitch) >= 90.1f) {
                cancel("head-position, pitch: " + pitch);
            }
        });
    }

}
