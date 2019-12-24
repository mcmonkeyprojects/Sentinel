package org.mcmonkey.sentinel.commands;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.command.Command;
import net.citizensnpcs.api.command.CommandContext;
import net.citizensnpcs.api.command.Requirements;
import net.citizensnpcs.api.util.Paginator;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.LivingEntity;
import org.mcmonkey.sentinel.SentinelPlugin;
import org.mcmonkey.sentinel.SentinelTrait;

public class SentinelInfoCommands {

    @Command(aliases = {"sentinel"}, usage = "info",
            desc = "Shows info on the current NPC.",
            modifiers = {"info"}, permission = "sentinel.info", min = 1, max = 2)
    @Requirements(livingEntity = true, ownership = true, traits = {SentinelTrait.class})
    public void info(CommandContext args, CommandSender sender, SentinelTrait sentinel) {
        String guardName = null;
        LivingEntity guarded = sentinel.getGuardingEntity();
        if (guarded != null) {
            guardName = guarded.getName();
        }
        else if (sentinel.guardedNPC >= 0 && CitizensAPI.getNPCRegistry().getById(sentinel.guardedNPC) != null) {
            guardName = "NPC " + sentinel.guardedNPC + ": " + CitizensAPI.getNPCRegistry().getById(sentinel.guardedNPC).getFullName();
        }
        else if (sentinel.getGuarding() != null) {
            OfflinePlayer player = Bukkit.getOfflinePlayer(sentinel.getGuarding());
            if (player != null && player.getName() != null) {
                guardName = player.getName();
            }
        }
        Paginator paginator = new Paginator().header(SentinelCommand.prefixGood + sentinel.getNPC().getFullName());
        paginator.addLine(SentinelCommand.prefixGood + "Owned by: " + ChatColor.AQUA + SentinelPlugin.instance.getOwner(sentinel.getNPC()));
        paginator.addLine(SentinelCommand.prefixGood + "Guarding: " + ChatColor.AQUA + (guardName == null ? "Nobody" : guardName));
        paginator.addLine(SentinelCommand.prefixGood + "Damage: " + ChatColor.AQUA + sentinel.damage
                + SentinelCommand.colorBasic + " Calculated: " + ChatColor.AQUA + sentinel.getDamage());
        paginator.addLine(SentinelCommand.prefixGood + "Armor: " + ChatColor.AQUA + sentinel.armor
                + (sentinel.getNPC().isSpawned() ? SentinelCommand.colorBasic + " Calculated: "
                + ChatColor.AQUA + sentinel.getArmor(sentinel.getLivingEntity()) : ""));
        paginator.addLine(SentinelCommand.prefixGood + "Health: " + ChatColor.AQUA +
                (sentinel.getNPC().isSpawned() ? sentinel.getLivingEntity().getHealth() + "/" : "") + sentinel.health);
        paginator.addLine(SentinelCommand.prefixGood + "Range: " + ChatColor.AQUA + sentinel.range);
        paginator.addLine(SentinelCommand.prefixGood + "Avoidance Range: " + ChatColor.AQUA + sentinel.avoidRange);
        paginator.addLine(SentinelCommand.prefixGood + "Attack Rate: " + ChatColor.AQUA + (sentinel.attackRate / 20.0));
        paginator.addLine(SentinelCommand.prefixGood + "Ranged Attack Rate: " + ChatColor.AQUA + (sentinel.attackRateRanged / 20.0));
        paginator.addLine(SentinelCommand.prefixGood + "Heal Rate: " + ChatColor.AQUA + (sentinel.healRate / 20.0));
        paginator.addLine(SentinelCommand.prefixGood + "Respawn Time: " + ChatColor.AQUA + (sentinel.respawnTime / 20.0));
        paginator.addLine(SentinelCommand.prefixGood + "Accuracy: " + ChatColor.AQUA + sentinel.accuracy);
        paginator.addLine(SentinelCommand.prefixGood + "Reach: " + ChatColor.AQUA + sentinel.reach);
        paginator.addLine(SentinelCommand.prefixGood + "Projectile Range: " + ChatColor.AQUA + sentinel.projectileRange);
        paginator.addLine(SentinelCommand.prefixGood + "Greeting: " + ChatColor.AQUA + (sentinel.greetingText == null ? "None" : sentinel.greetingText));
        paginator.addLine(SentinelCommand.prefixGood + "Warning: " + ChatColor.AQUA + (sentinel.warningText == null ? "None" : sentinel.warningText));
        paginator.addLine(SentinelCommand.prefixGood + "Greeting Range: " + ChatColor.AQUA + sentinel.greetRange);
        paginator.addLine(SentinelCommand.prefixGood + "Greeting Rate: " + ChatColor.AQUA + sentinel.greetRate);
        paginator.addLine(SentinelCommand.prefixGood + "Guard Distance Minimum: " + ChatColor.AQUA + sentinel.guardDistanceMinimum);
        paginator.addLine(SentinelCommand.prefixGood + "Guard Selection Range: " + ChatColor.AQUA + sentinel.guardSelectionRange);
        paginator.addLine(SentinelCommand.prefixGood + "Invincibility Enabled: " + ChatColor.AQUA + sentinel.invincible);
        paginator.addLine(SentinelCommand.prefixGood + "Fightback Enabled: " + ChatColor.AQUA + sentinel.fightback);
        paginator.addLine(SentinelCommand.prefixGood + "Ranged Chasing Enabled: " + ChatColor.AQUA + sentinel.rangedChase);
        paginator.addLine(SentinelCommand.prefixGood + "Close-Quarters Chasing Enabled: " + ChatColor.AQUA + sentinel.closeChase);
        paginator.addLine(SentinelCommand.prefixGood + "Maximum chase range: " + ChatColor.AQUA + sentinel.chaseRange);
        paginator.addLine(SentinelCommand.prefixGood + "Safe-Shot Enabled: " + ChatColor.AQUA + sentinel.safeShot);
        paginator.addLine(SentinelCommand.prefixGood + "Enemy-Drops Enabled: " + ChatColor.AQUA + sentinel.enemyDrops);
        paginator.addLine(SentinelCommand.prefixGood + "Autoswitch Enabled: " + ChatColor.AQUA + sentinel.autoswitch);
        paginator.addLine(SentinelCommand.prefixGood + "Realistic Targeting Enabled: " + ChatColor.AQUA + sentinel.realistic);
        paginator.addLine(SentinelCommand.prefixGood + "Run-Away Enabled: " + ChatColor.AQUA + sentinel.runaway);
        paginator.addLine(SentinelCommand.prefixGood + "Squad: " + ChatColor.AQUA + (sentinel.squad == null ? "None" : sentinel.squad));
        paginator.addLine(SentinelCommand.prefixGood + "Per-weapon damage values: " + ChatColor.AQUA + sentinel.weaponDamage.toString());
        paginator.addLine(SentinelCommand.prefixGood + "Weapon redirections: " + ChatColor.AQUA + sentinel.weaponRedirects.toString());
        int page = 1;
        if (args.argsLength() == 2) {
            try {
                page = args.getInteger(1);
            }
            catch (NumberFormatException ex) {
                sender.sendMessage(SentinelCommand.prefixBad + "First argument must be a valid page number.");
            }
        }
        paginator.sendPage(sender, page);
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
        double minutesSpawned = sentinel.stats_ticksSpawned / (20.0 * 60.0);
        sender.sendMessage(SentinelCommand.prefixGood + "Minutes spawned: " + ChatColor.AQUA + (((int) (minutesSpawned * 100)) * 0.01));
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
