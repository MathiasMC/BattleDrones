package me.MathiasMC.BattleDrones.support;

import me.MathiasMC.BattleDrones.BattleDrones;
import me.angeschossen.lands.api.integration.LandsIntegration;
import me.angeschossen.lands.api.player.LandPlayer;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.Collections;

public class Lands {

    private final LandsIntegration landsIntegration;

    public Lands(final BattleDrones plugin) {
        this.landsIntegration = new LandsIntegration(plugin);
    }

    public boolean canTarget(final Player player, final LivingEntity target) {
        final LandPlayer landPlayer = landsIntegration.getLandPlayer(player.getUniqueId());
        final LandPlayer targetLandPlayer = landsIntegration.getLandPlayer(target.getUniqueId());
        if (landPlayer != null && targetLandPlayer != null) {
            return Collections.disjoint(landPlayer.getLands(), targetLandPlayer.getLands());
        }
        return true;
    }
}
