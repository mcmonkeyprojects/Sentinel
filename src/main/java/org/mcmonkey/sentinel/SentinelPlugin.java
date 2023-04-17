package org.mcmonkey.sentinel;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.TraitInfo;
import net.citizensnpcs.api.trait.trait.Owner;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.*;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.mcmonkey.sentinel.commands.SentinelCommand;
import org.mcmonkey.sentinel.integration.*;
import org.mcmonkey.sentinel.metrics.BStatsMetricsLite;
import org.mcmonkey.sentinel.metrics.StatsRecord;
import org.mcmonkey.sentinel.targeting.SentinelTarget;
import org.mcmonkey.sentinel.utilities.*;

import java.io.*;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

/**
 * The main Sentinel plugin class.
 */
public class SentinelPlugin extends JavaPlugin {

    /**
     * A map of all valid event targets.
     */
    public static HashSet<String> validEventTargets = new HashSet<>(
            Arrays.asList("pvp", "pve", "pv", "pvnpc", "pvsentinel", "guarded_fight", "eve", "ev", "message")
    );

    /**
     * A map of typeable target names to valid targets.
     */
    public static HashMap<String, SentinelTarget> targetOptions = new HashMap<>();

    /**
     * A map of entity types to target types.
     */
    public static HashMap<EntityType, HashSet<SentinelTarget>> entityToTargets = new HashMap<>();

    /**
     * A map of target prefixes to the integration object.
     */
    public final static HashMap<String, SentinelIntegration> integrationPrefixMap = new HashMap<>();

    /**
     * All current integrations available to Sentinel.
     */
    public final static ArrayList<SentinelIntegration> integrations = new ArrayList<>();

    /**
     * Current plugin instance.
     */
    public static SentinelPlugin instance;

    /**
     * A list of all currently spawned Sentinel NPCs.
     */
    public ArrayList<SentinelTrait> currentSentinelNPCs = new ArrayList<>();

    /**
     * Cleans and returns the current Sentinel NPC list.
     */
    public ArrayList<SentinelTrait> cleanCurrentList() {
        ArrayList<SentinelTrait> npcs = SentinelPlugin.instance.currentSentinelNPCs;
        for (int i = 0; i < npcs.size(); i++) {
            if (!npcs.get(i).validateOnList()) {
                i--;
            }
        }
        return npcs;
    }

    /**
     * Permissions handler.
     */
    public Permission vaultPerms;

    /**
     * Total server tick time (for timing correction handlers).
     */
    public long tickTimeTotal = 0;

    /**
     * Configuration option: maximum health value any NPC can ever have.
     */
    public double maxHealth;

    /**
     * Configuration option: maximum duration (in ticks) an NPC can know where a hidden target is.
     */
    public int cleverTicks;

    /**
     * Configuration option: whether the skull weapon is allowed.
     */
    public boolean canUseSkull;

    /**
     * Configuration option: whether to block some events that may cause other plugins to have issues.
     */
    public boolean blockEvents;

    /**
     * Configuration option: whether to use an alternative (work-around) method of applying damage.
     */
    public boolean alternateDamage;

    /**
     * Configuration option: whether to work-around damage-giving issues.
     */
    public boolean workaroundDamage;

    /**
     * Configuration option: whether to do native attack instead of direct damage for melee.
     */
    public boolean doNativeAttack;

    /**
     * Configuration option: minimum arrow shooting speed.
     */
    public double minShootSpeed;

    /**
     * Configuration option: whether to work-around potential NPC item drop issues.
     */
    public boolean workaroundDrops;

    /**
     * Configuration option: whether to enable NPC death messages.
     */
    public boolean deathMessages;

    /**
     * Configuration option: the sound to play when using the Spectral attack.
     */
    public Sound spectralSound;

    /**
     * Configuration option: whether to ignore invisible targets.
     */
    public boolean ignoreInvisible;

    /**
     * Configuration option: guarding distance values.
     */
    public int guardDistanceMinimum, guardDistanceSelectionRange;

    /**
     * Configuration option: whether to work-around a pathfinder issue.
     */
    public boolean workaroundEntityChasePathfinder;

