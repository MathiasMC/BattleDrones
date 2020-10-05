package me.MathiasMC.BattleDrones.support;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import org.bukkit.entity.Entity;

import java.util.List;

public class WorldGuard {

    private com.sk89q.worldguard.WorldGuard worldGuard;

    public WorldGuard() {
        this.worldGuard = com.sk89q.worldguard.WorldGuard.getInstance();
    }

    public boolean canTarget(final Entity entity, final List<String> list) {
        if (list.isEmpty()) {
            return false;
        }
        final Location location = BukkitAdapter.adapt(entity.getLocation());
        final RegionContainer container = worldGuard.getPlatform().getRegionContainer();
        final RegionQuery query = container.createQuery();
        final ApplicableRegionSet set = query.getApplicableRegions(location);
        for (ProtectedRegion region : set) {
            if (list.contains(region.getId())) {
                return true;
            }
        }
        return false;
    }
}