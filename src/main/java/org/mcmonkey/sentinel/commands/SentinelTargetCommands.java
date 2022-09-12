package org.mcmonkey.sentinel.commands;

import net.citizensnpcs.api.command.Command;
import net.citizensnpcs.api.command.CommandContext;
import net.citizensnpcs.api.command.Requirements;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.mcmonkey.sentinel.SentinelCurrentTarget;
import org.mcmonkey.sentinel.SentinelPlugin;
import org.mcmonkey.sentinel.SentinelTrait;
import org.mcmonkey.sentinel.SentinelUtilities;
import org.mcmonkey.sentinel.targeting.SentinelTargetLabel;
import org.mcmonkey.sentinel.targeting.SentinelTargetList;

import java.util.Collection;
import java.util.UUID;

/**
 * Commands related to targeting.
 */
public class SentinelTargetCommands {

    public static void outputEntireTargetsList(CommandSender sender, SentinelTargetList list, String prefixType) {
        boolean any = false;
        any = any | outputTargetsList(sender, prefixType + " by Type", list.targets);
        any = any | outputTargetsList(sender, prefixType + " by Player Name", list.byPlayerName);
        any = any | outputTargetsList(sender, prefixType + " by NPC Name", list.byNpcName);
        any = any | outputTargetsList(sender, prefixType + " by Entity Name", list.byEntityName);
        any = any | outputTargetsList(sender, prefixType + " by Held Item", list.byHeldItem);
        any = any | outputTargetsList(sender, prefixType + " by Offhand Item", list.byOffhandItem);
        any = any | outputTargetsList(sender, prefixType + " by Equipped Item", list.byEquippedItem);
        any = any | outputTargetsList(sender, prefixType + " by Inventory-carried Item", list.byInventoryItem);
        any = any | outputTargetsList(sender, prefixType + " by Permissions Group", list.byGroup);
        any = any | outputTargetsList(sender, prefixType + " by Event", list.byEvent);
        any = any | outputTargetsList(sender, prefixType + " by Status", list.byStatus);
        any = any | outputTargetsList(sender, prefixType + " by Other", list.byOther);
        if (!list.byAllInOne.isEmpty()) {
            for (int i = 0; i < list.byAllInOne.size(); i++) {
                sender.sendMessage(SentinelCommand.prefixGood + prefixType + " by All-In-One ("
                        + SentinelCommand.colorEmphasis + i + SentinelCommand.colorBasic + "): "
                        + SentinelCommand.colorEmphasis + list.byAllInOne.get(i).toAllInOneString());
            }
            any = true;
        }
        if (!list.byMultiple.isEmpty()) {
            for (int i = 0; i < list.byMultiple.size(); i++) {
                sender.sendMessage(SentinelCommand.prefixGood + prefixType + " by Multiple ("
                        + SentinelCommand.colorEmphasis + i + SentinelCommand.colorBasic + "): "
                        + SentinelCommand.colorEmphasis + list.byMultiple.get(i).toMultiTargetString());
            }
            any = true;
        }
        if (!any) {
            sender.sendMessage(SentinelCommand.prefixGood + prefixType + ": Nothing.");
        }
    }

    public static boolean outputTargetsList(CommandSender sender, String label, Collection<String> targets) {
        if (!targets.isEmpty()) {
            sender.sendMessage(SentinelCommand.prefixGood + label + ": " + SentinelCommand.colorEmphasis + getNameTargetString(targets));
            return true;
        }
        return false;
    }

    public static boolean testLabel(CommandSender sender, SentinelTargetLabel label) {
        if (!label.isValidTarget()) {
            sender.sendMessage(SentinelCommand.prefixBad + "Invalid target! See the readme for a list of valid targets.");
            return false;
        }
        if (!label.isValidRegex()) {
            sender.sendMessage(SentinelCommand.prefixBad + "Bad regular expression!");
            return false;
        }
        if (!label.isValidPrefix()) {
            sender.sendMessage(SentinelCommand.prefixBad + "The target prefix '" + label.prefix + "' is unknown!");
            return false;
        }
        if (!label.isValidMulti()) {
            sender.sendMessage(SentinelCommand.prefixBad + "The multi-target '" + label.value + "' is invalid (targets within don't exist?)!");
            return false;
        }
        return true;
    }

