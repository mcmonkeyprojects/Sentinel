package org.mcmonkey.sentinel;

import org.bukkit.entity.EntityType;

import java.util.HashSet;

public enum SentinelTarget {
    NPCS(new EntityType[]{}, "NPC"),
    OWNER(new EntityType[]{}, "OWNER"),
    PASSIVE_MOB(new EntityType[]{
            EntityType.PIG, EntityType.OCELOT, EntityType.COW, EntityType.RABBIT, EntityType.SHEEP, EntityType.CHICKEN, EntityType.MUSHROOM_COW,
            EntityType.HORSE, EntityType.IRON_GOLEM, EntityType.SQUID, EntityType.VILLAGER, EntityType.WOLF, EntityType.SNOWMAN, EntityType.POLAR_BEAR,
            EntityType.DONKEY, EntityType.LLAMA, EntityType.MULE, EntityType.ZOMBIE_HORSE, EntityType.SKELETON_HORSE
            }, "PASSIVE_MOB", "PASSIVEMOB", "GOODMOB", "GOOD_MOB", "FRIENDLYMOB", "FRIENDLY_MOB"),
    MOBS(new EntityType[]{EntityType.GUARDIAN, EntityType.SHULKER, EntityType.CREEPER, EntityType.SKELETON, EntityType.ZOMBIE,
            EntityType.MAGMA_CUBE, EntityType.PIG_ZOMBIE, EntityType.SILVERFISH, EntityType.BAT, EntityType.BLAZE,
            EntityType.GHAST, EntityType.GIANT, EntityType.SLIME, EntityType.SPIDER, EntityType.CAVE_SPIDER, EntityType.ENDERMAN,
            EntityType.ENDERMITE, EntityType.WITHER, EntityType.ENDER_DRAGON, EntityType.WITCH,
            EntityType.PIG, EntityType.OCELOT, EntityType.COW, EntityType.RABBIT, EntityType.SHEEP, EntityType.CHICKEN, EntityType.MUSHROOM_COW,
            EntityType.HORSE, EntityType.IRON_GOLEM, EntityType.SQUID, EntityType.VILLAGER, EntityType.WOLF, EntityType.SNOWMAN, EntityType.POLAR_BEAR,
            EntityType.DONKEY, EntityType.LLAMA, EntityType.MULE, EntityType.ZOMBIE_HORSE, EntityType.SKELETON_HORSE,
            EntityType.VEX, EntityType.HUSK, EntityType.ELDER_GUARDIAN, EntityType.EVOKER, EntityType.STRAY, EntityType.ZOMBIE_VILLAGER,
            EntityType.WITHER_SKELETON, EntityType.VINDICATOR
            }, "MOB"),
    MONSTERS(new EntityType[]{EntityType.GUARDIAN, EntityType.SHULKER, EntityType.CREEPER, EntityType.SKELETON, EntityType.ZOMBIE,
            EntityType.MAGMA_CUBE, EntityType.PIG_ZOMBIE, EntityType.SILVERFISH, EntityType.BAT, EntityType.BLAZE,
            EntityType.GHAST, EntityType.GIANT, EntityType.SLIME, EntityType.SPIDER, EntityType.CAVE_SPIDER, EntityType.ENDERMAN,
            EntityType.ENDERMITE, EntityType.WITHER, EntityType.ENDER_DRAGON, EntityType.WITCH,
            EntityType.VEX, EntityType.HUSK, EntityType.ELDER_GUARDIAN, EntityType.EVOKER, EntityType.STRAY, EntityType.ZOMBIE_VILLAGER,
            EntityType.WITHER_SKELETON, EntityType.VINDICATOR
            }, "MONSTER"),
    PLAYERS(new EntityType[]{EntityType.PLAYER}, "PLAYER"),
    PIGS(new EntityType[]{EntityType.PIG}, "PIG"),
    OCELOTS(new EntityType[]{EntityType.OCELOT}, "OCELOT", "CAT"),
    COWS(new EntityType[]{EntityType.COW}, "COW"),
    RABBITS(new EntityType[]{EntityType.RABBIT}, "RABBIT", "BUNNY", "BUNNIE"),
    SHEEP(new EntityType[]{EntityType.SHEEP}, "SHEEP"),
    CHICKENS(new EntityType[]{EntityType.CHICKEN}, "CHICKEN", "DUCK"),
    HORSES(new EntityType[]{EntityType.HORSE}, "HORSE"),
    MUSHROOM_COW(new EntityType[]{EntityType.MUSHROOM_COW}, "MUSHROOM_COW", "MUSHROOMCOW", "MOOSHROOM"),
    IRON_GOLEMS(new EntityType[]{EntityType.IRON_GOLEM}, "IRON_GOLEM", "IRONGOLEM"),
    SQUIDS(new EntityType[]{EntityType.SQUID}, "SQUID"),
    VILLAGER(new EntityType[]{EntityType.VILLAGER}, "VILLAGER"),
    WOLF(new EntityType[]{EntityType.WOLF}, "WOLF"),
    SNOWMEN(new EntityType[]{EntityType.SNOWMAN}, "SNOWMAN", "SNOWMEN"),
    WITCH(new EntityType[]{EntityType.WITCH}, "WITCH"),
    GUARDIANS(new EntityType[]{EntityType.GUARDIAN}, "GUARDIAN"),
    SHULKERS(new EntityType[]{EntityType.SHULKER}, "SHULKER"),
    CREERERS(new EntityType[]{EntityType.CREEPER}, "CREEPER"),
    SKELETONS(new EntityType[]{EntityType.SKELETON}, "SKELETON"),
    POLAR_BEARS(new EntityType[]{EntityType.POLAR_BEAR}, "POLARBEAR", "POLAR_BEAR"),
    ZOMBIES(new EntityType[]{EntityType.ZOMBIE}, "ZOMBIE"),
    MAGMA_CUBES(new EntityType[]{EntityType.MAGMA_CUBE}, "MAGMA_CUBE", "MAGMACUBE"),
    ZOMBIE_PIGMEN(new EntityType[]{EntityType.PIG_ZOMBIE}, "PIG_ZOMBIE", "PIGZOMBIE", "ZOMBIEPIGMAN", "ZOMBIEPIGMEN", "ZOMBIE_PIGMAN", "ZOMBIE_PIGMEN", "ZOMBIE_PIGMAN"),
    SILVERFISH(new EntityType[]{EntityType.SILVERFISH}, "SILVERFISH", "SILVER_FISH", "SILVERFISHE", "SILVER_FISHE"),
    BATS(new EntityType[]{EntityType.BAT}, "BAT"),
    BLAZES(new EntityType[]{EntityType.BLAZE}, "BLAZE"),
    GHASTS(new EntityType[]{EntityType.GHAST}, "GHAST"),
    GIANTS(new EntityType[]{EntityType.GIANT}, "GIANT"),
    SLIME(new EntityType[]{EntityType.SLIME}, "SLIME"),
    SPIDER(new EntityType[]{EntityType.SPIDER}, "SPIDER"),
    CAVE_SPIDERS(new EntityType[]{EntityType.CAVE_SPIDER}, "CAVE_SPIDER", "CAVESPIDER"),
    ENDERMEN(new EntityType[]{EntityType.ENDERMAN}, "ENDERMAN", "ENDER_MAN", "ENDERMEN", "ENDER_MEN"),
    ENDERMITES(new EntityType[]{EntityType.ENDERMITE}, "ENDERMITE", "ENDER_MITE"),
    WITHER(new EntityType[]{EntityType.WITHER}, "WITHER"),
    ENDERDRAGON(new EntityType[]{EntityType.ENDER_DRAGON}, "ENDERDRAGON", "ENDER_DRAGON"),
    // 1.11
    VEXES(new EntityType[]{EntityType.VEX}, "VEX", "VEXE"),
    DONKEYS(new EntityType[]{EntityType.DONKEY}, "DONKEY"),
    LLAMAS(new EntityType[]{EntityType.LLAMA}, "LLAMA"),
    MULES(new EntityType[]{EntityType.MULE}, "MULE"),
    HUSKS(new EntityType[]{EntityType.HUSK}, "HUSK"),
    ELDER_GUARDIANS(new EntityType[]{EntityType.ELDER_GUARDIAN}, "ELDER_GUARDIAN", "ELDERGUARDIAN"),
    EVOKERS(new EntityType[]{EntityType.EVOKER}, "EVOKER"),
    SKELETON_HORSES(new EntityType[]{EntityType.SKELETON_HORSE}, "SKELETON_HORSE", "SKELETONHORSE"),
    STRAYS(new EntityType[]{EntityType.STRAY}, "STRAY"),
    ZOMBIE_VILLAGERS(new EntityType[]{EntityType.ZOMBIE_VILLAGER}, "ZOMBIE_VILLAGER", "ZOMBIEVILLAGER"),
    ZOMBIE_HORSES(new EntityType[]{EntityType.ZOMBIE_HORSE}, "ZOMBIE_HORSE", "ZOMBIEHORSE"),
    WITHER_SKELETONS(new EntityType[]{EntityType.WITHER_SKELETON}, "WITHER_SKELETON", "WITHERSKELETON"),
    VINDICATORS(new EntityType[]{EntityType.VINDICATOR}, "VINDICATOR"),
    ;

    public static HashSet<SentinelTarget> forEntityType(EntityType type) {
        return SentinelPlugin.entityToTargets.get(type);
    }

    public static SentinelTarget forName(String name) {
        return SentinelPlugin.targetOptions.get(name.toUpperCase());
    }

    SentinelTarget(EntityType[] types, String... names) {
        for (String name : names) {
            SentinelPlugin.targetOptions.put(name, this);
            SentinelPlugin.targetOptions.put(name + "S", this);
        }
        for (EntityType type : types) {
            SentinelPlugin.entityToTargets.get(type).add(this);
        }
    }
}
