package me.MathiasMC.BattleDrones.support;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.protection.FlagPermissions;
import com.bekvon.bukkit.residence.protection.ResidenceManager;
import com.iridium.iridiumskyblock.IridiumSkyblock;
import com.iridium.iridiumskyblock.Island;
import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.struct.Relation;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import me.MathiasMC.BattleDrones.BattleDrones;
import me.MathiasMC.BattleDrones.data.DroneHolder;
import me.MathiasMC.BattleDrones.data.PlayerConnect;
import me.angeschossen.lands.api.integration.LandsIntegration;
import me.angeschossen.lands.api.player.LandPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LocationSupport {

    private final BattleDrones plugin;

    private LandsIntegration landsIntegration = null;

    private FPlayers fPlayers = null;

    private TownyAdvanced towny = null;

    private Residence residence = null;

    private ResidenceManager residenceManager = null;

    public WorldGuard worldGuard = null;

    public LocationSupport(final BattleDrones plugin) {
        this.plugin = plugin;
        if (plugin.getServer().getPluginManager().getPlugin("WorldGuard") != null) {
            this.worldGuard = WorldGuard.getInstance();
        }
        if (plugin.getServer().getPluginManager().getPlugin("Lands") != null) {
            this.landsIntegration = new LandsIntegration(plugin);
        }
        if (plugin.getServer().getPluginManager().getPlugin("Factions") != null) {
            this.fPlayers = FPlayers.getInstance();
        }
        if (plugin.getServer().getPluginManager().getPlugin("Towny") != null) {
            this.towny = new TownyAdvanced(this.plugin);
        }
        if (plugin.getServer().getPluginManager().getPlugin("Residence") != null) {
            this.residence = Residence.getInstance();
            this.residenceManager = residence.getResidenceManager();
        }
    }

    public boolean inWorldGuardRegion(final Entity entity, final List<String> list) {
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

    public boolean inLocation(Player player, String drone) {
        if (plugin.config.get.getBoolean("iridium-skyblock.use") && plugin.getServer().getPluginManager().getPlugin("IridiumSkyblock") != null) {
            final Island island = IridiumSkyblock.getIslandManager().getIslandViaLocation(player.getLocation());
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
        if (plugin.config.get.contains("drone-worlds")) {
            final String worldName = player.getWorld().getName();
            if (plugin.config.get.contains("drone-worlds.list." + worldName + "." + drone)) {
                if (plugin.config.get.getBoolean("drone-worlds.blacklist")) {
                    if (!player.hasPermission("battledrones.bypass.drone-worlds." + worldName)) {
                        for (String command : plugin.config.get.getStringList("drone-worlds.list." + worldName + "." + drone)) {
                            plugin.getServer().dispatchCommand(plugin.consoleSender, command.replace("{player}", player.getName()));
                        }
                        return false;
                    } else {
                        return true;
                    }
                }
            } else {
                if (!plugin.config.get.getBoolean("drone-worlds.blacklist")) {
                    if (!player.hasPermission("battledrones.bypass.drone-worlds." + worldName)) {
                        for (String command : plugin.config.get.getStringList("drone-worlds.whitelist")) {
                            plugin.getServer().dispatchCommand(plugin.consoleSender, command.replace("{player}", player.getName()).replace("{drone}", plugin.internalPlaceholders.getActiveDrone(drone)).replace("{world}", worldName));
                        }
                        return false;
                    } else {
                        return true;
                    }
                }
            }
        }
        return true;
    }

    public void tp(final Player player) {
        if (plugin.config.get.contains("iridium-skyblock.toggle") && plugin.getServer().getPluginManager().getPlugin("IridiumSkyblock") != null) {
            final List<String> options = plugin.config.get.getStringList("iridium-skyblock.toggle.disabled");
            final Island island = IridiumSkyblock.getIslandManager().getIslandViaLocation(player.getLocation());
            if (island != null && island.isVisit()) {
                for (Player islandPlayer : island.getPlayersOnIsland()) {
                    final String member = islandPlayer.getUniqueId().toString();
                    if (island.getMembers().contains(member)) {
                        toggle(islandPlayer, options, "iridium-skyblock.toggle.commands");
                    }
                }
            }
        }
    }

    public void toggle(final Player player, final List<String> options, final String path) {
        final String uuid = player.getUniqueId().toString();
        if (plugin.list().contains(uuid)) {
            final PlayerConnect playerConnect = plugin.get(uuid);
            if (playerConnect.hasActive()) {
                final String drone = playerConnect.getActive();
                if (plugin.listDroneHolder().contains(uuid) && plugin.getDroneHolderUUID(uuid).containsKey(drone)) {
                    final DroneHolder droneHolder = plugin.getDroneHolder(uuid, drone);
                    final ArrayList<String> list = new ArrayList<>();
                    if (options.contains("PLAYERS") && droneHolder.getPlayers() != 0) {
                        droneHolder.setPlayers(0);
                        list.add("players");
                    }
                    if (options.contains("ANIMALS") && droneHolder.getAnimals() != 0) {
                        droneHolder.setAnimals(0);
                        list.add("animals");
                    }
                    if (options.contains("MONSTERS") && droneHolder.getMonsters() != 0) {
                        droneHolder.setMonsters(0);
                        list.add("monsters");
                    }
                    if (!list.isEmpty()) {
                        for (String command : plugin.config.get.getStringList(path)) {
                            plugin.getServer().dispatchCommand(plugin.consoleSender, command.replace("{player}", player.getName()).replace("{types}", list.toString().replace("[", "").replace("]", "")));
                        }
                        if (playerConnect.hasActive()) {
                            playerConnect.stopAI();
                            playerConnect.stopFindTargetAI();
                            plugin.droneManager.startAI(player, playerConnect, droneHolder, plugin.droneFiles.get(drone), drone);
                        }
                    }
                }
            }
        }
    }

    public boolean canTarget(final Player player, final LivingEntity target) {
        if (target instanceof Player) {
            if (plugin.config.get.getBoolean("lands") && landsIntegration != null) {
                final LandPlayer landPlayer = landsIntegration.getLandPlayer(player.getUniqueId());
                final LandPlayer targetLandPlayer = landsIntegration.getLandPlayer(target.getUniqueId());
                if (landPlayer != null && targetLandPlayer != null) {
                    return Collections.disjoint(landPlayer.getLands(), targetLandPlayer.getLands());
                }
            }
            if (plugin.config.get.getBoolean("factions") && fPlayers != null) {
                final FPlayer fPlayer = fPlayers.getByPlayer(player);
                final FPlayer fPlayerTarget = fPlayers.getByPlayer((Player) target);
                if (fPlayer.hasFaction() && fPlayerTarget.hasFaction()) {
                    if (fPlayer.getFaction().getFPlayers().contains(fPlayerTarget)) {
                        return false;
                    } else return !fPlayer.getFaction().getRelationWish(fPlayerTarget.getFaction()).equals(Relation.ALLY);
                }
            }
            if (plugin.config.get.getBoolean("towny-advanced") && towny != null) {
                return towny.canTarget(player, target);
            }
            if (plugin.config.get.getBoolean("residence") && residence != null && residenceManager != null) {
                final ClaimedResidence claimed = residenceManager.getByLoc(player);
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
            }
        }
        return true;
    }
}
