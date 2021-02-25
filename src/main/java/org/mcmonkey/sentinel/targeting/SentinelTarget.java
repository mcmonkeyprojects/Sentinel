package org.mcmonkey.sentinel.targeting;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.trait.trait.Owner;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.mcmonkey.sentinel.SentinelPlugin;
import org.mcmonkey.sentinel.SentinelTrait;

import java.util.*;

/**
 * Helper for target types.
 */
public class SentinelTarget {

    /**
     * All NPCs are targets.
     */
    public static SentinelTarget NPCS = new SentinelTarget(new EntityType[]{}, "NPC");

    /**
     * The NPC's owner is a target.
     */
    public static SentinelTarget OWNER = new SentinelTarget(new EntityType[]{}, "OWNER");

    /**
     * Player entity is a target.
     */
    public static SentinelTarget PLAYERS = new SentinelTarget(new EntityType[]{EntityType.PLAYER}, "PLAYER");

    /**
     * Entity types known to support native melee combat.
     */
    public static HashSet<EntityType> NATIVE_COMBAT_CAPABLE_TYPES = new HashSet<>(Arrays.asList(EntityType.ZOMBIE, EntityType.SKELETON));

    /**
     * Basic single-entity target types (that were added in Minecraft 1.8 or earlier).
     */
    public static SentinelTarget PIGS = new SentinelTarget(new EntityType[]{EntityType.PIG}, "PIG"),
            OCELOTS = new SentinelTarget(new EntityType[]{EntityType.OCELOT}, "OCELOT", "CAT"),
            COWS = new SentinelTarget(new EntityType[]{EntityType.COW}, "COW"),
            RABBITS = new SentinelTarget(new EntityType[]{EntityType.RABBIT}, "RABBIT", "BUNNY", "BUNNIE"),
            SHEEP = new SentinelTarget(new EntityType[]{EntityType.SHEEP}, "SHEEP"),
            CHICKENS = new SentinelTarget(new EntityType[]{EntityType.CHICKEN}, "CHICKEN", "DUCK"),
            HORSES = new SentinelTarget(new EntityType[]{EntityType.HORSE}, "HORSE"),
            MUSHROOM_COW = new SentinelTarget(new EntityType[]{EntityType.MUSHROOM_COW}, "MUSHROOM_COW", "MUSHROOMCOW", "MOOSHROOM"),
            IRON_GOLEMS = new SentinelTarget(new EntityType[]{EntityType.IRON_GOLEM}, "IRON_GOLEM", "IRONGOLEM"),
            SQUIDS = new SentinelTarget(new EntityType[]{EntityType.SQUID}, "SQUID"),
            VILLAGER = new SentinelTarget(new EntityType[]{EntityType.VILLAGER}, "VILLAGER"),
            WOLVES = new SentinelTarget(new EntityType[]{EntityType.WOLF}, "WOLF", "WOLVE"),
            SNOWMEN = new SentinelTarget(new EntityType[]{EntityType.SNOWMAN}, "SNOWMAN", "SNOWMEN"),
            WITCHES = new SentinelTarget(new EntityType[]{EntityType.WITCH}, "WITCH"),
            GUARDIANS = new SentinelTarget(new EntityType[]{EntityType.GUARDIAN}, "GUARDIAN"),
            CREERERS = new SentinelTarget(new EntityType[]{EntityType.CREEPER}, "CREEPER"),
            SKELETONS = new SentinelTarget(new EntityType[]{EntityType.SKELETON}, "SKELETON"),
            ZOMBIES = new SentinelTarget(new EntityType[]{EntityType.ZOMBIE}, "ZOMBIE"),
            MAGMA_CUBES = new SentinelTarget(new EntityType[]{EntityType.MAGMA_CUBE}, "MAGMA_CUBE", "MAGMACUBE"),
            SILVERFISH = new SentinelTarget(new EntityType[]{EntityType.SILVERFISH}, "SILVERFISH", "SILVER_FISH", "SILVERFISHE", "SILVER_FISHE"),
            BATS = new SentinelTarget(new EntityType[]{EntityType.BAT}, "BAT"),
            BLAZES = new SentinelTarget(new EntityType[]{EntityType.BLAZE}, "BLAZE"),
            GHASTS = new SentinelTarget(new EntityType[]{EntityType.GHAST}, "GHAST"),
            GIANTS = new SentinelTarget(new EntityType[]{EntityType.GIANT}, "GIANT"),
            SLIMES = new SentinelTarget(new EntityType[]{EntityType.SLIME}, "SLIME"),
            SPIDERS = new SentinelTarget(new EntityType[]{EntityType.SPIDER}, "SPIDER"),
            CAVE_SPIDERS = new SentinelTarget(new EntityType[]{EntityType.CAVE_SPIDER}, "CAVE_SPIDER", "CAVESPIDER"),
            ENDERMEN = new SentinelTarget(new EntityType[]{EntityType.ENDERMAN}, "ENDERMAN", "ENDER_MAN", "ENDERMEN", "ENDER_MEN"),
            ENDERMITES = new SentinelTarget(new EntityType[]{EntityType.ENDERMITE}, "ENDERMITE", "ENDER_MITE"),
            WITHERS = new SentinelTarget(new EntityType[]{EntityType.WITHER}, "WITHER"),
            ENDERDRAGONS = new SentinelTarget(new EntityType[]{EntityType.ENDER_DRAGON}, "ENDERDRAGON", "ENDER_DRAGON");

