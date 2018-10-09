package org.mcmonkey.sentinel;

import net.citizensnpcs.api.trait.trait.Inventory;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

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
                if (SentinelTarget.v1_9) {
                    if (mat == Material.ARROW || mat == Material.TIPPED_ARROW || mat == Material.SPECTRAL_ARROW) {
                        return item.clone();
                    }
                }
                else {
                    if (mat == Material.ARROW) {
                        return item.clone();
                    }
                }
            }
        }
        return sentinel.needsAmmo ? null : new ItemStack(Material.ARROW, 1);
    }

    /**
     * Reduces the durability of the NPC's held item.
     */
    public void reduceDurability() {
        if (SentinelTarget.v1_9) {
            ItemStack item = getLivingEntity().getEquipment().getItemInMainHand();
            if (item != null && item.getType() != Material.AIR) {
                if (item.getDurability() >= item.getType().getMaxDurability() - 1) {
                    getLivingEntity().getEquipment().setItemInMainHand(null);
                }
                else {
                    item.setDurability((short) (item.getDurability() + 1));
                    getLivingEntity().getEquipment().setItemInMainHand(item);
                }
            }
        }
        else {
            ItemStack item = getLivingEntity().getEquipment().getItemInHand();
            if (item != null && item.getType() != Material.AIR) {
                if (item.getDurability() >= item.getType().getMaxDurability() - 1) {
                    getLivingEntity().getEquipment().setItemInHand(null);
                }
                else {
                    item.setDurability((short) (item.getDurability() + 1));
                    getLivingEntity().getEquipment().setItemInHand(item);
                }
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
                if (mat == Material.ARROW || (SentinelTarget.v1_9 && (mat == Material.TIPPED_ARROW || mat == Material.SPECTRAL_ARROW))) {
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
     * Takes a snowball from the NPC's inventory.
     */
    public void takeSnowball() {
        if (!getNPC().hasTrait(Inventory.class)) {
            return;
        }
        Inventory inv = getNPC().getTrait(Inventory.class);
        ItemStack[] items = inv.getContents();
        for (int i = 0; i < items.length; i++) {
            ItemStack item = items[i];
            if (item != null) {
                Material mat = item.getType();
                if (mat == SentinelTarget.MATERIAL_SNOW_BALL) {
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
        if (SentinelTarget.v1_9) {
            ItemStack item = getLivingEntity().getEquipment().getItemInMainHand();
            if (item != null && item.getType() != Material.AIR) {
                if (item.getAmount() > 1) {
                    item.setAmount(item.getAmount() - 1);
                    getLivingEntity().getEquipment().setItemInMainHand(item);
                }
                else {
                    getLivingEntity().getEquipment().setItemInMainHand(null);
                }
            }
        }
        else {
            ItemStack item = getLivingEntity().getEquipment().getItemInHand();
            if (item != null && item.getType() != Material.AIR) {
                if (item.getAmount() > 1) {
                    item.setAmount(item.getAmount() - 1);
                    getLivingEntity().getEquipment().setItemInHand(item);
                }
                else {
                    getLivingEntity().getEquipment().setItemInHand(null);
                }
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
        if (!getNPC().hasTrait(Inventory.class)) {
            return;
        }
        int i = 0;
        Inventory inv = getNPC().getTrait(Inventory.class);
        ItemStack[] items = inv.getContents();
        ItemStack held = items[0] == null ? null : items[0].clone();
        boolean edit = false;
        while (!isRanged() && i < items.length - 1) {
            i++;
            if (items[i] != null && items[i].getType() != Material.AIR) {
                items[0] = items[i].clone();
                items[i] = new ItemStack(Material.AIR);
                inv.setContents(items);
                edit = true;
            }
        }
        if (edit) {
            items[i] = held;
            inv.setContents(items);
        }
    }

    /**
     * Swaps the NPC to a melee weapon if possible.
     */
    public void swapToMelee() {
        if (!getNPC().hasTrait(Inventory.class)) {
            return;
        }
        int i = 0;
        Inventory inv = getNPC().getTrait(Inventory.class);
        ItemStack[] items = inv.getContents();
        ItemStack held = items[0] == null ? null : items[0].clone();
        boolean edit = false;
        while (isRanged() && i < items.length - 1) {
            i++;
            if (items[i] != null && items[i].getType() != Material.AIR) {
                items[0] = items[i].clone();
                items[i] = new ItemStack(Material.AIR);
                inv.setContents(items);
                edit = true;
            }
        }
        if (edit) {
            items[i] = held;
            inv.setContents(items);
        }
    }

    /**
     * Returns whether the NPC is holding a ranged weapon.
     */
    public boolean isRanged() {
        return usesBow()
                || usesFireball()
                || usesSnowball()
                || usesLightning()
                || usesSpectral()
                || usesPotion();
    }

    /**
     * Returns the item held by an NPC.
     */
    public ItemStack getHeldItem() {
        if (!getNPC().hasTrait(Inventory.class)) {
            if (!getNPC().isSpawned()) {
                return null;
            }
            // TODO: Just only use this (instead of Inventory trait)?
            if (SentinelTarget.v1_9) {
                return getLivingEntity().getEquipment().getItemInMainHand();
            }
            else {
                return getLivingEntity().getEquipment().getItemInHand();
            }
        }
        return getNPC().getTrait(Inventory.class).getContents()[0];
    }

    /**
     * Returns whether the NPC is using a bow item.
     */
    public boolean usesBow() {
        ItemStack it = getHeldItem();
        return it != null && it.getType() == Material.BOW && getArrow() != null;
    }

    /**
     * Returns whether the NPC is using a fireball item.
     */
    public boolean usesFireball() {
        ItemStack it = getHeldItem();
        return it != null && it.getType() == SentinelTarget.MATERIAL_BLAZE_ROD;
    }

    /**
     * Returns whether the NPC is using a snowball item.
     */
    public boolean usesSnowball() {
        ItemStack it = getHeldItem();
        return it != null && it.getType() == SentinelTarget.MATERIAL_SNOW_BALL;
    }

    /**
     * Returns whether the NPC is using a lightning-attack item.
     */
    public boolean usesLightning() {
        ItemStack it = getHeldItem();
        return it != null && it.getType() == SentinelTarget.MATERIAL_NETHER_STAR;
    }

    /**
     * Returns whether the NPC is using an egg item.
     */
    public boolean usesEgg() {
        ItemStack it = getHeldItem();
        return it != null && it.getType() == Material.EGG;
    }

    /**
     * Returns whether the NPC is using a peal item.
     */
    public boolean usesPearl() {
        ItemStack it = getHeldItem();
        return it != null && it.getType() == Material.ENDER_PEARL;
    }

    /**
     * Returns whether the NPC is using a wither-skull item.
     */
    public boolean usesWitherSkull() {
        if (!SentinelPlugin.instance.canUseSkull) {
            return false;
        }
        ItemStack it = getHeldItem();
        return it != null && SentinelTarget.SKULL_MATERIALS.contains(it.getType());
    }

    /**
     * Returns whether the NPC is using a spectral-effect-attack item.
     */
    public boolean usesSpectral() {
        if (!SentinelTarget.v1_10) {
            return false;
        }
        ItemStack it = getHeldItem();
        return it != null && it.getType() == Material.SPECTRAL_ARROW;
    }

    /**
     * Returns whether the NPC is using a potion item.
     */
    public boolean usesPotion() {
        ItemStack it = getHeldItem();
        return it != null && SentinelTarget.POTION_MATERIALS.contains(it.getType());
    }

    /**
     * Returns whether the NPC can take durability from the held item.
     */
    public boolean shouldTakeDura() {
        Material type = getHeldItem().getType();
        return SentinelTarget.BOW_MATERIALS.contains(type) || SentinelTarget.SWORD_MATERIALS.contains(type)
                || SentinelTarget.PICKAXE_MATERIALS.contains(type) || SentinelTarget.AXE_MATERIALS.contains(type);
    }
}
