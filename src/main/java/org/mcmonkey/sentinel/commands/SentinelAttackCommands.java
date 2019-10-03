package org.mcmonkey.sentinel.commands;

import net.citizensnpcs.api.command.Command;
import net.citizensnpcs.api.command.CommandContext;
import net.citizensnpcs.api.command.Requirements;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.mcmonkey.sentinel.SentinelPlugin;
import org.mcmonkey.sentinel.SentinelTrait;

public class SentinelAttackCommands {

    @Command(aliases = {"sentinel"}, usage = "enemydrops ['true'/'false']",
            desc = "Toggles whether enemy mobs of this NPC drop items.",
            modifiers = {"enemydrops"}, permission = "sentinel.enemydrops", min = 1, max = 2)
    @Requirements(livingEntity = true, ownership = true, traits = {SentinelTrait.class})
    public void enemyDrops(CommandContext args, CommandSender sender, SentinelTrait sentinel) {
        boolean mode = !sentinel.enemyDrops;
        if (args.argsLength() > 1 && "true".equalsIgnoreCase(args.getString(1))) {
            mode = true;
        }
        if (args.argsLength() > 1 && "false".equalsIgnoreCase(args.getString(1))) {
            mode = false;
        }
        sentinel.enemyDrops = mode;
        if (sentinel.enemyDrops) {
            sender.sendMessage(SentinelCommand.prefixGood + "NPC enemy mobs now drop items and XP!");
        }
        else {
            sender.sendMessage(SentinelCommand.prefixGood + "NPC enemy mobs no longer drop items and XP!");
        }
    }

    @Command(aliases = {"sentinel"}, usage = "accuracy OFFSET",
            desc = "Sets the accuracy of an NPC.",
            modifiers = {"accuracy"}, permission = "sentinel.accuracy", min = 1, max = 2)
    @Requirements(livingEntity = true, ownership = true, traits = {SentinelTrait.class})
    public void accuracy(CommandContext args, CommandSender sender, SentinelTrait sentinel) {
        if (args.argsLength() <= 1) {
            sender.sendMessage(SentinelCommand.prefixGood + "Current accuracy offset: " + ChatColor.AQUA + sentinel.accuracy);
            return;
        }
        try {
            double d = args.getDouble(1);
            if (d >= 0 && d <= 10) {
                sentinel.accuracy = d;
                sender.sendMessage(SentinelCommand.prefixGood + "Accuracy offset set!");
            }
            else {
                throw new NumberFormatException("Number out of range (must be >= 0 and <= 10).");
            }
        }
        catch (NumberFormatException ex) {
            sender.sendMessage(SentinelCommand.prefixBad + "Invalid accuracy offset number: " + ex.getMessage());
        }
    }

    @Command(aliases = {"sentinel"}, usage = "reach REACH",
            desc = "Sets the NPC's reach (how far it can punch).",
            modifiers = {"reach"}, permission = "sentinel.reach", min = 1, max = 2)
    @Requirements(livingEntity = true, ownership = true, traits = {SentinelTrait.class})
    public void reach(CommandContext args, CommandSender sender, SentinelTrait sentinel) {
        if (args.argsLength() <= 1) {
            sender.sendMessage(SentinelCommand.prefixGood + "Current reach: " + ChatColor.AQUA + sentinel.reach);
            return;
        }
        try {
            double d = args.getDouble(1);
            if (d >= 0) {
                sentinel.reach = d;
                sender.sendMessage(SentinelCommand.prefixGood + "Reach set!");
            }
            else {
                throw new NumberFormatException("Number out of range (must be >= 0).");
            }
        }
        catch (NumberFormatException ex) {
            sender.sendMessage(SentinelCommand.prefixBad + "Invalid reach number: " + ex.getMessage());
        }
    }

