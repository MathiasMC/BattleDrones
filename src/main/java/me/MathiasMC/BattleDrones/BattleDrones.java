package me.MathiasMC.BattleDrones;

import com.google.common.io.ByteStreams;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import me.MathiasMC.BattleDrones.commands.BattleDrones_Command;
import me.MathiasMC.BattleDrones.data.Database;
import me.MathiasMC.BattleDrones.data.DroneHolder;
import me.MathiasMC.BattleDrones.data.PlayerConnect;
import me.MathiasMC.BattleDrones.drones.*;
import me.MathiasMC.BattleDrones.files.Config;
import me.MathiasMC.BattleDrones.files.DronesFolder;
import me.MathiasMC.BattleDrones.files.GUIFolder;
import me.MathiasMC.BattleDrones.files.Language;
import me.MathiasMC.BattleDrones.gui.Menu;
import me.MathiasMC.BattleDrones.listeners.*;
import me.MathiasMC.BattleDrones.managers.*;
import me.MathiasMC.BattleDrones.placeholders.InternalPlaceholders;
import me.MathiasMC.BattleDrones.placeholders.PlaceholderAPI;
import me.MathiasMC.BattleDrones.support.WorldGuard;
import me.MathiasMC.BattleDrones.utils.MetricsLite;
import me.MathiasMC.BattleDrones.utils.TextUtils;
import me.MathiasMC.BattleDrones.utils.UpdateUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.*;

public class BattleDrones extends JavaPlugin {

    public static BattleDrones call;

    public final ConsoleCommandSender consoleSender = Bukkit.getServer().getConsoleSender();

    public Database database;

    public TextUtils textUtils;
    public Config config;
    public Language language;

    public DronesFolder dronesFolder;
    public GUIFolder guiFolder;

    public ArmorStandManager armorStandManager;
    public GUIManager guiManager;
    public CalculateManager calculateManager;
    public DroneManager droneManager;
    public AIManager aiManager;

    public Laser laser;
    public Rocket rocket;
    public MachineGun machineGun;
    public ShieldGenerator shieldGenerator;
    public Healing healing;

    public InternalPlaceholders internalPlaceholders;

    private final Map<String, PlayerConnect> playerConnect = new HashMap<>();
    private final Map<String, HashMap<String, DroneHolder>> droneHolder = new HashMap<>();
    public final HashMap<Player, Menu> playerMenu = new HashMap<>();
    public final HashMap<String, FileConfiguration> droneFiles = new HashMap<>();
    public final HashMap<String, FileConfiguration> guiFiles = new HashMap<>();
    public final HashMap<String, String> drone_whitelist = new HashMap<>();
    public HashMap<String, ItemStack> drone_heads = new HashMap<>();

    public HashMap<String, LivingEntity> drone_targets = new HashMap<>();

    public HashSet<String> drone_players = new HashSet<>();

    public ArrayList<String> drones = new ArrayList<>();

    public WorldGuard worldGuard;

