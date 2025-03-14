package ai.idealistic.vacan.compatibility.manual.abilities;

import ai.idealistic.vacan.abstraction.check.CheckEnums;
import ai.idealistic.vacan.compatibility.Compatibility;
import ai.idealistic.vacan.functionality.server.Config;
import ai.idealistic.vacan.functionality.server.PluginBase;
import de.flo56958.minetinker.events.MTPlayerInteractEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class MineTinker implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    private void Enter(MTPlayerInteractEvent e) {
        Compatibility.CompatibilityType compatibilityType = Compatibility.CompatibilityType.MINE_TINKER;

        if (compatibilityType.isFunctional()) {
            Config.compatibility.evadeFalsePositives(
                    PluginBase.getProtocol(e.getPlayer()),
                    compatibilityType,
                    new CheckEnums.HackType[]{
                            CheckEnums.HackType.KILL_AURA,
                            CheckEnums.HackType.FAST_CLICKS,
                            CheckEnums.HackType.HIT_REACH,
                            CheckEnums.HackType.FAST_PLACE
                    },
                    40
            );
        }
    }
}
