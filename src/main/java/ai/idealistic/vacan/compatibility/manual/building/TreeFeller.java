package ai.idealistic.vacan.compatibility.manual.building;

import ai.idealistic.vacan.compatibility.Compatibility;
import ai.idealistic.vacan.utils.minecraft.world.BlockUtils;
import org.bukkit.block.Block;

public class TreeFeller {

    public static boolean canCancel(Block b) {
        return Compatibility.CompatibilityType.TREE_FELLER.isFunctional()
                && BlockUtils.areWoods(b.getType());
    }
}
