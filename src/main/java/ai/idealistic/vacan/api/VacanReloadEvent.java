package ai.idealistic.vacan.api;

import ai.idealistic.vacan.Register;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.Plugin;

public class VacanReloadEvent extends Event implements Cancellable {

    private boolean cancelled;

    public VacanReloadEvent() {
        cancelled = false;
        new me.vagdedes.spartan.api.SpartanReloadEvent();
    }

    public Plugin getPlugin() {
        return Register.plugin;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean b) {
        this.cancelled = b;
    }

    private static final HandlerList handlers = new HandlerList();

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
