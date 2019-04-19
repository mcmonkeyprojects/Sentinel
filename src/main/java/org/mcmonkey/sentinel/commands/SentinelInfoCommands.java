package org.mcmonkey.sentinel.commands;

import net.citizensnpcs.api.command.Command;
import net.citizensnpcs.api.command.CommandContext;
import net.citizensnpcs.api.command.Requirements;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.mcmonkey.sentinel.SentinelPlugin;
import org.mcmonkey.sentinel.SentinelTrait;

public class SentinelInfoCommands {

    @Command(aliases = {"sentinel"}, usage = "info",
            desc = "Shows info on the current NPC.",
            modifiers = {"info"}, permission = "sentinel.info", min = 1, max = 1)
    @Requirements(livingEntity = true, ownership = true, traits = {SentinelTrait.class})
    public void info(CommandContext args, CommandSender sender, SentinelTrait sentinel) {
        sender.sendMessage(SentinelCommand.prefixGood + ChatColor.RESET + sentinel.getNPC().getFullName() + SentinelCommand.colorBasic
                + ": owned by " + ChatColor.RESET + SentinelPlugin.instance.getOwner(sentinel.getNPC()) +
                (sentinel.getGuarding() == null ? "" : SentinelCommand.colorBasic
                        + ", guarding: " + ChatColor.RESET + Bukkit.getOfflinePlayer(sentinel.getGuarding()).getName()));
        sender.sendMessage(SentinelCommand.prefixGood + "Damage: " + ChatColor.AQUA + sentinel.damage
                + SentinelCommand.colorBasic + " Calculated: " + ChatColor.AQUA + sentinel.getDamage());
        sender.sendMessage(SentinelCommand.prefixGood + "Armor: " + ChatColor.AQUA + sentinel.armor
                + (sentinel.getNPC().isSpawned() ? SentinelCommand.colorBasic + " Calculated: "
                + ChatColor.AQUA + sentinel.getArmor(sentinel.getLivingEntity()) : ""));
        sender.sendMessage(SentinelCommand.prefixGood + "Health: " + ChatColor.AQUA +
                (sentinel.getNPC().isSpawned() ? sentinel.getLivingEntity().getHealth() + "/" : "") + sentinel.health);
        sender.sendMessage(SentinelCommand.prefixGood + "Range: " + ChatColor.AQUA + sentinel.range);
        sender.sendMessage(SentinelCommand.prefixGood + "Avoidance Range: " + ChatColor.AQUA + sentinel.avoidRange);
        sender.sendMessage(SentinelCommand.prefixGood + "Attack Rate: " + ChatColor.AQUA + (sentinel.attackRate / 20.0));
        sender.sendMessage(SentinelCommand.prefixGood + "Ranged Attack Rate: " + ChatColor.AQUA + (sentinel.attackRateRanged / 20.0));
        sender.sendMessage(SentinelCommand.prefixGood + "Heal Rate: " + ChatColor.AQUA + (sentinel.healRate / 20.0));
        sender.sendMessage(SentinelCommand.prefixGood + "Respawn Time: " + ChatColor.AQUA + (sentinel.respawnTime / 20.0));
        sender.sendMessage(SentinelCommand.prefixGood + "Accuracy: " + ChatColor.AQUA + sentinel.accuracy);
        sender.sendMessage(SentinelCommand.prefixGood + "Reach: " + ChatColor.AQUA + sentinel.reach);
        sender.sendMessage(SentinelCommand.prefixGood + "Invincibility Enabled: " + ChatColor.AQUA + sentinel.invincible);
        sender.sendMessage(SentinelCommand.prefixGood + "Fightback Enabled: " + ChatColor.AQUA + sentinel.fightback);
        sender.sendMessage(SentinelCommand.prefixGood + "Ranged Chasing Enabled: " + ChatColor.AQUA + sentinel.rangedChase);
        sender.sendMessage(SentinelCommand.prefixGood + "Close-Quarters Chasing Enabled: " + ChatColor.AQUA + sentinel.closeChase);
        sender.sendMessage(SentinelCommand.prefixGood + "Maximum chase range: " + ChatColor.AQUA + sentinel.chaseRange);
        sender.sendMessage(SentinelCommand.prefixGood + "Safe-Shot Enabled: " + ChatColor.AQUA + sentinel.safeShot);
        sender.sendMessage(SentinelCommand.prefixGood + "Enemy-Drops Enabled: " + ChatColor.AQUA + sentinel.enemyDrops);
        sender.sendMessage(SentinelCommand.prefixGood + "Autoswitch Enabled: " + ChatColor.AQUA + sentinel.autoswitch);
        sender.sendMessage(SentinelCommand.prefixGood + "Realistic Targeting Enabled: " + ChatColor.AQUA + sentinel.realistic);
        sender.sendMessage(SentinelCommand.prefixGood + "Run-Away Enabled: " + ChatColor.AQUA + sentinel.runaway);
        sender.sendMessage(SentinelCommand.prefixGood + "Squad: " + ChatColor.AQUA + (sentinel.squad == null ? "None" : sentinel.squad));
    }

