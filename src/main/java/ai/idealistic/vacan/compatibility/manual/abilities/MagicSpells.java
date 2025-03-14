package ai.idealistic.vacan.compatibility.manual.abilities;

import ai.idealistic.vacan.abstraction.check.CheckEnums;
import ai.idealistic.vacan.compatibility.Compatibility;
import ai.idealistic.vacan.functionality.server.Config;
import ai.idealistic.vacan.functionality.server.PluginBase;
import com.nisovin.magicspells.events.SpellCastEvent;
import com.nisovin.magicspells.events.SpellCastedEvent;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class MagicSpells implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    private void SpellCast(SpellCastEvent e) {
        if (Compatibility.CompatibilityType.MAGIC_SPELLS.isFunctional()) {
            LivingEntity caster = e.getCaster();

            if (caster instanceof Player) {
                Config.compatibility.evadeFalsePositives(
                        PluginBase.getProtocol((Player) caster),
                        Compatibility.CompatibilityType.MAGIC_SPELLS,
                        new CheckEnums.HackCategoryType[]{
                                CheckEnums.HackCategoryType.MOVEMENT,
                                CheckEnums.HackCategoryType.COMBAT
                        },
                        40
                );
            }
        }
    }

    @EventHandler
    private void SpellCasted(SpellCastedEvent e) {
        if (Compatibility.CompatibilityType.MAGIC_SPELLS.isFunctional()) {
            LivingEntity caster = e.getCaster();

            if (caster instanceof Player) {
                Config.compatibility.evadeFalsePositives(
                        PluginBase.getProtocol((Player) caster),
                        Compatibility.CompatibilityType.MAGIC_SPELLS,
                        new CheckEnums.HackCategoryType[]{
                                CheckEnums.HackCategoryType.MOVEMENT,
                                CheckEnums.HackCategoryType.COMBAT
                        },
                        40
                );
            }
        }
    }

    /*@EventHandler
    private void Spell(SpellEvent e) {
    }*/
}
