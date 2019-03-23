package org.mcmonkey.sentinel;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.projectiles.ProjectileSource;

import java.util.UUID;

public class SentinelEventHandler implements Listener {

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
                for (SentinelTrait sentinel : SentinelPlugin.instance.currentSentinelNPCs) {
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
     * Tries to get a Sentinel from an entity. Returns null if it is not a Sentinel.
     */
    public SentinelTrait tryGetSentinel(Entity entity) {
        if (CitizensAPI.getNPCRegistry().isNPC(entity)) {
            NPC npc = CitizensAPI.getNPCRegistry().getNPC(entity);
            if (npc.hasTrait(SentinelTrait.class)) {
                return npc.getTrait(SentinelTrait.class);
            }
        }
        return null;
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
        for (SentinelTrait sentinel : SentinelPlugin.instance.currentSentinelNPCs) {
            sentinel.whenSomethingMightDie(victimUuid);
        }
        SentinelTrait victim = tryGetSentinel(event.getEntity());
        if (victim != null) {
            victim.whenAttacksAreHappeningToMe(event);
        }
        SentinelTrait attacker = tryGetSentinel(event.getDamager());
        if (attacker != null) {
            attacker.whenAttacksAreHappeningFromMe(event);
        }
        if (event.getDamager() instanceof Projectile) {
            ProjectileSource source = ((Projectile) event.getDamager()).getShooter();
            if (source instanceof Entity) {
                SentinelTrait shooter = tryGetSentinel((Entity) source);
                if (shooter != null) {
                    shooter.whenAttacksAreHappeningFromMyArrow(event);
                }
            }
        }
    }

    /**
     * Called when combat has occurred in the world (and has been processed by all other plugins), to handle things like cancelling invalid damage to/from a Sentinel NPC,
     * adding targets (if combat occurs near an NPC), and if relevant handling config options that require overriding damage events.
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void whenAttacksHappened(EntityDamageByEntityEvent event) {
        if (event.isCancelled()) {
            return;
        }
        SentinelTrait victim = tryGetSentinel(event.getEntity());
        if (victim != null) {
            victim.whenAttacksHappened(event);
        }
        SentinelTrait attacker = tryGetSentinel(event.getDamager());
        if (attacker != null) {
            attacker.whenAttacksHappened(event);
        }
        if (event.getDamager() instanceof Projectile) {
            ProjectileSource source = ((Projectile) event.getDamager()).getShooter();
            if (source instanceof Entity) {
                SentinelTrait shooter = tryGetSentinel((Entity) source);
                if (shooter != null) {
                    shooter.whenAttacksHappened(event);
                }
            }
        }
        for (SentinelTrait sentinel : SentinelPlugin.instance.currentSentinelNPCs) {
            UUID guarding = sentinel.getGuarding();
            if (guarding != null) {
                if (event.getEntity().getUniqueId().equals(guarding)) {
                    sentinel.whenAttacksHappened(event);
                }
            }
        }
    }

    /**
     * Called when any entity dies, to process drops handling and targeting updates.
     */
    @EventHandler
    public void whenAnEnemyDies(EntityDeathEvent event) {
        UUID dead = event.getEntity().getUniqueId();
        for (SentinelTrait sentinel : SentinelPlugin.instance.currentSentinelNPCs) {
            sentinel.whenAnEnemyDies(dead);
            sentinel.whenSomethingDies(event);
            if (sentinel.getLivingEntity().getUniqueId().equals(dead)) {
                sentinel.whenWeDie(event);
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
        for (SentinelTrait sentinel : SentinelPlugin.instance.currentSentinelNPCs) {
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
        if (event.getTo().toVector().equals(event.getFrom().toVector())) {
            return;
        }
        for (SentinelTrait sentinel : SentinelPlugin.instance.currentSentinelNPCs) {
            sentinel.onPlayerMovesInRange(event);
        }
    }
}
