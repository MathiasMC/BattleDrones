package me.MathiasMC.BattleDrones.support;

import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.protection.FlagPermissions;
import org.bukkit.entity.Player;

public class Residence {

    private final com.bekvon.bukkit.residence.Residence residence;

    public Residence() {
        this.residence = com.bekvon.bukkit.residence.Residence.getInstance();
    }

    public boolean canTarget(final Player player, final Player target) {
        if (residence == null) {
            return true;
        }
        if (!residence.isEnabled()) {
            return true;
        }
        final ClaimedResidence claimed = residence.getResidenceManager().getByLoc(player);
        if (claimed != null) {
            if (!claimed.isOwner(player)) {
                if (!residence.getPermsByLoc(player.getLocation()).listPlayersFlags().contains(player.getName())) {
                    return true;
                }
                return !claimed.getRPlayer().getUniqueId().toString().equalsIgnoreCase(target.getUniqueId().toString());
            }
            return !residence.getPermsByLoc(player.getLocation()).listPlayersFlags().contains(target.getName());
        }
        final FlagPermissions flagPermissions = residence.getPermsByLoc(target.getLocation());
        if (flagPermissions != null) {
            return !residence.getPermsByLoc(target.getLocation()).listPlayersFlags().contains(player.getName());
        }
        return true;
    }
}
