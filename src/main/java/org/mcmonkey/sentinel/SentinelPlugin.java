package org.mcmonkey.sentinel;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.TraitInfo;
import net.citizensnpcs.api.trait.trait.Owner;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class SentinelPlugin extends JavaPlugin {

    public static final String ColorBasic = ChatColor.YELLOW.toString();

    public static final String prefixGood = ChatColor.DARK_GREEN + "[Sentinel] " + ColorBasic;

    public static final String prefixBad = ChatColor.DARK_GREEN + "[Sentinel] " + ChatColor.RED;

    static HashMap<String, SentinelTarget> targetOptions = new HashMap<String, SentinelTarget>();

    static HashMap<EntityType, HashSet<SentinelTarget>> entityToTargets = new HashMap<EntityType, HashSet<SentinelTarget>>();

    public static SentinelPlugin instance;

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
        if (getConfig().getInt("config version", 0) != 1) {
            getLogger().warning("Outdated Sentinel config - please delete it to regenerate it!");
        }
        tickRate = getConfig().getInt("update rate", 10);
        getLogger().info("Sentinel loaded!");
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
                sender.sendMessage(prefixGood + "Also allowed: player:NAME(REGEX), npc:NAME(REGEX), entityname:NAME(REGEX), helditem:MATERIALNAME(REGEX)");
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
                            sender.sendMessage(prefixBad + "Not tracking that name target!");
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
                            sender.sendMessage(prefixBad + "Already ignoring that name target!");
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
                            sender.sendMessage(prefixBad + "Was not ignoring that name target!");
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
        else if (arg0.equals("invincible") && sender.hasPermission("sentinel.invincible") && args.length > 1) {
            sentinel.setInvincible(!sentinel.invincible);
            if (sentinel.invincible) {
                sender.sendMessage(prefixGood + "Sentinel now invincible!");
            }
            else {
                sender.sendMessage(prefixGood + "Sentinel no longer invincible!");
            }
            return true;
        }
        else if (arg0.equals("fightback") && sender.hasPermission("sentinel.fightback") && args.length > 1) {
            sentinel.fightback = !sentinel.fightback;
            if (sentinel.fightback) {
                sender.sendMessage(prefixGood + "Sentinel now fights back!");
            }
            else {
                sender.sendMessage(prefixGood + "Sentinel no longer fights back!");
            }
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
            sender.sendMessage(prefixGood + "Ignored Targets: " + ChatColor.AQUA + getTargetString(sentinel.ignores));
            sender.sendMessage(prefixGood + "Ignored Player Name Targets: " + ChatColor.AQUA + getNameTargetString(sentinel.playerNameIgnores));
            sender.sendMessage(prefixGood + "Ignored NPC Name Targets: " + ChatColor.AQUA + getNameTargetString(sentinel.npcNameIgnores));
            sender.sendMessage(prefixGood + "Ignored Entity Name Targets: " + ChatColor.AQUA + getNameTargetString(sentinel.entityNameIgnores));
            sender.sendMessage(prefixGood + "Ignored Held Item Targets: " + ChatColor.AQUA + getNameTargetString(sentinel.heldItemIgnores));
            return true;
        }
        else if (arg0.equals("info") && sender.hasPermission("sentinel.info")) {
            sender.sendMessage(prefixGood + ChatColor.RESET + sentinel.getNPC().getFullName() + ColorBasic
                    + ": owned by " + ChatColor.RESET + getOwner(sentinel.getNPC()));
            sender.sendMessage(prefixGood + "Damage: " + ChatColor.AQUA + sentinel.damage);
            sender.sendMessage(prefixGood + "Armor: " + ChatColor.AQUA + sentinel.armor);
            sender.sendMessage(prefixGood + "Health: " + ChatColor.AQUA +
            (sentinel.getNPC().isSpawned() ? sentinel.getLivingEntity().getHealth() + "/": "") + sentinel.health);
            sender.sendMessage(prefixGood + "Range: " + ChatColor.AQUA + sentinel.range);
            sender.sendMessage(prefixGood + "Attack Rate: " + ChatColor.AQUA + sentinel.attackRate);
            sender.sendMessage(prefixGood + "Heal Rate: " + ChatColor.AQUA + sentinel.healRate);
            sender.sendMessage(prefixGood + "Invincibility Enabled: " + ChatColor.AQUA + sentinel.invincible);
            sender.sendMessage(prefixGood + "Fightback Enabled: " + ChatColor.AQUA + sentinel.fightback);
            sender.sendMessage(prefixGood + "Ranged Chasing Enabled: " + ChatColor.AQUA + sentinel.rangedChase);
            sender.sendMessage(prefixGood + "Close-Quarters Chasing Enabled: " + ChatColor.AQUA + sentinel.closeChase);
            return true;
        }
        else if (arg0.equals("stats") && sender.hasPermission("sentinel.info")) {
            sender.sendMessage(prefixGood + ChatColor.RESET + sentinel.getNPC().getFullName() + ColorBasic
                    + ": owned by " + ChatColor.RESET + getOwner(sentinel.getNPC()));
            sender.sendMessage(prefixGood + "Arrows fired: " + ChatColor.AQUA + sentinel.stats_arrowsFired);
            sender.sendMessage(prefixGood + "Potions thrown: " + ChatColor.AQUA + sentinel.stats_potionsThrown);
            sender.sendMessage(prefixGood + "Fireballs launched: " + ChatColor.AQUA + sentinel.stats_fireballsFired);
            sender.sendMessage(prefixGood + "Times spawned: " + ChatColor.AQUA + sentinel.stats_timesSpawned);
            sender.sendMessage(prefixGood + "Punches: " + ChatColor.AQUA + sentinel.stats_punches);
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
            if (sender.hasPermission("sentinel.invincible")) sender.sendMessage(prefixGood + "/sentinel invincible - Toggles whether the NPC is invincible.");
            if (sender.hasPermission("sentinel.fightback")) sender.sendMessage(prefixGood + "/sentinel fightback - Toggles whether the NPC will fight back.");
            if (sender.hasPermission("sentinel.info")) sender.sendMessage(prefixGood + "/sentinel info - Shows info on the current NPC.");
            if (sender.hasPermission("sentinel.info")) sender.sendMessage(prefixGood + "/sentinel targets - Shows the targets of the current NPC.");
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
