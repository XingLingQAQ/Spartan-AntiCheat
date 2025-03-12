package ai.idealistic.vacan.compatibility.manual.abilities.crackshot;

import ai.idealistic.vacan.abstraction.Enums;
import ai.idealistic.vacan.abstraction.data.Buffer;
import ai.idealistic.vacan.abstraction.protocol.PlayerProtocol;
import ai.idealistic.vacan.compatibility.Compatibility;
import ai.idealistic.vacan.functionality.server.Config;
import ai.idealistic.vacan.functionality.server.PluginBase;
import ai.idealistic.vacan.utils.java.OverflowMap;
import me.DeeCaaD.CrackShotPlus.Events.WeaponSecondScopeEvent;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.LinkedHashMap;

public class CrackShotPlus implements Listener {

    private static final Buffer buffers = new Buffer(
            new OverflowMap<>(new LinkedHashMap<>(), 512)
    );

    @EventHandler(priority = EventPriority.HIGHEST)
    private void WeaponScope(WeaponSecondScopeEvent e) {
        if (Compatibility.CompatibilityType.CRACK_SHOT_PLUS.isFunctional()) {
            PlayerProtocol protocol = PluginBase.getProtocol(e.getPlayer());

            if (!e.isCancelled()) {
                Config.compatibility.evadeFalsePositives(
                        protocol,
                        Compatibility.CompatibilityType.CRACK_SHOT_PLUS,
                        new Enums.HackCategoryType[]{
                                Enums.HackCategoryType.MOVEMENT,
                                Enums.HackCategoryType.COMBAT
                        },
                        20
                );

                if (e.isZoomIn()) {
                    buffers.set(protocol.getUUID() + "=crackshotplus=compatibility=scope", 1);
                } else {
                    buffers.remove(protocol.getUUID() + "=crackshotplus=compatibility=scope");
                }
            } else {
                buffers.remove(protocol.getUUID() + "=crackshotplus=compatibility=scope");
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    private void EntityDamage(EntityDamageEvent e) {
        if (Compatibility.CompatibilityType.CRACK_SHOT_PLUS.isFunctional()) {
            Entity entity = e.getEntity();

            if (entity instanceof Player) {
                PlayerProtocol p = PluginBase.getProtocol((Player) entity);

                if (isUsingScope(p)) {
                    Config.compatibility.evadeFalsePositives(
                            p,
                            Compatibility.CompatibilityType.CRACK_SHOT_PLUS,
                            new Enums.HackCategoryType[]{
                                    Enums.HackCategoryType.MOVEMENT,
                                    Enums.HackCategoryType.COMBAT
                            },
                            60
                    );
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    private void EntityByEntityDamage(EntityDamageByEntityEvent e) {
        if (Compatibility.CompatibilityType.CRACK_SHOT_PLUS.isFunctional()) {
            Entity entity = e.getDamager();

            if (entity instanceof Player) {
                PlayerProtocol p = PluginBase.getProtocol((Player) entity);

                if (isUsingScope(p)) {
                    Config.compatibility.evadeFalsePositives(
                            p,
                            Compatibility.CompatibilityType.CRACK_SHOT_PLUS,
                            new Enums.HackCategoryType[]{
                                    Enums.HackCategoryType.MOVEMENT,
                                    Enums.HackCategoryType.COMBAT
                            },
                            30
                    );
                }
            }
        }
    }

    static boolean isUsingScope(PlayerProtocol p) {
        return Compatibility.CompatibilityType.CRACK_SHOT_PLUS.isFunctional()
                && buffers.get(p.getUUID() + "=crackshotplus=compatibility=scope") != 0;
    }
}

