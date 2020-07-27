package me.MathiasMC.BattleDrones.files;

import me.MathiasMC.BattleDrones.BattleDrones;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class Particles {

    public FileConfiguration get;
    private final File file;

    public Particles(final BattleDrones plugin) {
        file = new File(plugin.getDataFolder(), "particles.yml");
        if (!file.exists()) {
            try {
                if (file.createNewFile()) {
                    plugin.copy("particles.yml", file);
                    plugin.textUtils.info("particles.yml ( A change was made )");
                } else {
                    plugin.textUtils.info("particles.yml ( Could not create file )");
                }
            } catch (IOException exception) {
                plugin.textUtils.exception(exception.getStackTrace(), exception.getMessage());
            }
        } else {
            plugin.textUtils.info("particles.yml ( Loaded )");
        }
        load();
    }

    public void load() {
        get = YamlConfiguration.loadConfiguration(file);
    }
}