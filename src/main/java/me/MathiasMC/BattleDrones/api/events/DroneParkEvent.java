package me.MathiasMC.BattleDrones.api.events;

import me.MathiasMC.BattleDrones.api.Type;
import me.MathiasMC.BattleDrones.data.DroneHolder;
import me.MathiasMC.BattleDrones.data.PlayerConnect;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class DroneParkEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    private boolean cancelled = false;

    private final Player player;

    private final PlayerConnect playerConnect;

    private final DroneHolder droneHolder;

    private Type type;

    public DroneParkEvent(Player player, PlayerConnect playerConnect, DroneHolder droneHolder) {
        this.player = player;
        this.playerConnect = playerConnect;
        this.droneHolder = droneHolder;
    }

    public Player getPlayer() {
        return this.player;
    }

    public PlayerConnect getPlayerConnect() {
        return this.playerConnect;
    }

    public DroneHolder getDroneHolder() {
        return this.droneHolder;
    }

    public Type getType() {
        return this.type;
    }

    public void setType(final Type set) {
        this.type = set;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean set) {
        cancelled = set;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
