package me.MathiasMC.BattleDrones.commands;

import me.MathiasMC.BattleDrones.BattleDrones;
import me.MathiasMC.BattleDrones.data.DroneHolder;
import me.MathiasMC.BattleDrones.data.PlayerConnect;
import me.MathiasMC.BattleDrones.gui.DroneMenu;
import me.MathiasMC.BattleDrones.gui.player.PlayerGUI;
import me.MathiasMC.BattleDrones.gui.shop.ShopGUI;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Objects;
import java.util.regex.Pattern;

public class BattleDrones_Command implements CommandExecutor {

    private final BattleDrones plugin;

    public BattleDrones_Command(final BattleDrones plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        if (cmd.getName().equalsIgnoreCase("battledrones")) {
            boolean unknown = true;
            String type;
            if (sender instanceof Player) {
                type = "player";
            } else {
                type = "console";
            }
            if (sender.hasPermission("battledrones")) {
                if (args.length == 0) {
                    if (type.equalsIgnoreCase("player")) {
                        for (String message : plugin.language.get.getStringList("command.message")) {
                            plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', message.replace("{player}", sender.getName()).replace("{version}", plugin.getDescription().getVersion())));
                        }
                    } else {
                        for (String message : plugin.language.get.getStringList("console.command.message")) {
                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message.replace("{version}", plugin.getDescription().getVersion())));
                        }
                    }
                } else {
                    if (args[0].equalsIgnoreCase("help")) {
                        unknown = false;
                        if (sender.hasPermission("battledrones.player.help")) {
                            if (type.equalsIgnoreCase("player")) {
                                for (String message : plugin.language.get.getStringList("help.message")) {
                                    plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', message.replace("{player}", sender.getName())));
                                }
                            } else {
                                for (String message : plugin.language.get.getStringList("console.help.message")) {
                                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                                }
                            }
                        } else {
                            if (type.equalsIgnoreCase("player")) {
                                for (String message : plugin.language.get.getStringList("help.permission")) {
                                    plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', message.replace("{player}", sender.getName())));
                                }
                            }
                        }
                    } else if (args[0].equalsIgnoreCase("shop")) {
                        unknown = false;
                        if (sender.hasPermission("battledrones.player.shop")) {
                            if (type.equalsIgnoreCase("player")) {
                                Player player = (Player) sender;
                                new ShopGUI(plugin.getPlayerMenu(player)).open();
                            } else {
                                if (args.length == 2) {
                                    Player target = plugin.getServer().getPlayer(args[1]);
                                    if (target != null) {
                                        new ShopGUI(plugin.getPlayerMenu(target)).open();
                                    } else {
                                        for (String message : plugin.language.get.getStringList("console.shop.online")) {
                                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                                        }
                                    }
                                } else {
                                    for (String message : plugin.language.get.getStringList("console.shop.usage")) {
                                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                                    }
                                }
                            }
                        } else {
                            if (type.equalsIgnoreCase("player")) {
                                for (String message : plugin.language.get.getStringList("shop.permission")) {
                                    plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', message.replace("{player}", sender.getName())));
                                }
                            }
                        }
                    } else if (args[0].equalsIgnoreCase("menu")) {
                        unknown = false;
                        if (sender.hasPermission("battledrones.player.menu")) {
                            if (type.equalsIgnoreCase("player")) {
                                Player player = (Player) sender;
                                new PlayerGUI(plugin.getPlayerMenu(player)).open();
                            } else {
                                if (args.length == 2) {
                                    Player target = plugin.getServer().getPlayer(args[1]);
                                    if (target != null) {
                                        new PlayerGUI(plugin.getPlayerMenu(target)).open();
                                    } else {
                                        for (String message : plugin.language.get.getStringList("console.menu.online")) {
                                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                                        }
                                    }
                                } else {
                                    for (String message : plugin.language.get.getStringList("console.menu.usage")) {
                                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                                    }
                                }
                            }
                        } else {
                            if (type.equalsIgnoreCase("player")) {
                                for (String message : plugin.language.get.getStringList("menu.permission")) {
                                    plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', message.replace("{player}", sender.getName())));
                                }
                            }
                        }
                    } else if (args[0].equalsIgnoreCase("drone")) {
                        unknown = false;
                        if (sender.hasPermission("battledrones.player.drone")) {
                            if (type.equalsIgnoreCase("player")) {
                                final Player player = (Player) sender;
                                final String uuid = player.getUniqueId().toString();
                                final PlayerConnect playerConnect = plugin.get(uuid);
                                if (playerConnect.hasActive()) {
                                    if (!plugin.listDroneHolder().contains(uuid) || !plugin.getDroneHolderUUID(uuid).containsKey(playerConnect.getActive())) {
                                        plugin.loadDroneHolder(uuid, playerConnect.getActive());
                                    }
                                    new DroneMenu(plugin.getPlayerMenu(player), playerConnect.getActive()).open();
                                } else {
                                    for (String message : plugin.language.get.getStringList("drone.active")) {
                                        plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', message.replace("{player}", sender.getName())));
                                    }
                                }
                            } else {
                                if (args.length == 2) {
                                    final Player target = plugin.getServer().getPlayer(args[1]);
                                    if (target != null) {
                                        final String uuid = target.getUniqueId().toString();
                                        final PlayerConnect playerConnect = plugin.get(uuid);
                                        if (playerConnect.hasActive()) {
                                            new DroneMenu(plugin.getPlayerMenu(target), playerConnect.getActive()).open();
                                        } else {
                                            for (String message : plugin.language.get.getStringList("drone.active")) {
                                                plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', message.replace("{player}", target.getName())));
                                            }
                                        }
                                    } else {
                                        for (String message : plugin.language.get.getStringList("console.drone.online")) {
                                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                                        }
                                    }
                                } else {
                                    for (String message : plugin.language.get.getStringList("console.drone.usage")) {
                                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                                    }
                                }
                            }
                        } else {
                            if (type.equalsIgnoreCase("player")) {
                                for (String message : plugin.language.get.getStringList("drone.permission")) {
                                    plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', message.replace("{player}", sender.getName())));
                                }
                            }
                        }
                    } else if (args[0].equalsIgnoreCase("activate")) {
                        unknown = false;
                        if (sender.hasPermission("battledrones.player.activate")) {
                            if (type.equalsIgnoreCase("player")) {
                                if (args.length == 2) {
                                    if (plugin.drones.contains(args[1])) {
                                        final Player player = (Player) sender;
                                        plugin.droneManager.spawnDrone(player, args[1], true, false);
                                    } else {
                                        for (String message : plugin.language.get.getStringList("activate.valid")) {
                                            plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', message.replace("{player}", sender.getName()).replace("{drone}", args[1])));
                                        }
                                    }
                                } else {
                                    for (String message : plugin.language.get.getStringList("activate.usage")) {
                                        plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', message.replace("{player}", sender.getName())));
                                    }
                                }
                            } else {
                                if (args.length == 3) {
                                    final Player target = plugin.getServer().getPlayer(args[2]);
                                    if (target != null) {
                                        if (plugin.drones.contains(args[1])) {
                                            plugin.droneManager.spawnDrone(target, args[1], true, true);
                                        } else {
                                            for (String message : plugin.language.get.getStringList("console.activate.valid")) {
                                                plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', message.replace("{drone}", args[1])));
                                            }
                                        }
                                    } else {
                                        for (String message : plugin.language.get.getStringList("console.activate.online")) {
                                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                                        }
                                    }
                                } else {
                                    for (String message : plugin.language.get.getStringList("console.activate.usage")) {
                                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                                    }
                                }
                            }
                        } else {
                            if (type.equalsIgnoreCase("player")) {
                                for (String message : plugin.language.get.getStringList("activate.permission")) {
                                    plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', message.replace("{player}", sender.getName())));
                                }
                            }
                        }
                    } else if (args[0].equalsIgnoreCase("deactivate")) {
                        unknown = false;
                        if (sender.hasPermission("battledrones.player.deactivate")) {
                            if (args.length == 1) {
                                if (type.equalsIgnoreCase("player")) {
                                    final Player player = (Player) sender;
                                    final String uuid = player.getUniqueId().toString();
                                    final PlayerConnect playerConnect = plugin.get(uuid);
                                    if (playerConnect.hasActive()) {
                                        final FileConfiguration file = plugin.droneFiles.get(playerConnect.getActive());
                                        plugin.droneManager.runCommands(player, playerConnect, file, "gui.REMOVE-COMMANDS", true);
                                        plugin.droneManager.waitSchedule(uuid, file);
                                    } else {
                                        for (String message : plugin.language.get.getStringList("deactivate.own")) {
                                            plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', message.replace("{player}", sender.getName())));
                                        }
                                    }
                                    playerConnect.stopDrone();
                                } else {
                                    for (String message : plugin.language.get.getStringList("console.deactivate.other.usage")) {
                                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                                    }
                                }
                            } else {
                                if (sender.hasPermission("battledrones.admin.deactivate")) {
                                    if (args.length == 3) {
                                        if (plugin.isInt(args[2])) {
                                            if (args[1].equalsIgnoreCase("all")) {
                                                plugin.drone_players.clear();
                                                for (String uuidConnect : plugin.list()) {
                                                    plugin.get(uuidConnect).stopDrone();
                                                    plugin.drone_players.add(uuidConnect);
                                                    plugin.getServer().getScheduler().runTaskLater(plugin, () -> BattleDrones.call.drone_players.remove(uuidConnect), Integer.parseInt(args[2]) * 20);
                                                }
                                                if (type.equalsIgnoreCase("player")) {
                                                    for (String command : plugin.language.get.getStringList("deactivate.other.all")) {
                                                        plugin.getServer().dispatchCommand(plugin.consoleSender, command.replace("{player}", sender.getName()).replace("{amount}", args[2]));
                                                    }
                                                } else {
                                                    for (String command : plugin.language.get.getStringList("console.deactivate.other.all")) {
                                                        plugin.getServer().dispatchCommand(plugin.consoleSender, command.replace("{amount}", args[2]));
                                                    }
                                                }
                                            } else if (plugin.drones.contains(args[1])) {
                                                plugin.drone_players.clear();
                                                for (String uuidConnect : plugin.list()) {
                                                    final PlayerConnect playerConnect = plugin.get(uuidConnect);
                                                    if (playerConnect.hasActive()) {
                                                        final String droneType = playerConnect.getActive();
                                                        if (args[1].equalsIgnoreCase(droneType)) {
                                                            playerConnect.stopDrone();
                                                            plugin.drone_players.add(uuidConnect);
                                                            plugin.getServer().getScheduler().runTaskLater(plugin, () -> plugin.drone_players.remove(uuidConnect), Integer.parseInt(args[2]) * 20);
                                                        }
                                                    }
                                                }
                                                if (type.equalsIgnoreCase("player")) {
                                                    for (String command : plugin.language.get.getStringList("deactivate.other.all-drone")) {
                                                        plugin.getServer().dispatchCommand(plugin.consoleSender, command.replace("{player}", sender.getName()).replace("{amount}", args[2]).replace("{drone}", plugin.internalPlaceholders.getActiveDrone(args[1])));
                                                    }
                                                } else {
                                                    for (String command : plugin.language.get.getStringList("console.deactivate.other.all-drone")) {
                                                        plugin.getServer().dispatchCommand(plugin.consoleSender, command.replace("{amount}", args[2]).replace("{drone}", plugin.internalPlaceholders.getActiveDrone(args[1])));
                                                    }
                                                }
                                            } else {
                                                final Player target = plugin.getServer().getPlayer(args[1]);
                                                if (target != null) {
                                                    final String uuid = target.getUniqueId().toString();
                                                    final PlayerConnect playerConnect = plugin.get(uuid);
                                                    plugin.drone_players.add(uuid);
                                                    plugin.getServer().getScheduler().runTaskLater(plugin, () -> plugin.drone_players.remove(uuid), Integer.parseInt(args[2]) * 20);
                                                    for (String message : plugin.language.get.getStringList("deactivate.player")) {
                                                        plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', message.replace("{player}", target.getName()).replace("{amount}", args[2])));
                                                    }
                                                    playerConnect.stopDrone();
                                                } else {
                                                    if (type.equalsIgnoreCase("player")) {
                                                        for (String message : plugin.language.get.getStringList("deactivate.other.online")) {
                                                            plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', message.replace("{player}", sender.getName())));
                                                        }
                                                    } else {
                                                        for (String message : plugin.language.get.getStringList("console.deactivate.other.online")) {
                                                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                                                        }
                                                    }
                                                }
                                            }
                                        } else {
                                            if (type.equalsIgnoreCase("player")) {
                                                for (String message : plugin.language.get.getStringList("deactivate.other.number")) {
                                                    plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', message.replace("{player}", sender.getName())));
                                                }
                                            } else {
                                                for (String message : plugin.language.get.getStringList("console.deactivate.other.number")) {
                                                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                                                }
                                            }
                                        }
                                    } else {
                                        if (type.equalsIgnoreCase("player")) {
                                            for (String message : plugin.language.get.getStringList("deactivate.other.usage")) {
                                                plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', message.replace("{player}", sender.getName())));
                                            }
                                        } else {
                                            for (String message : plugin.language.get.getStringList("console.deactivate.other.usage")) {
                                                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                                            }
                                        }
                                    }
                                } else {
                                    if (type.equalsIgnoreCase("player")) {
                                        for (String message : plugin.language.get.getStringList("deactivate.permission")) {
                                            plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', message.replace("{player}", sender.getName())));
                                        }
                                    }
                                }
                            }
                        } else {
                            if (type.equalsIgnoreCase("player")) {
                                for (String message : plugin.language.get.getStringList("deactivate.permission")) {
                                    plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', message.replace("{player}", sender.getName())));
                                }
                            }
                        }
                    } else if (args[0].equalsIgnoreCase("park")) {
                        unknown = false;
                        if (sender.hasPermission("battledrones.player.park")) {
                            if (type.equalsIgnoreCase("player")) {
                                final Player player = (Player) sender;
                                final String uuid = player.getUniqueId().toString();
                                final PlayerConnect playerConnect = plugin.get(uuid);
                                if (playerConnect.hasActive()) {
                                    park(player, playerConnect);
                                } else {
                                    for (String message : plugin.language.get.getStringList("park.active")) {
                                        plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', message.replace("{player}", sender.getName())));
                                    }
                                }
                            } else {
                                if (args.length == 2) {
                                    final Player target = plugin.getServer().getPlayer(args[1]);
                                    if (target != null) {
                                        final String uuid = target.getUniqueId().toString();
                                        final PlayerConnect playerConnect = plugin.get(uuid);
                                        if (playerConnect.hasActive()) {
                                            park(target, playerConnect);
                                        } else {
                                            for (String message : plugin.language.get.getStringList("park.active")) {
                                                plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', message.replace("{player}", target.getName())));
                                            }
                                        }
                                    } else {
                                        for (String message : plugin.language.get.getStringList("console.park.online")) {
                                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                                        }
                                    }
                                } else {
                                    for (String message : plugin.language.get.getStringList("console.park.usage")) {
                                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                                    }
                                }
                            }
                        } else {
                            if (type.equalsIgnoreCase("player")) {
                                for (String message : plugin.language.get.getStringList("park.permission")) {
                                    plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', message.replace("{player}", sender.getName())));
                                }
                            }
                        }
                    } else if (args[0].equalsIgnoreCase("reload")) {
                        unknown = false;
                        if (sender.hasPermission("battledrones.admin.reload")) {
                            if (args.length == 2) {
                                if (args[1].equalsIgnoreCase("all")) {
                                    plugin.config.load();
                                    plugin.language.load();
                                    plugin.guiFolder.load();
                                    plugin.dronesFolder.load();
                                    plugin.particles.load();
                                    plugin.particleManager.load();
                                    plugin.addHeads();
                                    if (type.equalsIgnoreCase("player")) {
                                        for (String message : plugin.language.get.getStringList("reload.all")) {
                                            plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', message.replace("{player}", sender.getName())));
                                        }
                                    } else {
                                        for (String message : plugin.language.get.getStringList("console.reload.all")) {
                                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                                        }
                                    }
                                } else if (args[1].equalsIgnoreCase("config")) {
                                    plugin.config.load();
                                    if (type.equalsIgnoreCase("player")) {
                                        for (String message : plugin.language.get.getStringList("reload.config")) {
                                            plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', message.replace("{player}", sender.getName())));
                                        }
                                    } else {
                                        for (String message : plugin.language.get.getStringList("console.reload.config")) {
                                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                                        }
                                    }
                                } else if (args[1].equalsIgnoreCase("language")) {
                                    plugin.language.load();
                                    if (type.equalsIgnoreCase("player")) {
                                        for (String message : plugin.language.get.getStringList("reload.language")) {
                                            plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', message.replace("{player}", sender.getName())));
                                        }
                                    } else {
                                        for (String message : plugin.language.get.getStringList("console.reload.language")) {
                                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                                        }
                                    }
                                } else if (args[1].equalsIgnoreCase("particles")) {
                                    plugin.particles.load();
                                    plugin.particleManager.load();
                                    if (type.equalsIgnoreCase("player")) {
                                        for (String message : plugin.language.get.getStringList("reload.particles")) {
                                            plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', message.replace("{player}", sender.getName())));
                                        }
                                    } else {
                                        for (String message : plugin.language.get.getStringList("console.reload.particles")) {
                                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                                        }
                                    }
                                } else if (args[1].equalsIgnoreCase("gui")) {
                                    plugin.guiFolder.load();
                                    if (type.equalsIgnoreCase("player")) {
                                        for (String message : plugin.language.get.getStringList("reload.gui")) {
                                            plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', message.replace("{player}", sender.getName())));
                                        }
                                    } else {
                                        for (String message : plugin.language.get.getStringList("console.reload.gui")) {
                                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                                        }
                                    }
                                } else if (args[1].equalsIgnoreCase("drones")) {
                                    plugin.dronesFolder.load();
                                    if (type.equalsIgnoreCase("player")) {
                                        for (String message : plugin.language.get.getStringList("reload.drones")) {
                                            plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', message.replace("{player}", sender.getName())));
                                        }
                                    } else {
                                        for (String message : plugin.language.get.getStringList("console.reload.drones")) {
                                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                                        }
                                    }
                                } else if (args[1].equalsIgnoreCase("heads")) {
                                    plugin.addHeads();
                                    if (type.equalsIgnoreCase("player")) {
                                        for (String message : plugin.language.get.getStringList("reload.heads")) {
                                            plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', message.replace("{player}", sender.getName())));
                                        }
                                    } else {
                                        for (String message : plugin.language.get.getStringList("console.reload.heads")) {
                                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                                        }
                                    }
                                }
                            } else {
                                if (type.equalsIgnoreCase("player")) {
                                    for (String message : plugin.language.get.getStringList("reload.usage")) {
                                        plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', message.replace("{player}", sender.getName())));
                                    }
                                } else {
                                    for (String message : plugin.language.get.getStringList("console.reload.usage")) {
                                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                                    }
                                }
                            }
                        } else {
                            if (type.equalsIgnoreCase("player")) {
                                for (String message : plugin.language.get.getStringList("reload.permission")) {
                                    plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', message.replace("{player}", sender.getName())));
                                }
                            }
                        }
                    } else if (args[0].equalsIgnoreCase("save")) {
                        unknown = false;
                        if (sender.hasPermission("battledrones.admin.save")) {
                            for (String uuid : plugin.list()) {
                                plugin.get(uuid).save();
                                if (plugin.listDroneHolder().contains(uuid)) {
                                    for (String drone : plugin.listDroneHolder()) {
                                        if (plugin.getDroneHolderUUID(uuid).containsKey(drone)) {
                                            plugin.getDroneHolder(uuid, drone).save();
                                        }
                                    }
                                }
                            }
                            if (type.equalsIgnoreCase("player")) {
                                for (String message : plugin.language.get.getStringList("save.message")) {
                                    plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', message.replace("{player}", sender.getName())));
                                }
                            } else {
                                for (String message : plugin.language.get.getStringList("console.save.message")) {
                                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                                }
                            }
                        } else {
                            if (type.equalsIgnoreCase("player")) {
                                for (String message : plugin.language.get.getStringList("save.permission")) {
                                    plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', message.replace("{player}", sender.getName())));
                                }
                            }
                        }
                    } else if (args[0].equalsIgnoreCase("unlock")) {
                        unknown = false;
                        if (sender.hasPermission("battledrones.admin.unlock")) {
                            if (args.length == 3) {
                                if (plugin.drones.contains(args[1])) {
                                    final Player target = plugin.getServer().getPlayer(args[2]);
                                    if (target != null) {
                                        final String targetUUID = target.getUniqueId().toString();
                                        BattleDrones.call.loadDroneHolder(targetUUID, args[1]);
                                        PlayerConnect playerConnect = plugin.get(targetUUID);
                                        DroneHolder droneHolder = plugin.getDroneHolder(targetUUID, args[1]);
                                        if (droneHolder.getUnlocked() != 1) {
                                            droneHolder.setUnlocked(1);
                                            droneHolder.setHealth(BattleDrones.call.droneFiles.get(args[1]).getInt(playerConnect.getGroup() + "." + droneHolder.getLevel() + ".health"));
                                        }
                                        droneHolder.save();
                                        if (type.equalsIgnoreCase("player")) {
                                            for (String message : plugin.language.get.getStringList("unlock.message")) {
                                                plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', message.replace("{player}", sender.getName()).replace("{drone}", args[1]).replace("{target}", target.getName())));
                                            }
                                        } else {
                                            for (String message : plugin.language.get.getStringList("console.unlock.message")) {
                                                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message.replace("{drone}", args[1]).replace("{target}", target.getName())));
                                            }
                                        }
                                    } else {
                                        if (type.equalsIgnoreCase("player")) {
                                            for (String message : plugin.language.get.getStringList("unlock.online")) {
                                                plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', message.replace("{player}", sender.getName())));
                                            }
                                        } else {
                                            for (String message : plugin.language.get.getStringList("console.unlock.online")) {
                                                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                                            }
                                        }
                                    }
                                } else {
                                    if (type.equalsIgnoreCase("player")) {
                                        for (String message : plugin.language.get.getStringList("unlock.type")) {
                                            plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', message.replace("{player}", sender.getName())));
                                        }
                                    } else {
                                        for (String message : plugin.language.get.getStringList("console.unlock.type")) {
                                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                                        }
                                    }
                                }
                            } else {
                                if (type.equalsIgnoreCase("player")) {
                                    for (String message : plugin.language.get.getStringList("unlock.usage")) {
                                        plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', message.replace("{player}", sender.getName())));
                                    }
                                } else {
                                    for (String message : plugin.language.get.getStringList("console.unlock.usage")) {
                                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                                    }
                                }
                            }
                        } else {
                            if (type.equalsIgnoreCase("player")) {
                                for (String message : plugin.language.get.getStringList("unlock.permission")) {
                                    plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', message.replace("{player}", sender.getName())));
                                }
                            }
                        }
                    } else if (args[0].equalsIgnoreCase("lock")) {
                        unknown = false;
                        if (sender.hasPermission("battledrones.admin.lock")) {
                            if (args.length == 3) {
                                if (plugin.drones.contains(args[1])) {
                                    final Player target = plugin.getServer().getPlayer(args[2]);
                                    if (target != null) {
                                        final String targetUUID = target.getUniqueId().toString();
                                        BattleDrones.call.loadDroneHolder(targetUUID, args[1]);
                                        DroneHolder droneHolder = plugin.getDroneHolder(targetUUID, args[1]);
                                        if (droneHolder.getUnlocked() != 0) {
                                            droneHolder.setUnlocked(0);
                                            droneHolder.setHealth(0);
                                            plugin.get(targetUUID).stopDrone();
                                        }
                                        droneHolder.save();
                                        if (type.equalsIgnoreCase("player")) {
                                            for (String message : plugin.language.get.getStringList("lock.message")) {
                                                plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', message.replace("{player}", sender.getName()).replace("{drone}", args[1]).replace("{target}", target.getName())));
                                            }
                                        } else {
                                            for (String message : plugin.language.get.getStringList("console.lock.message")) {
                                                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message.replace("{drone}", args[1]).replace("{target}", target.getName())));
                                            }
                                        }
                                    } else {
                                        if (type.equalsIgnoreCase("player")) {
                                            for (String message : plugin.language.get.getStringList("lock.online")) {
                                                plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', message.replace("{player}", sender.getName())));
                                            }
                                        } else {
                                            for (String message : plugin.language.get.getStringList("console.lock.online")) {
                                                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                                            }
                                        }
                                    }
                                } else {
                                    if (type.equalsIgnoreCase("player")) {
                                        for (String message : plugin.language.get.getStringList("lock.type")) {
                                            plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', message.replace("{player}", sender.getName())));
                                        }
                                    } else {
                                        for (String message : plugin.language.get.getStringList("console.lock.type")) {
                                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                                        }
                                    }
                                }
                            } else {
                                if (type.equalsIgnoreCase("player")) {
                                    for (String message : plugin.language.get.getStringList("lock.usage")) {
                                        plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', message.replace("{player}", sender.getName())));
                                    }
                                } else {
                                    for (String message : plugin.language.get.getStringList("console.lock.usage")) {
                                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                                    }
                                }
                            }
                        } else {
                            if (type.equalsIgnoreCase("player")) {
                                for (String message : plugin.language.get.getStringList("lock.permission")) {
                                    plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', message.replace("{player}", sender.getName())));
                                }
                            }
                        }
                    } else if (args[0].equalsIgnoreCase("group")) {
                        unknown = false;
                        if (sender.hasPermission("battledrones.admin.group")) {
                            if (args.length > 1) {
                                if (args[1].equalsIgnoreCase("set")) {
                                    if (args.length == 4) {
                                        Player target = plugin.getServer().getPlayer(args[2]);
                                        if (target != null) {
                                            if (plugin.isString(args[3])) {
                                                PlayerConnect droneHolder = plugin.get(target.getUniqueId().toString());
                                                droneHolder.setGroup(args[3]);
                                                if (type.equalsIgnoreCase("player")) {
                                                    for (String message : plugin.language.get.getStringList("group.set.message")) {
                                                        plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', message.replace("{player}", sender.getName()).replace("{target}", target.getName()).replace("{group}", args[3])));
                                                    }
                                                } else {
                                                    for (String message : plugin.language.get.getStringList("console.group.set.message")) {
                                                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message.replace("{group}", args[3])));
                                                    }
                                                }
                                            } else {
                                                if (type.equalsIgnoreCase("player")) {
                                                    for (String message : plugin.language.get.getStringList("group.valid")) {
                                                        plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', message.replace("{player}", sender.getName())));
                                                    }
                                                } else {
                                                    for (String message : plugin.language.get.getStringList("console.group.valid")) {
                                                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                                                    }
                                                }
                                            }
                                        } else {
                                            if (type.equalsIgnoreCase("player")) {
                                                for (String message : plugin.language.get.getStringList("group.online")) {
                                                    plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', message.replace("{player}", sender.getName())));
                                                }
                                            } else {
                                                for (String message : plugin.language.get.getStringList("console.group.online")) {
                                                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                                                }
                                            }
                                        }
                                    } else {
                                        if (type.equalsIgnoreCase("player")) {
                                            for (String message : plugin.language.get.getStringList("group.set.usage")) {
                                                plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', message.replace("{player}", sender.getName())));
                                            }
                                        } else {
                                            for (String message : plugin.language.get.getStringList("console.group.set.usage")) {
                                                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                                            }
                                        }
                                    }
                                } else if (args[1].equalsIgnoreCase("reset")) {
                                    if (args.length == 3) {
                                        Player target = plugin.getServer().getPlayer(args[2]);
                                        if (target != null) {
                                            PlayerConnect droneHolder = plugin.get(target.getUniqueId().toString());
                                            droneHolder.setGroup("default");
                                            if (type.equalsIgnoreCase("player")) {
                                                for (String message : plugin.language.get.getStringList("group.reset.message")) {
                                                    plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', message.replace("{player}", sender.getName()).replace("{target}", target.getName())));
                                                }
                                            } else {
                                                for (String message : plugin.language.get.getStringList("console.group.reset.message")) {
                                                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                                                }
                                            }
                                        } else {
                                            if (type.equalsIgnoreCase("player")) {
                                                for (String message : plugin.language.get.getStringList("group.online")) {
                                                    plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', message.replace("{player}", sender.getName())));
                                                }
                                            } else {
                                                for (String message : plugin.language.get.getStringList("console.group.online")) {
                                                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                                                }
                                            }
                                        }
                                    } else {
                                        if (type.equalsIgnoreCase("player")) {
                                            for (String message : plugin.language.get.getStringList("group.reset.usage")) {
                                                plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', message.replace("{player}", sender.getName())));
                                            }
                                        } else {
                                            for (String message : plugin.language.get.getStringList("console.group.reset.usage")) {
                                                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                                            }
                                        }
                                    }
                                } else {
                                    if (type.equalsIgnoreCase("player")) {
                                        for (String message : plugin.language.get.getStringList("group.usage")) {
                                            plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', message.replace("{player}", sender.getName())));
                                        }
                                    } else {
                                        for (String message : plugin.language.get.getStringList("console.group.usage")) {
                                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                                        }
                                    }
                                }
                            } else {
                                if (type.equalsIgnoreCase("player")) {
                                    for (String message : plugin.language.get.getStringList("group.usage")) {
                                        plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', message.replace("{player}", sender.getName())));
                                    }
                                } else {
                                    for (String message : plugin.language.get.getStringList("console.group.usage")) {
                                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                                    }
                                }
                            }
                        } else {
                            if (type.equalsIgnoreCase("player")) {
                                for (String message : plugin.language.get.getStringList("group.permission")) {
                                    plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', message.replace("{player}", sender.getName())));
                                }
                            }
                        }
                    } else if (args[0].equalsIgnoreCase("message")) {
                        unknown = false;
                        if (sender.hasPermission("battledrones.admin.message")) {
                            if (args.length <= 2) {
                                if (type.equalsIgnoreCase("player")) {
                                    for (String message : plugin.language.get.getStringList("message.usage")) {
                                        plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', message.replace("{player}", sender.getName())));
                                    }
                                } else {
                                    for (String message : plugin.language.get.getStringList("console.message.usage")) {
                                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                                    }
                                }
                            } else {
                                final Player target = plugin.getServer().getPlayer(args[1]);
                                if (target != null) {
                                    final StringBuilder sb = new StringBuilder();
                                    for (int i = 2; i < args.length; i++) {
                                        sb.append(args[i]).append(" ");
                                    }
                                    final String text = sb.toString().trim();
                                    if (!text.contains("\\n")) {
                                        target.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.replacePlaceholders(target, text)));
                                    } else {
                                        for (String message : text.split(Pattern.quote("\\n"))) {
                                            target.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.replacePlaceholders(target, message)));
                                        }
                                    }
                                } else {
                                    if (type.equalsIgnoreCase("player")) {
                                        for (String message : plugin.language.get.getStringList("message.online")) {
                                            plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', message.replace("{player}", sender.getName())));
                                        }
                                    } else {
                                        for (String message : plugin.language.get.getStringList("console.message.online")) {
                                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                                        }
                                    }
                                }
                            }
                        } else {
                            if (type.equalsIgnoreCase("player")) {
                                for (String message : plugin.language.get.getStringList("message.permission")) {
                                    plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', message.replace("{player}", sender.getName())));
                                }
                            }
                        }
                    } else if (args[0].equalsIgnoreCase("actionbar")) {
                        unknown = false;
                        if (sender.hasPermission("battledrones.admin.actionbar")) {
                            if (args.length > 3) {
                                final Player target = plugin.getServer().getPlayer(args[1]);
                                if (target != null) {
                                    if (plugin.isInt(args[2])) {
                                        final StringBuilder sb = new StringBuilder();
                                        for (int i = 3; i < args.length; i++) {
                                            sb.append(args[i]).append(" ");
                                        }
                                        try {
                                            int seconds = Integer.parseInt(args[2]);
                                            new BukkitRunnable() {
                                                int time = 0;

                                                @Override
                                                public void run() {
                                                    if (time >= seconds) {
                                                        this.cancel();
                                                    } else {
                                                        time++;
                                                        target.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', plugin.replacePlaceholders(target, sb.toString().trim()))));
                                                    }
                                                }
                                            }.runTaskTimer(plugin, 0, 20);
                                        } catch (NoSuchMethodError exception) {
                                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cThis command is not supported in this version."));
                                        }
                                    } else {
                                        if (type.equalsIgnoreCase("player")) {
                                            for (String message : plugin.language.get.getStringList("actionbar.number")) {
                                                plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', message.replace("{player}", sender.getName())));
                                            }
                                        } else {
                                            for (String message : plugin.language.get.getStringList("console.actionbar.number")) {
                                                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                                            }
                                        }
                                    }
                                } else {
                                    if (type.equalsIgnoreCase("player")) {
                                        for (String message : plugin.language.get.getStringList("actionbar.online")) {
                                            plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', message.replace("{player}", sender.getName())));
                                        }
                                    } else {
                                        for (String message : plugin.language.get.getStringList("console.actionbar.online")) {
                                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                                        }
                                    }
                                }
                            } else {
                                if (type.equalsIgnoreCase("player")) {
                                    for (String message : plugin.language.get.getStringList("actionbar.usage")) {
                                        plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', message.replace("{player}", sender.getName())));
                                    }
                                } else {
                                    for (String message : plugin.language.get.getStringList("console.actionbar.usage")) {
                                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                                    }
                                }
                            }
                        } else {
                            if (type.equalsIgnoreCase("player")) {
                                for (String message : plugin.language.get.getStringList("actionbar.permission")) {
                                    plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', message.replace("{player}", sender.getName())));
                                }
                            }
                        }
                    } else if (args[0].equalsIgnoreCase("broadcast")) {
                        unknown = false;
                        if (sender.hasPermission("battledrones.admin.broadcast")) {
                            if (args.length > 2) {
                                StringBuilder sb = new StringBuilder();
                                for (int i = 2; i < args.length; i++) {
                                    sb.append(args[i]).append(" ");
                                }
                                String text = sb.toString().trim();
                                if (!text.contains("\\n")) {
                                    if (type.equalsIgnoreCase("player")) {
                                        text = plugin.replacePlaceholders((Player) sender, text);
                                    }
                                    broadcast(ChatColor.translateAlternateColorCodes('&', text), args);
                                } else {
                                    for (String message : text.split(Pattern.quote("\\n"))) {
                                        if (type.equalsIgnoreCase("player")) {
                                            message = plugin.replacePlaceholders((Player) sender, message);
                                        }
                                        broadcast(ChatColor.translateAlternateColorCodes('&', message), args);
                                    }
                                }
                            } else {
                                if (type.equalsIgnoreCase("player")) {
                                    for (String message : plugin.language.get.getStringList("broadcast.usage")) {
                                        plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', message.replace("{player}", sender.getName())));
                                    }
                                } else {
                                    for (String message : plugin.language.get.getStringList("console.broadcast.usage")) {
                                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                                    }
                                }
                            }
                        } else {
                            if (type.equalsIgnoreCase("player")) {
                                for (String message : plugin.language.get.getStringList("broadcast.permission")) {
                                    plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', message));
                                }
                            }
                        }
                    } else if (args[0].equalsIgnoreCase("coins")) {
                        unknown = false;
                        if (sender.hasPermission("battledrones.admin.coins")) {
                            if (args.length > 1) {
                                if (args[1].equalsIgnoreCase("set") || args[1].equalsIgnoreCase("add") || args[1].equalsIgnoreCase("remove")) {
                                    if (args.length == 4) {
                                        Player target = plugin.getServer().getPlayer(args[2]);
                                        if (target != null) {
                                            if (plugin.isInt(args[3])) {
                                                PlayerConnect playerConnect = plugin.get(target.getUniqueId().toString());
                                                if (args[1].equalsIgnoreCase("set")) {
                                                    long set = Long.parseLong(args[3]);
                                                    if (set >= 0) {
                                                        playerConnect.setCoins(set);
                                                        playerConnect.save();
                                                        if (type.equalsIgnoreCase("player")) {
                                                            for (String message : plugin.language.get.getStringList("coins.set")) {
                                                                plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', message.replace("{player}", sender.getName()).replace("{target}", target.getName()).replace("{coins}", args[3])));
                                                            }
                                                        } else {
                                                            for (String message : plugin.language.get.getStringList("console.coins.set")) {
                                                                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message.replace("{target}", target.getName()).replace("{coins}", args[3])));
                                                            }
                                                        }
                                                    } else {
                                                        if (type.equalsIgnoreCase("player")) {
                                                            for (String message : plugin.language.get.getStringList("coins.0")) {
                                                                plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', message.replace("{player}", sender.getName())));
                                                            }
                                                        } else {
                                                            for (String message : plugin.language.get.getStringList("console.coins.0")) {
                                                                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                                                            }
                                                        }
                                                    }
                                                } else if (args[1].equalsIgnoreCase("add")) {
                                                    long set = playerConnect.getCoins() + Long.parseLong(args[3]);
                                                    if (set >= 0) {
                                                        playerConnect.setCoins(set);
                                                        playerConnect.save();
                                                        if (type.equalsIgnoreCase("player")) {
                                                            for (String message : plugin.language.get.getStringList("coins.add")) {
                                                                plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', message.replace("{player}", sender.getName()).replace("{target}", target.getName()).replace("{coins}", args[3])));
                                                            }
                                                        } else {
                                                            for (String message : plugin.language.get.getStringList("console.coins.add")) {
                                                                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message.replace("{target}", target.getName()).replace("{coins}", args[3])));
                                                            }
                                                        }
                                                    } else {
                                                        if (type.equalsIgnoreCase("player")) {
                                                            for (String message : plugin.language.get.getStringList("coins.0")) {
                                                                plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', message.replace("{player}", sender.getName())));
                                                            }
                                                        } else {
                                                            for (String message : plugin.language.get.getStringList("console.coins.0")) {
                                                                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                                                            }
                                                        }
                                                    }
                                                } else if (args[1].equalsIgnoreCase("remove")) {
                                                    long set = playerConnect.getCoins() - Long.parseLong(args[3]);
                                                    if (set >= 0) {
                                                        playerConnect.setCoins(set);
                                                        playerConnect.save();
                                                        if (type.equalsIgnoreCase("player")) {
                                                            for (String message : plugin.language.get.getStringList("coins.remove")) {
                                                                plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', message.replace("{player}", sender.getName()).replace("{target}", target.getName()).replace("{coins}", args[3])));
                                                            }
                                                        } else {
                                                            for (String message : plugin.language.get.getStringList("console.coins.remove")) {
                                                                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message.replace("{target}", target.getName()).replace("{coins}", args[3])));
                                                            }
                                                        }
                                                    } else {
                                                        if (type.equalsIgnoreCase("player")) {
                                                            for (String message : plugin.language.get.getStringList("coins.0")) {
                                                                plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', message.replace("{player}", sender.getName())));
                                                            }
                                                        } else {
                                                            for (String message : plugin.language.get.getStringList("console.coins.0")) {
                                                                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                                                            }
                                                        }
                                                    }
                                                }
                                            } else {
                                                if (type.equalsIgnoreCase("player")) {
                                                    for (String message : plugin.language.get.getStringList("coins.number")) {
                                                        plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', message.replace("{player}", sender.getName())));
                                                    }
                                                } else {
                                                    for (String message : plugin.language.get.getStringList("console.coins.number")) {
                                                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                                                    }
                                                }
                                            }
                                        } else {
                                            if (type.equalsIgnoreCase("player")) {
                                                for (String message : plugin.language.get.getStringList("coins.online")) {
                                                    plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', message.replace("{player}", sender.getName())));
                                                }
                                            } else {
                                                for (String message : plugin.language.get.getStringList("console.coins.online")) {
                                                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                                                }
                                            }
                                        }
                                    } else {
                                        if (type.equalsIgnoreCase("player")) {
                                            for (String message : plugin.language.get.getStringList("coins.usage")) {
                                                plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', message.replace("{player}", sender.getName())));
                                            }
                                        } else {
                                            for (String message : plugin.language.get.getStringList("console.coins.usage")) {
                                                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                                            }
                                        }
                                    }
                                } else {
                                    if (type.equalsIgnoreCase("player")) {
                                        for (String message : plugin.language.get.getStringList("coins.usage")) {
                                            plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', message.replace("{player}", sender.getName())));
                                        }
                                    } else {
                                        for (String message : plugin.language.get.getStringList("console.coins.usage")) {
                                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                                        }
                                    }
                                }
                            } else {
                                if (type.equalsIgnoreCase("player")) {
                                    for (String message : plugin.language.get.getStringList("coins.usage")) {
                                        plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', message.replace("{player}", sender.getName())));
                                    }
                                } else {
                                    for (String message : plugin.language.get.getStringList("console.coins.usage")) {
                                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                                    }
                                }
                            }
                        } else {
                            if (type.equalsIgnoreCase("player")) {
                                for (String message : plugin.language.get.getStringList("coins.permission")) {
                                    plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', message.replace("{player}", sender.getName())));
                                }
                            }
                        }
                    } else if (args[0].equalsIgnoreCase("give")) {
                        unknown = false;
                        if (sender.hasPermission("battledrones.admin.give")) {
                            if (args.length > 1) {
                                if (args[1].equalsIgnoreCase("ammo")) {
                                    if (args.length == 5) {
                                        Player target = plugin.getServer().getPlayer(args[3]);
                                        if (target != null) {
                                            if (plugin.isInt(args[4]) && Integer.parseInt(args[4]) > 0 && Integer.parseInt(args[4]) < 65) {
                                                if (plugin.drones.contains(args[2])) {
                                                    FileConfiguration file = plugin.droneFiles.get(args[2]);
                                                    final String material = file.getString("gui.AMMO.MATERIAL");
                                                    try {
                                                        ItemStack itemStack = plugin.getItemStack(material, Integer.parseInt(args[4]));
                                                        ItemMeta itemMeta = itemStack.getItemMeta();
                                                        if (itemMeta != null) {
                                                            itemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(file.getString("gui.AMMO.NAME"))));
                                                            ArrayList<String> lores = new ArrayList<>();
                                                            for (String lore : file.getStringList("gui.AMMO.LORES")) {
                                                                lores.add(ChatColor.translateAlternateColorCodes('&', lore));
                                                            }
                                                            itemMeta.setLore(lores);
                                                            itemStack.setItemMeta(itemMeta);
                                                            plugin.guiManager.glow(itemStack, file, "gui.AMMO.OPTIONS");
                                                            if (target.getInventory().firstEmpty() == -1) {
                                                                target.getWorld().dropItem(target.getLocation().add(0, 1, 0), itemStack);
                                                            } else {
                                                                target.getInventory().addItem(itemStack);
                                                            }
                                                            if (type.equalsIgnoreCase("player")) {
                                                                for (String message : plugin.language.get.getStringList("give.ammo.message")) {
                                                                    plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', message.replace("{player}", sender.getName()).replace("{amount}", args[4]).replace("{target}", target.getName()).replace("{drone}", args[2])));
                                                                }
                                                            } else {
                                                                for (String message : plugin.language.get.getStringList("console.give.ammo.message")) {
                                                                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message.replace("{amount}", args[4]).replace("{target}", target.getName()).replace("{drone}", args[2])));
                                                                }
                                                            }
                                                        } else {
                                                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cAn error has occurred."));
                                                        }
                                                    } catch (NullPointerException e) {
                                                        plugin.textUtils.gui(sender, "ammo", material);
                                                    }
                                                } else {
                                                    if (type.equalsIgnoreCase("player")) {
                                                        for (String message : plugin.language.get.getStringList("give.ammo.usage")) {
                                                            plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', message.replace("{player}", sender.getName())));
                                                        }
                                                    } else {
                                                        for (String message : plugin.language.get.getStringList("console.give.ammo.usage")) {
                                                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                                                        }
                                                    }
                                                }
                                            } else {
                                                if (type.equalsIgnoreCase("player")) {
                                                    for (String message : plugin.language.get.getStringList("give.number")) {
                                                        plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', message.replace("{player}", sender.getName())));
                                                    }
                                                } else {
                                                    for (String message : plugin.language.get.getStringList("console.give.number")) {
                                                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                                                    }
                                                }
                                            }
                                        } else {
                                            if (type.equalsIgnoreCase("player")) {
                                                for (String message : plugin.language.get.getStringList("give.online")) {
                                                    plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', message.replace("{player}", sender.getName())));
                                                }
                                            } else {
                                                for (String message : plugin.language.get.getStringList("console.give.online")) {
                                                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                                                }
                                            }
                                        }
                                    } else {
                                        if (type.equalsIgnoreCase("player")) {
                                            for (String message : plugin.language.get.getStringList("give.ammo.usage")) {
                                                plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', message.replace("{player}", sender.getName())));
                                            }
                                        } else {
                                            for (String message : plugin.language.get.getStringList("console.give.ammo.usage")) {
                                                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                                            }
                                        }
                                    }
                                } else {
                                    if (type.equalsIgnoreCase("player")) {
                                        for (String message : plugin.language.get.getStringList("give.usage")) {
                                            plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', message.replace("{player}", sender.getName())));
                                        }
                                    } else {
                                        for (String message : plugin.language.get.getStringList("console.give.usage")) {
                                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                                        }
                                    }
                                }
                            } else {
                                if (type.equalsIgnoreCase("player")) {
                                    for (String message : plugin.language.get.getStringList("give.usage")) {
                                        plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', message.replace("{player}", sender.getName())));
                                    }
                                } else {
                                    for (String message : plugin.language.get.getStringList("console.give.usage")) {
                                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                                    }
                                }
                            }
                        } else {
                            if (type.equalsIgnoreCase("player")) {
                                for (String message : plugin.language.get.getStringList("give.permission")) {
                                    plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', message.replace("{player}", sender.getName())));
                                }
                            }
                        }
                    } else if (args[0].equalsIgnoreCase("sound")) {
                        unknown = false;
                        if (sender.hasPermission("battledrones.admin.sound")) {
                            if (args.length > 1) {
                                try {
                                    Sound sound = Sound.valueOf(args[1].toUpperCase());
                                    if (args.length > 7) {
                                        World world = plugin.getServer().getWorld(args[2]);
                                        if (world != null) {
                                            if (plugin.isInt(args[3]) && plugin.isInt(args[4]) && plugin.isInt(args[5]) && plugin.isFloat(args[6]) && plugin.isFloat(args[7])) {
                                                if (args.length == 8) {
                                                    world.playSound(new Location(world, Integer.parseInt(args[3]), Integer.parseInt(args[4]), Integer.parseInt(args[5])), sound, Float.parseFloat(args[6]), Float.parseFloat(args[7]));
                                                } else {
                                                    Player target = plugin.getServer().getPlayer(args[8]);
                                                    if (target != null) {
                                                        target.playSound(new Location(world, Integer.parseInt(args[3]), Integer.parseInt(args[4]), Integer.parseInt(args[5])), sound, Float.parseFloat(args[6]), Float.parseFloat(args[7]));
                                                    } else {
                                                        if (type.equalsIgnoreCase("player")) {
                                                            for (String message : plugin.language.get.getStringList("sound.online")) {
                                                                plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', message.replace("{player}", sender.getName())));
                                                            }
                                                        } else {
                                                            for (String message : plugin.language.get.getStringList("console.sound.online")) {
                                                                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                                                            }
                                                        }
                                                    }
                                                }
                                            } else {
                                                if (type.equalsIgnoreCase("player")) {
                                                    for (String message : plugin.language.get.getStringList("sound.number")) {
                                                        plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', message.replace("{player}", sender.getName())));
                                                    }
                                                } else {
                                                    for (String message : plugin.language.get.getStringList("console.sound.number")) {
                                                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                                                    }
                                                }
                                            }
                                        } else {
                                            if (type.equalsIgnoreCase("player")) {
                                                for (String message : plugin.language.get.getStringList("sound.world")) {
                                                    plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', message.replace("{player}", sender.getName())));
                                                }
                                            } else {
                                                for (String message : plugin.language.get.getStringList("console.sound.world")) {
                                                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                                                }
                                            }
                                        }
                                    } else {
                                        if (type.equalsIgnoreCase("player")) {
                                            for (String message : plugin.language.get.getStringList("sound.usage")) {
                                                plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', message.replace("{player}", sender.getName())));
                                            }
                                        } else {
                                            for (String message : plugin.language.get.getStringList("console.sound.usage")) {
                                                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                                            }
                                        }
                                    }
                                } catch (IllegalArgumentException e) {
                                    if (type.equalsIgnoreCase("player")) {
                                        for (String message : plugin.language.get.getStringList("sound.found")) {
                                            plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', message.replace("{player}", sender.getName())));
                                        }
                                    } else {
                                        for (String message : plugin.language.get.getStringList("console.sound.found")) {
                                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                                        }
                                    }
                                }
                            } else {
                                ArrayList<String> list = new ArrayList<>();
                                for (Sound sound : Sound.values()) {
                                    list.add(sound.name().toLowerCase());
                                }
                                final String stringList = list.toString().replace("[", "").replace("]", "");
                                if (type.equalsIgnoreCase("player")) {
                                    for (String message : plugin.language.get.getStringList("sound.names")) {
                                        plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', message.replace("{player}", sender.getName()).replace("{sound_names}", stringList)));
                                    }
                                } else {
                                    for (String message : plugin.language.get.getStringList("console.sound.names")) {
                                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message.replace("{sound_names}", stringList)));
                                    }
                                }
                            }
                        } else {
                            if (type.equalsIgnoreCase("player")) {
                                for (String message : plugin.language.get.getStringList("sound.permission")) {
                                    plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', message.replace("{player}", sender.getName())));
                                }
                            }
                        }
                    }
                    if (unknown) {
                        if (type.equalsIgnoreCase("player")) {
                            for (String message : plugin.language.get.getStringList("command.unknown")) {
                                plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', message.replace("{player}", sender.getName()).replace("{command}", args[0])));
                            }
                        } else {
                            for (String message : plugin.language.get.getStringList("console.command.unknown")) {
                                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message.replace("{command}", args[0])));
                            }
                        }
                    }
                }
            } else {
                if (type.equalsIgnoreCase("player")) {
                    for (String message : plugin.language.get.getStringList("command.permission")) {
                        plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', message.replace("{player}", sender.getName())));
                    }
                }
            }
        }
        return true;
    }

    private void broadcast(final String text, final String[] args) {
        if (args[1].equalsIgnoreCase("null")) {
            plugin.getServer().broadcastMessage(text);
        } else {
            plugin.getServer().broadcast(text, args[1]);
        }
    }

    private void park(final Player player, final PlayerConnect playerConnect) {
        final String uuid = player.getUniqueId().toString();
        if (plugin.park.contains(uuid)) {
            plugin.park.remove(uuid);
            for (String message : plugin.language.get.getStringList("park.follow")) {
                plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', message.replace("{player}", player.getName())));
            }
        } else {
            final long coins = playerConnect.getCoins();
            if (!plugin.listDroneHolder().contains(uuid) || !plugin.getDroneHolderUUID(uuid).containsKey(playerConnect.getActive())) {
                plugin.loadDroneHolder(uuid, playerConnect.getActive());
            }
            final DroneHolder droneHolder = plugin.getDroneHolder(uuid, playerConnect.getActive());
            final FileConfiguration file = plugin.droneFiles.get(playerConnect.getActive());
            final String path = playerConnect.getGroup() + "." + droneHolder.getLevel() + ".park";
            if (file.contains(path)) {
                final int cost = file.getInt(path);
                if (!plugin.config.get.getBoolean("vault") && coins >= cost ||
                        plugin.config.get.getBoolean("vault") &&
                                plugin.getEconomy() != null &&
                                plugin.getEconomy().withdrawPlayer(player, cost).transactionSuccess()) {
                    if (!plugin.config.get.getBoolean("vault")) {
                        playerConnect.setCoins(coins - cost);
                    }
                    plugin.park.add(uuid);
                    for (String message : plugin.language.get.getStringList("park.parked")) {
                        plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', message.replace("{player}", player.getName()).replace("{cost}", String.valueOf(cost))));
                    }
                } else {
                    for (String message : plugin.language.get.getStringList("park.enough")) {
                        plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', message.replace("{player}", player.getName()).replace("{cost}", String.valueOf(cost))));
                    }
                }
            } else {
                for (String message : plugin.language.get.getStringList("park.drone")) {
                    plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', message.replace("{player}", player.getName())));
                }
            }
        }
    }
}