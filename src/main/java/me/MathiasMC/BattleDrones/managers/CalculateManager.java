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
import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;

public class CalculateManager {

    private final BattleDrones plugin;

    public CalculateManager(BattleDrones plugin) {
        this.plugin = plugin;
    }

    public final ArrayList<Double> x = new ArrayList<>();
    public final ArrayList<Double> y = new ArrayList<>();
    public final ArrayList<Double> z = new ArrayList<>();

    public void damage(LivingEntity livingEntity, double damage) {
        PotionEffect effect = livingEntity.getPotionEffect(PotionEffectType.INSTANT_DAMAGE);
        int resistance = effect == null ? 0 : effect.getAmplifier();
        int e = 0;
        if (livingEntity instanceof Player) {
            e = getEnchant(((Player) livingEntity).getInventory());
        }
        AttributeInstance armor = livingEntity.getAttribute(Attribute.ARMOR);
        AttributeInstance toughness = livingEntity.getAttribute(Attribute.ARMOR_TOUGHNESS);
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
        return (helm != null ? helm.getEnchantmentLevel(Enchantment.PROTECTION) : 0) +
                (chest != null ? chest.getEnchantmentLevel(Enchantment.PROTECTION) : 0) +
                (legs != null ? legs.getEnchantmentLevel(Enchantment.PROTECTION) : 0) +
                (boot != null ? boot.getEnchantmentLevel(Enchantment.PROTECTION) : 0);
    }

    private double dialed(double damage, double armor, double toughness, int resistance, int enchant) {
        return damage * (1 - Math.min(20, Math.max(armor / 5, armor - damage / (2 + toughness / 4))) / 25) * (1 - (resistance * 0.2)) * (1 - (Math.min(20.0, enchant) / 25));
    }

    public String getFirerate(double firerate) {
        DecimalFormat decimalFormat = (DecimalFormat) NumberFormat.getNumberInstance(Locale.of("en", "GB"));
        decimalFormat.applyPattern("#.##");
        return decimalFormat.format(20 / firerate);
    }

    public String getBar(int current, int max, String path, String extra) {
        char xp = (char) Integer.parseInt(Objects.requireNonNull(plugin.getFileUtils().config.getString(path + extra + "." + path + ".symbol")).substring(2), 16);
        char none = (char) Integer.parseInt(Objects.requireNonNull(plugin.getFileUtils().config.getString(path + extra + ".none.symbol")).substring(2), 16);
        ChatColor xpColor = getChatColor(Objects.requireNonNull(plugin.getFileUtils().config.getString(path + extra + "." + path + ".color")));
        ChatColor noneColor = getChatColor(Objects.requireNonNull(plugin.getFileUtils().config.getString(path + extra + ".none.color")));
        int bars = plugin.getFileUtils().config.getInt(path + extra + ".amount");
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

    public boolean isInt(String s) {
        try {
            Integer.parseInt(s);
        } catch(NumberFormatException e) {
            return false;
        }
        return true;
    }

    public boolean isFloat(String s) {
        try {
            Float.parseFloat(s);
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }

    public boolean isString(String text) {
        return text.matches("^[a-zA-Z]*$");
    }

    public double randomDouble(double min, double max) {
        return min + Math.random() * (max - min);
    }

    public float randomChance() {
        return new Random().nextFloat();
    }

    public ChatColor getChatColor(String colorCode){
        return switch (colorCode) {
            case "&0" -> ChatColor.BLACK;
            case "&1" -> ChatColor.DARK_BLUE;
            case "&2" -> ChatColor.DARK_GREEN;
            case "&3" -> ChatColor.DARK_AQUA;
            case "&4" -> ChatColor.DARK_RED;
            case "&5" -> ChatColor.DARK_PURPLE;
            case "&6" -> ChatColor.GOLD;
            case "&7" -> ChatColor.GRAY;
            case "&8" -> ChatColor.DARK_GRAY;
            case "&9" -> ChatColor.BLUE;
            case "&a" -> ChatColor.GREEN;
            case "&b" -> ChatColor.AQUA;
            case "&c" -> ChatColor.RED;
            case "&d" -> ChatColor.LIGHT_PURPLE;
            case "&e" -> ChatColor.YELLOW;
            case "&f" -> ChatColor.WHITE;
            case "&k" -> ChatColor.MAGIC;
            case "&l" -> ChatColor.BOLD;
            case "&m" -> ChatColor.STRIKETHROUGH;
            case "&n" -> ChatColor.UNDERLINE;
            case "&o" -> ChatColor.ITALIC;
            case "&r" -> ChatColor.RESET;
            default -> ChatColor.WHITE;
        };
    }
}