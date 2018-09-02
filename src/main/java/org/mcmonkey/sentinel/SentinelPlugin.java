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
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.mcmonkey.sentinel.external.SentryImport;
import org.mcmonkey.sentinel.integration.*;
import org.mcmonkey.sentinel.metrics.MetricsLite;
import org.mcmonkey.sentinel.metrics.StatsRecord;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

public class SentinelPlugin extends JavaPlugin implements Listener {

    public static final String ColorBasic = ChatColor.YELLOW.toString();

    public static final String prefixGood = ChatColor.DARK_GREEN + "[Sentinel] " + ColorBasic;

    public static final String prefixBad = ChatColor.DARK_GREEN + "[Sentinel] " + ChatColor.RED;

    static HashMap<String, SentinelTarget> targetOptions = new HashMap<>();

    static HashMap<EntityType, HashSet<SentinelTarget>> entityToTargets = new HashMap<>();

    public static SentinelPlugin instance;

    public Permission vaultPerms;

    public double maxHealth;

    public int cleverTicks;

    public boolean canUseSkull;

    public boolean blockEvents;

    public boolean alternateDamage;

    public boolean workaroundDamage;

    public double minShootSpeed;

    public boolean workaroundDrops;

    public boolean deathMessages;

    public Sound spectralSound;

    public boolean ignoreInvisible;

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

    public static boolean debugMe = false;

    public int tickRate = 10;

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

