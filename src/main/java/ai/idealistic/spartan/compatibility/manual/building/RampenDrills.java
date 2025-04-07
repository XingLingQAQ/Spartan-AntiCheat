package ai.idealistic.spartan.compatibility.manual.building;

import ai.idealistic.spartan.abstraction.check.CheckEnums;
import ai.idealistic.spartan.compatibility.Compatibility;
import ai.idealistic.spartan.functionality.server.Config;
import ai.idealistic.spartan.functionality.server.PluginBase;
import me.rampen88.drills.events.DrillBreakEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class RampenDrills implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    private void Event(DrillBreakEvent e) {
        Compatibility.CompatibilityType compatibilityType = Compatibility.CompatibilityType.RAMPEN_DRILLS;

        if (compatibilityType.isFunctional()) {
            Config.compatibility.evadeFalsePositives(
                    PluginBase.getProtocol(e.getPlayer()),
                    compatibilityType,
                    new CheckEnums.HackType[]{
                            CheckEnums.HackType.FAST_BREAK,
                            CheckEnums.HackType.NO_SWING,
                            CheckEnums.HackType.IMPOSSIBLE_ACTIONS,
                    },
                    5
            );
        }
    }
}
