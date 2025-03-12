package ai.idealistic.vacan.abstraction.data;

import ai.idealistic.vacan.abstraction.protocol.PlayerProtocol;
import ai.idealistic.vacan.abstraction.world.ServerBlock;
import ai.idealistic.vacan.abstraction.world.ServerLocation;
import ai.idealistic.vacan.functionality.server.MultiVersion;
import ai.idealistic.vacan.utils.math.AlgebraUtils;
import ai.idealistic.vacan.utils.minecraft.inventory.MaterialUtils;
import ai.idealistic.vacan.utils.minecraft.world.BlockUtils;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.bukkit.Location;
import org.bukkit.Material;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Data
@RequiredArgsConstructor
public class EnvironmentData {

    private static final Material MAGMA_BLOCK = MaterialUtils.get("magma");

    private boolean ice = false;
    private boolean slime = false;
    private boolean slimeWide = false;
    private boolean slimeHeight = false;
    private boolean liquid = false;
    private boolean semi = false;
    private boolean glide = false;
    private boolean jumpModify = false;
    private boolean climb = false;
    private boolean bubble = false;

    public EnvironmentData(PlayerProtocol protocol) {
        Location location = (protocol.getVehicle() == null) ?
                protocol.getLocation()
                : protocol.getVehicle().getLocation();
        double x = location.getX();
        double y = location.getY() - 0.1;
        double z = location.getZ();

        for (int dx = -2; dx <= 2; ++dx) {
            for (int dy = -2; dy <= 2; ++dy) {
                for (int dz = -2; dz <= 2; ++dz) {
                    boolean h = Math.abs(dx) < 2 && Math.abs(dz) < 2;
                    ServerBlock b = new ServerLocation(
                            new Location(protocol.getWorld(), x + (double) dx * 0.3, y + (double) dy * 0.5, z + (double) dz * 0.3)
                    ).getBlock();
                    Material material = protocol.packetWorld.getBlock(
                            new Location(protocol.getWorld(), x + (double) dx * 0.3, y + (double) dy * 0.5, z + (double) dz * 0.3)
                    );
                    Material materialWide = protocol.packetWorld.getBlock(
                            new Location(protocol.getWorld(), x + (double) dx * 0.5, y + (double) (dy * 0.5) - 0.3, z + (double) dz * 0.5)
                    );
                    Material materialTop = protocol.packetWorld.getBlock(
                            new Location(protocol.getWorld(), x + (double) dx * 0.5, y + (double) (dy * 0.5) + 1.0, z + (double) dz * 0.5)
                    );
                    Material materialFrom = protocol.packetWorld.getBlock(
                            new Location(protocol.getWorld(), x + (double) dx * 0.3, y + (double) dy * 0.5, z + (double) dz * 0.3)
                    );
                    Material m2 = b.getType();
                    if (material == null || materialFrom == null || m2 == null) return;

                    if ((BlockUtils.areHoneyBlocks(material)
                            || BlockUtils.areBeds(material)
                            || BlockUtils.areSlimeBlocks(material))) {
                        this.slime = true;
                    }
                    if (Math.abs(dx) < 2
                            && Math.abs(dz) < 2
                            && (BlockUtils.areInteractiveBushes(material)
                            || BlockUtils.areBeds(material)
                            || BlockUtils.isPowderSnow(material))) {
                        this.jumpModify = true;
                    }
                    if (BlockUtils.canClimb(material, false) || BlockUtils.canClimb(materialTop, false)) {
                        this.climb = true;
                    }
                    if (Math.abs(dy) < 2 && h
                            && (BlockUtils.isPowderSnow(material)
                            || (BlockUtils.isPowderSnow(materialTop)
                            || BlockUtils.areHoneyBlocks(material)
                            || BlockUtils.areWebs(material) || BlockUtils.areWebs(materialTop)
                            || BlockUtils.areInteractiveBushes(material)
                            || BlockUtils.areInteractiveBushes(materialTop)))) {
                        this.glide = true;
                    }
                    if (!BlockUtils.canClimb(material, false) &&
                            (BlockUtils.isSemiSolid(material)
                                    || BlockUtils.isSemiSolid(materialFrom)
                                    || BlockUtils.isSemiSolid(materialWide)
                                    || BlockUtils.areWalls(materialFrom)
                                    || BlockUtils.areCobbleWalls(materialFrom)
                                    || BlockUtils.areSlimeBlocks(material)
                                    || BlockUtils.areHoneyBlocks(material)
                                    || BlockUtils.areWebs(material)
                                    || BlockUtils.areWebs(materialTop))) {
                        this.semi = true;
                    }
                    if (MultiVersion.isOrGreater(MultiVersion.MCVersion.V1_13)
                            && (material.equals(Material.BUBBLE_COLUMN) || m2.equals(Material.BUBBLE_COLUMN))) {
                        this.bubble = true;
                    }
                    if (BlockUtils.isLiquidOrWaterLogged(b, true)
                            || BlockUtils.isLiquid(protocol.packetWorld.getBlock(
                            new Location(protocol.getWorld(), x + (double) dx * 0.3, y, z + (double) dz * 0.3)
                    ))
                            || BlockUtils.isLiquid(material)
                            || BlockUtils.isLiquid(m2)
                            || BlockUtils.isWaterLogged(b)) {

                        this.liquid = true;

                        for (double i = 0.0; i < Math.ceil(protocol.getEyeHeight()); i++) {
                            boolean broke = false;

                            for (ServerLocation locationModified : new ServerLocation(location).getSurroundingLocations(0.3, i, 0.3)) {
                                if (calculateBubbleWater(protocol, locationModified)) {
                                    broke = true;
                                    break;
                                }
                            }

                            if (broke) {
                                break;
                            }
                        }
                    }
                    if (BlockUtils.areIceBlocks(material)) {
                        this.ice = true;
                    }
                    {
                        double xF = protocol.getFromLocation().getX();
                        double yF = protocol.getFromLocation().getY();
                        double zF = protocol.getFromLocation().getZ();
                        Material materialBig = protocol.packetWorld.getBlock(
                                new Location(protocol.getWorld(), x + (double) dx,
                                        y + (double) dy, z + (double) dz)
                        );
                        Material materialBig2 = protocol.packetWorld.getBlock(
                                new Location(protocol.getWorld(),
                                        xF + (double) dx, yF + (double) dy - 1, zF + (double) dz)
                        );
                        Material materialBigFrom = protocol.packetWorld.getBlock(
                                new Location(protocol.getWorld(),
                                        xF + (double) dx, yF + (double) dy + 0.5, zF + (double) dz)
                        );
                        if (materialBig == null) return;
                        if ((BlockUtils.areHoneyBlocks(materialBig)
                                || BlockUtils.areBeds(materialBig)
                                || BlockUtils.areSlimeBlocks(materialBigFrom)
                                || BlockUtils.areSlimeBlocks(materialBig2)
                                || BlockUtils.areSlimeBlocks(materialBig))) {
                            protocol.slime0Tick = 3;
                        }
                        if (protocol.slime0Tick > 0) {
                            protocol.slime0Tick--;
                            this.slimeHeight = true;
                            this.slimeWide = true;
                            this.slime = true;
                        }
                    }
                }
            }
        }
    }

