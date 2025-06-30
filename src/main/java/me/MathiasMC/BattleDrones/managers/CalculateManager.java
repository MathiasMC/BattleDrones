package me.MathiasMC.BattleDrones.managers;

import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;
import java.util.stream.Stream;

public class CalculateManager {

    public final ArrayList<Double> x = new ArrayList<>();
    public final ArrayList<Double> y = new ArrayList<>();
    public final ArrayList<Double> z = new ArrayList<>();

    public void damage(LivingEntity livingEntity, double damage) {
        int resistance = Optional.ofNullable(livingEntity.getPotionEffect(PotionEffectType.INSTANT_DAMAGE))
                .map(PotionEffect::getAmplifier)
                .orElse(0);

        int enchantLevel = 0;
        if (livingEntity instanceof Player player) {
            PlayerInventory inv = player.getInventory();
            enchantLevel = Stream.of(inv.getHelmet(), inv.getChestplate(), inv.getLeggings(), inv.getBoots())
                    .filter(Objects::nonNull)
                    .mapToInt(item -> item.getEnchantmentLevel(Enchantment.PROTECTION))
                    .sum();
        }

        double armorValue = Optional.ofNullable(livingEntity.getAttribute(Attribute.ARMOR))
                .map(AttributeInstance::getValue)
                .orElse(0.0);

        double toughnessValue = Optional.ofNullable(livingEntity.getAttribute(Attribute.ARMOR_TOUGHNESS))
                .map(AttributeInstance::getValue)
                .orElse(0.0);

        double armorReduction = Math.min(20, Math.max(armorValue / 5, armorValue - damage / (2 + toughnessValue / 4)));

        double resistanceReduction = resistance * 0.2;
        double enchantReduction = Math.min(20.0, enchantLevel) / 25;

        double finalDamage = damage * (1 - armorReduction / 25) * (1 - resistanceReduction) * (1 - enchantReduction);

        livingEntity.damage(finalDamage);
    }

    public String getFirerate(double firerate) {
        DecimalFormat decimalFormat = (DecimalFormat) NumberFormat.getNumberInstance(Locale.of("en", "GB"));
        decimalFormat.applyPattern("#.##");
        return decimalFormat.format(20 / firerate);
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
}