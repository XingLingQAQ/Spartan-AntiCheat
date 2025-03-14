package ai.idealistic.vacan.abstraction.check.implementation.player;

import ai.idealistic.vacan.abstraction.check.CheckDetection;
import ai.idealistic.vacan.abstraction.check.CheckEnums.HackType;
import ai.idealistic.vacan.abstraction.check.CheckRunner;
import ai.idealistic.vacan.abstraction.check.definition.ImplementedDetection;
import ai.idealistic.vacan.abstraction.data.Buffer;
import ai.idealistic.vacan.abstraction.protocol.PlayerProtocol;
import ai.idealistic.vacan.compatibility.manual.vanilla.Attributes;
import ai.idealistic.vacan.functionality.server.MultiVersion;
import ai.idealistic.vacan.utils.minecraft.entity.PlayerUtils;
import org.bukkit.Difficulty;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.potion.PotionEffectType;

public class FastHeal extends CheckRunner {

    private final CheckDetection detection;
    private double previousHealth;
    private long time;
    private final Buffer.IndividualBuffer buffer;

    public FastHeal(HackType hackType, PlayerProtocol protocol) {
        super(hackType, protocol);
        this.detection = new ImplementedDetection(this, null, null, null, true);
        this.buffer = new Buffer.IndividualBuffer();
    }

    private long calculate() {
        int level = PlayerUtils.getPotionLevel(this.protocol, PotionEffectType.REGENERATION);
        long i = 0L;

        if (level > -1) {
            if (level <= 10) {
                i = level == 0 ? 300L : 45L;
            }
        } else {
            i = !MultiVersion.isOrGreater(MultiVersion.MCVersion.V1_9) ? 3700L : 450L;
        }
        return i;
    }

    @Override
    protected void handleInternal(boolean cancelled, Object object) {
        if (object instanceof EntityRegainHealthEvent) {
            this.detection.call(() -> {
                EntityRegainHealthEvent event = (EntityRegainHealthEvent) object;
                RegainReason reason = event.getRegainReason();

                if (reason != RegainReason.CUSTOM
                        && reason != RegainReason.MAGIC) {
                    double health = this.protocol.getHealth();
                    long timePassed = System.currentTimeMillis() - time;
                    time = System.currentTimeMillis();

                    if (previousHealth != health) {
                        long limit = calculate();

                        if (limit > 0L) {
                            if (timePassed < limit
                                    && buffer.count(1, 10) == 3) {
                                buffer.reset();
                                this.detection.cancel(
                                        "default, ms: " + timePassed + ", limit: " + limit
                                );

                                if (this.prevent()) {
                                    event.setCancelled(true);
                                }
                            }
                        }
                    }
                    previousHealth = health;
                }
            });
        } else if (object instanceof FoodLevelChangeEvent) {
            if (((FoodLevelChangeEvent) object).getFoodLevel() > this.protocol.bukkit().getFoodLevel()
                    && (!MultiVersion.isOrGreater(MultiVersion.MCVersion.V1_13)
                    || this.protocol.getItemInHand().getType() != Material.DRIED_KELP)) {
                this.protocol.profile().getRunner(hackType).addDisableCause(null, null, 10);
            }
        }
    }

    @Override
    protected boolean canRun() {
        if (this.protocol.getWorld().getDifficulty() != Difficulty.PEACEFUL
                && this.protocol.bukkit().getFireTicks() <= 0
                && Attributes.getAmount(this.protocol, Attributes.GENERIC_MAX_ABSORPTION) == 0.0) {
            GameMode gameMode = this.protocol.getGameMode();
            return gameMode == GameMode.ADVENTURE || gameMode == GameMode.SURVIVAL;
        } else {
            return false;
        }
    }
}
