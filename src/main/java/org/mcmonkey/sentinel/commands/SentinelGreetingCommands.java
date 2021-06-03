package org.mcmonkey.sentinel.commands;

import net.citizensnpcs.api.command.Command;
import net.citizensnpcs.api.command.CommandContext;
import net.citizensnpcs.api.command.Requirements;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.mcmonkey.sentinel.SentinelTrait;
import org.mcmonkey.sentinel.SentinelUtilities;

public class SentinelGreetingCommands {

    @Command(aliases = {"sentinel"}, usage = "greetrange GREETRANGE",
            desc = "Sets how far a player can be from an NPC before they are greeted.",
            modifiers = {"greetrange"}, permission = "sentinel.greet", min = 1, max = 2)
    @Requirements(livingEntity = true, ownership = true, traits = {SentinelTrait.class})
    public void greetRange(CommandContext args, CommandSender sender, SentinelTrait sentinel) {
        if (args.argsLength() <= 1) {
            sender.sendMessage(SentinelCommand.prefixGood + "Current greet range: " + ChatColor.AQUA + sentinel.greetRange);
            return;
        }
        try {
            Double d = args.getDouble(1);
            if (d < 100) {
                sentinel.greetRange = d;
                sender.sendMessage(SentinelCommand.prefixGood + "Range set!");
            }
            else {
                throw new NumberFormatException("Number out of range (must be < 100).");
            }
        }
        catch (NumberFormatException ex) {
            sender.sendMessage(SentinelCommand.prefixBad + "Invalid range number: " + ex.getMessage());
        }
        return;
    }

    @Command(aliases = {"sentinel"}, usage = "greetrate GREETRATE",
            desc = "Sets how quickly (in seconds) the Sentinel may re-greet any player.",
            modifiers = {"greetrate"}, permission = "sentinel.greet", min = 1, max = 2)
    @Requirements(livingEntity = true, ownership = true, traits = {SentinelTrait.class})
    public void greetRate(CommandContext args, CommandSender sender, SentinelTrait sentinel) {
        if (args.argsLength() <= 1) {
            sender.sendMessage(SentinelCommand.prefixGood + "Current rate: " + ChatColor.AQUA + (sentinel.greetRate / 20.0));
            return;
        }
        try {
            Double d = args.getDouble(1);
            if (d >= 0) {
                sentinel.greetRate = (int) (d * 20);
                sender.sendMessage(SentinelCommand.prefixGood + "Rate set!");
            }
            else {
                throw new NumberFormatException("Number out of range (must be >= 0).");
            }
        }
        catch (NumberFormatException ex) {
            sender.sendMessage(SentinelCommand.prefixBad + "Invalid rate number: " + ex.getMessage());
        }
        return;
    }

    @Command(aliases = {"sentinel"}, usage = "greeting GREETING",
            desc = "Sets a greeting message for the NPC to say.",
            modifiers = {"greeting"}, permission = "sentinel.greet", min = 1, max = 9999)
    @Requirements(livingEntity = true, ownership = true, traits = {SentinelTrait.class})
    public void greeting(CommandContext args, CommandSender sender, SentinelTrait sentinel) {
        sentinel.greetingText = SentinelUtilities.concatWithSpaces(args.getSlice(1), 0);
        sender.sendMessage(SentinelCommand.prefixGood + "Set!");
    }

    @Command(aliases = {"sentinel"}, usage = "warning WARNING",
            desc = "Sets a warning message for the NPC to say.",
            modifiers = {"warning"}, permission = "sentinel.greet", min = 1, max = 9999)
    @Requirements(livingEntity = true, ownership = true, traits = {SentinelTrait.class})
    public void warning(CommandContext args, CommandSender sender, SentinelTrait sentinel) {
        sentinel.warningText = SentinelUtilities.concatWithSpaces(args.getSlice(1), 0);
        sender.sendMessage(SentinelCommand.prefixGood + "Set!");
    }
}
