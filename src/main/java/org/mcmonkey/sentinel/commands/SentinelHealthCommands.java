package org.mcmonkey.sentinel.commands;

import net.citizensnpcs.api.command.Command;
import net.citizensnpcs.api.command.CommandContext;
import net.citizensnpcs.api.command.Requirements;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.mcmonkey.sentinel.SentinelEventHandler;
import org.mcmonkey.sentinel.SentinelPlugin;
import org.mcmonkey.sentinel.SentinelTrait;

public class SentinelHealthCommands {

    @Command(aliases = {"sentinel"}, usage = "armor ARMOR",
            desc = "Sets the NPC's armor level.",
            modifiers = {"armor"}, permission = "sentinel.armor", min = 1, max = 2)
    @Requirements(livingEntity = true, ownership = true, traits = {SentinelTrait.class})
    public void armor(CommandContext args, CommandSender sender, SentinelTrait sentinel) {
        if (args.argsLength() <= 1) {
            sender.sendMessage(SentinelCommand.prefixGood + "Current armor: " + ChatColor.AQUA + sentinel.armor
                    + (sentinel.getNPC().isSpawned() ? SentinelCommand.colorBasic + " Calculated: "
                    + ChatColor.AQUA + sentinel.getArmor(sentinel.getLivingEntity()) : ""));
            return;
        }
        try {
            double d = Double.parseDouble(args.getString(1));
            if (d <= 1) {
                sentinel.armor = d;
                String armorInfo;
                if (d == 0) {
                    armorInfo = "no armor";
                }
                else if (d < 0) {
                    armorInfo = "automatic armor calculation";
                }
                else if (d == 1) {
                    armorInfo = "100%, invincible armor";
                }
                else {
                    armorInfo = (d * 100) + "%";
                }
                sender.sendMessage(SentinelCommand.prefixGood + "Armor set to " + ChatColor.AQUA + d
                        + SentinelCommand.colorBasic + "! (" + armorInfo + ")");
            }
            else {
                throw new NumberFormatException("Number out of range (must be <= 1).");
            }
        }
        catch (NumberFormatException ex) {
            sender.sendMessage(SentinelCommand.prefixBad + "Invalid armor number: " + ex.getMessage());
        }
    }

    @Command(aliases = {"sentinel"}, usage = "health HEALTH",
            desc = "Sets the NPC's health level.",
            modifiers = {"health"}, permission = "sentinel.health", min = 1, max = 2)
    @Requirements(livingEntity = true, ownership = true, traits = {SentinelTrait.class})
    public void health(CommandContext args, CommandSender sender, SentinelTrait sentinel) {
        if (args.argsLength() <= 1) {
            sender.sendMessage(SentinelCommand.prefixGood + "Current max health: " + ChatColor.AQUA + sentinel.health);
            return;
        }
        try {
            double d = Double.parseDouble(args.getString(1));
            if ((d >= SentinelTrait.healthMin) && (d <= SentinelPlugin.instance.maxHealth)) {
                sentinel.setHealth(d);
                sender.sendMessage(SentinelCommand.prefixGood + "Health set!");
            }
            else {
                throw new NumberFormatException("Number out of range (must be >= " + SentinelTrait.healthMin + " and <= " + SentinelPlugin.instance.maxHealth + ").");
            }
        }
        catch (NumberFormatException ex) {
            sender.sendMessage(SentinelCommand.prefixBad + "Invalid health number: " + ex.getMessage());
        }
    }

    @Command(aliases = {"sentinel"}, usage = "healrate RATE",
            desc = "Changes the rate at which the NPC heals, in seconds.",
            modifiers = {"healrate"}, permission = "sentinel.healrate", min = 1, max = 2)
    @Requirements(livingEntity = true, ownership = true, traits = {SentinelTrait.class})
    public void healRate(CommandContext args, CommandSender sender, SentinelTrait sentinel) {
        if (args.argsLength() <= 1) {
            sender.sendMessage(SentinelCommand.prefixGood + "Current heal rate: " + ChatColor.AQUA + (sentinel.healRate / 20.0));
            return;
        }
        try {
            double da = Double.parseDouble(args.getString(1));
            int d = (int) (da * 20);
            if ((d >= SentinelPlugin.instance.tickRate && d <= SentinelTrait.healRateMax) || d == 0) {
                sentinel.healRate = d;
                sender.sendMessage(SentinelCommand.prefixGood + "Heal rate set!");
            }
            else {
                throw new NumberFormatException("Number out of range (must be >= " + (SentinelPlugin.instance.tickRate / 20.0) + " and <= " + (SentinelTrait.healRateMax / 20.0) + ", or 0).");
            }
        }
        catch (NumberFormatException ex) {
            sender.sendMessage(SentinelCommand.prefixBad + "Invalid rate number: " + ex.getMessage());
        }
        return;
    }

