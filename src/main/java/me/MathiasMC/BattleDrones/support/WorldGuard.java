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

    private final com.sk89q.worldguard.WorldGuard worldGuard;

    public WorldGuard(BattleDrones plugin) {
        this.plugin = plugin;
        this.worldGuard = com.sk89q.worldguard.WorldGuard.getInstance();
    }

    public boolean canTarget(Entity entity, FileConfiguration file, String path) {
        if (plugin.getSupport().worldGuard == null || file == null || !file.contains(path)) {
            return true;
        }

        List<String> excludedRegions = file.getStringList(path);
        if (excludedRegions.isEmpty()) {
            return true;
        }

        Location location = BukkitAdapter.adapt(entity.getLocation());
        RegionContainer regionContainer = worldGuard.getPlatform().getRegionContainer();
        RegionQuery regionQuery = regionContainer.createQuery();
        ApplicableRegionSet regionSet = regionQuery.getApplicableRegions(location);

        for (ProtectedRegion region : regionSet) {
            if (excludedRegions.contains(region.getId())) {
                return false;
            }
        }

        return !excludedRegions.contains("__global__");
    }
}