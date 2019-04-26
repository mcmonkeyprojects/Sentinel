package org.mcmonkey.sentinel.commands;

import net.citizensnpcs.api.command.Command;
import net.citizensnpcs.api.command.CommandContext;
import net.citizensnpcs.api.command.Requirements;
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
            modifiers = {"accuracy"}, permission = "sentinel.accuracy", min = 2, max = 2)
    @Requirements(livingEntity = true, ownership = true, traits = {SentinelTrait.class})
    public void accuracy(CommandContext args, CommandSender sender, SentinelTrait sentinel) {
        try {
            double d = Double.parseDouble(args.getString(1));
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
            modifiers = {"reach"}, permission = "sentinel.reach", min = 2, max = 2)
    @Requirements(livingEntity = true, ownership = true, traits = {SentinelTrait.class})
    public void reach(CommandContext args, CommandSender sender, SentinelTrait sentinel) {
        try {
            double d = Double.parseDouble(args.getString(1));
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
            modifiers = {"attackrate"}, permission = "sentinel.attackrate", min = 2, max = 3)
    @Requirements(livingEntity = true, ownership = true, traits = {SentinelTrait.class})
    public void attackRate(CommandContext args, CommandSender sender, SentinelTrait sentinel) {
        try {
            double da = Double.parseDouble(args.getString(1));
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
            modifiers = {"range"}, permission = "sentinel.range", min = 2, max = 2)
    @Requirements(livingEntity = true, ownership = true, traits = {SentinelTrait.class})
    public void range(CommandContext args, CommandSender sender, SentinelTrait sentinel) {
        try {
            double d = Double.parseDouble(args.getString(1));
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
            modifiers = {"damage"}, permission = "sentinel.damage", min = 2, max = 2)
    @Requirements(livingEntity = true, ownership = true, traits = {SentinelTrait.class})
    public void damage(CommandContext args, CommandSender sender, SentinelTrait sentinel) {
        try {
            Double d = Double.parseDouble(args.getString(1));
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
