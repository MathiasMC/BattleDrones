package me.MathiasMC.BattleDrones.utils;

import me.MathiasMC.BattleDrones.BattleDrones;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.Objects;

public class ControllerUtils {

    private final PlayerInventory playerInventory;
    private ItemStack controller;
    private int range;

    public ControllerUtils(final PlayerInventory playerInventory) {
        BattleDrones plugin = BattleDrones.getInstance();
        this.playerInventory = playerInventory;
        final ItemStack mainHand = playerInventory.getItemInMainHand();
        final ItemStack offHand = playerInventory.getItemInOffHand();
        final NamespacedKey key = new NamespacedKey(plugin, "drone_controller");
        if (offHand.hasItemMeta()) {
            final PersistentDataContainer persistentDataContainer = Objects.requireNonNull(offHand.getItemMeta()).getPersistentDataContainer();
            if (persistentDataContainer.has(key, PersistentDataType.INTEGER)) {
                this.range = persistentDataContainer.getOrDefault(key, PersistentDataType.INTEGER, 0);
                this.controller = offHand;
            }
        }
        if (mainHand.hasItemMeta()) {
            final PersistentDataContainer persistentDataContainer = Objects.requireNonNull(mainHand.getItemMeta()).getPersistentDataContainer();
            if (persistentDataContainer.has(key, PersistentDataType.INTEGER)) {
                this.range = persistentDataContainer.getOrDefault(key, PersistentDataType.INTEGER, 0);
                this.controller = mainHand;
            }
        }
    }

    public int getRange() {
        return this.range;
    }

    public boolean hasController() {
        return this.controller != null && this.range != 0;
    }

    public void damage(final Player player, final int damage) {
        if (damage == 0) {
            return;
        }
        if (player.hasPermission("battledrones.bypass.controller.durability")) {
            return;
        }
        final ItemMeta itemMeta = controller.getItemMeta();
        if (itemMeta instanceof Damageable) {
            final Damageable damageable = (Damageable) controller.getItemMeta();
            damageable.setDamage(damageable.getDamage() + damage);
            controller.setItemMeta((ItemMeta) damageable);
            if (damageable.getDamage() >= controller.getType().getMaxDurability()) {
                playerInventory.remove(controller);
            }
        }
    }
}
