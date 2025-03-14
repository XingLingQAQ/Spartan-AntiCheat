package me.vagdedes.spartan.api;

import ai.idealistic.vacan.abstraction.check.CheckEnums.HackType;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayerViolationCommandEvent extends Event implements Cancellable {

    private final Player p;
    private final HackType h;
    private final String c;
    @Setter
    @Getter
    private boolean cancelled;

    public PlayerViolationCommandEvent(Player player, HackType HackType, String command) {
        p = player;
        h = HackType;
        c = command;
        cancelled = false;
    }

    public Player getPlayer() {
        return p;
    }

    public HackType getHackType() {
        return h;
    }

    public String getCommand() {
        return c;
    }

    private static final HandlerList handlers = new HandlerList();

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
