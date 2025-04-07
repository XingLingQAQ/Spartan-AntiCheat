package ai.idealistic.spartan.api;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayerFoundOreEvent extends Event implements Cancellable {

    private final Player p;
    private final String me;
    private final Location l;
    private final Material ma;
    @Setter
    @Getter
    private boolean cancelled;

    public PlayerFoundOreEvent(Player player, String message, Location location, Material material) {
        p = player;
        me = message;
        l = location;
        ma = material;
        cancelled = false;
        me.vagdedes.spartan.api.PlayerFoundOreEvent event
                = new me.vagdedes.spartan.api.PlayerFoundOreEvent(player, message, location, material);
        event.setCancelled(this.isCancelled());
        Bukkit.getPluginManager().callEvent(event);
    }

    public Player getPlayer() {
        return p;
    }

    public String getMessage() {
        return me;
    }

    public Location getLocation() {
        return l;
    }

    public Material getMaterial() {
        return ma;
    }

    private static final HandlerList handlers = new HandlerList();

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
