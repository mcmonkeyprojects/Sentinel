package org.mcmonkey.sentinel.utilities;

import org.bukkit.Particle;
import org.bukkit.attribute.Attribute;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.potion.PotionType;

import java.lang.reflect.Field;

/**
 * Fixes for Spigot breaking API between updates.
 */
public class SentinelAPIBreakageFix {

    public static EntityType ENTITY_TYPE_MUSHROOM_COW = _getEnumValue(EntityType.class, "MUSHROOM_COW", "MOOSHROOM");

    public static EntityType ENTITY_TYPE_SNOWMAN = _getEnumValue(EntityType.class, "SNOWMAN", "SNOW_GOLEM");

    public static EntityType ENTITY_TYPE_FIREWORK = _getEnumValue(EntityType.class, "FIREWORK", "FIREWORK_ROCKET");

    public static Enchantment ENCHANTMENT_DAMAGE_ALL = _getEnumValue(Enchantment.class, "DAMAGE_ALL", "SHARPNESS");

    public static Enchantment ENCHANTMENT_ARROW_DAMAGE = _getEnumValue(Enchantment.class, "ARROW_DAMAGE", "POWER");

    public static Enchantment ENCHANTMENT_ARROW_FIRE = _getEnumValue(Enchantment.class, "ARROW_FIRE", "FLAME");

    public static Enchantment ENCHANTMENT_PROTECTION_FIRE = _getEnumValue(Enchantment.class, "PROTECTION_FIRE", "FIRE_PROTECTION");

    public static PotionType POTION_TYPE_UNCRAFTABLE = _getEnumValue(PotionType.class, "UNCRAFTABLE"); // NOTE: Null after 1.21

    public static Particle PARTICLE_SPELL = _getEnumValue(Particle.class, "SPELL", "EFFECT");

    public static Attribute ATTRIBUTE_GENERIC_KNOCKBACK_RESISTANCE = _getEnumValue(Attribute.class, "GENERIC_KNOCKBACK_RESISTANCE", "KNOCKBACK_RESISTANCE");

    /**
     * Note: not necessarily actually an enum.
     */
    public static <T> T _getEnumValue(Class<T> clazz, String... name) {
        try {
            for (Field f : clazz.getDeclaredFields()) {
                f.setAccessible(true);
                for (String n : name) {
                    if (f.getName().equalsIgnoreCase(n)) {
                        return (T) f.get(null);
                    }
                }
            }
        }
        catch (Throwable ex) {
            ex.printStackTrace();
        }
        return null;
    }
}
