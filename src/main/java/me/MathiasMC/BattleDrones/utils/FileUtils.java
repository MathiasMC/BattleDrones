package me.MathiasMC.BattleDrones.utils;

import com.google.common.io.ByteStreams;
import me.MathiasMC.BattleDrones.BattleDrones;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

public class FileUtils {

    private final BattleDrones plugin;

    private final File configFile;
    public FileConfiguration config;

    private final File particlesFile;
    public FileConfiguration particles;

    private final File languageFile;
    public FileConfiguration language;

    private final File headsFile;
    public FileConfiguration heads;

    private final File guiFolder;

    private final File dronesFolder;

    private final File guiPlayersFolder;

    private final File guiShopFolder;

    public final HashMap<String, File> droneFiles = new HashMap<>();
    public final HashMap<String, File> guiFiles = new HashMap<>();

    public FileUtils(BattleDrones plugin) {
        this.plugin = plugin;
        File pluginFolder = getFolder(plugin.getDataFolder().getPath());
        this.configFile = copyFile(plugin, pluginFolder, "config.yml", "config.yml");
        this.particlesFile = copyFile(plugin, pluginFolder, "particles.yml", "particles.yml");
        this.languageFile = copyFile(plugin, pluginFolder, "language.yml", "language.yml");
        this.headsFile = copyFile(plugin, pluginFolder, "heads.yml", "heads.yml");
        loadConfig();
        loadParticles();
        loadLanguage();
        loadHeads();
        this.guiFolder = getFolder(pluginFolder + File.separator + "gui");
        this.dronesFolder = getFolder(pluginFolder + File.separator + "drones");
        this.guiPlayersFolder = getFolder(guiFolder + File.separator + "player");
        this.guiShopFolder = getFolder(guiFolder + File.separator + "shop");
        guiFiles.put("player", copyFile(plugin, guiPlayersFolder, "player.yml", "gui/player/player.yml"));
        guiFiles.put("shop", copyFile(plugin, guiShopFolder, "shop.yml", "gui/shop/shop.yml"));
    }

    public void initialize(Plugin registeredPlugin, String droneName, String droneCategory) {
        droneFiles.put(droneName, copyFile(registeredPlugin, getFolder(dronesFolder + File.separator + droneCategory), droneName + ".yml", "drones/" + droneCategory + "/" + droneName + ".yml"));
        File guiCategoryFolder = getFolder(guiFolder + File.separator + droneName);
        guiFiles.put(droneName, copyFile(registeredPlugin, guiCategoryFolder, droneName + ".yml", "gui/" + droneName + "/" + droneName + ".yml"));
        guiFiles.put(droneName + "_ammo", copyFile(registeredPlugin, guiCategoryFolder, "ammo.yml", "gui/" + droneName + "/" + "ammo.yml"));
        guiFiles.put(droneName + "_whitelist", copyFile(registeredPlugin, guiCategoryFolder, "whitelist.yml", "gui/" + droneName + "/" + "whitelist.yml"));
        guiFiles.put("player_" + droneCategory, copyFile(registeredPlugin, guiPlayersFolder, droneCategory + ".yml", "gui/player/" + droneCategory + ".yml"));
        guiFiles.put("shop_" + droneCategory, copyFile(registeredPlugin, guiShopFolder, droneCategory + ".yml", "gui/shop/" + droneCategory + ".yml"));
    }

    public void loadConfig() {
        config = YamlConfiguration.loadConfiguration(configFile);
    }

    public void loadLanguage() {
        language = YamlConfiguration.loadConfiguration(languageFile);
    }

    public void loadParticles() {
        particles = YamlConfiguration.loadConfiguration(particlesFile);
    }

    public void loadHeads() {
        heads = YamlConfiguration.loadConfiguration(headsFile);
    }

    public File copyFile(Plugin registeredPlugin, File folder, String filePath, String path) {
        File file = new File(folder, filePath);
        if (!file.exists()) {
            try {
                file.createNewFile();
                try {
                    ByteStreams.copy(registeredPlugin.getResource(path), new FileOutputStream(file));
                } catch (NullPointerException e) {
                    plugin.getTextUtils().info("cant find: " + path);
                }
            } catch (IOException exception) {
                plugin.getTextUtils().error("Could not create file " + path);
            }
        }
        return file;
    }

    public File getFolder(String path) {
        File file = new File(path);
        if (!file.exists()) {
            file.mkdir();
        }
        return file;
    }

    public void loadDroneFiles() {
        for (String drone : droneFiles.keySet()) {
            plugin.droneFiles.put(drone, YamlConfiguration.loadConfiguration(droneFiles.get(drone)));
        }
    }

    public void loadGUIFiles() {
        for (String drone : guiFiles.keySet()) {
            plugin.guiFiles.put(drone, YamlConfiguration.loadConfiguration(guiFiles.get(drone)));
        }
    }

    public List<String> getBlockCheck(FileConfiguration file, String path) {
        if (file.contains(path + "block-check")) {
            return file.getStringList(path + "block-check");
        }
        return null;
    }
}