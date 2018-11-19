package org.mcmonkey.sentinel;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.TraitInfo;
import net.citizensnpcs.api.trait.trait.Owner;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.mcmonkey.sentinel.integration.*;
import org.mcmonkey.sentinel.metrics.BStatsMetricsLite;
import org.mcmonkey.sentinel.metrics.MetricsLite;
import org.mcmonkey.sentinel.metrics.StatsRecord;
import org.mcmonkey.sentinel.targeting.SentinelTarget;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * The main Sentinel plugin class.
 */
public class SentinelPlugin extends JavaPlugin implements Listener {

    /**
     * A map of typeable target names to valid targets.
     */
    public static HashMap<String, SentinelTarget> targetOptions = new HashMap<>();

    /**
     * A map of entity types to target types.
     */
    public static HashMap<EntityType, HashSet<SentinelTarget>> entityToTargets = new HashMap<>();

    /**
     * Current plugin instance.
     */
    public static SentinelPlugin instance;

    /**
     * Permissions handler.
     */
    public Permission vaultPerms;

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
    public int guardDistanceMinimum, guardDistanceSelectionRange, guardDistanceMargin;

    /**
     * Configuration option: whether to work-around a pathfinder issue.
     */
    public boolean workaroundEntityChasePathfinder;

    /**
     * Configuration option: whether to protect the NPC from being harmed by ignored entities.
     */
    public boolean protectFromIgnores;

    /**
     * Configuration option: Standard tick-rate for NPC updates.
     */
    public int tickRate = 10;

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

    /**
     * Expected configuration file version.
     */
    public final static int CONFIG_VERSION;

    static {
        for (EntityType type : EntityType.values()) {
            entityToTargets.put(type, new HashSet<>());
        }
        int confVer;
        try {
            InputStream inputConfigStream = SentinelPlugin.class.getResourceAsStream("/config.yml");
            InputStreamReader inputConfigReader = new InputStreamReader(inputConfigStream);
            YamlConfiguration config = YamlConfiguration.loadConfiguration(inputConfigReader);
            inputConfigReader.close();
            inputConfigStream.close();
            confVer = config.getInt("config version");
        }
        catch (Throwable ex) {
            ex.printStackTrace();
            confVer = -1;
        }
        CONFIG_VERSION = confVer;
    }

    /**
     * All current integrations available to Sentinel.
     */
    public final static ArrayList<SentinelIntegration> integrations = new ArrayList<>();

