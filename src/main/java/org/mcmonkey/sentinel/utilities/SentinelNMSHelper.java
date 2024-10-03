package org.mcmonkey.sentinel.utilities;

import net.citizensnpcs.util.NMS;
import org.bukkit.Bukkit;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.IronGolem;
import org.bukkit.event.inventory.InventoryEvent;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Field;

/**
 * Helper for NMS-based actions.
 */
public class SentinelNMSHelper {

    public static MethodHandle CRAFTENTITY_GETHANDLE, NMSENTITY_WORLDGETTER, NMSWORLD_BROADCASTENTITYEFFECT, NMSENTITY_GETDATAWATCHER, DATWATCHER_SET;

    public static Object ENTITYENDERMAN_DATAWATCHER_ANGRY;

    private static boolean nmsWorks = true, endermanValid = false;

    public static MethodHandle INVENTORYCLOSEEVENT_GETVIEW, INVENTORYVIEW_GETTITLE;

    public static void init() {
        try {
            if (SentinelVersionCompat.v1_10) {
                INVENTORYCLOSEEVENT_GETVIEW = NMS.getMethodHandle(InventoryEvent.class, "getView", true);
                INVENTORYVIEW_GETTITLE = NMS.getMethodHandle(INVENTORYCLOSEEVENT_GETVIEW.type().returnType(), "getTitle", true);
            }
            if (!SentinelVersionCompat.v1_12) {
                nmsWorks = false;
                return;
            }
            // Will be like "org.bukkit.craftbukkit.v1_16_R3"
            String bukkitPackageName = Bukkit.getServer().getClass().getPackage().getName();
            Class craftEntity = Class.forName(bukkitPackageName + ".entity.CraftEntity");
            CRAFTENTITY_GETHANDLE = NMS.getMethodHandle(craftEntity, "getHandle", true);
            Class nmsEntity, nmsWorld, nmsDataWatcher, nmsDataWatcherObject, nmsEntityEnderman, nmsHuman, nmsLivingEntity;
            String endermanAngryField = null;
            String broadcastEffectMethod = "broadcastEntityEffect", dataWatcherSet = "set";
            if (SentinelVersionCompat.v1_17) { // 1.17+ - Mojang mappings update
                nmsEntity = Class.forName("net.minecraft.world.entity.Entity");
                nmsWorld = Class.forName("net.minecraft.world.level.World"); // Level
                nmsDataWatcher = Class.forName("net.minecraft.network.syncher.DataWatcher"); // SynchedEntityData
                nmsDataWatcherObject = Class.forName("net.minecraft.network.syncher.DataWatcherObject"); // EntityDataAccessor
                nmsEntityEnderman = Class.forName("net.minecraft.world.entity.monster.EntityEnderman");
                if (SentinelVersionCompat.v1_21 && !SentinelVersionCompat.vFuture) { // 1.21 names
                    // https://minidigger.github.io/MiniMappingViewer/#/mojang/server/1.21
                    endermanAngryField = "ce"; // net.minecraft.world.entity.monster.EnderMan#DATA_CREEPY
                    broadcastEffectMethod = "a"; // net.minecraft.world.level.Level#broadcastEntityEvent(Entity,byte)
                    dataWatcherSet = "a"; // net.minecraft.network.syncher.SynchedEntityData#set
                }
                else if (SentinelVersionCompat.v1_20 && !SentinelVersionCompat.v1_21) { // 1.20 names
                    try {
                        // https://minidigger.github.io/MiniMappingViewer/#/mojang/server/1.20.2
                        if (nmsEntityEnderman.getDeclaredField("bV") != null) {
                            endermanAngryField = "bV"; // net.minecraft.world.entity.monster.EnderMan#DATA_CREEPY
                        }
                    }
                    catch (Throwable ex) {
                        // https://minidigger.github.io/MiniMappingViewer/#/mojang/server/1.20.6
                        endermanAngryField = "ca"; // net.minecraft.world.entity.monster.EnderMan#DATA_CREEPY
                    }
                    // https://minidigger.github.io/MiniMappingViewer/#/mojang/server/1.20.6
                    broadcastEffectMethod = "a"; // net.minecraft.world.level.Level#broadcastEntityEvent(Entity,byte)
                    dataWatcherSet = "a"; // net.minecraft.network.syncher.SynchedEntityData#set
                }
                else if (SentinelVersionCompat.v1_19 && !SentinelVersionCompat.v1_20) { // 1.19.4 names
                    // https://minidigger.github.io/MiniMappingViewer/#/mojang/server/1.19.4
                    endermanAngryField = "bU"; // net.minecraft.world.entity.monster.EnderMan#DATA_CREEPY
                    broadcastEffectMethod = "a"; // net.minecraft.world.level.Level#broadcastEntityEvent(Entity,byte)
                    dataWatcherSet = "b"; // net.minecraft.network.syncher.SynchedEntityData#set
                }
                else if (SentinelVersionCompat.v1_18 && !SentinelVersionCompat.v1_19) { // 1.18 names
                    // https://minidigger.github.io/MiniMappingViewer/#/mojang/server/1.18.2
                    endermanAngryField = "bX"; // net.minecraft.world.entity.monster.EnderMan#DATA_CREEPY
                    broadcastEffectMethod = "a"; // net.minecraft.world.level.Level#broadcastEntityEvent(Entity,byte)
                    dataWatcherSet = "b"; // net.minecraft.network.syncher.SynchedEntityData#set
                }
                else if (!SentinelVersionCompat.v1_18) { // 1.17 names
                    endermanAngryField = "bV"; // EnderMan#DATA_CREEPY
                }
            }
            else { // 1.12 through 1.16 - Original Spigot NMS versioned mappings
                String packageVersion = bukkitPackageName.substring(bukkitPackageName.lastIndexOf('.') + 1); // Should be like "v1_16_R3"
                String nmsPackageName = "net.minecraft.server." + packageVersion;
                nmsEntity = Class.forName(nmsPackageName + ".Entity");
                nmsWorld = Class.forName(nmsPackageName + ".World");
                nmsDataWatcher = Class.forName(nmsPackageName + ".DataWatcher");
                nmsDataWatcherObject = Class.forName(nmsPackageName + ".DataWatcherObject");
                nmsEntityEnderman = Class.forName(nmsPackageName + ".EntityEnderman");
                if (SentinelVersionCompat.v1_16) {
                    endermanAngryField = "bo";
                }
            }
            NMSENTITY_WORLDGETTER = NMS.getFirstGetter(nmsEntity, nmsWorld);
            NMSENTITY_GETDATAWATCHER = NMS.getFirstGetter(nmsEntity, nmsDataWatcher);
            NMSWORLD_BROADCASTENTITYEFFECT = NMS.getMethodHandle(nmsWorld, broadcastEffectMethod, true, nmsEntity, byte.class);
            DATWATCHER_SET = NMS.getMethodHandle(nmsDataWatcher, dataWatcherSet, true, nmsDataWatcherObject, Object.class);
            if (endermanAngryField != null && nmsEntityEnderman != null) {
                Field dataWatcherAngryField = NMS.getField(nmsEntityEnderman, endermanAngryField);
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


    /**
     * Gets the title of an inventory in an InventoryEvent (compensates for code change between Spigot versions).
     */
    public static String getInventoryTitle(InventoryEvent event) {
        try {
            if (SentinelVersionCompat.v1_10) {
                // Note: these methods are visible, but specific signatures change between versions, so reflection is only reliable inter-version option
                Object view = INVENTORYCLOSEEVENT_GETVIEW.invoke(event);
                return (String) INVENTORYVIEW_GETTITLE.invoke(view);
            }
            else {
                Object inventory = event.getInventory();
                return (String) inventory.getClass().getMethod("getTitle").invoke(inventory);
            }
        }
        catch (Throwable ex) {
            ex.printStackTrace();
        }
        return null;
    }
}
