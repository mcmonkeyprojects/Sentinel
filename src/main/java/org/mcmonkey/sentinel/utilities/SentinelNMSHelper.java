package org.mcmonkey.sentinel.utilities;

import net.citizensnpcs.util.NMS;
import org.bukkit.Bukkit;
import org.bukkit.entity.IronGolem;

import java.lang.invoke.MethodHandle;

/**
 * Helper for NMS-based actions.
 */
public class SentinelNMSHelper {

    public static MethodHandle CRAFTENTITY_GETHANDLE, NMSENTITY_WORLDGETTER, NMSWORLD_BROADCASTENTITYEFFECT;

    private static boolean nmsWorks = true;

    static {
        try {
            // Will be like "org.bukkit.craftbukkit.v1_16_R3"
            String bukkitPackageName = Bukkit.getServer().getClass().getPackage().getName();
            // Should be like "v1_16_R3"
            String packageVersion = bukkitPackageName.substring(bukkitPackageName.lastIndexOf('.') + 1);
            String nmsPackageName = "net.minecraft.server." + packageVersion;
            Class craftEntity = Class.forName(bukkitPackageName + ".entity.CraftEntity");
            CRAFTENTITY_GETHANDLE = NMS.getMethodHandle(craftEntity, "getHandle", true);
            Class nmsEntity = Class.forName(nmsPackageName + ".Entity");
            NMSENTITY_WORLDGETTER = NMS.getGetter(nmsEntity, "world");
            Class nmsWorld = Class.forName(nmsPackageName + ".World");
            NMSWORLD_BROADCASTENTITYEFFECT = NMS.getMethodHandle(nmsWorld, "broadcastEntityEffect", true, nmsEntity, byte.class);
        }
        catch (Throwable ex) {
            ex.printStackTrace();
            nmsWorks = false;
        }
    }

    public static void animateIronGolemSwing(IronGolem entity) {
        if (!nmsWorks) {
            return;
        }
        try {
            Object nmsHandle = CRAFTENTITY_GETHANDLE.invoke(entity);
            Object world = NMSENTITY_WORLDGETTER.invoke(nmsHandle);
            NMSWORLD_BROADCASTENTITYEFFECT.invoke(world, nmsHandle, (byte) 4);
        }
        catch (Throwable ex) {
            nmsWorks = false;
            ex.printStackTrace();
        }
    }
}
