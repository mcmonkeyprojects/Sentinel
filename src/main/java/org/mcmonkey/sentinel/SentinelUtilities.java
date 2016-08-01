package org.mcmonkey.sentinel;

import org.bukkit.Location;
import org.bukkit.util.Vector;

import java.util.Random;

public class SentinelUtilities {

    public static Random random = new Random();

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

    public static double hangtime(double launchAngle, double vel, double deltaY, double gravity) {
        double a = vel * Math.sin(launchAngle);
        double b = -2 * gravity * deltaY;
        double a2 = a * a + b;
        if (a2 < 0) {
            return 0;
        }
        return (a + Math.sqrt(a2)) / gravity;
    }

    public static String concatWithSpaces(String[] strs, int start) {
        String temp = "";
        for (int i = start; i < strs.length; i++) {
            temp += strs[i] + " ";
        }
        return temp;
    }
}
