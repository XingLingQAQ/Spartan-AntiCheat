package ai.idealistic.vacan.abstraction.check;

import ai.idealistic.vacan.utils.minecraft.inventory.MaterialUtils;
import lombok.Getter;
import org.bukkit.Material;

public class CheckEnums {

    public enum HackType {
        X_RAY(
                HackCategoryType.WORLD,
                ai.idealistic.vacan.abstraction.check.implementation.world.XRay.class,
                new String[]{
                        "This check will prevent client modules",
                        "that allow a player to see through blocks",
                        "in order to find rare ores, such as diamonds,",
                        "gold, and even emerald. (Logs must be enabled)"
                }
        ),
        EXPLOITS(
                HackCategoryType.WORLD,
                ai.idealistic.vacan.abstraction.check.implementation.world.exploits.Exploits.class,
                new String[]{
                        "This check will prevent client",
                        "modules that may potentially hurt",
                        "a server's functional performance."
                }
        ),
        NO_SWING(
                HackCategoryType.PLAYER,
                ai.idealistic.vacan.abstraction.check.implementation.player.NoSwing.class,
                new String[]{
                        "This check will prevent client modules",
                        "that manipulate packets and prevent",
                        "interaction animations from being shown."
                }
        ),
        IRREGULAR_MOVEMENTS(
                HackCategoryType.MOVEMENT,
                ai.idealistic.vacan.abstraction.check.implementation.movement.irregularmovements.IrregularMovements.class,
                new String[]{
                        "This check will prevent client modules",
                        "that allow a player to move abnormally,",
                        "such as stepping blocks or climbing walls."
                }
        ),
        IMPOSSIBLE_ACTIONS(
                HackCategoryType.WORLD,
                ai.idealistic.vacan.abstraction.check.implementation.world.impossibleactions.ImpossibleActions.class,
                new String[]{
                        "This check will prevent client modules",
                        "that allow a player to execute actions",
                        "in abnormal cases, such as when sleeping."
                }
        ),
        AUTO_RESPAWN(
                HackCategoryType.PLAYER,
                ai.idealistic.vacan.abstraction.check.implementation.player.AutoRespawn.class,
                new String[]{
                        "This check will prevent client modules",
                        "that allow a player to respawn faster",
                        "than what is physically expected."
                }
        ),
        INVENTORY_CLICKS(
                HackCategoryType.INVENTORY,
                ai.idealistic.vacan.abstraction.check.implementation.inventory.InventoryClicks.class,
                new String[]{
                        "This check will prevent client modules",
                        "that allow a player to interact with an",
                        "amount of items, in abnormally fast rates."
                }
        ),
        CRITICALS(
                HackCategoryType.COMBAT,
                ai.idealistic.vacan.abstraction.check.implementation.combat.Criticals.class,
                new String[]{
                        "This check will prevent client modules",
                        "that allow a player to critical damage",
                        "an entity without properly moving."
                }
        ),
        GHOST_HAND(
                HackCategoryType.WORLD,
                ai.idealistic.vacan.abstraction.check.implementation.world.GhostHand.class,
                new String[]{
                        "This check will prevent client modules",
                        "that allow a player to interact or break",
                        "blocks through walls of blocks."
                }
        ),
        BLOCK_REACH(
                HackCategoryType.WORLD,
                ai.idealistic.vacan.abstraction.check.implementation.world.BlockReach.class,
                new String[]{
                        "This check will prevent client modules",
                        "that allow a player to build or break",
                        "blocks within an abnormally long distance."
                }
        ),
        FAST_BOW(
                HackCategoryType.COMBAT,
                ai.idealistic.vacan.abstraction.check.implementation.combat.FastBow.class,
                new String[]{
                        "This check will prevent client modules",
                        "that allow a player to shoot arrows",
                        "in abnormally fast rates."
                }
        ),
        FAST_CLICKS(
                HackCategoryType.COMBAT,
                ai.idealistic.vacan.abstraction.check.implementation.combat.FastClicks.class,
                new String[]{
                        "This check will prevent client modules",
                        "that allow a player to click abnormally fast",
                        "or have an irregular clicking consistency."
                }
        ),
        FAST_HEAL(
                HackCategoryType.PLAYER,
                ai.idealistic.vacan.abstraction.check.implementation.player.FastHeal.class,
                new String[]{
                        "This check will prevent client modules",
                        "that allow a player to heal faster",
                        "than what is physically allowed."
                }
        ),
        IMPOSSIBLE_INVENTORY(
                HackCategoryType.INVENTORY,
                ai.idealistic.vacan.abstraction.check.implementation.inventory.ImpossibleInventory.class,
                new String[]{
                        "This check will prevent client modules",
                        "that allow a player to interact with",
                        "an inventory in abnormal cases, such",
                        "as when sprinting or walking."
                }
        ),
        HIT_REACH(
                HackCategoryType.COMBAT,
                ai.idealistic.vacan.abstraction.check.implementation.combat.HitReach.class,
                new String[]{
                        "This check will prevent client modules",
                        "that allow a player to hit entities",
                        "from an abnormally long distance"
                }
        ),
        FAST_BREAK(
                HackCategoryType.WORLD,
                ai.idealistic.vacan.abstraction.check.implementation.world.FastBreak.class,
                new String[]{
                        "This check will prevent client modules",
                        "that allow a player to break one or multiple",
                        "blocks irregularly fast."
                }
        ),
        FAST_PLACE(
                HackCategoryType.WORLD,
                ai.idealistic.vacan.abstraction.check.implementation.world.FastPlace.class,
                new String[]{
                        "This check will prevent client modules",
                        "that allow a player to place blocks",
                        "in abnormally fast rates."
                }
        ),
        MORE_PACKETS(
                HackCategoryType.MOVEMENT,
                ai.idealistic.vacan.abstraction.check.implementation.movement.MorePackets.class,
                new String[]{
                        "This check will prevent client modules",
                        "that allow a player to send abnormally",
                        "high amounts of movement packets."
                }
        ),
        FAST_EAT(
                HackCategoryType.PLAYER,
                ai.idealistic.vacan.abstraction.check.implementation.player.FastEat.class,
                new String[]{
                        "This check will prevent client modules",
                        "that allow a player to consume an amount",
                        "of food in an abnormal amount of time."
                }
        ),
        VELOCITY(
                HackCategoryType.COMBAT,
                ai.idealistic.vacan.abstraction.check.implementation.combat.Velocity.class,
                new String[]{
                        "This check will prevent client modules",
                        "that allow a player to receive abnormal",
                        "amounts of knockback, or none at all."
                }
        ),
        KILL_AURA(
                HackCategoryType.COMBAT,
                ai.idealistic.vacan.abstraction.check.implementation.combat.killaura.KillAura.class,
                new String[]{
                        "This check will prevent client modules",
                        "that allow a player to have an 'apparent'",
                        "combat advantage against any entity."
                }
        );

        @Getter
        private Check check;
        public final HackCategoryType category;
        public final Class<?> executor;
        public final String[] description;

        HackType(HackCategoryType category, Class<?> executor, String[] description) {
            this.category = category;
            this.executor = executor;
            this.description = description;
            this.check = new Check(this);
        }

        public void resetCheck() {
            if (this.check != null) {
                this.check = new Check(this);
            }
        }

        @Override
        public String toString() {
            return super.toString().toLowerCase().replace("_", "-");
        }
    }

    public enum HackCategoryType {
        COMBAT(Material.IRON_SWORD),
        MOVEMENT(Material.FEATHER),
        PLAYER(Material.STICK),
        WORLD(Material.DIAMOND_PICKAXE),
        INVENTORY(MaterialUtils.get("crafting_table"));

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
