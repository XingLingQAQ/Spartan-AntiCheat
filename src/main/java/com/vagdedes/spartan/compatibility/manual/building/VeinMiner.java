package com.vagdedes.spartan.compatibility.manual.building;

import com.vagdedes.spartan.Register;
import com.vagdedes.spartan.abstraction.configuration.implementation.Compatibility;
import com.vagdedes.spartan.abstraction.player.SpartanPlayer;
import com.vagdedes.spartan.compatibility.necessary.protocollib.ProtocolLib;
import com.vagdedes.spartan.functionality.server.Config;
import com.vagdedes.spartan.functionality.server.SpartanBukkit;
import me.vagdedes.spartan.system.Enums;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import wtf.choco.veinminer.api.event.player.PlayerVeinMineEvent;

public class VeinMiner implements Listener {

    public static void reload() {
        Register.enable(new VeinMiner(), VeinMiner.class);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    private void Event(PlayerVeinMineEvent e) {
        if (Compatibility.CompatibilityType.VEIN_MINER.isFunctional()) {
            Player n = e.getPlayer();

            if (ProtocolLib.isTemporary(n)) {
                return;
            }
            SpartanPlayer p = SpartanBukkit.getProtocol(n).spartanPlayer;

            if (p == null) {
                return;
            }
            Config.compatibility.evadeFalsePositives(
                    p,
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