    /**
     * Gets a string holding all name targets.
     */
    public static String getNameTargetString(Collection<String> targetList) {
        StringBuilder targets = new StringBuilder();
        for (String str : targetList) {
            targets.append(str).append(", ");
        }
        return targets.length() > 0 ? targets.substring(0, targets.length() - 2) : targets.toString();
    }

    @Command(aliases = {"sentinel"}, usage = "addtarget TYPE",
            desc = "Adds a target.",
            modifiers = {"addtarget"}, permission = "sentinel.addtarget", min = 2, max = 2)
    @Requirements(livingEntity = true, ownership = true, traits = {SentinelTrait.class})
    public void addTarget(CommandContext args, CommandSender sender, SentinelTrait sentinel) {
        SentinelTargetLabel targetLabel = new SentinelTargetLabel(args.getString(1));
        if (!testLabel(sender, targetLabel)) {
            return;
        }
        if (targetLabel.addToList(sentinel.allTargets)) {
            sender.sendMessage(SentinelCommand.prefixGood + "Tracking new target!");
        }
        else {
            sender.sendMessage(SentinelCommand.prefixBad + "Already tracking that target!");
        }
    }

    @Command(aliases = {"sentinel"}, usage = "removetarget TYPE",
            desc = "Removes a target.",
            modifiers = {"removetarget"}, permission = "sentinel.removetarget", min = 2, max = 2)
    @Requirements(livingEntity = true, ownership = true, traits = {SentinelTrait.class})
    public void removeTarget(CommandContext args, CommandSender sender, SentinelTrait sentinel) {
        SentinelTargetLabel targetLabel = new SentinelTargetLabel(args.getString(1));
        if (!testLabel(sender, targetLabel)) {
            return;
        }
        if (targetLabel.removeFromList(sentinel.allTargets)) {
            sender.sendMessage(SentinelCommand.prefixGood + "No longer tracking that target!");
        }
        else {
            sender.sendMessage(SentinelCommand.prefixBad + "Was already not tracking that target!");
        }
    }

    @Command(aliases = {"sentinel"}, usage = "addignore TYPE",
            desc = "Ignores a target.",
            modifiers = {"addignore"}, permission = "sentinel.addignore", min = 2, max = 2)
    @Requirements(livingEntity = true, ownership = true, traits = {SentinelTrait.class})
    public void addIgnore(CommandContext args, CommandSender sender, SentinelTrait sentinel) {
        SentinelTargetLabel targetLabel = new SentinelTargetLabel(args.getString(1));
        if (!testLabel(sender, targetLabel)) {
            return;
        }
        if (targetLabel.addToList(sentinel.allIgnores)) {
            sender.sendMessage(SentinelCommand.prefixGood + "Ignoring new target!");
        }
        else {
            sender.sendMessage(SentinelCommand.prefixBad + "Already ignoring that target!");
        }
    }

    @Command(aliases = {"sentinel"}, usage = "removeignore TYPE",
            desc = "Allows targeting a target.",
            modifiers = {"removeignore"}, permission = "sentinel.removeignore", min = 2, max = 2)
    @Requirements(livingEntity = true, ownership = true, traits = {SentinelTrait.class})
    public void removeIgnore(CommandContext args, CommandSender sender, SentinelTrait sentinel) {
        SentinelTargetLabel targetLabel = new SentinelTargetLabel(args.getString(1));
        if (!testLabel(sender, targetLabel)) {
            return;
        }
        if (targetLabel.removeFromList(sentinel.allIgnores)) {
            sender.sendMessage(SentinelCommand.prefixGood + "No longer ignoring that target!");
        }
        else {
            sender.sendMessage(SentinelCommand.prefixBad + "Was already not ignoring that target!");
        }
    }

