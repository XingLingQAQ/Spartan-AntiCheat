package com.vagdedes.spartan.abstraction.player;

import com.vagdedes.spartan.abstraction.data.Trackers;
import com.vagdedes.spartan.abstraction.world.SpartanLocation;
import com.vagdedes.spartan.functionality.server.MultiVersion;
import com.vagdedes.spartan.functionality.server.TPS;
import com.vagdedes.spartan.utils.math.AlgebraUtils;
import com.vagdedes.spartan.utils.minecraft.entity.CombatUtils;
import com.vagdedes.spartan.utils.minecraft.inventory.EnchantmentUtils;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;

public class SpartanPlayerDamage {

    private static final EntityDamageEvent.DamageCause WORLD_BORDER = CombatUtils.findDamageCause("WORLD_BORDER");

    // Separator

    private final SpartanPlayer parent;
    public final long time;
    public final EntityDamageEvent event;
    public final SpartanLocation location;
    private final ItemStack activeItem;
    public final boolean boss, explosive;

    SpartanPlayerDamage(SpartanPlayer player) {
        this.parent = player;
        this.time = 0L;
        this.event = null;
        this.location = player.movement.getLocation();
        this.activeItem = null;
        this.boss = false;
        this.explosive = false;
    }

    SpartanPlayerDamage(SpartanPlayer player, EntityDamageEvent event) {
        boolean abstractVelocity = false;
        player.trackers.add(Trackers.TrackerType.DAMAGE, (int) TPS.maximum);
        this.parent = player;
        this.location = player.movement.getLocation();
        this.time = System.currentTimeMillis();
        this.event = event;

        if (event instanceof EntityDamageByEntityEvent) {
            EntityDamageByEntityEvent actualEvent = (EntityDamageByEntityEvent) this.event;

            if (actualEvent.getDamager() instanceof Player) {
                this.activeItem = ((Player) actualEvent.getDamager()).getInventory().getItemInHand();
                this.boss = false;
                this.explosive = false;
            } else if (actualEvent.getDamager() instanceof LivingEntity) {
                this.activeItem = ((LivingEntity) actualEvent.getDamager()).getEquipment().getItemInHand();
                this.boss = MultiVersion.isOrGreater(MultiVersion.MCVersion.V1_14)
                        && actualEvent.getDamager() instanceof Boss;
                this.explosive = false;
            } else if (actualEvent.getDamager() instanceof Projectile) {
                Projectile projectile = (Projectile) actualEvent.getDamager();

                if (projectile.getShooter() instanceof LivingEntity) {
                    LivingEntity shooter = (LivingEntity) projectile.getShooter();
                    this.activeItem = shooter.getEquipment().getItemInHand();
                    this.boss = MultiVersion.isOrGreater(MultiVersion.MCVersion.V1_14)
                            && shooter instanceof Boss;

                    if (this.activeItem != null
                            && (this.activeItem.getType() == Material.BOW
                            || MultiVersion.isOrGreater(MultiVersion.MCVersion.V1_14)
                            && this.activeItem.getType() == Material.CROSSBOW)) {
                        int level = this.activeItem.getEnchantmentLevel(EnchantmentUtils.ARROW_KNOCKBACK);

                        if (level > 2) {
                            this.parent.trackers.add(
                                    Trackers.TrackerType.ABSTRACT_VELOCITY,
                                    AlgebraUtils.integerRound(Math.log(level) * TPS.maximum)
                            );
                            abstractVelocity = true;
                        }
                    }
                } else {
                    this.activeItem = null;
                    this.boss = false;
                }
                this.explosive = false;
            } else if (actualEvent.getDamager() instanceof Explosive) {
                this.activeItem = null;
                this.boss = false;
                this.explosive = true;
            } else {
                this.activeItem = null;
                this.boss = false;
                this.explosive = false;
            }
        } else {
            this.activeItem = null;
            this.boss = false;
            this.explosive = false;
        }

        if (this.activeItem != null) {
            int level = this.activeItem.getEnchantmentLevel(Enchantment.KNOCKBACK);

            if (level > 2) {
                this.parent.trackers.add(
                        Trackers.TrackerType.ABSTRACT_VELOCITY,
                        AlgebraUtils.integerRound(Math.log(level) * TPS.maximum)
                );
                abstractVelocity = true;
            }
        }

        if (!abstractVelocity && !event.isCancelled()) {
            player.trackers.disable(Trackers.TrackerType.ABSTRACT_VELOCITY, 2);
        }
    }

