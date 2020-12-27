package org.mcmonkey.sentinel;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Statistic;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.projectiles.ProjectileSource;
import org.mcmonkey.sentinel.utilities.VelocityTracker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class SentinelEventHandler implements Listener {


    private ArrayList<SentinelTrait> cleanCurrentList() {
        return SentinelPlugin.instance.cleanCurrentList();
    }

    @EventHandler
    public void onEntityExplodes(EntityExplodeEvent event) {
        if (event.isCancelled()) {
            return;
        }
        if (!SentinelPlugin.instance.preventExplosionBlockDamage) {
            return;
        }
        if (event.getEntity() instanceof Projectile) {
             ProjectileSource source = ((Projectile) event.getEntity()).getShooter();
             if (source instanceof Entity) {
                 SentinelTrait sourceSentinel = SentinelUtilities.tryGetSentinel((Entity) source);
                 if (sourceSentinel != null) {
                     event.blockList().clear();
                 }
             }
        }
    }

    @EventHandler
    public void onBlockIgnites(BlockIgniteEvent event) {
        if (event.isCancelled()) {
            return;
        }
        if (!SentinelPlugin.instance.preventExplosionBlockDamage) {
            return;
        }
        if (event.getIgnitingEntity() instanceof Projectile) {
            ProjectileSource source = ((Projectile) event.getIgnitingEntity()).getShooter();
            if (source instanceof Entity) {
                SentinelTrait sourceSentinel = SentinelUtilities.tryGetSentinel((Entity) source);
                if (sourceSentinel != null) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onEntityCombusts(EntityCombustEvent event) {
        if (event instanceof EntityCombustByEntityEvent || event instanceof EntityCombustByBlockEvent) {
            return;
        }
        if (!SentinelPlugin.instance.blockSunburn) {
            return;
        }
        if (SentinelUtilities.tryGetSentinel(event.getEntity()) == null) {
            return;
        }
        event.setCancelled(true);
    }

    /**
     * Called when a projectile hits a block, to auto-remove Sentinel-fired arrows quickly.
     */
    @EventHandler
    public void onProjectileHitsBlock(ProjectileHitEvent event) {
        if (SentinelPlugin.instance.arrowCleanupTime <= 0) {
            return;
        }
        final Projectile projectile = event.getEntity();
        ProjectileSource source = projectile.getShooter();
        if (!(source instanceof Entity)) {
            return;
        }
        SentinelTrait sentinel = SentinelUtilities.tryGetSentinel((Entity) source);
        if (sentinel == null) {
            return;
        }
        Bukkit.getScheduler().scheduleSyncDelayedTask(SentinelPlugin.instance, new Runnable() {
            @Override
            public void run() {
                if (projectile.isValid()) {
                    projectile.remove();
                }
            }
        }, SentinelPlugin.instance.arrowCleanupTime);
    }

    /**
     * Called when players chat, to process event message targets.
     */
    @EventHandler
    public void onAsyncPlayerChat(final AsyncPlayerChatEvent event) {
        if (event.isCancelled()) {
            return;
        }
        Bukkit.getScheduler().scheduleSyncDelayedTask(SentinelPlugin.instance, new Runnable() {
            @Override
            public void run() {
                if (!event.getPlayer().isOnline()) {
                    return;
                }
                for (SentinelTrait sentinel : cleanCurrentList()) {
                    if (sentinel.allTargets.isEventTarget(sentinel, event)) {
                        sentinel.targetingHelper.addTarget(event.getPlayer().getUniqueId());
                    }
                    if (sentinel.allAvoids.isEventTarget(sentinel, event)) {
                        sentinel.targetingHelper.addAvoid(event.getPlayer().getUniqueId());
                    }
                }
            }
        });
    }

    /**
     * Called when combat occurs in the world (and has not yet been processed by other plugins),
     * to handle things like cancelling invalid damage to/from a Sentinel NPC,
     * changing damage values given to or received from an NPC,
     * and if relevant handling config options that require overriding damage events.
     */
    @EventHandler(priority = EventPriority.LOW)
    public void whenAttacksAreHappening(EntityDamageByEntityEvent event) {
        if (event.isCancelled()) {
            return;
        }
        UUID victimUuid = event.getEntity().getUniqueId();
        for (SentinelTrait sentinel : cleanCurrentList()) {
            sentinel.whenSomethingMightDie(victimUuid);
        }
        SentinelTrait victim = SentinelUtilities.tryGetSentinel(event.getEntity());
        SentinelTrait attacker = SentinelUtilities.tryGetSentinel(event.getDamager());
        if (victim != null) {
            if (attacker != null && victim.getNPC().getId() == attacker.getNPC().getId()) {
                event.setCancelled(true);
                return;
            }
            victim.whenAttacksAreHappeningToMe(event);
        }
        if (attacker != null) {
            attacker.whenAttacksAreHappeningFromMe(event);
        }
        if (event.getDamager() instanceof Projectile) {
            ProjectileSource source = ((Projectile) event.getDamager()).getShooter();
            if (source instanceof Entity) {
                SentinelTrait shooter = SentinelUtilities.tryGetSentinel((Entity) source);
                if (shooter != null) {
                    shooter.whenAttacksAreHappeningFromMyArrow(event);
                }
            }
        }
    }

    /**
     * Called when damage has occurred in the world (before being processed by all other plugins), to handle things like
     * preventing overly rapid fire damage.
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void whenEntitiesAreHurt(EntityDamageEvent event) {
        if (event.isCancelled()) {
            return;
        }
        SentinelTrait victim = SentinelUtilities.tryGetSentinel(event.getEntity());
        if (victim != null) {
            victim.whenImHurt(event);
        }
    }

    /**
     * Called when combat has occurred in the world (and has been processed by all other plugins), to handle things like cancelling invalid damage to/from a Sentinel NPC,
     * adding targets (if combat occurs near an NPC), and if relevant handling config options that require overriding damage events.
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void whenAttacksHappened(EntityDamageByEntityEvent event) {
        if (event.isCancelled()) {
            return;
        }
        Entity damager = event.getDamager();
        if (event.getDamager() instanceof Projectile) {
            ProjectileSource source = ((Projectile) event.getDamager()).getShooter();
            if (source instanceof Entity) {
                damager = (Entity) source;
            }
        }
        SentinelTrait victim = SentinelUtilities.tryGetSentinel(event.getEntity());
        SentinelTrait attacker = SentinelUtilities.tryGetSentinel(damager);
        if (attacker != null) {
            if (!(event.getEntity() instanceof LivingEntity)) {
                event.setCancelled(true);
                return;
            }
            if (victim != null && victim.getNPC().getId() == attacker.getNPC().getId()) {
                event.setCancelled(true);
                return;
            }
            attacker.whenAttacksHappened(event);
        }
        if (victim != null) {
            victim.whenAttacksHappened(event);
        }
        for (SentinelTrait sentinel : cleanCurrentList()) {
            UUID guarding = sentinel.getGuarding();
            if (guarding != null && event.getEntity().getUniqueId().equals(guarding)) {
                sentinel.whenAttacksHappened(event);
            }
        }
        if (damager instanceof LivingEntity) {
            LivingEntity damagerLiving = (LivingEntity) damager;
            for (SentinelTrait sentinel : cleanCurrentList()) {
                if (sentinel.allTargets.isEventTarget(event)
                        && sentinel.targetingHelper.canSee(damagerLiving) && !sentinel.targetingHelper.isIgnored(damagerLiving)) {
                    sentinel.targetingHelper.addTarget(damager.getUniqueId());
                }
                if (sentinel.allAvoids.isEventTarget(event)
                        && sentinel.targetingHelper.canSee(damagerLiving) && !sentinel.targetingHelper.isIgnored(damagerLiving)) {
                    sentinel.targetingHelper.addAvoid(damager.getUniqueId());
                }
            }
        }
        if (event.getEntity() instanceof LivingEntity) {
            LivingEntity entity = (LivingEntity) event.getEntity();
            for (SentinelTrait sentinel : cleanCurrentList()) {
                if (sentinel.allTargets.isReverseEventTarget(sentinel, event)
                        && sentinel.targetingHelper.canSee(entity) && !sentinel.targetingHelper.isIgnored(entity)) {
                    sentinel.targetingHelper.addTarget(entity.getUniqueId());
                }
                if (sentinel.allAvoids.isReverseEventTarget(sentinel, event)
                        && sentinel.targetingHelper.canSee(entity) && !sentinel.targetingHelper.isIgnored(entity)) {
                    sentinel.targetingHelper.addAvoid(entity.getUniqueId());
                }
            }
        }
    }

    /**
     * Called when a player dies, to process handling of the message if a Sentinel caused it.
     */
    @EventHandler
    public void whenAPlayerDies(PlayerDeathEvent event) {
        if (event.getEntity().getLastDamageCause() instanceof EntityDamageByEntityEvent) {
            Entity damager = ((EntityDamageByEntityEvent) event.getEntity().getLastDamageCause()).getDamager();
            if (damager instanceof Projectile && ((Projectile) damager).getShooter() instanceof Entity) {
                damager = (Entity) ((Projectile) damager).getShooter();
            }
            if (damager instanceof Player && CitizensAPI.getNPCRegistry().isNPC(damager)) {
                SentinelTrait sentinel = SentinelUtilities.tryGetSentinel(damager);
                if (sentinel != null && sentinel.getNPC().requiresNameHologram() && event.getDeathMessage() != null) {
                    event.setDeathMessage(event.getDeathMessage().replace(sentinel.getNPC().getEntity().getName(), sentinel.getNPC().getFullName()));
                }
            }
        }
    }

    /**
     * Called when a player travels between worlds, mostly to clean some caches that might get confused about it.
     */
    @EventHandler
    public void whenAPlayerChangesWorld(PlayerChangedWorldEvent event) {
        VelocityTracker.playerVelocityEstimates.remove(event.getPlayer().getUniqueId());
    }

    /**
     * Called when any entity dies, to process drops handling and targeting updates.
     */
    @EventHandler
    public void whenAnEnemyDies(EntityDeathEvent event) {
        UUID dead = event.getEntity().getUniqueId();
        if (event.getEntity() instanceof Player) {
            VelocityTracker.playerVelocityEstimates.remove(dead);
        }
        for (SentinelTrait sentinel : cleanCurrentList()) {
            sentinel.whenAnEnemyDies(dead);
            sentinel.whenSomethingDies(event);
        }
    }

    /**
     * Called when a Sentinel NPC dies.
     */
    @EventHandler
    public void whenWeDie(EntityDeathEvent event) {
        SentinelTrait sentinel = SentinelUtilities.tryGetSentinel(event.getEntity());
        if (sentinel != null) {
            if (event.getEntity().getLastDamageCause() instanceof EntityDamageByEntityEvent) {
                Entity damager = ((EntityDamageByEntityEvent) event.getEntity().getLastDamageCause()).getDamager();
                if (damager instanceof Projectile && ((Projectile) damager).getShooter() instanceof Entity) {
                    damager = (Entity) ((Projectile) damager).getShooter();
                }
                if (damager instanceof Player && !CitizensAPI.getNPCRegistry().isNPC(damager)) {
                    TrackedKillToBlock blocker = killStatsToBlock.get(damager.getUniqueId());
                    if (blocker == null) {
                        blocker = new TrackedKillToBlock();
                        killStatsToBlock.put(damager.getUniqueId(), blocker);
                    }
                    blocker.addOne();
                }
            }
            sentinel.whenWeDie(event);
        }
    }

    /**
     * Class to help prevent kill statistic incrementing.
     */
    public static class TrackedKillToBlock {

        /**
         * The number of kills to block.
         */
        public int killCount = 0;

        /**
         * The server tick time of the block.
         */
        public long systemTick = 0;

        /**
         * Adds one kill and handles system time.
         */
        public void addOne() {
            if (systemTick != SentinelPlugin.instance.tickTimeTotal) {
                killCount = 0;
                systemTick = SentinelPlugin.instance.tickTimeTotal;
            }
            killCount++;
        }
    }

    /**
     * Map to track automatic kill-statistic incrementing.
     */
    public static HashMap<UUID, TrackedKillToBlock> killStatsToBlock = new HashMap<>();

    /**
     * Called when a player statistic increments, to prevent the "PLAYER_KILLS" stat updating for killing an NPC.
     */
    @EventHandler
    public void onStatisticIncrement(PlayerStatisticIncrementEvent event) {
        if (event.getStatistic() == Statistic.PLAYER_KILLS) {
            UUID uuid = event.getPlayer().getUniqueId();
            TrackedKillToBlock blocker = killStatsToBlock.get(uuid);
            if (blocker != null) {
                blocker.killCount--;
                if (blocker.systemTick != SentinelPlugin.instance.tickTimeTotal) {
                    killStatsToBlock.remove(uuid);
                    return;
                }
                if (blocker.killCount <= 0) {
                    killStatsToBlock.remove(uuid);
                }
                event.setCancelled(true);
            }
        }
    }

    /**
     * Called when a player teleports, to handle NPC guard updates.
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerTeleports(final PlayerTeleportEvent event) {
        if (event.isCancelled()) {
            return;
        }
        UUID uuid = event.getPlayer().getUniqueId();
        for (SentinelTrait sentinel : cleanCurrentList()) {
            if (sentinel.getGuarding() != null && sentinel.getGuarding().equals(uuid)) {
                sentinel.onPlayerTeleports(event);
            }
        }
    }

    /**
     * Called every time a player moves at all, for use with monitoring if players move into range of an NPC.
     */
    @EventHandler
    public void onPlayerMovesInRange(PlayerMoveEvent event) {
        if (event.isCancelled()) {
            return;
        }
        if (event.getTo().toVector().toBlockVector().equals(event.getFrom().toVector().toBlockVector())) {
            return;
        }
        for (SentinelTrait sentinel : cleanCurrentList()) {
            sentinel.onPlayerMovesInRange(event);
        }
    }

    /**
     * Prefix string for an inventory title.
     */
    public final static String InvPrefix = ChatColor.GREEN + "Sentinel ";

    /**
     * Called when an inventory is closed.
     */
    @EventHandler
    public void onInvClose(InventoryCloseEvent event) {
        String invTitle = SentinelUtilities.getInventoryTitle(event);
        if (invTitle.startsWith(InvPrefix)) {
            int id = Integer.parseInt(invTitle.substring(InvPrefix.length()));
            NPC npc = CitizensAPI.getNPCRegistry().getById(id);
            if (npc != null && npc.hasTrait(SentinelTrait.class)) {
                ArrayList<ItemStack> its = npc.getOrAddTrait(SentinelTrait.class).drops;
                its.clear();
                for (ItemStack it : event.getInventory().getContents()) {
                    if (it != null && it.getType() != Material.AIR) {
                        its.add(it);
                    }
                }
            }
        }
    }
}
