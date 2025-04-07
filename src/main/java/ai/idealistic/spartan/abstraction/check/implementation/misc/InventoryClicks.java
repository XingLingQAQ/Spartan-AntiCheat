package ai.idealistic.spartan.abstraction.check.implementation.misc;

import ai.idealistic.spartan.abstraction.check.Check;
import ai.idealistic.spartan.abstraction.check.CheckDetection;
import ai.idealistic.spartan.abstraction.check.CheckEnums;
import ai.idealistic.spartan.abstraction.check.CheckRunner;
import ai.idealistic.spartan.abstraction.check.definition.ImplementedDetection;
import ai.idealistic.spartan.abstraction.data.Buffer;
import ai.idealistic.spartan.abstraction.protocol.PlayerProtocol;
import ai.idealistic.spartan.utils.minecraft.entity.PlayerUtils;
import ai.idealistic.spartan.utils.minecraft.world.BlockUtils;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class InventoryClicks extends CheckRunner {

    private final CheckDetection
            shift,
            slow,
            medium,
            fast;
    private long time;
    private int oldHash;
    private final Buffer.IndividualBuffer
            slowBuffer,
            mediumBuffer,
            fastBuffer,
            shiftBuffer;

    public InventoryClicks(CheckEnums.HackType hackType, PlayerProtocol protocol) {
        super(hackType, protocol);
        this.shift = new ImplementedDetection(this, Check.DataType.JAVA, null, "shift", true);
        this.slow = new ImplementedDetection(this, Check.DataType.JAVA, null, "slow", true);
        this.medium = new ImplementedDetection(this, Check.DataType.JAVA, null, "medium", true);
        this.fast = new ImplementedDetection(this, Check.DataType.JAVA, null, "fast", true);
        this.slowBuffer = new Buffer.IndividualBuffer();
        this.mediumBuffer = new Buffer.IndividualBuffer();
        this.fastBuffer = new Buffer.IndividualBuffer();
        this.shiftBuffer = new Buffer.IndividualBuffer();
    }

    @Override
    protected void handleInternal(boolean cancelled, Object object) {
        if (object instanceof InventoryClickEvent) {
            if (this.shift.canCall()
                    || this.slow.canCall()
                    || this.medium.canCall()
                    || this.fast.canCall()) {
                InventoryClickEvent event = (InventoryClickEvent) object;
                ItemStack item = event.getCurrentItem();
                ClickType click = event.getClick();

                if (!click.isCreativeAction()
                        && !click.isKeyboardClick()
                        && item != null
                        && item.getItemMeta() != null) {
                    Material material = item.getType();

                    if (!BlockUtils.isSensitive(this.protocol, material)) {
                        boolean quick = this.protocol.bukkit().getOpenInventory().countSlots() <= PlayerUtils.playerInventorySlots,
                                shift = click.isShiftClick();
                        String type = BlockUtils.materialToString(material);

                        int newHash = type.hashCode(),
                                oldHash = this.oldHash;
                        this.oldHash = newHash;

                        if (shift
                                && !quick
                                && !BlockUtils.hasMaterial(this.protocol.bukkit().getOpenInventory().getCursor())
                                && shiftBuffer.count(1, 5) == 4) { // Inventory
                            shiftBuffer.reset();
                            this.shift.call(() -> this.shift.cancel("shift, item: " + type));
                        } else {
                            long timePassed = System.currentTimeMillis() - time;
                            time = System.currentTimeMillis();

                            if ((!shift && !quick || newHash != oldHash)) {
                                if (timePassed < 150L) {
                                    this.slow.call(() -> {
                                        if (slowBuffer.count(1, 10) >= 8) {
                                            this.slow.cancel("slow, ms: " + timePassed + ", item: " + type);
                                        }
                                    });
                                    if (timePassed < 100L) {
                                        this.medium.call(() -> {
                                            if (mediumBuffer.count(1, 5) >= 4) {
                                                this.medium.cancel("medium, ms: " + timePassed + ", item: " + type);
                                            }
                                        });
                                        this.fast.call(() -> {
                                            if (timePassed < 50L && fastBuffer.count(1, 5) >= 4) {
                                                this.fast.cancel("fast, ms: " + timePassed + ", item: " + type);
                                            }
                                        });
                                    }
                                }
                            }
                        }
                    }
                }

                if (this.prevent()) {
                    event.setCancelled(true);
                }
            }
        }
    }

}
