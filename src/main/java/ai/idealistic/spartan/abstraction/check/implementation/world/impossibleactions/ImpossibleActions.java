package ai.idealistic.spartan.abstraction.check.implementation.world.impossibleactions;

import ai.idealistic.spartan.abstraction.check.Check;
import ai.idealistic.spartan.abstraction.check.CheckDetection;
import ai.idealistic.spartan.abstraction.check.CheckEnums;
import ai.idealistic.spartan.abstraction.check.CheckRunner;
import ai.idealistic.spartan.abstraction.check.definition.ImplementedDetection;
import ai.idealistic.spartan.abstraction.event.CBlockPlaceEvent;
import ai.idealistic.spartan.abstraction.protocol.PlayerProtocol;
import ai.idealistic.spartan.abstraction.world.ServerLocation;
import ai.idealistic.spartan.compatibility.Compatibility;
import ai.idealistic.spartan.compatibility.manual.abilities.ItemsAdder;
import ai.idealistic.spartan.functionality.server.MultiVersion;
import ai.idealistic.spartan.functionality.server.TPS;
import ai.idealistic.spartan.utils.minecraft.world.BlockUtils;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ImpossibleActions extends CheckRunner {

    private final CheckDetection
            scaffoldDirection,
            scaffoldUp,
            scaffoldYaw,
            actions;
    private final Tower tower;
    private final ScaffoldAnalysis scaffoldAnalysis;
    private final GhostHand ghostHand;

    public ImpossibleActions(CheckEnums.HackType hackType, PlayerProtocol protocol) {
        super(hackType, protocol);
        this.scaffoldDirection = new ImplementedDetection(this, null, null, "scaffold_direction", true);
        this.scaffoldUp = new ImplementedDetection(this, Check.DataType.JAVA, null, "scaffold_up", true);
        this.scaffoldYaw = new ImplementedDetection(this, Check.DataType.JAVA, null, "scaffold_yaw", true);
        this.actions = new ImplementedDetection(this, null, null, "actions", true);
        this.ghostHand = new GhostHand(this);
        this.tower = new Tower(this);
        this.scaffoldAnalysis = new ScaffoldAnalysis(this);
    }

    @Override
    protected void handleInternal(boolean cancelled, Object object) {
        if (object instanceof CBlockPlaceEvent) {
            CBlockPlaceEvent event = (CBlockPlaceEvent) object;

            if (!ItemsAdder.is(event.placedBlock)) {
                Location playerLocation = this.protocol.getLocationOrVehicle();
                int playerY = playerLocation.getBlockY(),
                        blockY = event.placedBlock.getY();
                this.scaffoldAnalysis.run(event.placedBlock);

                if (blockY < playerY) {
                    BlockFace blockFace = event.placedAgainstBlock.getFace(event.placedBlock);

                    if (blockFace != null) {
                        switch (blockFace) {
                            case EAST:
                            case EAST_NORTH_EAST:
                            case EAST_SOUTH_EAST:
                            case WEST:
                            case WEST_NORTH_WEST:
                            case WEST_SOUTH_WEST:
                            case NORTH:
                            case NORTH_EAST:
                            case NORTH_NORTH_EAST:
                            case SOUTH:
                            case SOUTH_EAST:
                            case SOUTH_SOUTH_EAST:
                                if (this.scaffoldDirection.canCall()
                                        || this.scaffoldUp.canCall()
                                        || this.scaffoldYaw.canCall()) {
                                    double directionDistance = MultiVersion.isOrGreater(MultiVersion.MCVersion.V1_9)
                                            ? this.getDirection(blockFace).distance(playerLocation.getDirection())
                                            : Double.MAX_VALUE;

                                    if (directionDistance <= 1.25) {
                                        this.scaffoldDirection.call(() -> this.scaffoldDirection.cancel(
                                                "scaffold(direction)"
                                                        + ", vector-distance: " + directionDistance
                                                        + ", block-distance: " + ServerLocation.distance(playerLocation, event.placedAgainstBlock.getLocation())
                                        ));
                                    } else {
                                        float pitch = playerLocation.getPitch();

                                        if (pitch <= 0.0) {
                                            this.scaffoldUp.call(() -> this.scaffoldUp.cancel(
                                                    "scaffold(up)"
                                                            + ", pitch: " + pitch
                                                            + ", block-distance: " + ServerLocation.distance(playerLocation, event.placedAgainstBlock.getLocation())
                                            ));
                                        } else {
                                            this.scaffoldYaw.call(() -> {
                                                List<Map.Entry<Long, Location>> fromLocations = this.protocol.getPositionHistoryEntries();
                                                int requiredComparisons = 5, requiredSize = requiredComparisons + 1;

                                                if (fromLocations.size() >= requiredSize) { // 6-1 comparisons = 5
                                                    fromLocations = fromLocations.subList(fromLocations.size() - requiredSize, fromLocations.size());
                                                    Iterator<Map.Entry<Long, Location>> iterator = fromLocations.iterator();

                                                    if (!iterator.hasNext()) {
                                                        return;
                                                    }
                                                    Map.Entry<Long, Location> previousLocation = iterator.next();
                                                    double yawDeviation = 0.0;

                                                    while (iterator.hasNext()) {
                                                        Map.Entry<Long, Location> loopLocation = iterator.next();
                                                        long timePassed = System.currentTimeMillis() - loopLocation.getKey();

                                                        if (timePassed <= requiredComparisons * 2 * TPS.tickTime) {
                                                            double diff = (previousLocation.getValue().getYaw() - loopLocation.getValue().getYaw()) % 360.0f;
                                                            yawDeviation += diff * diff;
                                                            previousLocation = loopLocation;
                                                        } else {
                                                            return;
                                                        }
                                                    }
                                                    yawDeviation = Math.sqrt(yawDeviation / ((double) requiredComparisons));

                                                    if (yawDeviation >= 270.0) {
                                                        this.scaffoldYaw.cancel(
                                                                "scaffold(yaw)"
                                                                        + ", deviation: " + yawDeviation
                                                                        + ", block-distance: " + ServerLocation.distance(playerLocation, event.placedAgainstBlock.getLocation())
                                                        );
                                                    }
                                                }
                                            });
                                        }
                                    }
                                }
                                break;
                            case UP:
                                this.tower.run(event.placedBlock, playerLocation, playerY, blockY);
                                break;
                            default:
                                break;
                        }
                    }
                }
            }
        } else if (object instanceof BlockBreakEvent) {
            this.ghostHand.breaking((BlockBreakEvent) object);
        } else if (object instanceof PlayerInteractEvent) {
            PlayerInteractEvent event = (PlayerInteractEvent) object;
            this.ghostHand.interact(event);

            this.actions.call(() -> {
                Action action = event.getAction();

                if ((action == Action.LEFT_CLICK_AIR
                        || action == Action.LEFT_CLICK_BLOCK
                        || action == Action.RIGHT_CLICK_AIR
                        || action == Action.RIGHT_CLICK_BLOCK)
                        && !Compatibility.CompatibilityType.MC_MMO.isFunctional()) {
                    int cases;

                    if (this.protocol.bukkit().isSleeping()) {
                        cases = 1;
                    } else if (this.protocol.bukkit().isDead()) {
                        cases = 2;
                    } else if (BlockUtils.hasMaterial(this.protocol.bukkit().getOpenInventory().getCursor())) {
                        cases = 3;
                    } else {
                        cases = 0;
                    }

                    if (cases != 0) {
                        String action_string = action.toString().toLowerCase().replace("_", "-");
                        Block b = event.getClickedBlock();

                        if (b == null) {
                            this.actions.cancel("actions, action: " + action_string + ", case: " + cases);
                        } else if (!ItemsAdder.is(b)) {
                            this.actions.cancel("actions, action: " + action_string + ", case: " + cases + ", block: " + BlockUtils.blockToString(b));
                        }
                    }
                }
            });
        }
    }

    private Vector getDirection(BlockFace blockFace) {
        Vector direction = new Vector(blockFace.getModX(), blockFace.getModY(), blockFace.getModZ());

        if (blockFace.getModX() != 0 || blockFace.getModY() != 0 || blockFace.getModZ() != 0) {
            direction.normalize();
        }
        return direction;
    }

}
