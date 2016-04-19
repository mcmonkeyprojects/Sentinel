package org.mcmonkey.sentinel;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.TraitInfo;
import net.citizensnpcs.api.trait.trait.Owner;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class SentinelPlugin extends JavaPlugin implements Listener {

    public static final String ColorBasic = ChatColor.YELLOW.toString();

    public static final String prefixGood = ChatColor.DARK_GREEN + "[Sentinel] " + ColorBasic;

    public static final String prefixBad = ChatColor.DARK_GREEN + "[Sentinel] " + ChatColor.RED;

    static HashMap<String, SentinelTarget> targetOptions = new HashMap<String, SentinelTarget>();

    static HashMap<EntityType, HashSet<SentinelTarget>> entityToTargets = new HashMap<EntityType, HashSet<SentinelTarget>>();

    public static SentinelPlugin instance;

    public Permission vaultPerms;

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

    public int tickRate = 10;

    static {
        for (EntityType type: EntityType.values()) {
            entityToTargets.put(type, new HashSet<SentinelTarget>());
        }
    }

    @Override
    public void onEnable() {
        getLogger().info("Sentinel loading...");
        instance = this;
        CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(SentinelTrait.class).withName("sentinel"));
        saveDefaultConfig();
        if (getConfig().getInt("config version", 0) != 2) {
            getLogger().warning("Outdated Sentinel config - please delete it to regenerate it!");
        }
        BukkitRunnable postLoad = new BukkitRunnable() {
            @Override
            public void run() {
                for (NPC npc: CitizensAPI.getNPCRegistry()) {
                    if (!npc.isSpawned() && npc.hasTrait(SentinelTrait.class)) {
                        SentinelTrait sentinel = npc.getTrait(SentinelTrait.class);
                        if (sentinel.respawnTime > 0) {
                            npc.spawn(sentinel.spawnPoint == null ? npc.getStoredLocation() : sentinel.spawnPoint);
                        }
                    }
                }
            }
        };
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
        tryGetPerms();

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
                for (ItemStack it: event.getInventory().getContents()) {
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
        String arg0 = args.length > 0 ? args[0].toLowerCase(): "help";
        SentinelTrait sentinel = getSentinelFor(sender);
        if (sentinel == null && !arg0.equals("help")) {
            sender.sendMessage(prefixBad + "Must have an NPC selected!");
            return true;
        }
        else if (arg0.equals("addtarget") && sender.hasPermission("sentinel.addtarget") && args.length > 1) {
            SentinelTarget target = SentinelTarget.forName(args[1].toUpperCase());
            if (target == null) {
                String[] info = args[1].split(":", 2);
                if (info.length > 1) {
                    List<String> names = null;
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
                    try {
                        if ("Sentinel".matches(info[1])) {
                            ignoreMe++;
                        }
                    }
                    catch (Exception e) {
                        names = null;
                        sender.sendMessage(prefixBad + "Bad regular expression!");
                    }
                    if (names != null) {
                        if (names.contains(info[1])) {
                            sender.sendMessage(prefixBad + "Already tracking that name target!");
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
                for (SentinelTarget poss: SentinelTarget.values()) {
                    valid.append(poss.name()).append(", ");
                }
                sender.sendMessage(prefixGood + "Valid targets: " + valid.substring(0, valid.length() - 2));
                sender.sendMessage(prefixGood + "Also allowed: player:NAME(REGEX), npc:NAME(REGEX), entityname:NAME(REGEX), helditem:MATERIALNAME(REGEX), group:GROUPNAME(EXACT), event:pvp/pvnpc/pve");
            }
            else {
                if (sentinel.targets.add(target)) {
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
                    List<String> names = null;
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
                    try {
                        if ("Sentinel".matches(info[1])) {
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
                if (sentinel.targets.remove(target)) {
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
                    List<String> names = null;
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
                        if (!names.contains(info[1])) {
                            sender.sendMessage(prefixBad + "Already ignoring that target!");
                        }
                        else {
                            names.add(info[1]);
                            sender.sendMessage(prefixGood + "Ignoring new target!");
                        }
                        return true;
                    }
                    try {
                        if ("Sentinel".matches(info[1])) {
                            ignoreMe++;
                        }
                    }
                    catch (Exception e) {
                        names = null;
                        sender.sendMessage(prefixBad + "Bad regular expression!");
                    }
                    if (names != null) {
                        if (!names.contains(info[1])) {
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
                if (sentinel.ignores.add(target)) {
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
                    List<String> names = null;
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
                    try {
                        if ("Sentinel".matches(info[1])) {
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
                if (sentinel.ignores.remove(target)) {
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
                Double d = Double.valueOf(args[1]);
                if (d > 0 && d < 200) {
                    sentinel.range = d;
                    sender.sendMessage(prefixGood + "Range set!");
                }
                else {
                    throw new NumberFormatException("Number out or range.");
                }
            }
            catch (NumberFormatException ex) {
                sender.sendMessage(prefixBad + "Invalid range number!");
            }
            return true;
        }
        else if (arg0.equals("damage") && sender.hasPermission("sentinel.damage") && args.length > 1) {
            try {
                Double d = Double.valueOf(args[1]);
                if (d < 1000) {
                    sentinel.damage = d;
                    sender.sendMessage(prefixGood + "Damage set!");
                }
                else {
                    throw new NumberFormatException("Number out or range.");
                }
            }
            catch (NumberFormatException ex) {
                sender.sendMessage(prefixBad + "Invalid damage number!");
            }
            return true;
        }
        else if (arg0.equals("armor") && sender.hasPermission("sentinel.armor") && args.length > 1) {
            try {
                Double d = Double.valueOf(args[1]);
                if (d <= 1) {
                    sentinel.armor = d;
                    sender.sendMessage(prefixGood + "Armor set!");
                }
                else {
                    throw new NumberFormatException("Number out or range.");
                }
            }
            catch (NumberFormatException ex) {
                sender.sendMessage(prefixBad + "Invalid armor number!");
            }
            return true;
        }
        else if (arg0.equals("health") && sender.hasPermission("sentinel.health") && args.length > 1) {
            try {
                Double d = Double.valueOf(args[1]);
                if (d <= 200) {
                    sentinel.setHealth(d);
                    sender.sendMessage(prefixGood + "Health set!");
                }
                else {
                    throw new NumberFormatException("Number out or range.");
                }
            }
            catch (NumberFormatException ex) {
                sender.sendMessage(prefixBad + "Invalid health number!");
            }
            return true;
        }
        else if (arg0.equals("attackrate") && sender.hasPermission("sentinel.attackrate") && args.length > 1) {
            try {
                int d = Integer.valueOf(args[1]);
                if (d >= 10 && d <= 2000) {
                    sentinel.attackRate = d;
                    sender.sendMessage(prefixGood + "Attack rate set!");
                }
                else {
                    throw new NumberFormatException("Number out or range.");
                }
            }
            catch (NumberFormatException ex) {
                sender.sendMessage(prefixBad + "Invalid rate number!");
            }
            return true;
        }
        else if (arg0.equals("healrate") && sender.hasPermission("sentinel.healrate") && args.length > 1) {
            try {
                int d = Integer.valueOf(args[1]);
                if (d >= 0 && d <= 2000) {
                    sentinel.healRate = d;
                    sender.sendMessage(prefixGood + "Heal rate set!");
                }
                else {
                    throw new NumberFormatException("Number out or range.");
                }
            }
            catch (NumberFormatException ex) {
                sender.sendMessage(prefixBad + "Invalid rate number!");
            }
            return true;
        }
        else if (arg0.equals("respawntime") && sender.hasPermission("sentinel.respawntime") && args.length > 1) {
            try {
                long d = Long.valueOf(args[1]);
                sentinel.respawnTime = d;
                sender.sendMessage(prefixGood + "Respawn time set!");
            }
            catch (NumberFormatException ex) {
                sender.sendMessage(prefixBad + "Invalid time number!");
            }
            return true;
        }
        else if (arg0.equals("chaserange") && sender.hasPermission("sentinel.chaserange") && args.length > 1) {
            try {
                double d = Double.valueOf(args[1]);
                sentinel.chaseRange = d;
                sender.sendMessage(prefixGood + "Chase range set!");
            }
            catch (NumberFormatException ex) {
                sender.sendMessage(prefixBad + "Invalid range number!");
            }
            return true;
        }
        else if (arg0.equals("invincible") && sender.hasPermission("sentinel.invincible")) {
            sentinel.setInvincible(!sentinel.invincible);
            if (sentinel.invincible) {
                sender.sendMessage(prefixGood + "NPC now invincible!");
            }
            else {
                sender.sendMessage(prefixGood + "NPC no longer invincible!");
            }
            return true;
        }
        else if (arg0.equals("fightback") && sender.hasPermission("sentinel.fightback")) {
            sentinel.fightback = !sentinel.fightback;
            if (sentinel.fightback) {
                sender.sendMessage(prefixGood + "NPC now fights back!");
            }
            else {
                sender.sendMessage(prefixGood + "NPC no longer fights back!");
            }
            return true;
        }
        else if (arg0.equals("needammo") && sender.hasPermission("sentinel.needammo")) {
            sentinel.needsAmmo = !sentinel.needsAmmo;
            if (sentinel.needsAmmo) {
                sender.sendMessage(prefixGood + "NPC now needs ammo!");
            }
            else {
                sender.sendMessage(prefixGood + "NPC no longer needs ammo!");
            }
            return true;
        }
        else if (arg0.equals("safeshot") && sender.hasPermission("sentinel.safeshot")) {
            sentinel.safeShot = !sentinel.safeShot;
            if (sentinel.safeShot) {
                sender.sendMessage(prefixGood + "NPC now is a safe shot!");
            }
            else {
                sender.sendMessage(prefixGood + "NPC is no longer a safe shot!");
            }
            return true;
        }
        else if (arg0.equals("chaseclose") && sender.hasPermission("sentinel.chase")) {
            sentinel.closeChase = !sentinel.closeChase;
            if (sentinel.closeChase) {
                sender.sendMessage(prefixGood + "NPC now will chase while close!");
            }
            else {
                sender.sendMessage(prefixGood + "NPC no longer will chase while close!");
            }
            return true;
        }
        else if (arg0.equals("chaseranged") && sender.hasPermission("sentinel.chase")) {
            sentinel.rangedChase = !sentinel.rangedChase;
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
                sentinel.setGuarding(pl == null ? null: pl.getUniqueId());
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
                    sentinel.spawnPoint = pos;
                    sender.sendMessage(prefixGood + "Spawn point updated!");
                }
            }
            return true;
        }
        else if (arg0.equals("forgive") && sender.hasPermission("sentinel.forgive")) {
            sentinel.targets.clear();
            sender.sendMessage(prefixGood + "Targets forgiven.");
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
            sender.sendMessage(prefixGood + "Ignored Targets: " + ChatColor.AQUA + getTargetString(sentinel.ignores));
            sender.sendMessage(prefixGood + "Ignored Player Name Targets: " + ChatColor.AQUA + getNameTargetString(sentinel.playerNameIgnores));
            sender.sendMessage(prefixGood + "Ignored NPC Name Targets: " + ChatColor.AQUA + getNameTargetString(sentinel.npcNameIgnores));
            sender.sendMessage(prefixGood + "Ignored Entity Name Targets: " + ChatColor.AQUA + getNameTargetString(sentinel.entityNameIgnores));
            sender.sendMessage(prefixGood + "Ignored Held Item Targets: " + ChatColor.AQUA + getNameTargetString(sentinel.heldItemIgnores));
            sender.sendMessage(prefixGood + "Ignored Group Targets: " + ChatColor.AQUA + getNameTargetString(sentinel.groupIgnores));
            return true;
        }
        else if (arg0.equals("info") && sender.hasPermission("sentinel.info")) {
            sender.sendMessage(prefixGood + ChatColor.RESET + sentinel.getNPC().getFullName() + ColorBasic
                    + ": owned by " + ChatColor.RESET + getOwner(sentinel.getNPC()) +
                    (sentinel.getGuarding() == null ? "": ColorBasic + ", guarding: " + ChatColor.RESET + Bukkit.getOfflinePlayer(sentinel.getGuarding()).getName()));
            sender.sendMessage(prefixGood + "Damage: " + ChatColor.AQUA + sentinel.damage);
            sender.sendMessage(prefixGood + "Armor: " + ChatColor.AQUA + sentinel.armor);
            sender.sendMessage(prefixGood + "Health: " + ChatColor.AQUA +
            (sentinel.getNPC().isSpawned() ? sentinel.getLivingEntity().getHealth() + "/": "") + sentinel.health);
            sender.sendMessage(prefixGood + "Range: " + ChatColor.AQUA + sentinel.range);
            sender.sendMessage(prefixGood + "Attack Rate: " + ChatColor.AQUA + sentinel.attackRate);
            sender.sendMessage(prefixGood + "Heal Rate: " + ChatColor.AQUA + sentinel.healRate);
            sender.sendMessage(prefixGood + "Respawn Time: " + ChatColor.AQUA + sentinel.respawnTime);
            sender.sendMessage(prefixGood + "Invincibility Enabled: " + ChatColor.AQUA + sentinel.invincible);
            sender.sendMessage(prefixGood + "Fightback Enabled: " + ChatColor.AQUA + sentinel.fightback);
            sender.sendMessage(prefixGood + "Ranged Chasing Enabled: " + ChatColor.AQUA + sentinel.rangedChase);
            sender.sendMessage(prefixGood + "Close-Quarters Chasing Enabled: " + ChatColor.AQUA + sentinel.closeChase);
            sender.sendMessage(prefixGood + "Maximum chase range: " + ChatColor.AQUA + sentinel.chaseRange);
            sender.sendMessage(prefixGood + "Safe-Shot Enabled: " + ChatColor.AQUA + sentinel.safeShot);
            return true;
        }
        else if (arg0.equals("stats") && sender.hasPermission("sentinel.info")) {
            sender.sendMessage(prefixGood + ChatColor.RESET + sentinel.getNPC().getFullName() + ColorBasic
                    + ": owned by " + ChatColor.RESET + getOwner(sentinel.getNPC()));
            sender.sendMessage(prefixGood + "Arrows fired: " + ChatColor.AQUA + sentinel.stats_arrowsFired);
            sender.sendMessage(prefixGood + "Potions thrown: " + ChatColor.AQUA + sentinel.stats_potionsThrown);
            sender.sendMessage(prefixGood + "Fireballs launched: " + ChatColor.AQUA + sentinel.stats_fireballsFired);
            sender.sendMessage(prefixGood + "Punches: " + ChatColor.AQUA + sentinel.stats_punches);
            sender.sendMessage(prefixGood + "Times spawned: " + ChatColor.AQUA + sentinel.stats_timesSpawned);
            sender.sendMessage(prefixGood + "Damage Given: " + ChatColor.AQUA + sentinel.stats_damageGiven);
            sender.sendMessage(prefixGood + "Damage Taken: " + ChatColor.AQUA + sentinel.stats_damageTaken);
            sender.sendMessage(prefixGood + "Minutes spawned: " + ChatColor.AQUA + sentinel.stats_ticksSpawned / (20.0 * 60.0));
            return true;
        }
        else {
            if (sender.hasPermission("sentinel.basic")) sender.sendMessage(prefixGood + "/sentinel help - Shows help info.");
            if (sender.hasPermission("sentinel.addtarget")) sender.sendMessage(prefixGood + "/sentinel addtarget TYPE - Adds a target.");
            if (sender.hasPermission("sentinel.removetarget")) sender.sendMessage(prefixGood + "/sentinel removetarget TYPE - Removes a target.");
            if (sender.hasPermission("sentinel.addignore")) sender.sendMessage(prefixGood + "/sentinel addignore TYPE - Ignores a target.");
            if (sender.hasPermission("sentinel.removeignore")) sender.sendMessage(prefixGood + "/sentinel removeignore TYPE - Allows targetting a target.");
            if (sender.hasPermission("sentinel.range")) sender.sendMessage(prefixGood + "/sentinel range RANGE - Sets the NPC's maximum attack range.");
            if (sender.hasPermission("sentinel.damage")) sender.sendMessage(prefixGood + "/sentinel damage DAMAGE - Sets the NPC's attack damage.");
            if (sender.hasPermission("sentinel.armor")) sender.sendMessage(prefixGood + "/sentinel armor ARMOR - Sets the NPC's armor level.");
            if (sender.hasPermission("sentinel.health")) sender.sendMessage(prefixGood + "/sentinel health HEALTH - Sets the NPC's health level.");
            if (sender.hasPermission("sentinel.attackrate")) sender.sendMessage(prefixGood + "/sentinel attackrate RATE - Changes the rate at which the NPC attacks, in ticks.");
            if (sender.hasPermission("sentinel.healrate")) sender.sendMessage(prefixGood + "/sentinel healrate RATE - Changes the rate at which the NPC heals, in ticks.");
            if (sender.hasPermission("sentinel.respawntime")) sender.sendMessage(prefixGood + "/sentinel respawntime TIME - Changes the time it takes for the NPC to respawn, in ticks.");
            if (sender.hasPermission("sentinel.chaserange")) sender.sendMessage(prefixGood + "/sentinel chaserange RANGE - Changes the maximum distance an NPC will run before returning to base.");
            if (sender.hasPermission("sentinel.guard")) sender.sendMessage(prefixGood + "/sentinel guard (PLAYERNAME) - Makes the NPC guard a specific player. Don't specify a player to stop guarding.");
            if (sender.hasPermission("sentinel.invincible")) sender.sendMessage(prefixGood + "/sentinel invincible - Toggles whether the NPC is invincible.");
            if (sender.hasPermission("sentinel.fightback")) sender.sendMessage(prefixGood + "/sentinel fightback - Toggles whether the NPC will fight back.");
            if (sender.hasPermission("sentinel.needammo")) sender.sendMessage(prefixGood + "/sentinel needammo - Toggles whether the NPC will need ammo.");
            if (sender.hasPermission("sentinel.safeshot")) sender.sendMessage(prefixGood + "/sentinel safeshot - Toggles whether the NPC will avoid damaging non-targets.");
            if (sender.hasPermission("sentinel.chase")) sender.sendMessage(prefixGood + "/sentinel chaseclose - Toggles whether the NPC will chase while in 'close quarters' fights.");
            if (sender.hasPermission("sentinel.chase")) sender.sendMessage(prefixGood + "/sentinel chaseranged - Toggles whether the NPC will chase while in ranged fights.");
            if (sender.hasPermission("sentinel.drops")) sender.sendMessage(prefixGood + "/sentinel drops - Changes the drops of the current NPC.");
            if (sender.hasPermission("sentinel.spawnpoint")) sender.sendMessage(prefixGood + "/sentinel spawnpoint - Changes the NPC's spawn point to its current location, or removes it if it's already there.");
            if (sender.hasPermission("sentinel.forgive")) sender.sendMessage(prefixGood + "/sentinel forgive - Forgives all current targets.");
            if (sender.hasPermission("sentinel.info")) sender.sendMessage(prefixGood + "/sentinel info - Shows info on the current NPC.");
            if (sender.hasPermission("sentinel.info")) sender.sendMessage(prefixGood + "/sentinel targets - Shows the targets of the current NPC.");
            if (sender.hasPermission("sentinel.info")) sender.sendMessage(prefixGood + "/sentinel stats - Shows statistics about the current NPC.");
            if (sender.hasPermission("sentinel.admin")) sender.sendMessage(prefixGood + "Be careful, you can edit other player's NPCs!");
            return true;
        }
    }

    public String getNameTargetString(List<String> strs) {
        StringBuilder targets = new StringBuilder();
        for (String str: strs) {
            targets.append(str).append(", ");
        }
        return targets.length() > 0 ? targets.substring(0, targets.length() - 2): targets.toString();
    }

    public String getTargetString(HashSet<SentinelTarget> sentinel) {
        StringBuilder targets = new StringBuilder();
        for (SentinelTarget target: sentinel) {
            targets.append(target.name()).append(", ");
        }
        return targets.length() > 0 ? targets.substring(0, targets.length() - 2): targets.toString();
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