    /**
     * Called when the plugin is enabled at server startup.
     */
    @Override
    public void onEnable() {
        getLogger().info("Sentinel loading...");
        instance = this;
        CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(SentinelTrait.class).withName("sentinel"));
        saveDefaultConfig();
        int confVer = getConfig().getInt("config version", -1);
        if (confVer != CONFIG_VERSION) {
            if (CONFIG_VERSION == -1) {
                getLogger().warning("Default config data could not be found. May be a jar file issue?");
            }
            else if (confVer == -1) {
                getLogger().warning("Config data could not be found. May be an issue with your server plugins folder? (Check file access permissions).");
            }
            else {
                getLogger().warning("Outdated or invalid Sentinel config - please delete it to regenerate it (keep a backup copy of the original)!"
                        + " Expected version " + CONFIG_VERSION + " but you have " + confVer + ".");
            }
        }
        cleverTicks = getConfig().getInt("random.clever ticks", 10);
        canUseSkull = getConfig().getBoolean("random.skull allowed", true);
        blockEvents = getConfig().getBoolean("random.workaround bukkit events", false);
        alternateDamage = getConfig().getBoolean("random.enforce damage", false);
        workaroundDamage = getConfig().getBoolean("random.workaround damage", false);
        minShootSpeed = getConfig().getDouble("random.shoot speed minimum", 20);
        workaroundDrops = getConfig().getBoolean("random.workaround drops", false) || blockEvents;
        deathMessages = getConfig().getBoolean("random.death messages", true);
        try {
            spectralSound = Sound.valueOf(getConfig().getString("random.spectral sound", "ENTITY_VILLAGER_YES"));
        }
        catch (Throwable e) {
            getLogger().warning("Sentinel Configuration value 'random.spectral sound' is set to an invalid sound name. This is usually an ignorable issue.");
        }
        ignoreInvisible = getConfig().getBoolean("random.ignore invisible targets");
        guardDistanceMinimum = getConfig().getInt("random.guard follow distance.minimum", 7);
        guardDistanceMargin = getConfig().getInt("random.guard follow distance.selction range", 4);
        guardDistanceSelectionRange = getConfig().getInt("random.guard follow distance.margin", 2);
        workaroundEntityChasePathfinder = getConfig().getBoolean("random.workaround entity chase pathfinder", false);
        protectFromIgnores = getConfig().getBoolean("random.protected", false);
        BukkitRunnable postLoad = new BukkitRunnable() {
            @Override
            public void run() {
                for (NPC npc : CitizensAPI.getNPCRegistry()) {
                    if (!npc.isSpawned() && npc.hasTrait(SentinelTrait.class)) {
                        SentinelTrait sentinel = npc.getTrait(SentinelTrait.class);
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
        maxHealth = getConfig().getDouble("random.max health", 2000);
        tickRate = getConfig().getInt("update rate", 10);
        getLogger().info("Sentinel loaded!");
        getServer().getPluginManager().registerEvents(this, this);
        // mcstats.org
        try {
            MetricsLite metrics = new MetricsLite(this);
            metrics.start();
        }
        catch (Throwable e) {
            e.printStackTrace();
        }
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
                    new StatsRecord().start();
                }
            }
        }.runTaskTimer(this, 100, 20 * 60 * 60);
        tryGetPerms();
        integrations.add(new SentinelHealth());
        integrations.add(new SentinelPermissions());
        integrations.add(new SentinelSBTeams());
        integrations.add(new SentinelSquads());
        if (Bukkit.getPluginManager().getPlugin("Towny") != null) {
            try {
                integrations.add(new SentinelTowny());
                getLogger().info("Sentinel found Towny! Adding support for it!");
            }
            catch (Throwable ex) {
                ex.printStackTrace();
            }
        }
        if (Bukkit.getPluginManager().getPlugin("Factions") != null) {
            try {
                integrations.add(new SentinelFactions());
                getLogger().info("Sentinel found Factions! Adding support for it!");
            }
            catch (Throwable ex) {
                ex.printStackTrace();
            }
        }
        if (Bukkit.getPluginManager().getPlugin("CrackShot") != null) {
            try {
                integrations.add(new SentinelCrackShot());
                getLogger().info("Sentinel found CrackShot! Adding support for it!");
            }
            catch (Throwable ex) {
                ex.printStackTrace();
            }
        }
        if (Bukkit.getPluginManager().getPlugin("SimpleClans") != null) {
            try {
                integrations.add(new SentinelSimpleClans());
                getLogger().info("Sentinel found SimpleClans! Adding support for it!");
            }
            catch (Throwable ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * Prefix string for an inventory title.
     */
    final static String InvPrefix = ChatColor.GREEN + "Sentinel ";

    /**
     * Called when an inventory is closed.
     */
    @EventHandler
    public void onInvClose(InventoryCloseEvent event) {
        if (event.getInventory().getTitle().startsWith(InvPrefix)) {
            int id = Integer.parseInt(event.getInventory().getTitle().substring(InvPrefix.length()));
            NPC npc = CitizensAPI.getNPCRegistry().getById(id);
            if (npc != null && npc.hasTrait(SentinelTrait.class)) {
                ArrayList<ItemStack> its = npc.getTrait(SentinelTrait.class).drops;
                its.clear();
                for (ItemStack it : event.getInventory().getContents()) {
                    if (it != null && it.getType() != Material.AIR) {
                        its.add(it);
                    }
                }
            }
        }
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
        if (npc.hasTrait(SentinelTrait.class)) {
            return npc.getTrait(SentinelTrait.class);
        }
        return null;
    }

    /**
     * Handles a command given by a player or the server.
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        return SentinelCommand.onCommand(this, sender, command, label, args);
    }

    /**
     * Gets the owner identity of an NPC for output (player name or "server").
     */
    public String getOwner(NPC npc) {
        if (npc.getTrait(Owner.class).getOwnerId() == null) {
            return npc.getTrait(Owner.class).getOwner();
        }
        OfflinePlayer player = Bukkit.getOfflinePlayer(npc.getTrait(Owner.class).getOwnerId());
        if (player == null) {
            return "Server/Unknown";
        }
        return player.getName();
    }
}
