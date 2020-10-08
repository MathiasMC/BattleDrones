package me.MathiasMC.BattleDrones.support;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import me.MathiasMC.BattleDrones.BattleDrones;
import org.bukkit.entity.Entity;

import java.util.List;

public class WorldGuard {

    private final BattleDrones plugin;

    private com.sk89q.worldguard.WorldGuard worldGuard;

    public WorldGuard(final BattleDrones plugin) {
        this.plugin = plugin;
        if (plugin.getServer().getPluginManager().getPlugin("WorldGuard") != null) {
            this.worldGuard = com.sk89q.worldguard.WorldGuard.getInstance();
            plugin.textUtils.info("Found WorldGuard");
        }
    }

    public boolean canTarget(final Entity entity, final List<String> list) {
        if (plugin.support.worldGuard == null ) {
            return true;
        }
        if (!plugin.config.get.getBoolean("worldguard.use")) {
            return true;
        }
        if (list.isEmpty()) {
            return true;
        }
        final Location location = BukkitAdapter.adapt(entity.getLocation());
        final RegionContainer container = worldGuard.getPlatform().getRegionContainer();
        final RegionQuery query = container.createQuery();
        final ApplicableRegionSet set = query.getApplicableRegions(location);
        for (ProtectedRegion region : set) {
            if (list.contains(region.getId())) {
                return false;
            }
        }
        return true;
    }
}