    /**
     * Configuration option: whether to auto-enable use-new-finder.
     */
    public boolean autoCorrectpathfinderMode;

    /**
     * Legacy configuration option: whether to protect all NPCs from being harmed by ignored entities.
     */
    @Deprecated
    public boolean protectFromIgnores;

    /**
     * Configuration option: standard tick-rate for NPC updates.
     */
    public int tickRate = 10;

    /**
     * Configuration option: time to keep running away for.
     */
    public int runAwayTime;

    /**
     * Configuration option: whether to block players from damaging their own guards.
     */
    public boolean noGuardDamage;

    /**
     * Configuration option: whether to protect Sentinel NPCs from being burned by the sun.
     */
    public boolean blockSunburn;

    /**
     * Configuration option: whether to prevent Sentinel weapons causing damage to blocks.
     */
    public boolean preventExplosionBlockDamage;

    /**
     * Configuration option: time until arrow cleanup.
     */
    public int arrowCleanupTime;

    /**
     * Whether WorldGuard support is loaded and enabled.
     */
    public boolean hasWorldGuard;

    /**
     * Whether to use smarter LOS tracing.
     */
    public boolean enhanceLosTraces;

    /**
     * Whether NPCs should be able to target armor stands.
     */
    public boolean allowArmorStandTargets;

