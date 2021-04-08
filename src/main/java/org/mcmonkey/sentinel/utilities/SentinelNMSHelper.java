package org.mcmonkey.sentinel.utilities;

import net.citizensnpcs.util.NMS;
import org.bukkit.Bukkit;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.IronGolem;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Field;

/**
 * Helper for NMS-based actions.
 */
public class SentinelNMSHelper {

    public static MethodHandle CRAFTENTITY_GETHANDLE, NMSENTITY_WORLDGETTER, NMSWORLD_BROADCASTENTITYEFFECT, NMSENTITY_GETDATAWATCHER, DATWATCHER_SET;

    public static Object ENTITYENDERMAN_DATAWATCHER_ANGRY;

    private static boolean nmsWorks = true, endermanValid = false;

    public static void init() {
        try {
            if (!SentinelVersionCompat.v1_12) {
                nmsWorks = false;
                return;
            }
            // Will be like "org.bukkit.craftbukkit.v1_16_R3"
            String bukkitPackageName = Bukkit.getServer().getClass().getPackage().getName();
            // Should be like "v1_16_R3"
            String packageVersion = bukkitPackageName.substring(bukkitPackageName.lastIndexOf('.') + 1);
            String nmsPackageName = "net.minecraft.server." + packageVersion;
            Class craftEntity = Class.forName(bukkitPackageName + ".entity.CraftEntity");
            CRAFTENTITY_GETHANDLE = NMS.getMethodHandle(craftEntity, "getHandle", true);
            Class nmsEntity = Class.forName(nmsPackageName + ".Entity");
            NMSENTITY_WORLDGETTER = NMS.getGetter(nmsEntity, "world");
            NMSENTITY_GETDATAWATCHER = NMS.getMethodHandle(nmsEntity, "getDataWatcher", true);
            Class nmsWorld = Class.forName(nmsPackageName + ".World");
            NMSWORLD_BROADCASTENTITYEFFECT = NMS.getMethodHandle(nmsWorld, "broadcastEntityEffect", true, nmsEntity, byte.class);
            Class nmsDataWatcher = Class.forName(nmsPackageName + ".DataWatcher");
            Class nmsDataWatcherObject = Class.forName(nmsPackageName + ".DataWatcherObject");
            DATWATCHER_SET = NMS.getMethodHandle(nmsDataWatcher, "set", true, nmsDataWatcherObject, Object.class);
            if (SentinelVersionCompat.v1_16 && !SentinelVersionCompat.vFuture) {
                Class nmsEntityEnderman = Class.forName(nmsPackageName + ".EntityEnderman");
                Field dataWatcherAngryField = NMS.getField(nmsEntityEnderman, "bo");
                dataWatcherAngryField.setAccessible(true);
                ENTITYENDERMAN_DATAWATCHER_ANGRY = dataWatcherAngryField.get(null);
                endermanValid = true;
            }
        }
        catch (Throwable ex) {
            ex.printStackTrace();
            nmsWorks = false;
            endermanValid = false;
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

    public static void setEndermanAngry(Enderman entity, boolean angry) {
        if (!nmsWorks || !endermanValid) {
            return;
        }
        try {
            Object nmsHandle = CRAFTENTITY_GETHANDLE.invoke(entity);
            Object dataWatcher = NMSENTITY_GETDATAWATCHER.invoke(nmsHandle);
            DATWATCHER_SET.invoke(dataWatcher, ENTITYENDERMAN_DATAWATCHER_ANGRY, angry);
        }
        catch (Throwable ex) {
            endermanValid = false;
        }
    }
}
