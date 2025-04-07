package ai.idealistic.spartan.utils.minecraft.entity;

import ai.idealistic.spartan.abstraction.protocol.ExtendedPotionEffect;
import ai.idealistic.spartan.abstraction.protocol.PlayerProtocol;
import ai.idealistic.spartan.functionality.server.MultiVersion;
import ai.idealistic.spartan.functionality.server.TPS;
import ai.idealistic.spartan.utils.math.AlgebraUtils;
import ai.idealistic.spartan.utils.minecraft.inventory.MaterialUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

import static org.bukkit.potion.PotionEffectType.*;

public class PlayerUtils {

    public static final boolean
            slowFall = MultiVersion.isOrGreater(MultiVersion.MCVersion.V1_13),
            dolphinsGrace = MultiVersion.isOrGreater(MultiVersion.MCVersion.V1_13),
            soulSpeed = MultiVersion.isOrGreater(MultiVersion.MCVersion.V1_16),
            levitation = MultiVersion.isOrGreater(MultiVersion.MCVersion.V1_9),
            elytra = MultiVersion.isOrGreater(MultiVersion.MCVersion.V1_9),
            trident = MultiVersion.isOrGreater(MultiVersion.MCVersion.V1_13);
    private static final Material
            gold_sword = MaterialUtils.get("gold_sword"),
            wood_sword = MaterialUtils.get("wood_sword"),
            gold_axe = MaterialUtils.get("gold_axe"),
            wood_axe = MaterialUtils.get("wood_axe"),
            gold_pickaxe = MaterialUtils.get("gold_pickaxe"),
            wood_pickaxe = MaterialUtils.get("wood_pickaxe"),
            diamond_spade = MaterialUtils.get("diamond_spade"),
            iron_spade = MaterialUtils.get("iron_spade"),
            gold_spade = MaterialUtils.get("gold_spade"),
            stone_spade = MaterialUtils.get("stone_spade"),
            wood_spade = MaterialUtils.get("wood_spade");

    public static final double
            optimizationY = MultiVersion.isOrGreater(MultiVersion.MCVersion.V1_9) ? 0.005 : 0.003,
            airDrag = AlgebraUtils.floatDouble(0.98),
            waterDrag = AlgebraUtils.floatDouble(0.8),
            lavaDrag = 0.5,
            jumpAcceleration = AlgebraUtils.floatDouble(0.42),
            airAcceleration = 0.08,
            airAccelerationUnloaded = AlgebraUtils.floatDouble(0.098),
            slowFallAcceleration = 0.01,
            liquidAcceleration = 0.02,
            chunk = 16.0,
            climbingUpDefault = 0.12 * airDrag, // 0.11760
            climbingDownDefault = AlgebraUtils.floatDouble(0.15),
            honeyBlockDownDefault = AlgebraUtils.floatDouble(0.13) * airDrag,
            webBlockDownDefault = AlgebraUtils.floatDouble(0.64) * airDrag,
            maxJumpingMotionDifference;

    public static final int
            playerInventorySlots = (9 * 5) + 1,
            height,
            fallDamageAboveBlocks = 3;

    private static final Map<Byte, List<Double>> jumpsValues = new LinkedHashMap<>();
    private static final Map<PotionEffectType, Long> handledPotionEffects = new LinkedHashMap<>();

    static {
        handledPotionEffects.put(PotionEffectUtils.JUMP, AlgebraUtils.integerRound(TPS.maximum * 5L) * TPS.tickTime);
        handledPotionEffects.put(SPEED, AlgebraUtils.integerRound(TPS.maximum * 2L) * TPS.tickTime);

        if (dolphinsGrace) {
            handledPotionEffects.put(DOLPHINS_GRACE, AlgebraUtils.integerRound(TPS.maximum) * TPS.tickTime);
        }
        if (slowFall) {
            handledPotionEffects.put(SLOW_FALLING, 10L * TPS.tickTime);
        }
        if (levitation) {
            handledPotionEffects.put(LEVITATION, 10L * TPS.tickTime);
        }

        // Separator

        for (int jumpEffect = 0; jumpEffect < 256; jumpEffect++) {
            List<Double> jumps = new ArrayList<>();
            double jump = jumpAcceleration + (jumpEffect * 0.1);

            while (jump > 0.0) {
                jumps.add(jump);
                jump = (jump - airAcceleration) * airDrag;
            }
            jumpsValues.put((byte) jumpEffect, jumps);
        }
        Iterator<Double> iterator = jumpsValues.get((byte) 0).iterator();
        double maxJumpingMotionDifferenceLocal = Double.MIN_VALUE,
                previousJumpingMotion = iterator.next();

        while (iterator.hasNext()) {
            double currentMotion = iterator.next();
            maxJumpingMotionDifferenceLocal = Math.max(
                    previousJumpingMotion - currentMotion,
                    maxJumpingMotionDifferenceLocal
            );
            previousJumpingMotion = currentMotion;
        }
        maxJumpingMotionDifference = maxJumpingMotionDifferenceLocal;

        // Separator

        if (MultiVersion.isOrGreater(MultiVersion.MCVersion.V1_17)) {
            List<World> worlds = Bukkit.getWorlds();

            if (!worlds.isEmpty()) {
                int max = 256;

                for (World world : worlds) {
                    max = Math.max(world.getMaxHeight(), max);
                }
                height = max;
            } else {
                height = 256;
            }
        } else {
            height = 256;
        }
    }

