package org.mcmonkey.sentinel;

import org.bukkit.plugin.java.JavaPlugin;

public class SentinelPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        getLogger().info("Sentinel loading...");
        getLogger().info("Sentinel loaded!");
    }

    @Override
    public void onDisable() {
        getLogger().info("Sentinel unloading...");
        getLogger().info("Sentinel unloaded!");
    }
}
