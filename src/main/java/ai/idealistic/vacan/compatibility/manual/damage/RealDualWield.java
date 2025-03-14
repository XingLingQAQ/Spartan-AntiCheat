package ai.idealistic.vacan.compatibility.manual.damage;

import ai.idealistic.vacan.abstraction.check.CheckEnums;
import ai.idealistic.vacan.compatibility.Compatibility;
import ai.idealistic.vacan.functionality.server.Config;
import ai.idealistic.vacan.functionality.server.PluginBase;
import com.evill4mer.RealDualWield.Api.PlayerOffhandAnimationEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class RealDualWield implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    private void Event(PlayerOffhandAnimationEvent e) {
        Compatibility.CompatibilityType compatibilityType = Compatibility.CompatibilityType.REAL_DUAL_WIELD;

        if (compatibilityType.isFunctional()) {
            Config.compatibility.evadeFalsePositives(
                    PluginBase.getProtocol(e.getPlayer()),
                    compatibilityType,
                    new CheckEnums.HackType[]{
                            CheckEnums.HackType.KILL_AURA,
                            CheckEnums.HackType.HIT_REACH,
                            CheckEnums.HackType.CRITICALS,
                    },
                    5
            );
        }
    }
}
