package me.MathiasMC.BattleDrones.external;

import me.MathiasMC.BattleDrones.BattleDrones;
import me.MathiasMC.BattleDrones.external.drones.*;

public class ExternalDrones {

    private final BattleDrones plugin;

    public ExternalDrones() {
        this.plugin = BattleDrones.getInstance();
        new Gun(plugin, "laser", "energy").register();
        new Gun(plugin, "machine_gun", "kinetic").register();
        new Rocket(plugin, "rocket", "explode").register();
        new Rocket(plugin, "faf_missile", "explode").register();
        new Rocket(plugin, "mortar", "explode").register();
        new Healing(plugin, "healing", "protective").register();
        new Flamethrower(plugin, "flamethrower", "special").register();
        new Lightning(plugin, "lightning", "special").register();
        final ShieldGenerator shieldGenerator = new ShieldGenerator(plugin, "shield_generator", "protective");
        shieldGenerator.register();
        plugin.getServer().getPluginManager().registerEvents(shieldGenerator, plugin);
    }
}