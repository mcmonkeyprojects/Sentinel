package org.mcmonkey.sentinel.utilities;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.mcmonkey.sentinel.SentinelPlugin;
import org.mcmonkey.sentinel.targeting.SentinelTarget;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SentinelVersionCompat {

    /**
     * HashSets of Materials within a category (for multi-version support).
     */
    public static final HashSet<Material> SWORD_MATERIALS = new HashSet<>(),
            PICKAXE_MATERIALS = new HashSet<>(),
            AXE_MATERIALS = new HashSet<>(),
            HELMET_MATERIALS = new HashSet<>(),
            CHESTPLATE_MATERIALS = new HashSet<>(),
            LEGGINGS_MATERIALS = new HashSet<>(),
            BOOTS_MATERIALS = new HashSet<>(),
            BOW_MATERIALS = new HashSet<>(),
            POTION_MATERIALS = new HashSet<>(),
            SKULL_MATERIALS = new HashSet<>(),
            OTHER_RANGED_MATERIALS = new HashSet<>();

    /**
     * A map of weapon materials to their damage multipliers.
     */
    public static final Map<Material, Double> WEAPON_DAMAGE_MULTIPLIERS = new HashMap<>();

    /**
     * A map of armor materials to their protection multipliers.
     */
    public static final Map<Material, Double> ARMOR_PROTECTION_MULTIPLIERS = new HashMap<>();

    /**
     * A specific material (for multi-version support).
     */
    public static final Material MATERIAL_SNOW_BALL, MATERIAL_NETHER_STAR, MATERIAL_BLAZE_ROD;

    /**
     * Boolean indicating if the server version is >= the named version.
     */
    public static final boolean v1_8, v1_9, v1_10, v1_11, v1_12, v1_13, v1_14, v1_15, v1_16, v1_17, v1_18, vFuture;

    static {
        String vers = Bukkit.getBukkitVersion(); // Returns in format like: 1.12.2-R0.1-SNAPSHOT
        vFuture = vers.startsWith("1.19") || vers.startsWith("1.20") || vers.startsWith("1.21");
        v1_18 = vers.startsWith("1.18") || vFuture;
        v1_17 = vers.startsWith("1.17") || v1_18;
        v1_16 = vers.startsWith("1.16") || v1_17;
        v1_15 = vers.startsWith("1.15") || v1_16;
        v1_14 = vers.startsWith("1.14") || v1_15;
        v1_13 = vers.startsWith("1.13") || v1_14;
        v1_12 = vers.startsWith("1.12") || v1_13;
        v1_11 = vers.startsWith("1.11") || v1_12;
        v1_10 = vers.startsWith("1.10") || v1_11;
        v1_9 = vers.startsWith("1.9") || v1_10;
        v1_8 = vers.startsWith("1.8") || v1_9;
        if (v1_8 && !v1_9) {
            SentinelTarget.PASSIVE_MOBS = new SentinelTarget(v1_8_passive(), passiveNames());
            SentinelTarget.MOBS = new SentinelTarget(combine(v1_8_passive(), v1_8_monsters()), "MOB");
            SentinelTarget.MONSTERS = new SentinelTarget(v1_8_monsters(), "MONSTER");
        }
        if (v1_9) {
            SentinelTarget.SHULKERS = new SentinelTarget(new EntityType[]{EntityType.SHULKER}, "SHULKER");
        }
        if (v1_9 && !v1_10) {
            SentinelTarget.PASSIVE_MOBS = new SentinelTarget(v1_8_passive(), passiveNames()); // No new passives in 1.9
            SentinelTarget.MOBS = new SentinelTarget(combine(v1_8_passive(), v1_9_monsters()), "MOB");
            SentinelTarget.MONSTERS = new SentinelTarget(v1_9_monsters(), "MONSTER");
        }
        if (v1_10) {
            SentinelTarget.POLAR_BEARS = new SentinelTarget(new EntityType[]{EntityType.POLAR_BEAR}, "POLARBEAR", "POLAR_BEAR");
        }
        if (v1_10 && !v1_11) {
            SentinelTarget.PASSIVE_MOBS = new SentinelTarget(v1_10_passive(), passiveNames());
            SentinelTarget.MOBS = new SentinelTarget(combine(v1_10_passive(), v1_9_monsters()), "MOB"); // No new monsters in 1.10
            SentinelTarget.MONSTERS = new SentinelTarget(v1_9_monsters(), "MONSTER");
        }
        if (v1_11) {
            SentinelTarget.VEXES = new SentinelTarget(new EntityType[]{EntityType.VEX}, "VEX", "VEXE");
            SentinelTarget.DONKEYS = new SentinelTarget(new EntityType[]{EntityType.DONKEY}, "DONKEY");
            SentinelTarget.LLAMAS = new SentinelTarget(new EntityType[]{EntityType.LLAMA}, "LLAMA");
            SentinelTarget.MULES = new SentinelTarget(new EntityType[]{EntityType.MULE}, "MULE");
            SentinelTarget.HUSKS = new SentinelTarget(new EntityType[]{EntityType.HUSK}, "HUSK");
            SentinelTarget.ELDER_GUARDIANS = new SentinelTarget(new EntityType[]{EntityType.ELDER_GUARDIAN}, "ELDER_GUARDIAN", "ELDERGUARDIAN");
            SentinelTarget.EVOKERS = new SentinelTarget(new EntityType[]{EntityType.EVOKER}, "EVOKER");
            SentinelTarget.SKELETON_HORSES = new SentinelTarget(new EntityType[]{EntityType.SKELETON_HORSE}, "SKELETON_HORSE", "SKELETONHORSE");
            SentinelTarget.STRAYS = new SentinelTarget(new EntityType[]{EntityType.STRAY}, "STRAY");
            SentinelTarget.ZOMBIE_VILLAGERS = new SentinelTarget(new EntityType[]{EntityType.ZOMBIE_VILLAGER}, "ZOMBIE_VILLAGER", "ZOMBIEVILLAGER");
            SentinelTarget.ZOMBIE_HORSES = new SentinelTarget(new EntityType[]{EntityType.ZOMBIE_HORSE}, "ZOMBIE_HORSE", "ZOMBIEHORSE");
            SentinelTarget.WITHER_SKELETONS = new SentinelTarget(new EntityType[]{EntityType.WITHER_SKELETON}, "WITHER_SKELETON", "WITHERSKELETON");
            SentinelTarget.VINDICATORS = new SentinelTarget(new EntityType[]{EntityType.VINDICATOR}, "VINDICATOR");
        }
        if (v1_11 && !v1_12) {
            SentinelTarget.PASSIVE_MOBS = new SentinelTarget(v1_11_passive(), passiveNames());
            SentinelTarget.MOBS = new SentinelTarget(combine(v1_11_passive(), v1_11_monsters()), "MOB");
            SentinelTarget.MONSTERS = new SentinelTarget(v1_11_monsters(), "MONSTER");
        }
        if (v1_12) {
            SentinelTarget.PARROTS = new SentinelTarget(new EntityType[]{EntityType.PARROT}, "PARROT");
            SentinelTarget.ILLUSIONERS = new SentinelTarget(new EntityType[]{EntityType.ILLUSIONER}, "ILLUSIONER");
        }
        if (v1_12 && !v1_13) {
            SentinelTarget.PASSIVE_MOBS = new SentinelTarget(v1_12_passive(), passiveNames());
            SentinelTarget.MOBS = new SentinelTarget(combine(v1_12_passive(), v1_12_monsters()), "MOB");
            SentinelTarget.MONSTERS = new SentinelTarget(v1_12_monsters(), "MONSTER");
        }
        if (v1_13) {
            SentinelTarget.DOLPHINS = new SentinelTarget(new EntityType[]{EntityType.DOLPHIN}, "DOLPHIN");
            SentinelTarget.DROWNED = new SentinelTarget(new EntityType[]{EntityType.DROWNED}, "DROWNED");
            SentinelTarget.COD = new SentinelTarget(new EntityType[]{EntityType.COD}, "COD");
            SentinelTarget.SALMON = new SentinelTarget(new EntityType[]{EntityType.SALMON}, "SALMON");
            SentinelTarget.PUFFERFISH = new SentinelTarget(new EntityType[]{EntityType.PUFFERFISH}, "PUFFERFISH", "PUFFERFISHE");
            SentinelTarget.TROPICAL_FISH = new SentinelTarget(new EntityType[]{EntityType.TROPICAL_FISH}, "TROPICAL_FISH", "TROPICALFISH", "TROPICAL_FISHE", "TROPICALFISHE");
            SentinelTarget.FISH = new SentinelTarget(new EntityType[]{EntityType.TROPICAL_FISH, EntityType.PUFFERFISH, EntityType.SALMON, EntityType.COD}, "FISH", "FISHE");
            SentinelTarget.PHANTOMS = new SentinelTarget(new EntityType[]{EntityType.PHANTOM}, "PHANTOM");
            SentinelTarget.TURTLES = new SentinelTarget(new EntityType[]{EntityType.TURTLE}, "TURTLE");
        }
        if (v1_13 && !v1_14) {
            SentinelTarget.PASSIVE_MOBS = new SentinelTarget(v1_13_passive(), passiveNames());
            SentinelTarget.MOBS = new SentinelTarget(combine(v1_13_passive(), v1_13_monsters()), "MOB");
            SentinelTarget.MONSTERS = new SentinelTarget(v1_13_monsters(), "MONSTER");
        }
        if (v1_14) {
            SentinelTarget.RAVAGERS = new SentinelTarget(new EntityType[]{EntityType.RAVAGER}, "RAVAGER");
            SentinelTarget.PILLAGERS = new SentinelTarget(new EntityType[]{EntityType.PILLAGER}, "PILLAGER");
            SentinelTarget.CATS = new SentinelTarget(new EntityType[]{EntityType.CAT}, "CAT");
            SentinelTarget.PANDAS = new SentinelTarget(new EntityType[]{EntityType.PANDA}, "PANDA");
            SentinelTarget.TRADER_LLAMAS = new SentinelTarget(new EntityType[]{EntityType.TRADER_LLAMA}, "TRADER_LLAMA", "TRADERLLAMA");
            SentinelTarget.WANDERING_TRADERS = new SentinelTarget(new EntityType[]{EntityType.WANDERING_TRADER}, "WANDERING_TRADER", "WANDERINGTRADER", "TRADER");
            SentinelTarget.FOXES = new SentinelTarget(new EntityType[]{EntityType.FOX}, "FOX", "FOXE");
        }
        if (v1_14 && !v1_15) {
            SentinelTarget.PASSIVE_MOBS = new SentinelTarget(v1_14_passive(), passiveNames());
            SentinelTarget.MOBS = new SentinelTarget(combine(v1_14_passive(), v1_14_monsters()), "MOB");
            SentinelTarget.MONSTERS = new SentinelTarget(v1_14_monsters(), "MONSTER");
            BOW_MATERIALS.add(getMaterial("CROSSBOW"));
        }
        if (v1_15) {
            SentinelTarget.BEES = new SentinelTarget(new EntityType[]{EntityType.BEE}, "BEE");
        }
        if (v1_15 && !v1_16) {
            SentinelTarget.PASSIVE_MOBS = new SentinelTarget(v1_14_passive(), passiveNames()); // no new passives in 1.15
            SentinelTarget.MOBS = new SentinelTarget(combine(v1_14_passive(), v1_15_monsters()), "MOB");
            SentinelTarget.MONSTERS = new SentinelTarget(v1_14_monsters(), "MONSTER");
        }
        if (!v1_16) {
            SentinelTarget.ZOMBIE_PIGMEN = new SentinelTarget(new EntityType[]{EntityType.valueOf("PIG_ZOMBIE")}, "PIG_ZOMBIE", "PIGZOMBIE", "ZOMBIEPIGMAN", "ZOMBIEPIGMEN", "ZOMBIE_PIGMAN", "ZOMBIE_PIGMEN", "ZOMBIE_PIGMAN");
        }
        if (v1_16) {
            SentinelTarget.HOGLINS = new SentinelTarget(new EntityType[]{EntityType.HOGLIN}, "HOGLIN");
            SentinelTarget.PIGLINS = new SentinelTarget(new EntityType[]{EntityType.PIGLIN}, "PIGLIN");
            SentinelTarget.STRIDERS = new SentinelTarget(new EntityType[]{EntityType.STRIDER}, "STRIDER");
            SentinelTarget.ZOGLINS = new SentinelTarget(new EntityType[]{EntityType.ZOGLIN}, "ZOGLIN");
            SentinelTarget.PIGLIN_BRUTE = new SentinelTarget(new EntityType[]{EntityType.PIGLIN_BRUTE}, "PIGLIN_BRUTE");
            SentinelTarget.ZOMBIFIED_PIGLINS = new SentinelTarget(new EntityType[]{EntityType.ZOMBIFIED_PIGLIN}, "ZOMBIFIED_PIGLIN", "ZOMBIFIEDPIGLIN", "ZOMBIE_PIGLIN", "ZOMBIEPIGLIN", "PIG_ZOMBIE", "PIGZOMBIE", "ZOMBIEPIGMAN", "ZOMBIEPIGMEN", "ZOMBIE_PIGMAN", "ZOMBIE_PIGMEN", "ZOMBIE_PIGMAN");
        }
        if (v1_16 && !v1_17) {
            SentinelTarget.PASSIVE_MOBS = new SentinelTarget(v1_16_passive(), passiveNames());
            SentinelTarget.MOBS = new SentinelTarget(combine(v1_16_passive(), v1_16_monsters()), "MOB");
            SentinelTarget.MONSTERS = new SentinelTarget(v1_16_monsters(), "MONSTER");
        }
        if (v1_17) {
            SentinelTarget.HOGLINS = new SentinelTarget(new EntityType[]{EntityType.GOAT}, "GOAT");
            SentinelTarget.PIGLINS = new SentinelTarget(new EntityType[]{EntityType.AXOLOTL}, "AXOLOTL");
        }
        if (v1_17) { // 1.18 and 1.17 have equivalent mob lists
            SentinelTarget.PASSIVE_MOBS = new SentinelTarget(v1_17_passive(), passiveNames());
            SentinelTarget.MOBS = new SentinelTarget(combine(v1_17_passive(), v1_16_monsters()), "MOB");
            SentinelTarget.MONSTERS = new SentinelTarget(v1_16_monsters(), "MONSTER");
        }
        // ========================== End Entities ==========================
        // ========================== Begin Materials ==========================
        if (v1_16) {
            addAllMaterials(SWORD_MATERIALS, "NETHERITE_SWORD");
            WEAPON_DAMAGE_MULTIPLIERS.put(getMaterial("NETHERITE_SWORD"), 8.0);
            addAllMaterials(PICKAXE_MATERIALS, "NETHERITE_PICKAXE");
            addAllMaterials(AXE_MATERIALS, "NETHERITE_AXE");
            addAllMaterials(HELMET_MATERIALS, "NETHERITE_HELMET");
            ARMOR_PROTECTION_MULTIPLIERS.put(getMaterial("NETHERITE_HELMET"), 0.12);
            addAllMaterials(CHESTPLATE_MATERIALS, "NETHERITE_CHESTPLATE");
            ARMOR_PROTECTION_MULTIPLIERS.put(getMaterial("NETHERITE_CHESTPLATE"), 0.32);
            addAllMaterials(LEGGINGS_MATERIALS, "NETHERITE_LEGGINGS");
            ARMOR_PROTECTION_MULTIPLIERS.put(getMaterial("NETHERITE_LEGGINGS"), 0.24);
            addAllMaterials(BOOTS_MATERIALS, "NETHERITE_BOOTS");
            ARMOR_PROTECTION_MULTIPLIERS.put(getMaterial("NETHERITE_BOOTS"), 0.12);
        }
        if (v1_13) {
            // Sword
            addAllMaterials(SWORD_MATERIALS, "DIAMOND_SWORD", "IRON_SWORD", "STONE_SWORD", "GOLDEN_SWORD", "WOODEN_SWORD");
            WEAPON_DAMAGE_MULTIPLIERS.put(getMaterial("DIAMOND_SWORD"), 7.0);
            WEAPON_DAMAGE_MULTIPLIERS.put(getMaterial("IRON_SWORD"), 6.0);
            WEAPON_DAMAGE_MULTIPLIERS.put(getMaterial("STONE_SWORD"), 5.0);
            WEAPON_DAMAGE_MULTIPLIERS.put(getMaterial("GOLDEN_SWORD"), 4.0);
            WEAPON_DAMAGE_MULTIPLIERS.put(getMaterial("WOODEN_SWORD"), 4.0);
            WEAPON_DAMAGE_MULTIPLIERS.put(getMaterial("TRIDENT"), 8.0);
            WEAPON_DAMAGE_MULTIPLIERS.put(getMaterial("BOOK"), 6.0);
            OTHER_RANGED_MATERIALS.add(getMaterial("TRIDENT"));
            // Pickaxe
            addAllMaterials(PICKAXE_MATERIALS, "DIAMOND_PICKAXE", "IRON_PICKAXE", "STONE_PICKAXE", "GOLDEN_PICKAXE", "WOODEN_PICKAXE");
            allMaterialsTo(WEAPON_DAMAGE_MULTIPLIERS, PICKAXE_MATERIALS, 3.0);
            // Axe
            addAllMaterials(AXE_MATERIALS, "DIAMOND_AXE", "IRON_AXE", "STONE_AXE", "GOLDEN_AXE", "WOODEN_AXE");
            allMaterialsTo(WEAPON_DAMAGE_MULTIPLIERS, AXE_MATERIALS, 9.0);
            // Bow
            BOW_MATERIALS.add(getMaterial("BOW"));
            // Helmet
            addAllMaterials(HELMET_MATERIALS, "DIAMOND_HELMET", "GOLDEN_HELMET", "IRON_HELMET", "LEATHER_HELMET", "CHAINMAIL_HELMET", "TURTLE_HELMET");
            ARMOR_PROTECTION_MULTIPLIERS.put(getMaterial("DIAMOND_HELMET"), 0.12);
            ARMOR_PROTECTION_MULTIPLIERS.put(getMaterial("GOLDEN_HELMET"), 0.08);
            ARMOR_PROTECTION_MULTIPLIERS.put(getMaterial("IRON_HELMET"), 0.08);
            ARMOR_PROTECTION_MULTIPLIERS.put(getMaterial("LEATHER_HELMET"), 0.04);
            ARMOR_PROTECTION_MULTIPLIERS.put(getMaterial("CHAINMAIL_HELMET"), 0.08);
            ARMOR_PROTECTION_MULTIPLIERS.put(getMaterial("TURTLE_HELMET"), 0.08);
            // Chestplate
            addAllMaterials(CHESTPLATE_MATERIALS, "DIAMOND_CHESTPLATE", "GOLDEN_CHESTPLATE", "IRON_CHESTPLATE", "LEATHER_CHESTPLATE", "CHAINMAIL_CHESTPLATE");
            ARMOR_PROTECTION_MULTIPLIERS.put(getMaterial("DIAMOND_CHESTPLATE"), 0.32);
            ARMOR_PROTECTION_MULTIPLIERS.put(getMaterial("GOLDEN_CHESTPLATE"), 0.20);
            ARMOR_PROTECTION_MULTIPLIERS.put(getMaterial("IRON_CHESTPLATE"), 0.24);
            ARMOR_PROTECTION_MULTIPLIERS.put(getMaterial("LEATHER_CHESTPLATE"), 0.12);
            ARMOR_PROTECTION_MULTIPLIERS.put(getMaterial("CHAINMAIL_CHESTPLATE"), 0.20);
            // Leggings
            addAllMaterials(LEGGINGS_MATERIALS, "DIAMOND_LEGGINGS", "GOLDEN_LEGGINGS", "IRON_LEGGINGS", "LEATHER_LEGGINGS", "CHAINMAIL_LEGGINGS");
            ARMOR_PROTECTION_MULTIPLIERS.put(getMaterial("DIAMOND_LEGGINGS"), 0.24);
            ARMOR_PROTECTION_MULTIPLIERS.put(getMaterial("GOLDEN_LEGGINGS"), 0.12);
            ARMOR_PROTECTION_MULTIPLIERS.put(getMaterial("IRON_LEGGINGS"), 0.20);
            ARMOR_PROTECTION_MULTIPLIERS.put(getMaterial("LEATHER_LEGGINGS"), 0.08);
            ARMOR_PROTECTION_MULTIPLIERS.put(getMaterial("CHAINMAIL_LEGGINGS"), 0.16);
            // Boots
            addAllMaterials(BOOTS_MATERIALS, "DIAMOND_BOOTS", "GOLDEN_BOOTS", "IRON_BOOTS", "LEATHER_BOOTS", "CHAINMAIL_BOOTS");
            ARMOR_PROTECTION_MULTIPLIERS.put(getMaterial("DIAMOND_BOOTS"), 0.12);
            ARMOR_PROTECTION_MULTIPLIERS.put(getMaterial("GOLDEN_BOOTS"), 0.04);
            ARMOR_PROTECTION_MULTIPLIERS.put(getMaterial("IRON_BOOTS"), 0.08);
            ARMOR_PROTECTION_MULTIPLIERS.put(getMaterial("LEATHER_BOOTS"), 0.04);
            ARMOR_PROTECTION_MULTIPLIERS.put(getMaterial("CHAINMAIL_BOOTS"), 0.04);
            // Potions
            addAllMaterials(POTION_MATERIALS, "POTION", "LINGERING_POTION", "SPLASH_POTION");
            // Skulls
            addAllMaterials(SKULL_MATERIALS, "WITHER_SKELETON_SKULL", "WITHER_SKELETON_WALL_SKULL");
            // Weapons
            MATERIAL_SNOW_BALL = getMaterial("SNOWBALL");
        }
        else { // v1_12 or lower
            // Sword
            addAllMaterials(SWORD_MATERIALS, "DIAMOND_SWORD", "IRON_SWORD", "STONE_SWORD", "GOLD_SWORD", "WOOD_SWORD");
            WEAPON_DAMAGE_MULTIPLIERS.put(getMaterial("DIAMOND_SWORD"), 7.0);
            WEAPON_DAMAGE_MULTIPLIERS.put(getMaterial("IRON_SWORD"), 6.0);
            WEAPON_DAMAGE_MULTIPLIERS.put(getMaterial("STONE_SWORD"), 5.0);
            WEAPON_DAMAGE_MULTIPLIERS.put(getMaterial("GOLD_SWORD"), 4.0);
            WEAPON_DAMAGE_MULTIPLIERS.put(getMaterial("WOOD_SWORD"), 4.0);
            // Pickaxe
            addAllMaterials(PICKAXE_MATERIALS, "DIAMOND_PICKAXE", "IRON_PICKAXE", "STONE_PICKAXE", "GOLD_PICKAXE", "WOOD_PICKAXE");
            allMaterialsTo(WEAPON_DAMAGE_MULTIPLIERS, PICKAXE_MATERIALS, 2.0);
            // Axe
            addAllMaterials(AXE_MATERIALS, "DIAMOND_AXE", "IRON_AXE", "STONE_AXE", "GOLD_AXE", "WOOD_AXE");
            allMaterialsTo(WEAPON_DAMAGE_MULTIPLIERS, AXE_MATERIALS, 3.0);
            // Bow
            BOW_MATERIALS.add(getMaterial("BOW"));
            // Helmet
            addAllMaterials(HELMET_MATERIALS, "DIAMOND_HELMET", "GOLD_HELMET", "IRON_HELMET", "LEATHER_HELMET", "CHAINMAIL_HELMET");
            ARMOR_PROTECTION_MULTIPLIERS.put(getMaterial("DIAMOND_HELMET"), 0.12);
            ARMOR_PROTECTION_MULTIPLIERS.put(getMaterial("GOLD_HELMET"), 0.08);
            ARMOR_PROTECTION_MULTIPLIERS.put(getMaterial("IRON_HELMET"), 0.08);
            ARMOR_PROTECTION_MULTIPLIERS.put(getMaterial("LEATHER_HELMET"), 0.04);
            ARMOR_PROTECTION_MULTIPLIERS.put(getMaterial("CHAINMAIL_HELMET"), 0.08);
            // Chestplate
            addAllMaterials(CHESTPLATE_MATERIALS, "DIAMOND_CHESTPLATE", "GOLD_CHESTPLATE", "IRON_CHESTPLATE", "LEATHER_CHESTPLATE", "CHAINMAIL_CHESTPLATE");
            ARMOR_PROTECTION_MULTIPLIERS.put(getMaterial("DIAMOND_CHESTPLATE"), 0.32);
            ARMOR_PROTECTION_MULTIPLIERS.put(getMaterial("GOLD_CHESTPLATE"), 0.20);
            ARMOR_PROTECTION_MULTIPLIERS.put(getMaterial("IRON_CHESTPLATE"), 0.24);
            ARMOR_PROTECTION_MULTIPLIERS.put(getMaterial("LEATHER_CHESTPLATE"), 0.12);
            ARMOR_PROTECTION_MULTIPLIERS.put(getMaterial("CHAINMAIL_CHESTPLATE"), 0.20);
            // Leggings
            addAllMaterials(LEGGINGS_MATERIALS, "DIAMOND_LEGGINGS", "GOLD_LEGGINGS", "IRON_LEGGINGS", "LEATHER_LEGGINGS", "CHAINMAIL_LEGGINGS");
            ARMOR_PROTECTION_MULTIPLIERS.put(getMaterial("DIAMOND_LEGGINGS"), 0.24);
            ARMOR_PROTECTION_MULTIPLIERS.put(getMaterial("GOLD_LEGGINGS"), 0.12);
            ARMOR_PROTECTION_MULTIPLIERS.put(getMaterial("IRON_LEGGINGS"), 0.20);
            ARMOR_PROTECTION_MULTIPLIERS.put(getMaterial("LEATHER_LEGGINGS"), 0.08);
            ARMOR_PROTECTION_MULTIPLIERS.put(getMaterial("CHAINMAIL_LEGGINGS"), 0.16);
            // Boots
            addAllMaterials(BOOTS_MATERIALS, "DIAMOND_BOOTS", "GOLD_BOOTS", "IRON_BOOTS", "LEATHER_BOOTS", "CHAINMAIL_BOOTS");
            ARMOR_PROTECTION_MULTIPLIERS.put(getMaterial("DIAMOND_BOOTS"), 0.12);
            ARMOR_PROTECTION_MULTIPLIERS.put(getMaterial("GOLD_BOOTS"), 0.04);
            ARMOR_PROTECTION_MULTIPLIERS.put(getMaterial("IRON_BOOTS"), 0.08);
            ARMOR_PROTECTION_MULTIPLIERS.put(getMaterial("LEATHER_BOOTS"), 0.04);
            ARMOR_PROTECTION_MULTIPLIERS.put(getMaterial("CHAINMAIL_BOOTS"), 0.04);
            // Potions
            if (v1_9) {
                addAllMaterials(POTION_MATERIALS, "POTION", "LINGERING_POTION", "SPLASH_POTION");
            }
            else {
                addAllMaterials(POTION_MATERIALS, "POTION");
            }
            // Skulls
            addAllMaterials(SKULL_MATERIALS, "SKULL", "SKULL_ITEM");
            // Weapons
            MATERIAL_SNOW_BALL = getMaterial("SNOW_BALL");
        }
        MATERIAL_NETHER_STAR = getMaterial("NETHER_STAR");
        MATERIAL_BLAZE_ROD = getMaterial("BLAZE_ROD");
    }

    static void warnOldVersion(String versionName, int yearsOld, String extra) {
        SentinelPlugin.instance.getLogger().warning("The version " + versionName + " is " + yearsOld + " years old (as of 2020) and therefore cannot be supported."
                + " Sentinel will work on this version, but we will not provide support if you have running it."
                + " Please update your server to a more recent minecraft version as soon as you are able." + extra);
    }

    public static void init() {
        int currentYear = Math.min(LocalDate.now().getYear(), 2021); // min in case clock is mis-set to some default like 2000 or something.
        if (v1_8 && !v1_9) {
            warnOldVersion("1.8", currentYear - 2014, " See also https://wiki.citizensnpcs.co/Minecraft_1.8");
        }
        else if (v1_9 && !v1_10) {
            warnOldVersion("1.9", currentYear - 2016, "");
        }
        else if (v1_10 && !v1_11) {
            warnOldVersion("1.10", currentYear - 2016, "");
        }
        else if (v1_11 && !v1_12) { // 2016
            warnOldVersion("1.11", currentYear - 2016, "");
        }
        else if (v1_12 && !vFuture) {
            SentinelPlugin.instance.getLogger().info("Sentinel loaded on a fully supported Minecraft version."
                + " If you encounter any issues or need to ask a question, please join our Discord at https://discord.gg/Q6pZGSR and post in the '#sentinel' channel.");
        }
        else {
            SentinelPlugin.instance.getLogger().warning("You are running on an unrecognized (future?) minecraft version."
                + " Support channel be guaranteed. Check if there is a newer version of Sentinel available that supports your minecraft version.");
        }
    }

    /**
     * Returns whether an item material is a valid weapon type.
     */
    public static boolean isWeapon(Material mat) {
        return WEAPON_DAMAGE_MULTIPLIERS.containsKey(mat)
                || POTION_MATERIALS.contains(mat)
                || BOW_MATERIALS.contains(mat)
                || SKULL_MATERIALS.contains(mat)
                || mat == MATERIAL_SNOW_BALL
                || mat == MATERIAL_BLAZE_ROD
                || mat == MATERIAL_NETHER_STAR;
    }

    /**
     * Returns whether an item is a valid ranged weapon type.
     */
    public static boolean isRangedWeapon(ItemStack item) {
        if (item == null) {
            return false;
        }
        Material mat = item.getType();
        return BOW_MATERIALS.contains(mat)
                || OTHER_RANGED_MATERIALS.contains(mat)
                || mat == MATERIAL_SNOW_BALL;
    }

    /**
     * Combines two arrays of EntityType values.
     */
    public static EntityType[] combine(EntityType[] a, EntityType... b) {
        EntityType[] types = new EntityType[a.length + b.length];
        System.arraycopy(a, 0, types, 0, a.length);
        System.arraycopy(b, 0, types, a.length, b.length);
        return types;
    }

    /**
     * Name array for passive mob types.
     */
    static String[] passiveNames() {
        return new String[]{"PASSIVE_MOB", "PASSIVEMOB", "GOODMOB", "GOOD_MOB", "FRIENDLYMOB", "FRIENDLY_MOB"};
    }

    static EntityType[] v1_8_passive() {
        return new EntityType[]{
                EntityType.PIG, EntityType.OCELOT, EntityType.COW, EntityType.RABBIT, EntityType.SHEEP, EntityType.CHICKEN, EntityType.MUSHROOM_COW,
                EntityType.HORSE, EntityType.IRON_GOLEM, EntityType.SQUID, EntityType.VILLAGER, EntityType.WOLF, EntityType.SNOWMAN
        };
    }

    static EntityType[] v1_10_passive() {
        return combine(v1_8_passive(), EntityType.POLAR_BEAR);
    }

    static EntityType[] v1_11_passive() {
        return combine(v1_10_passive(), EntityType.DONKEY, EntityType.LLAMA, EntityType.MULE, EntityType.ZOMBIE_HORSE, EntityType.SKELETON_HORSE);
    }

    static EntityType[] v1_12_passive() {
        return combine(v1_11_passive(), EntityType.PARROT);
    }

    static EntityType[] v1_13_passive() {
        return combine(v1_12_passive(), EntityType.DOLPHIN, EntityType.COD, EntityType.SALMON, EntityType.PUFFERFISH, EntityType.TROPICAL_FISH, EntityType.TURTLE);
    }

    static EntityType[] v1_14_passive() {
        return combine(v1_13_passive(), EntityType.CAT, EntityType.PANDA, EntityType.TRADER_LLAMA, EntityType.WANDERING_TRADER, EntityType.FOX);
    }

    static EntityType[] v1_16_passive() {
        return combine(v1_14_passive(), EntityType.STRIDER);
    }

    static EntityType[] v1_17_passive() {
        return combine(v1_16_passive(), EntityType.GOAT, EntityType.AXOLOTL);
    }

    static EntityType[] v1_8_monsters() {
        return new EntityType[]{EntityType.GUARDIAN, EntityType.CREEPER, EntityType.SKELETON, EntityType.ZOMBIE,
                EntityType.MAGMA_CUBE, EntityType.valueOf("PIG_ZOMBIE"), EntityType.SILVERFISH, EntityType.BAT, EntityType.BLAZE,
                EntityType.GHAST, EntityType.GIANT, EntityType.SLIME, EntityType.SPIDER, EntityType.CAVE_SPIDER, EntityType.ENDERMAN,
                EntityType.ENDERMITE, EntityType.WITHER, EntityType.ENDER_DRAGON, EntityType.WITCH};
    }

    static EntityType[] v1_9_monsters() {
        return combine(v1_8_monsters(), EntityType.SHULKER);
    }

    static EntityType[] v1_11_monsters() {
        return combine(v1_9_monsters(), EntityType.VEX, EntityType.HUSK, EntityType.ELDER_GUARDIAN,
                EntityType.EVOKER, EntityType.STRAY, EntityType.ZOMBIE_VILLAGER,
                EntityType.WITHER_SKELETON, EntityType.VINDICATOR);
    }

    static EntityType[] v1_12_monsters() {
        return combine(v1_11_monsters(), EntityType.ILLUSIONER);
    }

    static EntityType[] v1_13_monsters() {
        return combine(v1_12_monsters(), EntityType.DROWNED, EntityType.PHANTOM);
    }

    static EntityType[] v1_14_monsters() {
        return combine(v1_13_monsters(), EntityType.RAVAGER, EntityType.PILLAGER);
    }

    static EntityType[] v1_15_monsters() {
        return combine(v1_14_monsters(), EntityType.BEE);
    }

    static EntityType[] v1_16_monsters() {
        return new EntityType[]{
                // 1.8 minus PigZombie
                EntityType.GUARDIAN, EntityType.CREEPER, EntityType.SKELETON, EntityType.ZOMBIE,
                EntityType.MAGMA_CUBE, EntityType.SILVERFISH, EntityType.BAT, EntityType.BLAZE,
                EntityType.GHAST, EntityType.GIANT, EntityType.SLIME, EntityType.SPIDER, EntityType.CAVE_SPIDER, EntityType.ENDERMAN,
                EntityType.ENDERMITE, EntityType.WITHER, EntityType.ENDER_DRAGON, EntityType.WITCH,
                // 1.9
                EntityType.SHULKER,
                // 1.11
                EntityType.VEX, EntityType.HUSK, EntityType.ELDER_GUARDIAN,
                EntityType.EVOKER, EntityType.STRAY, EntityType.ZOMBIE_VILLAGER,
                EntityType.WITHER_SKELETON, EntityType.VINDICATOR,
                // 1.12
                EntityType.ILLUSIONER,
                // 1.13
                EntityType.DROWNED, EntityType.PHANTOM,
                // 1.14
                EntityType.RAVAGER, EntityType.PILLAGER,
                // 1.15
                EntityType.BEE,
                // 1.16
                EntityType.HOGLIN, EntityType.PIGLIN, EntityType.ZOGLIN, EntityType.ZOMBIFIED_PIGLIN, EntityType.PIGLIN_BRUTE
        };
    }

    /**
     * Gets the Material instance for a name (multi-version support).
     */
    public static Material getMaterial(String name) {
        try {
            return Material.valueOf(name);
        }
        catch (IllegalArgumentException ex) {
            SentinelPlugin.instance.getLogger().warning("Sentinel loader failed to handle material name '" + name + "', that material will not function (REPORT THIS ERROR!)");
            return Material.valueOf("STICK");
        }
    }

    /**
     * Adds a a list of named Materials to a set.
     */
    public static void addAllMaterials(Set<Material> set, String... matNames) {
        for (String mat : matNames) {
            set.add(getMaterial(mat));
        }
    }

    /**
     * Adds a set of materials to a map as keys with a specific value.
     */
    public static void allMaterialsTo(Map<Material, Double> map, Set<Material> set, Double val) {
        for (Material mat : set) {
            map.put(mat, val);
        }
    }
}
