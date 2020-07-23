package me.MathiasMC.BattleDrones.files;

import me.MathiasMC.BattleDrones.BattleDrones;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class Language {

    public FileConfiguration get;
    private final File file;

    public Language(final BattleDrones plugin) {
        file = new File(plugin.getDataFolder(), "language.yml");
        if (!file.exists()) {
            try {
                if (file.createNewFile()) {
                    plugin.copy("language.yml", file);
                    plugin.textUtils.info("language.yml ( A change was made )");
                } else {
                    plugin.textUtils.info("language.yml ( Could not create file )");
                }
            } catch (IOException exception) {
                plugin.textUtils.exception(exception.getStackTrace(), exception.getMessage());
            }
        } else {
            plugin.textUtils.info("language.yml ( Loaded )");
        }
        load();
    }

    public void load() {
        get = YamlConfiguration.loadConfiguration(file);
    }
}