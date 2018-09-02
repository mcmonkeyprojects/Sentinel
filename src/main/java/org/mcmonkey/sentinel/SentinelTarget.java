package org.mcmonkey.sentinel;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SentinelTarget {
    public static SentinelTarget NPCS = new SentinelTarget(new EntityType[]{}, "NPC");
    public static SentinelTarget OWNER = new SentinelTarget(new  EntityType[]{}, "OWNER");
    public static SentinelTarget PLAYERS = new SentinelTarget(new  EntityType[]{EntityType.PLAYER}, "PLAYER");
    public static SentinelTarget PIGS = new SentinelTarget(new  EntityType[]{EntityType.PIG}, "PIG");
    public static SentinelTarget OCELOTS = new SentinelTarget(new  EntityType[]{EntityType.OCELOT}, "OCELOT", "CAT");
    public static SentinelTarget COWS = new SentinelTarget(new  EntityType[]{EntityType.COW}, "COW");
    public static SentinelTarget RABBITS = new SentinelTarget(new  EntityType[]{EntityType.RABBIT}, "RABBIT", "BUNNY", "BUNNIE");
    public static SentinelTarget SHEEP = new SentinelTarget(new  EntityType[]{EntityType.SHEEP}, "SHEEP");
    public static SentinelTarget CHICKENS = new SentinelTarget(new  EntityType[]{EntityType.CHICKEN}, "CHICKEN", "DUCK");
    public static SentinelTarget HORSES = new SentinelTarget(new  EntityType[]{EntityType.HORSE}, "HORSE");
    public static SentinelTarget MUSHROOM_COW = new SentinelTarget(new  EntityType[]{EntityType.MUSHROOM_COW}, "MUSHROOM_COW", "MUSHROOMCOW", "MOOSHROOM");
    public static SentinelTarget IRON_GOLEMS = new SentinelTarget(new  EntityType[]{EntityType.IRON_GOLEM}, "IRON_GOLEM", "IRONGOLEM");
    public static SentinelTarget SQUIDS = new SentinelTarget(new  EntityType[]{EntityType.SQUID}, "SQUID");
    public static SentinelTarget VILLAGER = new SentinelTarget(new  EntityType[]{EntityType.VILLAGER}, "VILLAGER");
    public static SentinelTarget WOLF = new SentinelTarget(new  EntityType[]{EntityType.WOLF}, "WOLF");
    public static SentinelTarget SNOWMEN = new SentinelTarget(new  EntityType[]{EntityType.SNOWMAN}, "SNOWMAN", "SNOWMEN");
    public static SentinelTarget WITCH = new SentinelTarget(new  EntityType[]{EntityType.WITCH}, "WITCH");
    public static SentinelTarget GUARDIANS = new SentinelTarget(new  EntityType[]{EntityType.GUARDIAN}, "GUARDIAN");
    public static SentinelTarget CREERERS = new SentinelTarget(new  EntityType[]{EntityType.CREEPER}, "CREEPER");
    public static SentinelTarget SKELETONS = new SentinelTarget(new  EntityType[]{EntityType.SKELETON}, "SKELETON");
    public static SentinelTarget ZOMBIES = new SentinelTarget(new  EntityType[]{EntityType.ZOMBIE}, "ZOMBIE");
    public static SentinelTarget MAGMA_CUBES = new SentinelTarget(new  EntityType[]{EntityType.MAGMA_CUBE}, "MAGMA_CUBE", "MAGMACUBE");
    public static SentinelTarget ZOMBIE_PIGMEN = new SentinelTarget(new  EntityType[]{EntityType.PIG_ZOMBIE}, "PIG_ZOMBIE", "PIGZOMBIE", "ZOMBIEPIGMAN", "ZOMBIEPIGMEN", "ZOMBIE_PIGMAN", "ZOMBIE_PIGMEN", "ZOMBIE_PIGMAN");
    public static SentinelTarget SILVERFISH = new SentinelTarget(new  EntityType[]{EntityType.SILVERFISH}, "SILVERFISH", "SILVER_FISH", "SILVERFISHE", "SILVER_FISHE");
    public static SentinelTarget BATS = new SentinelTarget(new  EntityType[]{EntityType.BAT}, "BAT");
    public static SentinelTarget BLAZES = new SentinelTarget(new  EntityType[]{EntityType.BLAZE}, "BLAZE");
    public static SentinelTarget GHASTS = new SentinelTarget(new  EntityType[]{EntityType.GHAST}, "GHAST");
    public static SentinelTarget GIANTS = new SentinelTarget(new  EntityType[]{EntityType.GIANT}, "GIANT");
    public static SentinelTarget SLIME = new SentinelTarget(new  EntityType[]{EntityType.SLIME}, "SLIME");
    public static SentinelTarget SPIDER = new SentinelTarget(new  EntityType[]{EntityType.SPIDER}, "SPIDER");
    public static SentinelTarget CAVE_SPIDERS = new SentinelTarget(new  EntityType[]{EntityType.CAVE_SPIDER}, "CAVE_SPIDER", "CAVESPIDER");
    public static SentinelTarget ENDERMEN = new SentinelTarget(new  EntityType[]{EntityType.ENDERMAN}, "ENDERMAN", "ENDER_MAN", "ENDERMEN", "ENDER_MEN");
    public static SentinelTarget ENDERMITES = new SentinelTarget(new  EntityType[]{EntityType.ENDERMITE}, "ENDERMITE", "ENDER_MITE");
    public static SentinelTarget WITHER = new SentinelTarget(new  EntityType[]{EntityType.WITHER}, "WITHER");
    public static SentinelTarget ENDERDRAGON = new SentinelTarget(new  EntityType[]{EntityType.ENDER_DRAGON}, "ENDERDRAGON", "ENDER_DRAGON");

    public static final Set<Material> SWORD_MATERIALS = new HashSet<>();
    public static final Set<Material> PICKAXE_MATERIALS = new HashSet<>();
    public static final Set<Material> AXE_MATERIALS = new HashSet<>();
    public static final Set<Material> HELMET_MATERIALS = new HashSet<>();
    public static final Set<Material> CHESTPLATE_MATERIALS = new HashSet<>();
    public static final Set<Material> LEGGINGS_MATERIALS = new HashSet<>();
    public static final Set<Material> BOOTS_MATERIALS = new HashSet<>();
    public static final Set<Material> BOW_MATERIALS = new HashSet<>();
    public static final Set<Material> POTION_MATERIALS = new HashSet<>();
    public static final Set<Material> SKULL_MATERIALS = new HashSet<>();

    public static final Map<Material, Double> WEAPON_DAMAGE_MULTIPLIERS = new HashMap<>();
    public static final Map<Material, Double> ARMOR_PROTECTION_MULTIPLIERS = new HashMap<>();

    public static final Material MATERIAL_SNOW_BALL, MATERIAL_NETHER_STAR, MATERIAL_BLAZE_ROD;

    public static final boolean v1_8, v1_9, v1_10, v1_11, v1_12, v1_13;

    static {
        String vers = Bukkit.getBukkitVersion(); // Returns in format like: 1.12.2-R0.1-SNAPSHOT
        v1_13 = vers.startsWith("1.13");
        v1_12 = vers.startsWith("1.12") || v1_13;
        v1_11 = vers.startsWith("1.11") || v1_12;
        v1_10 = vers.startsWith("1.10") || v1_11;
        v1_9 = vers.startsWith("1.9") || v1_10;
        v1_8 = vers.startsWith("1.8") || v1_9;
        if (v1_8 && !v1_9) {
            SentinelTarget PASSIVE_MOB = new SentinelTarget(new  EntityType[]{
                    EntityType.PIG, EntityType.OCELOT, EntityType.COW, EntityType.RABBIT, EntityType.SHEEP, EntityType.CHICKEN, EntityType.MUSHROOM_COW,
                    EntityType.HORSE, EntityType.IRON_GOLEM, EntityType.SQUID, EntityType.VILLAGER, EntityType.WOLF, EntityType.SNOWMAN

            }, "PASSIVE_MOB", "PASSIVEMOB", "GOODMOB", "GOOD_MOB", "FRIENDLYMOB", "FRIENDLY_MOB");
            SentinelTarget MOBS = new SentinelTarget(new  EntityType[]{EntityType.GUARDIAN, EntityType.CREEPER, EntityType.SKELETON, EntityType.ZOMBIE,
                    EntityType.MAGMA_CUBE, EntityType.PIG_ZOMBIE, EntityType.SILVERFISH, EntityType.BAT, EntityType.BLAZE,
                    EntityType.GHAST, EntityType.GIANT, EntityType.SLIME, EntityType.SPIDER, EntityType.CAVE_SPIDER, EntityType.ENDERMAN,
                    EntityType.ENDERMITE, EntityType.WITHER, EntityType.ENDER_DRAGON, EntityType.WITCH,
                    EntityType.PIG, EntityType.OCELOT, EntityType.COW, EntityType.RABBIT, EntityType.SHEEP, EntityType.CHICKEN, EntityType.MUSHROOM_COW,
                    EntityType.HORSE, EntityType.IRON_GOLEM, EntityType.SQUID, EntityType.VILLAGER, EntityType.WOLF, EntityType.SNOWMAN,
            }, "MOB");
            SentinelTarget MONSTERS = new SentinelTarget(new  EntityType[]{EntityType.GUARDIAN, EntityType.CREEPER, EntityType.SKELETON, EntityType.ZOMBIE,
                    EntityType.MAGMA_CUBE, EntityType.PIG_ZOMBIE, EntityType.SILVERFISH, EntityType.BAT, EntityType.BLAZE,
                    EntityType.GHAST, EntityType.GIANT, EntityType.SLIME, EntityType.SPIDER, EntityType.CAVE_SPIDER, EntityType.ENDERMAN,
                    EntityType.ENDERMITE, EntityType.WITHER, EntityType.ENDER_DRAGON, EntityType.WITCH
            }, "MONSTER");
        }
        if (v1_9) {
            SentinelTarget SHULKERS = new SentinelTarget(new  EntityType[]{EntityType.SHULKER}, "SHULKER");
        }
        if (v1_9 && !v1_10) {
            SentinelTarget PASSIVE_MOB = new SentinelTarget(new  EntityType[]{
                    EntityType.PIG, EntityType.OCELOT, EntityType.COW, EntityType.RABBIT, EntityType.SHEEP, EntityType.CHICKEN, EntityType.MUSHROOM_COW,
                    EntityType.HORSE, EntityType.IRON_GOLEM, EntityType.SQUID, EntityType.VILLAGER, EntityType.WOLF, EntityType.SNOWMAN

            }, "PASSIVE_MOB", "PASSIVEMOB", "GOODMOB", "GOOD_MOB", "FRIENDLYMOB", "FRIENDLY_MOB");
            SentinelTarget MOBS = new SentinelTarget(new  EntityType[]{EntityType.GUARDIAN, EntityType.SHULKER, EntityType.CREEPER, EntityType.SKELETON, EntityType.ZOMBIE,
                    EntityType.MAGMA_CUBE, EntityType.PIG_ZOMBIE, EntityType.SILVERFISH, EntityType.BAT, EntityType.BLAZE,
                    EntityType.GHAST, EntityType.GIANT, EntityType.SLIME, EntityType.SPIDER, EntityType.CAVE_SPIDER, EntityType.ENDERMAN,
                    EntityType.ENDERMITE, EntityType.WITHER, EntityType.ENDER_DRAGON, EntityType.WITCH,
                    EntityType.PIG, EntityType.OCELOT, EntityType.COW, EntityType.RABBIT, EntityType.SHEEP, EntityType.CHICKEN, EntityType.MUSHROOM_COW,
                    EntityType.HORSE, EntityType.IRON_GOLEM, EntityType.SQUID, EntityType.VILLAGER, EntityType.WOLF, EntityType.SNOWMAN,
            }, "MOB");
            SentinelTarget MONSTERS = new SentinelTarget(new  EntityType[]{EntityType.GUARDIAN, EntityType.SHULKER, EntityType.CREEPER, EntityType.SKELETON, EntityType.ZOMBIE,
                    EntityType.MAGMA_CUBE, EntityType.PIG_ZOMBIE, EntityType.SILVERFISH, EntityType.BAT, EntityType.BLAZE,
                    EntityType.GHAST, EntityType.GIANT, EntityType.SLIME, EntityType.SPIDER, EntityType.CAVE_SPIDER, EntityType.ENDERMAN,
                    EntityType.ENDERMITE, EntityType.WITHER, EntityType.ENDER_DRAGON, EntityType.WITCH
            }, "MONSTER");
        }
        if (v1_10) {
            SentinelTarget POLAR_BEARS = new SentinelTarget(new EntityType[]{EntityType.POLAR_BEAR}, "POLARBEAR", "POLAR_BEAR");
        }
        if (v1_10 && !v1_11) {
            SentinelTarget PASSIVE_MOB = new SentinelTarget(new  EntityType[]{
                    EntityType.PIG, EntityType.OCELOT, EntityType.COW, EntityType.RABBIT, EntityType.SHEEP, EntityType.CHICKEN, EntityType.MUSHROOM_COW,
                    EntityType.HORSE, EntityType.IRON_GOLEM, EntityType.SQUID, EntityType.VILLAGER, EntityType.WOLF, EntityType.SNOWMAN, EntityType.POLAR_BEAR,

            }, "PASSIVE_MOB", "PASSIVEMOB", "GOODMOB", "GOOD_MOB", "FRIENDLYMOB", "FRIENDLY_MOB");
            SentinelTarget MOBS = new SentinelTarget(new  EntityType[]{EntityType.GUARDIAN, EntityType.SHULKER, EntityType.CREEPER, EntityType.SKELETON, EntityType.ZOMBIE,
                    EntityType.MAGMA_CUBE, EntityType.PIG_ZOMBIE, EntityType.SILVERFISH, EntityType.BAT, EntityType.BLAZE,
                    EntityType.GHAST, EntityType.GIANT, EntityType.SLIME, EntityType.SPIDER, EntityType.CAVE_SPIDER, EntityType.ENDERMAN,
                    EntityType.ENDERMITE, EntityType.WITHER, EntityType.ENDER_DRAGON, EntityType.WITCH,
                    EntityType.PIG, EntityType.OCELOT, EntityType.COW, EntityType.RABBIT, EntityType.SHEEP, EntityType.CHICKEN, EntityType.MUSHROOM_COW,
                    EntityType.HORSE, EntityType.IRON_GOLEM, EntityType.SQUID, EntityType.VILLAGER, EntityType.WOLF, EntityType.SNOWMAN, EntityType.POLAR_BEAR,
            }, "MOB");
            SentinelTarget MONSTERS = new SentinelTarget(new  EntityType[]{EntityType.GUARDIAN, EntityType.SHULKER, EntityType.CREEPER, EntityType.SKELETON, EntityType.ZOMBIE,
                    EntityType.MAGMA_CUBE, EntityType.PIG_ZOMBIE, EntityType.SILVERFISH, EntityType.BAT, EntityType.BLAZE,
                    EntityType.GHAST, EntityType.GIANT, EntityType.SLIME, EntityType.SPIDER, EntityType.CAVE_SPIDER, EntityType.ENDERMAN,
                    EntityType.ENDERMITE, EntityType.WITHER, EntityType.ENDER_DRAGON, EntityType.WITCH
            }, "MONSTER");
        }
        if (v1_11) {
            SentinelTarget VEXES = new SentinelTarget(new EntityType[]{EntityType.VEX}, "VEX", "VEXE");
            SentinelTarget DONKEYS = new SentinelTarget(new EntityType[]{EntityType.DONKEY}, "DONKEY");
            SentinelTarget LLAMAS = new SentinelTarget(new EntityType[]{EntityType.LLAMA}, "LLAMA");
            SentinelTarget MULES = new SentinelTarget(new EntityType[]{EntityType.MULE}, "MULE");
            SentinelTarget HUSKS = new SentinelTarget(new EntityType[]{EntityType.HUSK}, "HUSK");
            SentinelTarget ELDER_GUARDIANS = new SentinelTarget(new EntityType[]{EntityType.ELDER_GUARDIAN}, "ELDER_GUARDIAN", "ELDERGUARDIAN");
            SentinelTarget EVOKERS = new SentinelTarget(new EntityType[]{EntityType.EVOKER}, "EVOKER");
            SentinelTarget SKELETON_HORSES = new SentinelTarget(new EntityType[]{EntityType.SKELETON_HORSE}, "SKELETON_HORSE", "SKELETONHORSE");
            SentinelTarget STRAYS = new SentinelTarget(new EntityType[]{EntityType.STRAY}, "STRAY");
            SentinelTarget ZOMBIE_VILLAGERS = new SentinelTarget(new EntityType[]{EntityType.ZOMBIE_VILLAGER}, "ZOMBIE_VILLAGER", "ZOMBIEVILLAGER");
            SentinelTarget ZOMBIE_HORSES = new SentinelTarget(new EntityType[]{EntityType.ZOMBIE_HORSE}, "ZOMBIE_HORSE", "ZOMBIEHORSE");
            SentinelTarget WITHER_SKELETONS = new SentinelTarget(new EntityType[]{EntityType.WITHER_SKELETON}, "WITHER_SKELETON", "WITHERSKELETON");
            SentinelTarget VINDICATORS = new SentinelTarget(new EntityType[]{EntityType.VINDICATOR}, "VINDICATOR");
        }
        if (v1_11 && !v1_12) {
            SentinelTarget PASSIVE_MOB = new SentinelTarget(new  EntityType[]{
                    EntityType.PIG, EntityType.OCELOT, EntityType.COW, EntityType.RABBIT, EntityType.SHEEP, EntityType.CHICKEN, EntityType.MUSHROOM_COW,
                    EntityType.HORSE, EntityType.IRON_GOLEM, EntityType.SQUID, EntityType.VILLAGER, EntityType.WOLF, EntityType.SNOWMAN, EntityType.POLAR_BEAR,
                    EntityType.DONKEY, EntityType.LLAMA, EntityType.MULE, EntityType.ZOMBIE_HORSE, EntityType.SKELETON_HORSE
            }, "PASSIVE_MOB", "PASSIVEMOB", "GOODMOB", "GOOD_MOB", "FRIENDLYMOB", "FRIENDLY_MOB");
            SentinelTarget MOBS = new SentinelTarget(new  EntityType[]{EntityType.GUARDIAN, EntityType.SHULKER, EntityType.CREEPER, EntityType.SKELETON, EntityType.ZOMBIE,
                    EntityType.MAGMA_CUBE, EntityType.PIG_ZOMBIE, EntityType.SILVERFISH, EntityType.BAT, EntityType.BLAZE,
                    EntityType.GHAST, EntityType.GIANT, EntityType.SLIME, EntityType.SPIDER, EntityType.CAVE_SPIDER, EntityType.ENDERMAN,
                    EntityType.ENDERMITE, EntityType.WITHER, EntityType.ENDER_DRAGON, EntityType.WITCH,
                    EntityType.PIG, EntityType.OCELOT, EntityType.COW, EntityType.RABBIT, EntityType.SHEEP, EntityType.CHICKEN, EntityType.MUSHROOM_COW,
                    EntityType.HORSE, EntityType.IRON_GOLEM, EntityType.SQUID, EntityType.VILLAGER, EntityType.WOLF, EntityType.SNOWMAN, EntityType.POLAR_BEAR,
                    EntityType.DONKEY, EntityType.LLAMA, EntityType.MULE, EntityType.ZOMBIE_HORSE, EntityType.SKELETON_HORSE,
                    EntityType.VEX, EntityType.HUSK, EntityType.ELDER_GUARDIAN, EntityType.EVOKER, EntityType.STRAY, EntityType.ZOMBIE_VILLAGER,
                    EntityType.WITHER_SKELETON, EntityType.VINDICATOR
            }, "MOB");
            SentinelTarget MONSTERS = new SentinelTarget(new  EntityType[]{EntityType.GUARDIAN, EntityType.SHULKER, EntityType.CREEPER, EntityType.SKELETON, EntityType.ZOMBIE,
                    EntityType.MAGMA_CUBE, EntityType.PIG_ZOMBIE, EntityType.SILVERFISH, EntityType.BAT, EntityType.BLAZE,
                    EntityType.GHAST, EntityType.GIANT, EntityType.SLIME, EntityType.SPIDER, EntityType.CAVE_SPIDER, EntityType.ENDERMAN,
                    EntityType.ENDERMITE, EntityType.WITHER, EntityType.ENDER_DRAGON, EntityType.WITCH,
                    EntityType.VEX, EntityType.HUSK, EntityType.ELDER_GUARDIAN, EntityType.EVOKER, EntityType.STRAY, EntityType.ZOMBIE_VILLAGER,
                    EntityType.WITHER_SKELETON, EntityType.VINDICATOR
            }, "MONSTER");
        }
        if (v1_12) {
            SentinelTarget PARROTS = new SentinelTarget(new EntityType[]{EntityType.PARROT}, "PARROT");
            SentinelTarget ILLUSIONERS = new SentinelTarget(new EntityType[]{EntityType.ILLUSIONER}, "ILLUSIONER");
        }
        if (v1_12 && !v1_13) {
            SentinelTarget PASSIVE_MOB = new SentinelTarget(new  EntityType[]{
                    EntityType.PIG, EntityType.OCELOT, EntityType.COW, EntityType.RABBIT, EntityType.SHEEP, EntityType.CHICKEN, EntityType.MUSHROOM_COW,
                    EntityType.HORSE, EntityType.IRON_GOLEM, EntityType.SQUID, EntityType.VILLAGER, EntityType.WOLF, EntityType.SNOWMAN, EntityType.POLAR_BEAR,
                    EntityType.DONKEY, EntityType.LLAMA, EntityType.MULE, EntityType.ZOMBIE_HORSE, EntityType.SKELETON_HORSE, EntityType.PARROT
            }, "PASSIVE_MOB", "PASSIVEMOB", "GOODMOB", "GOOD_MOB", "FRIENDLYMOB", "FRIENDLY_MOB");
            SentinelTarget MOBS = new SentinelTarget(new  EntityType[]{EntityType.GUARDIAN, EntityType.SHULKER, EntityType.CREEPER, EntityType.SKELETON, EntityType.ZOMBIE,
                    EntityType.MAGMA_CUBE, EntityType.PIG_ZOMBIE, EntityType.SILVERFISH, EntityType.BAT, EntityType.BLAZE,
                    EntityType.GHAST, EntityType.GIANT, EntityType.SLIME, EntityType.SPIDER, EntityType.CAVE_SPIDER, EntityType.ENDERMAN,
                    EntityType.ENDERMITE, EntityType.WITHER, EntityType.ENDER_DRAGON, EntityType.WITCH,
                    EntityType.PIG, EntityType.OCELOT, EntityType.COW, EntityType.RABBIT, EntityType.SHEEP, EntityType.CHICKEN, EntityType.MUSHROOM_COW,
                    EntityType.HORSE, EntityType.IRON_GOLEM, EntityType.SQUID, EntityType.VILLAGER, EntityType.WOLF, EntityType.SNOWMAN, EntityType.POLAR_BEAR,
                    EntityType.DONKEY, EntityType.LLAMA, EntityType.MULE, EntityType.ZOMBIE_HORSE, EntityType.SKELETON_HORSE,
                    EntityType.VEX, EntityType.HUSK, EntityType.ELDER_GUARDIAN, EntityType.EVOKER, EntityType.STRAY, EntityType.ZOMBIE_VILLAGER,
                    EntityType.WITHER_SKELETON, EntityType.VINDICATOR, EntityType.PARROT, EntityType.ILLUSIONER
            }, "MOB");
            SentinelTarget MONSTERS = new SentinelTarget(new  EntityType[]{EntityType.GUARDIAN, EntityType.SHULKER, EntityType.CREEPER, EntityType.SKELETON, EntityType.ZOMBIE,
                    EntityType.MAGMA_CUBE, EntityType.PIG_ZOMBIE, EntityType.SILVERFISH, EntityType.BAT, EntityType.BLAZE,
                    EntityType.GHAST, EntityType.GIANT, EntityType.SLIME, EntityType.SPIDER, EntityType.CAVE_SPIDER, EntityType.ENDERMAN,
                    EntityType.ENDERMITE, EntityType.WITHER, EntityType.ENDER_DRAGON, EntityType.WITCH,
                    EntityType.VEX, EntityType.HUSK, EntityType.ELDER_GUARDIAN, EntityType.EVOKER, EntityType.STRAY, EntityType.ZOMBIE_VILLAGER,
                    EntityType.WITHER_SKELETON, EntityType.VINDICATOR, EntityType.ILLUSIONER
            }, "MONSTER");
        }
        if (v1_13) {
            SentinelTarget DOLPHIN = new SentinelTarget(new EntityType[]{EntityType.DOLPHIN}, "DOLPHIN");
            SentinelTarget DROWNED = new SentinelTarget(new EntityType[]{EntityType.DROWNED}, "DROWNED");
            SentinelTarget COD = new SentinelTarget(new EntityType[]{EntityType.COD}, "COD");
            SentinelTarget SALMON = new SentinelTarget(new EntityType[]{EntityType.SALMON}, "SALMON");
            SentinelTarget PUFFERFISH = new SentinelTarget(new EntityType[]{EntityType.PUFFERFISH}, "PUFFERFISH");
            SentinelTarget TROPICAL_FISH = new SentinelTarget(new EntityType[]{EntityType.TROPICAL_FISH}, "TROPICAL_FISH", "TROPICALFISH");
            SentinelTarget PHANTOM = new SentinelTarget(new EntityType[]{EntityType.PHANTOM}, "PHANTOM");
            SentinelTarget TURTLE = new SentinelTarget(new EntityType[]{EntityType.TURTLE}, "TURTLE");
        }
        if (v1_13) { // && !v1_14
            SentinelTarget PASSIVE_MOB = new SentinelTarget(new  EntityType[]{
                    EntityType.PIG, EntityType.OCELOT, EntityType.COW, EntityType.RABBIT, EntityType.SHEEP, EntityType.CHICKEN, EntityType.MUSHROOM_COW,
                    EntityType.HORSE, EntityType.IRON_GOLEM, EntityType.SQUID, EntityType.VILLAGER, EntityType.WOLF, EntityType.SNOWMAN, EntityType.POLAR_BEAR,
                    EntityType.DONKEY, EntityType.LLAMA, EntityType.MULE, EntityType.ZOMBIE_HORSE, EntityType.SKELETON_HORSE, EntityType.PARROT,
                    EntityType.DOLPHIN, EntityType.COD, EntityType.SALMON, EntityType.PUFFERFISH, EntityType.TROPICAL_FISH, EntityType.TURTLE
            }, "PASSIVE_MOB", "PASSIVEMOB", "GOODMOB", "GOOD_MOB", "FRIENDLYMOB", "FRIENDLY_MOB");
            SentinelTarget MOBS = new SentinelTarget(new  EntityType[]{EntityType.GUARDIAN, EntityType.SHULKER, EntityType.CREEPER, EntityType.SKELETON, EntityType.ZOMBIE,
                    EntityType.MAGMA_CUBE, EntityType.PIG_ZOMBIE, EntityType.SILVERFISH, EntityType.BAT, EntityType.BLAZE,
                    EntityType.GHAST, EntityType.GIANT, EntityType.SLIME, EntityType.SPIDER, EntityType.CAVE_SPIDER, EntityType.ENDERMAN,
                    EntityType.ENDERMITE, EntityType.WITHER, EntityType.ENDER_DRAGON, EntityType.WITCH,
                    EntityType.PIG, EntityType.OCELOT, EntityType.COW, EntityType.RABBIT, EntityType.SHEEP, EntityType.CHICKEN, EntityType.MUSHROOM_COW,
                    EntityType.HORSE, EntityType.IRON_GOLEM, EntityType.SQUID, EntityType.VILLAGER, EntityType.WOLF, EntityType.SNOWMAN, EntityType.POLAR_BEAR,
                    EntityType.DONKEY, EntityType.LLAMA, EntityType.MULE, EntityType.ZOMBIE_HORSE, EntityType.SKELETON_HORSE,
                    EntityType.VEX, EntityType.HUSK, EntityType.ELDER_GUARDIAN, EntityType.EVOKER, EntityType.STRAY, EntityType.ZOMBIE_VILLAGER,
                    EntityType.WITHER_SKELETON, EntityType.VINDICATOR, EntityType.PARROT, EntityType.ILLUSIONER, EntityType.DROWNED, EntityType.PHANTOM,
                    EntityType.DOLPHIN, EntityType.COD, EntityType.SALMON, EntityType.PUFFERFISH, EntityType.TROPICAL_FISH, EntityType.TURTLE
            }, "MOB");
            SentinelTarget MONSTERS = new SentinelTarget(new  EntityType[]{EntityType.GUARDIAN, EntityType.SHULKER, EntityType.CREEPER, EntityType.SKELETON, EntityType.ZOMBIE,
                    EntityType.MAGMA_CUBE, EntityType.PIG_ZOMBIE, EntityType.SILVERFISH, EntityType.BAT, EntityType.BLAZE,
                    EntityType.GHAST, EntityType.GIANT, EntityType.SLIME, EntityType.SPIDER, EntityType.CAVE_SPIDER, EntityType.ENDERMAN,
                    EntityType.ENDERMITE, EntityType.WITHER, EntityType.ENDER_DRAGON, EntityType.WITCH,
                    EntityType.VEX, EntityType.HUSK, EntityType.ELDER_GUARDIAN, EntityType.EVOKER, EntityType.STRAY, EntityType.ZOMBIE_VILLAGER,
                    EntityType.WITHER_SKELETON, EntityType.VINDICATOR, EntityType.ILLUSIONER, EntityType.DROWNED, EntityType.PHANTOM
            }, "MONSTER");
        }
        // == End Entities ==
        // == Begin Materials ==
        if (v1_13) {
            // Sword
            addAllMaterials(SWORD_MATERIALS, "DIAMOND_SWORD", "IRON_SWORD", "STONE_SWORD", "GOLDEN_SWORD", "WOODEN_SWORD");
            WEAPON_DAMAGE_MULTIPLIERS.put(getMaterial("DIAMOND_SWORD"), 7.0);
            WEAPON_DAMAGE_MULTIPLIERS.put(getMaterial("IRON_SWORD"), 6.0);
            WEAPON_DAMAGE_MULTIPLIERS.put(getMaterial("STONE_SWORD"), 5.0);
            WEAPON_DAMAGE_MULTIPLIERS.put(getMaterial("GOLDEN_SWORD"), 4.0);
            WEAPON_DAMAGE_MULTIPLIERS.put(getMaterial("WOODEN_SWORD"), 4.0);
            // Pickaxe
            addAllMaterials(PICKAXE_MATERIALS, "DIAMOND_PICKAXE", "IRON_PICKAXE", "STONE_PICKAXE", "GOLDEN_PICKAXE", "WOODEN_PICKAXE");
            allMaterialsTo(WEAPON_DAMAGE_MULTIPLIERS, PICKAXE_MATERIALS, 2.0);
            // Axe
            addAllMaterials(AXE_MATERIALS, "DIAMOND_AXE", "IRON_AXE", "STONE_AXE", "GOLDEN_AXE", "WOODEN_AXE");
            allMaterialsTo(WEAPON_DAMAGE_MULTIPLIERS, AXE_MATERIALS, 3.0);
            // Bow
            BOW_MATERIALS.add(getMaterial("BOW"));
            // Helmet
            addAllMaterials(HELMET_MATERIALS, "DIAMOND_HELMET", "GOLDEN_HELMET", "IRON_HELMET", "LEATHER_HELMET", "CHAINMAIL_HELMET");
            ARMOR_PROTECTION_MULTIPLIERS.put(getMaterial("DIAMOND_HELMET"), 0.12);
            ARMOR_PROTECTION_MULTIPLIERS.put(getMaterial("GOLDEN_HELMET"), 0.08);
            ARMOR_PROTECTION_MULTIPLIERS.put(getMaterial("IRON_HELMET"), 0.08);
            ARMOR_PROTECTION_MULTIPLIERS.put(getMaterial("LEATHER_HELMET"), 0.04);
            ARMOR_PROTECTION_MULTIPLIERS.put(getMaterial("CHAINMAIL_HELMET"), 0.08);
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
            MATERIAL_NETHER_STAR = getMaterial("NETHER_STAR");
            MATERIAL_BLAZE_ROD = getMaterial("BLAZE_ROD");
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
            MATERIAL_NETHER_STAR = getMaterial("NETHER_STAR");
            MATERIAL_BLAZE_ROD = getMaterial("BLAZE_ROD");
        }
    }

    public static Material getMaterial(String name) {
        try {
            return Material.valueOf(name);
        }
        catch (IllegalArgumentException ex) {
            SentinelPlugin.instance.getLogger().warning("Sentinel loader failed to handle material name '" + name + "', that material will not function (REPORT THIS ERROR!)");
            return Material.valueOf("STICK");
        }
    }

    public static void addAllMaterials(Set<Material> set, String... matNames) {
        for (String mat : matNames) {
            set.add(getMaterial(mat));
        }
    }

    public static void allMaterialsTo(Map<Material, Double> map, Set<Material> set, Double val) {
        for (Material mat : set) {
            map.put(mat, val);
        }
    }

    public static HashSet<SentinelTarget> forEntityType(EntityType type) {
        return SentinelPlugin.entityToTargets.get(type);
    }

    public static SentinelTarget forName(String name) {
        return SentinelPlugin.targetOptions.get(name.toUpperCase());
    }

    private String[] names;

    public String name() {
        return names[0];
    }

    SentinelTarget(EntityType[] types, String... names) {
        this.names = names;
        for (String name : names) {
            SentinelPlugin.targetOptions.put(name, this);
            SentinelPlugin.targetOptions.put(name + "S", this);
        }
        for (EntityType type : types) {
            SentinelPlugin.entityToTargets.get(type).add(this);
        }
    }
}
