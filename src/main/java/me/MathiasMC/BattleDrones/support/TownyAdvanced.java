package me.MathiasMC.BattleDrones.support;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import me.MathiasMC.BattleDrones.BattleDrones;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.Optional;

public class TownyAdvanced {

    private final BattleDrones plugin;

    private TownyAPI townyAPI;

    public TownyAdvanced(final BattleDrones plugin) {
        this.plugin = plugin;
        this.townyAPI = TownyAPI.getInstance();
    }

    public boolean canTarget(final Player player, final LivingEntity target) {
        try {
            final Resident resident = townyAPI.getDataSource().getResident(player.getName());
            if (resident.hasTown()) {
                final Town town = townyAPI.getDataSource().getTown(resident.getTown().getName());
                Optional<Resident> playerResident = town.getResidents().stream()
                        .filter(r -> r.getName().equals(target.getName()))
                        .findFirst();
                return !playerResident.isPresent();
            }
            return true;
        } catch (NotRegisteredException e) {
            return true;
        }
    }
}