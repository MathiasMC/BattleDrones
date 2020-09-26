package me.MathiasMC.BattleDrones.files;

import me.MathiasMC.BattleDrones.BattleDrones;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class DronesFolder {

    private final BattleDrones plugin;

    final File laser;
    final File machine_gun;
    final File rocket;
    final File shield_generator;
    final File healing;
    final File flamethrower;
    final File faf_missile;
    final File mortar;
    final File lightning;

    public DronesFolder(final BattleDrones plugin) {
        this.plugin = plugin;
        File folder = new File(plugin.getDataFolder() + File.separator + "drones");
        if (!folder.exists()) {
            folder.mkdir();
        }
        File folderEnergy = new File(folder + File.separator + "energy");
        if (!folderEnergy.exists()) {
            folderEnergy.mkdir();
        }
        File folderExplode = new File(folder + File.separator + "explode");
        if (!folderExplode.exists()) {
            folderExplode.mkdir();
        }
        File folderKinetic = new File(folder + File.separator + "kinetic");
        if (!folderKinetic.exists()) {
            folderKinetic.mkdir();
        }
        File folderProtective = new File(folder + File.separator + "protective");
        if (!folderProtective.exists()) {
            folderProtective.mkdir();
        }
        File folderSpecial = new File(folder + File.separator + "special");
        if (!folderSpecial.exists()) {
            folderSpecial.mkdir();
        }
        laser = new File(folderEnergy, "laser.yml");
        if (!laser.exists()) {
            try {
                laser.createNewFile();
                plugin.copy("drones/energy/laser.yml", laser);
            } catch (IOException exception) {
                plugin.textUtils.exception(exception.getStackTrace(), exception.getMessage());
            }
        }
        rocket = new File(folderExplode, "rocket.yml");
        if (!rocket.exists()) {
            try {
                rocket.createNewFile();
                plugin.copy("drones/explode/rocket.yml", rocket);
            } catch (IOException exception) {
                plugin.textUtils.exception(exception.getStackTrace(), exception.getMessage());
            }
        }
        machine_gun = new File(folderKinetic, "machine_gun.yml");
        if (!machine_gun.exists()) {
            try {
                machine_gun.createNewFile();
                plugin.copy("drones/kinetic/machine_gun.yml", machine_gun);
            } catch (IOException exception) {
                plugin.textUtils.exception(exception.getStackTrace(), exception.getMessage());
            }
        }
        shield_generator = new File(folderProtective, "shield_generator.yml");
        if (!shield_generator.exists()) {
            try {
                shield_generator.createNewFile();
                plugin.copy("drones/protective/shield_generator.yml", shield_generator);
            } catch (IOException exception) {
                plugin.textUtils.exception(exception.getStackTrace(), exception.getMessage());
            }
        }
        healing = new File(folderProtective, "healing.yml");
        if (!healing.exists()) {
            try {
                healing.createNewFile();
                plugin.copy("drones/protective/healing.yml", healing);
            } catch (IOException exception) {
                plugin.textUtils.exception(exception.getStackTrace(), exception.getMessage());
            }
        }
        flamethrower = new File(folderSpecial, "flamethrower.yml");
        if (!flamethrower.exists()) {
            try {
                flamethrower.createNewFile();
                plugin.copy("drones/special/flamethrower.yml", flamethrower);
            } catch (IOException exception) {
                plugin.textUtils.exception(exception.getStackTrace(), exception.getMessage());
            }
        }
        faf_missile = new File(folderExplode, "faf_missile.yml");
        if (!faf_missile.exists()) {
            try {
                faf_missile.createNewFile();
                plugin.copy("drones/explode/faf_missile.yml", faf_missile);
            } catch (IOException exception) {
                plugin.textUtils.exception(exception.getStackTrace(), exception.getMessage());
            }
        }
        mortar = new File(folderExplode, "mortar.yml");
        if (!mortar.exists()) {
            try {
                mortar.createNewFile();
                plugin.copy("drones/explode/mortar.yml", mortar);
            } catch (IOException exception) {
                plugin.textUtils.exception(exception.getStackTrace(), exception.getMessage());
            }
        }
        lightning = new File(folderSpecial, "lightning.yml");
        if (!lightning.exists()) {
            try {
                lightning.createNewFile();
                plugin.copy("drones/special/lightning.yml", lightning);
            } catch (IOException exception) {
                plugin.textUtils.exception(exception.getStackTrace(), exception.getMessage());
            }
        }
        load();
    }

    public void load() {
        plugin.droneFiles.put("laser", YamlConfiguration.loadConfiguration(laser));
        plugin.droneFiles.put("rocket", YamlConfiguration.loadConfiguration(rocket));
        plugin.droneFiles.put("machine_gun", YamlConfiguration.loadConfiguration(machine_gun));
        plugin.droneFiles.put("shield_generator", YamlConfiguration.loadConfiguration(shield_generator));
        plugin.droneFiles.put("healing", YamlConfiguration.loadConfiguration(healing));
        plugin.droneFiles.put("flamethrower", YamlConfiguration.loadConfiguration(flamethrower));
        plugin.droneFiles.put("faf_missile", YamlConfiguration.loadConfiguration(faf_missile));
        plugin.droneFiles.put("mortar", YamlConfiguration.loadConfiguration(mortar));
        plugin.droneFiles.put("lightning", YamlConfiguration.loadConfiguration(lightning));
    }
}