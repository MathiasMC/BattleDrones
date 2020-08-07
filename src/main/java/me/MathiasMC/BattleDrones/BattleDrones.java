package me.MathiasMC.BattleDrones;

import com.google.common.io.ByteStreams;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import me.MathiasMC.BattleDrones.commands.BattleDrones_Command;
import me.MathiasMC.BattleDrones.commands.BattleDrones_TabComplete;
import me.MathiasMC.BattleDrones.data.Database;
import me.MathiasMC.BattleDrones.data.DroneHolder;
import me.MathiasMC.BattleDrones.data.PlayerConnect;
import me.MathiasMC.BattleDrones.drones.*;
import me.MathiasMC.BattleDrones.files.*;
import me.MathiasMC.BattleDrones.gui.Menu;
import me.MathiasMC.BattleDrones.listeners.*;
import me.MathiasMC.BattleDrones.managers.*;
import me.MathiasMC.BattleDrones.placeholders.InternalPlaceholders;
import me.MathiasMC.BattleDrones.placeholders.MVdWPlaceholderAPI;
import me.MathiasMC.BattleDrones.placeholders.PlaceholderAPI;
import me.MathiasMC.BattleDrones.support.LocationSupport;
import me.MathiasMC.BattleDrones.utils.MetricsLite;
import me.MathiasMC.BattleDrones.utils.TextUtils;
import me.MathiasMC.BattleDrones.utils.UpdateUtils;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.RegisteredServiceProvider;
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
    public Particles particles;

    public DronesFolder dronesFolder;
    public GUIFolder guiFolder;

    public ArmorStandManager armorStandManager;
    public GUIManager guiManager;
    public CalculateManager calculateManager;
    public DroneManager droneManager;
    public AIManager aiManager;
    public ParticleManager particleManager;

    public Rocket rocket;
    public ShieldGenerator shieldGenerator;
    public Healing healing;
    public Flamethrower flamethrower;
    public Gun gun;

    public InternalPlaceholders internalPlaceholders;

    private final Map<String, PlayerConnect> playerConnect = new HashMap<>();
    private final Map<String, HashMap<String, DroneHolder>> droneHolder = new HashMap<>();
    private final HashMap<Player, Menu> playerMenu = new HashMap<>();

    public final HashMap<String, FileConfiguration> droneFiles = new HashMap<>();
    public final HashMap<String, FileConfiguration> guiFiles = new HashMap<>();
    public final HashMap<String, String> drone_whitelist = new HashMap<>();
    public final HashMap<String, ItemStack> drone_heads = new HashMap<>();
    public final HashMap<String, LivingEntity> drone_targets = new HashMap<>();
    public final HashSet<String> drone_players = new HashSet<>();
    public final ArrayList<String> drones = new ArrayList<>();
    public final HashSet<String> drone_amount = new HashSet<>();
    public final HashSet<ArmorStand> projectiles = new HashSet<>();
    public final HashSet<String> park = new HashSet<>();

    public LocationSupport locationSupport;

    private Economy econ = null;

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
        drones.add("flamethrower");
        drones.add("faf_missile");
        drones.add("mortar");

        textUtils = new TextUtils(this);
        config = new Config(this);
        language = new Language(this);
        particles = new Particles(this);

        dronesFolder = new DronesFolder(this);
        guiFolder = new GUIFolder(this);

        armorStandManager = new ArmorStandManager(this);
        guiManager = new GUIManager(this);
        calculateManager = new CalculateManager(this);
        droneManager = new DroneManager(this);
        aiManager = new AIManager(this);
        particleManager = new ParticleManager(this);

        internalPlaceholders = new InternalPlaceholders(this);

        rocket = new Rocket(this);
        shieldGenerator = new ShieldGenerator(this);
        healing = new Healing(this);
        flamethrower = new Flamethrower(this);
        gun = new Gun(this);

        locationSupport = new LocationSupport(this);

        database = new Database(this);
        if (database.set()) {
            database.loadOnlinePlayers();
            getServer().getPluginManager().registerEvents(new PlayerLogin(this), this);
            getServer().getPluginManager().registerEvents(new PlayerQuit(this), this);
            getServer().getPluginManager().registerEvents(new InventoryClick(), this);
            getServer().getPluginManager().registerEvents(new AsyncPlayerChat(this), this);
            getServer().getPluginManager().registerEvents(new PlayerInteract(this), this);
            getServer().getPluginManager().registerEvents(new PlayerInteractAtEntity(this), this);
            getServer().getPluginManager().registerEvents(new PlayerDeath(this), this);
            getServer().getPluginManager().registerEvents(new EntityDamageByEntity(this), this);
            getServer().getPluginManager().registerEvents(new PlayerChangedWorld(this), this);
            getServer().getPluginManager().registerEvents(new PlayerTeleport(this), this);
            getCommand("battledrones").setExecutor(new BattleDrones_Command(this));
            getCommand("battledrones").setTabCompleter(new BattleDrones_TabComplete(this));
            addHeads();
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
            if (config.get.getBoolean("vault")) {
                if (setupEconomy()) {
                    textUtils.info("Vault found");
                }
            }
            particleManager.load();
            if (config.get.getBoolean("cleanup")) {
                droneManager.cleanUP();
            }
            if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
                new PlaceholderAPI(this).register();
                textUtils.info("PlaceholderAPI (found)");
            }
            if (getServer().getPluginManager().getPlugin("MVdWPlaceholderAPI") != null) {
                new MVdWPlaceholderAPI().register(this);
                textUtils.info("MVdWPlaceholderAPI (found)");
            }
        } else {
            textUtils.error("Disabling plugin cannot connect to database");
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        for (String uuid : list()) {
            get(uuid).stopDrone();
        }
        try {
            database.close();
        } catch (SQLException exception) {
            textUtils.exception(exception.getStackTrace(), exception.getMessage());
        }
        call = null;
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        final RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return true;
    }

    public Economy getEconomy() {
        return econ;
    }

    public String replacePlaceholders(final Player player, String message) {
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            message = me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(player, message);
        }
        return message;
    }

    public void load(final String uuid) {
        PlayerConnect playerConnect = new PlayerConnect(uuid);
        this.playerConnect.put(uuid, playerConnect);
    }

    public void loadDroneHolder(final String uuid, final String drone) {
        if (!this.droneHolder.containsKey(uuid)) {
            this.droneHolder.put(uuid, new HashMap<>());
        }
        final HashMap<String, DroneHolder> map = this.droneHolder.get(uuid);
        if (!map.containsKey(drone)) {
            map.put(drone, new DroneHolder(uuid, drone));
            this.droneHolder.put(uuid, map);
        }
    }

    public void unload(final String uuid) {
        PlayerConnect playerConnect = this.playerConnect.remove(uuid);
        if (playerConnect != null) {
            playerConnect.save();
        }
    }

    public void unloadDroneHolder(final String uuid) {
        if (this.droneHolder.containsKey(uuid)) {
            for (String drone : this.droneHolder.get(uuid).keySet()) {
                this.droneHolder.get(uuid).get(drone).save();
            }
        }
        this.droneHolder.remove(uuid);
    }

    public PlayerConnect get(final String uuid) {
        return playerConnect.get(uuid);
    }

    public DroneHolder getDroneHolder(final String uuid, final String drone) {
        return droneHolder.get(uuid).get(drone);
    }

    public HashMap<String, DroneHolder> getDroneHolderUUID(final String uuid) {
        return droneHolder.get(uuid);
    }

    public Set<String> list() {
        return playerConnect.keySet();
    }

    public Set<String> listDroneHolder() {
        return droneHolder.keySet();
    }

    public boolean isInt(final String s) {
        try {
            Integer.parseInt(s);
        } catch(NumberFormatException e) {
            return false;
        }
        return true;
    }

    public boolean isFloat(final String s) {
        try {
            Float.parseFloat(s);
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }

    public double randomDouble(final double min, final double max) {
        return min + Math.random() * (max - min);
    }

    public float randomChance() {
        return new Random().nextFloat();
    }

    public boolean getEntityLook(final Player player, final Entity entity) {
        final Location location = player.getEyeLocation();
        return entity.getLocation().add(0, 1, 0).toVector().subtract(location.toVector()).normalize().dot(location.getDirection()) > 0.95D;
    }

    public Menu getPlayerMenu(final Player player) {
        Menu playerMenu;
        if (!this.playerMenu.containsKey(player)) {
            playerMenu = new Menu(player);
            this.playerMenu.put(player, playerMenu);
            return playerMenu;
        } else {
            return this.playerMenu.get(player);
        }
    }

    public ItemStack getItemStack(final String bb, final int amount) {
        try {
            return new ItemStack(Material.getMaterial(bb), amount);
        } catch (Exception e) {
            return null;
        }
    }

    public boolean isString(final String text) {
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

    public void addHeads() {
        drone_heads.clear();
        for (String head : config.get.getConfigurationSection("heads").getKeys(false)) {
            drone_heads.put(head, setTexture(config.get.getString("heads." + head)));
        }
        textUtils.info("Loaded ( " + config.get.getConfigurationSection("heads").getKeys(false).size() + " ) heads");
    }

    public ItemStack setTexture(final String texture) {
        final ItemStack itemStack = new ItemStack(Material.PLAYER_HEAD, 1);
        final SkullMeta itemMeta = (SkullMeta) itemStack.getItemMeta();
        final GameProfile gameProfile = new GameProfile(UUID.randomUUID(), "");
        gameProfile.getProperties().put("textures", new Property("textures", texture));
        final Field profileField;
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

    public void copy(final String filename, final File file) {
        try {
            ByteStreams.copy(getResource(filename), new FileOutputStream(file));
        } catch (IOException exception) {
            textUtils.exception(exception.getStackTrace(), exception.getMessage());
        }
    }
}