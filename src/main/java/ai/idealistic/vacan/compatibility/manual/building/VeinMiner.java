package ai.idealistic.vacan.compatibility.manual.building;

import ai.idealistic.vacan.Register;
import ai.idealistic.vacan.abstraction.Enums;
import ai.idealistic.vacan.compatibility.Compatibility;
import ai.idealistic.vacan.functionality.server.Config;
import ai.idealistic.vacan.functionality.server.PluginBase;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
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
                    new Enums.HackType[]{
                            Enums.HackType.NoSwing,
                            Enums.HackType.FastBreak,
                            Enums.HackType.GhostHand,
                            Enums.HackType.BlockReach
                    },
                    30
            );
        }
    }
}
