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

    public CalculateManager(final BattleDrones plugin) {
        this.plugin = plugin;
    }

    public final ArrayList<Double> x = new ArrayList<>();
    public final ArrayList<Double> y = new ArrayList<>();
    public final ArrayList<Double> z = new ArrayList<>();

    public void damage(final LivingEntity livingEntity, final double damage) {
        final PotionEffect effect = livingEntity.getPotionEffect(PotionEffectType.DAMAGE_RESISTANCE);
        final int resistance = effect == null ? 0 : effect.getAmplifier();
        int e = 0;
        if (livingEntity instanceof Player) {
            e = getEnchant(((Player) livingEntity).getInventory());
        }
        final AttributeInstance armor = livingEntity.getAttribute(Attribute.GENERIC_ARMOR);
        final AttributeInstance toughness = livingEntity.getAttribute(Attribute.GENERIC_ARMOR_TOUGHNESS);
        double armorValue = 0;
        if (armor != null) {
            armorValue = armor.getValue();
        }
        double toughnessValue = 0;
        if (toughness != null) {
            toughnessValue = toughness.getValue();
        }
        final double dialed = dialed(damage, armorValue, toughnessValue, resistance, e);
        livingEntity.damage(dialed);
    }

    private int getEnchant(final PlayerInventory inv) {
        final ItemStack helm = inv.getHelmet();
        final ItemStack chest = inv.getChestplate();
        final ItemStack legs = inv.getLeggings();
        final ItemStack boot = inv.getBoots();
        return (helm != null ? helm.getEnchantmentLevel(Enchantment.DAMAGE_ALL) : 0) +
                (chest != null ? chest.getEnchantmentLevel(Enchantment.DAMAGE_ALL) : 0) +
                (legs != null ? legs.getEnchantmentLevel(Enchantment.DAMAGE_ALL) : 0) +
                (boot != null ? boot.getEnchantmentLevel(Enchantment.DAMAGE_ALL) : 0);
    }

    private double dialed(final double damage, final double armor, final double toughness, final int resistance, final int enchant) {
        return damage * (1 - Math.min(20, Math.max(armor / 5, armor - damage / (2 + toughness / 4))) / 25) * (1 - (resistance * 0.2)) * (1 - (Math.min(20.0, enchant) / 25));
    }

    public String getFirerate(final double firerate) {
        final DecimalFormat decimalFormat = (DecimalFormat) NumberFormat.getNumberInstance(new Locale("en", "UK"));
        decimalFormat.applyPattern("#.##");
        return decimalFormat.format(20 / firerate);
    }

    public String getBar(final int current, final int max, final String path, final String extra) {
        final char xp = (char) Integer.parseInt(Objects.requireNonNull(plugin.getFileUtils().config.getString(path + extra + "." + path + ".symbol")).substring(2), 16);
        final char none = (char) Integer.parseInt(Objects.requireNonNull(plugin.getFileUtils().config.getString(path + extra + ".none.symbol")).substring(2), 16);
        final ChatColor xpColor = getChatColor(Objects.requireNonNull(plugin.getFileUtils().config.getString(path + extra + "." + path + ".color")));
        final ChatColor noneColor = getChatColor(Objects.requireNonNull(plugin.getFileUtils().config.getString(path + extra + ".none.color")));
        final int bars = plugin.getFileUtils().config.getInt(path + extra + ".amount");
        final int progressBars = (bars * getPercent(current, max) / 100);
        try {
            return Strings.repeat("" + xpColor + xp, progressBars) + Strings.repeat("" + noneColor + none, bars - progressBars);
        } catch (Exception exception) {
            return "";
        }
    }

    public int getPercent(final int current, final int max) {
        if (current > max) {
            return 100;
        }
        final double percent = ((double) current / (double) max) * 100;
        return (int) Math.round(percent);
    }

    public int getProcentFromDouble(final double current) {
        final double percent = current * 100;
        return (int) Math.round(percent);
    }

    public boolean isInt(final String s) {
        try {
            Integer.parseInt(s);
        } catch(NumberFormatException e) {
            return false;
        }
        return true;
    }

    public boolean isFloat(final String s) {
        try {
            Float.parseFloat(s);
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }

    public boolean isString(final String text) {
        return text.matches("^[a-zA-Z]*$");
    }

    public double randomDouble(final double min, final double max) {
        return min + Math.random() * (max - min);
    }

    public float randomChance() {
        return new Random().nextFloat();
    }

    public ChatColor getChatColor(final String colorCode){
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