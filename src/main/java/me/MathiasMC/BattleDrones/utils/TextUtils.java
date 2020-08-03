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

    public void info(final String text) {
        logger.info("[" + prefix + "] " + text);
    }

    public void warning(final String text) {
        logger.warning("[" + prefix + "] " + text);
    }

    public void error(final String text) {
        logger.severe("[" + prefix + "] " + text);
    }

    public void exception(final StackTraceElement[] stackTraceElement, final String text) {
        info("(!) " + prefix + " has being encountered an error, pasting below for support (!)");
        for (StackTraceElement traceElement : stackTraceElement) {
            error(traceElement.toString());
        }
        info("Message: " + text);
        info(prefix + " version: " + plugin.getDescription().getVersion());
        info("Please report this error to me on spigot");
        info("(!) " + prefix + " (!)");
    }

    public void gui(final CommandSender target, final String itemType, final String material) {
        target.sendMessage(ChatColor.translateAlternateColorCodes('&',"&cAn error has occurred, " + itemType + " item &7" + material + " &cis not found."));
    }
}