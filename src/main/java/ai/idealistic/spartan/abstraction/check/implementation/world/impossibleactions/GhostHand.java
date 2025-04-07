package ai.idealistic.spartan.abstraction.check.implementation.world.impossibleactions;

import ai.idealistic.spartan.abstraction.check.CheckDetection;
import ai.idealistic.spartan.abstraction.check.CheckRunner;
import ai.idealistic.spartan.abstraction.check.definition.ImplementedDetection;
import ai.idealistic.spartan.abstraction.world.ServerLocation;
import ai.idealistic.spartan.compatibility.Compatibility;
import ai.idealistic.spartan.compatibility.manual.abilities.ItemsAdder;
import ai.idealistic.spartan.compatibility.manual.building.PrinterMode;
import ai.idealistic.spartan.compatibility.manual.building.TreeFeller;
import ai.idealistic.spartan.compatibility.manual.entity.Vehicles;
import ai.idealistic.spartan.compatibility.manual.vanilla.Attributes;
import ai.idealistic.spartan.utils.minecraft.entity.PlayerUtils;
import ai.idealistic.spartan.utils.minecraft.inventory.MaterialUtils;
import ai.idealistic.spartan.utils.minecraft.world.BlockUtils;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class GhostHand extends CheckDetection {

    private final CheckDetection breaking, interact;

    public GhostHand(CheckRunner executor) {
        super(executor, null, null, null, null);
        this.breaking = new ImplementedDetection(executor, null, null, "breaking", true);
        this.interact = new ImplementedDetection(executor, null, null, "interact", true);
    }

    void breaking(BlockBreakEvent event) {
        this.breaking.call(() -> {
            if (!Compatibility.CompatibilityType.ADVANCED_ENCHANTMENTS.isFunctional()) {
                Block b = event.getBlock();

                if (canDo(b)
                        && !ItemsAdder.is(b)) {
                    long breakTime = MaterialUtils.getBlockBreakTime(this.protocol, this.protocol.getItemInHand(), b.getType());

                    if (breakTime <= 500L) {
                        return;
                    }
                    Location location = this.protocol.getLocationOrVehicle().clone().add(
                            0,
                            this.protocol.getEyeHeight(),
                            0
                    );
                    double bDistance = ServerLocation.distance(location, b.getLocation());
                    ServerLocation t = this.protocol.getTargetBlock(bDistance, PlayerUtils.chunk);

                    if (t != null) {
                        double tDistance = t.distance(location);

                        if (tDistance < bDistance) {
                            this.breaking.cancel(
                                    "breaking"
                                            + ", interacted-block: " + BlockUtils.blockToString(b)
                                            + ", blocking-block: " + BlockUtils.blockToString(t.getBlock())
                                            + ", interacted-distance: " + bDistance
                                            + ", blocking-distance: " + tDistance,
                                    null,
                                    1
                            );

                            if (this.breaking.prevent()) {
                                event.setCancelled(true);
                            }
                        }
                    }
                }
            }
        });
    }

    void interact(PlayerInteractEvent event) {
        this.interact.call(() -> {
            if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                Block b = event.getClickedBlock();

                if (b != null
                        && canDo(b)
                        && BlockUtils.isChangeable(b.getType())) {
                    Location location = this.protocol.getLocationOrVehicle().clone().add(
                            0,
                            this.protocol.getEyeHeight(),
                            0
                    );
                    double bDistance = ServerLocation.distance(location, b.getLocation());
                    ServerLocation t = this.protocol.getTargetBlock(bDistance, PlayerUtils.chunk);

                    if (t != null) {
                        double tDistance = t.distance(location);

                        if (tDistance < bDistance) {
                            this.interact.cancel(
                                    "interact"
                                            + ", interacted-block: " + BlockUtils.blockToString(b)
                                            + ", blocking-block: " + BlockUtils.blockToString(t.getBlock())
                                            + ", interacted-distance: " + bDistance
                                            + ", blocking-distance: " + tDistance,
                                    null,
                                    1
                            );

                            if (this.interact.prevent()) {
                                event.setCancelled(true);
                            }
                        }
                    }
                }
            }
        });
    }

    private boolean canDo(Block b) {
        return b.getWorld() == this.protocol.getWorld()
                && !TreeFeller.canCancel(b);
    }

    private boolean canRun() {
        if (!this.protocol.isFlying()
                && !Compatibility.CompatibilityType.MC_MMO.isFunctional()
                && !Vehicles.has(this.protocol, Vehicles.DRILL)
                && !PrinterMode.isUsing(this.protocol)
                && Attributes.getAmount(this.protocol, Attributes.PLAYER_BLOCK_INTERACTION_RANGE) == Double.MIN_VALUE) {
            GameMode gameMode = this.protocol.getGameMode();
            return gameMode == GameMode.SURVIVAL
                    || gameMode == GameMode.ADVENTURE;
        }
        return false;
    }

}
