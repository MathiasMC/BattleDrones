package me.MathiasMC.BattleDrones.gui;

import me.MathiasMC.BattleDrones.BattleDrones;
import me.MathiasMC.BattleDrones.data.PlayerConnect;
import org.bukkit.entity.Player;

public class Menu {

    private final Player player;

    private final String uuid;

    private final PlayerConnect playerConnect;

    public Menu(Player player) {
        this.player = player;
        this.uuid = player.getUniqueId().toString();
        this.playerConnect = BattleDrones.getInstance().getPlayerConnect(uuid);
    }

    public Player getPlayer() {
        return this.player;
    }

    public String getUuid() {
        return this.uuid;
    }

    public PlayerConnect getPlayerConnect() {
        return this.playerConnect;
    }
}