    @Command(aliases = {"sentinel"}, usage = "addavoid TYPE",
            desc = "Avoids a target.",
            modifiers = {"addavoid"}, permission = "sentinel.addavoid", min = 2, max = 2)
    @Requirements(livingEntity = true, ownership = true, traits = {SentinelTrait.class})
    public void addAvoid(CommandContext args, CommandSender sender, SentinelTrait sentinel) {
        SentinelTargetLabel targetLabel = new SentinelTargetLabel(args.getString(1));
        if (!testLabel(sender, targetLabel)) {
            return;
        }
        if (targetLabel.addToList(sentinel.allAvoids)) {
            sender.sendMessage(SentinelCommand.prefixGood + "Avoiding a new target!");
        }
        else {
            sender.sendMessage(SentinelCommand.prefixBad + "Already avoiding that target!");
        }
    }

    @Command(aliases = {"sentinel"}, usage = "removeavoid TYPE",
            desc = "Stops avoiding a target.",
            modifiers = {"removeavoid"}, permission = "sentinel.removeavoid", min = 2, max = 2)
    @Requirements(livingEntity = true, ownership = true, traits = {SentinelTrait.class})
    public void removeAvoid(CommandContext args, CommandSender sender, SentinelTrait sentinel) {
        SentinelTargetLabel targetLabel = new SentinelTargetLabel(args.getString(1));
        if (!testLabel(sender, targetLabel)) {
            return;
        }
        if (targetLabel.removeFromList(sentinel.allAvoids)) {
            sender.sendMessage(SentinelCommand.prefixGood + "No longer avoiding that target!");
        }
        else {
            sender.sendMessage(SentinelCommand.prefixBad + "Was already not tracking that target!");
        }
    }

    @Command(aliases = {"sentinel"}, usage = "avoidrange RANGE",
            desc = "Sets the distance to try to keep from threats.",
            modifiers = {"avoidrange"}, permission = "sentinel.avoidrange", min = 1, max = 2)
    @Requirements(livingEntity = true, ownership = true, traits = {SentinelTrait.class})
    public void avoidRange(CommandContext args, CommandSender sender, SentinelTrait sentinel) {
        if (args.argsLength() <= 1) {
            sender.sendMessage(SentinelCommand.prefixGood + "Current avoid range: " + SentinelCommand.colorEmphasis + sentinel.avoidRange);
            return;
        }
        try {
            double d = Double.parseDouble(args.getString(1));
            if (d >= 4 && d < 100) {
                sentinel.avoidRange = d;
                sender.sendMessage(SentinelCommand.prefixGood + "Avoidance range set!");
            }
            else {
                throw new NumberFormatException("Number out of range (must be >= 4 and < 100).");
            }
        }
        catch (NumberFormatException ex) {
            sender.sendMessage(SentinelCommand.prefixBad + "Invalid range number: " + ex.getMessage());
        }
    }

    @Command(aliases = {"sentinel"}, usage = "targettime TIME",
            desc = "Sets the NPC's enemy target time limit in seconds.",
            modifiers = {"targettime"}, permission = "sentinel.targettime", min = 1, max = 2)
    @Requirements(livingEntity = true, ownership = true, traits = {SentinelTrait.class})
    public void targetTime(CommandContext args, CommandSender sender, SentinelTrait sentinel) {
        if (args.argsLength() <= 1) {
            sender.sendMessage(SentinelCommand.prefixGood + "Current target time: " + SentinelCommand.colorEmphasis + (sentinel.enemyTargetTime / 20.0));
            return;
        }
        try {
            double d = Double.parseDouble(args.getString(1));
            if (d >= 0) {
                sentinel.enemyTargetTime = (long) (d * 20);
                sender.sendMessage(SentinelCommand.prefixGood + "Target time set!");
            }
            else {
                throw new NumberFormatException("Number out of range (must be >= 0).");
            }
        }
        catch (NumberFormatException ex) {
            sender.sendMessage(SentinelCommand.prefixBad + "Invalid time number: " + ex.getMessage());
        }
    }

