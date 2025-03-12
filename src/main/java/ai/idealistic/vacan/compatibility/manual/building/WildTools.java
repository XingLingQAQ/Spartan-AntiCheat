package ai.idealistic.vacan.compatibility.manual.building;

import ai.idealistic.vacan.abstraction.Enums;
import ai.idealistic.vacan.compatibility.Compatibility;
import ai.idealistic.vacan.functionality.server.Config;
import ai.idealistic.vacan.functionality.server.PluginBase;
import com.bgsoftware.wildtools.api.events.ToolUseEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class WildTools implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    private void Event(ToolUseEvent e) {
        Compatibility.CompatibilityType compatibilityType = Compatibility.CompatibilityType.WILD_TOOLS;

        if (compatibilityType.isFunctional()) {
            Config.compatibility.evadeFalsePositives(
                    PluginBase.getProtocol(e.getPlayer()),
                    compatibilityType,
                    new Enums.HackType[]{
                            Enums.HackType.FastBreak,
                            Enums.HackType.BlockReach,
                            Enums.HackType.GhostHand,
                    },
                    40
            );
        }
    }
}
