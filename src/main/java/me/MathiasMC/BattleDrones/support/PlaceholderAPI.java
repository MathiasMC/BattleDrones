package me.MathiasMC.BattleDrones.support;

import me.MathiasMC.BattleDrones.BattleDrones;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;

public class PlaceholderAPI extends PlaceholderExpansion {

    private final BattleDrones plugin;

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
            return plugin.getPlaceholderManager().getActiveDrone(plugin.getPlayerConnect(uuid).getActive());
        }
        if(identifier.equals("coins")){
            return String.valueOf(plugin.getPlayerConnect(uuid).getCoins());
        }
        if(identifier.equals("group")){
            return plugin.getPlayerConnect(uuid).getGroup();
        }
        if(identifier.equals("health")){
            return String.valueOf(plugin.getPlaceholderManager().getDroneHealth(uuid));
        }
        if(identifier.equals("health_max")){
            return String.valueOf(plugin.getPlaceholderManager().getDroneMaxHealth(uuid));
        }
        if(identifier.equals("health_bar")){
            return plugin.getPlaceholderManager().getDroneHealthBar(uuid);
        }
        if(identifier.equals("health_percent")){
            return String.valueOf(plugin.getCalculateManager().getPercent(plugin.getPlaceholderManager().getDroneHealth(uuid), plugin.getPlaceholderManager().getDroneMaxHealth(uuid)));
        }
        if(identifier.equals("ammo")){
            return String.valueOf(plugin.getPlaceholderManager().getDroneAmmo(uuid));
        }
        if(identifier.equals("ammo_max")){
            return String.valueOf(plugin.getPlaceholderManager().getDroneMaxAmmo(uuid));
        }
        if(identifier.equals("ammo_bar")){
            return plugin.getPlaceholderManager().getDroneAmmoBar(uuid);
        }
        if(identifier.equals("ammo_percent")){
            return String.valueOf(plugin.getCalculateManager().getPercent(plugin.getPlaceholderManager().getDroneAmmo(uuid), plugin.getPlaceholderManager().getDroneMaxAmmo(uuid)));
        }
        if (identifier.equals("monsters")) {
            return plugin.getPlaceholderManager().getAvailability(uuid, "monsters");
        }
        if (identifier.equals("animals")) {
            return plugin.getPlaceholderManager().getAvailability(uuid, "animals");
        }
        if (identifier.equals("players")) {
            return plugin.getPlaceholderManager().getAvailability(uuid, "players");
        }
        if (identifier.equals("parked")) {
            return plugin.getPlaceholderManager().getQuestion(uuid, "parked");
        }
        if (identifier.equals("stationary")) {
            return plugin.getPlaceholderManager().getQuestion(uuid, "stationary");
        }
        if (identifier.equals("move")) {
            return plugin.getPlaceholderManager().getQuestion(uuid, "move");
        }
        return null;
    }
}