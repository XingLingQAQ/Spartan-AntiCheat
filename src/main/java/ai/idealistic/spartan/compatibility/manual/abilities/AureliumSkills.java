package ai.idealistic.spartan.compatibility.manual.abilities;

import ai.idealistic.spartan.abstraction.data.Cooldowns;
import ai.idealistic.spartan.abstraction.protocol.PlayerProtocol;
import ai.idealistic.spartan.compatibility.Compatibility;
import ai.idealistic.spartan.functionality.server.PluginBase;
import ai.idealistic.spartan.utils.java.OverflowMap;
import com.archyx.aureliumskills.api.event.TerraformBlockBreakEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.LinkedHashMap;

public class AureliumSkills implements Listener {

    private static final Cooldowns cooldowns = new Cooldowns(
            new OverflowMap<>(new LinkedHashMap<>(), 512)
    );

    @EventHandler(priority = EventPriority.HIGHEST)
    private void Event(TerraformBlockBreakEvent e) {
        if (Compatibility.CompatibilityType.AURELIUM_SKILLS.isEnabled()) {
            PlayerProtocol protocol = PluginBase.getProtocol(e.getPlayer());
            cooldowns.add(protocol.getUUID() + "=aureliumskills=compatibility", 20);
        }
    }

    public static boolean isUsing(PlayerProtocol p) {
        return Compatibility.CompatibilityType.AURELIUM_SKILLS.isFunctional()
                && !cooldowns.canDo(p.getUUID() + "=aureliumskills=compatibility");
    }
}
