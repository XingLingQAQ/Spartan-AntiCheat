package ai.idealistic.vacan.abstraction.profiling;

import ai.idealistic.vacan.abstraction.check.Check;
import ai.idealistic.vacan.abstraction.check.CheckEnums;
import ai.idealistic.vacan.abstraction.check.CheckRunner;
import ai.idealistic.vacan.abstraction.protocol.PlayerProtocol;
import ai.idealistic.vacan.functionality.server.PluginBase;
import ai.idealistic.vacan.utils.minecraft.inventory.InventoryUtils;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;

public class PlayerProfile {

    public static final String
            activeFor = " was active for: ";

    public final String name;
    private final MiningHistory[] miningHistory;
    private ItemStack skull;
    private OfflinePlayer offlinePlayer;
    @Getter
    private final CheckRunner[] runners;
    @Getter
    @Setter
    private Check.DataType lastDataType;
    @Getter
    private Check.DetectionType lastDetectionType;
    @Getter
    private final ProfileContinuity continuity;

    // Separator

    public PlayerProfile(String name) {
        this.name = name;
        this.skull = null;
        this.runners = new CheckRunner[CheckEnums.HackType.values().length];
        this.miningHistory = new MiningHistory[MiningHistory.MiningOre.values().length];
        this.lastDataType = Check.DataType.JAVA;
        this.lastDetectionType = PluginBase.packetsEnabled()
                ? Check.DetectionType.PACKETS
                : Check.DetectionType.BUKKIT;
        this.continuity = new ProfileContinuity(this);

        for (MiningHistory.MiningOre ore : MiningHistory.MiningOre.values()) {
            this.miningHistory[ore.ordinal()] = new MiningHistory(this, ore);
        }

        // Separator

        PlayerProtocol protocol = PluginBase.getProtocol(name);

        if (protocol != null) {
            this.offlinePlayer = protocol.bukkit();
            this.registerRunners(protocol);
        } else {
            this.offlinePlayer = null;
            this.registerRunners(null);
        }
    }

    public PlayerProfile(PlayerProtocol protocol) {
        this.name = protocol.bukkit().getName();
        this.offlinePlayer = protocol.bukkit(); // Attention
        this.skull = null;
        this.runners = new CheckRunner[CheckEnums.HackType.values().length];
        this.miningHistory = new MiningHistory[MiningHistory.MiningOre.values().length];
        this.lastDataType = protocol.getDataType();
        this.lastDetectionType = protocol.packetsEnabled()
                ? Check.DetectionType.PACKETS
                : Check.DetectionType.BUKKIT;
        this.continuity = new ProfileContinuity(this);

        for (MiningHistory.MiningOre ore : MiningHistory.MiningOre.values()) {
            this.miningHistory[ore.ordinal()] = new MiningHistory(this, ore);
        }
        this.registerRunners(protocol);
    }

    // Separator

    public void update(PlayerProtocol protocol) {
        this.offlinePlayer = protocol.bukkit();
        this.lastDataType = protocol.getDataType();
        this.lastDetectionType = protocol.detectionType;
        this.registerRunners(protocol);
    }

    PlayerProtocol protocol() {
        return this.runners[0].protocol;
    }

    public boolean isOnline() {
        PlayerProtocol protocol = this.protocol();
        return protocol != null && PluginBase.isOnline(protocol);
    }

    public CheckRunner getRunner(CheckEnums.HackType hackType) {
        return this.runners[hackType.ordinal()];
    }

    public void executeRunners(Object cancelled, Object object) {
        for (CheckRunner runner : this.getRunners()) {
            runner.handle(cancelled, object);
        }
    }

    private void registerRunners(PlayerProtocol protocol) {
        for (CheckEnums.HackType hackType : CheckEnums.HackType.values()) {
            try {
                this.runners[hackType.ordinal()] = (CheckRunner) hackType.executor
                        .getConstructor(hackType.getClass(), PlayerProtocol.class)
                        .newInstance(hackType, protocol);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    // Separator

    public ItemStack getSkull() {
        if (this.skull == null) {
            if (this.offlinePlayer == null) {
                return InventoryUtils.getSkull(null, name, false);
            } else {
                return this.skull = InventoryUtils.getSkull(offlinePlayer, name, false);
            }
        } else {
            return this.skull;
        }
    }

    // Separator

    public MiningHistory getMiningHistory(MiningHistory.MiningOre ore) {
        return miningHistory[ore.ordinal()];
    }

}
