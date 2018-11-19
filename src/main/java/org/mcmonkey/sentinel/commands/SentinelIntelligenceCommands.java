package org.mcmonkey.sentinel.commands;

import net.citizensnpcs.api.command.Command;
import net.citizensnpcs.api.command.CommandContext;
import net.citizensnpcs.api.command.Requirements;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.mcmonkey.sentinel.SentinelTrait;
import org.mcmonkey.sentinel.SentinelUtilities;

import java.util.Locale;

public class SentinelIntelligenceCommands {

    @Command(aliases = {"sentinel"}, usage = "squad SQUAD",
            desc = "Sets the NPC's squad name (give blank name for none).",
            modifiers = {"squad"}, permission = "sentinel.squad", min = 1, max = 9999)
    @Requirements(livingEntity = true, ownership = true, traits = {SentinelTrait.class})
    public void squad(CommandContext args, CommandSender sender, SentinelTrait sentinel) {
        sentinel.squad = SentinelUtilities.concatWithSpaces(args.getSlice(1), 1).toLowerCase(Locale.ENGLISH);
        if (sentinel.squad.equals("null") || sentinel.squad.length() == 0) {
            sentinel.squad = null;
        }
        sender.sendMessage(SentinelCommand.prefixGood + "Set!");
    }

    @Command(aliases = {"sentinel"}, usage = "guard [PLAYERNAME]",
            desc = "Makes the NPC guard a specific player - don't specify a player to stop guarding.",
            modifiers = {"guard"}, permission = "sentinel.guard", min = 1, max = 2)
    @Requirements(livingEntity = true, ownership = true, traits = {SentinelTrait.class})
    public void guard(CommandContext args, CommandSender sender, SentinelTrait sentinel) {
        if (args.argsLength() > 1) {
            Player pl = Bukkit.getPlayer(args.getString(1));
            sentinel.setGuarding(pl == null ? null : pl.getUniqueId());
        }
        else {
            sentinel.setGuarding(null);
        }
        if (sentinel.getGuarding() == null) {
            sender.sendMessage(SentinelCommand.prefixGood + "NPC now guarding its area!");
        }
        else {
            sender.sendMessage(SentinelCommand.prefixGood + "NPC now guarding that player!");
        }
    }

    @Command(aliases = {"sentinel"}, usage = "needammo ['true'/'false']",
            desc = "Toggles whether the NPC will need ammo.",
            modifiers = {"needammo"}, permission = "sentinel.needammo", min = 1, max = 2)
    @Requirements(livingEntity = true, ownership = true, traits = {SentinelTrait.class})
    public void needAmmo(CommandContext args, CommandSender sender, SentinelTrait sentinel) {
        boolean mode = !sentinel.needsAmmo;
        if (args.argsLength() > 1 && "true".equalsIgnoreCase(args.getString(1))) {
            mode = true;
        }
        if (args.argsLength() > 1 && "false".equalsIgnoreCase(args.getString(1))) {
            mode = false;
        }
        sentinel.needsAmmo = mode;
        if (sentinel.needsAmmo) {
            sender.sendMessage(SentinelCommand.prefixGood + "NPC now needs ammo!");
        }
        else {
            sender.sendMessage(SentinelCommand.prefixGood + "NPC no longer needs ammo!");
        }
    }

    @Command(aliases = {"sentinel"}, usage = "autoswitch ['true'/'false']",
            desc = "Toggles whether the NPC automatically switches items.",
            modifiers = {"autoswitch"}, permission = "sentinel.autoswitch", min = 1, max = 2)
    @Requirements(livingEntity = true, ownership = true, traits = {SentinelTrait.class})
    public void autoswitch(CommandContext args, CommandSender sender, SentinelTrait sentinel) {
        boolean mode = !sentinel.autoswitch;
        if (args.argsLength() > 1 && "true".equalsIgnoreCase(args.getString(1))) {
            mode = true;
        }
        if (args.argsLength() > 1 && "false".equalsIgnoreCase(args.getString(1))) {
            mode = false;
        }
        sentinel.autoswitch = mode;
        if (sentinel.autoswitch) {
            sender.sendMessage(SentinelCommand.prefixGood + "NPC now automatically switches items!");
        }
        else {
            sender.sendMessage(SentinelCommand.prefixGood + "NPC no longer automatically switches items!");
        }
    }

    @Command(aliases = {"sentinel"}, usage = "realistic ['true'/'false']",
            desc = "Toggles whether the NPC should use 'realistic' targeting logic (don't attack things you can't see.)",
            modifiers = {"realistic"}, permission = "sentinel.realistic", min = 1, max = 2)
    @Requirements(livingEntity = true, ownership = true, traits = {SentinelTrait.class})
    public void realistic(CommandContext args, CommandSender sender, SentinelTrait sentinel) {
        boolean mode = !sentinel.realistic;
        if (args.argsLength() > 1 && "true".equalsIgnoreCase(args.getString(1))) {
            mode = true;
        }
        if (args.argsLength() > 1 && "false".equalsIgnoreCase(args.getString(1))) {
            mode = false;
        }
        sentinel.realistic = mode;
        if (sentinel.realistic) {
            sender.sendMessage(SentinelCommand.prefixGood + "NPC now targets realistically!");
        }
        else {
            sender.sendMessage(SentinelCommand.prefixGood + "NPC no longer targets realistically!");
        }
    }
}