    @Command(aliases = {"sentinel"}, usage = "attackrate RATE ['ranged']",
            desc = "Changes the rate at which the NPC attacks, in seconds - either ranged or close modes.",
            modifiers = {"attackrate"}, permission = "sentinel.attackrate", min = 1, max = 3)
    @Requirements(livingEntity = true, ownership = true, traits = {SentinelTrait.class})
    public void attackRate(CommandContext args, CommandSender sender, SentinelTrait sentinel) {
        if (args.argsLength() <= 1) {
            sender.sendMessage(SentinelCommand.prefixGood + "Current attack rate (close): " + ChatColor.AQUA + (sentinel.attackRate / 20.0));
            sender.sendMessage(SentinelCommand.prefixGood + "Current attack rate (ranged): " + ChatColor.AQUA + (sentinel.attackRateRanged / 20.0));
            return;
        }
        try {
            double da = args.getDouble(1);
            int d = (int) (da * 20);
            if (d >= SentinelPlugin.instance.tickRate && d <= SentinelTrait.attackRateMax) {
                if (args.argsLength() > 2 && args.getString(2).toLowerCase().contains("ranged")) {
                    sentinel.attackRateRanged = d;
                    sender.sendMessage(SentinelCommand.prefixGood + "Ranged attack rate set!");
                }
                else {
                    sentinel.attackRate = d;
                    sender.sendMessage(SentinelCommand.prefixGood + "Attack rate set!");
                }
            }
            else {
                throw new NumberFormatException("Number out of range (must be >= " + (SentinelPlugin.instance.tickRate / 20.0) + " and <= " + (SentinelTrait.attackRateMax / 20.0) + ").");
            }
        }
        catch (NumberFormatException ex) {
            sender.sendMessage(SentinelCommand.prefixBad + "Invalid rate number: " + ex.getMessage());
        }
    }

    @Command(aliases = {"sentinel"}, usage = "range RANGE",
            desc = "Sets the NPC's maximum attack range.",
            modifiers = {"range"}, permission = "sentinel.range", min = 1, max = 2)
    @Requirements(livingEntity = true, ownership = true, traits = {SentinelTrait.class})
    public void range(CommandContext args, CommandSender sender, SentinelTrait sentinel) {
        if (args.argsLength() <= 1) {
            sender.sendMessage(SentinelCommand.prefixGood + "Current range: " + ChatColor.AQUA + sentinel.range);
            return;
        }
        try {
            double d = args.getDouble(1);
            if (d > 0 && d < 200) {
                sentinel.range = d;
                sender.sendMessage(SentinelCommand.prefixGood + "Range set!");
            }
            else {
                throw new NumberFormatException("Number out of range (must be > 0 and < 200).");
            }
        }
        catch (NumberFormatException ex) {
            sender.sendMessage(SentinelCommand.prefixBad + "Invalid range number: " + ex.getMessage());
        }
    }

    @Command(aliases = {"sentinel"}, usage = "damage DAMAGE",
            desc = "Sets the NPC's attack damage.",
            modifiers = {"damage"}, permission = "sentinel.damage", min = 1, max = 2)
    @Requirements(livingEntity = true, ownership = true, traits = {SentinelTrait.class})
    public void damage(CommandContext args, CommandSender sender, SentinelTrait sentinel) {
        if (args.argsLength() <= 1) {
            sender.sendMessage(SentinelCommand.prefixGood + "Current damage: " + ChatColor.AQUA + sentinel.damage
                    + SentinelCommand.colorBasic + " Calculated: " + ChatColor.AQUA + sentinel.getDamage());
            return;
        }
        try {
            double d = args.getDouble(1);
            if (d < SentinelPlugin.instance.maxHealth) {
                sentinel.damage = d;
                sender.sendMessage(SentinelCommand.prefixGood + "Damage set!");
            }
            else {
                throw new NumberFormatException("Number out of range (must be < " + SentinelPlugin.instance.maxHealth + ").");
            }
        }
        catch (NumberFormatException ex) {
            sender.sendMessage(SentinelCommand.prefixBad + "Invalid damage number: " + ex.getMessage());
        }
    }

