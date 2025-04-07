package ai.idealistic.spartan.abstraction.check;

import ai.idealistic.spartan.abstraction.check.implementation.misc.ImpossibleInventory;
import ai.idealistic.spartan.abstraction.check.implementation.misc.InventoryClicks;
import ai.idealistic.spartan.abstraction.check.implementation.movement.GravitySimulation;
import ai.idealistic.spartan.abstraction.check.implementation.movement.SpeedSimulation;
import ai.idealistic.spartan.abstraction.check.implementation.movement.exploits.Exploits;
import ai.idealistic.spartan.utils.minecraft.inventory.MaterialUtils;
import lombok.Getter;
import org.bukkit.Material;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CheckEnums {

    public enum HackType {
        HIT_REACH(
                HackCategoryType.COMBAT,
                ai.idealistic.spartan.abstraction.check.implementation.combat.HitReach.class
        ),
        CRITICALS(
                HackCategoryType.COMBAT,
                ai.idealistic.spartan.abstraction.check.implementation.combat.Criticals.class
        ),
        FAST_CLICKS(
                HackCategoryType.COMBAT,
                ai.idealistic.spartan.abstraction.check.implementation.combat.FastClicks.class
        ),
        VELOCITY(
                HackCategoryType.COMBAT,
                ai.idealistic.spartan.abstraction.check.implementation.combat.Velocity.class
        ),
        KILL_AURA(
                HackCategoryType.COMBAT,
                ai.idealistic.spartan.abstraction.check.implementation.combat.killaura.KillAura.class
        ),
        IRREGULAR_MOVEMENTS(
                HackCategoryType.MOVEMENT,
                ai.idealistic.spartan.abstraction.check.implementation.movement.irregularmovements.IrregularMovements.class
        ),
        GRAVITY_SIMULATION(
                HackCategoryType.MOVEMENT,
                GravitySimulation.class
        ),
        SPEED_SIMULATION(
                HackCategoryType.MOVEMENT,
                SpeedSimulation.class
        ),
        EXPLOITS(
                HackCategoryType.MOVEMENT,
                Exploits.class
        ),
        MORE_PACKETS(
                HackCategoryType.MOVEMENT,
                ai.idealistic.spartan.abstraction.check.implementation.movement.MorePackets.class
        ),
        X_RAY(
                HackCategoryType.WORLD,
                ai.idealistic.spartan.abstraction.check.implementation.world.XRay.class
        ),
        IMPOSSIBLE_ACTIONS(
                HackCategoryType.WORLD,
                ai.idealistic.spartan.abstraction.check.implementation.world.impossibleactions.ImpossibleActions.class
        ),
        FAST_BREAK(
                HackCategoryType.WORLD,
                ai.idealistic.spartan.abstraction.check.implementation.world.FastBreak.class
        ),
        FAST_PLACE(
                HackCategoryType.WORLD,
                ai.idealistic.spartan.abstraction.check.implementation.world.FastPlace.class
        ),
        BLOCK_REACH(
                HackCategoryType.WORLD,
                ai.idealistic.spartan.abstraction.check.implementation.world.BlockReach.class
        ),
        NO_SWING(
                HackCategoryType.MISCELLANEOUS,
                ai.idealistic.spartan.abstraction.check.implementation.misc.NoSwing.class
        ),
        AUTO_RESPAWN(
                HackCategoryType.MISCELLANEOUS,
                ai.idealistic.spartan.abstraction.check.implementation.misc.AutoRespawn.class
        ),
        INVENTORY_CLICKS(
                HackCategoryType.MISCELLANEOUS,
                InventoryClicks.class
        ),
        FAST_HEAL(
                HackCategoryType.MISCELLANEOUS,
                ai.idealistic.spartan.abstraction.check.implementation.misc.FastHeal.class
        ),
        IMPOSSIBLE_INVENTORY(
                HackCategoryType.MISCELLANEOUS,
                ImpossibleInventory.class
        ),
        FAST_EAT(
                HackCategoryType.MISCELLANEOUS,
                ai.idealistic.spartan.abstraction.check.implementation.misc.FastEat.class
        );

        @Getter
        private Check check;
        public final HackCategoryType category;
        public final Class<?> executor;
        private final Map<String, Long> detections;

        HackType(HackCategoryType category, Class<?> executor) {
            this.category = category;
            this.executor = executor;
            this.detections = new ConcurrentHashMap<>();
            this.check = new Check(this);
        }

        public void resetCheck() {
            if (this.check != null) {
                this.check = new Check(this);
            }
        }

        public void addDetection(String detection, long averageTime) {
            this.detections.put(detection, averageTime);
        }

        public void removeDetection(String detection) {
            this.detections.remove(detection);
        }

        public long getDefaultAverageTime(String detection) {
            return this.detections.get(detection);
        }

        public Collection<String> getDetections() {
            return this.detections.keySet();
        }

        @Override
        public String toString() {
            return super.toString().toLowerCase().replace("_", "-");
        }
    }

    public enum HackCategoryType {
        COMBAT(Material.IRON_SWORD),
        MOVEMENT(Material.FEATHER),
        WORLD(Material.DIAMOND_PICKAXE),
        MISCELLANEOUS(MaterialUtils.get("crafting_table"));

        public final Material material;

        HackCategoryType(Material material) {
            this.material = material;
        }

        @Override
        public String toString() {
            return super.toString().toLowerCase().replace("_", "-");
        }

    }

}