    @Command(aliases = {"sentinel"}, usage = "stats",
            desc = "Shows statistics about the current NPC.",
            modifiers = {"stats"}, permission = "sentinel.info", min = 1, max = 1)
    @Requirements(livingEntity = true, ownership = true, traits = {SentinelTrait.class})
    public void stats(CommandContext args, CommandSender sender, SentinelTrait sentinel) {
        sender.sendMessage(SentinelCommand.prefixGood + ChatColor.RESET + sentinel.getNPC().getFullName() + SentinelCommand.colorBasic
                + ": owned by " + ChatColor.RESET + SentinelPlugin.instance.getOwner(sentinel.getNPC()));
        sender.sendMessage(SentinelCommand.prefixGood + "Arrows fired: " + ChatColor.AQUA + sentinel.stats_arrowsFired);
        sender.sendMessage(SentinelCommand.prefixGood + "Potions thrown: " + ChatColor.AQUA + sentinel.stats_potionsThrown);
        sender.sendMessage(SentinelCommand.prefixGood + "Fireballs launched: " + ChatColor.AQUA + sentinel.stats_fireballsFired);
        sender.sendMessage(SentinelCommand.prefixGood + "Snowballs thrown: " + ChatColor.AQUA + sentinel.stats_snowballsThrown);
        sender.sendMessage(SentinelCommand.prefixGood + "Eggs thrown: " + ChatColor.AQUA + sentinel.stats_eggsThrown);
        sender.sendMessage(SentinelCommand.prefixGood + "Pearls used: " + ChatColor.AQUA + sentinel.stats_pearlsUsed);
        sender.sendMessage(SentinelCommand.prefixGood + "Skulls thrown: " + ChatColor.AQUA + sentinel.stats_skullsThrown);
        sender.sendMessage(SentinelCommand.prefixGood + "Punches: " + ChatColor.AQUA + sentinel.stats_punches);
        sender.sendMessage(SentinelCommand.prefixGood + "Times spawned: " + ChatColor.AQUA + sentinel.stats_timesSpawned);
        sender.sendMessage(SentinelCommand.prefixGood + "Damage Given: " + ChatColor.AQUA + sentinel.stats_damageGiven);
        sender.sendMessage(SentinelCommand.prefixGood + "Damage Taken: " + ChatColor.AQUA + sentinel.stats_damageTaken);
        sender.sendMessage(SentinelCommand.prefixGood + "Minutes spawned: " + ChatColor.AQUA + sentinel.stats_ticksSpawned / (20.0 * 60.0));
    }

    @Command(aliases = {"sentinel"}, usage = "debug",
            desc = "Toggles debugging.",
            modifiers = {"debug"}, permission = "sentinel.debug", min = 1, max = 1)
    public void debug(CommandContext args, CommandSender sender) {
        SentinelPlugin.debugMe = !SentinelPlugin.debugMe;
        sender.sendMessage(SentinelCommand.prefixGood + "Toggled: " + SentinelPlugin.debugMe + "!");
    }

    @Command(aliases = {"sentinel"}, usage = "reload",
            desc = "Reloads the configuration file.",
            modifiers = {"reload"}, permission = "sentinel.reload", min = 1, max = 1)
    public void reload(CommandContext args, CommandSender sender) {
        SentinelPlugin.instance.loadConfigSettings();
        sender.sendMessage(SentinelCommand.prefixGood + "Reload the config file.");
    }
}
