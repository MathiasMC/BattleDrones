package me.MathiasMC.BattleDrones.commands;

import me.MathiasMC.BattleDrones.BattleDrones;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BattleDrones_TabComplete implements TabCompleter {

    private final BattleDrones plugin;

    public BattleDrones_TabComplete(final BattleDrones plugin) {
        this.plugin = plugin;
    }

    public List<String> onTabComplete(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        if (sender instanceof Player) {
            final Player player = (Player) sender;
            final List<String> commands = new ArrayList<>();
            final List<String> list = new ArrayList<>();
            if (player.hasPermission("battledrones.command.help")) {
                if (args.length == 1) {
                    commands.add("help");
                }
            }
            if (player.hasPermission("battledrones.command.reload")) {
                if (args.length == 1) {
                    commands.add("reload");
                }
            }
            if (player.hasPermission("battledrones.command.save")) {
                if (args.length == 1) {
                    commands.add("save");
                }
            }
            if (player.hasPermission("battledrones.command.shop")) {
                if (args.length == 1) {
                    commands.add("shop");
                }
            }
            if (player.hasPermission("battledrones.command.menu")) {
                if (args.length == 1) {
                    commands.add("menu");
                }
            }
            if (player.hasPermission("battledrones.command.message")) {
                if (args.length == 1) {
                    commands.add("message");
                } else if (args.length > 1 && args[0].equalsIgnoreCase("message")) {
                    if (args.length == 2) {
                        commands.add(player.getName());
                    } else if (args.length == 3) {
                        commands.add("text");
                    }
                }
            }
            if (player.hasPermission("battledrones.command.coins")) {
                if (args.length == 1) {
                    commands.add("coins");
                } else if (args.length > 1 && args[0].equalsIgnoreCase("coins")) {
                    if (args.length == 2) {
                        commands.add("set");
                        commands.add("add");
                        commands.add("remove");
                    } else if (args.length == 3) {
                        commands.add(player.getName());
                    } else if (args.length == 4) {
                        commands.add("amount");
                    }
                }
            }
            if (player.hasPermission("battledrones.command.unlock")) {
                if (args.length == 1) {
                    commands.add("unlock");
                } else if (args.length == 2 || args.length == 3) {
                    if (args[0].equalsIgnoreCase("unlock")) {
                        if (args.length == 2) {
                            commands.add("droneType");
                        } else {
                            commands.add(player.getName());
                        }
                    }
                }
            }
            if (player.hasPermission("battledrones.command.lock")) {
                if (args.length == 1) {
                    commands.add("lock");
                } else if (args.length == 2 || args.length == 3) {
                    if (args[0].equalsIgnoreCase("lock")) {
                        if (args.length == 2) {
                            commands.add("droneType");
                        } else {
                            commands.add(player.getName());
                        }
                    }
                }
            }
            if (player.hasPermission("battledrones.command.sound")) {
                if (args.length == 1) {
                    commands.add("sound");
                } else if (args.length > 1 && args[0].equalsIgnoreCase("sound")) {
                    if (args.length == 2) {
                        commands.add("soundName");
                    } else if (args.length == 3) {
                        commands.add("world");
                    } else if (args.length == 4) {
                        commands.add("x");
                    } else if (args.length == 5) {
                        commands.add("y");
                    } else if (args.length == 6) {
                        commands.add("z");
                    } else if (args.length == 7) {
                        commands.add("volume");
                    } else if (args.length == 8) {
                        commands.add("pitch");
                    } else if (args.length == 9) {
                        commands.add(player.getName());
                    }
                }
            }
            if (player.hasPermission("battledrones.command.group")) {
                if (args.length == 1) {
                    commands.add("group");
                } else if (args.length > 1 && args[0].equalsIgnoreCase("group")){
                    if (args.length == 2) {
                        commands.add("set");
                        commands.add("reset");
                    } else if (args.length == 3) {
                        commands.add(player.getName());
                    } else if (args.length == 4) {
                        if (args[1].equalsIgnoreCase("set")) {
                            commands.add("group");
                        }
                    }
                }
            }
            if (player.hasPermission("battledrones.command.give")) {
                if (args.length == 1) {
                    commands.add("give");
                } else if (args.length > 1 && args[0].equalsIgnoreCase("give")) {
                    if (args.length == 2) {
                        commands.add("ammo");
                    } else if (args.length == 3) {
                        commands.addAll(plugin.drones);
                    } else if (args.length == 4) {
                        commands.add(player.getName());
                    } else if (args.length == 5) {
                        commands.add("amount");
                    }
                }
            }
            if (player.hasPermission("battledrones.command.broadcast")) {
                if (args.length == 1) {
                    commands.add("broadcast");
                } else if (args.length > 1 && args[0].equalsIgnoreCase("broadcast")) {
                    if (args.length == 2) {
                        commands.add("null");
                    } else if (args.length == 3) {
                        commands.add("text");
                    }
                }
            }
            if (player.hasPermission("battledrones.command.activate")) {
                if (args.length == 1) {
                    commands.add("activate");
                } else if (args.length > 1 && args[0].equalsIgnoreCase("activate")) {
                    if (args.length == 2) {
                        commands.addAll(plugin.drones);
                    }
                }
            }
            if (player.hasPermission("battledrones.command.deactivate")) {
                if (args.length == 1) {
                    commands.add("deactivate");
                } else if (args.length > 1 && args[0].equalsIgnoreCase("deactivate")) {
                    if (player.hasPermission("battledrones.command.deactivate.other")) {
                        if (args.length == 2) {
                            commands.add("all");
                            commands.add(player.getName());
                            commands.addAll(plugin.drones);
                        } else if (args.length == 3) {
                            commands.add("seconds");
                        }
                    }
                }
            }
            StringUtil.copyPartialMatches(args[args.length - 1], commands, list);
            Collections.sort(list);
            return list;
        }
        return null;
    }
}