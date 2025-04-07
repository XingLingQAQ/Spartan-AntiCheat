package ai.idealistic.spartan.abstraction.profiling;

import ai.idealistic.spartan.abstraction.check.CheckEnums;
import ai.idealistic.spartan.abstraction.protocol.PlayerProtocol;
import ai.idealistic.spartan.api.PlayerFoundOreEvent;
import ai.idealistic.spartan.functionality.server.Config;
import ai.idealistic.spartan.functionality.server.MultiVersion;
import ai.idealistic.spartan.functionality.tracking.AntiCheatLogs;
import ai.idealistic.spartan.utils.minecraft.entity.PlayerUtils;
import ai.idealistic.spartan.utils.minecraft.world.BlockUtils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

public class MiningHistory {

    public static final String
            environmentIdentifier = "Environment: ",
            amountIdentifier = "Amount: ",
            oreIdentifier = "Ore: ";

    public static void log(PlayerProtocol protocol, Block block, boolean cancelled) {
        if (protocol.getGameMode() == GameMode.SURVIVAL
                && PlayerUtils.isPickaxeItem(protocol.getItemInHand().getType())) {
            MiningHistory.MiningOre ore = MiningHistory.getMiningOre(block.getType());

            if (ore != null) {
                World.Environment environment = block.getWorld().getEnvironment();
                int x = block.getX(), y = block.getY(), z = block.getZ(), amount = 1;
                String key = ore.toString(),
                        log = "(" + AntiCheatLogs.playerIdentifier + protocol.bukkit().getName() + "), "
                                + "(" + amountIdentifier + amount + "), "
                                + "(" + oreIdentifier + key + "), "
                                + "(" + environmentIdentifier + BlockUtils.environmentToString(environment) + "), "
                                + "(W-XYZ: " + block.getWorld().getName() + " " + x + " " + y + " " + z + ")";

                // API Event
                PlayerFoundOreEvent event;

                if (Config.settings.getBoolean("Important.enable_developer_api")) {
                    event = new PlayerFoundOreEvent(protocol.bukkit(), log, block.getLocation(), block.getType());
                    Bukkit.getPluginManager().callEvent(event);
                } else {
                    event = null;
                }

                if (event == null || !event.isCancelled()) {
                    AntiCheatLogs.logInfo(
                            protocol,
                            null,
                            log,
                            false,
                            block.getType(),
                            null,
                            System.currentTimeMillis()
                    );
                    MiningHistory miningHistory = protocol.profile().getMiningHistory(ore);

                    if (miningHistory != null) {
                        String pluralKey = key.endsWith("s") ? (key + "es") : (key + "s");
                        miningHistory.increaseMines(environment, amount);
                        protocol.getRunner(CheckEnums.HackType.X_RAY).handle(
                                cancelled,
                                new Object[]{environment, miningHistory, ore, pluralKey});
                    }
                }
            }
        }
    }

    public static MiningHistory.MiningOre getMiningOre(Material material) {
        if (MultiVersion.isOrGreater(MultiVersion.MCVersion.V1_16)) {
            if (material == Material.ANCIENT_DEBRIS) {
                return MiningHistory.MiningOre.ANCIENT_DEBRIS;
            }
            if (material == Material.GILDED_BLACKSTONE || material == Material.NETHER_GOLD_ORE) {
                return MiningHistory.MiningOre.GOLD;
            }
            if (MultiVersion.isOrGreater(MultiVersion.MCVersion.V1_17)) {
                if (material == Material.DEEPSLATE_DIAMOND_ORE) {
                    return MiningHistory.MiningOre.DIAMOND;
                }
                if (material == Material.DEEPSLATE_EMERALD_ORE) {
                    return MiningHistory.MiningOre.EMERALD;
                }
                if (material == Material.DEEPSLATE_GOLD_ORE) {
                    return MiningHistory.MiningOre.GOLD;
                }
            }
        }
        switch (material) {
            case DIAMOND_ORE:
                return MiningHistory.MiningOre.DIAMOND;
            case EMERALD_ORE:
                return MiningHistory.MiningOre.EMERALD;
            case GOLD_ORE:
                return MiningHistory.MiningOre.GOLD;
        }
        return null;
    }

    public enum MiningOre {
        ANCIENT_DEBRIS, DIAMOND, EMERALD, GOLD;

        private final String string;

        MiningOre() {
            string = this.name().toLowerCase().replace("_", "-");
        }

        @Override
        public String toString() {
            return string;
        }
    }

    private final PlayerProfile profile;
    public final MiningOre ore;
    private final int[] count;

    MiningHistory(PlayerProfile profile, MiningOre ore) {
        World.Environment[] environments = World.Environment.values();
        this.profile = profile;
        this.ore = ore;
        this.count = new int[environments.length];

        for (int i = 0; i < environments.length; i++) {
            this.count[i] = 0;
        }
    }

    public double getMinesToTimeRatio(World.Environment environment) {
        int mines = this.count[environment.ordinal()];

        if (mines > 0) {
            long onlineTime = this.profile.getContinuity().getOnlineTime();

            if (onlineTime > 0L) {
                onlineTime /= 1_000L; // Convert to seconds
                return mines / ((double) onlineTime);
            } else {
                return 0.0;
            }
        } else {
            return 0.0;
        }
    }

    public void increaseMines(World.Environment environment, int amount) {
        count[environment.ordinal()] += amount;
    }

}
