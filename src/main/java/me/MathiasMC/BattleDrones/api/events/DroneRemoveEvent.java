package me.MathiasMC.BattleDrones.api.events;

import me.MathiasMC.BattleDrones.BattleDrones;
import me.MathiasMC.BattleDrones.api.Type;
import me.MathiasMC.BattleDrones.data.DroneHolder;
import me.MathiasMC.BattleDrones.data.PlayerConnect;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class DroneRemoveEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();

    private final BattleDrones plugin;

    private boolean cancelled = false;

    private final Player player;

    private final PlayerConnect playerConnect;

    private final DroneHolder droneHolder;

    private Type type;

    private List<String> removeCommands;

    public DroneRemoveEvent(Player player, PlayerConnect playerConnect, DroneHolder droneHolder) {
        this.plugin = BattleDrones.getInstance();
        this.removeCommands = plugin.droneFiles.get(droneHolder.getDrone()).getStringList("gui.REMOVE-COMMANDS");
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

    public List<String> getRemoveCommands() {
        return this.removeCommands;
    }

    public void setType(final Type set) {
        this.type = set;
    }

    public void setRemoveCommands(final List<String> set) {
        this.removeCommands = set;
    }

    public void remove() {
        plugin.getEntityManager().removeDrone(this);
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