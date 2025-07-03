package me.MathiasMC.BattleDrones.external;

import me.MathiasMC.BattleDrones.BattleDrones;
import me.MathiasMC.BattleDrones.api.DroneRegistry;
import me.MathiasMC.BattleDrones.external.drones.*;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class ExternalDrones {

    public ExternalDrones(BattleDrones plugin) {
        List<String> disable = plugin.getFileUtils().config.getStringList("disable");

        Map<String, Supplier<DroneRegistry>> drones = createDrones(plugin);

        for (Map.Entry<String, Supplier<DroneRegistry>> entry : drones.entrySet()) {
            if (!disable.contains(entry.getKey())) {
                entry.getValue().get().register();
            }
        }

        if (!disable.contains("shield_generator")) {
            ShieldGenerator shieldGenerator = new ShieldGenerator(plugin, "shield_generator", "protective");
            shieldGenerator.register();
            plugin.getServer().getPluginManager().registerEvents(shieldGenerator, plugin);
        }
    }

    private static @NotNull Map<String, Supplier<DroneRegistry>> createDrones(BattleDrones plugin) {
        Map<String, Supplier<DroneRegistry>> drones = new LinkedHashMap<>();
        drones.put("laser", () -> new Laser(plugin, "laser", "energy"));
        drones.put("machine_gun", () -> new MachineGun(plugin, "machine_gun", "kinetic"));
        drones.put("rocket", () -> new Rocket(plugin, "rocket", "explode"));
        drones.put("faf_missile", () -> new FaFMissile(plugin, "faf_missile", "explode"));
        drones.put("mortar", () -> new Mortar(plugin, "mortar", "explode"));
        drones.put("healing", () -> new Healing(plugin, "healing", "protective"));
        drones.put("flamethrower", () -> new Flamethrower(plugin, "flamethrower", "special"));
        drones.put("lightning", () -> new Lightning(plugin, "lightning", "special"));
        drones.put("teleport", () -> new Teleport(plugin, "teleport", "special"));
        return drones;
    }
}