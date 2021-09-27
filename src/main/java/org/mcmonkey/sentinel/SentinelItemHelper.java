package org.mcmonkey.sentinel;

import net.citizensnpcs.api.trait.trait.Inventory;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.mcmonkey.sentinel.utilities.SentinelVersionCompat;

import java.util.function.Function;

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
        Inventory inv = getNPC().getOrAddTrait(Inventory.class);
        ItemStack[] items = inv.getContents();
        for (ItemStack item : items) {
            if (item != null) {
                Material mat = item.getType();
                if (mat == Material.ARROW
                        || (SentinelVersionCompat.v1_9 && (mat == Material.TIPPED_ARROW || mat == Material.SPECTRAL_ARROW))
                        || (SentinelVersionCompat.v1_14 && mat == Material.FIREWORK_ROCKET)) {
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
            if (getLivingEntity().getEquipment() == null) {
                return;
            }
            if (SentinelVersionCompat.v1_9) {
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
        Inventory inv = getNPC().getOrAddTrait(Inventory.class);
        ItemStack[] items = inv.getContents();
        for (int i = 0; i < items.length; i++) {
            ItemStack item = items[i];
            if (item != null) {
                Material mat = item.getType();
                if (mat == Material.ARROW
                        || (SentinelVersionCompat.v1_9 && (mat == Material.TIPPED_ARROW || mat == Material.SPECTRAL_ARROW))
                        || (SentinelVersionCompat.v1_14 && mat == Material.FIREWORK_ROCKET)) {
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
            if (getLivingEntity().getEquipment() == null) {
                return;
            }
            if (SentinelVersionCompat.v1_9) {
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
        Inventory inv = getNPC().getOrAddTrait(Inventory.class);
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
                if (SentinelVersionCompat.isWeapon(mat)) {
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
     * Swaps offhand to shield for defense, if possible. Returns true if a shield was equipped.
     */
    public boolean swapToShield() {
        if (!SentinelVersionCompat.v1_9 || !getNPC().isSpawned() || !getNPC().hasTrait(Inventory.class)) {
            return false;
        }
        Inventory inv = getNPC().getOrAddTrait(Inventory.class);
        ItemStack[] items = inv.getContents();
        int index = -1;
        // Skip index 0 (main hand)
        for (int i = 1; i < items.length; i++) {
            if (items[i] != null && items[i].getType() == Material.SHIELD) {
                index = i;
                break;
            }
        }
        // Index 40 is offhand.
        if (index != -1 && index != 40) {
            ItemStack oldItem = SentinelUtilities.getOffhandItem(sentinel.getLivingEntity()).clone();
            ItemStack newItem = items[index].clone();
            items[index] = oldItem;
            items[40] = newItem;
            inv.setContents(items);
            sentinel.getLivingEntity().getEquipment().setItemInOffHand(newItem);
            return true;
        }
        return false;
    }

    /**
     * Swaps weapon to the first item that matches the input function.
     */
    public void swapToMatch(Function<ItemStack, Boolean> doSwap, boolean isRanged) {
        if (!getNPC().isSpawned() || !getNPC().hasTrait(Inventory.class)) {
            return;
        }
        Inventory inv = getNPC().getOrAddTrait(Inventory.class);
        ItemStack[] items = inv.getContents();
        ItemStack held = items[0] == null ? null : items[0].clone();
        if (doSwap.apply(held)) {
            return;
        }
        int bestIndex = -1;
        double bestDamage = -1;
        for (int i = 0; i < items.length; i++) {
            if (sentinel.getLivingEntity() instanceof Player && i >= 36 && i <= 39) {
                // Patch for armor, which is "in the inventory" but not really tracked through it
                continue;
            }
            if (doSwap.apply(items[i])) {
                double possibleDamage = sentinel.getDamage(isRanged, items[i]);
                if (possibleDamage > bestDamage) {
                    bestDamage = possibleDamage;
                    bestIndex = i;
                }
            }
        }
        if (bestIndex != -1) {
            items[0] = items[bestIndex] == null ? null : items[bestIndex].clone();
            items[bestIndex] = held == null ? null : held.clone();
            inv.setContents(items);
            if (sentinel.getLivingEntity() instanceof Player && bestIndex == 40 && SentinelVersionCompat.v1_9 && sentinel.getLivingEntity().getEquipment() != null) {
                // Patch for offhand, which is "in the inventory" but not really tracked through it
                sentinel.getLivingEntity().getEquipment().setItemInOffHand(items[bestIndex]);
            }
        }
    }

    /**
     * Swaps the NPC to an open hand if possible.
     */
    public void swapToOpenHand() {
        swapToMatch(i -> i == null || i.getType() == Material.AIR, false);
    }

    /**
     * Swaps the NPC to a ranged weapon if possible.
     */
    public void swapToRanged() {
        swapToMatch(i -> i != null && i.getType() != Material.AIR && isRanged(i), true);
    }

    /**
     * Swaps the NPC to a melee weapon if possible.
     */
    public void swapToMelee() {
        swapToMatch(this::isMeleeWeapon, false);
    }

    /**
     * Returns whether the NPC is holding a shield in its offhand.
     */
    public boolean hasShield() {
        if (SentinelVersionCompat.v1_9) {
            ItemStack item = SentinelUtilities.getOffhandItem(sentinel.getLivingEntity());
            return item != null && item.getType() == Material.SHIELD;
        }
        return false;
    }

    /**
     * Returns whether the item is some form of melee weapon.
     */
    public boolean isMeleeWeapon(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) {
            return false;
        }
        item = autoRedirect(item);
        if (SentinelVersionCompat.isWeapon(item.getType()) && !isRanged(item)) {
            return true;
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
                || usesPotion(item)
                || usesLlamaSpit(item)
                || usesShulkerBullet(item);
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
            return autoRedirect(getNPC().getOrAddTrait(Inventory.class).getContents()[0]);
        }
        return null;
    }

    /**
     * Returns whether the NPC is using a bow item.
     */
    public boolean usesBow(ItemStack it) {
        if (it == null) {
            return false;
        }
        if (SentinelVersionCompat.v1_14) {
            if (it.getType() == Material.CROSSBOW && getArrow() != null) {
                return true;
            }
        }
        return it.getType() == Material.BOW && getArrow() != null;
    }

    /**
     * Returns whether the NPC is using a fireball item.
     */
    public boolean usesFireball(ItemStack it) {
        return it != null && it.getType() == SentinelVersionCompat.MATERIAL_BLAZE_ROD;
    }

    /**
     * Returns whether the NPC is using a snowball item.
     */
    public boolean usesSnowball(ItemStack it) {
        return it != null && it.getType() == SentinelVersionCompat.MATERIAL_SNOW_BALL;
    }

    /**
     * Returns whether the NPC is using a lightning-attack item.
     */
    public boolean usesLightning(ItemStack it) {
        return it != null && it.getType() == SentinelVersionCompat.MATERIAL_NETHER_STAR;
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
    public boolean usesPearl(ItemStack it) {
        return it != null && it.getType() == Material.ENDER_PEARL;
    }

    /**
     * Returns whether the NPC is using a wither-skull item.
     */
    public boolean usesWitherSkull(ItemStack it) {
        if (!SentinelPlugin.instance.canUseSkull) {
            return false;
        }
        return it != null && SentinelVersionCompat.SKULL_MATERIALS.contains(it.getType());
    }

    /**
     * Returns whether the NPC is using a trident item.
     */
    public boolean usesTrident(ItemStack it) {
        if (!SentinelVersionCompat.v1_13) {
            return false;
        }
        return it != null && it.getType() == Material.TRIDENT;
    }

    /**
     * Returns whether the NPC is using a spectral-effect-attack item.
     */
    public boolean usesSpectral(ItemStack it) {
        if (!SentinelVersionCompat.v1_10) {
            return false;
        }
        return it != null && it.getType() == Material.SPECTRAL_ARROW;
    }

    /**
     * Returns whether the NPC is using a potion item.
     */
    public boolean usesPotion(ItemStack it) {
        return it != null && SentinelVersionCompat.POTION_MATERIALS.contains(it.getType());
    }

    /**
     * Returns whether the NPC is using a llama spit item (white_dye).
     */
    public boolean usesLlamaSpit(ItemStack it) {
        if (!SentinelVersionCompat.v1_16) {
            return false;
        }
        return it != null && it.getType() == Material.WHITE_DYE;
    }

    /**
     * Returns whether the NPC is using a shulker bullet item.
     */
    public boolean usesShulkerBullet(ItemStack it) {
        if (!SentinelPlugin.instance.canUseSkull) {
            return false;
        }
        if (!SentinelVersionCompat.v1_13) {
            return false;
        }
        return it != null && it.getType() == Material.SHULKER_SHELL;
    }

    /**
     * Returns whether the NPC is using an evoker fangs attack book.
     */
    public boolean usesFangsBook(ItemStack it) {
        if (!SentinelPlugin.instance.canUseSkull) {
            return false;
        }
        if (!SentinelVersionCompat.v1_13) {
            return false;
        }
        return it != null && it.getType() == Material.BOOK;
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
        return SentinelVersionCompat.BOW_MATERIALS.contains(type) || SentinelVersionCompat.SWORD_MATERIALS.contains(type)
                || SentinelVersionCompat.PICKAXE_MATERIALS.contains(type) || SentinelVersionCompat.AXE_MATERIALS.contains(type);
    }
}
