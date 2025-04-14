package ai.idealistic.spartan.abstraction.event;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;

public class PlayerAttackEvent implements Cancellable {

    public final Player player;
    public final LivingEntity target;
    private boolean cancelled;

    public PlayerAttackEvent(Player player, LivingEntity target, boolean cancelled) {
        this.player = player;
        this.target = target;
        this.cancelled = cancelled;
    }

    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }

    @Override
    public void setCancelled(boolean b) {
        this.cancelled = b;
    }
}
