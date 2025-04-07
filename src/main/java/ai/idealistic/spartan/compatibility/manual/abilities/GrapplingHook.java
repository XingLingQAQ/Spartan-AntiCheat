package ai.idealistic.spartan.compatibility.manual.abilities;

import ai.idealistic.spartan.abstraction.check.CheckEnums;
import ai.idealistic.spartan.abstraction.protocol.PlayerProtocol;
import ai.idealistic.spartan.compatibility.Compatibility;
import ai.idealistic.spartan.functionality.server.Config;
import ai.idealistic.spartan.functionality.server.PluginBase;
import ai.idealistic.spartan.utils.minecraft.server.PluginUtils;
import com.snowgears.grapplinghook.api.HookAPI;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;

public class GrapplingHook implements Listener {

    private final static String name = "grapplinghook";

    private static boolean isItem(ItemStack i) {
        if (Compatibility.CompatibilityType.GRAPPLING_HOOK.isFunctional()) {
            try {
                return PluginUtils.exists(name) ? HookAPI.isGrapplingHook(i) : i.getType() == Material.FISHING_ROD;
            } catch (Exception ignored) {
            }
        }
        return false;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    private void Event(PlayerFishEvent e) {
        if (Compatibility.CompatibilityType.GRAPPLING_HOOK.isFunctional() && e.getState() == PlayerFishEvent.State.CAUGHT_ENTITY) {
            Entity caught = e.getCaught();

            if (caught instanceof Player) {
                PlayerProtocol p = PluginBase.getProtocol((Player) caught),
                        t = PluginBase.getProtocol(e.getPlayer());

                if (!p.equals(t) && isItem(t.getItemInHand())) {
                    if (PluginUtils.exists(name)) {
                        Config.compatibility.evadeFalsePositives(
                                p,
                                Compatibility.CompatibilityType.GRAPPLING_HOOK,
                                new CheckEnums.HackCategoryType[]{
                                        CheckEnums.HackCategoryType.MOVEMENT,
                                        CheckEnums.HackCategoryType.COMBAT
                                },
                                40
                        );
                    } else {
                        Config.compatibility.evadeFalsePositives(
                                p,
                                Compatibility.CompatibilityType.GRAPPLING_HOOK,
                                new CheckEnums.HackCategoryType[]{
                                        CheckEnums.HackCategoryType.MOVEMENT,
                                        CheckEnums.HackCategoryType.COMBAT
                                },
                                10
                        );
                    }
                }
            }
        }
    }
}
