package org.mcmonkey.sentinel;

import net.citizensnpcs.api.trait.trait.Inventory;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.mcmonkey.sentinel.targeting.SentinelTarget;

/**
 * Helper for item management.
 */
public class SentinelItemHelper extends SentinelHelperObject {

    /**
     * Gets the correct ArrowItem type for the NPC based on inventory items (can be null if the NPC needs ammo but has none).
     */
    public ItemStack getArrow() {
        if (!getNPC().hasTrait(Inventory.class)) {
            return sentinel.needsAmmo ? null : new ItemStack(Material.ARROW, 1);
        }
        Inventory inv = getNPC().getTrait(Inventory.class);
        ItemStack[] items = inv.getContents();
        for (ItemStack item : items) {
            if (item != null) {
                Material mat = item.getType();
                if (mat == Material.ARROW
                        || (SentinelTarget.v1_9 && (mat == Material.TIPPED_ARROW || mat == Material.SPECTRAL_ARROW))
                        || (SentinelTarget.v1_14 && mat == Material.FIREWORK_ROCKET)) {
                    return item.clone();
                }
            }
        }
        return sentinel.needsAmmo ? null : new ItemStack(Material.ARROW, 1);
    }

    /**
     * Reduces the durability of the NPC's held item.
     */
    public void reduceDurability() {
        ItemStack toSet;
        ItemStack item = getHeldItem();
        if (item != null && item.getType() != Material.AIR) {
            if (item.getDurability() >= item.getType().getMaxDurability() - 1) {
                toSet = null;
            }
            else {
                item.setDurability((short) (item.getDurability() + 1));
                toSet = item;
            }
            if (SentinelTarget.v1_9) {
                getLivingEntity().getEquipment().setItemInMainHand(toSet);
            }
            else {
                getLivingEntity().getEquipment().setItemInHand(toSet);
            }
        }
    }

    /**
     * Takes an arrow from the NPC's inventory.
     */
    public void takeArrow() {
        if (!getNPC().hasTrait(Inventory.class)) {
            return;
        }
        Inventory inv = getNPC().getTrait(Inventory.class);
        ItemStack[] items = inv.getContents();
        for (int i = 0; i < items.length; i++) {
            ItemStack item = items[i];
            if (item != null) {
                Material mat = item.getType();
                if (mat == Material.ARROW
                        || (SentinelTarget.v1_9 && (mat == Material.TIPPED_ARROW || mat == Material.SPECTRAL_ARROW))
                        || (SentinelTarget.v1_14 && mat == Material.FIREWORK_ROCKET)) {
                    if (item.getAmount() > 1) {
                        item.setAmount(item.getAmount() - 1);
                        items[i] = item;
                        inv.setContents(items);
                        return;
                    }
                    else {
                        items[i] = null;
                        inv.setContents(items);
                        return;
                    }
                }
            }
        }
    }

    /**
     * Takes one item from the NPC's held items (for consumables).
     */
    public void takeOne() {
        ItemStack toSet;
        ItemStack item = getHeldItem();
        if (item != null && item.getType() != Material.AIR) {
            if (item.getAmount() > 1) {
                item.setAmount(item.getAmount() - 1);
                toSet = item;
            }
            else {
                toSet = null;
            }
            if (SentinelTarget.v1_9) {
                getLivingEntity().getEquipment().setItemInMainHand(toSet);
            }
            else {
                getLivingEntity().getEquipment().setItemInHand(toSet);
            }
        }
    }

    /**
     * Grabs the next item for an NPC to use and moves it into the NPC's hand.
     */
    public void grabNextItem() {
        if (!getNPC().hasTrait(Inventory.class)) {
            return;
        }
        Inventory inv = getNPC().getTrait(Inventory.class);
        ItemStack[] items = inv.getContents();
        ItemStack held = items[0];
        if (held != null && held.getType() != Material.AIR) {
            return;
        }
        for (int i = 0; i < items.length; i++) {
            ItemStack item = items[i];
            if (item != null) {
                item = item.clone();
                Material mat = item.getType();
                if (SentinelTarget.isWeapon(mat)) {
                    if (item.getAmount() > 1) {
                        item.setAmount(item.getAmount() - 1);
                        items[i] = item;
                        items[0] = item.clone();
                        items[0].setAmount(1);
                        inv.setContents(items);
                        item = item.clone();
                        item.setAmount(1);
                        return;
                    }
                    else {
                        items[i] = new ItemStack(Material.AIR);
                        items[0] = item.clone();
                        inv.setContents(items);
                        return;
                    }
                }
            }
        }
    }

