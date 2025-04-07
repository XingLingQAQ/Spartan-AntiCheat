package ai.idealistic.spartan.api;

import ai.idealistic.spartan.abstraction.check.CheckEnums.HackType;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayerViolationEvent extends Event implements Cancellable {

    private final Player p;
    private final HackType h;
    private final String m;
    @Setter
    @Getter
    private boolean cancelled;

    public PlayerViolationEvent(Player player, HackType HackType, String message) {
        p = player;
        h = HackType;
        m = message;
        cancelled = false;
        me.vagdedes.spartan.api.PlayerViolationEvent event
                = new me.vagdedes.spartan.api.PlayerViolationEvent(player, HackType, message);
        event.setCancelled(this.isCancelled());
        Bukkit.getPluginManager().callEvent(event);
    }

    public Player getPlayer() {
        return p;
    }

    public HackType getHackType() {
        return h;
    }

    public String getMessage() {
        return m;
    }

    private static final HandlerList handlers = new HandlerList();

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
