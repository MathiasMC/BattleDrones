package me.MathiasMC.BattleDrones.support;

import me.MathiasMC.BattleDrones.BattleDrones;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

public class Vault {

    private Economy econ = null;

    public Vault() {
        BattleDrones plugin = BattleDrones.getInstance();
        RegisteredServiceProvider<Economy> rsp = plugin.getServer().getServicesManager().getRegistration(Economy.class);
        this.econ = (rsp != null) ? rsp.getProvider() : null;
    }

    public Economy getEconomy() {
        return econ;
    }

    public boolean withdraw(Player player, long cost) {
        return econ != null && econ.withdrawPlayer(player, cost).transactionSuccess();
    }
}
