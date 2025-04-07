package ai.idealistic.spartan.api;

import ai.idealistic.spartan.abstraction.check.CheckEnums.HackType;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class CheckCancelEvent extends Event implements Cancellable {

    private final Player p;
    private final HackType h;
    @Setter
    @Getter
    private boolean cancelled;

    public CheckCancelEvent(Player player, HackType HackType) {
        p = player;
        h = HackType;
        cancelled = false;
        me.vagdedes.spartan.api.CheckCancelEvent event
                = new me.vagdedes.spartan.api.CheckCancelEvent(player, HackType);
        event.setCancelled(this.isCancelled());
        Bukkit.getPluginManager().callEvent(event);
    }

    public Player getPlayer() {
        return p;
    }

    public HackType getHackType() {
        return h;
    }

    private static final HandlerList handlers = new HandlerList();

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