    @Command(aliases = {"sentinel"}, usage = "runaway ['true'/'false']",
            desc = "Toggles whether the NPC will run away when attacked.",
            modifiers = {"runaway"}, permission = "sentinel.runaway", min = 1, max = 2)
    @Requirements(livingEntity = true, ownership = true, traits = {SentinelTrait.class})
    public void runaway(CommandContext args, CommandSender sender, SentinelTrait sentinel) {
        boolean mode = !sentinel.runaway;
        if (args.argsLength() > 1 && "true".equalsIgnoreCase(args.getString(1))) {
            mode = true;
        }
        if (args.argsLength() > 1 && "false".equalsIgnoreCase(args.getString(1))) {
            mode = false;
        }
        sentinel.runaway = mode;
        if (sentinel.runaway) {
            sender.sendMessage(SentinelCommand.prefixGood + "NPC now runs away!");
        }
        else {
            sender.sendMessage(SentinelCommand.prefixGood + "NPC no longer runs away!");
        }
    }

    @Command(aliases = {"sentinel"}, usage = "protected ['true'/'false']",
            desc = "Toggles whether the NPC should be protected from damage by ignore targets.",
            modifiers = { "protected" }, permission = "sentinel.protected", min = 1, max = 2)
    @Requirements(livingEntity = true, ownership = true, traits = {SentinelTrait.class})
    public void protectedFromIgnores(CommandContext args, CommandSender sender, SentinelTrait sentinel) {
        boolean mode = !sentinel.protectFromIgnores;
        if (args.argsLength() > 1 && "true".equalsIgnoreCase(args.getString(1))) {
            mode = true;
        }
        if (args.argsLength() > 1 && "false".equalsIgnoreCase(args.getString(1))) {
            mode = false;
        }
        sentinel.protectFromIgnores = mode;
        if (sentinel.protectFromIgnores) {
            sender.sendMessage(SentinelCommand.prefixGood + "NPC is now protected from ignore targets!");
        }
        else {
            sender.sendMessage(SentinelCommand.prefixGood + "NPC no longer protected from ignore targets!");
        }
    }

    @Command(aliases = {"sentinel"}, usage = "fightback ['true'/'false']",
            desc = "Toggles whether the NPC will fight back.",
            modifiers = {"fightback"}, permission = "sentinel.fightback", min = 1, max = 2)
    @Requirements(livingEntity = true, ownership = true, traits = {SentinelTrait.class})
    public void fightback(CommandContext args, CommandSender sender, SentinelTrait sentinel) {
        boolean mode = !sentinel.fightback;
        if (args.argsLength() > 1 && "true".equalsIgnoreCase(args.getString(1))) {
            mode = true;
        }
        if (args.argsLength() > 1 && "false".equalsIgnoreCase(args.getString(1))) {
            mode = false;
        }
        sentinel.fightback = mode;
        if (sentinel.fightback) {
            sender.sendMessage(SentinelCommand.prefixGood + "NPC now fights back!");
        }
        else {
            sender.sendMessage(SentinelCommand.prefixGood + "NPC no longer fights back!");
        }
    }

    @Command(aliases = {"sentinel"}, usage = "forgive (id)",
            desc = "Forgives all current targets.",
            modifiers = {"forgive"}, permission = "sentinel.forgive", min = 1, max = 2)
    @Requirements(livingEntity = true, ownership = true, traits = {SentinelTrait.class})
    public void forgive(CommandContext args, CommandSender sender, SentinelTrait sentinel) {
        if (args.argsLength() > 1) {
            UUID id = null;
            String forgivable = args.getString(1);
            if (forgivable.length() == 36 && forgivable.contains("-")) {
                id = UUID.fromString(forgivable);
            }
            else {
                Player player = Bukkit.getPlayer(forgivable);
                if (player != null) {
                    id = player.getUniqueId();
                }
            }
            if (id == null) {
                sender.sendMessage(SentinelCommand.prefixBad + "Invalid player target input.");
                return;
            }
            SentinelCurrentTarget toRemove = new SentinelCurrentTarget();
            toRemove.targetID = id;
            boolean rem1 = sentinel.targetingHelper.currentTargets.remove(toRemove);
            boolean rem2 = sentinel.targetingHelper.currentAvoids.remove(toRemove);
            boolean rem3 = sentinel.chasing != null && SentinelUtilities.uuidEquals(sentinel.chasing.getUniqueId(), id);
            if (rem3) {
                sentinel.chasing = null;
            }
            if (rem1 || rem2 || rem3) {
                sender.sendMessage(SentinelCommand.prefixGood + "Specified target forgiven.");
            }
            else {
                sender.sendMessage(SentinelCommand.prefixGood + "Specified entity is already not a current target.");
            }
        }
        else {
            sentinel.targetingHelper.currentTargets.clear();
            sentinel.targetingHelper.currentAvoids.clear();
            sentinel.chasing = null;
            sender.sendMessage(SentinelCommand.prefixGood + "Targets forgiven.");
        }
    }

