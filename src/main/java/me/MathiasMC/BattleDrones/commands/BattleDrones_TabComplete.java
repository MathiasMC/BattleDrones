package me.MathiasMC.BattleDrones.commands;

import me.MathiasMC.BattleDrones.BattleDrones;
import org.bukkit.Sound;
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
            if (player.hasPermission("battledrones.player.help") || player.hasPermission("battledrones.admin.help")) {
                if (args.length == 1) {
                    commands.add("help");
                }
            }
            if (player.hasPermission("battledrones.player.shop")) {
                if (args.length == 1) {
                    commands.add("shop");
                }
            }
            if (player.hasPermission("battledrones.player.menu")) {
                if (args.length == 1) {
                    commands.add("menu");
                }
            }
            if (player.hasPermission("battledrones.player.drone")) {
                if (args.length == 1) {
                    commands.add("drone");
                }
            }
            if (player.hasPermission("battledrones.player.activate")) {
                if (args.length == 1) {
                    commands.add("activate");
                } else if (args.length > 1 && args[0].equalsIgnoreCase("activate")) {
                    if (args.length == 2) {
                        commands.addAll(plugin.drones.keySet());
                    }
                }
            }
            if (player.hasPermission("battledrones.player.deactivate")) {
                if (args.length == 1) {
                    commands.add("deactivate");
                } else if (args.length > 1 && args[0].equalsIgnoreCase("deactivate")) {
                    if (player.hasPermission("battledrones.admin.deactivate")) {
                        if (args.length == 2) {
                            commands.add("all");
                            commands.addAll(getPlayers(args[1]));
                            commands.addAll(plugin.drones.keySet());
                        } else if (args.length == 3) {
                            commands.add("seconds");
                        }
                    }
                }
            }
            if (player.hasPermission("battledrones.player.park")) {
                if (args.length == 1) {
                    commands.add("park");
                }
            }
            if (player.hasPermission("battledrones.player.move")) {
                if (args.length == 1) {
                    commands.add("move");
                }
            }
            if (player.hasPermission("battledrones.admin.reload")) {
                if (args.length == 1) {
                    commands.add("reload");
                } else if (args.length > 1 && args[0].equalsIgnoreCase("reload")) {
                    if (args.length == 2) {
                        commands.add("all");
                        commands.add("config");
                        commands.add("language");
                        commands.add("particles");
                        commands.add("gui");
                        commands.add("drones");
                        commands.add("heads");
                    }
                }
            }
            if (player.hasPermission("battledrones.admin.save")) {
                if (args.length == 1) {
                    commands.add("save");
                }
            }
            if (player.hasPermission("battledrones.admin.unlock")) {
                if (args.length == 1) {
                    commands.add("unlock");
                } else if (args.length == 2 || args.length == 3) {
                    if (args[0].equalsIgnoreCase("unlock")) {
                        if (args.length == 2) {
                            commands.addAll(plugin.drones.keySet());
                        } else {
                            commands.addAll(getPlayers(args[2]));
                        }
                    }
                }
            }
            if (player.hasPermission("battledrones.admin.lock")) {
                if (args.length == 1) {
                    commands.add("lock");
                } else if (args.length == 2 || args.length == 3) {
                    if (args[0].equalsIgnoreCase("lock")) {
                        if (args.length == 2) {
                            commands.addAll(plugin.drones.keySet());
                        } else {
                            commands.addAll(getPlayers(args[2]));
                        }
                    }
                }
            }
            if (player.hasPermission("battledrones.admin.group")) {
                if (args.length == 1) {
                    commands.add("group");
                } else if (args.length > 1 && args[0].equalsIgnoreCase("group")){
                    if (args.length == 2) {
                        commands.add("set");
                        commands.add("reset");
                    } else if (args.length == 3) {
                        commands.addAll(getPlayers(args[2]));
                    } else if (args.length == 4) {
                        if (args[1].equalsIgnoreCase("set")) {
                            commands.add("group");
                        }
                    }
                }
            }
            if (player.hasPermission("battledrones.admin.message")) {
                if (args.length == 1) {
                    commands.add("message");
                } else if (args.length > 1 && args[0].equalsIgnoreCase("message")) {
                    if (args.length == 2) {
                        commands.addAll(getPlayers(args[1]));
                    } else if (args.length == 3) {
                        commands.add("text");
                    }
                }
            }
            if (player.hasPermission("battledrones.admin.actionbar")) {
                if (args.length == 1) {
                    commands.add("actionbar");
                } else if (args.length > 1 && args[0].equalsIgnoreCase("actionbar")) {
                    if (args.length == 2) {
                        commands.addAll(getPlayers(args[1]));
                    } else if (args.length == 3) {
                        commands.add("seconds");
                    } else if (args.length == 4) {
                        commands.add("text");
                    }
                }
            }
            if (player.hasPermission("battledrones.admin.broadcast")) {
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
            if (player.hasPermission("battledrones.admin.coins")) {
                if (args.length == 1) {
                    commands.add("coins");
                } else if (args.length > 1 && args[0].equalsIgnoreCase("coins")) {
                    if (args.length == 2) {
                        commands.add("set");
                        commands.add("add");
                        commands.add("remove");
                    } else if (args.length == 3) {
                        commands.addAll(getPlayers(args[2]));
                    } else if (args.length == 4) {
                        commands.add("amount");
                    }
                }
            }
            if (player.hasPermission("battledrones.admin.give")) {
                if (args.length == 1) {
                    commands.add("give");
                } else if (args.length > 1 && args[0].equalsIgnoreCase("give")) {
                    if (args.length == 2) {
                        commands.add("ammo");
                        commands.add("controller");
                    } else if (args.length == 3) {
                        if (args[1].equalsIgnoreCase("ammo")) {
                            commands.addAll(plugin.drones.keySet());
                        } else if (args[1].equalsIgnoreCase("controller")) {
                            commands.addAll(getPlayers(args[2]));
                        }
                    } else if (args.length == 4) {
                        if (args[1].equalsIgnoreCase("ammo")) {
                            commands.addAll(getPlayers(args[3]));
                        } else if (args[1].equalsIgnoreCase("controller")) {
                            commands.add("range");
                        }
                    } else if (args.length == 5) {
                        if (args[1].equalsIgnoreCase("ammo")) {
                            commands.add("64");
                        }
                    }
                }
            }
            if (player.hasPermission("battledrones.admin.sound")) {
                if (args.length == 1) {
                    commands.add("sound");
                } else if (args.length > 1 && args[0].equalsIgnoreCase("sound")) {
                    if (args.length == 2) {
                        for (Sound sound : Sound.values()) {
                            commands.add(sound.name().toLowerCase());
                        }
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
                        commands.addAll(getPlayers(args[8]));
                    }
                }
            }
            if (player.hasPermission("battledrones.admin.update")) {
                if (args.length == 1) {
                    commands.add("update");
                }
            }
            StringUtil.copyPartialMatches(args[args.length - 1], commands, list);
            Collections.sort(list);
            return list;
        }
        return null;
    }

    private List<String> getPlayers(String startsWith) {
        final List<String> list = new ArrayList<>();
        for (Player onlinePlayer : plugin.getServer().getOnlinePlayers()) {
            if (onlinePlayer.isOnline()) {
                final String name = onlinePlayer.getName();
                if (name.startsWith(startsWith)) {
                    list.add(name);
                }
            }
        }
        return list;
    }
}