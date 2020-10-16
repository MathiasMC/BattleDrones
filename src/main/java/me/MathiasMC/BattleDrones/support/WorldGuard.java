package me.MathiasMC.BattleDrones.support;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import me.MathiasMC.BattleDrones.BattleDrones;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;

import java.util.List;

public class WorldGuard {

    private final BattleDrones plugin;

    private com.sk89q.worldguard.WorldGuard worldGuard;

    public WorldGuard(final BattleDrones plugin) {
        this.plugin = plugin;
        if (plugin.getServer().getPluginManager().getPlugin("WorldGuard") != null) {
            this.worldGuard = com.sk89q.worldguard.WorldGuard.getInstance();
            plugin.getTextUtils().info("Found WorldGuard");
        }
    }

    public boolean canTarget(final Entity entity, final FileConfiguration file, final String path) {
        if (plugin.getSupport().worldGuard == null ) {
            return true;
        }
        if (!file.contains(path)) {
            return true;
        }
        final List<String> list = file.getStringList(path);
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
        return !list.contains("__global__");
    }
}