package me.MathiasMC.BattleDrones.commands;

import me.MathiasMC.BattleDrones.BattleDrones;
import me.MathiasMC.BattleDrones.data.DroneHolder;
import me.MathiasMC.BattleDrones.data.PlayerConnect;
import me.MathiasMC.BattleDrones.gui.player.PlayerGUI;
import me.MathiasMC.BattleDrones.gui.shop.ShopGUI;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.regex.Pattern;

public class BattleDrones_Command implements CommandExecutor {

    private final BattleDrones plugin;

    public BattleDrones_Command(final BattleDrones plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        if (cmd.getName().equalsIgnoreCase("battledrones")) {
            if (sender.hasPermission("battledrones.command")) {
                boolean unknown = true;
                String type;
                if (sender instanceof Player) {
                    type = "player";
                } else {
                    type = "console";
                }
                if (args.length == 0) {
                    for (String message : plugin.language.get.getStringList("battledrones.command.message")) {
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message.replace("{version}", plugin.getDescription().getVersion())));
                    }
                } else {
                    if (args[0].equalsIgnoreCase("help")) {
                        unknown = false;
                        if (sender.hasPermission("battledrones.command.help")) {
                            for (String message : plugin.language.get.getStringList("battledrones.help.message")) {
                                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                            }
                        } else {
                            for (String message : plugin.language.get.getStringList("battledrones.help.permission")) {
                                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                            }
                        }
                    } else if (args[0].equalsIgnoreCase("reload")) {
                        unknown = false;
                        if (sender.hasPermission("battledrones.command.reload")) {
                            plugin.config.load();
                            plugin.language.load();
                            plugin.guiFolder.load();
                            plugin.dronesFolder.load();
                            for (String message : plugin.language.get.getStringList("battledrones.reload.message")) {
                                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                            }
                        } else {
                            for (String message : plugin.language.get.getStringList("battledrones.reload.permission")) {
                                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                            }
                        }
                    } else if (args[0].equalsIgnoreCase("message")) {
                        unknown = false;
                        if (sender.hasPermission("battledrones.command.message")) {
                            if (args.length <= 2) {
                                for (String message : plugin.language.get.getStringList("battledrones.message.usage")) {
                                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                                }
                            } else {
                                Player target = plugin.getServer().getPlayer(args[1]);
                                if (target != null) {
                                    StringBuilder sb = new StringBuilder();
                                    for (int i = 2; i < args.length; i++) {
                                        sb.append(args[i]).append(" ");
                                    }
                                    String text = sb.toString().trim();
                                    if (!text.contains("\\n")) {
                                        target.sendMessage(ChatColor.translateAlternateColorCodes('&', text));
                                    } else {
                                        for (String message : text.split(Pattern.quote("\\n"))) {
                                            target.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                                        }
                                    }
                                } else {
                                    for (String message : plugin.language.get.getStringList("battledrones.message.online")) {
                                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                                    }
                                }
                            }
                        } else {
                            for (String message : plugin.language.get.getStringList("battledrones.message.permission")) {
                                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                            }
                        }
                    } else if (args[0].equalsIgnoreCase("save")) {
                        unknown = false;
                        if (sender.hasPermission("battledrones.command.save")) {
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
                            for (String message : plugin.language.get.getStringList("battledrones.save.message")) {
                                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                            }
                        } else {
                            for (String message : plugin.language.get.getStringList("battledrones.save.permission")) {
                                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                            }
                        }
                    } else if (args[0].equalsIgnoreCase("coins")) {
                        unknown = false;
                        if (sender.hasPermission("battledrones.command.coins")) {
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
                                                        for (String message : plugin.language.get.getStringList("battledrones.coins.set")) {
                                                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message.replace("{player}", target.getName()).replace("{coins}", args[3])));
                                                        }
                                                    } else {
                                                        for (String message : plugin.language.get.getStringList("battledrones.coins.0")) {
                                                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                                                        }
                                                    }
                                                } else if (args[1].equalsIgnoreCase("add")) {
                                                    long set = playerConnect.getCoins() + Long.parseLong(args[3]);
                                                    if (set >= 0) {
                                                        playerConnect.setCoins(set);
                                                        playerConnect.save();
                                                        for (String message : plugin.language.get.getStringList("battledrones.coins.add")) {
                                                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message.replace("{player}", target.getName()).replace("{coins}", args[3])));
                                                        }
                                                    } else {
                                                        for (String message : plugin.language.get.getStringList("battledrones.coins.0")) {
                                                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                                                        }
                                                    }
                                                } else if (args[1].equalsIgnoreCase("remove")) {
                                                    long set = playerConnect.getCoins() - Long.parseLong(args[3]);
                                                    if (set >= 0) {
                                                        playerConnect.setCoins(set);
                                                        playerConnect.save();
                                                        for (String message : plugin.language.get.getStringList("battledrones.coins.remove")) {
                                                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message.replace("{player}", target.getName()).replace("{coins}", args[3])));
                                                        }
                                                    } else {
                                                        for (String message : plugin.language.get.getStringList("battledrones.coins.0")) {
                                                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                                                        }
                                                    }
                                                }
                                            } else {
                                                for (String message : plugin.language.get.getStringList("battledrones.coins.number")) {
                                                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                                                }
                                            }
                                        } else {
                                            for (String message : plugin.language.get.getStringList("battledrones.coins.online")) {
                                                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                                            }
                                        }
                                    } else {
                                        for (String message : plugin.language.get.getStringList("battledrones.coins.usage")) {
                                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                                        }
                                    }
                                } else {
                                    for (String message : plugin.language.get.getStringList("battledrones.coins.usage")) {
                                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                                    }
                                }
                            } else {
                                for (String message : plugin.language.get.getStringList("battledrones.coins.usage")) {
                                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                                }
                            }
                        } else {
                            for (String message : plugin.language.get.getStringList("battledrones.coins.permission")) {
                                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                            }
                        }
                    } else if (args[0].equalsIgnoreCase("shop")) {
                        unknown = false;
                        if (sender.hasPermission("battledrones.command.shop")) {
                            if (type.equalsIgnoreCase("player")) {
                                Player player = (Player) sender;
                                new ShopGUI(plugin.getPlayerMenu(player)).open();
                            } else {
                                if (args.length == 2) {
                                    Player target = plugin.getServer().getPlayer(args[1]);
                                    if (target != null) {
                                        new ShopGUI(plugin.getPlayerMenu(target)).open();
                                    } else {
                                        for (String message : plugin.language.get.getStringList("battledrones.shop.online")) {
                                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                                        }
                                    }
                                } else {
                                    for (String message : plugin.language.get.getStringList("battledrones.shop.usage")) {
                                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                                    }
                                }
                            }
                        } else {
                            for (String message : plugin.language.get.getStringList("battledrones.shop.permission")) {
                                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                            }
                        }
                    } else if (args[0].equalsIgnoreCase("menu")) {
                        unknown = false;
                        if (sender.hasPermission("battledrones.command.menu")) {
                            if (type.equalsIgnoreCase("player")) {
                                Player player = (Player) sender;
                                new PlayerGUI(plugin.getPlayerMenu(player)).open();
                            } else {
                                if (args.length == 2) {
                                    Player target = plugin.getServer().getPlayer(args[1]);
                                    if (target != null) {
                                        new PlayerGUI(plugin.getPlayerMenu(target)).open();
                                    } else {
                                        for (String message : plugin.language.get.getStringList("battledrones.menu.online")) {
                                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                                        }
                                    }
                                } else {
                                    for (String message : plugin.language.get.getStringList("battledrones.menu.usage")) {
                                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                                    }
                                }
                            }
                        } else {
                            for (String message : plugin.language.get.getStringList("battledrones.menu.permission")) {
                                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                            }
                        }
                    } else if (args[0].equalsIgnoreCase("unlock")) {
                        unknown = false;
                        if (sender.hasPermission("battledrones.command.unlock")) {
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
                                        for (String message : plugin.language.get.getStringList("battledrones.unlock.message")) {
                                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message.replace("{drone}", args[1]).replace("{player}", target.getName())));
                                        }
                                    } else {
                                        for (String message : plugin.language.get.getStringList("battledrones.unlock.online")) {
                                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                                        }
                                    }
                                } else {
                                    for (String message : plugin.language.get.getStringList("battledrones.unlock.type")) {
                                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                                    }
                                }
                            } else {
                                for (String message : plugin.language.get.getStringList("battledrones.unlock.usage")) {
                                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                                }
                            }
                        } else {
                            for (String message : plugin.language.get.getStringList("battledrones.unlock.permission")) {
                                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                            }
                        }
                    } else if (args[0].equalsIgnoreCase("lock")) {
                        unknown = false;
                        if (sender.hasPermission("battledrones.command.lock")) {
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
                                        }
                                        droneHolder.save();
                                        for (String message : plugin.language.get.getStringList("battledrones.lock.message")) {
                                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message.replace("{drone}", args[1]).replace("{player}", target.getName())));
                                        }
                                    } else {
                                        for (String message : plugin.language.get.getStringList("battledrones.lock.online")) {
                                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                                        }
                                    }
                                } else {
                                    for (String message : plugin.language.get.getStringList("battledrones.lock.type")) {
                                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                                    }
                                }
                            } else {
                                for (String message : plugin.language.get.getStringList("battledrones.lock.usage")) {
                                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                                }
                            }
                        } else {
                            for (String message : plugin.language.get.getStringList("battledrones.lock.permission")) {
                                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                            }
                        }
                    } else if (args[0].equalsIgnoreCase("sound")) {
                        unknown = false;
                        if (sender.hasPermission("battledrones.command.sound")) {
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
                                                        for (String message : plugin.language.get.getStringList("battledrones.sound.online")) {
                                                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                                                        }
                                                    }
                                                }
                                            } else {
                                                for (String message : plugin.language.get.getStringList("battledrones.sound.number")) {
                                                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                                                }
                                            }
                                        } else {
                                            for (String message : plugin.language.get.getStringList("battledrones.sound.world")) {
                                                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                                            }
                                        }
                                    } else {
                                        for (String message : plugin.language.get.getStringList("battledrones.sound.usage")) {
                                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                                        }
                                    }
                                } catch (IllegalArgumentException e) {
                                    for (String message : plugin.language.get.getStringList("battledrones.sound.found")) {
                                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                                    }
                                }
                            } else {
                                ArrayList<String> list = new ArrayList<>();
                                for (Sound sound : Sound.values()) {
                                    list.add(sound.name().toLowerCase());
                                }
                                for (String message : plugin.language.get.getStringList("battledrones.sound.names")) {
                                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message.replace("{sound_names}", list.toString().replace("[", "").replace("]", ""))));
                                }
                            }
                        } else {
                            for (String message : plugin.language.get.getStringList("battledrones.sound.permission")) {
                                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                            }
                        }
                    } else if (args[0].equalsIgnoreCase("group")) {
                        unknown = false;
                        if (sender.hasPermission("battledrones.command.group")) {
                            if (args.length > 1) {
                                if (args[1].equalsIgnoreCase("set")) {
                                    if (args.length == 4) {
                                        Player target = plugin.getServer().getPlayer(args[2]);
                                        if (target != null) {
                                            if (plugin.isString(args[3])) {
                                                PlayerConnect droneHolder = plugin.get(target.getUniqueId().toString());
                                                droneHolder.setGroup(args[3]);
                                                for (String message : plugin.language.get.getStringList("battledrones.group.set.message")) {
                                                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message.replace("{player}", target.getName()).replace("{group}", args[3])));
                                                }
                                            } else {
                                                for (String message : plugin.language.get.getStringList("battledrones.group.valid")) {
                                                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                                                }
                                            }
                                        } else {
                                            for (String message : plugin.language.get.getStringList("battledrones.group.online")) {
                                                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                                            }
                                        }
                                    } else {
                                        for (String message : plugin.language.get.getStringList("battledrones.group.set.usage")) {
                                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                                        }
                                    }
                                } else if (args[1].equalsIgnoreCase("reset")) {
                                    if (args.length == 3) {
                                        Player target = plugin.getServer().getPlayer(args[2]);
                                        if (target != null) {
                                            PlayerConnect droneHolder = plugin.get(target.getUniqueId().toString());
                                            droneHolder.setGroup("default");
                                            for (String message : plugin.language.get.getStringList("battledrones.group.reset.message")) {
                                                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message.replace("{player}", target.getName())));
                                            }
                                        } else {
                                            for (String message : plugin.language.get.getStringList("battledrones.group.online")) {
                                                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                                            }
                                        }
                                    } else {
                                        for (String message : plugin.language.get.getStringList("battledrones.group.reset.usage")) {
                                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                                        }
                                    }
                                } else {
                                    for (String message : plugin.language.get.getStringList("battledrones.group.usage")) {
                                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                                    }
                                }
                            } else {
                                for (String message : plugin.language.get.getStringList("battledrones.group.usage")) {
                                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                                }
                            }
                        } else {
                            for (String message : plugin.language.get.getStringList("battledrones.group.permission")) {
                                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                            }
                        }
                    } else if (args[0].equalsIgnoreCase("give")) {
                        unknown = false;
                        if (sender.hasPermission("battledrones.command.give")) {
                            if (args.length > 1) {
                                if (args[1].equalsIgnoreCase("ammo")) {
                                    if (args.length == 5) {
                                        Player target = plugin.getServer().getPlayer(args[3]);
                                        if (target != null) {
                                            if (plugin.isInt(args[4]) && Integer.parseInt(args[4]) > 0 && Integer.parseInt(args[4]) < 65) {
                                                if (plugin.drones.contains(args[2])) {
                                                    FileConfiguration file = plugin.droneFiles.get(args[2]);
                                                    ItemStack itemStack = plugin.getItemStack(file.getString("gui.AMMO.MATERIAL"), Integer.parseInt(args[4]));
                                                    ItemMeta itemMeta = itemStack.getItemMeta();
                                                    itemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', file.getString("gui.AMMO.NAME")));
                                                    ArrayList<String> lores = new ArrayList<>();
                                                    for (String lore : file.getStringList("gui.AMMO.LORES")) {
                                                        lores.add(ChatColor.translateAlternateColorCodes('&', lore));
                                                    }
                                                    itemMeta.setLore(lores);
                                                    itemStack.setItemMeta(itemMeta);
                                                    if (target.getInventory().firstEmpty() == -1) {
                                                        target.getWorld().dropItem(target.getLocation().add(0, 1, 0), itemStack);
                                                    } else {
                                                        target.getInventory().addItem(itemStack);
                                                    }
                                                    for (String message : plugin.language.get.getStringList("battledrones.give.ammo.message")) {
                                                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message.replace("{amount}", args[4]).replace("{player}", target.getName()).replace("{drone}", args[2])));
                                                    }
                                                } else {
                                                    for (String message : plugin.language.get.getStringList("battledrones.give.ammo.usage")) {
                                                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                                                    }
                                                }
                                            } else {
                                                for (String message : plugin.language.get.getStringList("battledrones.give.number")) {
                                                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                                                }
                                            }
                                        } else {
                                            for (String message : plugin.language.get.getStringList("battledrones.give.online")) {
                                                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                                            }
                                        }
                                    } else {
                                        for (String message : plugin.language.get.getStringList("battledrones.give.ammo.usage")) {
                                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                                        }
                                    }
                                } else {
                                    for (String message : plugin.language.get.getStringList("battledrones.give.usage")) {
                                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                                    }
                                }
                            } else {
                                for (String message : plugin.language.get.getStringList("battledrones.give.usage")) {
                                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                                }
                            }
                        } else {
                            for (String message : plugin.language.get.getStringList("battledrones.give.permission")) {
                                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                            }
                        }
                    }
                    if (unknown) {
                        for (String message : plugin.language.get.getStringList("battledrones.command.unknown")) {
                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message.replace("{command}", args[0])));
                        }
                    }
                }
            } else {
                for (String message : plugin.language.get.getStringList("battledrones.command.permission")) {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                }
            }
        }
        return true;
    }
}