package me.MathiasMC.BattleDrones.utils;

import me.MathiasMC.BattleDrones.BattleDrones;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.logging.Logger;

public class TextUtils {

    private final BattleDrones plugin;

    private final Logger logger = Bukkit.getLogger();

    private final String prefix;

    public TextUtils(final BattleDrones plugin) {
        this.plugin = plugin;
        this.prefix = plugin.getDescription().getName();
    }

    public void info(String text) {
        logger.info("[" + prefix + "] " + text);
    }

    public void warning(String text) {
        logger.warning("[" + prefix + "] " + text);
    }

    public void error(String text) {
        logger.severe("[" + prefix + "] " + text);
    }

    public void debug(String text) {
        logger.warning("[" + prefix + "] [DEBUG] " + text);
    }

    public void exception(StackTraceElement[] stackTraceElement, String text) {
        info("(!) " + prefix + " has being encountered an error, pasting below for support (!)");
        for (int i = 0; i < stackTraceElement.length; i++) {
            error(stackTraceElement[i].toString());
        }
        info("Message: " + text);
        info(prefix + " version: " + plugin.getDescription().getVersion());
        info("Please report this error to me on spigot");
        info("(!) " + prefix + " (!)");
    }

    public void gui(CommandSender target, String itemType, String material) {
        target.sendMessage(ChatColor.translateAlternateColorCodes('&',"&cAn error has occurred, " + itemType + " item &7" + material + " &cis not found."));
    }
}