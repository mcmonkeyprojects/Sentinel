package org.mcmonkey.sentinel.commands;

import net.citizensnpcs.api.command.Command;
import net.citizensnpcs.api.command.CommandContext;
import net.citizensnpcs.api.command.Requirements;
import org.bukkit.command.CommandSender;
import org.mcmonkey.sentinel.SentinelTrait;

public class SentinelChaseCommands {

    @Command(aliases = {"sentinel"}, usage = "speed SPEED",
            desc = "Sets the NPC's movement speed modifier.",
            modifiers = {"speed"}, permission = "sentinel.speed", min = 2, max = 2)
    @Requirements(livingEntity = true, ownership = true, traits = {SentinelTrait.class})
    public void speed(CommandContext args, CommandSender sender, SentinelTrait sentinel) {
        try {
            Double d = Double.parseDouble(args.getString(1));
            if (d < 1000 && d >= 0) {
                sentinel.speed = d;
                sender.sendMessage(SentinelCommand.prefixGood + "Speed set!");
            }
            else {
                throw new NumberFormatException("Number out of range (must be >= 0 and < 1000).");
            }
        }
        catch (NumberFormatException ex) {
            sender.sendMessage(SentinelCommand.prefixBad + "Invalid speed number: " + ex.getMessage());
        }
    }

    @Command(aliases = {"sentinel"}, usage = "chaserange RANGE",
            desc = "Changes the maximum distance an NPC will run before returning to base.",
            modifiers = {"chaserange"}, permission = "sentinel.chaserange", min = 2, max = 2)
    @Requirements(livingEntity = true, ownership = true, traits = {SentinelTrait.class})
    public void chaseRange(CommandContext args, CommandSender sender, SentinelTrait sentinel) {
        try {
            double d = Double.parseDouble(args.getString(1));
            sentinel.chaseRange = d;
            sender.sendMessage(SentinelCommand.prefixGood + "Chase range set!");
        }
        catch (NumberFormatException ex) {
            sender.sendMessage(SentinelCommand.prefixBad + "Invalid range number: " + ex.getMessage());
        }
    }

    @Command(aliases = {"sentinel"}, usage = "chaseclose ['true'/'false']",
            desc = "Toggles whether the NPC will chase while in 'close quarters' fights.",
            modifiers = {"chaseclose"}, permission = "sentinel.chase", min = 1, max = 2)
    @Requirements(livingEntity = true, ownership = true, traits = {SentinelTrait.class})
    public void chaseClose(CommandContext args, CommandSender sender, SentinelTrait sentinel) {
        boolean mode = !sentinel.closeChase;
        if (args.argsLength() > 1 && "true".equalsIgnoreCase(args.getString(1))) {
            mode = true;
        }
        if (args.argsLength() > 1 && "false".equalsIgnoreCase(args.getString(1))) {
            mode = false;
        }
        sentinel.closeChase = mode;
        if (sentinel.closeChase) {
            sender.sendMessage(SentinelCommand.prefixGood + "NPC now will chase while close!");
        }
        else {
            sender.sendMessage(SentinelCommand.prefixGood + "NPC no longer will chase while close!");
        }
    }

    @Command(aliases = {"sentinel"}, usage = "chaseranged ['true'/'false']",
            desc = "Toggles whether the NPC will chase while in ranged fights.",
            modifiers = {"chaseranged"}, permission = "sentinel.chase", min = 1, max = 2)
    @Requirements(livingEntity = true, ownership = true, traits = {SentinelTrait.class})
    public void chaseRanged(CommandContext args, CommandSender sender, SentinelTrait sentinel) {
        boolean mode = !sentinel.rangedChase;
        if (args.argsLength() > 1 && "true".equalsIgnoreCase(args.getString(1))) {
            mode = true;
        }
        if (args.argsLength() > 1 && "false".equalsIgnoreCase(args.getString(1))) {
            mode = false;
        }
        sentinel.rangedChase = mode;
        if (sentinel.rangedChase) {
            sender.sendMessage(SentinelCommand.prefixGood + "NPC now will chase while ranged!");
        }
        else {
            sender.sendMessage(SentinelCommand.prefixGood + "NPC no longer will chase while ranged!");
        }
    }
}