    public final static List<SentinelIntegration> integrations = new ArrayList<>();

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
        workaroundDrops = getConfig().getBoolean("random.workaround drops", false);
        deathMessages = getConfig().getBoolean("random.death messages", true);
        spectralSound = Sound.valueOf(getConfig().getString("random.spectral sound", "ENTITY_VILLAGER_YES"));
        ignoreInvisible = getConfig().getBoolean("random.ignore invisible targets");
        BukkitRunnable postLoad = new BukkitRunnable() {
            @Override
            public void run() {
                for (NPC npc : CitizensAPI.getNPCRegistry()) {
                    if (!npc.isSpawned() && npc.hasTrait(SentinelTrait.class)) {
                        SentinelTrait sentinel = npc.getTrait(SentinelTrait.class);
                        for (String target : new HashSet<>(sentinel.targets)) {
                            sentinel.targets.add(SentinelTarget.forName(target).name());
                        }
                        for (String target : new HashSet<>(sentinel.ignores)) {
                            sentinel.ignores.add(SentinelTarget.forName(target).name());
                        }
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
        maxHealth = getConfig().getDouble("random.max health", 2000);
        postLoad.runTaskLater(this, 40);
        tickRate = getConfig().getInt("update rate", 10);
        getLogger().info("Sentinel loaded!");
        getServer().getPluginManager().registerEvents(this, this);
        try {
            MetricsLite metrics = new MetricsLite(this);
            metrics.start();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
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

    final static String InvPrefix = ChatColor.GREEN + "Sentinel ";

    @EventHandler
    public void onInvClose(InventoryCloseEvent event) {
        if (event.getInventory().getTitle().startsWith(InvPrefix)) {
            int id = Integer.parseInt(event.getInventory().getTitle().substring(InvPrefix.length()));
            NPC npc = CitizensAPI.getNPCRegistry().getById(id);
            if (npc != null && npc.hasTrait(SentinelTrait.class)) {
                List<ItemStack> its = npc.getTrait(SentinelTrait.class).drops;
                its.clear();
                for (ItemStack it : event.getInventory().getContents()) {
                    if (it != null && it.getType() != Material.AIR) {
                        its.add(it);
                    }
                }
            }
        }
    }

    @Override
    public void onDisable() {
        getLogger().info("Sentinel unloading...");
        getLogger().info("Sentinel unloaded!");
    }

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

    private long ignoreMe = 0;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String arg0 = args.length > 0 ? args[0].toLowerCase() : "help";
        SentinelTrait sentinel = getSentinelFor(sender);
        if (arg0.equals("sentryimport") && sender.hasPermission("sentinel.sentryimport")) {
            if (Bukkit.getServer().getPluginManager().getPlugin("Sentry") == null) {
                sender.sendMessage(prefixBad + "Sentry plugin must be installed to perform import!");
            }
            else {
                sender.sendMessage(prefixGood + "Converting all NPCs from Sentry to Sentinel...");
                int imported = SentryImport.PerformImport();
                sender.sendMessage(prefixGood + "Imported " + imported + " Sentry NPCs. You may now restart and remove the Sentry plugin.");
            }
            return true;
        }
        else if (sentinel == null && !arg0.equals("help") && !arg0.equals("debug")) {
            sender.sendMessage(prefixBad + "Must have a Sentinel NPC selected! Use /trait sentinel to ensure an NPC becomes a Sentinel.");
            return true;
        }
        else if (arg0.equals("addtarget") && sender.hasPermission("sentinel.addtarget") && args.length > 1) {
            SentinelTarget target = SentinelTarget.forName(args[1].toUpperCase());
            if (target == null) {
                String[] info = args[1].split(":", 2);
                if (info.length > 1) {
                    info[1] = ChatColor.translateAlternateColorCodes('&', info[1]);
                    List<String> names;
                    boolean doRegex = true;
                    if (info[0].equalsIgnoreCase("player")) {
                        names = sentinel.playerNameTargets;
                    }
                    else if (info[0].equalsIgnoreCase("npc")) {
                        names = sentinel.npcNameTargets;
                    }
                    else if (info[0].equalsIgnoreCase("entityname")) {
                        names = sentinel.entityNameTargets;
                    }
                    else if (info[0].equalsIgnoreCase("helditem")) {
                        names = sentinel.heldItemTargets;
                    }
                    else if (info[0].equalsIgnoreCase("group")) {
                        names = sentinel.groupTargets;
                        if (names.contains(info[1])) {
                            sender.sendMessage(prefixBad + "Already tracking that name target!");
                        }
                        else {
                            names.add(info[1]);
                            sender.sendMessage(prefixGood + "Tracking new target!");
                        }
                        return true;
                    }
                    else if (info[0].equalsIgnoreCase("event")) {
                        info[1] = info[1].toLowerCase();
                        names = sentinel.eventTargets;
                    }
                    else {
                        doRegex = false;
                        names = sentinel.otherTargets;
                        info[1] = info[0].toLowerCase() + ":" + info[1];
                    }
                    try {
                        if (doRegex && "Sentinel".matches(info[1])) {
                            ignoreMe++;
                        }
                    }
                    catch (Exception e) {
                        names = null;
                        sender.sendMessage(prefixBad + "Bad regular expression!");
                    }
                    if (names != null) {
                        if (names.contains(info[1])) {
                            sender.sendMessage(prefixBad + "Already tracking that target!");
                        }
                        else {
                            names.add(info[1]);
                            sender.sendMessage(prefixGood + "Tracking new target!");
                        }
                        return true;
                    }
                }
                sender.sendMessage(prefixBad + "Invalid target!");
                StringBuilder valid = new StringBuilder();
                for (String poss : targetOptions.keySet()) {
                    valid.append(poss).append(", ");
                }
                sender.sendMessage(prefixGood + "Valid targets: " + valid.substring(0, valid.length() - 2));
                sender.sendMessage(prefixGood + "Also allowed: player:NAME(REGEX), npc:NAME(REGEX), entityname:NAME(REGEX),"
                        + "helditem:MATERIALNAME(REGEX), group:GROUPNAME(EXACT), event:pvp/pvnpc/pve");
                for (SentinelIntegration si : integrations) {
                    sender.sendMessage(prefixGood + "Also: " + si.getTargetHelp());
                }
            }
            else {
                if (sentinel.targets.add(target.name())) {
                    sender.sendMessage(prefixGood + "Target added!");
                }
                else {
                    sender.sendMessage(prefixBad + "Target already added!");
                }
            }
            return true;
        }
        else if (arg0.equals("removetarget") && sender.hasPermission("sentinel.removetarget") && args.length > 1) {
            SentinelTarget target = SentinelTarget.forName(args[1].toUpperCase());
            if (target == null) {
                String[] info = args[1].split(":", 2);
                if (info.length > 1) {
                    info[1] = ChatColor.translateAlternateColorCodes('&', info[1]);
                    List<String> names;
                    boolean doRegex = true;
                    if (info[0].equalsIgnoreCase("player")) {
                        names = sentinel.playerNameTargets;
                    }
                    else if (info[0].equalsIgnoreCase("npc")) {
                        names = sentinel.npcNameTargets;
                    }
                    else if (info[0].equalsIgnoreCase("entityname")) {
                        names = sentinel.entityNameTargets;
                    }
                    else if (info[0].equalsIgnoreCase("helditem")) {
                        names = sentinel.heldItemTargets;
                    }
                    else if (info[0].equalsIgnoreCase("group")) {
                        names = sentinel.groupTargets;
                        if (!names.remove(info[1])) {
                            sender.sendMessage(prefixBad + "Not tracking that target!");
                        }
                        else {
                            sender.sendMessage(prefixGood + "No longer tracking that target!");
                        }
                        return true;
                    }
                    else if (info[0].equalsIgnoreCase("event")) {
                        info[1] = info[1].toLowerCase();
                        names = sentinel.eventTargets;
                    }
                    else {
                        doRegex = false;
                        names = sentinel.otherTargets;
                        info[1] = info[0].toLowerCase() + ":" + info[1];
                    }
                    try {
                        if (doRegex && "Sentinel".matches(info[1])) {
                            ignoreMe++;
                        }
                    }
                    catch (Exception e) {
                        names = null;
                        sender.sendMessage(prefixBad + "Bad regular expression!");
                    }
                    if (names != null) {
                        if (!names.remove(info[1])) {
                            sender.sendMessage(prefixBad + "Not tracking that target!");
                        }
                        else {
                            sender.sendMessage(prefixGood + "No longer tracking that target!");
                        }
                        return true;
                    }
                }
                sender.sendMessage(prefixBad + "Invalid target!");
                sender.sendMessage(prefixGood + "See '/sentinel addtarget help' to view valid targets!");
            }
            else {
                if (sentinel.targets.remove(target.name())) {
                    sender.sendMessage(prefixGood + "Target removed!");
                }
                else {
                    sender.sendMessage(prefixBad + "Target not added!");
                }
            }
            return true;
        }
        else if (arg0.equals("addignore") && sender.hasPermission("sentinel.addignore") && args.length > 1) {
            SentinelTarget target = SentinelTarget.forName(args[1].toUpperCase());
            if (target == null) {
                String[] info = args[1].split(":", 2);
                if (info.length > 1) {
                    info[1] = ChatColor.translateAlternateColorCodes('&', info[1]);
                    List<String> names;
                    boolean doRegex = true;
                    if (info[0].equalsIgnoreCase("player")) {
                        names = sentinel.playerNameIgnores;
                    }
                    else if (info[0].equalsIgnoreCase("npc")) {
                        names = sentinel.npcNameIgnores;
                    }
                    else if (info[0].equalsIgnoreCase("entityname")) {
                        names = sentinel.entityNameIgnores;
                    }
                    else if (info[0].equalsIgnoreCase("helditem")) {
                        names = sentinel.heldItemIgnores;
                    }
                    else if (info[0].equalsIgnoreCase("group")) {
                        names = sentinel.groupIgnores;
                        if (names.contains(info[1])) {
                            sender.sendMessage(prefixBad + "Already ignoring that target!");
                        }
                        else {
                            names.add(info[1]);
                            sender.sendMessage(prefixGood + "Ignoring new target!");
                        }
                        return true;
                    }
                    else {
                        doRegex = false;
                        names = sentinel.otherIgnores;
                        info[1] = info[0].toLowerCase() + ":" + info[1];
                    }
                    try {
                        if (doRegex && "Sentinel".matches(info[1])) {
                            ignoreMe++;
                        }
                    }
                    catch (Exception e) {
                        names = null;
                        sender.sendMessage(prefixBad + "Bad regular expression!");
                    }
                    if (names != null) {
                        if (names.contains(info[1])) {
                            sender.sendMessage(prefixBad + "Already ignoring that target!");
                        }
                        else {
                            names.add(info[1]);
                            sender.sendMessage(prefixGood + "Ignoring new target!");
                        }
                        return true;
                    }
                }
                sender.sendMessage(prefixBad + "Invalid ignore target!");
                sender.sendMessage(prefixGood + "See '/sentinel addtarget help' to view valid targets!");
            }
            else {
                if (sentinel.ignores.add(target.name())) {
                    sender.sendMessage(prefixGood + "Ignore added!");
                }
                else {
                    sender.sendMessage(prefixBad + "Ignore already added!");
                }
            }
            return true;
        }
        else if (arg0.equals("removeignore") && sender.hasPermission("sentinel.removeignore") && args.length > 1) {
            SentinelTarget target = SentinelTarget.forName(args[1].toUpperCase());
            if (target == null) {
                String[] info = args[1].split(":", 2);
                if (info.length > 1) {
                    info[1] = ChatColor.translateAlternateColorCodes('&', info[1]);
                    List<String> names;
                    boolean doRegex = true;
                    if (info[0].equalsIgnoreCase("player")) {
                        names = sentinel.playerNameIgnores;
                    }
                    else if (info[0].equalsIgnoreCase("npc")) {
                        names = sentinel.npcNameIgnores;
                    }
                    else if (info[0].equalsIgnoreCase("entityname")) {
                        names = sentinel.entityNameIgnores;
                    }
                    else if (info[0].equalsIgnoreCase("helditem")) {
                        names = sentinel.heldItemIgnores;
                    }
                    else if (info[0].equalsIgnoreCase("group")) {
                        names = sentinel.groupIgnores;
                        if (!names.remove(info[1])) {
                            sender.sendMessage(prefixBad + "Was not ignoring that target!");
                        }
                        else {
                            sender.sendMessage(prefixGood + "Not ignoring that target along longer!");
                        }
                        return true;
                    }
                    else {
                        doRegex = false;
                        names = sentinel.otherIgnores;
                        info[1] = info[0].toLowerCase() + ":" + info[1];
                    }
                    try {
                        if (doRegex && "Sentinel".matches(info[1])) {
                            ignoreMe++;
                        }
                    }
                    catch (Exception e) {
                        names = null;
                        sender.sendMessage(prefixBad + "Bad regular expression!");
                    }
                    if (names != null) {
                        if (!names.remove(info[1])) {
                            sender.sendMessage(prefixBad + "Was not ignoring that target!");
                        }
                        else {
                            sender.sendMessage(prefixGood + "Not ignoring that target along longer!");
                        }
                        return true;
                    }
                }
                sender.sendMessage(prefixBad + "Invalid ignore target!");
                sender.sendMessage(prefixGood + "See '/sentinel addtarget help' to view valid targets!");
            }
            else {
                if (sentinel.ignores.remove(target.name())) {
                    sender.sendMessage(prefixGood + "Ignore removed!");
                }
                else {
                    sender.sendMessage(prefixBad + "Ignore not added!");
                }
            }
            return true;
        }
        else if (arg0.equals("range") && sender.hasPermission("sentinel.range") && args.length > 1) {
            try {
                Double d = Double.parseDouble(args[1]);
                if (d > 0 && d < 200) {
                    sentinel.range = d;
                    sender.sendMessage(prefixGood + "Range set!");
                }
                else {
                    throw new NumberFormatException("Number out of range.");
                }
            }
            catch (NumberFormatException ex) {
                sender.sendMessage(prefixBad + "Invalid range number: " + ex.getMessage());
            }
            return true;
        }
        else if (arg0.equals("damage") && sender.hasPermission("sentinel.damage") && args.length > 1) {
            try {
                Double d = Double.parseDouble(args[1]);
                if (d < 1000) {
                    sentinel.damage = d;
                    sender.sendMessage(prefixGood + "Damage set!");
                }
                else {
                    throw new NumberFormatException("Number out of range.");
                }
            }
            catch (NumberFormatException ex) {
                sender.sendMessage(prefixBad + "Invalid damage number: " + ex.getMessage());
            }
            return true;
        }
        else if (arg0.equals("speed") && sender.hasPermission("sentinel.speed") && args.length > 1) {
            try {
                Double d = Double.parseDouble(args[1]);
                if (d < 1000 && d >= 0) {
                    sentinel.speed = d;
                    sender.sendMessage(prefixGood + "Speed set!");
                }
                else {
                    throw new NumberFormatException("Number out of range.");
                }
            }
            catch (NumberFormatException ex) {
                sender.sendMessage(prefixBad + "Invalid speed number: " + ex.getMessage());
            }
            return true;
        }
        else if (arg0.equals("greetrange") && sender.hasPermission("sentinel.speed") && args.length > 1) {
            try {
                Double d = Double.parseDouble(args[1]);
                if (d < 100) {
                    sentinel.greetRange = d;
                    sender.sendMessage(prefixGood + "Range set!");
                }
                else {
                    throw new NumberFormatException("Number out of range.");
                }
            }
            catch (NumberFormatException ex) {
                sender.sendMessage(prefixBad + "Invalid range number: " + ex.getMessage());
            }
            return true;
        }
        else if (arg0.equals("armor") && sender.hasPermission("sentinel.armor") && args.length > 1) {
            try {
                Double d = Double.parseDouble(args[1]);
                if (d <= 1) {
                    sentinel.armor = d;
                    sender.sendMessage(prefixGood + "Armor set!");
                }
                else {
                    throw new NumberFormatException("Number out of range.");
                }
            }
            catch (NumberFormatException ex) {
                sender.sendMessage(prefixBad + "Invalid armor number: " + ex.getMessage());
            }
            return true;
        }
        else if (arg0.equals("health") && sender.hasPermission("sentinel.health") && args.length > 1) {
            try {
                Double d = Double.parseDouble(args[1]);
                if ((d >= SentinelTrait.healthMin) && (d <= maxHealth)) {
                    sentinel.setHealth(d);
                    sender.sendMessage(prefixGood + "Health set!");
                }
                else {
                    throw new NumberFormatException("Number out of range.");
                }
            }
            catch (NumberFormatException ex) {
                sender.sendMessage(prefixBad + "Invalid health number: " + ex.getMessage());
            }
            return true;
        }
        else if (arg0.equals("attackrate") && sender.hasPermission("sentinel.attackrate") && args.length > 1) {
            try {
                double da = Double.parseDouble(args[1]);
                int d = (int)(da * 20);
                if (d >= tickRate && d <= SentinelTrait.attackRateMax) {
                    if (args.length > 2 && args[2].contains("ranged")) {
                        sentinel.attackRateRanged = d;
                        sender.sendMessage(prefixGood + "Ranged attack rate set!");
                    }
                    else {
                        sentinel.attackRate = d;
                        sender.sendMessage(prefixGood + "Attack rate set!");
                    }
                }
                else {
                    throw new NumberFormatException("Number out of range.");
                }
            }
            catch (NumberFormatException ex) {
                sender.sendMessage(prefixBad + "Invalid rate number: " + ex.getMessage());
            }
            return true;
        }
        else if (arg0.equals("healrate") && sender.hasPermission("sentinel.healrate") && args.length > 1) {
            try {
                double da = Double.parseDouble(args[1]);
                int d = (int)(da * 20);
                if ((d >= tickRate && d <= SentinelTrait.healRateMax) || d == 0) {
                    sentinel.healRate = d;
                    sender.sendMessage(prefixGood + "Heal rate set!");
                }
                else {
                    throw new NumberFormatException("Number out of range.");
                }
            }
            catch (NumberFormatException ex) {
                sender.sendMessage(prefixBad + "Invalid rate number: " + ex.getMessage());
            }
            return true;
        }
        else if (arg0.equals("targettime") && sender.hasPermission("sentinel.targettime") && args.length > 1) {
            try {
                double d = Double.parseDouble(args[1]);
                if (d >= 0) {
                    sentinel.enemyTargetTime = (int)(d * 20);
                    sender.sendMessage(prefixGood + "Target time set!");
                }
                else {
                    throw new NumberFormatException("Number out of range.");
                }
            }
            catch (NumberFormatException ex) {
                sender.sendMessage(prefixBad + "Invalid time number: " + ex.getMessage());
            }
            return true;
        }
        else if (arg0.equals("respawntime") && sender.hasPermission("sentinel.respawntime") && args.length > 1) {
            try {
                double d = Double.parseDouble(args[1]);
                sentinel.respawnTime = (long)(d * 20);
                sender.sendMessage(prefixGood + "Respawn time set!");
            }
            catch (NumberFormatException ex) {
                sender.sendMessage(prefixBad + "Invalid time number: " + ex.getMessage());
            }
            return true;
        }
        else if (arg0.equals("chaserange") && sender.hasPermission("sentinel.chaserange") && args.length > 1) {
            try {
                double d = Double.parseDouble(args[1]);
                sentinel.chaseRange = d;
                sender.sendMessage(prefixGood + "Chase range set!");
            }
            catch (NumberFormatException ex) {
                sender.sendMessage(prefixBad + "Invalid range number!");
            }
            return true;
        }
        else if (arg0.equals("accuracy") && sender.hasPermission("sentinel.accuracy") && args.length > 1) {
            try {
                double d = Double.parseDouble(args[1]);
                if (d < 0 || d > 10) {
                    throw new NumberFormatException("Number out of range!");
                }
                else {
                    sentinel.accuracy = d;
                    sender.sendMessage(prefixGood + "Accuracy offset set!");
                }
            }
            catch (NumberFormatException ex) {
                sender.sendMessage(prefixBad + "Invalid accuracy offset number: " + ex.getMessage());
            }
            return true;
        }
        else if (arg0.equals("reach") && sender.hasPermission("sentinel.reach") && args.length > 1) {
            try {
                double d = Double.parseDouble(args[1]);
                if (d < 0) {
                    throw new NumberFormatException("Number out of range!");
                }
                else {
                    sentinel.reach = d;
                    sender.sendMessage(prefixGood + "Reach set!");
                }
            }
            catch (NumberFormatException ex) {
                sender.sendMessage(prefixBad + "Invalid reach number: " + ex.getMessage());
            }
            return true;
        }
        else if (arg0.equals("invincible") && sender.hasPermission("sentinel.invincible")) {
            boolean mode = !sentinel.invincible;
            if (args.length > 1 && "true".equalsIgnoreCase(args[1])) {
                mode = true;
            }
            if (args.length > 1 && "false".equalsIgnoreCase(args[1])) {
                mode = false;
            }
            sentinel.setInvincible(mode);
            if (sentinel.invincible) {
                sender.sendMessage(prefixGood + "NPC now invincible!");
            }
            else {
                sender.sendMessage(prefixGood + "NPC no longer invincible!");
            }
            return true;
        }
        else if (arg0.equals("autoswitch") && sender.hasPermission("sentinel.autoswitch")) {
            sentinel.autoswitch = !sentinel.autoswitch;
            if (sentinel.autoswitch) {
                sender.sendMessage(prefixGood + "NPC now automatically switches items!");
            }
            else {
                sender.sendMessage(prefixGood + "NPC no longer automatically switches items!");
            }
            return true;
        }
        else if (arg0.equals("realistic") && sender.hasPermission("sentinel.realistic")) {
            boolean mode = !sentinel.realistic;
            if (args.length > 1 && "true".equalsIgnoreCase(args[1])) {
                mode = true;
            }
            if (args.length > 1 && "false".equalsIgnoreCase(args[1])) {
                mode = false;
            }
            sentinel.realistic = mode;
            if (sentinel.realistic) {
                sender.sendMessage(prefixGood + "NPC now targets realistically!");
            }
            else {
                sender.sendMessage(prefixGood + "NPC no longer targets realistically!");
            }
            return true;
        }
        else if (arg0.equals("fightback") && sender.hasPermission("sentinel.fightback")) {
            boolean mode = !sentinel.fightback;
            if (args.length > 1 && "true".equalsIgnoreCase(args[1])) {
                mode = true;
            }
            if (args.length > 1 && "false".equalsIgnoreCase(args[1])) {
                mode = false;
            }
            sentinel.fightback = mode;
            if (sentinel.fightback) {
                sender.sendMessage(prefixGood + "NPC now fights back!");
            }
            else {
                sender.sendMessage(prefixGood + "NPC no longer fights back!");
            }
            return true;
        }
        else if (arg0.equals("needammo") && sender.hasPermission("sentinel.needammo")) {
            boolean mode = !sentinel.needsAmmo;
            if (args.length > 1 && "true".equalsIgnoreCase(args[1])) {
                mode = true;
            }
            if (args.length > 1 && "false".equalsIgnoreCase(args[1])) {
                mode = false;
            }
            sentinel.needsAmmo = mode;
            if (sentinel.needsAmmo) {
                sender.sendMessage(prefixGood + "NPC now needs ammo!");
            }
            else {
                sender.sendMessage(prefixGood + "NPC no longer needs ammo!");
            }
            return true;
        }
        else if (arg0.equals("safeshot") && sender.hasPermission("sentinel.safeshot")) {
            boolean mode = !sentinel.safeShot;
            if (args.length > 1 && "true".equalsIgnoreCase(args[1])) {
                mode = true;
            }
            if (args.length > 1 && "false".equalsIgnoreCase(args[1])) {
                mode = false;
            }
            sentinel.safeShot = mode;
            if (sentinel.safeShot) {
                sender.sendMessage(prefixGood + "NPC now is a safe shot!");
            }
            else {
                sender.sendMessage(prefixGood + "NPC is no longer a safe shot!");
            }
            return true;
        }
        else if (arg0.equals("chaseclose") && sender.hasPermission("sentinel.chase")) {
            boolean mode = !sentinel.closeChase;
            if (args.length > 1 && "true".equalsIgnoreCase(args[1])) {
                mode = true;
            }
            if (args.length > 1 && "false".equalsIgnoreCase(args[1])) {
                mode = false;
            }
            sentinel.closeChase = mode;
            if (sentinel.closeChase) {
                sender.sendMessage(prefixGood + "NPC now will chase while close!");
            }
            else {
                sender.sendMessage(prefixGood + "NPC no longer will chase while close!");
            }
            return true;
        }
        else if (arg0.equals("chaseranged") && sender.hasPermission("sentinel.chase")) {
            boolean mode = !sentinel.rangedChase;
            if (args.length > 1 && "true".equalsIgnoreCase(args[1])) {
                mode = true;
            }
            if (args.length > 1 && "false".equalsIgnoreCase(args[1])) {
                mode = false;
            }
            sentinel.rangedChase = mode;
            if (sentinel.rangedChase) {
                sender.sendMessage(prefixGood + "NPC now will chase while ranged!");
            }
            else {
                sender.sendMessage(prefixGood + "NPC no longer will chase while ranged!");
            }
            return true;
        }
        else if (arg0.equals("guard") && sender.hasPermission("sentinel.guard")) {
            if (args.length > 1) {
                Player pl = Bukkit.getPlayer(args[1]);
                sentinel.setGuarding(pl == null ? null : pl.getUniqueId());
            }
            else {
                sentinel.setGuarding(null);
            }
            if (sentinel.getGuarding() == null) {
                sender.sendMessage(prefixGood + "NPC now guarding its area!");
            }
            else {
                sender.sendMessage(prefixGood + "NPC now guarding that player!");
            }
            return true;
        }
        else if (arg0.equals("drops") && sender.hasPermission("sentinel.drops")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(prefixBad + "Players only!");
                return true;
            }
            Inventory inv = Bukkit.createInventory(null, 9 * 4, InvPrefix + sentinel.getNPC().getId());
            ItemStack[] items = new ItemStack[sentinel.drops.size()];
            inv.addItem(sentinel.drops.toArray(items));
            ((Player) sender).openInventory(inv);
            return true;
        }
        else if (arg0.equals("spawnpoint") && sender.hasPermission("sentinel.spawnpoint")) {
            if (!sentinel.getNPC().isSpawned()) {
                sender.sendMessage(prefixBad + "NPC must be spawned for this command!");
            }
            else {
                Location pos = sentinel.getLivingEntity().getLocation().getBlock().getLocation();
                if (sentinel.spawnPoint != null
                        && pos.getBlockX() == sentinel.spawnPoint.getBlockX()
                        && pos.getBlockY() == sentinel.spawnPoint.getBlockY()
                        && pos.getBlockZ() == sentinel.spawnPoint.getBlockZ()
                        && pos.getWorld().getName().equals(sentinel.spawnPoint.getWorld().getName())) {
                    sentinel.spawnPoint = null;
                    sender.sendMessage(prefixGood + "Spawn point removed!");
                }
                else {
                    sentinel.spawnPoint = pos.add(0.5, 0.0, 0.5);
                    sentinel.spawnPoint.setYaw(sentinel.getLivingEntity().getLocation().getYaw());
                    sender.sendMessage(prefixGood + "Spawn point updated!");
                }
            }
            return true;
        }
        else if (arg0.equals("forgive") && sender.hasPermission("sentinel.forgive")) {
            sentinel.currentTargets.clear();
            sentinel.chasing = null;
            sender.sendMessage(prefixGood + "Targets forgiven.");
            return true;
        }
        else if (arg0.equals("enemydrops") && sender.hasPermission("sentinel.enemydrops")) {
            boolean mode = !sentinel.enemyDrops;
            if (args.length > 1 && "true".equalsIgnoreCase(args[1])) {
                mode = true;
            }
            if (args.length > 1 && "false".equalsIgnoreCase(args[1])) {
                mode = false;
            }
            sentinel.enemyDrops = mode;
            if (sentinel.enemyDrops) {
                sender.sendMessage(prefixGood + "NPC enemy mobs now drop items and XP!");
            }
            else {
                sender.sendMessage(prefixGood + "NPC enemy mobs no longer drop items and XP!");
            }
            return true;
        }
        else if (arg0.equals("kill") && sender.hasPermission("sentinel.kill")) {
            if (!sentinel.getNPC().isSpawned()) {
                sender.sendMessage(prefixBad + "NPC is already dead!");
            }
            else {
                sentinel.getLivingEntity().damage(sentinel.health * 2);
                sender.sendMessage(prefixGood + "Killed!");
            }
            return true;
        }
        else if (arg0.equals("respawn") && sender.hasPermission("sentinel.respawn")) {
            Location loc = sentinel.spawnPoint == null ? sentinel.getNPC().getStoredLocation() : sentinel.spawnPoint;
            if (!sentinel.getNPC().spawn(loc)) {
                sentinel.getNPC().teleport(loc, PlayerTeleportEvent.TeleportCause.COMMAND);
            }
            sender.sendMessage(prefixGood + "Respawned!");
            return true;
        }
        else if (arg0.equals("greeting") && sender.hasPermission("sentinel.greet")) {
            sentinel.greetingText = SentinelUtilities.concatWithSpaces(args, 1);
            sender.sendMessage(prefixGood + "Set!");
            return true;
        }
        else if (arg0.equals("warning") && sender.hasPermission("sentinel.greet")) {
            sentinel.warningText = SentinelUtilities.concatWithSpaces(args, 1);
            sender.sendMessage(prefixGood + "Set!");
            return true;
        }
        else if (arg0.equals("squad") && sender.hasPermission("sentinel.squad") && args.length > 1) {
            sentinel.squad = SentinelUtilities.concatWithSpaces(args, 1).toLowerCase(Locale.ENGLISH);
            if (sentinel.squad.equals("null")) {
                sentinel.squad = null;
            }
            sender.sendMessage(prefixGood + "Set!");
            return true;
        }
        else if (arg0.equals("debug") && sender.hasPermission("sentinel.debug")) {
            debugMe = !debugMe;
            sender.sendMessage(prefixGood + "Toggled: " + debugMe + "!");
            return true;
        }
        else if (arg0.equals("targets") && sender.hasPermission("sentinel.info")) {
            sender.sendMessage(prefixGood + ChatColor.RESET + sentinel.getNPC().getFullName() + ColorBasic
                    + ": owned by " + ChatColor.RESET + getOwner(sentinel.getNPC()));
            sender.sendMessage(prefixGood + "Targets: " + ChatColor.AQUA + getTargetString(sentinel.targets));
            sender.sendMessage(prefixGood + "Player Name Targets: " + ChatColor.AQUA + getNameTargetString(sentinel.playerNameTargets));
            sender.sendMessage(prefixGood + "NPC Name Targets: " + ChatColor.AQUA + getNameTargetString(sentinel.npcNameTargets));
            sender.sendMessage(prefixGood + "Entity Name Targets: " + ChatColor.AQUA + getNameTargetString(sentinel.entityNameTargets));
            sender.sendMessage(prefixGood + "Held Item Targets: " + ChatColor.AQUA + getNameTargetString(sentinel.heldItemTargets));
            sender.sendMessage(prefixGood + "Group Targets: " + ChatColor.AQUA + getNameTargetString(sentinel.groupTargets));
            sender.sendMessage(prefixGood + "Event Targets: " + ChatColor.AQUA + getNameTargetString(sentinel.eventTargets));
            sender.sendMessage(prefixGood + "Other Targets: " + ChatColor.AQUA + getNameTargetString(sentinel.otherTargets));
            sender.sendMessage(prefixGood + "Ignored Targets: " + ChatColor.AQUA + getTargetString(sentinel.ignores));
            sender.sendMessage(prefixGood + "Ignored Player Name Targets: " + ChatColor.AQUA + getNameTargetString(sentinel.playerNameIgnores));
            sender.sendMessage(prefixGood + "Ignored NPC Name Targets: " + ChatColor.AQUA + getNameTargetString(sentinel.npcNameIgnores));
            sender.sendMessage(prefixGood + "Ignored Entity Name Targets: " + ChatColor.AQUA + getNameTargetString(sentinel.entityNameIgnores));
            sender.sendMessage(prefixGood + "Ignored Held Item Targets: " + ChatColor.AQUA + getNameTargetString(sentinel.heldItemIgnores));
            sender.sendMessage(prefixGood + "Ignored Group Targets: " + ChatColor.AQUA + getNameTargetString(sentinel.groupIgnores));
            sender.sendMessage(prefixGood + "Ignored Other Targets: " + ChatColor.AQUA + getNameTargetString(sentinel.otherIgnores));
            return true;
        }
        else if (arg0.equals("info") && sender.hasPermission("sentinel.info")) {
            sender.sendMessage(prefixGood + ChatColor.RESET + sentinel.getNPC().getFullName() + ColorBasic
                    + ": owned by " + ChatColor.RESET + getOwner(sentinel.getNPC()) +
                    (sentinel.getGuarding() == null ? "" : ColorBasic + ", guarding: " + ChatColor.RESET + Bukkit.getOfflinePlayer(sentinel.getGuarding()).getName()));
            sender.sendMessage(prefixGood + "Damage: " + ChatColor.AQUA + sentinel.damage);
            sender.sendMessage(prefixGood + "Armor: " + ChatColor.AQUA + sentinel.armor);
            sender.sendMessage(prefixGood + "Health: " + ChatColor.AQUA +
                    (sentinel.getNPC().isSpawned() ? sentinel.getLivingEntity().getHealth() + "/" : "") + sentinel.health);
            sender.sendMessage(prefixGood + "Range: " + ChatColor.AQUA + sentinel.range);
            sender.sendMessage(prefixGood + "Attack Rate: " + ChatColor.AQUA + sentinel.attackRate);
            sender.sendMessage(prefixGood + "Ranged Attack Rate: " + ChatColor.AQUA + sentinel.attackRateRanged);
            sender.sendMessage(prefixGood + "Heal Rate: " + ChatColor.AQUA + sentinel.healRate);
            sender.sendMessage(prefixGood + "Respawn Time: " + ChatColor.AQUA + sentinel.respawnTime);
            sender.sendMessage(prefixGood + "Accuracy: " + ChatColor.AQUA + sentinel.accuracy);
            sender.sendMessage(prefixGood + "Reach: " + ChatColor.AQUA + sentinel.reach);
            sender.sendMessage(prefixGood + "Invincibility Enabled: " + ChatColor.AQUA + sentinel.invincible);
            sender.sendMessage(prefixGood + "Fightback Enabled: " + ChatColor.AQUA + sentinel.fightback);
            sender.sendMessage(prefixGood + "Ranged Chasing Enabled: " + ChatColor.AQUA + sentinel.rangedChase);
            sender.sendMessage(prefixGood + "Close-Quarters Chasing Enabled: " + ChatColor.AQUA + sentinel.closeChase);
            sender.sendMessage(prefixGood + "Maximum chase range: " + ChatColor.AQUA + sentinel.chaseRange);
            sender.sendMessage(prefixGood + "Safe-Shot Enabled: " + ChatColor.AQUA + sentinel.safeShot);
            sender.sendMessage(prefixGood + "Enemy-Drops Enabled: " + ChatColor.AQUA + sentinel.enemyDrops);
            sender.sendMessage(prefixGood + "Autoswitch Enabled: " + ChatColor.AQUA + sentinel.autoswitch);
            sender.sendMessage(prefixGood + "Realistic Targetting Enabled: " + ChatColor.AQUA + sentinel.realistic);
            sender.sendMessage(prefixGood + "Squad: " + ChatColor.AQUA + (sentinel.squad == null ? "None" : sentinel.squad));
            return true;
        }
        else if (arg0.equals("stats") && sender.hasPermission("sentinel.info")) {
            sender.sendMessage(prefixGood + ChatColor.RESET + sentinel.getNPC().getFullName() + ColorBasic
                    + ": owned by " + ChatColor.RESET + getOwner(sentinel.getNPC()));
            sender.sendMessage(prefixGood + "Arrows fired: " + ChatColor.AQUA + sentinel.stats_arrowsFired);
            sender.sendMessage(prefixGood + "Potions thrown: " + ChatColor.AQUA + sentinel.stats_potionsThrown);
            sender.sendMessage(prefixGood + "Fireballs launched: " + ChatColor.AQUA + sentinel.stats_fireballsFired);
            sender.sendMessage(prefixGood + "Snowballs thrown: " + ChatColor.AQUA + sentinel.stats_snowballsThrown);
            sender.sendMessage(prefixGood + "Eggs thrown: " + ChatColor.AQUA + sentinel.stats_eggsThrown);
            sender.sendMessage(prefixGood + "Pearls used: " + ChatColor.AQUA + sentinel.stats_pearlsUsed);
            sender.sendMessage(prefixGood + "Skulls thrown: " + ChatColor.AQUA + sentinel.stats_skullsThrown);
            sender.sendMessage(prefixGood + "Punches: " + ChatColor.AQUA + sentinel.stats_punches);
            sender.sendMessage(prefixGood + "Times spawned: " + ChatColor.AQUA + sentinel.stats_timesSpawned);
            sender.sendMessage(prefixGood + "Damage Given: " + ChatColor.AQUA + sentinel.stats_damageGiven);
            sender.sendMessage(prefixGood + "Damage Taken: " + ChatColor.AQUA + sentinel.stats_damageTaken);
            sender.sendMessage(prefixGood + "Minutes spawned: " + ChatColor.AQUA + sentinel.stats_ticksSpawned / (20.0 * 60.0));
            return true;
        }
        else {
            if (sender.hasPermission("sentinel.basic")) {
                sender.sendMessage(prefixGood + "/sentinel help - Shows help info.");
            }
            if (sender.hasPermission("sentinel.addtarget")) {
                sender.sendMessage(prefixGood + "/sentinel addtarget TYPE - Adds a target.");
            }
            if (sender.hasPermission("sentinel.removetarget")) {
                sender.sendMessage(prefixGood + "/sentinel removetarget TYPE - Removes a target.");
            }
            if (sender.hasPermission("sentinel.addignore")) {
                sender.sendMessage(prefixGood + "/sentinel addignore TYPE - Ignores a target.");
            }
            if (sender.hasPermission("sentinel.removeignore")) {
                sender.sendMessage(prefixGood + "/sentinel removeignore TYPE - Allows targeting a target.");
            }
            if (sender.hasPermission("sentinel.range")) {
                sender.sendMessage(prefixGood + "/sentinel range RANGE - Sets the NPC's maximum attack range.");
            }
            if (sender.hasPermission("sentinel.damage")) {
                sender.sendMessage(prefixGood + "/sentinel damage DAMAGE - Sets the NPC's attack damage.");
            }
            if (sender.hasPermission("sentinel.armor")) {
                sender.sendMessage(prefixGood + "/sentinel armor ARMOR - Sets the NPC's armor level.");
            }
            if (sender.hasPermission("sentinel.health")) {
                sender.sendMessage(prefixGood + "/sentinel health HEALTH - Sets the NPC's health level.");
            }
            if (sender.hasPermission("sentinel.attackrate")) {
                sender.sendMessage(prefixGood + "/sentinel attackrate RATE ['ranged'] - Changes the rate at which the NPC attacks, in ticks. Either ranged or close modes.");
            }
            if (sender.hasPermission("sentinel.healrate")) {
                sender.sendMessage(prefixGood + "/sentinel healrate RATE - Changes the rate at which the NPC heals, in ticks.");
            }
            if (sender.hasPermission("sentinel.respawntime")) {
                sender.sendMessage(prefixGood + "/sentinel respawntime TIME - Changes the time it takes for the NPC to respawn, in ticks.");
            }
            if (sender.hasPermission("sentinel.chaserange")) {
                sender.sendMessage(prefixGood + "/sentinel chaserange RANGE - Changes the maximum distance an NPC will run before returning to base.");
            }
            if (sender.hasPermission("sentinel.guard")) {
                sender.sendMessage(prefixGood + "/sentinel guard (PLAYERNAME) - Makes the NPC guard a specific player. Don't specify a player to stop guarding.");
            }
            if (sender.hasPermission("sentinel.invincible")) {
                sender.sendMessage(prefixGood + "/sentinel invincible - Toggles whether the NPC is invincible.");
            }
            if (sender.hasPermission("sentinel.fightback")) {
                sender.sendMessage(prefixGood + "/sentinel fightback - Toggles whether the NPC will fight back.");
            }
            if (sender.hasPermission("sentinel.needammo")) {
                sender.sendMessage(prefixGood + "/sentinel needammo - Toggles whether the NPC will need ammo.");
            }
            if (sender.hasPermission("sentinel.safeshot")) {
                sender.sendMessage(prefixGood + "/sentinel safeshot - Toggles whether the NPC will avoid damaging non-targets.");
            }
            if (sender.hasPermission("sentinel.chase")) {
                sender.sendMessage(prefixGood + "/sentinel chaseclose - Toggles whether the NPC will chase while in 'close quarters' fights.");
            }
            if (sender.hasPermission("sentinel.chase")) {
                sender.sendMessage(prefixGood + "/sentinel chaseranged - Toggles whether the NPC will chase while in ranged fights.");
            }
            if (sender.hasPermission("sentinel.drops")) {
                sender.sendMessage(prefixGood + "/sentinel drops - Changes the drops of the current NPC.");
            }
            if (sender.hasPermission("sentinel.spawnpoint")) {
                sender.sendMessage(prefixGood + "/sentinel spawnpoint - Changes the NPC's spawn point to its current location, or removes it if it's already there.");
            }
            if (sender.hasPermission("sentinel.forgive")) {
                sender.sendMessage(prefixGood + "/sentinel forgive - Forgives all current targets.");
            }
            if (sender.hasPermission("sentinel.enemydrops")) {
                sender.sendMessage(prefixGood + "/sentinel enemydrops - Toggles whether enemy mobs of this NPC drop items.");
            }
            if (sender.hasPermission("sentinel.kill")) {
                sender.sendMessage(prefixGood + "/sentinel kill - Kills the NPC.");
            }
            if (sender.hasPermission("sentinel.respawn")) {
                sender.sendMessage(prefixGood + "/sentinel respawn - Respawns the NPC.");
            }
            if (sender.hasPermission("sentinel.targettime")) {
                sender.sendMessage(prefixGood + "/sentinel targettime TIME - Sets the NPC's enemy target time limit.");
            }
            if (sender.hasPermission("sentinel.speed")) {
                sender.sendMessage(prefixGood + "/sentinel speed SPEED - Sets the NPC's movement speed modifier.");
            }
            if (sender.hasPermission("sentinel.autoswitch")) {
                sender.sendMessage(prefixGood + "/sentinel autoswitch - Toggles whether the NPC automatically switches items.");
            }
            if (sender.hasPermission("sentinel.accuracy")) {
                sender.sendMessage(prefixGood + "/sentinel accuracy OFFSET - Sets the accuracy of an NPC.");
            }
            if (sender.hasPermission("sentinel.squad")) {
                sender.sendMessage(prefixGood + "/sentinel squad SQUAD - Sets the NPC's squad name (null for none).");
            }
            if (sender.hasPermission("sentinel.realistic")) {
                sender.sendMessage(prefixGood + "/sentinel realistic - Toggles whether the NPC should use \"realistic\" targeting logic (don't attack things you can't see.)");
            }
            if (sender.hasPermission("sentinel.reach")) {
                sender.sendMessage(prefixGood + "/sentinel reach REACH - Sets the NPC's reach (how far it can punch.)");
            }
            if (sender.hasPermission("sentinel.greet")) {
                sender.sendMessage(prefixGood + "/sentinel greeting GREETING - Sets a greeting message for the NPC to say.");
            }
            if (sender.hasPermission("sentinel.greet")) {
                sender.sendMessage(prefixGood + "/sentinel warning WARNING - Sets a warning message for the NPC to say.");
            }
            if (sender.hasPermission("sentinel.greet")) {
                sender.sendMessage(prefixGood + "/sentinel greetrange RANGE - Sets how far a player can be from an NPC before they are greeted.");
            }
            if (sender.hasPermission("sentinel.info")) {
                sender.sendMessage(prefixGood + "/sentinel info - Shows info on the current NPC.");
            }
            if (sender.hasPermission("sentinel.info")) {
                sender.sendMessage(prefixGood + "/sentinel targets - Shows the targets of the current NPC.");
            }
            if (sender.hasPermission("sentinel.info")) {
                sender.sendMessage(prefixGood + "/sentinel stats - Shows statistics about the current NPC.");
            }
            if (sender.hasPermission("sentinel.admin")) {
                sender.sendMessage(prefixGood + "Be careful, you can edit other player's NPCs!");
            }
            return true;
        }
    }

    public String getNameTargetString(List<String> strs) {
        StringBuilder targets = new StringBuilder();
        for (String str : strs) {
            targets.append(str).append(", ");
        }
        return targets.length() > 0 ? targets.substring(0, targets.length() - 2) : targets.toString();
    }

    public String getTargetString(HashSet<String> sentinel) {
        StringBuilder targets = new StringBuilder();
        for (String target : sentinel) {
            targets.append(target).append(", ");
        }
        return targets.length() > 0 ? targets.substring(0, targets.length() - 2) : targets.toString();
    }

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
