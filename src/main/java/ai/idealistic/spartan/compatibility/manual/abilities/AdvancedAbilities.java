package ai.idealistic.spartan.compatibility.manual.abilities;

import ai.idealistic.spartan.abstraction.check.CheckEnums;
import ai.idealistic.spartan.compatibility.Compatibility;
import ai.idealistic.spartan.functionality.server.Config;
import ai.idealistic.spartan.functionality.server.PluginBase;
import be.anybody.api.advancedabilities.ability.event.AbilityCallEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class AdvancedAbilities implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    private void AbilityEvent(AbilityCallEvent e) {
        if (Compatibility.CompatibilityType.ADVANCED_ABILITIES.isFunctional()) {
            Config.compatibility.evadeFalsePositives(
                    PluginBase.getProtocol(e.getPlayer()),
                    Compatibility.CompatibilityType.ADVANCED_ABILITIES,
                    new CheckEnums.HackCategoryType[]{
                            CheckEnums.HackCategoryType.MOVEMENT,
                            CheckEnums.HackCategoryType.COMBAT
                    },
                    60
            );
        }
    }
}
