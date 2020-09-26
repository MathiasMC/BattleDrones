package me.MathiasMC.BattleDrones.files;

import me.MathiasMC.BattleDrones.BattleDrones;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class GUIFolder {

    private final BattleDrones plugin;

    final File shop;
    final File shop_energy;
    final File shop_kinetic;
    final File shop_explode;
    final File shop_protective;
    final File shop_special;

    final File player;
    final File player_energy;
    final File player_kinetic;
    final File player_explode;
    final File player_protective;
    final File player_special;

    final File laser;
    final File laser_whitelist;
    final File laser_ammo;

    final File rocket;
    final File rocket_whitelist;
    final File rocket_ammo;

    final File machine_gun;
    final File machine_gun_whitelist;
    final File machine_gun_ammo;

    final File shield_generator;
    final File shield_generator_whitelist;
    final File shield_generator_ammo;

    final File healing;
    final File healing_whitelist;
    final File healing_ammo;

    final File flamethrower;
    final File flamethrower_whitelist;
    final File flamethrower_ammo;

    final File faf_missile;
    final File faf_missile_whitelist;
    final File faf_missile_ammo;

    final File mortar;
    final File mortar_whitelist;
    final File mortar_ammo;

    final File lightning;
    final File lightning_whitelist;
    final File lightning_ammo;

    public GUIFolder(final BattleDrones plugin) {
        this.plugin = plugin;
        File folder = new File(plugin.getDataFolder() + File.separator + "gui");
        if (!folder.exists()) {
            folder.mkdir();
        }
        File folderShop = new File(folder + File.separator + "shop");
        if (!folderShop.exists()) {
            folderShop.mkdir();
        }
        File folderPlayer = new File(folder + File.separator + "player");
        if (!folderPlayer.exists()) {
            folderPlayer.mkdir();
        }
        File folderLaser = new File(folder + File.separator + "laser");
        if (!folderLaser.exists()) {
            folderLaser.mkdir();
        }
        File folderRocket = new File(folder + File.separator + "rocket");
        if (!folderRocket.exists()) {
            folderRocket.mkdir();
        }
        File folderMachineGun = new File(folder + File.separator + "machine_gun");
        if (!folderMachineGun.exists()) {
            folderMachineGun.mkdir();
        }
        File folderShieldGenerator = new File(folder + File.separator + "shield_generator");
        if (!folderShieldGenerator.exists()) {
            folderShieldGenerator.mkdir();
        }
        File folderHealing = new File(folder + File.separator + "healing");
        if (!folderHealing.exists()) {
            folderHealing.mkdir();
        }
        File folderFlamethrower = new File(folder + File.separator + "flamethrower");
        if (!folderFlamethrower.exists()) {
            folderFlamethrower.mkdir();
        }
        File folderFAFMissile = new File(folder + File.separator + "faf_missile");
        if (!folderFAFMissile.exists()) {
            folderFAFMissile.mkdir();
        }
        File folderMortar = new File(folder + File.separator + "mortar");
        if (!folderMortar.exists()) {
            folderMortar.mkdir();
        }
        File folderLightning = new File(folder + File.separator + "lightning");
        if (!folderLightning.exists()) {
            folderLightning.mkdir();
        }
        shop = new File(folderShop, "shop.yml");
        if (!shop.exists()) {
            try {
                shop.createNewFile();
                plugin.copy("gui/shop/shop.yml", shop);
            } catch (IOException exception) {
                plugin.textUtils.exception(exception.getStackTrace(), exception.getMessage());
            }
        }
        shop_energy = new File(folderShop, "energy.yml");
        if (!shop_energy.exists()) {
            try {
                shop_energy.createNewFile();
                plugin.copy("gui/shop/energy.yml", shop_energy);
            } catch (IOException exception) {
                plugin.textUtils.exception(exception.getStackTrace(), exception.getMessage());
            }
        }
        shop_kinetic = new File(folderShop, "kinetic.yml");
        if (!shop_kinetic.exists()) {
            try {
                shop_kinetic.createNewFile();
                plugin.copy("gui/shop/kinetic.yml", shop_kinetic);
            } catch (IOException exception) {
                plugin.textUtils.exception(exception.getStackTrace(), exception.getMessage());
            }
        }
        shop_explode = new File(folderShop, "explode.yml");
        if (!shop_explode.exists()) {
            try {
                shop_explode.createNewFile();
                plugin.copy("gui/shop/explode.yml", shop_explode);
            } catch (IOException exception) {
                plugin.textUtils.exception(exception.getStackTrace(), exception.getMessage());
            }
        }
        shop_protective = new File(folderShop, "protective.yml");
        if (!shop_protective.exists()) {
            try {
                shop_protective.createNewFile();
                plugin.copy("gui/shop/protective.yml", shop_protective);
            } catch (IOException exception) {
                plugin.textUtils.exception(exception.getStackTrace(), exception.getMessage());
            }
        }
        shop_special = new File(folderShop, "special.yml");
        if (!shop_special.exists()) {
            try {
                shop_special.createNewFile();
                plugin.copy("gui/shop/special.yml", shop_special);
            } catch (IOException exception) {
                plugin.textUtils.exception(exception.getStackTrace(), exception.getMessage());
            }
        }
        player = new File(folderPlayer, "player.yml");
        if (!player.exists()) {
            try {
                player.createNewFile();
                plugin.copy("gui/player/player.yml", player);
            } catch (IOException exception) {
                plugin.textUtils.exception(exception.getStackTrace(), exception.getMessage());
            }
        }
        player_energy = new File(folderPlayer, "energy.yml");
        if (!player_energy.exists()) {
            try {
                player_energy.createNewFile();
                plugin.copy("gui/player/energy.yml", player_energy);
            } catch (IOException exception) {
                plugin.textUtils.exception(exception.getStackTrace(), exception.getMessage());
            }
        }
        player_kinetic = new File(folderPlayer, "kinetic.yml");
        if (!player_kinetic.exists()) {
            try {
                player_kinetic.createNewFile();
                plugin.copy("gui/player/kinetic.yml", player_kinetic);
            } catch (IOException exception) {
                plugin.textUtils.exception(exception.getStackTrace(), exception.getMessage());
            }
        }
        player_explode = new File(folderPlayer, "explode.yml");
        if (!player_explode.exists()) {
            try {
                player_explode.createNewFile();
                plugin.copy("gui/player/explode.yml", player_explode);
            } catch (IOException exception) {
                plugin.textUtils.exception(exception.getStackTrace(), exception.getMessage());
            }
        }
        player_protective = new File(folderPlayer, "protective.yml");
        if (!player_protective.exists()) {
            try {
                player_protective.createNewFile();
                plugin.copy("gui/player/protective.yml", player_protective);
            } catch (IOException exception) {
                plugin.textUtils.exception(exception.getStackTrace(), exception.getMessage());
            }
        }
        player_special = new File(folderPlayer, "special.yml");
        if (!player_special.exists()) {
            try {
                player_special.createNewFile();
                plugin.copy("gui/player/special.yml", player_special);
            } catch (IOException exception) {
                plugin.textUtils.exception(exception.getStackTrace(), exception.getMessage());
            }
        }
        laser = new File(folderLaser, "laser.yml");
        if (!laser.exists()) {
            try {
                laser.createNewFile();
                plugin.copy("gui/laser/laser.yml", laser);
            } catch (IOException exception) {
                plugin.textUtils.exception(exception.getStackTrace(), exception.getMessage());
            }
        }
        laser_whitelist = new File(folderLaser, "whitelist.yml");
        if (!laser_whitelist.exists()) {
            try {
                laser_whitelist.createNewFile();
                plugin.copy("gui/laser/whitelist.yml", laser_whitelist);
            } catch (IOException exception) {
                plugin.textUtils.exception(exception.getStackTrace(), exception.getMessage());
            }
        }
        laser_ammo = new File(folderLaser, "ammo.yml");
        if (!laser_ammo.exists()) {
            try {
                laser_ammo.createNewFile();
                plugin.copy("gui/laser/ammo.yml", laser_ammo);
            } catch (IOException exception) {
                plugin.textUtils.exception(exception.getStackTrace(), exception.getMessage());
            }
        }
        rocket = new File(folderRocket, "rocket.yml");
        if (!rocket.exists()) {
            try {
                rocket.createNewFile();
                plugin.copy("gui/rocket/rocket.yml", rocket);
            } catch (IOException exception) {
                plugin.textUtils.exception(exception.getStackTrace(), exception.getMessage());
            }
        }
        rocket_whitelist = new File(folderRocket, "whitelist.yml");
        if (!rocket_whitelist.exists()) {
            try {
                rocket_whitelist.createNewFile();
                plugin.copy("gui/rocket/whitelist.yml", rocket_whitelist);
            } catch (IOException exception) {
                plugin.textUtils.exception(exception.getStackTrace(), exception.getMessage());
            }
        }
        rocket_ammo = new File(folderRocket, "ammo.yml");
        if (!rocket_ammo.exists()) {
            try {
                rocket_ammo.createNewFile();
                plugin.copy("gui/rocket/ammo.yml", rocket_ammo);
            } catch (IOException exception) {
                plugin.textUtils.exception(exception.getStackTrace(), exception.getMessage());
            }
        }
        machine_gun = new File(folderMachineGun, "machine_gun.yml");
        if (!machine_gun.exists()) {
            try {
                machine_gun.createNewFile();
                plugin.copy("gui/machine_gun/machine_gun.yml", machine_gun);
            } catch (IOException exception) {
                plugin.textUtils.exception(exception.getStackTrace(), exception.getMessage());
            }
        }
        machine_gun_whitelist = new File(folderMachineGun, "whitelist.yml");
        if (!machine_gun_whitelist.exists()) {
            try {
                machine_gun_whitelist.createNewFile();
                plugin.copy("gui/machine_gun/whitelist.yml", machine_gun_whitelist);
            } catch (IOException exception) {
                plugin.textUtils.exception(exception.getStackTrace(), exception.getMessage());
            }
        }
        machine_gun_ammo = new File(folderMachineGun, "ammo.yml");
        if (!machine_gun_ammo.exists()) {
            try {
                machine_gun_ammo.createNewFile();
                plugin.copy("gui/machine_gun/ammo.yml", machine_gun_ammo);
            } catch (IOException exception) {
                plugin.textUtils.exception(exception.getStackTrace(), exception.getMessage());
            }
        }
        shield_generator = new File(folderShieldGenerator, "shield_generator.yml");
        if (!shield_generator.exists()) {
            try {
                shield_generator.createNewFile();
                plugin.copy("gui/shield_generator/shield_generator.yml", shield_generator);
            } catch (IOException exception) {
                plugin.textUtils.exception(exception.getStackTrace(), exception.getMessage());
            }
        }
        shield_generator_whitelist = new File(folderShieldGenerator, "whitelist.yml");
        if (!shield_generator_whitelist.exists()) {
            try {
                shield_generator_whitelist.createNewFile();
                plugin.copy("gui/shield_generator/whitelist.yml", shield_generator_whitelist);
            } catch (IOException exception) {
                plugin.textUtils.exception(exception.getStackTrace(), exception.getMessage());
            }
        }
        shield_generator_ammo = new File(folderShieldGenerator, "ammo.yml");
        if (!shield_generator_ammo.exists()) {
            try {
                shield_generator_ammo.createNewFile();
                plugin.copy("gui/shield_generator/ammo.yml", shield_generator_ammo);
            } catch (IOException exception) {
                plugin.textUtils.exception(exception.getStackTrace(), exception.getMessage());
            }
        }
        healing = new File(folderHealing, "healing.yml");
        if (!healing.exists()) {
            try {
                healing.createNewFile();
                plugin.copy("gui/healing/healing.yml", healing);
            } catch (IOException exception) {
                plugin.textUtils.exception(exception.getStackTrace(), exception.getMessage());
            }
        }
        healing_whitelist = new File(folderHealing, "whitelist.yml");
        if (!healing_whitelist.exists()) {
            try {
                healing_whitelist.createNewFile();
                plugin.copy("gui/healing/whitelist.yml", healing_whitelist);
            } catch (IOException exception) {
                plugin.textUtils.exception(exception.getStackTrace(), exception.getMessage());
            }
        }
        healing_ammo = new File(folderHealing, "ammo.yml");
        if (!healing_ammo.exists()) {
            try {
                healing_ammo.createNewFile();
                plugin.copy("gui/healing/ammo.yml", healing_ammo);
            } catch (IOException exception) {
                plugin.textUtils.exception(exception.getStackTrace(), exception.getMessage());
            }
        }
        flamethrower = new File(folderFlamethrower, "flamethrower.yml");
        if (!flamethrower.exists()) {
            try {
                flamethrower.createNewFile();
                plugin.copy("gui/flamethrower/flamethrower.yml", flamethrower);
            } catch (IOException exception) {
                plugin.textUtils.exception(exception.getStackTrace(), exception.getMessage());
            }
        }
        flamethrower_whitelist = new File(folderFlamethrower, "whitelist.yml");
        if (!flamethrower_whitelist.exists()) {
            try {
                flamethrower_whitelist.createNewFile();
                plugin.copy("gui/flamethrower/whitelist.yml", flamethrower_whitelist);
            } catch (IOException exception) {
                plugin.textUtils.exception(exception.getStackTrace(), exception.getMessage());
            }
        }
        flamethrower_ammo = new File(folderFlamethrower, "ammo.yml");
        if (!flamethrower_ammo.exists()) {
            try {
                flamethrower_ammo.createNewFile();
                plugin.copy("gui/flamethrower/ammo.yml", flamethrower_ammo);
            } catch (IOException exception) {
                plugin.textUtils.exception(exception.getStackTrace(), exception.getMessage());
            }
        }
        faf_missile = new File(folderFAFMissile, "faf_missile.yml");
        if (!faf_missile.exists()) {
            try {
                faf_missile.createNewFile();
                plugin.copy("gui/faf_missile/faf_missile.yml", faf_missile);
            } catch (IOException exception) {
                plugin.textUtils.exception(exception.getStackTrace(), exception.getMessage());
            }
        }
        faf_missile_whitelist = new File(folderFAFMissile, "whitelist.yml");
        if (!faf_missile_whitelist.exists()) {
            try {
                faf_missile_whitelist.createNewFile();
                plugin.copy("gui/faf_missile/whitelist.yml", faf_missile_whitelist);
            } catch (IOException exception) {
                plugin.textUtils.exception(exception.getStackTrace(), exception.getMessage());
            }
        }
        faf_missile_ammo = new File(folderFAFMissile, "ammo.yml");
        if (!faf_missile_ammo.exists()) {
            try {
                faf_missile_ammo.createNewFile();
                plugin.copy("gui/faf_missile/ammo.yml", faf_missile_ammo);
            } catch (IOException exception) {
                plugin.textUtils.exception(exception.getStackTrace(), exception.getMessage());
            }
        }
        mortar = new File(folderMortar, "mortar.yml");
        if (!mortar.exists()) {
            try {
                mortar.createNewFile();
                plugin.copy("gui/mortar/mortar.yml", mortar);
            } catch (IOException exception) {
                plugin.textUtils.exception(exception.getStackTrace(), exception.getMessage());
            }
        }
        mortar_whitelist = new File(folderMortar, "whitelist.yml");
        if (!mortar_whitelist.exists()) {
            try {
                mortar_whitelist.createNewFile();
                plugin.copy("gui/mortar/whitelist.yml", mortar_whitelist);
            } catch (IOException exception) {
                plugin.textUtils.exception(exception.getStackTrace(), exception.getMessage());
            }
        }
        mortar_ammo = new File(folderMortar, "ammo.yml");
        if (!mortar_ammo.exists()) {
            try {
                mortar_ammo.createNewFile();
                plugin.copy("gui/mortar/ammo.yml", mortar_ammo);
            } catch (IOException exception) {
                plugin.textUtils.exception(exception.getStackTrace(), exception.getMessage());
            }
        }
        lightning = new File(folderLightning, "lightning.yml");
        if (!lightning.exists()) {
            try {
                lightning.createNewFile();
                plugin.copy("gui/lightning/lightning.yml", lightning);
            } catch (IOException exception) {
                plugin.textUtils.exception(exception.getStackTrace(), exception.getMessage());
            }
        }
        lightning_whitelist = new File(folderLightning, "whitelist.yml");
        if (!lightning_whitelist.exists()) {
            try {
                lightning_whitelist.createNewFile();
                plugin.copy("gui/lightning/whitelist.yml", lightning_whitelist);
            } catch (IOException exception) {
                plugin.textUtils.exception(exception.getStackTrace(), exception.getMessage());
            }
        }
        lightning_ammo = new File(folderLightning, "ammo.yml");
        if (!lightning_ammo.exists()) {
            try {
                lightning_ammo.createNewFile();
                plugin.copy("gui/lightning/ammo.yml", lightning_ammo);
            } catch (IOException exception) {
                plugin.textUtils.exception(exception.getStackTrace(), exception.getMessage());
            }
        }
        load();
    }

    public void load() {
        plugin.guiFiles.put("shop", YamlConfiguration.loadConfiguration(shop));
        plugin.guiFiles.put("shop_energy", YamlConfiguration.loadConfiguration(shop_energy));
        plugin.guiFiles.put("shop_kinetic", YamlConfiguration.loadConfiguration(shop_kinetic));
        plugin.guiFiles.put("shop_explode", YamlConfiguration.loadConfiguration(shop_explode));
        plugin.guiFiles.put("shop_protective", YamlConfiguration.loadConfiguration(shop_protective));
        plugin.guiFiles.put("shop_special", YamlConfiguration.loadConfiguration(shop_special));

        plugin.guiFiles.put("player", YamlConfiguration.loadConfiguration(player));
        plugin.guiFiles.put("player_energy", YamlConfiguration.loadConfiguration(player_energy));
        plugin.guiFiles.put("player_kinetic", YamlConfiguration.loadConfiguration(player_kinetic));
        plugin.guiFiles.put("player_explode", YamlConfiguration.loadConfiguration(player_explode));
        plugin.guiFiles.put("player_protective", YamlConfiguration.loadConfiguration(player_protective));
        plugin.guiFiles.put("player_special", YamlConfiguration.loadConfiguration(player_special));

        plugin.guiFiles.put("laser", YamlConfiguration.loadConfiguration(laser));
        plugin.guiFiles.put("laser_whitelist", YamlConfiguration.loadConfiguration(laser_whitelist));
        plugin.guiFiles.put("laser_ammo", YamlConfiguration.loadConfiguration(laser_ammo));

        plugin.guiFiles.put("rocket", YamlConfiguration.loadConfiguration(rocket));
        plugin.guiFiles.put("rocket_whitelist", YamlConfiguration.loadConfiguration(rocket_whitelist));
        plugin.guiFiles.put("rocket_ammo", YamlConfiguration.loadConfiguration(rocket_ammo));

        plugin.guiFiles.put("machine_gun", YamlConfiguration.loadConfiguration(machine_gun));
        plugin.guiFiles.put("machine_gun_whitelist", YamlConfiguration.loadConfiguration(machine_gun_whitelist));
        plugin.guiFiles.put("machine_gun_ammo", YamlConfiguration.loadConfiguration(machine_gun_ammo));

        plugin.guiFiles.put("shield_generator", YamlConfiguration.loadConfiguration(shield_generator));
        plugin.guiFiles.put("shield_generator_whitelist", YamlConfiguration.loadConfiguration(shield_generator_whitelist));
        plugin.guiFiles.put("shield_generator_ammo", YamlConfiguration.loadConfiguration(shield_generator_ammo));

        plugin.guiFiles.put("healing", YamlConfiguration.loadConfiguration(healing));
        plugin.guiFiles.put("healing_whitelist", YamlConfiguration.loadConfiguration(healing_whitelist));
        plugin.guiFiles.put("healing_ammo", YamlConfiguration.loadConfiguration(healing_ammo));

        plugin.guiFiles.put("flamethrower", YamlConfiguration.loadConfiguration(flamethrower));
        plugin.guiFiles.put("flamethrower_whitelist", YamlConfiguration.loadConfiguration(flamethrower_whitelist));
        plugin.guiFiles.put("flamethrower_ammo", YamlConfiguration.loadConfiguration(flamethrower_ammo));

        plugin.guiFiles.put("faf_missile", YamlConfiguration.loadConfiguration(faf_missile));
        plugin.guiFiles.put("faf_missile_whitelist", YamlConfiguration.loadConfiguration(faf_missile_whitelist));
        plugin.guiFiles.put("faf_missile_ammo", YamlConfiguration.loadConfiguration(faf_missile_ammo));

        plugin.guiFiles.put("mortar", YamlConfiguration.loadConfiguration(mortar));
        plugin.guiFiles.put("mortar_whitelist", YamlConfiguration.loadConfiguration(mortar_whitelist));
        plugin.guiFiles.put("mortar_ammo", YamlConfiguration.loadConfiguration(mortar_ammo));

        plugin.guiFiles.put("lightning", YamlConfiguration.loadConfiguration(lightning));
        plugin.guiFiles.put("lightning_whitelist", YamlConfiguration.loadConfiguration(lightning_whitelist));
        plugin.guiFiles.put("lightning_ammo", YamlConfiguration.loadConfiguration(lightning_ammo));
    }
}