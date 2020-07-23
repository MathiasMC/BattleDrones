package me.MathiasMC.BattleDrones.placeholders;

import me.MathiasMC.BattleDrones.BattleDrones;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;

public class PlaceholderAPI extends PlaceholderExpansion {

    private BattleDrones plugin;

    public PlaceholderAPI(BattleDrones plugin){
        this.plugin = plugin;
    }

    @Override
    public boolean persist(){
        return true;
    }

    @Override
    public boolean canRegister(){
        return true;
    }

    @Override
    public String getAuthor(){
        return plugin.getDescription().getAuthors().toString();
    }

    @Override
    public String getIdentifier(){
        return "battledrones";
    }

    @Override
    public String getVersion(){
        return plugin.getDescription().getVersion();
    }

    @Override
    public String onPlaceholderRequest(Player player, String identifier) {
        if (player == null) {
            return "";
        }
        final String uuid = player.getUniqueId().toString();
        if(identifier.equals("active")){
            return plugin.internalPlaceholders.getActiveDrone(plugin.get(uuid));
        }
        if(identifier.equals("coins")){
            return String.valueOf(plugin.get(uuid).getCoins());
        }
        if(identifier.equals("group")){
            return plugin.get(uuid).getGroup();
        }
        if(identifier.equals("health")){
            return String.valueOf(plugin.internalPlaceholders.getDroneHealth(plugin.get(uuid), uuid));
        }
        if(identifier.equals("health_max")){
            return String.valueOf(plugin.internalPlaceholders.getDroneMaxHealth(plugin.get(uuid), uuid));
        }
        if(identifier.equals("health_bar")){
            return String.valueOf(plugin.internalPlaceholders.getDroneHealthBar(plugin.get(uuid), uuid));
        }
        if(identifier.equals("ammo")){
            return String.valueOf(plugin.internalPlaceholders.getDroneAmmo(plugin.get(uuid), uuid));
        }
        if(identifier.equals("ammo_max")){
            return String.valueOf(plugin.internalPlaceholders.getDroneMaxAmmo(plugin.get(uuid), uuid));
        }
        return null;
    }
}
