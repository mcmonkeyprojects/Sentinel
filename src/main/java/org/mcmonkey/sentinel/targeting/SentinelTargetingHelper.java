package org.mcmonkey.sentinel.targeting;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.trait.Owner;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.mcmonkey.sentinel.*;

import java.util.HashSet;
import java.util.UUID;

/**
 * Helper for targeting logic on an NPC.
 */
public class SentinelTargetingHelper extends SentinelHelperObject {

    /**
     * Returns whether the NPC can see the target entity.
     */
    public boolean canSee(LivingEntity entity) {
        if (!getLivingEntity().hasLineOfSight(entity)) {
            return false;
        }
        if (sentinel.realistic) {
            float yaw = getLivingEntity().getEyeLocation().getYaw();
            while (yaw < 0) {
                yaw += 360;
            }
            while (yaw >= 360) {
                yaw -= 360;
            }
            Vector rel = entity.getLocation().toVector().subtract(getLivingEntity().getLocation().toVector()).normalize();
            float yawHelp = SentinelUtilities.getYaw(rel);
            if (!(Math.abs(yawHelp - yaw) < 90 ||
                    Math.abs(yawHelp + 360 - yaw) < 90 ||
                    Math.abs(yaw + 360 - yawHelp) < 90)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns whether the NPC is using a potion item.
     */
    public boolean shouldTarget(LivingEntity entity) {
        if (entity.getUniqueId().equals(getLivingEntity().getUniqueId())) {
            return false;
        }
        return isTargeted(entity) && !isIgnored(entity);
    }

    /**
     * The set of all current targets for this NPC.
     */
    public HashSet<SentinelCurrentTarget> currentTargets = new HashSet<>();
    /**
     * Adds a temporary target to this NPC (and squadmates if relevant).
     */
    public void addTarget(UUID id) {
        if (id.equals(getLivingEntity().getUniqueId())) {
            return;
        }
        if (!(SentinelUtilities.getEntityForID(id) instanceof LivingEntity)) {
            return;
        }
        addTargetNoBounce(id);
        if (sentinel.squad != null) {
            for (NPC npc : CitizensAPI.getNPCRegistry()) {
                if (npc.hasTrait(SentinelTrait.class)) {
                    SentinelTrait squadMade = npc.getTrait(SentinelTrait.class);
                    if (squadMade.squad != null && squadMade.squad.equals(sentinel.squad)) {
                        addTargetNoBounce(id);
                    }
                }
            }
        }
    }

    /**
     * Removes a temporary target from this NPC (and squadmates if relevant).
     * Returns whether anything was removed.
     */
    public boolean removeTarget(UUID id) {
        SentinelCurrentTarget target = new SentinelCurrentTarget();
        target.targetID = id;
        boolean removed = currentTargets.remove(target);
        if (removed && sentinel.squad != null) {
            for (NPC npc : CitizensAPI.getNPCRegistry()) {
                if (npc.hasTrait(SentinelTrait.class)) {
                    SentinelTrait squadMade = npc.getTrait(SentinelTrait.class);
                    if (squadMade.squad != null && squadMade.squad.equals(sentinel.squad)) {
                        sentinel.targetingHelper.currentTargets.remove(target);
                    }
                }
            }
        }
        return removed;
    }

    /**
     * Adds a target directly to the NPC. Prefer {@code addTarget} over this in most cases.
     */
    public void addTargetNoBounce(UUID id) {
        SentinelCurrentTarget target = new SentinelCurrentTarget();
        target.targetID = id;
        target.ticksLeft = sentinel.enemyTargetTime;
        currentTargets.remove(target);
        currentTargets.add(target);
    }

    /**
     * Returns whether an entity is invisible to this NPC.
     */
    public boolean isInvisible(LivingEntity entity) {
        SentinelCurrentTarget sct = new SentinelCurrentTarget();
        sct.targetID = entity.getUniqueId();
        return !currentTargets.contains(sct) && SentinelUtilities.isInvisible(entity);
    }

    /**
     * Returns whether an entity is ignored by this NPC's ignore lists.
     */
    public boolean isIgnored(LivingEntity entity) {
        if (isInvisible(entity)) {
            return true;
        }
        if (entity.getUniqueId().equals(getLivingEntity().getUniqueId())) {
            return true;
        }
        if (sentinel.getGuarding() != null && entity.getUniqueId().equals(sentinel.getGuarding())) {
            return true;
        }
        sentinel.allIgnores.checkRecalculateTargetsCache();
        if (sentinel.allIgnores.targetsProcessed.contains(SentinelTarget.OWNER) && entity.getUniqueId().equals(getNPC().getTrait(Owner.class).getOwnerId())) {
            return true;
        }
        return sentinel.allIgnores.isTarget(entity);
    }

    /**
     * Returns whether an entity is targeted by this NPC's ignore lists.
     */
    public boolean isTargeted(LivingEntity entity) {
        if (isInvisible(entity)) {
            return false;
        }
        if (entity.getUniqueId().equals(getLivingEntity().getUniqueId())) {
            return false;
        }
        if (sentinel.getGuarding() != null && entity.getUniqueId().equals(sentinel.getGuarding())) {
            return false;
        }
        SentinelCurrentTarget target = new SentinelCurrentTarget();
        target.targetID = entity.getUniqueId();
        if (currentTargets.contains(target)) {
            return true;
        }
        sentinel.allTargets.checkRecalculateTargetsCache();
        if (sentinel.allTargets.targetsProcessed.contains(SentinelTarget.OWNER) && entity.getUniqueId().equals(getNPC().getTrait(Owner.class).getOwnerId())) {
            return true;
        }
        return sentinel.allTargets.isTarget(entity);
    }

    /**
     * This method searches for the nearest targetable entity with direct line-of-sight.
     * Failing a direct line of sight, the nearest entity in range at all will be chosen.
     */
    public LivingEntity findBestTarget() {
        boolean ignoreGlow = itemHelper.usesSpectral();
        double rangesquared = sentinel.range * sentinel.range;
        double crsq = sentinel.chaseRange * sentinel.chaseRange;
        Location pos = sentinel.getGuardZone();
        if (!sentinel.getGuardZone().getWorld().equals(getLivingEntity().getWorld())) {
            // Emergency corrective measures...
            getNPC().getNavigator().cancelNavigation();
            getLivingEntity().teleport(sentinel.getGuardZone());
            return null;
        }
        if (!pos.getWorld().equals(getLivingEntity().getWorld())) {
            return null;
        }
        LivingEntity closest = null;
        boolean wasLos = false;
        for (LivingEntity ent : getLivingEntity().getWorld().getLivingEntities()) {
            if ((ignoreGlow && ent.isGlowing()) || ent.isDead()) {
                continue;
            }
            double dist = ent.getEyeLocation().distanceSquared(pos);
            SentinelCurrentTarget sct = new SentinelCurrentTarget();
            sct.targetID = ent.getUniqueId();
            if ((dist < rangesquared && shouldTarget(ent) && canSee(ent)) || (dist < crsq && currentTargets.contains(sct))) {
                boolean hasLos = canSee(ent);
                if (!wasLos || hasLos) {
                    rangesquared = dist;
                    closest = ent;
                    wasLos = hasLos;
                }
            }
        }
        return closest;
    }

    /**
     * Updates the current targets set for the NPC.
     */
    public void updateTargets() {
        for (SentinelCurrentTarget uuid : new HashSet<>(currentTargets)) {
            Entity e = SentinelUtilities.getEntityForID(uuid.targetID);
            if (e == null) {
                currentTargets.remove(uuid);
                continue;
            }
            if (e instanceof Player && (((Player) e).getGameMode() == GameMode.CREATIVE || ((Player) e).getGameMode() == GameMode.SPECTATOR)) {
                currentTargets.remove(uuid);
                continue;
            }
            if (e.isDead()) {
                currentTargets.remove(uuid);
                continue;
            }
            double d = e.getWorld().equals(getLivingEntity().getWorld()) ?
                    e.getLocation().distanceSquared(getLivingEntity().getLocation())
                    : 10000.0 * 10000.0;
            if (d > sentinel.range * sentinel.range * 4 && d > sentinel.chaseRange * sentinel.chaseRange * 4) {
                currentTargets.remove(uuid);
                continue;
            }
            if (uuid.ticksLeft > 0) {
                uuid.ticksLeft -= SentinelPlugin.instance.tickRate;
                if (uuid.ticksLeft <= 0) {
                    currentTargets.remove(uuid);
                }
            }
        }
        if (sentinel.chasing != null) {
            SentinelCurrentTarget cte = new SentinelCurrentTarget();
            cte.targetID = sentinel.chasing.getUniqueId();
            if (!currentTargets.contains(cte)) {
                sentinel.chasing = null;
                getNPC().getNavigator().cancelNavigation();
            }
        }
    }
}