    public EntityDamageByEntityEvent getEntityDamageByEntityEvent() {
        return this.event != null && this.event instanceof EntityDamageByEntityEvent
                ? (EntityDamageByEntityEvent) this.event
                : null;
    }

    public long timePassed() {
        return System.currentTimeMillis() - this.time;
    }

    public ItemStack getActiveItem() {
        return this.activeItem == null
                ? new ItemStack(Material.AIR)
                : this.activeItem;
    }

    public boolean isPropellingProjectile() {
        if (this.event != null
                && !this.event.isCancelled()
                && this.timePassed() <= TPS.maximum
                && this.parent.movement.getTicksOnAir() <= TPS.maximum
                && this.location.getPitch() <= -60.0f
                && this.event instanceof EntityDamageByEntityEvent) {
            EntityDamageByEntityEvent event = (EntityDamageByEntityEvent) this.event;

            if (event.getCause() == EntityDamageEvent.DamageCause.PROJECTILE) {
                Player p = this.parent.getInstance();
                return p != null && event.getDamager().equals(p);
            }
        }
        return false;
    }

    public boolean hasBigKnockBack() {
        if (this.event == null || this.event.isCancelled()) {
            return false;
        } else {
            int time = AlgebraUtils.integerRound(TPS.maximum * 2.0);
            return this.timePassed() <= time
                    && this.parent.movement.getTicksOnAir() <= time
                    && (this.boss
                    || this.explosive
                    || this.activeItem != null
                    && this.activeItem.containsEnchantment(Enchantment.KNOCKBACK));
        }
    }

    public boolean isSignificant() {
        if (this.event == null || this.event.isCancelled()) {
            return false;
        } else if (this.hasBigKnockBack()) {
            return true;
        } else {
            switch (this.event.getCause()) {
                case ENTITY_ATTACK:
                case PROJECTILE:
                    return this.timePassed() <= TPS.tickTime;
                default:
                    return MultiVersion.isOrGreater(MultiVersion.MCVersion.V1_9)
                            && (event.getCause() == EntityDamageEvent.DamageCause.DRAGON_BREATH
                            || event.getCause() == EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK)

                            || MultiVersion.isOrGreater(MultiVersion.MCVersion.V1_19)
                            && event.getCause() == EntityDamageEvent.DamageCause.SONIC_BOOM;
            }
        }
    }

    public boolean isSubtle() {
        if (this.event == null || this.event.isCancelled()) {
            return false;
        } else {
            EntityDamageEvent.DamageCause cause = this.event.getCause();

            switch (cause) {
                case STARVATION:
                case DROWNING:
                case LIGHTNING:
                case THORNS:
                case VOID:
                case POISON:
                case WITHER:
                case CONTACT:
                case FALL:
                case LAVA:
                    return this.timePassed() <= AlgebraUtils.integerRound(TPS.maximum / 2.0)
                            && this.parent.movement.getTicksOnAir() <= TPS.maximum;
                default:
                    return cause == EntityDamageEvent.DamageCause.SUFFOCATION
                            && !this.parent.isOutsideOfTheBorder(0.0)

                            || MultiVersion.isOrGreater(MultiVersion.MCVersion.V1_8)
                            && cause == WORLD_BORDER

                            || MultiVersion.isOrGreater(MultiVersion.MCVersion.V1_10)
                            && cause == EntityDamageEvent.DamageCause.HOT_FLOOR

                            || MultiVersion.isOrGreater(MultiVersion.MCVersion.V1_11)
                            && cause == EntityDamageEvent.DamageCause.MAGIC

                            || MultiVersion.isOrGreater(MultiVersion.MCVersion.V1_17)
                            && cause == EntityDamageEvent.DamageCause.FREEZE;
            }
        }
    }

}