    @Command(aliases = {"sentinel"}, usage = "respawntime TIME",
            desc = "Changes the time it takes for the NPC to respawn, in seconds.",
            modifiers = {"respawntime"}, permission = "sentinel.respawntime", min = 1, max = 2)
    @Requirements(livingEntity = true, ownership = true, traits = {SentinelTrait.class})
    public void respawnTime(CommandContext args, CommandSender sender, SentinelTrait sentinel) {
        if (args.argsLength() <= 1) {
            sender.sendMessage(SentinelCommand.prefixGood + "Current respawn time: " + ChatColor.AQUA + (sentinel.respawnTime / 20.0));
            return;
        }
        try {
            double d = Double.parseDouble(args.getString(1));
            sentinel.respawnTime = (long) (d * 20);
            sender.sendMessage(SentinelCommand.prefixGood + "Respawn time set!");
        }
        catch (NumberFormatException ex) {
            sender.sendMessage(SentinelCommand.prefixBad + "Invalid time number: " + ex.getMessage());
        }
    }

    @Command(aliases = {"sentinel"}, usage = "invincible ['true'/'false']",
            desc = "Toggles whether the NPC is invincible.",
            modifiers = {"invincible"}, permission = "sentinel.invincible", min = 1, max = 2)
    @Requirements(livingEntity = true, ownership = true, traits = {SentinelTrait.class})
    public void invincible(CommandContext args, CommandSender sender, SentinelTrait sentinel) {
        boolean mode = !sentinel.invincible;
        if (args.argsLength() > 1 && "true".equalsIgnoreCase(args.getString(1))) {
            mode = true;
        }
        if (args.argsLength() > 1 && "false".equalsIgnoreCase(args.getString(1))) {
            mode = false;
        }
        sentinel.setInvincible(mode);
        if (sentinel.invincible) {
            sender.sendMessage(SentinelCommand.prefixGood + "NPC now invincible!");
        }
        else {
            sender.sendMessage(SentinelCommand.prefixGood + "NPC no longer invincible!");
        }
    }

    @Command(aliases = {"sentinel"}, usage = "kill",
            desc = "Kills the NPC.",
            modifiers = {"kill"}, permission = "sentinel.kill", min = 1, max = 1)
    @Requirements(livingEntity = true, ownership = true, traits = {SentinelTrait.class})
    public void kill(CommandContext args, CommandSender sender, SentinelTrait sentinel) {
        if (!sentinel.getNPC().isSpawned()) {
            sender.sendMessage(SentinelCommand.prefixBad + "NPC is already dead!");
        }
        else {
            sentinel.getLivingEntity().damage(sentinel.health * 2);
            sender.sendMessage(SentinelCommand.prefixGood + "Killed!");
        }
    }

    @Command(aliases = {"sentinel"}, usage = "respawn",
            desc = "Respawns the NPC.",
            modifiers = {"respawn"}, permission = "sentinel.respawn", min = 1, max = 1)
    @Requirements(livingEntity = true, ownership = true, traits = {SentinelTrait.class})
    public void respawn(CommandContext args, CommandSender sender, SentinelTrait sentinel) {
        Location loc = sentinel.spawnPoint == null ? sentinel.getNPC().getStoredLocation() : sentinel.spawnPoint;
        if (!sentinel.getNPC().spawn(loc)) {
            sentinel.getNPC().teleport(loc, PlayerTeleportEvent.TeleportCause.COMMAND);
        }
        sender.sendMessage(SentinelCommand.prefixGood + "Respawned!");
    }

