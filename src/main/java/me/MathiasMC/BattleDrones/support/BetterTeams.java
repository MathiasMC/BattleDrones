package me.MathiasMC.BattleDrones.support;

import com.booksaw.betterTeams.Team;
import com.booksaw.betterTeams.TeamPlayer;
import me.MathiasMC.BattleDrones.BattleDrones;
import org.bukkit.entity.Player;

public class BetterTeams {

    private final BattleDrones plugin;

    public BetterTeams(final BattleDrones plugin) {
        this.plugin = plugin;
    }

    public boolean canTarget(final Player player, final Player target) {
        final Team team = Team.getTeam(player);
        if (team != null) {
            final TeamPlayer teamPlayer = team.getTeamPlayer(target);
            if (teamPlayer != null) {
                return !team.getMembers().contains(teamPlayer);
            }
            if (plugin.getFileUtils().config.getBoolean("better-teams.ally")) {
                final Team targetTeam = Team.getTeam(target);
                return targetTeam == null || !team.isAlly(targetTeam.getID());
            }
        }
        return true;
    }
}
