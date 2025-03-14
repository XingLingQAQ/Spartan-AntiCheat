package ai.idealistic.vacan.abstraction.check.implementation.combat;

import ai.idealistic.vacan.abstraction.check.CheckDetection;
import ai.idealistic.vacan.abstraction.check.CheckEnums.HackType;
import ai.idealistic.vacan.abstraction.check.CheckRunner;
import ai.idealistic.vacan.abstraction.check.definition.ImplementedDetection;
import ai.idealistic.vacan.abstraction.data.Buffer;
import ai.idealistic.vacan.abstraction.protocol.PlayerProtocol;
import org.bukkit.Material;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.PlayerInventory;

public class FastBow extends CheckRunner {

    private final CheckDetection
            bowForceDetection,
            bowShotsDetection;
    private long interactTime;
    private final Buffer.IndividualBuffer bowForce, bowShots;

    public FastBow(HackType hackType, PlayerProtocol protocol) {
        super(hackType, protocol);
        this.bowForceDetection = new ImplementedDetection(this, null, null, "bow_force", true,
                CheckDetection.DEFAULT_AVERAGE_TIME * 2L,
                CheckDetection.TIME_TO_NOTIFY,
                CheckDetection.TIME_TO_PREVENT,
                CheckDetection.TIME_TO_PUNISH);
        this.bowShotsDetection = new ImplementedDetection(this, null, null, "bow_shots", true,
                CheckDetection.DEFAULT_AVERAGE_TIME * 2L,
                CheckDetection.TIME_TO_NOTIFY,
                CheckDetection.TIME_TO_PREVENT,
                CheckDetection.TIME_TO_PUNISH);
        this.bowForce = new Buffer.IndividualBuffer();
        this.bowShots = new Buffer.IndividualBuffer();
    }

    @Override
    protected void handleInternal(boolean cancelled, Object object) {
        if (object instanceof EntityShootBowEvent) {
            EntityShootBowEvent event = (EntityShootBowEvent) object;

            if (this.protocol.getItemInHand().getType() == Material.BOW) {
                checkBowShots(event);
                checkBowForce(event);
            }
        } else if (object instanceof PlayerInteractEvent) {
            PlayerInteractEvent event = (PlayerInteractEvent) object;

            if (event.getAction() == Action.RIGHT_CLICK_AIR) {
                PlayerInventory inventory = this.protocol.bukkit().getInventory();

                if (inventory.getItemInHand().getType() == Material.BOW
                        && inventory.contains(Material.ARROW)) {
                    this.interactTime = System.currentTimeMillis();
                }
            }
        }
    }

    private void checkBowForce(EntityShootBowEvent event) {
        this.bowForceDetection.call(() -> {
            if (event.getForce() != 1.0f) {
                return;
            }
            long timePassed = System.currentTimeMillis() - interactTime;

            if (timePassed > 500L) {
                return;
            }
            int buffer = bowForce.count(1, 20),
                    threshold = 2;

            if (buffer >= threshold) {
                bowForce.reset();
                this.bowForceDetection.cancel(
                        "bow-force, ms: " + timePassed
                );

                if (this.prevent()) {
                    event.setCancelled(true);
                }
            }
        });
    }

    private void checkBowShots(EntityShootBowEvent event) {
        this.bowShotsDetection.call(() -> {
            int buffer = bowShots.count(1, 10),
                    threshold = 7;
            if (buffer >= threshold) {
                this.bowShotsDetection.cancel(
                        "bow-shots"
                );

                if (this.prevent()) {
                    event.setCancelled(true);
                }
            }
        });
    }

}