    @Command(aliases = {"sentinel"}, usage = "weapondamage MATERIAL DAMAGE",
            desc = "Sets the NPC's attack damage for a specific weapon material.",
            modifiers = {"weapondamage"}, permission = "sentinel.weapondamage", min = 2, max = 3)
    @Requirements(livingEntity = true, ownership = true, traits = {SentinelTrait.class})
    public void weaponDamage(CommandContext args, CommandSender sender, SentinelTrait sentinel) {
        String weapon = args.getString(1).toLowerCase();
        try {
            Material.valueOf(weapon.toUpperCase());
        }
        catch (IllegalArgumentException ex) {
            sender.sendMessage(SentinelCommand.prefixBad + "Invalid weapon material (name misspelled?)");
            return;
        }
        if (args.argsLength() <= 2) {
            Double damage = sentinel.weaponDamage.get(weapon);
            sender.sendMessage(SentinelCommand.prefixGood + "Current weapon damage for '" + weapon + "': " + ChatColor.AQUA + (damage == null ? "Unset" : damage));
            return;
        }
        try {
            double d = args.getDouble(2);
            if (d < SentinelPlugin.instance.maxHealth) {
                if (d < 0) {
                    sentinel.weaponDamage.remove(weapon);
                    sender.sendMessage(SentinelCommand.prefixGood + "Weapon damage removed!");
                }
                else {
                    sentinel.weaponDamage.put(weapon, d);
                    sender.sendMessage(SentinelCommand.prefixGood + "Weapon damage set!");
                }
            }
            else {
                throw new NumberFormatException("Number out of range (must be < " + SentinelPlugin.instance.maxHealth + ").");
            }
        }
        catch (NumberFormatException ex) {
            sender.sendMessage(SentinelCommand.prefixBad + "Invalid weapon damage number: " + ex.getMessage());
        }
    }

    @Command(aliases = {"sentinel"}, usage = "weaponredirect MATERIAL_ONE MATERIAL_TWO",
            desc = "Sets the NPC to treat material one as though it's material two.",
            modifiers = {"weaponredirect"}, permission = "sentinel.weaponredirect", min = 2, max = 3)
    @Requirements(livingEntity = true, ownership = true, traits = {SentinelTrait.class})
    public void weaponRedirect(CommandContext args, CommandSender sender, SentinelTrait sentinel) {
        String weaponOne = args.getString(1).toLowerCase();
        try {
            Material.valueOf(weaponOne.toUpperCase());
        }
        catch (IllegalArgumentException ex) {
            sender.sendMessage(SentinelCommand.prefixBad + "Invalid weapon-one material (name misspelled?)");
            return;
        }
        if (args.argsLength() <= 2) {
            String redirect = sentinel.weaponRedirects.get(weaponOne);
            sender.sendMessage(SentinelCommand.prefixGood + "Current weapon redirect for '" + weaponOne + "': " + ChatColor.AQUA + (redirect == null ? "Unset" : redirect));
            return;
        }
        String weaponTwo = args.getString(2).toLowerCase();
        try {
            Material.valueOf(weaponTwo.toUpperCase());
        }
        catch (IllegalArgumentException ex) {
            sender.sendMessage(SentinelCommand.prefixBad + "Invalid weapon-two material (name misspelled?)");
            return;
        }
        sentinel.weaponRedirects.put(weaponOne, weaponTwo);
        sender.sendMessage(SentinelCommand.prefixGood + "Weapon redirect set!");
    }

    @Command(aliases = {"sentinel"}, usage = "safeshot ['true'/'false']",
            desc = "Toggles whether the NPC will avoid damaging non-targets.",
            modifiers = {"safeshot"}, permission = "sentinel.safeshot", min = 1, max = 2)
    @Requirements(livingEntity = true, ownership = true, traits = {SentinelTrait.class})
    public void safeShot(CommandContext args, CommandSender sender, SentinelTrait sentinel) {
        boolean mode = !sentinel.safeShot;
        if (args.argsLength() > 1 && "true".equalsIgnoreCase(args.getString(1))) {
            mode = true;
        }
        if (args.argsLength() > 1 && "false".equalsIgnoreCase(args.getString(1))) {
            mode = false;
        }
        sentinel.safeShot = mode;
        if (sentinel.safeShot) {
            sender.sendMessage(SentinelCommand.prefixGood + "NPC now is a safe shot!");
        }
        else {
            sender.sendMessage(SentinelCommand.prefixGood + "NPC is no longer a safe shot!");
        }
    }
}