    /**
     * Swaps the NPC to a ranged weapon if possible.
     */
    public void swapToRanged() {
        if (!getNPC().isSpawned() || !getNPC().hasTrait(Inventory.class)) {
            return;
        }
        Inventory inv = getNPC().getTrait(Inventory.class);
        ItemStack[] items = inv.getContents();
        ItemStack held = items[0] == null ? null : items[0].clone();
        if (isRanged(held)) {
            return;
        }
        for (int i = 0; i < items.length; i++) {
            if (sentinel.getLivingEntity() instanceof Player && i >= 36 && i <= 39) {
                // Patch for armor, which is "in the inventory" but not really tracked through it
                continue;
            }
            if (items[i] != null && items[i].getType() != Material.AIR && isRanged(items[i])) {
                items[0] = items[i].clone();
                items[i] = held == null ? null : held.clone();
                inv.setContents(items);
                if (sentinel.getLivingEntity() instanceof Player && i == 40) {
                    // Patch for offhand, which is "in the inventory" but not really tracked through it
                    sentinel.getLivingEntity().getEquipment().setItemInOffHand(items[i]);
                }
                return;
            }
        }
    }

    /**
     * Swaps the NPC to a melee weapon if possible.
     */
    public void swapToMelee() {
        if (!getNPC().isSpawned() || !getNPC().hasTrait(Inventory.class)) {
            return;
        }
        Inventory inv = getNPC().getTrait(Inventory.class);
        ItemStack[] items = inv.getContents();
        ItemStack held = items[0] == null ? null : items[0].clone();
        if (!isRanged(held)) {
            return;
        }
        for (int i = 0; i < items.length; i++) {
            if (sentinel.getLivingEntity() instanceof Player && i >= 36 && i <= 39) {
                // Patch for armor, which is "in the inventory" but not really tracked through it
                continue;
            }
            if (items[i] != null && items[i].getType() != Material.AIR && !isRanged(items[i])) {
                items[0] = items[i].clone();
                items[i] = held == null ? null : held.clone();
                inv.setContents(items);
                if (sentinel.getLivingEntity() instanceof Player && i == 40) {
                    // Patch for offhand, which is "in the inventory" but not really tracked through it
                    sentinel.getLivingEntity().getEquipment().setItemInOffHand(items[i]);
                }
                return;
            }
        }
    }

    /**
     * Returns whether the NPC is holding a shield in its offhand.
     */
    public boolean hasShield() {
        if (SentinelTarget.v1_9) {
            ItemStack item = SentinelUtilities.getOffhandItem(sentinel.getLivingEntity());
            return item != null && item.getType() == Material.SHIELD;
        }
        return false;
    }

    /**
     * Returns whether the NPC is holding a ranged weapon.
     */
    public boolean isRanged() {
        return isRanged(getHeldItem());
    }

    /**
     * Returns whether the NPC is holding a ranged weapon.
     */
    public boolean isRanged(ItemStack item) {
        return usesBow(item)
                || usesFireball(item)
                || usesSnowball(item)
                || usesLightning(item)
                || usesEgg(item)
                || usesPearl(item)
                || usesWitherSkull(item)
                || usesTrident(item)
                || usesSpectral(item)
                || usesPotion(item);
    }

    /**
     * Processes weapon redirection for an item, returning the redirected item (or an unchanged one).
     */
    public ItemStack autoRedirect(ItemStack stack) {
        if (stack == null) {
            return null;
        }
        String redirect = sentinel.weaponRedirects.get(stack.getType().name().toLowerCase());
        if (redirect == null) {
            return stack;
        }
        Material mat = Material.valueOf(redirect.toUpperCase());
        ItemStack newStack = stack.clone();
        newStack.setType(mat);
        return newStack;
    }

    /**
     * Returns the item held by an NPC.
     */
    public ItemStack getHeldItem() {
        if (!getNPC().isSpawned()) {
            return null;
        }
        ItemStack stack = SentinelUtilities.getHeldItem(getLivingEntity());
        if (stack != null && stack.getType() != Material.AIR) {
            return autoRedirect(stack);
        }
        if (getNPC().hasTrait(Inventory.class)) {
            // Note: this allows entities that don't normally have equipment to still 'hold' weapons (eg a cow can hold a bow)
            return autoRedirect(getNPC().getTrait(Inventory.class).getContents()[0]);
        }
        return null;
    }

