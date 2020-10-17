package me.MathiasMC.BattleDrones.external;

import me.MathiasMC.BattleDrones.BattleDrones;
import me.MathiasMC.BattleDrones.external.drones.*;

import java.util.List;

public class ExternalDrones {

    private final BattleDrones plugin;

    public ExternalDrones() {
        this.plugin = BattleDrones.getInstance();
        final List<String> disable = plugin.getFileUtils().config.getStringList("disable");
        if (!disable.contains("laser")) {
            new Gun(plugin, "laser", "energy").register();
        }
        if (!disable.contains("machine_gun")) {
            new Gun(plugin, "machine_gun", "kinetic").register();
        }
        if (!disable.contains("rocket")) {
            new Rocket(plugin, "rocket", "explode").register();
        }
        if (!disable.contains("faf_missile")) {
            new Rocket(plugin, "faf_missile", "explode").register();
        }
        if (!disable.contains("mortar")) {
            new Rocket(plugin, "mortar", "explode").register();
        }
        if (!disable.contains("healing")) {
            new Healing(plugin, "healing", "protective").register();
        }
        if (!disable.contains("flamethrower")) {
            new Flamethrower(plugin, "flamethrower", "special").register();
        }
        if (!disable.contains("lightning")) {
            new Lightning(plugin, "lightning", "special").register();
        }
        if (!disable.contains("shield_generator")) {
            final ShieldGenerator shieldGenerator = new ShieldGenerator(plugin, "shield_generator", "protective");
            shieldGenerator.register();
            plugin.getServer().getPluginManager().registerEvents(shieldGenerator, plugin);
        }
        if (!disable.contains("teleport")) {
            new Teleport(plugin, "teleport", "special").register();
        }
    }
}