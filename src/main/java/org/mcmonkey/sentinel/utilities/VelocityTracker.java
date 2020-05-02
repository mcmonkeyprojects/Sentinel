package org.mcmonkey.sentinel.utilities;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Player velocity is not tracked on the server normally. This class helps track it by estimating player motion.
 */
public class VelocityTracker {

    /**
     * Map of players (by UUID) to their velocity.
     * Players that log off are removed from this list by SentinelEventHandler#whenAnEnemyDies.
     */
    public static Map<UUID, VelocityTracker> playerVelocityEstimates = new HashMap<>();

    /**
     * Get the current velocity for a player.
     * Returns a zero vector for unknown players.
     */
    public static Vector getVelocityFor(Player player) {
        VelocityTracker result = playerVelocityEstimates.get(player.getUniqueId());
        if (result == null) {
            return new Vector(0, 0, 0);
        }
        return result.velocity;
    }

    private static Location locationOpti = new Location(null, 0, 0, 0);

    /**
     * Updates the velocity tracker for all players.
     */
    public static void runAll() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            VelocityTracker tracker = playerVelocityEstimates.get(player.getUniqueId());
            if (tracker == null) {
                tracker = new VelocityTracker();
                tracker.lastLocation = player.getLocation();
                playerVelocityEstimates.put(player.getUniqueId(), tracker);
            }
            if (player.getWorld().equals(tracker.lastLocation.getWorld())) {
                // This is an optimization hack to reduce object creation, because this loops often
                Location velocity = player.getLocation(locationOpti).subtract(tracker.lastLocation);
                tracker.velocity.setX(velocity.getX());
                tracker.velocity.setY(velocity.getY());
                tracker.velocity.setZ(velocity.getZ());
            }
            tracker.lastLocation = player.getLocation();
        }
    }

    /**
     * The last known velocity for this player.
     */
    public Vector velocity = new Vector(0, 0, 0);

    /**
     * The last known location for this player.
     */
    public Location lastLocation;
}
