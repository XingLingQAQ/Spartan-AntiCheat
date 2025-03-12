package ai.idealistic.vacan.compatibility.manual.building;

import ai.idealistic.vacan.abstraction.protocol.PlayerProtocol;
import ai.idealistic.vacan.compatibility.Compatibility;
import ai.idealistic.vacan.utils.minecraft.entity.PlayerUtils;

public class SuperPickaxe {

    public static boolean isUsing(PlayerProtocol p) {
        return Compatibility.CompatibilityType.SUPER_PICKAXE.isFunctional()
                && PlayerUtils.isPickaxeItem(p.getItemInHand().getType());
    }

}
