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
import org.bukkit.*;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;
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

    //Managers API
    private ItemStackManager itemStackManager;
    private CalculateManager calculateManager;
    private DroneManager droneManager;
    private ParticleManager particleManager;
    private DroneControllerManager droneControllerManager;
    private EntityManager entityManager;
    private PlaceholderManager placeholderManager;
    private TaskManager taskManager;


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
        calculateManager = new CalculateManager(this);
        droneManager = new DroneManager(this);
        particleManager = new ParticleManager(this);
        droneControllerManager = new DroneControllerManager(this);
        entityManager = new EntityManager(this);
        placeholderManager = new PlaceholderManager(this);
        taskManager = new TaskManager(this);

        support = new Support(this);

        if (database.set()) {
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

            new MetricsLite(this, 8224);
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
            if (fileUtils.config.getBoolean("cleanup")) {
                cleanUP();
            }
            if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
                new PlaceholderAPI(this).register();
                textUtils.info("PlaceholderAPI (found)");
            }


        } else {
            textUtils.error("Disabling plugin cannot connect to database");
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        for (String uuid : listPlayerConnect()) {
            getPlayerConnect(uuid).stopDrone(true, true);
        }
        try {
            database.close();
        } catch (SQLException exception) {
            textUtils.exception(exception.getStackTrace(), exception.getMessage());
        }
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

    public void unloadPlayerConnect(final String uuid) {
        final PlayerConnect playerConnect = this.playerConnect.remove(uuid);
        if (playerConnect != null) {
            playerConnect.save();
        }
    }

    public void unloadDroneHolder(final String uuid) {
        if (droneHolder.containsKey(uuid)) {
            for (String drone : this.droneHolder.get(uuid).keySet()) {
                droneHolder.get(uuid).get(drone).save();
            }
        }
        droneHolder.remove(uuid);
    }

    public PlayerConnect getPlayerConnect(final String uuid) {
        if (playerConnect.containsKey(uuid)) {
            return playerConnect.get(uuid);
        }
        final PlayerConnect playerConnect = new PlayerConnect(uuid);
        this.playerConnect.put(uuid, playerConnect);
        return playerConnect;
    }

    public DroneHolder getDroneHolder(final String uuid, final String drone) {
        if (!droneHolder.containsKey(uuid)) {
            droneHolder.put(uuid, new HashMap<>());
        }
        final HashMap<String, DroneHolder> map = droneHolder.get(uuid);
        if (map.containsKey(drone)) {
            return droneHolder.get(uuid).get(drone);
        }
        final DroneHolder droneHolder = new DroneHolder(uuid, drone);
        map.put(drone, droneHolder);
        this.droneHolder.put(uuid, map);
        return droneHolder;
    }

    public Set<String> listPlayerConnect() {
        return playerConnect.keySet();
    }

    public Set<String> listDroneHolder() {
        return droneHolder.keySet();
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

    private void cleanUP() {
        final ArrayList<ArmorStand> armorStands = new ArrayList<>();
        final NamespacedKey namespacedKey = new NamespacedKey(this, "drone_uuid");
        for (World world : getServer().getWorlds()) {
            for (Entity entity : world.getEntities()) {
                if (entity instanceof ArmorStand) {
                    final ArmorStand armorStand = (ArmorStand) entity;
                    final String key = armorStand.getPersistentDataContainer().get(namespacedKey, PersistentDataType.STRING);
                    if (key != null) {
                        armorStands.add(armorStand);
                        armorStand.remove();
                    }
                }
            }
        }
        textUtils.info("CleanUP found: ( " + armorStands.size() + " ) drones removed.");
        armorStands.clear();
    }

    public void addHeads() {
        for (String head : fileUtils.heads.getConfigurationSection("").getKeys(false)) {
            drone_heads.put(head, itemStackManager.getHeadTexture(fileUtils.heads.getString(head)));
        }
        textUtils.info("Loaded ( " + fileUtils.heads.getConfigurationSection("").getKeys(false).size() + " ) heads");
    }
}