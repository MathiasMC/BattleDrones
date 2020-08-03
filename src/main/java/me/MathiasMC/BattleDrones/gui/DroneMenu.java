package me.MathiasMC.BattleDrones.gui;

import me.MathiasMC.BattleDrones.BattleDrones;
import me.MathiasMC.BattleDrones.data.DroneHolder;
import me.MathiasMC.BattleDrones.data.PlayerConnect;
import me.MathiasMC.BattleDrones.gui.player.*;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Objects;

public class DroneMenu extends GUI {

    private final BattleDrones plugin = BattleDrones.call;
    private final FileConfiguration file;
    private final String drone;
    private final Player player = playerMenu.getPlayer();
    private final String uuid = playerMenu.getUuid();

    public DroneMenu(Menu playerMenu, String drone) {
        super(playerMenu);
        this.drone = drone;
        file = plugin.guiFiles.get(drone);
    }

    @Override
    public String getName() {
        return ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(file.getString("settings.name")));
    }

    @Override
    public int getSize() {
        return file.getInt("settings.size");
    }

    @Override
    public void click(InventoryClickEvent e) {
        final int slot = e.getSlot();
        if (file.contains(String.valueOf(slot))) {
            PlayerConnect playerConnect = plugin.get(uuid);
            DroneHolder droneHolder = plugin.getDroneHolder(uuid, drone);
            final FileConfiguration droneFile = plugin.droneFiles.get(drone);
            boolean updateAI = false;
            if (file.getStringList(slot + ".OPTIONS").contains("BACK")) {
                if (drone.equalsIgnoreCase("laser")) {
                    new EnergyGUI(plugin.getPlayerMenu(player)).open();
                } else if (drone.equalsIgnoreCase("machine_gun")) {
                    new KineticGUI(plugin.getPlayerMenu(player)).open();
                } else if (drone.equalsIgnoreCase("rocket") || drone.equalsIgnoreCase("faf_missile") || drone.equalsIgnoreCase("mortar")) {
                    new ExplodeGUI(plugin.getPlayerMenu(player)).open();
                } else if (drone.equalsIgnoreCase("shield_generator") || drone.equalsIgnoreCase("healing")) {
                    new ProtectiveGUI(plugin.getPlayerMenu(player)).open();
                } else if (drone.equalsIgnoreCase("flamethrower")) {
                    new SpecialGUI(plugin.getPlayerMenu(player)).open();
                }
            } else if (file.getStringList(slot + ".OPTIONS").contains("DRONE_MONSTERS")) {
                if (droneHolder.getMonsters() == 1) {
                    droneHolder.setMonsters(0);
                } else {
                    droneHolder.setMonsters(1);
                }
                new DroneMenu(plugin.getPlayerMenu(player), drone).open();
                updateAI = true;
            } else if (file.getStringList(slot + ".OPTIONS").contains("DRONE_ANIMALS")) {
                if (droneHolder.getAnimals() == 1) {
                    droneHolder.setAnimals(0);
                } else {
                    droneHolder.setAnimals(1);
                }
                new DroneMenu(plugin.getPlayerMenu(player), drone).open();
                updateAI = true;
            } else if (file.getStringList(slot + ".OPTIONS").contains("DRONE_PLAYERS")) {
                if (droneHolder.getPlayers() == 1) {
                    droneHolder.setPlayers(0);
                } else {
                    droneHolder.setPlayers(1);
                }
                new DroneMenu(plugin.getPlayerMenu(player), drone).open();
                updateAI = true;
            } else if (file.getStringList(slot + ".OPTIONS").contains("DRONE_WHITELIST")) {
                new WhitelistGUI(plugin.getPlayerMenu(player), drone).open();
            } else if (file.getStringList(slot + ".OPTIONS").contains("DRONE_AMMO")) {
                new AmmoGUI(plugin.getPlayerMenu(player), drone).open();
            } else if (file.getStringList(slot + ".OPTIONS").contains("DRONE_UPGRADE")) {
                FileConfiguration file = plugin.droneFiles.get(drone);
                String path = playerConnect.getGroup() + "." + (droneHolder.getLevel() + 1);
                if (file.contains(path)) {
                    long coins = playerConnect.getCoins();
                    long cost = file.getLong(path + ".cost");
                    if (!plugin.config.get.getBoolean("vault") && coins >= cost ||
                            plugin.config.get.getBoolean("vault") &&
                                    plugin.getEconomy() != null &&
                                    plugin.getEconomy().withdrawPlayer(player, cost).transactionSuccess()) {
                        if (!plugin.config.get.getBoolean("vault")) {
                            playerConnect.setCoins(coins - cost);
                        }
                        droneHolder.setLevel((droneHolder.getLevel() + 1));
                        if (plugin.config.get.getBoolean("update-upgrade") && playerConnect.hasActive()) {
                            playerConnect.stopDrone();
                            plugin.droneManager.spawnDrone(player, drone, true, true);
                        }
                        for (String command : file.getStringList(playerConnect.getGroup() + "." + droneHolder.getLevel() + ".commands.levelup")) {
                            plugin.getServer().dispatchCommand(plugin.consoleSender, command.replace("{player}", player.getName()));
                        }
                        droneHolder.save();
                        new DroneMenu(plugin.getPlayerMenu(player), drone).open();
                    } else {
                        for (String command : file.getStringList(playerConnect.getGroup() + "." + droneHolder.getLevel() + ".commands.enough")) {
                            plugin.getServer().dispatchCommand(plugin.consoleSender, command.replace("{player}", player.getName()));
                        }
                    }
                } else {
                    for (String command : file.getStringList(playerConnect.getGroup() + "." + droneHolder.getLevel() + ".commands.max")) {
                        plugin.getServer().dispatchCommand(plugin.consoleSender, command.replace("{player}", player.getName()));
                    }
                }
            }
            if (plugin.config.get.getBoolean("update-toggle") && updateAI) {
                if (playerConnect.hasActive()) {
                    playerConnect.stopAI();
                    playerConnect.stopFindTargetAI();
                    plugin.droneManager.startAI(player, playerConnect, droneHolder, droneFile, drone);
                }
            }
            plugin.guiManager.dispatchCommand(file, slot, player);
        }
    }

    @Override
    public void setItems() {
        DroneHolder droneHolder = plugin.getDroneHolder(uuid, drone);
        setPlayerGUI(plugin.droneFiles.get(drone),
                plugin.guiFiles.get(drone),
                plugin.get(playerMenu.getUuid()).getGroup(),
                inventory,
                droneHolder.getHealth(),
                droneHolder.getMonsters(),
                droneHolder.getAnimals(),
                droneHolder.getPlayers(),
                droneHolder.getLevel(),
                droneHolder.getAmmo(),
                droneHolder.getExclude().size()
        );
    }

    public void setPlayerGUI(FileConfiguration file, FileConfiguration gui, String group, Inventory inventory, int health, int monsters, int animals, int players, int drone_level, int drone_ammo, int WhitelistSize) {
        final String path = group + "." + drone_level + ".";
        final String path_next = group + "." + (drone_level + 1);
        final String level = String.valueOf(drone_level);
        final int ammo = drone_ammo;
        final String max_ammo_slots = file.getString(path + "max-ammo-slots");
        final String min_max = file.getString(path + "min") + "-" + file.getString(path + "max");
        final String range = file.getString(path + "range");
        String firerate = "";
        String cooldown = "";
        if (file.contains(path + "cooldown")) {
            cooldown = file.getString(path + "cooldown");
            firerate = plugin.calculateManager.getFirerate(file.getDouble(path + "cooldown"));
        }
        String shield_generator_damage = plugin.calculateManager.getProcentFromDouble(file.getDouble(path + "min")) + "-" + plugin.calculateManager.getProcentFromDouble(file.getDouble(path + "max"));
        String chance = "";
        if (file.contains(path + "chance")) {
            chance = String.valueOf(plugin.calculateManager.getProcentFromDouble(file.getDouble(path + "chance")));
        }
        String setfire_chance = "";
        if (file.contains(path + "setfire-chance")) {
            setfire_chance = String.valueOf(plugin.calculateManager.getProcentFromDouble(file.getDouble(path + "setfire-chance")));
        }
        String burning_time = "";
        if (file.contains(path + "burning-time")) {
            burning_time = file.getString(path + "burning-time");
        }
        String rocket_speed = "";
        if (file.contains(path + "rocket-speed")) {
            rocket_speed = file.getString(path + "rocket-speed");
        }
        String rocket_radius = "";
        if (file.contains(path + "rocket-radius")) {
            rocket_radius = file.getString(path + "rocket-radius");
        }
        String rocket_time = "";
        if (file.contains(path + "rocket-time")) {
            rocket_time = file.getString(path + "rocket-time");
        }
        String knockback = "";
        if (file.contains(path + "knockback")) {
            knockback = file.getString(path + "knockback");
        }
        final String regen_health = file.getString(path + "regen.health");
        final String regen_seconds = file.getString(path + "regen.delay");
        final String whitelist_count = String.valueOf(WhitelistSize);
        String level_next = "";
        String min_max_next = "";
        String range_next = "";
        String firerate_next = "";
        String cooldown_next = "";
        String shield_generator_damage_next = "";
        String chance_next = "";
        String setfire_chance_next = "";
        String burning_time_next = "";
        String rocket_speed_next = "";
        String rocket_radius_next = "";
        String rocket_time_next = "";
        String knockback_next = "";
        String regen_health_next = "";
        String regen_seconds_next = "";
        String cost_next = "";
        String max_ammo_slots_next = "";
        if (file.contains(path_next)) {
            level_next = String.valueOf((drone_level + 1));
            min_max_next = file.getString(path_next + ".min") + "-" + file.getString(path_next + ".max");
            shield_generator_damage_next = plugin.calculateManager.getProcentFromDouble(file.getDouble(path_next + "min")) + "-" + plugin.calculateManager.getProcentFromDouble(file.getDouble(path_next + "max"));
            range_next = file.getString(path_next + ".range");
            if (file.contains(path_next + ".cooldown")) {
                cooldown_next = file.getString(path_next + ".cooldown");
                firerate_next = plugin.calculateManager.getFirerate(file.getDouble(path_next + ".cooldown"));
            }
            if (file.contains(path_next + ".chance")) {
                chance_next = String.valueOf(plugin.calculateManager.getProcentFromDouble(file.getDouble(path_next + ".chance")));
            }
            if (file.contains(path_next + ".setfire-chance")) {
                setfire_chance_next = String.valueOf(plugin.calculateManager.getProcentFromDouble(file.getDouble(path_next + ".setfire-chance")));
            }
            if (file.contains(path_next + ".burning-time")) {
                burning_time_next = file.getString(path_next + ".burning-time");
            }
            if (file.contains(path_next + ".rocket-speed")) {
                rocket_speed_next = file.getString(path_next + ".rocket-speed");
            }
            if (file.contains(path_next + ".rocket-radius")) {
                rocket_radius_next = file.getString(path_next + ".rocket-radius");
            }
            if (file.contains(path_next + ".rocket-time")) {
                rocket_time_next = file.getString(path_next + ".rocket-time");
            }
            if (file.contains(path_next + ".knockback")) {
                knockback_next = file.getString(path_next + ".knockback");
            }
            if (file.contains(path_next + ".max-ammo-slots")) {
                max_ammo_slots_next = file.getString(path_next + ".max-ammo-slots");
            }
            regen_health_next = file.getString(path_next + ".regen.health");
            regen_seconds_next = file.getString(path_next + ".regen.delay");
            cost_next = file.getString(path_next + ".cost");
        }
        String mobs_current = plugin.language.get.getString("gui.drone.disabled");
        String animals_current = plugin.language.get.getString("gui.drone.disabled");
        String players_current = plugin.language.get.getString("gui.drone.disabled");
        if (gui.contains("settings.monsters")) {
            if (monsters == 1) {
                mobs_current = plugin.language.get.getString("gui.drone.enabled");
                setItemStack(inventory, gui, "settings.monsters.enabled");
            } else {
                setItemStack(inventory, gui, "settings.monsters.disabled");
            }
        }
        if (gui.contains("settings.animals")) {
            if (animals == 1) {
                animals_current = plugin.language.get.getString("gui.drone.enabled");
                setItemStack(inventory, gui, "settings.animals.enabled");
            } else {
                setItemStack(inventory, gui, "settings.animals.disabled");
            }
        }
        if (gui.contains("settings.players")) {
            if (players == 1) {
                players_current = plugin.language.get.getString("gui.drone.enabled");
                setItemStack(inventory, gui, "settings.players.enabled");
            } else {
                setItemStack(inventory, gui, "settings.players.disabled");
            }
        }
        for (String key : Objects.requireNonNull(gui.getConfigurationSection("")).getKeys(false)) {
            if (!key.equalsIgnoreCase("settings")) {
                ItemStack itemStack;
                if (!gui.contains(key + ".HEAD")) {
                    itemStack = plugin.getItemStack(gui.getString(key + ".MATERIAL"), gui.getInt(key + ".AMOUNT"));
                } else {
                    itemStack = plugin.drone_heads.get(gui.getString(key + ".HEAD"));
                }
                ItemMeta itemMeta = itemStack.getItemMeta();
                if (itemMeta == null) {
                    return;
                }
                itemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(gui.getString(key + ".NAME"))
                        .replace("{health}", plugin.calculateManager.getBar(health, file.getInt(path + "health"), "health", ""))
                        .replace("{health_percentage}", String.valueOf(plugin.calculateManager.getPercent(health, file.getInt(path + "health"))))
                        .replace("{max_ammo_slots}", max_ammo_slots)
                        .replace("{level}", level)
                        .replace("{cost}", cost_next)
                        .replace("{ammo}", String.valueOf(ammo))
                        .replace("{ammo_bar}", plugin.calculateManager.getBar(ammo, file.getInt(path + "max-ammo-slots") * 64, "ammo", ""))
                        .replace("{ammo_percentage}", String.valueOf((plugin.calculateManager.getPercent(ammo, file.getInt(path + "max-ammo-slots") * 64))))
                        .replace("{min_max}", min_max)
                        .replace("{shield_generator_damage}", shield_generator_damage)
                        .replace("{range}", range)
                        .replace("{firerate}", firerate)
                        .replace("{cooldown}", cooldown)
                        .replace("{chance}", chance)
                        .replace("{setfire_chance}", setfire_chance)
                        .replace("{burning_time}", burning_time)
                        .replace("{knockback}", knockback)
                        .replace("{rocket_speed}", rocket_speed)
                        .replace("{rocket_radius}", rocket_radius)
                        .replace("{rocket_time}", rocket_time)
                        .replace("{regen_health}", regen_health)
                        .replace("{regen_delay}", regen_seconds)
                        .replace("{max_ammo_slots_next}", max_ammo_slots_next)
                        .replace("{level_next}", level_next)
                        .replace("{min_max_next}", min_max_next)
                        .replace("{shield_generator_damage_next}", shield_generator_damage_next)
                        .replace("{range_next}", range_next)
                        .replace("{firerate_next}", firerate_next)
                        .replace("{cooldown_next}", cooldown_next)
                        .replace("{chance_next}", chance_next)
                        .replace("{setfire_chance_next}", setfire_chance_next)
                        .replace("{burning_time_next}", burning_time_next)
                        .replace("{knockback_next}", knockback_next)
                        .replace("{rocket_speed_next}", rocket_speed_next)
                        .replace("{rocket_radius_next}", rocket_radius_next)
                        .replace("{rocket_time_next}", rocket_time_next)
                        .replace("{regen_health_next}", regen_health_next)
                        .replace("{regen_delay_next}", regen_seconds_next)
                        .replace("{mobs_current}", mobs_current)
                        .replace("{animals_current}", animals_current)
                        .replace("{players_current}", players_current)
                        .replace("{whitelist}", whitelist_count)));
                ArrayList<String> list = new ArrayList<>();
                for (String lores : gui.getStringList(key + ".LORES")) {
                    list.add(ChatColor.translateAlternateColorCodes('&', lores
                            .replace("{health}", plugin.calculateManager.getBar(health, file.getInt(path + "health"), "health", ""))
                            .replace("{health_percentage}", String.valueOf(plugin.calculateManager.getPercent(health, file.getInt(path + "health"))))
                            .replace("{max_ammo_slots}", max_ammo_slots)
                            .replace("{level}", level)
                            .replace("{cost}", cost_next)
                            .replace("{ammo}", String.valueOf(ammo))
                            .replace("{ammo_bar}", plugin.calculateManager.getBar(ammo, file.getInt(path + "max-ammo-slots") * 64, "ammo", ""))
                            .replace("{ammo_percentage}", String.valueOf((plugin.calculateManager.getPercent(ammo, file.getInt(path + "max-ammo-slots") * 64))))
                            .replace("{min_max}", min_max)
                            .replace("{shield_generator_damage}", shield_generator_damage)
                            .replace("{range}", range)
                            .replace("{firerate}", firerate)
                            .replace("{cooldown}", cooldown)
                            .replace("{chance}", chance)
                            .replace("{setfire_chance}", setfire_chance)
                            .replace("{burning_time}", burning_time)
                            .replace("{knockback}", knockback)
                            .replace("{rocket_speed}", rocket_speed)
                            .replace("{rocket_radius}", rocket_radius)
                            .replace("{rocket_time}", rocket_time)
                            .replace("{regen_health}", regen_health)
                            .replace("{regen_delay}", regen_seconds)
                            .replace("{max_ammo_slots_next}", max_ammo_slots_next)
                            .replace("{level_next}", level_next)
                            .replace("{min_max_next}", min_max_next)
                            .replace("{shield_generator_damage_next}", shield_generator_damage_next)
                            .replace("{range_next}", range_next)
                            .replace("{firerate_next}", firerate_next)
                            .replace("{cooldown_next}", cooldown_next)
                            .replace("{chance_next}", chance_next)
                            .replace("{setfire_chance_next}", setfire_chance_next)
                            .replace("{burning_time_next}", burning_time_next)
                            .replace("{knockback_next}", knockback_next)
                            .replace("{rocket_speed_next}", rocket_speed_next)
                            .replace("{rocket_radius_next}", rocket_radius_next)
                            .replace("{rocket_time_next}", rocket_time_next)
                            .replace("{regen_health_next}", regen_health_next)
                            .replace("{regen_delay_next}", regen_seconds_next)
                            .replace("{mobs_current}", mobs_current)
                            .replace("{animals_current}", animals_current)
                            .replace("{players_current}", players_current)
                            .replace("{whitelist}", whitelist_count)
                    ));
                }
                itemMeta.setLore(list);
                itemStack.setItemMeta(itemMeta);
                inventory.setItem(Integer.parseInt(key), itemStack);
            }
        }
        if (!file.contains(path_next)) {
            if (gui.contains("settings.upgrade")) {
                setItemStack(inventory, gui, "settings.upgrade");
            }
        }
    }

    public void setItemStack(Inventory inventory, FileConfiguration file, String path) {
        ItemStack itemStack;
        if (!file.contains(path + ".HEAD")) {
            itemStack = plugin.getItemStack(file.getString(path + ".MATERIAL"), file.getInt(path + ".AMOUNT"));
        } else {
            itemStack = plugin.drone_heads.get(file.getString(path + ".HEAD"));
        }
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta == null) {
            return;
        }
        itemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(file.getString(path + ".NAME"))));
        ArrayList<String> list = new ArrayList<>();
        for (String lores : file.getStringList(path + ".LORES")) {
            list.add(ChatColor.translateAlternateColorCodes('&', lores));
        }
        itemMeta.setLore(list);
        itemStack.setItemMeta(itemMeta);
        inventory.setItem(file.getInt(path + ".POSITION"), itemStack);
    }
}