    @Override
    public void onEnable() {
        call = this;
        if (!getDataFolder().exists()) {
            getDataFolder().mkdir();
        }
        drones.add("laser");
        drones.add("rocket");
        drones.add("machine_gun");
        drones.add("shield_generator");
        drones.add("healing");
        textUtils = new TextUtils(this);
        config = new Config(this);
        language = new Language(this);
        armorStandManager = new ArmorStandManager(this);
        guiManager = new GUIManager(this);
        laser = new Laser(this);
        rocket = new Rocket(this);
        machineGun = new MachineGun(this);
        shieldGenerator = new ShieldGenerator(this);
        healing = new Healing(this);
        internalPlaceholders = new InternalPlaceholders(this);
        dronesFolder = new DronesFolder(this);
        guiFolder = new GUIFolder(this);
        calculateManager = new CalculateManager(this);
        droneManager = new DroneManager(this);
        aiManager = new AIManager(this);
        worldGuard = new WorldGuard(this);
        database = new Database(this);
        if (database.set()) {
            database.loadOnlinePlayers();
            getServer().getPluginManager().registerEvents(new PlayerLogin(this), this);
            getServer().getPluginManager().registerEvents(new PlayerQuit(this), this);
            getServer().getPluginManager().registerEvents(new InventoryClick(), this);
            getServer().getPluginManager().registerEvents(new AsyncPlayerChat(this), this);
            getServer().getPluginManager().registerEvents(new PlayerInteract(this), this);
            getServer().getPluginManager().registerEvents(new PlayerDeath(this), this);
            getServer().getPluginManager().registerEvents(new EntityDamageByEntity(this), this);
            getCommand("battledrones").setExecutor(new BattleDrones_Command(this));
            addHeads();
            PlaceholderAPI();
            if (config.get.getBoolean("save.use")) {
                saveSchedule();
            }
            new MetricsLite(this, 8224);
            if (config.get.getBoolean("update-check")) {
                new UpdateUtils(this, 81850).getVersion(version -> {
                    if (this.getDescription().getVersion().equalsIgnoreCase(version)) {
                        textUtils.info("You are using the latest version of BattleDrones (" + getDescription().getVersion() + ")");
                    } else {
                        textUtils.warning("Version: " + version + " has been released! you are currently using version: " + getDescription().getVersion());
                    }
                });
            }
        } else {
            textUtils.error("Disabling plugin cannot connect to database");
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        for (String uuid : list()) {
            get(uuid).remove();
        }
        try {
            database.close();
        } catch (SQLException exception) {
            textUtils.exception(exception.getStackTrace(), exception.getMessage());
        }
        call = null;
    }

    public void load(String uuid) {
        PlayerConnect playerConnect = new PlayerConnect(uuid);
        this.playerConnect.put(uuid, playerConnect);
    }

    public void loadDroneHolder(String uuid, String drone) {
        if (!this.droneHolder.containsKey(uuid)) {
            this.droneHolder.put(uuid, new HashMap<>());
        }
        HashMap<String, DroneHolder> map = this.droneHolder.get(uuid);
        if (!map.containsKey(drone)) {
            map.put(drone, new DroneHolder(uuid, drone));
            this.droneHolder.put(uuid, map);
        }
    }

    public void unload(String uuid) {
        PlayerConnect playerConnect = this.playerConnect.remove(uuid);
        if (playerConnect != null) {
            playerConnect.save();
        }
    }

    public void unloadDroneHolder(String uuid) {
        if (this.droneHolder.containsKey(uuid)) {
            for (String drone : this.droneHolder.get(uuid).keySet()) {
                this.droneHolder.get(uuid).get(drone).save();
            }
        }
        this.droneHolder.remove(uuid);
    }

    public PlayerConnect get(String uuid) {
        return playerConnect.get(uuid);
    }

    public DroneHolder getDroneHolder(String uuid, String drone) {
        return droneHolder.get(uuid).get(drone);
    }

    public HashMap<String, DroneHolder> getDroneHolderUUID(String uuid) {
        return droneHolder.get(uuid);
    }

    public Set<String> list() {
        return playerConnect.keySet();
    }

    public Set<String> listDroneHolder() {
        return droneHolder.keySet();
    }

    public boolean isInt(String s) {
        try {
            Integer.parseInt(s);
        } catch(NumberFormatException e) {
            return false;
        }
        return true;
    }

    public boolean isFloat(String s) {
        try {
            Float.parseFloat(s);
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }

    private void PlaceholderAPI() {
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new PlaceholderAPI(this).register();
            textUtils.info("PlaceholderAPI (found) adding placeholders");
        }
    }

    public double randomDouble(final double min, final double max) {
        return min + Math.random() * (max - min);
    }

    public float randomChance() {
        return new Random().nextFloat();
    }

    public Menu getPlayerMenu(Player player) {
        Menu playerMenu;
        if (!this.playerMenu.containsKey(player)) {
            playerMenu = new Menu(player);
            this.playerMenu.put(player, playerMenu);
            return playerMenu;
        } else {
            return this.playerMenu.get(player);
        }
    }

    public ItemStack getItemStack(String bb, int amount) {
        try {
            return new ItemStack(Material.getMaterial(bb), amount);
        } catch (Exception e) {
            return null;
        }
    }

    public boolean isString(String text) {
        return text.matches("^[a-zA-Z]*$");
    }

    private void saveSchedule() {
        final int interval = config.get.getInt("save.interval");
        textUtils.info("Saving to the database every ( " + interval + " ) minutes");
        getServer().getScheduler().runTaskTimerAsynchronously(this, () -> {
            for (String uuid : list()) {
                get(uuid).save();
            }
        }, interval * 1200, interval * 1200);
    }

    private void addHeads() {
        for (String head : config.get.getConfigurationSection("heads").getKeys(false)) {
            drone_heads.put(head, setTexture(config.get.getString("heads." + head)));
        }
        textUtils.info("Loaded ( " + config.get.getConfigurationSection("heads").getKeys(false).size() + " ) heads");
    }

    public ItemStack setTexture(String texture) {
        ItemStack itemStack = new ItemStack(Material.PLAYER_HEAD, 1);
        SkullMeta itemMeta = (SkullMeta) itemStack.getItemMeta();
        GameProfile gameProfile = new GameProfile(UUID.randomUUID(), "");
        gameProfile.getProperties().put("textures", new Property("textures", texture));
        Field profileField;
        try {
            profileField = itemMeta.getClass().getDeclaredField("profile");
            profileField.setAccessible(true);
            profileField.set(itemMeta, gameProfile);
        } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
            textUtils.exception(e.getStackTrace(), e.getMessage());
        }
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    public void copy(String filename, File file) {
        try {
            ByteStreams.copy(getResource(filename), new FileOutputStream(file));
        } catch (IOException exception) {
            textUtils.exception(exception.getStackTrace(), exception.getMessage());
        }
    }
}