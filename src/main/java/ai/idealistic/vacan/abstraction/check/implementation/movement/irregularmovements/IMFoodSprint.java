package ai.idealistic.vacan.abstraction.check.implementation.movement.irregularmovements;

import ai.idealistic.vacan.abstraction.check.Check;
import ai.idealistic.vacan.abstraction.check.CheckDetection;
import ai.idealistic.vacan.abstraction.check.CheckRunner;
import ai.idealistic.vacan.abstraction.data.Buffer;
import org.bukkit.GameMode;

public class IMFoodSprint extends CheckDetection {

    private final Buffer.IndividualBuffer buffer;

    IMFoodSprint(CheckRunner executor) {
        super(executor, Check.DataType.JAVA, null, "food_sprint", true);
        this.buffer = new Buffer.IndividualBuffer();
    }

    void run() {
        this.call(() -> {
            if (this.protocol.isSprinting()
                    && !this.protocol.wasGliding()) {
                GameMode gameMode = this.protocol.getGameMode();
                int food = this.protocol.bukkit().getFoodLevel(),
                        threshold = 6;

                if (food <= threshold
                        && (gameMode == GameMode.SURVIVAL
                        || gameMode == GameMode.ADVENTURE)) {
                    double ratio = buffer.count(1, 10) / 10.0;

                    if (ratio >= 0.4) {
                        cancel(
                                "food-sprint"
                                        + ", game-mode: " + gameMode.toString().toLowerCase().replace("_", "-")
                                        + ", food: " + food,
                                this.protocol.getFromLocation(),
                                0,
                                true
                        );
                    }
                }
            }
        });
    }

}
