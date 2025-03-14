package ai.idealistic.vacan.abstraction.check.implementation.player;

import ai.idealistic.vacan.abstraction.check.CheckDetection;
import ai.idealistic.vacan.abstraction.check.CheckEnums;
import ai.idealistic.vacan.abstraction.check.CheckRunner;
import ai.idealistic.vacan.abstraction.check.definition.ImplementedDetection;
import ai.idealistic.vacan.abstraction.data.Buffer;
import ai.idealistic.vacan.abstraction.event.PlayerAttackEvent;
import ai.idealistic.vacan.abstraction.protocol.PlayerProtocol;
import ai.idealistic.vacan.compatibility.manual.abilities.AureliumSkills;
import ai.idealistic.vacan.compatibility.manual.abilities.ItemsAdder;
import ai.idealistic.vacan.compatibility.manual.building.MineBomb;
import ai.idealistic.vacan.compatibility.manual.building.TreeFeller;
import ai.idealistic.vacan.compatibility.manual.entity.Vehicles;
import ai.idealistic.vacan.functionality.server.TPS;
import ai.idealistic.vacan.utils.minecraft.entity.CombatUtils;
import ai.idealistic.vacan.utils.minecraft.world.BlockUtils;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class NoSwing extends CheckRunner {

    private final CheckDetection breaking, damage;
    private static final int refreshTime = 30 * 20;
    private long breakTime, animationTime;
    private final Buffer.IndividualBuffer damageBuffer;
    private long checkDamage;

    public NoSwing(CheckEnums.HackType hackType, PlayerProtocol protocol) {
        super(hackType, protocol);
        this.breakTime = Long.MAX_VALUE;
        this.animationTime = Long.MAX_VALUE;
        this.damageBuffer = new Buffer.IndividualBuffer();
        this.breaking = new ImplementedDetection(this, null, null, "breaking", true);
        this.damage = new ImplementedDetection(this, null, null, "damage", true);
    }

    @Override
    protected void handleInternal(boolean cancelled, Object object) {
        if (object instanceof PlayerInteractEvent) {
            PlayerInteractEvent e = (PlayerInteractEvent) object;

            if (e.getAction() == Action.LEFT_CLICK_BLOCK) {
                Block block = e.getClickedBlock();

                if (block == null
                        || !ItemsAdder.is(block)) {
                    this.breakTime = System.currentTimeMillis();
                }
            }
        } else if (object instanceof PlayerAnimationEvent) {
            this.animationTime = System.currentTimeMillis();
        } else if (object instanceof BlockBreakEvent) {
            this.breaking.call(() -> {
                if (MineBomb.isUsing(this.protocol)
                        || AureliumSkills.isUsing(this.protocol)) {
                    return;
                }
                BlockBreakEvent e = (BlockBreakEvent) object;
                Block block = e.getBlock();

                if (!BlockUtils.isSensitive(this.protocol, block.getType())
                        && !TreeFeller.canCancel(block)
                        && !ItemsAdder.is(block)
                        && System.currentTimeMillis() - breakTime > TPS.tickTime) {
                    if (this.animationTime == Long.MAX_VALUE) {
                        this.breaking.cancel(
                                "breaking"
                                        + ", block: " + BlockUtils.blockToString(block)
                                        + ", item: " + BlockUtils.materialToString(this.protocol.getItemInHand().getType()));

                        if (this.breaking.prevent()) {
                            e.setCancelled(true);
                        }
                    } else {
                        resetAnimationData();
                    }
                }
            });
        } else if (object instanceof PlayerAttackEvent) {
            this.damage.call(() -> {
                if (this.protocol.getAttackCooldown() != 1.0f) {
                    return;
                }
                LivingEntity entity = ((PlayerAttackEvent) object).target;

                if (!CombatUtils.canCheck(this.protocol, entity)) {
                    return;
                }
                long time = System.currentTimeMillis();

                if (checkDamage < time) {
                    checkDamage = time + (20L * TPS.tickTime);

                    if (this.animationTime == Long.MAX_VALUE) {
                        if (damageBuffer.count(1, refreshTime) >= 3) {
                            damageBuffer.reset();
                            this.damage.cancel(
                                    "damage"
                                            + ", entity: " + CombatUtils.entityToString(entity)
                                            + ", item: " + BlockUtils.materialToString(this.protocol.getItemInHand().getType())
                            );
                        }
                    } else {
                        resetAnimationData();
                    }
                }
            });
        }
    }

    private void resetAnimationData() {
        this.animationTime = Long.MAX_VALUE;
    }

    @Override
    public boolean canRun() {
        return !Vehicles.has(this.protocol, Vehicles.DRILL);
    }

}
