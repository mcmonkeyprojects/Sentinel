package org.mcmonkey.sentinel;

import net.citizensnpcs.api.ai.EntityTarget;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import org.mcmonkey.sentinel.targeting.SentinelTarget;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.regex.Pattern;

public class SentinelUtilities {

    /**
     * A random object for reuse.
     */
    public static Random random = new Random();

    /**
     * Returns the item held in an entity's hand.
     */
    public static ItemStack getHeldItem(LivingEntity entity) {
        if (SentinelTarget.v1_9) {
            return entity.getEquipment().getItemInMainHand();
        }
        else {
            return entity.getEquipment().getItemInHand();
        }
    }

    /**
     * Traces a ray from a start to an end, returning the end of the ray (stopped early if there are solid blocks in the way).
     */
    public static Location rayTrace(Location start, Location end) {
        double dSq = start.distanceSquared(end);
        if (dSq < 1) {
            if (end.getBlock().getType().isSolid()) {
                return start.clone();
            }
            return end.clone();
        }
        double dist = Math.sqrt(dSq);
        Vector move = end.toVector().subtract(start.toVector()).multiply(1.0 / dist);
        int iters = (int) Math.ceil(dist);
        Location cur = start.clone();
        Location next = cur.clone().add(move);
        for (int i = 0; i < iters; i++) {
            if (next.getBlock().getType().isSolid()) {
                return cur;
            }
            cur = cur.add(move);
            next = next.add(move);
        }
        return cur;
    }

    /**
     * Picks an accessible location near the start location, within a range.
     */
    public static Location pickNear(Location start, double range) {
        Location hit = rayTrace(start.clone().add(0, 1.5, 0), start.clone().add(
                SentinelUtilities.randomDecimal(-range, range), 1.5, SentinelUtilities.randomDecimal(range, range)));
        if (hit.subtract(0, 1, 0).getBlock().getType().isSolid()) {
            return hit;
        }
        return hit.subtract(0, 1, 0);
    }

    /**
     * Look up table for pre-compiled regex values.
     */
    public static HashMap<String, Pattern> regexes = new HashMap<>(128);

    /**
     * Gets a compiled regex pattern for a string of the regex.
     * More efficient than recompiling every-time due to lookup-table usage.
     */
    public static Pattern regexFor(String input) {
        Pattern result = regexes.get(input);
        if (result != null) {
            return result;
        }
        result = Pattern.compile(input, Pattern.CASE_INSENSITIVE);
        regexes.put(input, result);
        return result;
    }