    @Command(aliases = {"sentinel"}, usage = "targets",
            desc = "Shows the targets of the current NPC.",
            modifiers = {"targets"}, permission = "sentinel.info", min = 1, max = 1)
    @Requirements(livingEntity = true, ownership = true, traits = {SentinelTrait.class})
    public void targets(CommandContext args, CommandSender sender, SentinelTrait sentinel) {
        sender.sendMessage(SentinelCommand.prefixGood + ChatColor.RESET + sentinel.getNPC().getFullName() + SentinelCommand.colorBasic
                + ": owned by " + ChatColor.RESET + SentinelPlugin.instance.getOwner(sentinel.getNPC()));
        outputEntireTargetsList(sender, sentinel.allTargets, "Targeted");
    }

    @Command(aliases = {"sentinel"}, usage = "ignores",
            desc = "Shows the ignore targets of the current NPC.",
            modifiers = {"ignores"}, permission = "sentinel.info", min = 1, max = 1)
    @Requirements(livingEntity = true, ownership = true, traits = {SentinelTrait.class})
    public void ignores(CommandContext args, CommandSender sender, SentinelTrait sentinel) {
        sender.sendMessage(SentinelCommand.prefixGood + ChatColor.RESET + sentinel.getNPC().getFullName() + SentinelCommand.colorBasic
                + ": owned by " + ChatColor.RESET + SentinelPlugin.instance.getOwner(sentinel.getNPC()));
        outputEntireTargetsList(sender, sentinel.allIgnores, "Ignored");
    }

    @Command(aliases = {"sentinel"}, usage = "avoids",
            desc = "Shows the avoid targets of the current NPC.",
            modifiers = {"avoids"}, permission = "sentinel.info", min = 1, max = 1)
    @Requirements(livingEntity = true, ownership = true, traits = {SentinelTrait.class})
    public void avoids(CommandContext args, CommandSender sender, SentinelTrait sentinel) {
        sender.sendMessage(SentinelCommand.prefixGood + ChatColor.RESET + sentinel.getNPC().getFullName() + SentinelCommand.colorBasic
                + ": owned by " + ChatColor.RESET + SentinelPlugin.instance.getOwner(sentinel.getNPC()));
        outputEntireTargetsList(sender, sentinel.allAvoids, "Avoided");
    }

    @Command(aliases = {"sentinel"}, usage = "avoidreturnpoint",
            desc = "Changes the location the NPC runs to when avoid mode is activated, or removes it if the NPC is already there.",
            modifiers = {"avoidreturnpoint"}, permission = "sentinel.avoidreturnpoint", min = 1, max = 1)
    @Requirements(livingEntity = true, ownership = true, traits = {SentinelTrait.class})
    public void avoidReturnpoint(CommandContext args, CommandSender sender, SentinelTrait sentinel) {
        if (!sentinel.getNPC().isSpawned()) {
            sender.sendMessage(SentinelCommand.prefixBad + "NPC must be spawned for this command!");
            return;
        }
        Location pos = sentinel.getLivingEntity().getLocation().getBlock().getLocation();
        if (sentinel.avoidReturnPoint != null
                && pos.getBlockX() == sentinel.avoidReturnPoint.getBlockX()
                && pos.getBlockY() == sentinel.avoidReturnPoint.getBlockY()
                && pos.getBlockZ() == sentinel.avoidReturnPoint.getBlockZ()
                && pos.getWorld().getName().equals(sentinel.avoidReturnPoint.getWorld().getName())) {
            sentinel.avoidReturnPoint = null;
            sender.sendMessage(SentinelCommand.prefixGood + "Avoid-return point removed!");
        }
        else {
            sentinel.avoidReturnPoint = pos.add(0.5, 0.0, 0.5);
            sender.sendMessage(SentinelCommand.prefixGood + "Avoid-return point updated!");
        }
    }
}
