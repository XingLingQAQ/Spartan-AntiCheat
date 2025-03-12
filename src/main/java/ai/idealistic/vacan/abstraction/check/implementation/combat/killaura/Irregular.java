package ai.idealistic.vacan.abstraction.check.implementation.combat.killaura;

import ai.idealistic.vacan.abstraction.check.CheckDetection;
import ai.idealistic.vacan.abstraction.check.CheckRunner;
import ai.idealistic.vacan.functionality.server.MultiVersion;
import ai.idealistic.vacan.utils.minecraft.entity.PlayerUtils;
import ai.idealistic.vacan.utils.minecraft.server.PluginUtils;
import ai.idealistic.vacan.utils.minecraft.world.BlockUtils;
import org.bukkit.GameRule;
import org.bukkit.inventory.InventoryView;

public class Irregular extends CheckDetection {

    Irregular(CheckRunner executor) {
        super(
                executor,
                null,
                null,
                "irregular",
                true,
                1L,
                1L,
                1L,
                1L
        );
    }

    void run() {
        this.call(() -> {
            if (this.protocol.bukkit().isDead()) {
                if (!PluginUtils.contains("respawn")
                        && (!MultiVersion.isOrGreater(MultiVersion.MCVersion.V1_15)
                        || !(this.protocol.getWorld().getGameRuleValue(GameRule.DO_IMMEDIATE_RESPAWN) == true))) {
                    cancel("irregular, scenario: death");
                }
            } else if (this.protocol.bukkit().isSleeping()) {
                cancel("irregular, scenario: sleeping");
            } else {
                InventoryView inventoryView = this.protocol.bukkit().getOpenInventory();

                if (BlockUtils.hasMaterial(inventoryView.getCursor())) {
                    cancel("irregular, scenario: cursor");
                } else if (inventoryView.countSlots() > PlayerUtils.playerInventorySlots) {
                    cancel("irregular, scenario: inventory");
                }
            }
        });
    }

}
