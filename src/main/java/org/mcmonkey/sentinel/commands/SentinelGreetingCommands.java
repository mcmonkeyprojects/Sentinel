package org.mcmonkey.sentinel.commands;

import net.citizensnpcs.api.command.Command;
import net.citizensnpcs.api.command.CommandContext;
import net.citizensnpcs.api.command.Requirements;
import org.bukkit.command.CommandSender;
import org.mcmonkey.sentinel.SentinelTrait;
import org.mcmonkey.sentinel.SentinelUtilities;

public class SentinelGreetingCommands {


    @Command(aliases = {"sentinel"}, usage = "greetrange GREETRANGE",
            desc = "Sets how far a player can be from an NPC before they are greeted.",
            modifiers = {"greetrange"}, permission = "sentinel.greet", min = 2, max = 2)
    @Requirements(livingEntity = true, ownership = true, traits = {SentinelTrait.class})
    public void greetRange(CommandContext args, CommandSender sender, SentinelTrait sentinel) {
        try {
            Double d = Double.parseDouble(args.getString(1));
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

    @Command(aliases = {"sentinel"}, usage = "greeting GREETING",
            desc = "Sets a greeting message for the NPC to say.",
            modifiers = {"greeting"}, permission = "sentinel.greet", min = 1, max = 9999)
    @Requirements(livingEntity = true, ownership = true, traits = {SentinelTrait.class})
    public void greeting(CommandContext args, CommandSender sender, SentinelTrait sentinel) {
        sentinel.greetingText = SentinelUtilities.concatWithSpaces(args.getSlice(1), 1);
        sender.sendMessage(SentinelCommand.prefixGood + "Set!");
    }

    @Command(aliases = {"sentinel"}, usage = "warning WARNING",
            desc = "Sets a warning message for the NPC to say.",
            modifiers = {"warning"}, permission = "sentinel.greet", min = 1, max = 9999)
    @Requirements(livingEntity = true, ownership = true, traits = {SentinelTrait.class})
    public void warning(CommandContext args, CommandSender sender, SentinelTrait sentinel) {
        sentinel.warningText = SentinelUtilities.concatWithSpaces(args.getSlice(1), 1);
        sender.sendMessage(SentinelCommand.prefixGood + "Set!");
    }
}
