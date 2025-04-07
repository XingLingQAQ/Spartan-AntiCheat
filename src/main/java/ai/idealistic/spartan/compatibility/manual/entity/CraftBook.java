package ai.idealistic.spartan.compatibility.manual.entity;

import ai.idealistic.spartan.compatibility.Compatibility;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class CraftBook {

    private static double limit = -1.0;

    public static void resetBoatLimit() {
        limit = -1.0;
    }

    public static double getBoatLimit() {
        if (limit != -1.0) {
            return limit;
        }
        if (Compatibility.CompatibilityType.CRAFT_BOOK.isFunctional()) {
            File file = new File("plugins/CraftBook/mechanisms.yml");

            if (file.exists()) {
                YamlConfiguration filea = YamlConfiguration.loadConfiguration(file);
                double value = filea.getDouble("mechanics.BoatSpeedModifiers.max-speed");

                if (value < 0.0) {
                    value = 0.1;
                } else {
                    value += 0.1;
                }
                limit = value;
                return value;
            }
        }
        return limit = 0.0;
    }
}
