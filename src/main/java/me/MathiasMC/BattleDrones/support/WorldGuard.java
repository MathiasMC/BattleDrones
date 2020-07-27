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

    public WorldGuard(final BattleDrones plugin) {
        this.plugin = plugin;
    }

    public boolean inRegion(Entity entity) {
        if (plugin.getServer().getPluginManager().getPlugin("WorldGuard") != null) {
            final List<String> list = plugin.config.get.getStringList("worldguard");
            if (list.isEmpty()) {
                return true;
            }
            final Location location = BukkitAdapter.adapt(entity.getLocation());
            final RegionContainer container = com.sk89q.worldguard.WorldGuard.getInstance().getPlatform().getRegionContainer();
            final RegionQuery query = container.createQuery();
            final ApplicableRegionSet set = query.getApplicableRegions(location);
            for (ProtectedRegion region : set) {
                if (list.contains(region.getId())) {
                    return false;
                }
            }
            return true;
        }
        return true;
    }
}
