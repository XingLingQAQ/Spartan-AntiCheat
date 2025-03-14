package ai.idealistic.vacan.compatibility.manual.building;

import ai.idealistic.vacan.abstraction.check.CheckEnums;
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
                    new CheckEnums.HackType[]{
                            CheckEnums.HackType.FAST_BREAK,
                            CheckEnums.HackType.BLOCK_REACH,
                            CheckEnums.HackType.GHOST_HAND,
                    },
                    40
            );
        }
    }
}
