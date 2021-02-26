package org.mcmonkey.sentinel.utilities;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.mcmonkey.sentinel.SentinelPlugin;
import org.mcmonkey.sentinel.SentinelTrait;
import org.mcmonkey.sentinel.events.SentinelWantsToPathEvent;

public class SentinelWorldGuardHelper implements Listener {

    public SentinelWorldGuardHelper() {
        Bukkit.getPluginManager().registerEvents(this, SentinelPlugin.instance);
    }

    public static Object getRegionFor(String name, World world) {
        RegionManager manager = WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(world));
        if (manager == null) {
            return null;
        }
        return manager.getRegion(name);
    }

    public static ProtectedRegion getRegionFor(SentinelTrait sentinel) {
        if (sentinel.worldguardRegion == null || sentinel.worldguardRegion.isEmpty()) {
            return null;
        }
        if (sentinel.worldguardRegionCache == null) {
            sentinel.worldguardRegionCache = getRegionFor(sentinel.worldguardRegion, sentinel.getNPC().getStoredLocation().getWorld());
            if (sentinel.worldguardRegionCache == null) {
                return null;
            }
        }
        return (ProtectedRegion) sentinel.worldguardRegionCache;
    }

    @EventHandler
    public void onWantsPath(SentinelWantsToPathEvent event) {
        SentinelTrait sentinel = event.getNPC().getOrAddTrait(SentinelTrait.class);
        ProtectedRegion region = getRegionFor(sentinel);
        if (region == null) {
            return;
        }
        if (!region.contains(BukkitAdapter.asBlockVector(event.destination))) {
            event.setCancelled(true);
        }
    }
}
