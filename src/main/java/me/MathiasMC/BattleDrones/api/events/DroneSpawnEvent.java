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

public class DroneSpawnEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();

    private final BattleDrones plugin;

    private boolean cancelled = false;

    private final Player player;

    private final PlayerConnect playerConnect;

    private final DroneHolder droneHolder;

    private boolean bypassWait = false;

    private boolean bypassDroneAmount = false;

    private boolean bypassLocation = false;

    private Type type;

    private List<String> spawnCommands;

    private boolean automatic = true;

    public DroneSpawnEvent(Player player, PlayerConnect playerConnect, DroneHolder droneHolder) {
        this.plugin = BattleDrones.getInstance();
        this.spawnCommands = plugin.droneFiles.get(droneHolder.getDrone()).getStringList("gui.SPAWN-COMMANDS");
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

    public boolean isBypassWait() {
        return this.bypassWait;
    }

    public boolean isBypassDroneAmount() {
        return this.bypassDroneAmount;
    }

    public boolean isBypassLocation() {
        return this.bypassLocation;
    }

    public Type getType() {
        return this.type;
    }

    public List<String> getSpawnCommands() {
        return this.spawnCommands;
    }

    public boolean isAutomatic() {
        return this.automatic;
    }

    public void setBypassWait(final boolean set) {
        this.bypassWait = set;
    }

    public void setBypassDroneAmount(final boolean set) {
        this.bypassDroneAmount = set;
    }

    public void setBypassLocation(final boolean set) {
        this.bypassLocation = set;
    }

    public void setType(final Type set) {
        this.type = set;
    }

    public void setSpawnCommands(final List<String> set) {
        this.spawnCommands = set;
    }

    public void setAutomatic(final boolean set) {
        automatic = set;
        playerConnect.setAutomatic(set);
    }

    public boolean hasWait() {
        return plugin.drone_wait.contains(droneHolder.getUniqueId());
    }

    public boolean isInLocation() {
        return plugin.getSupport().inLocation(player, droneHolder.getDrone());
    }

    public void spawn() {
        plugin.getEntityManager().spawnDrone(this);
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