package me.MathiasMC.BattleDrones;

import me.MathiasMC.BattleDrones.api.DroneRegistry;
import me.MathiasMC.BattleDrones.commands.BattleDrones_Command;
import me.MathiasMC.BattleDrones.commands.BattleDrones_TabComplete;
import me.MathiasMC.BattleDrones.data.Database;
import me.MathiasMC.BattleDrones.data.DroneHolder;
import me.MathiasMC.BattleDrones.data.PlayerConnect;
import me.MathiasMC.BattleDrones.external.ExternalDrones;
import me.MathiasMC.BattleDrones.gui.Menu;
import me.MathiasMC.BattleDrones.listeners.*;
import me.MathiasMC.BattleDrones.managers.*;
import me.MathiasMC.BattleDrones.support.PlaceholderAPI;
import me.MathiasMC.BattleDrones.support.Support;
import me.MathiasMC.BattleDrones.utils.*;
import org.bstats.bukkit.Metrics;
import org.bukkit.*;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class BattleDrones extends JavaPlugin {

    private static BattleDrones call;

    public final ConsoleCommandSender consoleSender = Bukkit.getServer().getConsoleSender();

    public Database database;

    private TextUtils textUtils;

    private FileUtils fileUtils;

    // Data
    private final Map<String, PlayerConnect> playerConnect = new HashMap<>();
    private final Map<String, HashMap<String, DroneHolder>> droneHolder = new HashMap<>();
    private final HashMap<Player, Menu> playerMenu = new HashMap<>();

    // Data containers
    public final NamespacedKey droneKey = new NamespacedKey(this, "drone_uuid");
    public final NamespacedKey projectileKey = new NamespacedKey(this, "projectile");
    public final NamespacedKey droneControllerKey = new NamespacedKey(this, "drone_controller");

    // Drone registry
    public final HashMap<String, DroneRegistry> droneRegistry = new HashMap<>();

    // Drone amount map contains all players uuid with an active drone
    public final HashSet<String> drone_amount = new HashSet<>();

    // Drone park map contains all players uuid with an parked drone
    public final HashSet<String> park = new HashSet<>();

    // Drones map the key is the name from the drone config and the value is the internal registered name
    public final HashMap<String, String> drones = new HashMap<>();

    // Drones map the key is the category and list of drones in that category
    public final HashMap<String, List<String>> category = new HashMap<>();

    // Files
    public final HashMap<String, FileConfiguration> droneFiles = new HashMap<>();//
    public final HashMap<String, FileConfiguration> guiFiles = new HashMap<>();//

    private int pruneDroneTaskId;

    //Managers API
    private ItemStackManager itemStackManager;
    private CalculateManager calculateManager;
    private DroneManager droneManager;
    private ParticleManager particleManager;
    private DroneControllerManager droneControllerManager;
    private EntityManager entityManager;
    private PlaceholderManager placeholderManager;
    private TaskManager taskManager;
    private DronePruneManager dronePruneManager;


    public final HashMap<String, String> drone_whitelist = new HashMap<>();//
    public final HashMap<String, ItemStack> drone_heads = new HashMap<>();//
    public final HashMap<String, LivingEntity> drone_targets = new HashMap<>();//
    public final HashSet<String> drone_wait = new HashSet<>();
    public final HashSet<ArmorStand> projectiles = new HashSet<>();
    public final HashSet<String> drone_follow = new HashSet<>();

    private Support support;

    @Override
    public void onEnable() {
        call = this;

        textUtils = new TextUtils(this);
        fileUtils = new FileUtils(this);

        database = new Database(this);

        itemStackManager = new ItemStackManager(this);
        calculateManager = new CalculateManager();
        droneManager = new DroneManager(this);
        particleManager = new ParticleManager(this);
        droneControllerManager = new DroneControllerManager(this);
        entityManager = new EntityManager(this);
        placeholderManager = new PlaceholderManager(this);
        taskManager = new TaskManager(this);
        dronePruneManager = new DronePruneManager(this);

        support = new Support(this);

        if (!database.checkConnection()) {
            textUtils.error("Disabling plugin cannot connect to database");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        getServer().getPluginManager().registerEvents(new PlayerLogin(this), this);
        getServer().getPluginManager().registerEvents(new PlayerQuit(this), this);
        getServer().getPluginManager().registerEvents(new InventoryClick(), this);
        getServer().getPluginManager().registerEvents(new AsyncPlayerChat(this), this);
        getServer().getPluginManager().registerEvents(new PlayerInteract(this), this);
        getServer().getPluginManager().registerEvents(new PlayerInteractAtEntity(this), this);
        getServer().getPluginManager().registerEvents(new PlayerDeath(this), this);
        getServer().getPluginManager().registerEvents(new PlayerChangedWorld(this), this);
        getServer().getPluginManager().registerEvents(new PlayerTeleport(this), this);
        getServer().getPluginManager().registerEvents(new ProjectileHit(this), this);
        getServer().getPluginManager().registerEvents(new PlayerRespawn(this), this);

        new ExternalDrones();

        if (fileUtils.config.getBoolean("swap.use")) {
            getServer().getPluginManager().registerEvents(new PlayerSwapHandItems(this), this);
        }

        getCommand("battledrones").setExecutor(new BattleDrones_Command(this));
        getCommand("battledrones").setTabCompleter(new BattleDrones_TabComplete(this));

        addHeads();

        if (fileUtils.config.getBoolean("update-check")) {
            new UpdateUtils(this, 81850).getVersion(version -> {
                if (this.getDescription().getVersion().equalsIgnoreCase(version)) {
                    textUtils.info("You are using the latest version of BattleDrones (" + getDescription().getVersion() + ")");
                } else {
                    textUtils.warning("Version: " + version + " has been released! you are currently using version: " + getDescription().getVersion());
                }
            });
        }
        particleManager.load();

        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new PlaceholderAPI(this).register();
            textUtils.info("PlaceholderAPI (found)");
        }

        dronePruneManager.start(fileUtils.config.getLong("prune"));

        new Metrics(this, 8224);
    }

    @Override
    public void onDisable() {
        for (String uuid : listPlayerConnect()) {
            //getPlayerConnect(uuid).stopDrone(true, true);
        }
        database.close();
        dronePruneManager.stop();
        call = null;
    }

    public static BattleDrones getInstance() {
        return call;
    }

    public Support getSupport() {
        return this.support;
    }

    public FileUtils getFileUtils() {
        return this.fileUtils;
    }

    public TextUtils getTextUtils() {
        return this.textUtils;
    }

    public EntityManager getEntityManager() {
        return this.entityManager;
    }

    public PlaceholderManager getPlaceholderManager() {
        return this.placeholderManager;
    }

    public ItemStackManager getItemStackManager() {
        return this.itemStackManager;
    }

    public DroneControllerManager getDroneControllerManager() {
        return this.droneControllerManager;
    }

    public CalculateManager getCalculateManager() {
        return this.calculateManager;
    }

    public ParticleManager getParticleManager() {
        return this.particleManager;
    }

    public DroneManager getDroneManager() {
        return this.droneManager;
    }

    public TaskManager getTaskManager() {
        return this.taskManager;
    }

    public DronePruneManager getDronePruneManager() {
        return this.dronePruneManager;
    }

    public void unloadPlayerConnect(String uuid) {
        PlayerConnect playerConnect = this.playerConnect.remove(uuid);
        if (playerConnect != null) {
            playerConnect.save();
        }
    }

    public void unloadDroneHolder(String uuid) {
        Map<String, DroneHolder> map = droneHolder.get(uuid);
        if (map != null) {
            map.values().forEach(DroneHolder::save);
            droneHolder.remove(uuid);
        }
    }

    public PlayerConnect getPlayerConnect(String uuid) {
        return playerConnect.computeIfAbsent(uuid, PlayerConnect::new);
    }

    public DroneHolder getDroneHolder(String uuid, String drone) {
        Map<String, DroneHolder> map = droneHolder.computeIfAbsent(uuid, k -> new HashMap<>());
        return map.computeIfAbsent(drone, d -> new DroneHolder(uuid, d));
    }

    public Set<String> listPlayerConnect() {
        return playerConnect.keySet();
    }

    public Set<String> listDroneHolder() {
        return droneHolder.keySet();
    }

    public Menu getPlayerMenu(Player player) {
        return playerMenu.computeIfAbsent(player, Menu::new);
    }

    public void addHeads() {
        for (String head : fileUtils.heads.getConfigurationSection("").getKeys(false)) {
            drone_heads.put(head, itemStackManager.getHeadTexture(fileUtils.heads.getString(head)));
        }
        textUtils.info("Loaded ( " + fileUtils.heads.getConfigurationSection("").getKeys(false).size() + " ) heads.");
    }
}