    /**
     * Returns whether the NPC is using a bow item.
     */
    public boolean usesBow() {
        return usesBow(getHeldItem());
    }

    /**
     * Returns whether the NPC is using a bow item.
     */
    public boolean usesBow(ItemStack it) {
        if (it == null) {
            return false;
        }
        if (SentinelTarget.v1_14) {
            if (it.getType() == Material.CROSSBOW && getArrow() != null) {
                return true;
            }
        }
        return it.getType() == Material.BOW && getArrow() != null;
    }

    /**
     * Returns whether the NPC is using a fireball item.
     */
    public boolean usesFireball() {
        return usesFireball(getHeldItem());
    }

    /**
     * Returns whether the NPC is using a fireball item.
     */
    public boolean usesFireball(ItemStack it) {
        return it != null && it.getType() == SentinelTarget.MATERIAL_BLAZE_ROD;
    }

    /**
     * Returns whether the NPC is using a snowball item.
     */
    public boolean usesSnowball() {
        return usesSnowball(getHeldItem());
    }

    /**
     * Returns whether the NPC is using a snowball item.
     */
    public boolean usesSnowball(ItemStack it) {
        return it != null && it.getType() == SentinelTarget.MATERIAL_SNOW_BALL;
    }

    /**
     * Returns whether the NPC is using a lightning-attack item.
     */
    public boolean usesLightning() {
        return usesLightning(getHeldItem());
    }

    /**
     * Returns whether the NPC is using a lightning-attack item.
     */
    public boolean usesLightning(ItemStack it) {
        return it != null && it.getType() == SentinelTarget.MATERIAL_NETHER_STAR;
    }

    /**
     * Returns whether the NPC is using an egg item.
     */
    public boolean usesEgg() {
        return usesEgg(getHeldItem());
    }

    /**
     * Returns whether the NPC is using an egg item.
     */
    public boolean usesEgg(ItemStack it) {
        return it != null && it.getType() == Material.EGG;
    }

    /**
     * Returns whether the NPC is using a pearl item.
     */
    public boolean usesPearl() {
        return usesPearl(getHeldItem());
    }

    /**
     * Returns whether the NPC is using a pearl item.
     */
    public boolean usesPearl(ItemStack it) {
        return it != null && it.getType() == Material.ENDER_PEARL;
    }

    /**
     * Returns whether the NPC is using a wither-skull item.
     */
    public boolean usesWitherSkull() {
        return usesWitherSkull(getHeldItem());
    }

    /**
     * Returns whether the NPC is using a wither-skull item.
     */
    public boolean usesWitherSkull(ItemStack it) {
        if (!SentinelPlugin.instance.canUseSkull) {
            return false;
        }
        return it != null && SentinelTarget.SKULL_MATERIALS.contains(it.getType());
    }

    /**
     * Returns whether the NPC is using a trident item.
     */
    public boolean usesTrident() {
        return usesTrident(getHeldItem());
    }

    /**
     * Returns whether the NPC is using a trident item.
     */
    public boolean usesTrident(ItemStack it) {
        if (!SentinelTarget.v1_13) {
            return false;
        }
        return it != null && it.getType() == Material.TRIDENT;
    }

    /**
     * Returns whether the NPC is using a spectral-effect-attack item.
     */
    public boolean usesSpectral() {
        return usesSpectral(getHeldItem());
    }

    /**
     * Returns whether the NPC is using a spectral-effect-attack item.
     */
    public boolean usesSpectral(ItemStack it) {
        if (!SentinelTarget.v1_10) {
            return false;
        }
        return it != null && it.getType() == Material.SPECTRAL_ARROW;
    }

    /**
     * Returns whether the NPC is using a potion item.
     */
    public boolean usesPotion() {
        return usesPotion(getHeldItem());
    }

    /**
     * Returns whether the NPC is using a potion item.
     */
    public boolean usesPotion(ItemStack it) {
        return it != null && SentinelTarget.POTION_MATERIALS.contains(it.getType());
    }

    /**
     * Returns whether the NPC can take durability from the held item.
     */
    public boolean shouldTakeDura() {
        ItemStack it = getHeldItem();
        if (it == null) {
            return false;
        }
        Material type = it.getType();
        return SentinelTarget.BOW_MATERIALS.contains(type) || SentinelTarget.SWORD_MATERIALS.contains(type)
                || SentinelTarget.PICKAXE_MATERIALS.contains(type) || SentinelTarget.AXE_MATERIALS.contains(type);
    }
}