    /**
     * Fills the {@code vaultPerms} object if possible.
     */
    public void tryGetPerms() {
        if (Bukkit.getServer().getPluginManager().getPlugin("Vault") == null) {
            return;
        }
        try {
            RegisteredServiceProvider<Permission> rsp = Bukkit.getServer().getServicesManager().getRegistration(Permission.class);
            vaultPerms = rsp.getProvider();
            getLogger().info("Vault linked! Group targets will work.");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Whether debugging is enabled.
     */
    public static boolean debugMe = false;

    static {
        for (EntityType type : EntityType.values()) {
            entityToTargets.put(type, new HashSet<>());
        }
    }

    /**
     * Registers a new integration to Sentinel.
     */
    public void registerIntegration(SentinelIntegration integration) {
        integrations.add(integration);
        for (String prefix : integration.getTargetPrefixes()) {
            integrationPrefixMap.put(prefix, integration);
            SentinelCommand.addTargetTabCompletions.add(prefix + ":");
        }
    }

    private double findMaxHealth() {
        try {
            Class<?> clazz = Class.forName("org.spigotmc.SpigotConfig");
            Field maxHealthField = clazz.getField("maxHealth");
            maxHealthField.setAccessible(true);
            return maxHealthField.getDouble(null);
        }
        catch (Throwable ex) {
            getLogger().warning("Cannot find SpigotConfig to read the server's max health value.");
            ex.printStackTrace();
        }
        return getConfig().getDouble("random.max health", 2000);
    }

    /**
     * Reloads the config and updates settings fields accordingly.
     */
    public void loadConfigSettings() {
        reloadConfig();
        cleverTicks = getConfig().getInt("random.clever ticks", 10);
        canUseSkull = getConfig().getBoolean("random.skull allowed", true);
        blockEvents = getConfig().getBoolean("random.workaround bukkit events", false);
        alternateDamage = getConfig().getBoolean("random.enforce damage", false);
        workaroundDamage = getConfig().getBoolean("random.workaround damage", false);
        doNativeAttack = getConfig().getBoolean("random.do native attacks", true);
        minShootSpeed = getConfig().getDouble("random.shoot speed minimum", 20);
        workaroundDrops = getConfig().getBoolean("random.workaround drops", false) || blockEvents;
        deathMessages = getConfig().getBoolean("random.death messages", true);
        enhanceLosTraces = getConfig().getBoolean("random.enhance los traces", true);
        try {
            spectralSound = Sound.valueOf(getConfig().getString("random.spectral sound", "ENTITY_VILLAGER_YES"));
        }
        catch (Throwable e) {
            getLogger().warning("Sentinel Configuration value 'random.spectral sound' is set to an invalid sound name. This is usually an ignorable issue.");
        }
        ignoreInvisible = getConfig().getBoolean("random.ignore invisible targets");
        guardDistanceMinimum = getConfig().getInt("random.guard follow distance.minimum", 7);
        guardDistanceSelectionRange = getConfig().getInt("random.guard follow distance.selection range", 4);
        workaroundEntityChasePathfinder = getConfig().getBoolean("random.workaround entity chase pathfinder", true);
        protectFromIgnores = getConfig().getBoolean("random.protected", false);
        runAwayTime = getConfig().getInt("random.run away time");
        maxHealth = findMaxHealth();
        noGuardDamage = getConfig().getBoolean("random.no guard damage", true);
        arrowCleanupTime = getConfig().getInt("random.arrow cleanup time", 200);
        blockSunburn = getConfig().getBoolean("random.block sunburn", true);
        preventExplosionBlockDamage = getConfig().getBoolean("random.prevent explosion block damage", true);
        tickRate = getConfig().getInt("update rate", 10);
        autoCorrectpathfinderMode = getConfig().getBoolean("random.auto correct pathfinder mode", true);
        allowArmorStandTargets = getConfig().getBoolean("random.allow armor stand targets", false);
    }

    /**
     * Called when the plugin is enabled at server startup.
     */
    @Override
    public void onEnable() {
        getLogger().info("Sentinel loading...");
        instance = this;
        getLogger().info("Running on java version: " + System.getProperty("java.version"));
        SentinelVersionCompat.init();
        CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(SentinelTrait.class).withName("sentinel"));
        saveDefaultConfig();
        try {
            // Automatic config file update
            InputStream properConfig = SentinelPlugin.class.getResourceAsStream("/config.yml");
            String properConfigString = SentinelUtilities.streamToString(properConfig);
            properConfig.close();
            FileInputStream currentConfig = new FileInputStream(getDataFolder() + "/config.yml");
            String currentConfigString = SentinelUtilities.streamToString(currentConfig);
            currentConfig.close();
            String updated = ConfigUpdater.updateConfig(currentConfigString, properConfigString);
            if (updated != null) {
                getLogger().info("Your config file is outdated. Automatically updating it...");
                FileOutputStream configOutput = new FileOutputStream(getDataFolder() + "/config.yml");
                OutputStreamWriter writer = new OutputStreamWriter(configOutput);
                writer.write(updated);
                writer.close();
                configOutput.close();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        loadConfigSettings();
        BukkitRunnable postLoad = new BukkitRunnable() {
            @Override
            public void run() {
                for (NPC npc : CitizensAPI.getNPCRegistry()) {
                    if (!npc.isSpawned() && npc.hasTrait(SentinelTrait.class)) {
                        SentinelTrait sentinel = npc.getOrAddTrait(SentinelTrait.class);
                        if (sentinel.respawnTime > 0) {
                            if (sentinel.spawnPoint == null && npc.getStoredLocation() == null) {
                                getLogger().warning("NPC " + npc.getId() + " has a null spawn point and can't be spawned. Perhaps the world was deleted?");
                                continue;
                            }
                            npc.spawn(sentinel.spawnPoint == null ? npc.getStoredLocation() : sentinel.spawnPoint);
                        }
                    }
                }
            }
        };
        postLoad.runTaskLater(this, 40);
        new SentinelCommand().buildCommandHandler(getCommand("sentinel"));
        Bukkit.getPluginManager().registerEvents(new SentinelEventHandler(), this);
        // bstats.org
        try {
            BStatsMetricsLite metrics = new BStatsMetricsLite(this);
        }
        catch (Throwable e) {
            e.printStackTrace();
        }
        // neo.mcmonkey.org
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!getConfig().getBoolean("stats_opt_out", false)) {
                    StatsRecord.trigger();
                }
            }
        }.runTaskTimer(this, 200, 20 * 60 * 60);
        tryGetPerms();
        registerIntegration(new SentinelHealth());
        registerIntegration(new SentinelNPCOwnedBy());
        registerIntegration(new SentinelPermissions());
        registerIntegration(new SentinelPotion());
        registerIntegration(new SentinelSBScore());
        registerIntegration(new SentinelSBTeams());
        registerIntegration(new SentinelSquads());
        registerIntegration(new SentinelUUID());
        if (Bukkit.getPluginManager().getPlugin("Towny") != null) {
            try {
                registerIntegration(new SentinelTowny());
                getLogger().info("Sentinel found Towny! Adding support for it!");
            }
            catch (Throwable ex) {
                ex.printStackTrace();
            }
        }
        if (Bukkit.getPluginManager().getPlugin("Factions") != null) {
            boolean canLoad = false;
            try {
                if (Class.forName("com.massivecraft.factions.RelationParticipator") != null) {
                    canLoad = true;
                }
            }
            catch (ClassNotFoundException ex) {
                getLogger().warning("You are running an unsupported fork of Factions that has listed itself as Factions."
                        + " Sentinel will not load the integration for it."
                        + " Please inform the developer of your Factions fork to set the plugin name and classpath to their own plugin, "
                        + "to avoid issues with plugins built for the official version of Factions.");
            }
            if (canLoad) {
                try {
                    registerIntegration(new SentinelFactions());
                    getLogger().info("Sentinel found Factions! Adding support for it!");
                }
                catch (Throwable ex) {
                    ex.printStackTrace();
                }
            }
        }
        if (Bukkit.getPluginManager().getPlugin("CrackShot") != null) {
            try {
                registerIntegration(new SentinelCrackShot());
                getLogger().info("Sentinel found CrackShot! Adding support for it!");
            }
            catch (Throwable ex) {
                ex.printStackTrace();
            }
        }
        if (Bukkit.getPluginManager().getPlugin("SimpleClans") != null) {
            try {
                registerIntegration(new SentinelSimpleClans());
                getLogger().info("Sentinel found SimpleClans! Adding support for it!");
            }
            catch (Throwable ex) {
                ex.printStackTrace();
            }
        }
        if (Bukkit.getPluginManager().getPlugin("War") != null) {
            try {
                registerIntegration(new SentinelWar());
                getLogger().info("Sentinel found War! Adding support for it!");
            }
            catch (Throwable ex) {
                ex.printStackTrace();
            }
        }
        if (Bukkit.getPluginManager().getPlugin("SimplePets") != null) {
            try {
                registerIntegration(new SentinelSimplePets());
                getLogger().info("Sentinel found SimplePets! Adding support for it!");
            }
            catch (Throwable ex) {
                ex.printStackTrace();
            }
        }
        if (Bukkit.getPluginManager().getPlugin("WorldGuard") != null) {
            try {
                new SentinelWorldGuardHelper();
                hasWorldGuard = true;
                getLogger().info("Sentinel found WorldGuard! Adding support for it!");
            }
            catch (Throwable ex) {
                ex.printStackTrace();
            }
        }
        new BukkitRunnable() {
            @Override
            public void run() {
                tickTimeTotal++;
            }
        }.runTaskTimer(this, 1, 1);
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, VelocityTracker::runAll, 20, 20);
        SentinelNMSHelper.init();
        getLogger().info("Sentinel loaded!");
    }

    /**
     * Called when the plugin is disabled at server shutdown.
     */
    @Override
    public void onDisable() {
        getLogger().info("Sentinel unloading...");
        getLogger().info("Sentinel unloaded!");
    }

    /**
     * Gets the Sentinel Trait instance for a given command sender (based on their selected NPC).
     */
    public SentinelTrait getSentinelFor(CommandSender sender) {
        NPC npc = CitizensAPI.getDefaultNPCSelector().getSelected(sender);
        if (npc == null) {
            return null;
        }
        return npc.getTraitNullable(SentinelTrait.class);
    }

    /**
     * Gets the owner identity of an NPC for output (player name or "server").
     */
    public String getOwner(NPC npc) {
        return getOwner(npc, "Server/Unknown");
    }

    /**
     * Gets the owner identity of an NPC for output (player name or default String value).
     */
    public String getOwner(NPC npc, String defName) {
        Owner trait = npc.getOrAddTrait(Owner.class);
        if (trait.getOwnerId() == null) {
            return trait.getOwner();
        }
        OfflinePlayer player = Bukkit.getOfflinePlayer(trait.getOwnerId());
        if (player == null || !player.hasPlayedBefore() || player.getName() == null) {
            return defName;
        }
        return player.getName();
    }
}
