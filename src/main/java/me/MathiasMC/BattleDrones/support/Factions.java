package me.MathiasMC.BattleDrones.support;

import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.struct.Relation;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class Factions {

    private FPlayers fPlayers;

    public Factions() {
        this.fPlayers = FPlayers.getInstance();
    }

    public boolean canTarget(final Player player, final LivingEntity target) {
        if (fPlayers == null) {
            return true;
        }
        final FPlayer fPlayer = fPlayers.getByPlayer(player);
        final FPlayer fPlayerTarget = fPlayers.getByPlayer((Player) target);
        if (fPlayer.hasFaction() && fPlayerTarget.hasFaction()) {
            if (fPlayer.getFaction().getFPlayers().contains(fPlayerTarget)) {
                return false;
            } else return !fPlayer.getFaction().getRelationWish(fPlayerTarget.getFaction()).equals(Relation.ALLY);
        }
        return true;
    }
}