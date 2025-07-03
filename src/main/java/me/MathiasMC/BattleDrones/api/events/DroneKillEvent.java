package me.MathiasMC.BattleDrones.api.events;

import me.MathiasMC.BattleDrones.data.DroneHolder;
import me.MathiasMC.BattleDrones.data.PlayerConnect;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class DroneKillEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    private final Player player;

    private final PlayerConnect playerConnect;

    private final DroneHolder droneHolder;

    private final LivingEntity target;

    public DroneKillEvent(Player player, PlayerConnect playerConnect, DroneHolder droneHolder, LivingEntity target) {
        this.player = player;
        this.playerConnect = playerConnect;
        this.droneHolder = droneHolder;
        this.target = target;
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

    public LivingEntity getTarget() {
        return this.target;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}