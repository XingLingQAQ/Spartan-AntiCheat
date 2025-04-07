package ai.idealistic.spartan.utils.minecraft.world;

import ai.idealistic.spartan.abstraction.world.ServerLocation;
import org.bukkit.Location;
import org.bukkit.Material;

import java.util.Set;

public class BlockPatternUtils {

    public static boolean isBlockPattern(double[][] positions,
                                         ServerLocation location,
                                         boolean surroundings,
                                         Material match) {
        if (match != null) {
            for (double[] coordinates : positions) {
                if (surroundings) {
                    for (ServerLocation surroundingLocation : location.getSurroundingLocations(coordinates[0], coordinates[1], coordinates[2])) {
                        Material material = surroundingLocation.getBlock().getTypeOrNull();

                        if (material != null && match == material) {
                            return true;
                        }
                    }
                } else {
                    Material material = location.clone().add(coordinates[0], coordinates[1], coordinates[2]).getBlock().getTypeOrNull();

                    if (material != null && match == material) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static boolean isBlockPattern(double[][] positions,
                                         Location location,
                                         boolean surroundings,
                                         Material match) {
        return isBlockPattern(positions, new ServerLocation(location), surroundings, match);
    }

    // Separator

    @SafeVarargs
    public static boolean isBlockPattern(double[][] positions,
                                         ServerLocation location,
                                         boolean surroundings,
                                         Set<Material>... sets) {
        for (double[] coordinates : positions) {
            if (surroundings) {
                for (ServerLocation surroundingLocation : location.getSurroundingLocations(coordinates[0], coordinates[1], coordinates[2])) {
                    Material material = surroundingLocation.getBlock().getTypeOrNull();

                    if (material != null) {
                        for (Set<Material> set : sets) {
                            if (set.contains(material)) {
                                return true;
                            }
                        }
                    }
                }
            } else {
                Material material = location.clone().add(coordinates[0], coordinates[1], coordinates[2]).getBlock().getTypeOrNull();

                if (material != null) {
                    for (Set<Material> set : sets) {
                        if (set.contains(material)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    @SafeVarargs
    public static boolean isBlockPattern(double[][] positions,
                                         Location location,
                                         boolean surroundings,
                                         Set<Material>... sets) {
        return isBlockPattern(positions, new ServerLocation(location), surroundings, sets);
    }

    // Separator

    @SafeVarargs
    public static boolean isBlockPattern(double[] coordinates,
                                         ServerLocation location,
                                         boolean surroundings,
                                         Set<Material>... sets) {
        double x = coordinates[0], y = coordinates[1], z = coordinates[2];

        if (surroundings) {
            for (ServerLocation surroundingLocation : location.getSurroundingLocations(x, y, z)) {
                Material material = surroundingLocation.getBlock().getTypeOrNull();

                if (material != null) {
                    for (Set<Material> set : sets) {
                        if (set.contains(material)) {
                            return true;
                        }
                    }
                }
            }
        } else {
            Material material = location.clone().add(x, y, z).getBlock().getTypeOrNull();

            if (material != null) {
                for (Set<Material> set : sets) {
                    if (set.contains(material)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @SafeVarargs
    public static boolean isBlockPattern(double[] coordinates,
                                         Location location,
                                         boolean surroundings,
                                         Set<Material>... sets) {
        return isBlockPattern(coordinates, new ServerLocation(location), surroundings, sets);
    }

}
