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

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

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
        addLineIfNeeded(paginator, "Owned by", SentinelPlugin.instance.getOwner(sentinel.getNPC(), ""));
        addLineIfNeeded(paginator, "Guarding", (guardName == null ? "" : guardName));
        addLineIfNeeded(paginator, "Damage", sentinel.damage + SentinelCommand.colorBasic + " Calculated: " + SentinelCommand.colorEmphasis + sentinel.getDamage(true));
        addLineIfNeeded(paginator, "Armor", sentinel.armor + (sentinel.getNPC().isSpawned() ? SentinelCommand.colorBasic + " Calculated: " + SentinelCommand.colorEmphasis + sentinel.getArmor(sentinel.getLivingEntity()) : ""));
        addLineIfNeeded(paginator, "Health", (sentinel.getNPC().isSpawned() ? sentinel.getLivingEntity().getHealth() + "/" : "") + sentinel.health);
        addLineIfNeeded(paginator, "Range", sentinel.range);
        addLineIfNeeded(paginator, "Avoidance Range", sentinel.avoidRange);
        addLineIfNeeded(paginator, "Attack Rate", (sentinel.attackRate / 20.0));
        addLineIfNeeded(paginator, "Ranged Attack Rate", (sentinel.attackRateRanged / 20.0));
        addLineIfNeeded(paginator, "Heal Rate", (sentinel.healRate / 20.0));
        addLineIfNeeded(paginator, "Respawn Time", (sentinel.respawnTime / 20.0));
        addLineIfNeeded(paginator, "Accuracy", sentinel.accuracy);
        addLineIfNeeded(paginator, "Reach", sentinel.reach);
        addLineIfNeeded(paginator, "Projectile Range", sentinel.projectileRange);
        addLineIfNeeded(paginator, "Greeting", (sentinel.greetingText == null ? "" : sentinel.greetingText));
        addLineIfNeeded(paginator, "Warning", (sentinel.warningText == null ? "" : sentinel.warningText));
        addLineIfNeeded(paginator, "Greeting Range", sentinel.greetRange);
        addLineIfNeeded(paginator, "Greeting Rate", sentinel.greetRate);
        addLineIfNeeded(paginator, "Guard Distance Minimum", sentinel.guardDistanceMinimum);
        addLineIfNeeded(paginator, "Guard Selection Range", sentinel.guardSelectionRange);
        addLineIfNeeded(paginator, "Invincibility Enabled", sentinel.invincible);
        addLineIfNeeded(paginator, "Protect from Ignores Enabled", sentinel.protectFromIgnores);
        addLineIfNeeded(paginator, "Fightback Enabled", sentinel.fightback);
        addLineIfNeeded(paginator, "Ranged Chasing Enabled", sentinel.rangedChase);
        addLineIfNeeded(paginator, "Close-Quarters Chasing Enabled", sentinel.closeChase);
        addLineIfNeeded(paginator, "Maximum chase range", sentinel.chaseRange);
        addLineIfNeeded(paginator, "Safe-Shot Enabled", sentinel.safeShot);
        addLineIfNeeded(paginator, "Enemy-Drops Enabled", sentinel.enemyDrops);
        addLineIfNeeded(paginator, "Autoswitch Enabled", sentinel.autoswitch);
        addLineIfNeeded(paginator, "Realistic Targeting Enabled", sentinel.realistic);
        addLineIfNeeded(paginator, "Knockback allowed", sentinel.allowKnockback);
        addLineIfNeeded(paginator, "Run-Away Enabled", sentinel.runaway);
        addLineIfNeeded(paginator, "Squad", (sentinel.squad == null ? "" : sentinel.squad));
        addLineIfNeeded(paginator, "Spawnpoint", (sentinel.spawnPoint == null ? "" : sentinel.spawnPoint.toVector().toBlockVector().toString()));
        addLineIfNeeded(paginator, "Per-weapon damage values", sentinel.weaponDamage.toString());
        addLineIfNeeded(paginator, "Weapon redirections", sentinel.weaponRedirects.toString());
        addLineIfNeeded(paginator, "Drops", sentinel.drops == null ? "" : sentinel.drops.size());
        addLineIfNeeded(paginator, "Death XP", sentinel.deathXP);
        if (SentinelPlugin.instance.hasWorldGuard) {
            addLineIfNeeded(paginator, "WorldGuard region limit", sentinel.worldguardRegion);
        }
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

    private static void addLineIfNeeded(Paginator paginator, String name, Object value) {
        if (value instanceof String && (((String) value).isEmpty() || value.equals("{}"))) {
            return;
        }
        paginator.addLine(SentinelCommand.prefixGood + name + ": " + SentinelCommand.colorEmphasis + value);
    }

    private static DecimalFormat twoDigitFormat = new DecimalFormat("0.00", new DecimalFormatSymbols(Locale.US));

    @Command(aliases = {"sentinel"}, usage = "stats",
            desc = "Shows statistics about the current NPC.",
            modifiers = {"stats"}, permission = "sentinel.info", min = 1, max = 1)
    @Requirements(livingEntity = true, ownership = true, traits = {SentinelTrait.class})
    public void stats(CommandContext args, CommandSender sender, SentinelTrait sentinel) {
        sender.sendMessage(SentinelCommand.prefixGood + ChatColor.RESET + sentinel.getNPC().getFullName() + SentinelCommand.colorBasic
                + ": owned by " + ChatColor.RESET + SentinelPlugin.instance.getOwner(sentinel.getNPC()));
        sender.sendMessage(SentinelCommand.prefixGood + "Arrows fired: " + SentinelCommand.colorEmphasis + sentinel.stats_arrowsFired);
        sender.sendMessage(SentinelCommand.prefixGood + "Potions thrown: " + SentinelCommand.colorEmphasis + sentinel.stats_potionsThrown);
        sender.sendMessage(SentinelCommand.prefixGood + "Fireballs launched: " + SentinelCommand.colorEmphasis + sentinel.stats_fireballsFired);
        sender.sendMessage(SentinelCommand.prefixGood + "Snowballs thrown: " + SentinelCommand.colorEmphasis + sentinel.stats_snowballsThrown);
        sender.sendMessage(SentinelCommand.prefixGood + "Eggs thrown: " + SentinelCommand.colorEmphasis + sentinel.stats_eggsThrown);
        sender.sendMessage(SentinelCommand.prefixGood + "Pearls used: " + SentinelCommand.colorEmphasis + sentinel.stats_pearlsUsed);
        sender.sendMessage(SentinelCommand.prefixGood + "Skulls thrown: " + SentinelCommand.colorEmphasis + sentinel.stats_skullsThrown);
        sender.sendMessage(SentinelCommand.prefixGood + "Llama spits spat: " + SentinelCommand.colorEmphasis + sentinel.stats_llamaSpitShot);
        sender.sendMessage(SentinelCommand.prefixGood + "Shulker bullets shot: " + SentinelCommand.colorEmphasis + sentinel.stats_shulkerBulletsShot);
        sender.sendMessage(SentinelCommand.prefixGood + "Evoker fangs spawned: " + SentinelCommand.colorEmphasis + sentinel.stats_evokerFangsSpawned);
        sender.sendMessage(SentinelCommand.prefixGood + "Punches: " + SentinelCommand.colorEmphasis + sentinel.stats_punches);
        sender.sendMessage(SentinelCommand.prefixGood + "Times spawned: " + SentinelCommand.colorEmphasis + sentinel.stats_timesSpawned);
        sender.sendMessage(SentinelCommand.prefixGood + "Damage Given: " + SentinelCommand.colorEmphasis + sentinel.stats_damageGiven);
        sender.sendMessage(SentinelCommand.prefixGood + "Damage Taken: " + SentinelCommand.colorEmphasis + sentinel.stats_damageTaken);
        double minutesSpawned = sentinel.stats_ticksSpawned / (20.0 * 60.0);
        sender.sendMessage(SentinelCommand.prefixGood + "Minutes spawned: " + SentinelCommand.colorEmphasis + twoDigitFormat.format(minutesSpawned));
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
        sender.sendMessage(SentinelCommand.prefixGood + "Reloaded the config file.");
    }
}
