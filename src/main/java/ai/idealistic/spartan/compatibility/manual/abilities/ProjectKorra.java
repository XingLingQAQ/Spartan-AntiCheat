package ai.idealistic.spartan.compatibility.manual.abilities;

import ai.idealistic.spartan.abstraction.check.CheckEnums;
import ai.idealistic.spartan.abstraction.protocol.PlayerProtocol;
import ai.idealistic.spartan.compatibility.Compatibility;
import ai.idealistic.spartan.functionality.server.Config;
import ai.idealistic.spartan.functionality.server.PluginBase;
import com.projectkorra.projectkorra.event.AbilityDamageEntityEvent;
import com.projectkorra.projectkorra.event.AbilityProgressEvent;
import com.projectkorra.projectkorra.event.AbilityStartEvent;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class ProjectKorra implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    private void AbilityStart(AbilityStartEvent e) {
        if (Compatibility.CompatibilityType.PROJECT_KORRA.isFunctional()) {
            evadeCombatFPs(PluginBase.getProtocol(e.getAbility().getPlayer()), 60);
        }
    }

    @EventHandler
    private void AbilityProgress(AbilityProgressEvent e) {
        if (Compatibility.CompatibilityType.PROJECT_KORRA.isFunctional()) {
            evadeCombatFPs(PluginBase.getProtocol(e.getAbility().getPlayer()), 40);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    private void AbilityDamageEvent(AbilityDamageEntityEvent e) {
        if (Compatibility.CompatibilityType.PROJECT_KORRA.isFunctional()) {
            Entity entity = e.getEntity();

            if (entity instanceof Player) {
                evadeCombatFPs(
                        PluginBase.getProtocol((Player) entity),
                        60
                );
            }
        }
    }

    private static void evadeCombatFPs(PlayerProtocol protocol, int ticks) {
        Compatibility.CompatibilityType compatibilityType = Compatibility.CompatibilityType.PROJECT_KORRA;
        Config.compatibility.evadeFalsePositives(
                protocol,
                compatibilityType,
                new CheckEnums.HackCategoryType[]{
                        CheckEnums.HackCategoryType.MOVEMENT,
                        CheckEnums.HackCategoryType.COMBAT
                },
                ticks
        );
        Config.compatibility.evadeFalsePositives(
                protocol,
                compatibilityType,
                CheckEnums.HackType.NO_SWING,
                ticks
        );
    }

}
