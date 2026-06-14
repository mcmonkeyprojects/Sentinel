package org.mcmonkey.sentinel.utilities;

import net.citizensnpcs.util.NMS;
import org.bukkit.Bukkit;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.IronGolem;
import org.bukkit.event.inventory.InventoryEvent;

import java.lang.invoke.MethodHandle;

/**
 * Helper for NMS-based actions.
 */
public class SentinelNMSHelper {

    public static MethodHandle CRAFTENTITY_GETHANDLE, NMSENTITY_WORLDGETTER, NMSWORLD_BROADCASTENTITYEFFECT, NMSENTITY_GETDATAWATCHER, DATWATCHER_SET;

    private static boolean nmsWorks = true;

    public static MethodHandle INVENTORYCLOSEEVENT_GETVIEW, INVENTORYVIEW_GETTITLE;

    public static Class<?> getOptionalFieldType(Class<?> clazz, String fieldName) {
        try {
            return clazz.getDeclaredField(fieldName).getType();
        }
        catch (NoSuchFieldException ex) {
            return null;
        }
    }

    /**
     * Returns the first of the given class names that exists (multi-mapping support: Mojang-mapped vs legacy Spigot-remapped NMS).
     */
    private static Class<?> classForFirst(String... classNames) throws ClassNotFoundException {
        for (String name : classNames) {
            try {
                return Class.forName(name);
            }
            catch (ClassNotFoundException ex) {
                // Try the next candidate name.
            }
        }
        throw new ClassNotFoundException("None of the candidate NMS classes exist: " + String.join(", ", classNames));
    }

    /**
     * Returns a handle for the first of the given method names that exists on the class (multi-mapping support: Mojang names vs obfuscated/Spigot names).
     */
    private static MethodHandle methodHandleForFirst(Class<?> clazz, Class<?>[] params, String... methodNames) {
        for (String name : methodNames) {
            MethodHandle handle = NMS.getMethodHandle(clazz, name, false, params); // log=false: missing candidates are expected, don't spam the console
            if (handle != null) {
                return handle;
            }
        }
        return null;
    }

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
            Class nmsEntity, nmsWorld, nmsDataWatcher, nmsDataWatcherObject;
            String broadcastEffectMethod = "broadcastEntityEffect", dataWatcherSet = "set";
            if (SentinelVersionCompat.v1_17) { // 1.17+ - Mojang mappings update
                nmsEntity = Class.forName("net.minecraft.world.entity.Entity");
                // Try Mojang-mapped names first (server runtime mappings since 1.20.5+, including 26.1), then the legacy Spigot-remapped names (1.17-1.21).
                nmsWorld = classForFirst("net.minecraft.world.level.Level", "net.minecraft.world.level.World");
                nmsDataWatcher = classForFirst("net.minecraft.network.syncher.SynchedEntityData", "net.minecraft.network.syncher.DataWatcher");
                nmsDataWatcherObject = classForFirst("net.minecraft.network.syncher.EntityDataAccessor", "net.minecraft.network.syncher.DataWatcherObject");
                if (SentinelVersionCompat.v1_21 && !SentinelVersionCompat.vFuture) { // 1.21 names
                    broadcastEffectMethod = "a"; // net.minecraft.world.level.Level#broadcastEntityEvent(Entity,byte)
                    dataWatcherSet = "a"; // net.minecraft.network.syncher.SynchedEntityData#set
                }
                else if (SentinelVersionCompat.v1_20 && !SentinelVersionCompat.v1_21) { // 1.20 names
                    // https://minidigger.github.io/MiniMappingViewer/#/mojang/server/1.20.6
                    broadcastEffectMethod = "a"; // net.minecraft.world.level.Level#broadcastEntityEvent(Entity,byte)
                    dataWatcherSet = "a"; // net.minecraft.network.syncher.SynchedEntityData#set
                }
                else if (SentinelVersionCompat.v1_19 && !SentinelVersionCompat.v1_20) { // 1.19.4 names
                    // https://minidigger.github.io/MiniMappingViewer/#/mojang/server/1.19.4
                    broadcastEffectMethod = "a"; // net.minecraft.world.level.Level#broadcastEntityEvent(Entity,byte)
                    dataWatcherSet = "b"; // net.minecraft.network.syncher.SynchedEntityData#set
                }
                else if (SentinelVersionCompat.v1_18 && !SentinelVersionCompat.v1_19) { // 1.18 names
                    // https://minidigger.github.io/MiniMappingViewer/#/mojang/server/1.18.2
                    broadcastEffectMethod = "a"; // net.minecraft.world.level.Level#broadcastEntityEvent(Entity,byte)
                    dataWatcherSet = "b"; // net.minecraft.network.syncher.SynchedEntityData#set
                }
            }
            else { // 1.12 through 1.16 - Original Spigot NMS versioned mappings
                String packageVersion = bukkitPackageName.substring(bukkitPackageName.lastIndexOf('.') + 1); // Should be like "v1_16_R3"
                String nmsPackageName = "net.minecraft.server." + packageVersion;
                nmsEntity = Class.forName(nmsPackageName + ".Entity");
                nmsWorld = Class.forName(nmsPackageName + ".World");
                nmsDataWatcher = Class.forName(nmsPackageName + ".DataWatcher");
                nmsDataWatcherObject = Class.forName(nmsPackageName + ".DataWatcherObject");
            }
            NMSENTITY_WORLDGETTER = NMS.getFirstGetter(nmsEntity, nmsWorld);
            NMSENTITY_GETDATAWATCHER = NMS.getFirstGetter(nmsEntity, nmsDataWatcher);
            // Try the Mojang-mapped method name first (26.1+ runs on Mojang mappings), then fall back to the per-version Spigot/obfuscated name selected above.
            NMSWORLD_BROADCASTENTITYEFFECT = methodHandleForFirst(nmsWorld, new Class<?>[]{nmsEntity, byte.class}, "broadcastEntityEvent", broadcastEffectMethod);
            DATWATCHER_SET = methodHandleForFirst(nmsDataWatcher, new Class<?>[]{nmsDataWatcherObject, Object.class}, "set", dataWatcherSet);
            if (NMSWORLD_BROADCASTENTITYEFFECT == null || DATWATCHER_SET == null) {
                nmsWorks = false; // Couldn't resolve the NMS methods on this version; cosmetic NMS effects will be skipped (plugin otherwise functions normally).
            }
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

    public static void setEndermanAngry(Enderman entity, boolean angry) {
        NMS.setEndermanAngry(entity, angry);
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
