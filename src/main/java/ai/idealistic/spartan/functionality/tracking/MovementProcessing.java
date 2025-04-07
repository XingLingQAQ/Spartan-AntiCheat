package ai.idealistic.spartan.functionality.tracking;

import ai.idealistic.spartan.abstraction.protocol.PlayerProtocol;
import ai.idealistic.spartan.compatibility.Compatibility;
import ai.idealistic.spartan.compatibility.manual.abilities.ItemsAdder;
import ai.idealistic.spartan.compatibility.manual.building.MythicMobs;
import ai.idealistic.spartan.compatibility.manual.vanilla.Attributes;
import ai.idealistic.spartan.utils.minecraft.entity.CombatUtils;
import org.bukkit.entity.Entity;

import java.util.List;

public class MovementProcessing {

    public static boolean canCheck(PlayerProtocol protocol,
                                   boolean vehicle,
                                   boolean elytra,
                                   boolean flight,
                                   boolean playerAttributes,
                                   boolean environmentalAttributes) {
        if ((elytra || !protocol.isGliding())
                && (flight || !protocol.wasFlying())
                && (vehicle || protocol.getVehicle() == null)

                && (playerAttributes
                || Attributes.getAmount(protocol, Attributes.GENERIC_MOVEMENT_SPEED) == Double.MIN_VALUE
                && Attributes.getAmount(protocol, Attributes.GENERIC_JUMP_STRENGTH) == Double.MIN_VALUE)

                && (environmentalAttributes
                || Attributes.getAmount(protocol, Attributes.GENERIC_STEP_HEIGHT) == Double.MIN_VALUE
                && Attributes.getAmount(protocol, Attributes.GENERIC_GRAVITY) == Double.MIN_VALUE)) {
            if (Compatibility.CompatibilityType.MYTHIC_MOBS.isFunctional()
                    || Compatibility.CompatibilityType.ITEMS_ADDER.isFunctional()) {
                List<Entity> entities = protocol.getNearbyEntities(
                        CombatUtils.maxHitDistance,
                        CombatUtils.maxHitDistance,
                        CombatUtils.maxHitDistance
                );

                if (!entities.isEmpty()) {
                    for (Entity entity : entities) {
                        if (MythicMobs.is(entity) || ItemsAdder.is(entity)) {
                            return false;
                        }
                    }
                }
            }
            return true;
        }
        return false;
    }

}