    // Jumping

    public static boolean isJumping(double d, int jump, double diff) {
        if (d > 0.0) {
            for (double value : jumpsValues.get((byte) jump)) {
                if (Math.abs(value - d) < diff) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean justJumped(double d, int jump, double diff) {
        if (d > 0.0) {
            for (double value : jumpsValues.get((byte) jump)) {
                if (Math.abs(value - d) < diff) {
                    return true;
                }
            }
        }
        return false;
    }

    public static List<Double> getJumpMotions(int jump) {
        return jumpsValues.get((byte) jump);
    }

    public static double getJumpMotionSum(int jump) {
        return jumpsValues.get((byte) jump).stream().mapToDouble(Double::doubleValue).sum();
    }

    public static double getJumpMotion(int jump, int tick) {
        return jumpsValues.get((byte) jump).get(tick);
    }

    public static int getJumpTicks(int jump) {
        return getJumpMotions(jump).size();
    }

    // Falling

    public static double calculateTerminalVelocity(double drag, double acceleration) {
        return ((1.0 / (1.0 - drag)) * acceleration);
    }

    public static double calculateNextFallMotion(double motion,
                                                 double acceleration, double drag) {
        double terminalVelocity = calculateTerminalVelocity(acceleration, drag);

        if (motion >= -terminalVelocity) {
            return (motion + acceleration) * drag;
        } else {
            return Double.MIN_VALUE;
        }
    }

    // Inventory

    public static boolean isSpadeItem(Material m) {
        return m == diamond_spade
                || m == iron_spade
                || m == gold_spade
                || m == stone_spade
                || m == wood_spade
                || MultiVersion.isOrGreater(MultiVersion.MCVersion.V1_16) && m == Material.NETHERITE_HOE;
    }

    public static boolean isPickaxeItem(Material m) {
        return m == Material.DIAMOND_PICKAXE
                || m == Material.IRON_PICKAXE
                || m == Material.STONE_PICKAXE
                || m == gold_pickaxe
                || m == wood_pickaxe
                || MultiVersion.isOrGreater(MultiVersion.MCVersion.V1_16) && m == Material.NETHERITE_PICKAXE;
    }

    public static boolean isAxeItem(Material m) {
        return m == Material.DIAMOND_AXE
                || m == Material.IRON_AXE
                || m == Material.STONE_AXE
                || m == gold_axe
                || m == wood_axe
                || MultiVersion.isOrGreater(MultiVersion.MCVersion.V1_16) && m == Material.NETHERITE_AXE;
    }

    public static boolean isSwordItem(Material type) {
        return type == Material.DIAMOND_SWORD
                || type == gold_sword
                || type == Material.IRON_SWORD
                || type == Material.STONE_SWORD
                || type == wood_sword
                || MultiVersion.isOrGreater(MultiVersion.MCVersion.V1_16) && type == Material.NETHERITE_SWORD;
    }

    // Potion Effects

    public static int getPotionLevel(PlayerProtocol protocol, PotionEffectType potionEffectType) {
        ExtendedPotionEffect potionEffect = protocol.getPotionEffect(
                potionEffectType,
                handledPotionEffects.getOrDefault(potionEffectType, 0L)
        );

        if (potionEffect != null) {
            return potionEffect.bukkitEffect.getAmplifier();
        } else {
            return -1;
        }
    }

}
