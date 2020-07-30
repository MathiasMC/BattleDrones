package me.MathiasMC.BattleDrones.managers;

import com.google.common.base.Strings;
import me.MathiasMC.BattleDrones.BattleDrones;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

public class CalculateManager {

    private final BattleDrones plugin;

    public CalculateManager(final BattleDrones plugin) {
        this.plugin = plugin;
    }

    public void damage(LivingEntity livingEntity, double damage) {
        PotionEffect effect = livingEntity.getPotionEffect(PotionEffectType.DAMAGE_RESISTANCE);
        int resistance = effect == null ? 0 : effect.getAmplifier();
        int e = 0;
        if (livingEntity instanceof Player) {
            e = getEnchant(((Player) livingEntity).getInventory());
        }
        AttributeInstance armor = livingEntity.getAttribute(Attribute.GENERIC_ARMOR);
        AttributeInstance toughness = livingEntity.getAttribute(Attribute.GENERIC_ARMOR_TOUGHNESS);
        double armorValue = 0;
        if (armor != null) {
            armorValue = armor.getValue();
        }
        double toughnessValue = 0;
        if (toughness != null) {
            toughnessValue = toughness.getValue();
        }
        double dialed = dialed(damage, armorValue, toughnessValue, resistance, e);
        livingEntity.damage(dialed);
    }

    private int getEnchant(PlayerInventory inv) {
        ItemStack helm = inv.getHelmet();
        ItemStack chest = inv.getChestplate();
        ItemStack legs = inv.getLeggings();
        ItemStack boot = inv.getBoots();
        return (helm != null ? helm.getEnchantmentLevel(Enchantment.DAMAGE_ALL) : 0) +
                (chest != null ? chest.getEnchantmentLevel(Enchantment.DAMAGE_ALL) : 0) +
                (legs != null ? legs.getEnchantmentLevel(Enchantment.DAMAGE_ALL) : 0) +
                (boot != null ? boot.getEnchantmentLevel(Enchantment.DAMAGE_ALL) : 0);
    }

    public double dialed(double damage, double armor, double toughness, int resistance, int enchant) {
        return damage * (1 - Math.min(20, Math.max(armor / 5, armor - damage / (2 + toughness / 4))) / 25) * (1 - (resistance * 0.2)) * (1 - (Math.min(20.0, enchant) / 25));
    }

    public String getFirerate(double firerate) {
        DecimalFormat decimalFormat = (DecimalFormat) NumberFormat.getNumberInstance(new Locale("en", "UK"));
        decimalFormat.applyPattern("#.##");
        return decimalFormat.format(20 / firerate);
    }

    public String getBar(int current, int max, String path, String extra) {
        char xp = (char) Integer.parseInt(plugin.config.get.getString(path + extra + "." + path + ".symbol").substring(2), 16);
        char none = (char) Integer.parseInt(plugin.config.get.getString(path + extra + ".none.symbol").substring(2), 16);
        ChatColor xpColor = getChatColor(plugin.config.get.getString(path + extra + "." + path + ".color"));
        ChatColor noneColor = getChatColor(plugin.config.get.getString(path + extra + ".none.color"));
        int bars = plugin.config.get.getInt(path + extra + ".amount");
        int progressBars = (bars * getPercent(current, max) / 100);
        try {
            return Strings.repeat("" + xpColor + xp, progressBars) + Strings.repeat("" + noneColor + none, bars - progressBars);
        } catch (Exception exception) {
            return "";
        }
    }

    public int getPercent(int current, int max) {
        if (current > max) {
            return 100;
        }
        double percent = ((double) current / (double) max) * 100;
        return (int) Math.round(percent);
    }

    public int getProcentFromDouble(double current) {
        double percent = current * 100;
        return (int) Math.round(percent);
    }

    public ChatColor getChatColor(String colorCode){
        switch (colorCode){
            case "&0" : return ChatColor.BLACK;
            case "&1" : return ChatColor.DARK_BLUE;
            case "&2" : return ChatColor.DARK_GREEN;
            case "&3" : return ChatColor.DARK_AQUA;
            case "&4" : return ChatColor.DARK_RED;
            case "&5" : return ChatColor.DARK_PURPLE;
            case "&6" : return ChatColor.GOLD;
            case "&7" : return ChatColor.GRAY;
            case "&8" : return ChatColor.DARK_GRAY;
            case "&9" : return ChatColor.BLUE;
            case "&a" : return ChatColor.GREEN;
            case "&b" : return ChatColor.AQUA;
            case "&c" : return ChatColor.RED;
            case "&d" : return ChatColor.LIGHT_PURPLE;
            case "&e" : return ChatColor.YELLOW;
            case "&f" : return ChatColor.WHITE;
            case "&k" : return ChatColor.MAGIC;
            case "&l" : return ChatColor.BOLD;
            case "&m" : return ChatColor.STRIKETHROUGH;
            case "&n" : return ChatColor.UNDERLINE;
            case "&o" : return ChatColor.ITALIC;
            case "&r" : return ChatColor.RESET;
            default: return ChatColor.WHITE;
        }
    }
}