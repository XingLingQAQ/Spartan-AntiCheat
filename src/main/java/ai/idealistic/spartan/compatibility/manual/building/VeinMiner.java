package ai.idealistic.spartan.compatibility.manual.building;

import ai.idealistic.spartan.Register;
import ai.idealistic.spartan.abstraction.check.CheckEnums;
import ai.idealistic.spartan.compatibility.Compatibility;
import ai.idealistic.spartan.functionality.server.Config;
import ai.idealistic.spartan.functionality.server.PluginBase;
import ai.idealistic.spartan.functionality.server.TPS;
import ai.idealistic.spartan.utils.math.AlgebraUtils;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import wtf.choco.veinminer.api.event.player.PlayerClientActivateVeinMinerEvent;
import wtf.choco.veinminer.api.event.player.PlayerVeinMineEvent;

public class VeinMiner implements Listener {

    public static void reload() {
        Register.enable(new VeinMiner());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    private void Event(PlayerVeinMineEvent e) {
        if (Compatibility.CompatibilityType.VEIN_MINER.isFunctional()) {
            Config.compatibility.evadeFalsePositives(
                    PluginBase.getProtocol(e.getPlayer()),
                    Compatibility.CompatibilityType.VEIN_MINER,
                    new CheckEnums.HackType[]{
                            CheckEnums.HackType.NO_SWING,
                            CheckEnums.HackType.FAST_BREAK,
                            CheckEnums.HackType.IMPOSSIBLE_ACTIONS,
                            CheckEnums.HackType.BLOCK_REACH,
                            CheckEnums.HackType.FAST_PLACE
                    },
                    AlgebraUtils.integerRound(TPS.maximum)
            );
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    private void Event(PlayerClientActivateVeinMinerEvent e) {
        if (Compatibility.CompatibilityType.VEIN_MINER.isFunctional()) {
            Config.compatibility.evadeFalsePositives(
                    PluginBase.getProtocol(e.getPlayer()),
                    Compatibility.CompatibilityType.VEIN_MINER,
                    new CheckEnums.HackType[]{
                            CheckEnums.HackType.NO_SWING,
                            CheckEnums.HackType.FAST_BREAK,
                            CheckEnums.HackType.IMPOSSIBLE_ACTIONS,
                            CheckEnums.HackType.BLOCK_REACH,
                            CheckEnums.HackType.FAST_PLACE
                    },
                    AlgebraUtils.integerRound(TPS.maximum)
            );
        }
    }

}
