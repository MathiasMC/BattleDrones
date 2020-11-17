package me.MathiasMC.BattleDrones.commands;

import me.MathiasMC.BattleDrones.BattleDrones;
import me.MathiasMC.BattleDrones.api.Type;
import me.MathiasMC.BattleDrones.api.events.DroneParkEvent;
import me.MathiasMC.BattleDrones.api.events.DroneRemoveEvent;
import me.MathiasMC.BattleDrones.data.DroneHolder;
import me.MathiasMC.BattleDrones.data.PlayerConnect;
import me.MathiasMC.BattleDrones.api.events.DroneSpawnEvent;
import me.MathiasMC.BattleDrones.gui.menu.DroneGUI;
import me.MathiasMC.BattleDrones.gui.menu.SelectGUI;
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
import org.bukkit.persistence.PersistentDataType;
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
                        for (String message : plugin.getFileUtils().language.getStringList("command.message")) {
                            plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', message.replace("{player}", sender.getName()).replace("{version}", plugin.getDescription().getVersion())));
                        }
                    } else {
                        for (String message : plugin.getFileUtils().language.getStringList("console.command.message")) {
                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message.replace("{version}", plugin.getDescription().getVersion())));
                        }
                    }
                } else {
                    if (args[0].equalsIgnoreCase("help")) {
                        unknown = false;
                        if (type.equalsIgnoreCase("player")) {
                            if (sender.hasPermission("battledrones.admin.help")) {
                                for (String message : plugin.getFileUtils().language.getStringList("help.admin")) {
                                    plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', message.replace("{player}", sender.getName())));
                                }
                            } else if (sender.hasPermission("battledrones.player.help")) {
                                for (String message : plugin.getFileUtils().language.getStringList("help.player")) {
                                    plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', message.replace("{player}", sender.getName())));
                                }
                            } else {
                                for (String message : plugin.getFileUtils().language.getStringList("help.permission")) {
                                    plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', message.replace("{player}", sender.getName())));
                                }
                            }
                        } else {
                            for (String message : plugin.getFileUtils().language.getStringList("console.help.message")) {
                                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                            }
                        }
                    } else if (args[0].equalsIgnoreCase("shop")) {
                        unknown = false;
                        if (sender.hasPermission("battledrones.player.shop")) {
                            if (type.equalsIgnoreCase("player")) {
                                Player player = (Player) sender;
                                new SelectGUI(plugin.getPlayerMenu(player), "shop").open();
                            } else {
                                if (args.length == 2) {
                                    Player target = plugin.getServer().getPlayer(args[1]);
                                    if (target != null) {
                                        new SelectGUI(plugin.getPlayerMenu(target), "shop").open();
                                    } else {
                                        for (String message : plugin.getFileUtils().language.getStringList("console.shop.online")) {
                                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                                        }
                                    }
                                } else {
                                    for (String message : plugin.getFileUtils().language.getStringList("console.shop.usage")) {
                                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                                    }
                                }
                            }
                        } else {
                            if (type.equalsIgnoreCase("player")) {
                                for (String message : plugin.getFileUtils().language.getStringList("shop.permission")) {
                                    plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', message.replace("{player}", sender.getName())));
                                }
                            }
                        }
                    } else if (args[0].equalsIgnoreCase("menu")) {
                        unknown = false;
                        if (sender.hasPermission("battledrones.player.menu")) {
                            if (type.equalsIgnoreCase("player")) {
                                Player player = (Player) sender;
                                new SelectGUI(plugin.getPlayerMenu(player), "player").open();
                            } else {
                                if (args.length == 2) {
                                    Player target = plugin.getServer().getPlayer(args[1]);
                                    if (target != null) {
                                        new SelectGUI(plugin.getPlayerMenu(target), "player").open();
                                    } else {
                                        for (String message : plugin.getFileUtils().language.getStringList("console.menu.online")) {
                                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                                        }
                                    }
                                } else {
                                    for (String message : plugin.getFileUtils().language.getStringList("console.menu.usage")) {
                                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                                    }
                                }
                            }
                        } else {
                            if (type.equalsIgnoreCase("player")) {
                                for (String message : plugin.getFileUtils().language.getStringList("menu.permission")) {
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
                                final PlayerConnect playerConnect = plugin.getPlayerConnect(uuid);
                                if (playerConnect.isActive()) {
                                    new DroneGUI(plugin.getPlayerMenu(player), playerConnect.getActive()).open();
                                } else {
                                    for (String message : plugin.getFileUtils().language.getStringList("drone.active")) {
                                        plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', message.replace("{player}", sender.getName())));
                                    }
                                }
                            } else {
                                if (args.length == 2) {
                                    final Player target = plugin.getServer().getPlayer(args[1]);
                                    if (target != null) {
                                        final String uuid = target.getUniqueId().toString();
                                        final PlayerConnect playerConnect = plugin.getPlayerConnect(uuid);
                                        if (playerConnect.isActive()) {
                                            new DroneGUI(plugin.getPlayerMenu(target), playerConnect.getActive()).open();
                                        } else {
                                            for (String message : plugin.getFileUtils().language.getStringList("drone.active")) {
                                                plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', message.replace("{player}", target.getName())));
                                            }
                                        }
                                    } else {
                                        for (String message : plugin.getFileUtils().language.getStringList("console.drone.online")) {
                                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                                        }
                                    }
                                } else {
                                    for (String message : plugin.getFileUtils().language.getStringList("console.drone.usage")) {
                                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                                    }
                                }
                            }
                        } else {
                            if (type.equalsIgnoreCase("player")) {
                                for (String message : plugin.getFileUtils().language.getStringList("drone.permission")) {
                                    plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', message.replace("{player}", sender.getName())));
                                }
                            }
                        }
                    } else if (args[0].equalsIgnoreCase("activate")) {
                        unknown = false;
                        if (sender.hasPermission("battledrones.player.activate")) {
                            if (type.equalsIgnoreCase("player")) {
                                if (args.length == 2) {
                                    if (plugin.drones.containsKey(args[1])) {
                                        final Player player = (Player) sender;
                                        final DroneSpawnEvent droneSpawnEvent = new DroneSpawnEvent(player, plugin.getPlayerConnect(player.getUniqueId().toString()), plugin.getDroneHolder(player.getUniqueId().toString(), plugin.drones.get(args[1])));
                                        droneSpawnEvent.setBypassWait(false);
                                        droneSpawnEvent.setBypassDroneAmount(false);
                                        droneSpawnEvent.setBypassLocation(false);
                                        droneSpawnEvent.setType(Type.COMMAND);
                                        droneSpawnEvent.spawn();
                                    } else {
                                        for (String message : plugin.getFileUtils().language.getStringList("activate.valid")) {
                                            plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', message.replace("{player}", sender.getName()).replace("{drone}", args[1])));
                                        }
                                    }
                                } else {
                                    for (String message : plugin.getFileUtils().language.getStringList("activate.usage")) {
                                        plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', message.replace("{player}", sender.getName())));
                                    }
                                }
                            } else {
                                if (args.length == 3) {
                                    final Player target = plugin.getServer().getPlayer(args[2]);
                                    if (target != null) {
                                        if (plugin.drones.containsKey(args[1])) {
                                            final DroneSpawnEvent droneSpawnEvent = new DroneSpawnEvent(target, plugin.getPlayerConnect(target.getUniqueId().toString()), plugin.getDroneHolder(target.getUniqueId().toString(), plugin.drones.get(args[1])));
                                            droneSpawnEvent.setBypassWait(true);
                                            droneSpawnEvent.setBypassDroneAmount(true);
                                            droneSpawnEvent.setBypassLocation(true);
                                            droneSpawnEvent.setType(Type.COMMAND);
                                            droneSpawnEvent.spawn();
                                        } else {
                                            for (String message : plugin.getFileUtils().language.getStringList("console.activate.valid")) {
                                                plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', message.replace("{drone}", args[1])));
                                            }
                                        }
                                    } else {
                                        for (String message : plugin.getFileUtils().language.getStringList("console.activate.online")) {
                                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                                        }
                                    }
                                } else {
                                    for (String message : plugin.getFileUtils().language.getStringList("console.activate.usage")) {
                                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                                    }
                                }
                            }
                        } else {
                            if (type.equalsIgnoreCase("player")) {
                                for (String message : plugin.getFileUtils().language.getStringList("activate.permission")) {
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
                                    final PlayerConnect playerConnect = plugin.getPlayerConnect(uuid);
                                    if (playerConnect.isActive()) {
                                        final FileConfiguration file = plugin.droneFiles.get(playerConnect.getActive());
                                        final DroneHolder droneHolder = plugin.getDroneHolder(uuid, playerConnect.getActive());
                                        final DroneRemoveEvent droneRemoveEvent = new DroneRemoveEvent(player, playerConnect, droneHolder);
                                        droneRemoveEvent.setType(Type.COMMAND);
                                        droneRemoveEvent.setRemoveCommands(null);
                                        droneRemoveEvent.remove();
                                        plugin.getDroneManager().waitSchedule(uuid, file);
                                    } else {
                                        for (String message : plugin.getFileUtils().language.getStringList("deactivate.own")) {
                                            plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', message.replace("{player}", sender.getName())));
                                        }
                                    }
                                } else {
                                    for (String message : plugin.getFileUtils().language.getStringList("console.deactivate.other.usage")) {
                                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                                    }
                                }
                            } else {
                                if (sender.hasPermission("battledrones.admin.deactivate")) {
                                    if (args.length == 3) {
                                        if (plugin.getCalculateManager().isInt(args[2])) {
                                            if (args[1].equalsIgnoreCase("all")) {
                                                plugin.drone_wait.clear();
                                                for (Player player : plugin.getServer().getOnlinePlayers()) {
                                                    final String uuid = player.getUniqueId().toString();
                                                    final PlayerConnect playerConnect = plugin.getPlayerConnect(uuid);
                                                    if (playerConnect.isActive()) {
                                                        final DroneHolder droneHolder = plugin.getDroneHolder(uuid, playerConnect.getActive());
                                                        final DroneRemoveEvent droneRemoveEvent = new DroneRemoveEvent(player, playerConnect, droneHolder);
                                                        droneRemoveEvent.setType(Type.COMMAND_ALL);
                                                        droneRemoveEvent.setRemoveCommands(null);
                                                        droneRemoveEvent.remove();
                                                        plugin.drone_wait.add(uuid);
                                                        plugin.getServer().getScheduler().runTaskLater(plugin, () -> plugin.drone_wait.remove(uuid), Integer.parseInt(args[2]) * 20);
                                                    }
                                                }
                                                if (type.equalsIgnoreCase("player")) {
                                                    for (String command : plugin.getFileUtils().language.getStringList("deactivate.other.all")) {
                                                        plugin.getServer().dispatchCommand(plugin.consoleSender, command.replace("{player}", sender.getName()).replace("{amount}", args[2]));
                                                    }
                                                } else {
                                                    for (String command : plugin.getFileUtils().language.getStringList("console.deactivate.other.all")) {
                                                        plugin.getServer().dispatchCommand(plugin.consoleSender, command.replace("{amount}", args[2]));
                                                    }
                                                }
                                            } else if (plugin.drones.containsKey(args[1])) {
                                                plugin.drone_wait.clear();
                                                for (Player player : plugin.getServer().getOnlinePlayers()) {
                                                    final String uuid = player.getUniqueId().toString();
                                                    final PlayerConnect playerConnect = plugin.getPlayerConnect(uuid);
                                                    if (playerConnect.isActive()) {
                                                        if (plugin.drones.get(args[1]).equalsIgnoreCase(playerConnect.getActive())) {
                                                            final DroneHolder droneHolder = plugin.getDroneHolder(uuid, playerConnect.getActive());
                                                            final DroneRemoveEvent droneRemoveEvent = new DroneRemoveEvent(player, playerConnect, droneHolder);
                                                            droneRemoveEvent.setType(Type.COMMAND_DRONE);
                                                            droneRemoveEvent.setRemoveCommands(null);
                                                            droneRemoveEvent.remove();
                                                            plugin.drone_wait.add(uuid);
                                                            plugin.getServer().getScheduler().runTaskLater(plugin, () -> plugin.drone_wait.remove(uuid), Integer.parseInt(args[2]) * 20);
                                                        }
                                                    }
                                                }
                                                if (type.equalsIgnoreCase("player")) {
                                                    for (String command : plugin.getFileUtils().language.getStringList("deactivate.other.all-drone")) {
                                                        plugin.getServer().dispatchCommand(plugin.consoleSender, command.replace("{player}", sender.getName()).replace("{amount}", args[2]).replace("{drone}", plugin.getPlaceholderManager().getActiveDrone(args[1])));
                                                    }
                                                } else {
                                                    for (String command : plugin.getFileUtils().language.getStringList("console.deactivate.other.all-drone")) {
                                                        plugin.getServer().dispatchCommand(plugin.consoleSender, command.replace("{amount}", args[2]).replace("{drone}", plugin.getPlaceholderManager().getActiveDrone(args[1])));
                                                    }
                                                }
                                            } else {
                                                final Player target = plugin.getServer().getPlayer(args[1]);
                                                if (target != null) {
                                                    final String uuid = target.getUniqueId().toString();
                                                    final PlayerConnect playerConnect = plugin.getPlayerConnect(uuid);
                                                    if (playerConnect.isActive()) {
                                                        final DroneHolder droneHolder = plugin.getDroneHolder(uuid, playerConnect.getActive());
                                                        final DroneRemoveEvent droneRemoveEvent = new DroneRemoveEvent(target, playerConnect, droneHolder);
                                                        droneRemoveEvent.setType(Type.COMMAND_PLAYER);
                                                        droneRemoveEvent.setRemoveCommands(null);
                                                        droneRemoveEvent.remove();
                                                        plugin.drone_wait.add(uuid);
                                                        plugin.getServer().getScheduler().runTaskLater(plugin, () -> plugin.drone_wait.remove(uuid), Integer.parseInt(args[2]) * 20);
                                                        for (String message : plugin.getFileUtils().language.getStringList("deactivate.player")) {
                                                            plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', message.replace("{player}", target.getName()).replace("{amount}", args[2])));
                                                        }
                                                    }
                                                } else {
                                                    if (type.equalsIgnoreCase("player")) {
                                                        for (String message : plugin.getFileUtils().language.getStringList("deactivate.other.online")) {
                                                            plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', message.replace("{player}", sender.getName())));
                                                        }
                                                    } else {
                                                        for (String message : plugin.getFileUtils().language.getStringList("console.deactivate.other.online")) {
                                                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                                                        }
                                                    }
                                                }
                                            }
                                        } else {
                                            if (type.equalsIgnoreCase("player")) {
                                                for (String message : plugin.getFileUtils().language.getStringList("deactivate.other.number")) {
                                                    plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', message.replace("{player}", sender.getName())));
                                                }
                                            } else {
                                                for (String message : plugin.getFileUtils().language.getStringList("console.deactivate.other.number")) {
                                                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                                                }
                                            }
                                        }
                                    } else {
                                        if (type.equalsIgnoreCase("player")) {
                                            for (String message : plugin.getFileUtils().language.getStringList("deactivate.other.usage")) {
                                                plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', message.replace("{player}", sender.getName())));
                                            }
                                        } else {
                                            for (String message : plugin.getFileUtils().language.getStringList("console.deactivate.other.usage")) {
                                                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                                            }
                                        }
                                    }
                                } else {
                                    if (type.equalsIgnoreCase("player")) {
                                        for (String message : plugin.getFileUtils().language.getStringList("deactivate.permission")) {
                                            plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', message.replace("{player}", sender.getName())));
                                        }
                                    }
                                }
                            }
                        } else {
                            if (type.equalsIgnoreCase("player")) {
                                for (String message : plugin.getFileUtils().language.getStringList("deactivate.permission")) {
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
                                final PlayerConnect playerConnect = plugin.getPlayerConnect(uuid);
                                if (playerConnect.isActive()) {
                                    park(player, playerConnect);
                                } else {
                                    for (String message : plugin.getFileUtils().language.getStringList("park.active")) {
                                        plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', message.replace("{player}", sender.getName())));
                                    }
                                }
                            } else {
                                if (args.length == 2) {
                                    final Player target = plugin.getServer().getPlayer(args[1]);
                                    if (target != null) {
                                        final String uuid = target.getUniqueId().toString();
                                        final PlayerConnect playerConnect = plugin.getPlayerConnect(uuid);
                                        if (playerConnect.isActive()) {
                                            park(target, playerConnect);
                                        } else {
                                            for (String message : plugin.getFileUtils().language.getStringList("park.active")) {
                                                plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', message.replace("{player}", target.getName())));
                                            }
                                        }
                                    } else {
                                        for (String message : plugin.getFileUtils().language.getStringList("console.park.online")) {
                                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                                        }
                                    }
                                } else {
                                    for (String message : plugin.getFileUtils().language.getStringList("console.park.usage")) {
                                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                                    }
                                }
                            }
                        } else {
                            if (type.equalsIgnoreCase("player")) {
                                for (String message : plugin.getFileUtils().language.getStringList("park.permission")) {
                                    plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', message.replace("{player}", sender.getName())));
                                }
                            }
                        }
                    } else if (args[0].equalsIgnoreCase("reload")) {
                        unknown = false;
                        if (sender.hasPermission("battledrones.admin.reload")) {
                            if (args.length == 2) {
                                if (args[1].equalsIgnoreCase("all")) {
                                    plugin.getFileUtils().loadConfig();
                                    plugin.getFileUtils().loadLanguage();
                                    plugin.getFileUtils().loadGUIFiles();
                                    plugin.getFileUtils().loadDroneFiles();
                                    plugin.getFileUtils().loadParticles();
                                    plugin.getParticleManager().load();
                                    plugin.addHeads();
                                    plugin.getDroneControllerManager().updateFollowPath();
                                    if (type.equalsIgnoreCase("player")) {
                                        for (String message : plugin.getFileUtils().language.getStringList("reload.all")) {
                                            plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', message.replace("{player}", sender.getName())));
                                        }
                                    } else {
                                        for (String message : plugin.getFileUtils().language.getStringList("console.reload.all")) {
                                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                                        }
                                    }
                                } else if (args[1].equalsIgnoreCase("config")) {
                                    plugin.getFileUtils().loadConfig();
                                    plugin.getDroneControllerManager().updateFollowPath();
                                    if (type.equalsIgnoreCase("player")) {
                                        for (String message : plugin.getFileUtils().language.getStringList("reload.config")) {
                                            plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', message.replace("{player}", sender.getName())));
                                        }
                                    } else {
                                        for (String message : plugin.getFileUtils().language.getStringList("console.reload.config")) {
                                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                                        }
                                    }
                                } else if (args[1].equalsIgnoreCase("language")) {
                                    plugin.getFileUtils().loadLanguage();
                                    if (type.equalsIgnoreCase("player")) {
                                        for (String message : plugin.getFileUtils().language.getStringList("reload.language")) {
                                            plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', message.replace("{player}", sender.getName())));
                                        }
                                    } else {
                                        for (String message : plugin.getFileUtils().language.getStringList("console.reload.language")) {
                                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                                        }
                                    }
                                } else if (args[1].equalsIgnoreCase("particles")) {
                                    plugin.getFileUtils().loadParticles();
                                    plugin.getParticleManager().load();
                                    if (type.equalsIgnoreCase("player")) {
                                        for (String message : plugin.getFileUtils().language.getStringList("reload.particles")) {
                                            plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', message.replace("{player}", sender.getName())));
                                        }
                                    } else {
                                        for (String message : plugin.getFileUtils().language.getStringList("console.reload.particles")) {
                                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                                        }
                                    }
                                } else if (args[1].equalsIgnoreCase("gui")) {
                                    plugin.getFileUtils().loadGUIFiles();
                                    if (type.equalsIgnoreCase("player")) {
                                        for (String message : plugin.getFileUtils().language.getStringList("reload.gui")) {
                                            plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', message.replace("{player}", sender.getName())));
                                        }
                                    } else {
                                        for (String message : plugin.getFileUtils().language.getStringList("console.reload.gui")) {
                                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                                        }
                                    }
                                } else if (args[1].equalsIgnoreCase("drones")) {
                                    plugin.getFileUtils().loadDroneFiles();
                                    if (type.equalsIgnoreCase("player")) {
                                        for (String message : plugin.getFileUtils().language.getStringList("reload.drones")) {
                                            plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', message.replace("{player}", sender.getName())));
                                        }
                                    } else {
                                        for (String message : plugin.getFileUtils().language.getStringList("console.reload.drones")) {
                                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                                        }
                                    }
                                } else if (args[1].equalsIgnoreCase("heads")) {
                                    plugin.getFileUtils().loadHeads();
                                    plugin.addHeads();
                                    if (type.equalsIgnoreCase("player")) {
                                        for (String message : plugin.getFileUtils().language.getStringList("reload.heads")) {
                                            plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', message.replace("{player}", sender.getName())));
                                        }
                                    } else {
                                        for (String message : plugin.getFileUtils().language.getStringList("console.reload.heads")) {
                                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                                        }
                                    }
                                }
                            } else {
                                if (type.equalsIgnoreCase("player")) {
                                    for (String message : plugin.getFileUtils().language.getStringList("reload.usage")) {
                                        plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', message.replace("{player}", sender.getName())));
                                    }
                                } else {
                                    for (String message : plugin.getFileUtils().language.getStringList("console.reload.usage")) {
                                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                                    }
                                }
                            }
                        } else {
                            if (type.equalsIgnoreCase("player")) {
                                for (String message : plugin.getFileUtils().language.getStringList("reload.permission")) {
                                    plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', message.replace("{player}", sender.getName())));
                                }
                            }
                        }
                    } else if (args[0].equalsIgnoreCase("save")) {
                        unknown = false;
                        if (sender.hasPermission("battledrones.admin.save")) {
                            for (String uuid : plugin.listPlayerConnect()) {
                                plugin.getPlayerConnect(uuid).save();
                                if (plugin.listDroneHolder().contains(uuid)) {
                                    for (String drone : plugin.listDroneHolder()) {
                                        plugin.getDroneHolder(uuid, drone).save();
                                    }
                                }
                            }
                            if (type.equalsIgnoreCase("player")) {
                                for (String message : plugin.getFileUtils().language.getStringList("save.message")) {
                                    plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', message.replace("{player}", sender.getName())));
                                }
                            } else {
                                for (String message : plugin.getFileUtils().language.getStringList("console.save.message")) {
                                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                                }
                            }
                        } else {
                            if (type.equalsIgnoreCase("player")) {
                                for (String message : plugin.getFileUtils().language.getStringList("save.permission")) {
                                    plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', message.replace("{player}", sender.getName())));
                                }
                            }
                        }
                    } else if (args[0].equalsIgnoreCase("unlock")) {
                        unknown = false;
                        if (sender.hasPermission("battledrones.admin.unlock")) {
                            if (args.length == 3) {
                                if (plugin.drones.containsKey(args[1])) {
                                    final Player target = plugin.getServer().getPlayer(args[2]);
                                    if (target != null) {
                                        final String targetUUID = target.getUniqueId().toString();
                                        PlayerConnect playerConnect = plugin.getPlayerConnect(targetUUID);
                                        DroneHolder droneHolder = plugin.getDroneHolder(targetUUID, plugin.drones.get(args[1]));
                                        if (droneHolder.getUnlocked() != 1) {
                                            droneHolder.setUnlocked(1);
                                            droneHolder.setHealth(plugin.droneFiles.get(plugin.drones.get(args[1])).getInt(playerConnect.getGroup() + "." + droneHolder.getLevel() + ".health"));
                                        }
                                        droneHolder.save();
                                        if (type.equalsIgnoreCase("player")) {
                                            for (String message : plugin.getFileUtils().language.getStringList("unlock.message")) {
                                                plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', message.replace("{player}", sender.getName()).replace("{drone}", args[1]).replace("{target}", target.getName())));
                                            }
                                        } else {
                                            for (String message : plugin.getFileUtils().language.getStringList("console.unlock.message")) {
                                                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message.replace("{drone}", args[1]).replace("{target}", target.getName())));
                                            }
                                        }
                                    } else {
                                        if (type.equalsIgnoreCase("player")) {
                                            for (String message : plugin.getFileUtils().language.getStringList("unlock.online")) {
                                                plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', message.replace("{player}", sender.getName())));
                                            }
                                        } else {
                                            for (String message : plugin.getFileUtils().language.getStringList("console.unlock.online")) {
                                                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                                            }
                                        }
                                    }
                                } else {
                                    if (type.equalsIgnoreCase("player")) {
                                        for (String message : plugin.getFileUtils().language.getStringList("unlock.type")) {
                                            plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', message.replace("{player}", sender.getName())));
                                        }
                                    } else {
                                        for (String message : plugin.getFileUtils().language.getStringList("console.unlock.type")) {
                                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                                        }
                                    }
                                }
                            } else {
                                if (type.equalsIgnoreCase("player")) {
                                    for (String message : plugin.getFileUtils().language.getStringList("unlock.usage")) {
                                        plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', message.replace("{player}", sender.getName())));
                                    }
                                } else {
                                    for (String message : plugin.getFileUtils().language.getStringList("console.unlock.usage")) {
                                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                                    }
                                }
                            }
                        } else {
                            if (type.equalsIgnoreCase("player")) {
                                for (String message : plugin.getFileUtils().language.getStringList("unlock.permission")) {
                                    plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', message.replace("{player}", sender.getName())));
                                }
                            }
                        }
                    } else if (args[0].equalsIgnoreCase("lock")) {
                        unknown = false;
                        if (sender.hasPermission("battledrones.admin.lock")) {
                            if (args.length == 3) {
                                if (plugin.drones.containsKey(args[1])) {
                                    final Player target = plugin.getServer().getPlayer(args[2]);
                                    if (target != null) {
                                        final String targetUUID = target.getUniqueId().toString();
                                        DroneHolder droneHolder = plugin.getDroneHolder(targetUUID, plugin.drones.get(args[1]));
                                        if (droneHolder.getUnlocked() != 0) {
                                            droneHolder.setUnlocked(0);
                                            droneHolder.setHealth(0);
                                            plugin.getPlayerConnect(targetUUID).stopDrone(true, true);
                                        }
                                        droneHolder.save();
                                        if (type.equalsIgnoreCase("player")) {
                                            for (String message : plugin.getFileUtils().language.getStringList("lock.message")) {
                                                plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', message.replace("{player}", sender.getName()).replace("{drone}", args[1]).replace("{target}", target.getName())));
                                            }
                                        } else {
                                            for (String message : plugin.getFileUtils().language.getStringList("console.lock.message")) {
                                                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message.replace("{drone}", args[1]).replace("{target}", target.getName())));
                                            }
                                        }
                                    } else {
                                        if (type.equalsIgnoreCase("player")) {
                                            for (String message : plugin.getFileUtils().language.getStringList("lock.online")) {
                                                plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', message.replace("{player}", sender.getName())));
                                            }
                                        } else {
                                            for (String message : plugin.getFileUtils().language.getStringList("console.lock.online")) {
                                                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                                            }
                                        }
                                    }
                                } else {
                                    if (type.equalsIgnoreCase("player")) {
                                        for (String message : plugin.getFileUtils().language.getStringList("lock.type")) {
                                            plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', message.replace("{player}", sender.getName())));
                                        }
                                    } else {
                                        for (String message : plugin.getFileUtils().language.getStringList("console.lock.type")) {
                                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                                        }
                                    }
                                }
                            } else {
                                if (type.equalsIgnoreCase("player")) {
                                    for (String message : plugin.getFileUtils().language.getStringList("lock.usage")) {
                                        plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', message.replace("{player}", sender.getName())));
                                    }
                                } else {
                                    for (String message : plugin.getFileUtils().language.getStringList("console.lock.usage")) {
                                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                                    }
                                }
                            }
                        } else {
                            if (type.equalsIgnoreCase("player")) {
                                for (String message : plugin.getFileUtils().language.getStringList("lock.permission")) {
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
                                            if (plugin.getCalculateManager().isString(args[3])) {
                                                PlayerConnect droneHolder = plugin.getPlayerConnect(target.getUniqueId().toString());
                                                droneHolder.setGroup(args[3]);
                                                if (type.equalsIgnoreCase("player")) {
                                                    for (String message : plugin.getFileUtils().language.getStringList("group.set.message")) {
                                                        plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', message.replace("{player}", sender.getName()).replace("{target}", target.getName()).replace("{group}", args[3])));
                                                    }
                                                } else {
                                                    for (String message : plugin.getFileUtils().language.getStringList("console.group.set.message")) {
                                                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message.replace("{group}", args[3])));
                                                    }
                                                }
                                            } else {
                                                if (type.equalsIgnoreCase("player")) {
                                                    for (String message : plugin.getFileUtils().language.getStringList("group.valid")) {
                                                        plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', message.replace("{player}", sender.getName())));
                                                    }
                                                } else {
                                                    for (String message : plugin.getFileUtils().language.getStringList("console.group.valid")) {
                                                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                                                    }
                                                }
                                            }
                                        } else {
                                            if (type.equalsIgnoreCase("player")) {
                                                for (String message : plugin.getFileUtils().language.getStringList("group.online")) {
                                                    plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', message.replace("{player}", sender.getName())));
                                                }
                                            } else {
                                                for (String message : plugin.getFileUtils().language.getStringList("console.group.online")) {
                                                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                                                }
                                            }
                                        }
                                    } else {
                                        if (type.equalsIgnoreCase("player")) {
                                            for (String message : plugin.getFileUtils().language.getStringList("group.set.usage")) {
                                                plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', message.replace("{player}", sender.getName())));
                                            }
                                        } else {
                                            for (String message : plugin.getFileUtils().language.getStringList("console.group.set.usage")) {
                                                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                                            }
                                        }
                                    }
                                } else if (args[1].equalsIgnoreCase("reset")) {
                                    if (args.length == 3) {
                                        Player target = plugin.getServer().getPlayer(args[2]);
                                        if (target != null) {
                                            PlayerConnect droneHolder = plugin.getPlayerConnect(target.getUniqueId().toString());
                                            droneHolder.setGroup("default");
                                            if (type.equalsIgnoreCase("player")) {
                                                for (String message : plugin.getFileUtils().language.getStringList("group.reset.message")) {
                                                    plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', message.replace("{player}", sender.getName()).replace("{target}", target.getName())));
                                                }
                                            } else {
                                                for (String message : plugin.getFileUtils().language.getStringList("console.group.reset.message")) {
                                                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                                                }
                                            }
                                        } else {
                                            if (type.equalsIgnoreCase("player")) {
                                                for (String message : plugin.getFileUtils().language.getStringList("group.online")) {
                                                    plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', message.replace("{player}", sender.getName())));
                                                }
                                            } else {
                                                for (String message : plugin.getFileUtils().language.getStringList("console.group.online")) {
                                                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                                                }
                                            }
                                        }
                                    } else {
                                        if (type.equalsIgnoreCase("player")) {
                                            for (String message : plugin.getFileUtils().language.getStringList("group.reset.usage")) {
                                                plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', message.replace("{player}", sender.getName())));
                                            }
                                        } else {
                                            for (String message : plugin.getFileUtils().language.getStringList("console.group.reset.usage")) {
                                                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                                            }
                                        }
                                    }
                                } else {
                                    if (type.equalsIgnoreCase("player")) {
                                        for (String message : plugin.getFileUtils().language.getStringList("group.usage")) {
                                            plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', message.replace("{player}", sender.getName())));
                                        }
                                    } else {
                                        for (String message : plugin.getFileUtils().language.getStringList("console.group.usage")) {
                                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                                        }
                                    }
                                }
                            } else {
                                if (type.equalsIgnoreCase("player")) {
                                    for (String message : plugin.getFileUtils().language.getStringList("group.usage")) {
                                        plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', message.replace("{player}", sender.getName())));
                                    }
                                } else {
                                    for (String message : plugin.getFileUtils().language.getStringList("console.group.usage")) {
                                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                                    }
                                }
                            }
                        } else {
                            if (type.equalsIgnoreCase("player")) {
                                for (String message : plugin.getFileUtils().language.getStringList("group.permission")) {
                                    plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', message.replace("{player}", sender.getName())));
                                }
                            }
                        }
                    } else if (args[0].equalsIgnoreCase("message")) {
                        unknown = false;
                        if (sender.hasPermission("battledrones.admin.message")) {
                            if (args.length <= 2) {
                                if (type.equalsIgnoreCase("player")) {
                                    for (String message : plugin.getFileUtils().language.getStringList("message.usage")) {
                                        plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', message.replace("{player}", sender.getName())));
                                    }
                                } else {
                                    for (String message : plugin.getFileUtils().language.getStringList("console.message.usage")) {
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
                                        target.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getPlaceholderManager().replacePlaceholders(target, text)));
                                    } else {
                                        for (String message : text.split(Pattern.quote("\\n"))) {
                                            target.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getPlaceholderManager().replacePlaceholders(target, message)));
                                        }
                                    }
                                } else {
                                    if (type.equalsIgnoreCase("player")) {
                                        for (String message : plugin.getFileUtils().language.getStringList("message.online")) {
                                            plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', message.replace("{player}", sender.getName())));
                                        }
                                    } else {
                                        for (String message : plugin.getFileUtils().language.getStringList("console.message.online")) {
                                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                                        }
                                    }
                                }
                            }
                        } else {
                            if (type.equalsIgnoreCase("player")) {
                                for (String message : plugin.getFileUtils().language.getStringList("message.permission")) {
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
                                    if (plugin.getCalculateManager().isInt(args[2])) {
                                        final StringBuilder sb = new StringBuilder();
                                        for (int i = 3; i < args.length; i++) {
                                            sb.append(args[i]).append(" ");
                                        }
                                        try {
                                            int seconds = Integer.parseInt(args[2]);
                                            if (seconds != 0) {
                                                new BukkitRunnable() {
                                                    int time = 0;

                                                    @Override
                                                    public void run() {
                                                        if (time >= seconds) {
                                                            this.cancel();
                                                        } else {
                                                            time++;
                                                            target.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', plugin.getPlaceholderManager().replacePlaceholders(target, sb.toString().trim()))));
                                                        }
                                                    }
                                                }.runTaskTimer(plugin, 0, 20);
                                            } else {
                                                target.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', plugin.getPlaceholderManager().replacePlaceholders(target, sb.toString().trim()))));
                                            }
                                        } catch (NoSuchMethodError exception) {
                                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cThis command is not supported in this version."));
                                        }
                                    } else {
                                        if (type.equalsIgnoreCase("player")) {
                                            for (String message : plugin.getFileUtils().language.getStringList("actionbar.number")) {
                                                plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', message.replace("{player}", sender.getName())));
                                            }
                                        } else {
                                            for (String message : plugin.getFileUtils().language.getStringList("console.actionbar.number")) {
                                                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                                            }
                                        }
                                    }
                                } else {
                                    if (type.equalsIgnoreCase("player")) {
                                        for (String message : plugin.getFileUtils().language.getStringList("actionbar.online")) {
                                            plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', message.replace("{player}", sender.getName())));
                                        }
                                    } else {
                                        for (String message : plugin.getFileUtils().language.getStringList("console.actionbar.online")) {
                                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                                        }
                                    }
                                }
                            } else {
                                if (type.equalsIgnoreCase("player")) {
                                    for (String message : plugin.getFileUtils().language.getStringList("actionbar.usage")) {
                                        plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', message.replace("{player}", sender.getName())));
                                    }
                                } else {
                                    for (String message : plugin.getFileUtils().language.getStringList("console.actionbar.usage")) {
                                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                                    }
                                }
                            }
                        } else {
                            if (type.equalsIgnoreCase("player")) {
                                for (String message : plugin.getFileUtils().language.getStringList("actionbar.permission")) {
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
                                        text = plugin.getPlaceholderManager().replacePlaceholders((Player) sender, text);
                                    }
                                    broadcast(ChatColor.translateAlternateColorCodes('&', text), args);
                                } else {
                                    for (String message : text.split(Pattern.quote("\\n"))) {
                                        if (type.equalsIgnoreCase("player")) {
                                            message = plugin.getPlaceholderManager().replacePlaceholders((Player) sender, message);
                                        }
                                        broadcast(ChatColor.translateAlternateColorCodes('&', message), args);
                                    }
                                }
                            } else {
                                if (type.equalsIgnoreCase("player")) {
                                    for (String message : plugin.getFileUtils().language.getStringList("broadcast.usage")) {
                                        plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', message.replace("{player}", sender.getName())));
                                    }
                                } else {
                                    for (String message : plugin.getFileUtils().language.getStringList("console.broadcast.usage")) {
                                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                                    }
                                }
                            }
                        } else {
                            if (type.equalsIgnoreCase("player")) {
                                for (String message : plugin.getFileUtils().language.getStringList("broadcast.permission")) {
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
                                            if (plugin.getCalculateManager().isInt(args[3])) {
                                                PlayerConnect playerConnect = plugin.getPlayerConnect(target.getUniqueId().toString());
                                                if (args[1].equalsIgnoreCase("set")) {
                                                    long set = Long.parseLong(args[3]);
                                                    if (set >= 0) {
                                                        playerConnect.setCoins(set);
                                                        playerConnect.save();
                                                        if (type.equalsIgnoreCase("player")) {
                                                            for (String message : plugin.getFileUtils().language.getStringList("coins.set")) {
                                                                plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', message.replace("{player}", sender.getName()).replace("{target}", target.getName()).replace("{coins}", args[3])));
                                                            }
                                                        } else {
                                                            for (String message : plugin.getFileUtils().language.getStringList("console.coins.set")) {
                                                                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message.replace("{target}", target.getName()).replace("{coins}", args[3])));
                                                            }
                                                        }
                                                    } else {
                                                        if (type.equalsIgnoreCase("player")) {
                                                            for (String message : plugin.getFileUtils().language.getStringList("coins.0")) {
                                                                plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', message.replace("{player}", sender.getName())));
                                                            }
                                                        } else {
                                                            for (String message : plugin.getFileUtils().language.getStringList("console.coins.0")) {
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
                                                            for (String message : plugin.getFileUtils().language.getStringList("coins.add")) {
                                                                plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', message.replace("{player}", sender.getName()).replace("{target}", target.getName()).replace("{coins}", args[3])));
                                                            }
                                                        } else {
                                                            for (String message : plugin.getFileUtils().language.getStringList("console.coins.add")) {
                                                                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message.replace("{target}", target.getName()).replace("{coins}", args[3])));
                                                            }
                                                        }
                                                    } else {
                                                        if (type.equalsIgnoreCase("player")) {
                                                            for (String message : plugin.getFileUtils().language.getStringList("coins.0")) {
                                                                plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', message.replace("{player}", sender.getName())));
                                                            }
                                                        } else {
                                                            for (String message : plugin.getFileUtils().language.getStringList("console.coins.0")) {
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
                                                            for (String message : plugin.getFileUtils().language.getStringList("coins.remove")) {
                                                                plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', message.replace("{player}", sender.getName()).replace("{target}", target.getName()).replace("{coins}", args[3])));
                                                            }
                                                        } else {
                                                            for (String message : plugin.getFileUtils().language.getStringList("console.coins.remove")) {
                                                                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message.replace("{target}", target.getName()).replace("{coins}", args[3])));
                                                            }
                                                        }
                                                    } else {
                                                        if (type.equalsIgnoreCase("player")) {
                                                            for (String message : plugin.getFileUtils().language.getStringList("coins.0")) {
                                                                plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', message.replace("{player}", sender.getName())));
                                                            }
                                                        } else {
                                                            for (String message : plugin.getFileUtils().language.getStringList("console.coins.0")) {
                                                                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                                                            }
                                                        }
                                                    }
                                                }
                                            } else {
                                                if (type.equalsIgnoreCase("player")) {
                                                    for (String message : plugin.getFileUtils().language.getStringList("coins.number")) {
                                                        plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', message.replace("{player}", sender.getName())));
                                                    }
                                                } else {
                                                    for (String message : plugin.getFileUtils().language.getStringList("console.coins.number")) {
                                                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                                                    }
                                                }
                                            }
                                        } else {
                                            if (type.equalsIgnoreCase("player")) {
                                                for (String message : plugin.getFileUtils().language.getStringList("coins.online")) {
                                                    plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', message.replace("{player}", sender.getName())));
                                                }
                                            } else {
                                                for (String message : plugin.getFileUtils().language.getStringList("console.coins.online")) {
                                                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                                                }
                                            }
                                        }
                                    } else {
                                        if (type.equalsIgnoreCase("player")) {
                                            for (String message : plugin.getFileUtils().language.getStringList("coins.usage")) {
                                                plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', message.replace("{player}", sender.getName())));
                                            }
                                        } else {
                                            for (String message : plugin.getFileUtils().language.getStringList("console.coins.usage")) {
                                                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                                            }
                                        }
                                    }
                                } else {
                                    if (type.equalsIgnoreCase("player")) {
                                        for (String message : plugin.getFileUtils().language.getStringList("coins.usage")) {
                                            plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', message.replace("{player}", sender.getName())));
                                        }
                                    } else {
                                        for (String message : plugin.getFileUtils().language.getStringList("console.coins.usage")) {
                                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                                        }
                                    }
                                }
                            } else {
                                if (type.equalsIgnoreCase("player")) {
                                    for (String message : plugin.getFileUtils().language.getStringList("coins.usage")) {
                                        plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', message.replace("{player}", sender.getName())));
                                    }
                                } else {
                                    for (String message : plugin.getFileUtils().language.getStringList("console.coins.usage")) {
                                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                                    }
                                }
                            }
                        } else {
                            if (type.equalsIgnoreCase("player")) {
                                for (String message : plugin.getFileUtils().language.getStringList("coins.permission")) {
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
                                            if (plugin.getCalculateManager().isInt(args[4]) && Integer.parseInt(args[4]) > 0 && Integer.parseInt(args[4]) < 65) {
                                                if (plugin.drones.containsKey(args[2])) {
                                                    FileConfiguration file = plugin.droneFiles.get(plugin.drones.get(args[2]));
                                                    final String material = file.getString("gui.AMMO.MATERIAL");
                                                    try {
                                                        ItemStack itemStack = plugin.getItemStackManager().getItemStack(material, Integer.parseInt(args[4]));
                                                        ItemMeta itemMeta = itemStack.getItemMeta();
                                                        if (itemMeta != null) {
                                                            itemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(file.getString("gui.AMMO.NAME"))));
                                                            ArrayList<String> lores = new ArrayList<>();
                                                            for (String lore : file.getStringList("gui.AMMO.LORES")) {
                                                                lores.add(ChatColor.translateAlternateColorCodes('&', lore));
                                                            }
                                                            itemMeta.setLore(lores);
                                                            itemStack.setItemMeta(itemMeta);
                                                            plugin.getItemStackManager().glow(itemStack, file, "gui.AMMO.OPTIONS");
                                                            if (target.getInventory().firstEmpty() == -1) {
                                                                target.getWorld().dropItem(target.getLocation().add(0, 1, 0), itemStack);
                                                            } else {
                                                                target.getInventory().addItem(itemStack);
                                                            }
                                                            if (type.equalsIgnoreCase("player")) {
                                                                for (String message : plugin.getFileUtils().language.getStringList("give.ammo.message")) {
                                                                    plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', message.replace("{player}", sender.getName()).replace("{amount}", args[4]).replace("{target}", target.getName()).replace("{drone}", args[2])));
                                                                }
                                                            } else {
                                                                for (String message : plugin.getFileUtils().language.getStringList("console.give.ammo.message")) {
                                                                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message.replace("{amount}", args[4]).replace("{target}", target.getName()).replace("{drone}", args[2])));
                                                                }
                                                            }
                                                        } else {
                                                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cAn error has occurred."));
                                                        }
                                                    } catch (NullPointerException e) {
                                                        plugin.getTextUtils().gui(sender, "ammo", material);
                                                    }
                                                } else {
                                                    if (type.equalsIgnoreCase("player")) {
                                                        for (String message : plugin.getFileUtils().language.getStringList("give.ammo.usage")) {
                                                            plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', message.replace("{player}", sender.getName())));
                                                        }
                                                    } else {
                                                        for (String message : plugin.getFileUtils().language.getStringList("console.give.ammo.usage")) {
                                                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                                                        }
                                                    }
                                                }
                                            } else {
                                                if (type.equalsIgnoreCase("player")) {
                                                    for (String message : plugin.getFileUtils().language.getStringList("give.ammo.number")) {
                                                        plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', message.replace("{player}", sender.getName())));
                                                    }
                                                } else {
                                                    for (String message : plugin.getFileUtils().language.getStringList("console.give.ammo.number")) {
                                                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                                                    }
                                                }
                                            }
                                        } else {
                                            if (type.equalsIgnoreCase("player")) {
                                                for (String message : plugin.getFileUtils().language.getStringList("give.online")) {
                                                    plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', message.replace("{player}", sender.getName())));
                                                }
                                            } else {
                                                for (String message : plugin.getFileUtils().language.getStringList("console.give.online")) {
                                                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                                                }
                                            }
                                        }
                                    } else {
                                        if (type.equalsIgnoreCase("player")) {
                                            for (String message : plugin.getFileUtils().language.getStringList("give.ammo.usage")) {
                                                plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', message.replace("{player}", sender.getName())));
                                            }
                                        } else {
                                            for (String message : plugin.getFileUtils().language.getStringList("console.give.ammo.usage")) {
                                                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                                            }
                                        }
                                    }
                                } else if (args[1].equalsIgnoreCase("controller")) {
                                    if (args.length == 4) {
                                        final Player target = plugin.getServer().getPlayer(args[2]);
                                        if (target != null) {
                                            if (plugin.getCalculateManager().isInt(args[3]) && Integer.parseInt(args[3]) != 0) {
                                                final ItemStack itemStack = plugin.getItemStackManager().getItemStack(plugin.getFileUtils().config.getString("controller.MATERIAL"), 1);
                                                final ItemMeta itemMeta = itemStack.getItemMeta();
                                                itemMeta.getPersistentDataContainer().set(new NamespacedKey(plugin, "drone_controller"), PersistentDataType.INTEGER, Integer.parseInt(args[3]));
                                                itemMeta.setDisplayName(plugin.getPlaceholderManager().replacePlaceholders(target, ChatColor.translateAlternateColorCodes('&', plugin.getFileUtils().config.getString("controller.NAME").replace("{range}", args[3]))));
                                                final ArrayList<String> list = new ArrayList<>();
                                                for (String lore : plugin.getFileUtils().config.getStringList("controller.LORE")) {
                                                    list.add(plugin.getPlaceholderManager().replacePlaceholders(target, ChatColor.translateAlternateColorCodes('&', lore.replace("{range}", args[3]))));
                                                }
                                                itemMeta.setLore(list);
                                                itemStack.setItemMeta(itemMeta);
                                                target.getInventory().addItem(itemStack);
                                                if (type.equalsIgnoreCase("player")) {
                                                    for (String message : plugin.getFileUtils().language.getStringList("give.controller.message")) {
                                                        plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', message.replace("{player}", sender.getName()).replace("{target}", target.getName())));
                                                    }
                                                } else {
                                                    for (String message : plugin.getFileUtils().language.getStringList("console.give.controller.message")) {
                                                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message.replace("{target}", target.getName())));
                                                    }
                                                }
                                            } else {
                                                if (type.equalsIgnoreCase("player")) {
                                                    for (String message : plugin.getFileUtils().language.getStringList("give.controller.number")) {
                                                        plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', message.replace("{player}", sender.getName())));
                                                    }
                                                } else {
                                                    for (String message : plugin.getFileUtils().language.getStringList("console.give.controller.number")) {
                                                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                                                    }
                                                }
                                            }
                                        } else {
                                            if (type.equalsIgnoreCase("player")) {
                                                for (String message : plugin.getFileUtils().language.getStringList("give.online")) {
                                                    plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', message.replace("{player}", sender.getName())));
                                                }
                                            } else {
                                                for (String message : plugin.getFileUtils().language.getStringList("console.give.online")) {
                                                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                                                }
                                            }
                                        }
                                    } else {
                                        if (type.equalsIgnoreCase("player")) {
                                            for (String message : plugin.getFileUtils().language.getStringList("give.controller.usage")) {
                                                plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', message.replace("{player}", sender.getName())));
                                            }
                                        } else {
                                            for (String message : plugin.getFileUtils().language.getStringList("console.give.controller.usage")) {
                                                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                                            }
                                        }
                                    }
                                } else {
                                    if (type.equalsIgnoreCase("player")) {
                                        for (String message : plugin.getFileUtils().language.getStringList("give.usage")) {
                                            plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', message.replace("{player}", sender.getName())));
                                        }
                                    } else {
                                        for (String message : plugin.getFileUtils().language.getStringList("console.give.usage")) {
                                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                                        }
                                    }
                                }
                            } else {
                                if (type.equalsIgnoreCase("player")) {
                                    for (String message : plugin.getFileUtils().language.getStringList("give.usage")) {
                                        plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', message.replace("{player}", sender.getName())));
                                    }
                                } else {
                                    for (String message : plugin.getFileUtils().language.getStringList("console.give.usage")) {
                                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                                    }
                                }
                            }
                        } else {
                            if (type.equalsIgnoreCase("player")) {
                                for (String message : plugin.getFileUtils().language.getStringList("give.permission")) {
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
                                            if (plugin.getCalculateManager().isInt(args[3]) && plugin.getCalculateManager().isInt(args[4]) && plugin.getCalculateManager().isInt(args[5]) && plugin.getCalculateManager().isFloat(args[6]) && plugin.getCalculateManager().isFloat(args[7])) {
                                                if (args.length == 8) {
                                                    world.playSound(new Location(world, Integer.parseInt(args[3]), Integer.parseInt(args[4]), Integer.parseInt(args[5])), sound, Float.parseFloat(args[6]), Float.parseFloat(args[7]));
                                                } else {
                                                    Player target = plugin.getServer().getPlayer(args[8]);
                                                    if (target != null) {
                                                        target.playSound(new Location(world, Integer.parseInt(args[3]), Integer.parseInt(args[4]), Integer.parseInt(args[5])), sound, Float.parseFloat(args[6]), Float.parseFloat(args[7]));
                                                    } else {
                                                        if (type.equalsIgnoreCase("player")) {
                                                            for (String message : plugin.getFileUtils().language.getStringList("sound.online")) {
                                                                plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', message.replace("{player}", sender.getName())));
                                                            }
                                                        } else {
                                                            for (String message : plugin.getFileUtils().language.getStringList("console.sound.online")) {
                                                                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                                                            }
                                                        }
                                                    }
                                                }
                                            } else {
                                                if (type.equalsIgnoreCase("player")) {
                                                    for (String message : plugin.getFileUtils().language.getStringList("sound.number")) {
                                                        plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', message.replace("{player}", sender.getName())));
                                                    }
                                                } else {
                                                    for (String message : plugin.getFileUtils().language.getStringList("console.sound.number")) {
                                                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                                                    }
                                                }
                                            }
                                        } else {
                                            if (type.equalsIgnoreCase("player")) {
                                                for (String message : plugin.getFileUtils().language.getStringList("sound.world")) {
                                                    plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', message.replace("{player}", sender.getName())));
                                                }
                                            } else {
                                                for (String message : plugin.getFileUtils().language.getStringList("console.sound.world")) {
                                                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                                                }
                                            }
                                        }
                                    } else {
                                        if (type.equalsIgnoreCase("player")) {
                                            for (String message : plugin.getFileUtils().language.getStringList("sound.usage")) {
                                                plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', message.replace("{player}", sender.getName())));
                                            }
                                        } else {
                                            for (String message : plugin.getFileUtils().language.getStringList("console.sound.usage")) {
                                                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                                            }
                                        }
                                    }
                                } catch (IllegalArgumentException e) {
                                    if (type.equalsIgnoreCase("player")) {
                                        for (String message : plugin.getFileUtils().language.getStringList("sound.found")) {
                                            plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', message.replace("{player}", sender.getName())));
                                        }
                                    } else {
                                        for (String message : plugin.getFileUtils().language.getStringList("console.sound.found")) {
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
                                    for (String message : plugin.getFileUtils().language.getStringList("sound.names")) {
                                        plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', message.replace("{player}", sender.getName()).replace("{sound_names}", stringList)));
                                    }
                                } else {
                                    for (String message : plugin.getFileUtils().language.getStringList("console.sound.names")) {
                                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message.replace("{sound_names}", stringList)));
                                    }
                                }
                            }
                        } else {
                            if (type.equalsIgnoreCase("player")) {
                                for (String message : plugin.getFileUtils().language.getStringList("sound.permission")) {
                                    plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', message.replace("{player}", sender.getName())));
                                }
                            }
                        }
                    } else if (args[0].equalsIgnoreCase("update")) {
                        unknown = false;
                        if (sender.hasPermission("battledrones.admin.update")) {
                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7[&dBattleDrones&7] &aNo new drone available."));
                        } else {
                            if (type.equalsIgnoreCase("player")) {
                                for (String message : plugin.getFileUtils().language.getStringList("update.permission")) {
                                    plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', message.replace("{player}", sender.getName())));
                                }
                            }
                        }
                    }
                    if (unknown) {
                        if (type.equalsIgnoreCase("player")) {
                            for (String message : plugin.getFileUtils().language.getStringList("command.unknown")) {
                                plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', message.replace("{player}", sender.getName()).replace("{command}", args[0])));
                            }
                        } else {
                            for (String message : plugin.getFileUtils().language.getStringList("console.command.unknown")) {
                                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message.replace("{command}", args[0])));
                            }
                        }
                    }
                }
            } else {
                if (type.equalsIgnoreCase("player")) {
                    for (String message : plugin.getFileUtils().language.getStringList("command.permission")) {
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
        final DroneHolder droneHolder = plugin.getDroneHolder(uuid, playerConnect.getActive());
        final DroneParkEvent droneParkEvent = new DroneParkEvent(player, playerConnect, droneHolder);
        if (plugin.park.contains(uuid)) {
            droneParkEvent.setType(Type.UNPARK);
            plugin.getServer().getPluginManager().callEvent(droneParkEvent);
            if (droneParkEvent.isCancelled()) {
                return;
            }
            plugin.park.remove(uuid);
            for (String message : plugin.getFileUtils().language.getStringList("park.follow")) {
                plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', message.replace("{player}", player.getName())));
            }
            return;
        }

        droneParkEvent.setType(Type.PARK);
        plugin.getServer().getPluginManager().callEvent(droneParkEvent);
        if (droneParkEvent.isCancelled()) {
            return;
        }
        final FileConfiguration file = plugin.droneFiles.get(playerConnect.getActive());
        final String path = playerConnect.getGroup() + "." + droneHolder.getLevel() + ".park";
        if (file.contains(path)) {
            final int cost = file.getInt(path);
            if (plugin.getSupport().vault.withdraw(player, cost)) {
                plugin.park.add(uuid);
                for (String message : plugin.getFileUtils().language.getStringList("park.parked")) {
                    plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', message.replace("{player}", player.getName()).replace("{cost}", String.valueOf(cost))));
                }
            } else {
                for (String message : plugin.getFileUtils().language.getStringList("park.enough")) {
                    plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', message.replace("{player}", player.getName()).replace("{cost}", String.valueOf(cost))));
                }
            }
        } else {
            for (String message : plugin.getFileUtils().language.getStringList("park.drone")) {
                plugin.getServer().dispatchCommand(plugin.consoleSender, ChatColor.translateAlternateColorCodes('&', message.replace("{player}", player.getName())));
            }
        }
    }
}