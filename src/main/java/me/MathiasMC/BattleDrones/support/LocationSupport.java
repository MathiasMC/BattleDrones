package me.MathiasMC.BattleDrones.support;

import com.iridium.iridiumskyblock.IridiumSkyblock;
import com.iridium.iridiumskyblock.Island;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import me.MathiasMC.BattleDrones.BattleDrones;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.List;

public class LocationSupport {

    private final BattleDrones plugin;

    public LocationSupport(final BattleDrones plugin) {
        this.plugin = plugin;
    }

    public boolean inWorldGuardRegion(Entity entity) {
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

    public boolean inLocation(Player player) {
        if (plugin.config.get.getBoolean("iridium-skyblock.use") && plugin.getServer().getPluginManager().getPlugin("IridiumSkyblock") != null) {
            Island island = IridiumSkyblock.getIslandManager().getIslandViaLocation(player.getLocation());
            if (island != null) {
                if (island.getMembers().contains(player.getUniqueId().toString())) {
                    return true;
                } else {
                    for (String command : plugin.config.get.getStringList("iridium-skyblock.island")) {
                        plugin.getServer().dispatchCommand(plugin.consoleSender, command.replace("{player}", player.getName()));
                    }
                }
            } else {
                for (String command : plugin.config.get.getStringList("iridium-skyblock.no-island")) {
                    plugin.getServer().dispatchCommand(plugin.consoleSender, command.replace("{player}", player.getName()));
                }
            }
            return false;
        }
        return true;
    }
}