    /**
     * Returns whether a list of regex values match the a string.
     */
    public static boolean isRegexTargeted(String name, List<String> regexes) {
        for (String str : regexes) {
            Pattern pattern = SentinelUtilities.regexFor(".*" + str + ".*");
            if (pattern.matcher(name).matches()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gets the yaw angle value (in degrees) for a vector.
     */
    public static float getYaw(Vector vector) {
        double dx = vector.getX();
        double dz = vector.getZ();
        double yaw = 0;
        // Set yaw
        if (dx != 0) {
            // Set yaw start value based on dx
            if (dx < 0) {
                yaw = 1.5 * Math.PI;
            }
            else {
                yaw = 0.5 * Math.PI;
            }
            yaw -= Math.atan(dz / dx); // or atan2?
        }
        else if (dz < 0) {
            yaw = Math.PI;
        }
        return (float) (-yaw * (180.0 / Math.PI));
    }

    /**
     * Gets a random decimal from a minimum value to a maximum value.
     */
    public static double randomDecimal(double min, double max) {
        return (random.nextDouble() * (max - min)) + min;
    }

    /**
     * Returns when an item is considered to be an air item.
     */
    public static boolean isAir(ItemStack its) {
        return its == null || its.getType() == Material.AIR;
    }

    /**
     * Gets the entity for a given UUID.
     */
    public static Entity getEntityForID(UUID id) {
        if (!SentinelTarget.v1_12) {
            for (World world : Bukkit.getServer().getWorlds()) {
                for (Entity e : world.getEntities()) {
                    if (e.getUniqueId().equals(id)) {
                        return e;
                    }
                }
            }
            return null;
        }
        return Bukkit.getServer().getEntity(id);
    }

    /**
     * Returns whether an entity is invisible (when invisible targets are ignorable).
     */
    public static boolean isInvisible(LivingEntity entity) {
        EntityEquipment eq = entity.getEquipment();
        if (!SentinelPlugin.instance.ignoreInvisible
                || !entity.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
            return false;
        }
        if (SentinelTarget.v1_9) {
            if (!isAir(eq.getItemInMainHand()) || !isAir(eq.getItemInOffHand())) {
                return false;
            }
        }
        else {
            if (!isAir(eq.getItemInHand())) {
                return false;
            }
        }
        return isAir(eq.getBoots()) && isAir(eq.getLeggings()) && isAir(eq.getChestplate()) && isAir(eq.getHelmet());
    }

    /**
     * Gets the entity target referenced by a CitizensAPI {@code EntityTarget} object.
     * Should never return null except in error cases.
     */
    public static Entity getTargetFor(EntityTarget targ) {
        if (SentinelTarget.v1_9) {
            return targ.getTarget();
        }
        try {
            Method meth = EntityTarget.class.getMethod("getTarget");
            meth.setAccessible(true);
            return (LivingEntity) meth.invoke(targ);
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        return targ.getTarget(); // If the pre-1.9 reflection call failed, just call it directly and let Java produce an exception that will propagate up normally.
    }

    /**
     * Gets a 'launch detail' (starting location with direction vector set to correct firing direction, and a vector holding the exact launch vector, scaled to the correct speed).
     */
    public static HashMap.SimpleEntry<Location, Vector> getLaunchDetail(Location start, Location target, Vector lead) {
        double speeda;
        double angt = Double.POSITIVE_INFINITY;
        double sbase = SentinelPlugin.instance.minShootSpeed;
        for (speeda = sbase; speeda <= sbase + 15; speeda += 5) {
            // TODO: Mathematically calculate a valid starting speed, to avoid pointlessly looping on a math utility.
            angt = SentinelUtilities.getArrowAngle(start, target, speeda, 20);
            if (!Double.isInfinite(angt)) {
                break;
            }
        }
        if (Double.isInfinite(angt)) {
            return null;
        }
        double hangT = SentinelUtilities.hangtime(angt, speeda, target.getY() - start.getY(), 20);
        Location to = target.clone().add(lead.clone().multiply(hangT));
        Vector relative = to.clone().subtract(start.toVector()).toVector();
        double deltaXZ = Math.sqrt(relative.getX() * relative.getX() + relative.getZ() * relative.getZ());
        if (deltaXZ == 0) {
            deltaXZ = 0.1;
        }
        for (speeda = sbase; speeda <= sbase + 15; speeda += 5) {
            angt = SentinelUtilities.getArrowAngle(start, to, speeda, 20);
            if (!Double.isInfinite(angt)) {
                break;
            }
        }
        if (Double.isInfinite(angt)) {
            return null;
        }
        relative.setY(Math.tan(angt) * deltaXZ);
        relative = relative.normalize();
        Vector normrel = relative.clone();
        speeda = speeda + (1.188 * hangT * hangT);
        relative = relative.multiply(speeda / 20.0);
        start.setDirection(normrel);
        return new HashMap.SimpleEntry<>(start, relative);
    }

    /**
     * Calculates the ideal angle to fire an arrow at to hit a target (roughly based on older Sentry code).
     *
     * Can return {@code Double.NEGATIVE_INFINITY} when hitting the target is impossible.
     */
    public static double getArrowAngle(Location fireFrom, Location fireTo, double speed, double gravity) {
        Vector delta = fireTo.clone().subtract(fireFrom).toVector();
        double deltaXZ = Math.sqrt(delta.getX() * delta.getX() + delta.getZ() * delta.getZ());
        if (deltaXZ == 0) {
            deltaXZ = 0.1;
        }
        double deltaY = fireTo.getY() - fireFrom.getY();
        double v2 = speed * speed;
        double v4 = v2 * v2;
        double basic = gravity * (gravity * deltaXZ * deltaXZ + 2 * deltaY * v2);
        if (v4 < basic) {
            return Double.NEGATIVE_INFINITY;
        }
        else {
            return Math.atan((v2 - Math.sqrt(v4 - basic)) / (gravity * deltaXZ));
        }
    }

    /**
     * Calculates the hang-time (time from shot until landing) of a projectile (roughly based on older Sentry code).
     */
    public static double hangtime(double launchAngle, double vel, double deltaY, double gravity) {
        double a = vel * Math.sin(launchAngle);
        double b = -2 * gravity * deltaY;
        double a2 = a * a + b;
        if (a2 < 0) {
            return 0;
        }
        return (a + Math.sqrt(a2)) / gravity;
    }

    /**
     * Concatenates (combines) an array of strings with spaces in between - just a shorthand/helper method.
     */
    public static String concatWithSpaces(String[] strs, int start) {
        StringBuilder temp = new StringBuilder();
        for (int i = start; i < strs.length; i++) {
            temp.append(strs[i]).append(" ");
        }
        return temp.toString();
    }

    /**
     * Constant: name of the standard encoding to prefer (UTF-8).
     */
    public final static String ENCODING = "UTF-8";

    /**
     * Constant: The size of a buffer, 10 kilobytes.
     */
    public final static int BUFFER_10_KB = 1024 * 10;

    /**
     * Converts an input stream to a string (of the stream's contents).
     *
     * This only needs to exist because Java inexplicably has no easy native way to accomplish this.
     */
    public static String streamToString(InputStream is) {
        try {
            final char[] buffer = new char[BUFFER_10_KB];
            final StringBuilder out = new StringBuilder();
            try (Reader in = new InputStreamReader(is, ENCODING)) {
                while (true) {
                    int rsz = in.read(buffer, 0, buffer.length);
                    if (rsz < 0) {
                        break;
                    }
                    out.append(buffer, 0, rsz);
                }
            }
            return out.toString();
        }
        catch (Exception ex) {
            return null;
        }
    }
}