    /**
     * Valid target types for 1.9 or higher.
     */
    public static SentinelTarget SHULKERS;

    /**
     * Valid target types for 1.10 or higher.
     */
    public static SentinelTarget POLAR_BEARS;

    /**
     * Valid target types for 1.11 or higher.
     */
    public static SentinelTarget VEXES, DONKEYS, LLAMAS, MULES, HUSKS, ELDER_GUARDIANS,
            EVOKERS, SKELETON_HORSES, STRAYS, ZOMBIE_VILLAGERS, ZOMBIE_HORSES, WITHER_SKELETONS, VINDICATORS;

    /**
     * Valid target types for 1.12 or higher.
     */
    public static SentinelTarget PARROTS, ILLUSIONERS;

    /**
     * Valid target types for 1.13 or higher.
     */
    public static SentinelTarget DOLPHINS, DROWNED, COD, SALMON, PUFFERFISH, TROPICAL_FISH, PHANTOMS, TURTLES, FISH;

    /**
     * Valid target types for 1.14 or higher.
     */
    public static SentinelTarget RAVAGERS, PILLAGERS, CATS, PANDAS, TRADER_LLAMAS, WANDERING_TRADERS, FOXES;

    /**
     * Valid target types for 1.15 or higher.
     */
    public static SentinelTarget BEES;

    /**
     * Valid target types for 1.15 or LOWER.
     */
    public static SentinelTarget ZOMBIE_PIGMEN;

    /**
     * Valid target types for 1.16 or higher.
     */
    public static SentinelTarget HOGLINS, PIGLINS, STRIDERS, ZOGLINS, ZOMBIFIED_PIGLINS, PIGLIN_BRUTE;

    /**
     * Multiple-entity-type targets.
     */
    public static SentinelTarget PASSIVE_MOBS, MONSTERS, MOBS;

    /**
     * Gets the set of targets that include an entity type.
     */
    public static HashSet<SentinelTarget> forEntityType(EntityType type) {
        return SentinelPlugin.entityToTargets.get(type);
    }

    /**
     * Gets the Sentinel target that matches a name.
     */
    public static SentinelTarget forName(String name) {
        return SentinelPlugin.targetOptions.get(name.toUpperCase());
    }

    /**
     * The array of valid names for this target type.
     */
    public String[] names;

    /**
     * The name of this target type.
     */
    public String name() {
        return names[0];
    }

    /**
     * Constructs the target type instance.
     */
    public SentinelTarget(EntityType[] types, String... names) {
        this.names = names;
        this.types = new HashSet<>(Arrays.asList(types));
        for (String name : names) {
            SentinelPlugin.targetOptions.put(name, this);
            SentinelPlugin.targetOptions.put(name + "S", this);
        }
        for (EntityType type : types) {
            SentinelPlugin.entityToTargets.get(type).add(this);
        }
    }

    public HashSet<EntityType> types;

    /**
     * Returns whether this SentinelTarget targets the given entity.
     */
    public boolean isTarget(LivingEntity entity) {
        return isTarget(entity, null);
    }

    /**
     * Returns whether this SentinelTarget targets the given entity for the given Sentinel.
     */
    public boolean isTarget(LivingEntity entity, SentinelTrait sentinel) {
        if (types.contains(entity.getType())) {
            return true;
        }
        if (this == NPCS && CitizensAPI.getNPCRegistry().isNPC(entity)) {
            return true;
        }
        if (this == OWNER && sentinel != null
            && entity.getUniqueId().equals(sentinel.getNPC().getOrAddTrait(Owner.class).getOwnerId())) {
            return true;
        }
        return false;
    }
}