    @Command(aliases = {"sentinel"}, usage = "drops",
            desc = "Changes the drops of the current NPC.",
            modifiers = {"drops"}, permission = "sentinel.drops", min = 1, max = 1)
    @Requirements(livingEntity = true, ownership = true, traits = {SentinelTrait.class})
    public void drops(CommandContext args, CommandSender sender, SentinelTrait sentinel) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(SentinelCommand.prefixBad + "Players only!");
            return;
        }
        Inventory inv = Bukkit.createInventory(null, 9 * 4, SentinelEventHandler.InvPrefix + sentinel.getNPC().getId());
        ItemStack[] items = new ItemStack[sentinel.drops.size()];
        inv.addItem(sentinel.drops.toArray(items));
        ((Player) sender).openInventory(inv);
    }

    @Command(aliases = {"sentinel"}, usage = "dropchance ID CHANCE",
            desc = "Changes the chance of a drop.",
            modifiers = {"dropchance"}, permission = "sentinel.drops", min = 1, max = 3)
    @Requirements(livingEntity = true, ownership = true, traits = {SentinelTrait.class})
    public void dropChance(CommandContext args, CommandSender sender, SentinelTrait sentinel) {
        if (args.argsLength() < 3) {
            if (sentinel.drops.isEmpty()) {
                sender.sendMessage(SentinelCommand.prefixBad + "No drops currently set on this NPC. Use /sentinel drops");
                sentinel.dropChances.clear();
                return;
            }
            for (int i = 0; i < sentinel.drops.size(); i++) {
                double chance = 100.0;
                if (i < sentinel.dropChances.size()) {
                    chance = sentinel.dropChances.get(i) * 100.0;
                }
                sender.sendMessage(SentinelCommand.prefixGood + "[" + (i + 1) + "]: " + sentinel.drops.get(i).getType().name() + ": " + chance + "%");
            }
            return;
        }
        int id;
        double chance;
        try {
            id = args.getInteger(1) - 1;
            chance = args.getDouble(2);
        }
        catch (NumberFormatException ex) {
            sender.sendMessage(SentinelCommand.prefixBad + "Input invalid. Arguments must be an integer then a double - /sentinel dropchance ID CHANCE");
            return;
        }
        if (id < 0 || id >= sentinel.drops.size()) {
            sender.sendMessage(SentinelCommand.prefixBad + "Input invalid. ID must be a valid listed ID - to see valid IDs, type /sentinel dropchance");
            return;
        }
        if (chance < 0 || chance > 100) {
            sender.sendMessage(SentinelCommand.prefixBad + "Input invalid. Chance must be from 0 to 100 (percent).");
            return;
        }
        while (sentinel.dropChances.size() > sentinel.drops.size()) {
            sentinel.dropChances.remove(sentinel.drops.size());
        }
        while (sentinel.dropChances.size() < sentinel.drops.size()) {
            sentinel.dropChances.add(1.0);
        }
        sentinel.dropChances.set(id, chance * 0.01);
        sender.sendMessage(SentinelCommand.prefixGood + "Drop chance for " + sentinel.drops.get(id).getType().name() + " set to " + chance + "%.");
    }

    @Command(aliases = {"sentinel"}, usage = "spawnpoint",
            desc = "Changes the NPC's spawn point to its current location, or removes it if it's already there.",
            modifiers = {"spawnpoint"}, permission = "sentinel.spawnpoint", min = 1, max = 1)
    @Requirements(livingEntity = true, ownership = true, traits = {SentinelTrait.class})
    public void spawnpoint(CommandContext args, CommandSender sender, SentinelTrait sentinel) {
        if (!sentinel.getNPC().isSpawned()) {
            sender.sendMessage(SentinelCommand.prefixBad + "NPC must be spawned for this command!");
            return;
        }
        Location pos = sentinel.getLivingEntity().getLocation().getBlock().getLocation();
        if (sentinel.spawnPoint != null
                && pos.getBlockX() == sentinel.spawnPoint.getBlockX()
                && pos.getBlockY() == sentinel.spawnPoint.getBlockY()
                && pos.getBlockZ() == sentinel.spawnPoint.getBlockZ()
                && pos.getWorld().getName().equals(sentinel.spawnPoint.getWorld().getName())) {
            sentinel.spawnPoint = null;
            sender.sendMessage(SentinelCommand.prefixGood + "Spawn point removed!");
        }
        else {
            sentinel.spawnPoint = pos.add(0.5, 0.0, 0.5);
            sentinel.spawnPoint.setYaw(sentinel.getLivingEntity().getLocation().getYaw());
            sender.sendMessage(SentinelCommand.prefixGood + "Spawn point updated!");
        }
    }
}