    private boolean calculateBubbleWater(PlayerProtocol protocol, ServerLocation location) {
        if (MultiVersion.isOrGreater(MultiVersion.MCVersion.V1_13)) {
            int blockY = location.getBlockY(), minY = BlockUtils.getMinHeight(protocol.getWorld());

            if (blockY > minY) {
                ServerLocation locationModified = location.clone();
                int max = (blockY - minY),
                        playerHeight = AlgebraUtils.integerCeil(protocol.getEyeHeight());
                Set<Integer> emptyNonLiquid = new HashSet<>(max),
                        fullNonLiquid = new HashSet<>(max);

                for (int i = 0; i <= max; i++) {
                    Collection<ServerLocation> locations = locationModified.clone().add(0, -i, 0).getSurroundingLocations(0.3, 0, 0.3);

                    for (ServerLocation loc : locations) {
                        ServerBlock block = loc.getBlock();
                        Material type = block.getType();

                        if (type == Material.SOUL_SAND) {
                            protocol.soulSandWater = System.currentTimeMillis();
                            protocol.lastVelocity = System.currentTimeMillis();
                            return true;
                        } else if (type == MAGMA_BLOCK) {
                            protocol.magmaCubeWater = System.currentTimeMillis();
                            protocol.lastVelocity = System.currentTimeMillis();
                            return true;
                        } else if (BlockUtils.isSolid(type)) {
                            if (!block.isWaterLogged()) {
                                fullNonLiquid.add(i);

                                if (fullNonLiquid.size() == playerHeight) {
                                    return false;
                                }
                            }
                        } else if (!block.isWaterLogged()) {
                            emptyNonLiquid.add(i);

                            if (emptyNonLiquid.size() == 8) {
                                return false;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    public boolean isAllFalse() {
        return !(isIce() || isSemi() || isSlime() || isSlimeHeight() || isLiquid() || isJumpModify() || isBubble());
    }
}
