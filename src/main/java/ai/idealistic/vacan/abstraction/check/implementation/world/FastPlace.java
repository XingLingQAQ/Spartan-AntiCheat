package ai.idealistic.vacan.abstraction.check.implementation.world;

import ai.idealistic.vacan.abstraction.check.CheckDetection;
import ai.idealistic.vacan.abstraction.check.CheckEnums.HackType;
import ai.idealistic.vacan.abstraction.check.CheckRunner;
import ai.idealistic.vacan.abstraction.check.definition.ImplementedDetection;
import ai.idealistic.vacan.abstraction.data.Buffer;
import ai.idealistic.vacan.abstraction.event.CBlockPlaceEvent;
import ai.idealistic.vacan.abstraction.protocol.PlayerProtocol;
import ai.idealistic.vacan.compatibility.Compatibility;
import ai.idealistic.vacan.compatibility.manual.abilities.ItemsAdder;
import ai.idealistic.vacan.compatibility.manual.building.PrinterMode;
import ai.idealistic.vacan.compatibility.manual.entity.Vehicles;
import ai.idealistic.vacan.functionality.server.MultiVersion;
import ai.idealistic.vacan.utils.minecraft.world.BlockUtils;
import org.bukkit.Material;

public class FastPlace extends CheckRunner {

    private final CheckDetection fast, medium, slow;
    private long time;
    private final Buffer.IndividualBuffer fastBuffer, mediumBuffer, slowBuffer;

    public FastPlace(HackType hackType, PlayerProtocol protocol) {
        super(hackType, protocol);
        this.fast = new ImplementedDetection(this, null, null, "fast", true);
        this.medium = new ImplementedDetection(this, null, null, "medium", true);
        this.slow = new ImplementedDetection(this, null, null, "slow", true);
        this.fastBuffer = new Buffer.IndividualBuffer();
        this.mediumBuffer = new Buffer.IndividualBuffer();
        this.slowBuffer = new Buffer.IndividualBuffer();
    }

    @Override
    protected void handleInternal(boolean cancelled, Object object) {
        if (object instanceof CBlockPlaceEvent) {
            CBlockPlaceEvent event = (CBlockPlaceEvent) object;

            if (!ItemsAdder.is(event.placedBlock)
                    && !BlockUtils.isScaffoldingBlock(event.placedBlock.getType())) {
                Material material = event.placedBlock.getType();

                if (material != Material.FIRE
                        && material != Material.AIR

                        && (!MultiVersion.isOrGreater(MultiVersion.MCVersion.V1_13)
                        || material != Material.CAVE_AIR
                        && material != Material.VOID_AIR)) {
                    long timePassed = System.currentTimeMillis() - time;
                    time = System.currentTimeMillis();
                    int ticks = 20;

                    if (timePassed <= 150L) {
                        this.slow.call(() -> {
                            if (slowBuffer.count(1, ticks) >= 9) {
                                this.slow.cancel(
                                        "slow"
                                                + ", ms: " + timePassed
                                                + ", block: " + BlockUtils.materialToString(material),
                                        null,
                                        ticks
                                );
                            }
                        });
                        if (timePassed <= 100L) {
                            this.medium.call(() -> {
                                if (mediumBuffer.count(1, ticks) >= 7) {
                                    this.medium.cancel(
                                            "medium"
                                                    + ", ms: " + timePassed
                                                    + ", block: " + BlockUtils.materialToString(material),
                                            null,
                                            ticks
                                    );
                                }
                            });
                            this.fast.call(() -> {
                                if (timePassed <= 50L
                                        && !this.protocol.isBedrockPlayer()
                                        && !Compatibility.CompatibilityType.VEIN_MINER.isFunctional()
                                        && fastBuffer.count(1, ticks) >= 5) {
                                    this.fast.cancel(
                                            "fast"
                                                    + ", ms: " + timePassed
                                                    + ", block: " + BlockUtils.materialToString(material),
                                            null,
                                            ticks
                                    );
                                }
                            });
                        }
                    }
                }
            }
        }
    }

    @Override
    protected boolean canRun() {
        return !this.protocol.isFlying()
                && !PrinterMode.isUsing(this.protocol)
                && !Vehicles.has(this.protocol, new String[]{Vehicles.DRILL, Vehicles.TRACTOR});
    }
}
