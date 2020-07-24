package me.MathiasMC.BattleDrones.managers;

import com.google.common.base.Strings;
import me.MathiasMC.BattleDrones.BattleDrones;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

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
        if (armor != null && toughness != null) {
            double dialed = dialed(damage, armor.getValue(), toughness.getValue(), resistance, e);
            livingEntity.damage(dialed);
        } else {
            livingEntity.damage(damage);
        }
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


    public String getHealthBar(long current, long max) {
        char xp = (char) Integer.parseInt(plugin.config.get.getString("health.health.symbol").substring(2), 16);
        char none = (char) Integer.parseInt(plugin.config.get.getString("health.none.symbol").substring(2), 16);
        ChatColor xpColor = getChatColor(plugin.config.get.getString("health.health.color"));
        ChatColor noneColor = getChatColor(plugin.config.get.getString("health.none.color"));
        int bars = plugin.config.get.getInt("health.amount");
        int progressBars = (int) (bars * getPercent(current, max) / 100);
        try {
            return Strings.repeat("" + xpColor + xp, progressBars) + Strings.repeat("" + noneColor + none, bars - progressBars);
        } catch (Exception exception) {
            return "";
        }
    }

    public String getHealthBarPlaceholder(long current, long max) {
        char xp = (char) Integer.parseInt(plugin.config.get.getString("health-placeholder.health.symbol").substring(2), 16);
        char none = (char) Integer.parseInt(plugin.config.get.getString("health-placeholder.none.symbol").substring(2), 16);
        ChatColor xpColor = getChatColor(plugin.config.get.getString("health-placeholder.health.color"));
        ChatColor noneColor = getChatColor(plugin.config.get.getString("health-placeholder.none.color"));
        int bars = plugin.config.get.getInt("health-placeholder.amount");
        int progressBars = (int) (bars * getPercent(current, max) / 100);
        try {
            return Strings.repeat("" + xpColor + xp, progressBars) + Strings.repeat("" + noneColor + none, bars - progressBars);
        } catch (Exception exception) {
            return "";
        }
    }

    public long getPercent(long current, long max) {
        double percent = ((double) current / (double) max) * 100;
        return Math.round(percent);
    }

    public long getProcentFromDouble(double current) {
        double percent = current * 100;
        return Math.round(percent);
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

    public void line(Location start, Location end, FileConfiguration file, String path) {
        final double distance = start.distance(end);
        final Vector p1Vector = start.toVector();
        final World world = start.getWorld();
        final double space = file.getDouble(path + "particle-line.space");
        final int r = file.getInt(path + "particle-line.rgb.r");
        final int g = file.getInt(path + "particle-line.rgb.g");
        final int b = file.getInt(path + "particle-line.rgb.b");
        final int amount = file.getInt(path + "particle-line.amount");
        final int size = file.getInt(path + "particle-line.size");
        final Vector vector = end.toVector().clone().subtract(p1Vector).normalize().multiply(space);
        double length = 0;
        for (; length < distance; p1Vector.add(vector)) {
            Particle.DustOptions dustOptions = new Particle.DustOptions(Color.fromRGB(r, g, b), size);
            start.getWorld().spawnParticle(Particle.REDSTONE, p1Vector.toLocation(world), amount, 0, 0, 0, 0F, dustOptions);
            length += space;
        }
    }

    public void sphere(Location location, double r, double rows, int RGB_R, int RGB_G, int RGB_B, int sizeParticle, int amountParticle) {
        for (double phi = 0; phi <= Math.PI; phi += Math.PI / rows) {
            for (double theta = 0; theta <= 2 * Math.PI; theta += Math.PI / rows) {
                double x = r * Math.cos(theta) * Math.sin(phi);
                double y = r * Math.cos(phi) + 0.3;
                double z = r * Math.sin(theta) * Math.sin(phi);
                location.add(x, y, z);
                Particle.DustOptions dustOptions = new Particle.DustOptions(Color.fromRGB(RGB_R, RGB_G, RGB_B), sizeParticle);
                location.getWorld().spawnParticle(Particle.REDSTONE, location, amountParticle, 0, 0, 0, 0F, dustOptions);
                location.subtract(x, y, z);
            }
        }
    }
}