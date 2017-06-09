package org.mcmonkey.sentinel;

import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;

import java.util.HashSet;

public class SentinelTarget {
    public static SentinelTarget NPCS = new SentinelTarget(new EntityType[]{}, "NPC");
    public static SentinelTarget OWNER = new SentinelTarget(new  EntityType[]{}, "OWNER");
    public static SentinelTarget PASSIVE_MOB = new SentinelTarget(new  EntityType[]{
            EntityType.PIG, EntityType.OCELOT, EntityType.COW, EntityType.RABBIT, EntityType.SHEEP, EntityType.CHICKEN, EntityType.MUSHROOM_COW,
            EntityType.HORSE, EntityType.IRON_GOLEM, EntityType.SQUID, EntityType.VILLAGER, EntityType.WOLF, EntityType.SNOWMAN, EntityType.POLAR_BEAR,
            EntityType.DONKEY, EntityType.LLAMA, EntityType.MULE, EntityType.ZOMBIE_HORSE, EntityType.SKELETON_HORSE
    }, "PASSIVE_MOB", "PASSIVEMOB", "GOODMOB", "GOOD_MOB", "FRIENDLYMOB", "FRIENDLY_MOB");
    public static SentinelTarget MOBS = new SentinelTarget(new  EntityType[]{EntityType.GUARDIAN, EntityType.SHULKER, EntityType.CREEPER, EntityType.SKELETON, EntityType.ZOMBIE,
            EntityType.MAGMA_CUBE, EntityType.PIG_ZOMBIE, EntityType.SILVERFISH, EntityType.BAT, EntityType.BLAZE,
            EntityType.GHAST, EntityType.GIANT, EntityType.SLIME, EntityType.SPIDER, EntityType.CAVE_SPIDER, EntityType.ENDERMAN,
            EntityType.ENDERMITE, EntityType.WITHER, EntityType.ENDER_DRAGON, EntityType.WITCH,
            EntityType.PIG, EntityType.OCELOT, EntityType.COW, EntityType.RABBIT, EntityType.SHEEP, EntityType.CHICKEN, EntityType.MUSHROOM_COW,
            EntityType.HORSE, EntityType.IRON_GOLEM, EntityType.SQUID, EntityType.VILLAGER, EntityType.WOLF, EntityType.SNOWMAN, EntityType.POLAR_BEAR,
            EntityType.DONKEY, EntityType.LLAMA, EntityType.MULE, EntityType.ZOMBIE_HORSE, EntityType.SKELETON_HORSE,
            EntityType.VEX, EntityType.HUSK, EntityType.ELDER_GUARDIAN, EntityType.EVOKER, EntityType.STRAY, EntityType.ZOMBIE_VILLAGER,
            EntityType.WITHER_SKELETON, EntityType.VINDICATOR
    }, "MOB");
    public static SentinelTarget MONSTERS = new SentinelTarget(new  EntityType[]{EntityType.GUARDIAN, EntityType.SHULKER, EntityType.CREEPER, EntityType.SKELETON, EntityType.ZOMBIE,
            EntityType.MAGMA_CUBE, EntityType.PIG_ZOMBIE, EntityType.SILVERFISH, EntityType.BAT, EntityType.BLAZE,
            EntityType.GHAST, EntityType.GIANT, EntityType.SLIME, EntityType.SPIDER, EntityType.CAVE_SPIDER, EntityType.ENDERMAN,
            EntityType.ENDERMITE, EntityType.WITHER, EntityType.ENDER_DRAGON, EntityType.WITCH,
            EntityType.VEX, EntityType.HUSK, EntityType.ELDER_GUARDIAN, EntityType.EVOKER, EntityType.STRAY, EntityType.ZOMBIE_VILLAGER,
            EntityType.WITHER_SKELETON, EntityType.VINDICATOR
    }, "MONSTER");
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
    public static SentinelTarget SHULKERS = new SentinelTarget(new  EntityType[]{EntityType.SHULKER}, "SHULKER");
    public static SentinelTarget CREERERS = new SentinelTarget(new  EntityType[]{EntityType.CREEPER}, "CREEPER");
    public static SentinelTarget SKELETONS = new SentinelTarget(new  EntityType[]{EntityType.SKELETON}, "SKELETON");
    public static SentinelTarget POLAR_BEARS = new SentinelTarget(new  EntityType[]{EntityType.POLAR_BEAR}, "POLARBEAR", "POLAR_BEAR");
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
    static {
        String vers = Bukkit.getServer().getVersion();
        boolean v1_12 = vers.contains("1.12");
        boolean v1_11 = vers.contains("1.11") || v1_12;
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
        if (v1_12) {
            SentinelTarget PARROTS = new SentinelTarget(new EntityType[]{EntityType.PARROT}, "PARROT");
            SentinelTarget ILLUSIONERS = new SentinelTarget(new EntityType[]{EntityType.ILLUSIONER}, "ILLUSIONER");
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
