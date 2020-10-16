package me.MathiasMC.BattleDrones.support;

import me.MathiasMC.BattleDrones.BattleDrones;
import me.MathiasMC.BattleDrones.data.PlayerConnect;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

public class Vault {

    private final BattleDrones plugin;

    private Economy econ = null;

    public Vault() {
        this.plugin = BattleDrones.getInstance();
        if (plugin.getServer().getPluginManager().getPlugin("Vault") == null) {
            return;
        }
        final RegisteredServiceProvider<Economy> rsp = plugin.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return;
        }
        econ = rsp.getProvider();
        plugin.getTextUtils().info("Vault found");
    }

    public Economy getEconomy() {
        return econ;
    }

    public boolean withdraw(final Player player, final long cost) {
        final PlayerConnect playerConnect = plugin.getPlayerConnect(player.getUniqueId().toString());
        final long coins = playerConnect.getCoins();
        if (!plugin.getFileUtils().config.getBoolean("vault") && coins >= cost ||
                plugin.getFileUtils().config.getBoolean("vault") &&
                        getEconomy() != null &&
                        getEconomy().withdrawPlayer(player, cost).transactionSuccess()) {
            if (!plugin.getFileUtils().config.getBoolean("vault")) {
                playerConnect.setCoins(coins - cost);
            }
            return true;
        }
        return false;
    }
}
