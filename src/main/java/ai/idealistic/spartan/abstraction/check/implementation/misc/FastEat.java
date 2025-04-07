package ai.idealistic.spartan.abstraction.check.implementation.misc;

import ai.idealistic.spartan.abstraction.check.CheckDetection;
import ai.idealistic.spartan.abstraction.check.CheckEnums.HackType;
import ai.idealistic.spartan.abstraction.check.CheckRunner;
import ai.idealistic.spartan.abstraction.check.definition.ImplementedDetection;
import ai.idealistic.spartan.abstraction.protocol.PlayerProtocol;
import ai.idealistic.spartan.compatibility.manual.vanilla.Attributes;
import ai.idealistic.spartan.functionality.server.MultiVersion;
import ai.idealistic.spartan.utils.minecraft.inventory.MaterialUtils;
import org.bukkit.Difficulty;
import org.bukkit.Material;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffectType;

public class FastEat extends CheckRunner {

    private final CheckDetection interact, eat;
    private static final Material cake = MaterialUtils.get("cake");
    private static final long
            delay = 1_000L,
            driedKelp = 550L;

    private long delayTime, eatTime;

    public FastEat(HackType hackType, PlayerProtocol protocol) {
        super(hackType, protocol);
        this.interact = new ImplementedDetection(this, null, null, "interact", true);
        this.eat = new ImplementedDetection(this, null, null, "eat", true);
    }

    private long getDelay() {
        if (MultiVersion.isOrGreater(MultiVersion.MCVersion.V1_9)) {
            PlayerInventory inventory = this.protocol.getInventory();

            for (ItemStack itemStack : new ItemStack[]{inventory.getItemInHand(), inventory.getItemInOffHand()}) {
                if (itemStack != null) {
                    Material type = itemStack.getType();

                    if (MultiVersion.isOrGreater(MultiVersion.MCVersion.V1_13) && type == Material.DRIED_KELP) {
                        return driedKelp;
                    }
                    if (type.isEdible()) {
                        return delay;
                    }
                }
            }
        } else {
            ItemStack itemStack = this.protocol.getItemInHand();

            if (itemStack != null) {
                Material type = itemStack.getType();

                if (type.isEdible()) {
                    return delay;
                }
            }
        }
        return 0L;
    }

    private void checkDelay() {
        this.eat.call(() -> {
            long time = System.currentTimeMillis() - delayTime;
            this.delayTime = System.currentTimeMillis();
            long delay = getDelay();

            if (delay > 0L && time <= delay) {
                this.eat.cancel(
                        "eat, ms: " + time
                );
            }
        });
    }

    private void checkEating() {
        this.interact.call(() -> {
            long time = System.currentTimeMillis() - eatTime,
                    delay = getDelay();

            if (delay > 0L && time <= delay) {
                this.interact.cancel(
                        "interact, ms: " + time + ", delay: " + delay
                );
            }
        });
    }

    @Override
    protected void handleInternal(boolean cancelled, Object object) {
        if (object instanceof FoodLevelChangeEvent) {
            FoodLevelChangeEvent e = (FoodLevelChangeEvent) object;
            int lvl = e.getFoodLevel();

            if (lvl > this.protocol.bukkit().getFoodLevel()) {
                checkDelay();
                checkEating();

                if (this.prevent()) {
                    e.setCancelled(true);
                }
            }
        } else if (object instanceof PlayerInteractEvent) {
            PlayerInteractEvent e = (PlayerInteractEvent) object;

            switch (e.getAction()) {
                case RIGHT_CLICK_BLOCK:
                    if (e.getClickedBlock().getType() == cake) {
                        this.protocol.getRunner(hackType).addDisableCause(null, null, 10);
                    }
                    break;
                case RIGHT_CLICK_AIR:
                    if (this.protocol.getItemInHand().getType().isEdible()) {
                        this.eatTime = System.currentTimeMillis();
                    }
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    protected boolean canRun() {
        return this.protocol.getWorld().getDifficulty() != Difficulty.PEACEFUL
                && !this.protocol.hasPotionEffect(PotionEffectType.SATURATION, 0)
                && Attributes.getAmount(this.protocol, Attributes.GENERIC_MAX_ABSORPTION) == Double.MIN_VALUE;
    }
}
