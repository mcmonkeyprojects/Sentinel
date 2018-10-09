package org.mcmonkey.sentinel;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.trait.Owner;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

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
        if (SentinelTarget.v1_9) {
            if (entity.getEquipment() != null && entity.getEquipment().getItemInMainHand() != null
                    && SentinelUtilities.isRegexTargeted(entity.getEquipment().getItemInMainHand().getType().name(), sentinel.heldItemIgnores)) {
                return true;
            }
        }
        else {
            if (entity.getEquipment() != null && entity.getEquipment().getItemInHand() != null
                    && SentinelUtilities.isRegexTargeted(entity.getEquipment().getItemInHand().getType().name(), sentinel.heldItemIgnores)) {
                return true;
            }
        }
        for (SentinelIntegration integration : SentinelPlugin.integrations) {
            for (String text : sentinel.otherIgnores) {
                if (integration.isTarget(entity, text)) {
                    return true;
                }
            }
        }
        if (entity.hasMetadata("NPC")) {
            return sentinel.ignores.contains(SentinelTarget.NPCS.name()) ||
                    SentinelUtilities.isRegexTargeted(CitizensAPI.getNPCRegistry().getNPC(entity).getName(), sentinel.npcNameIgnores);
        }
        else if (entity instanceof Player) {
            if (((Player) entity).getGameMode() == GameMode.CREATIVE || ((Player) entity).getGameMode() == GameMode.SPECTATOR) {
                return true;
            }
            if (SentinelUtilities.isRegexTargeted(((Player) entity).getName(), sentinel.playerNameIgnores)) {
                return true;
            }
            if (SentinelPlugin.instance.vaultPerms != null) {
                for (String group : sentinel.groupIgnores) {
                    if (SentinelPlugin.instance.vaultPerms.playerInGroup((Player) entity, group)) {
                        return true;
                    }
                }
            }
        }
        else if (SentinelUtilities.isRegexTargeted(entity.getCustomName() == null ? entity.getType().name() : entity.getCustomName(), sentinel.entityNameIgnores)) {
            return true;
        }
        if (sentinel.ignores.contains(SentinelTarget.OWNER.name()) && entity.getUniqueId().equals(getNPC().getTrait(Owner.class).getOwnerId())) {
            return true;
        }
        HashSet<SentinelTarget> possible = SentinelPlugin.entityToTargets.get(entity.getType());
        for (SentinelTarget poss : possible) {
            if (sentinel.ignores.contains(poss.name())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns whether an entity is targeted by this NPC's ignore lists.
     */
    public boolean isTargeted(LivingEntity entity) {
        if (isInvisible(entity)) {
            return false;
        }
        SentinelCurrentTarget target = new SentinelCurrentTarget();
        target.targetID = entity.getUniqueId();
        if (entity.getUniqueId().equals(getLivingEntity().getUniqueId())) {
            return false;
        }
        if (sentinel.getGuarding() != null && entity.getUniqueId().equals(sentinel.getGuarding())) {
            return false;
        }
        if (currentTargets.contains(target)) {
            return true;
        }
        if (SentinelTarget.v1_9) {
            if (entity.getEquipment() != null && entity.getEquipment().getItemInMainHand() != null
                    && SentinelUtilities.isRegexTargeted(entity.getEquipment().getItemInMainHand().getType().name(), sentinel.heldItemTargets)) {
                return true;
            }
        }
        else {
            if (entity.getEquipment() != null && entity.getEquipment().getItemInHand() != null
                    && SentinelUtilities.isRegexTargeted(entity.getEquipment().getItemInHand().getType().name(), sentinel.heldItemTargets)) {
                return true;
            }
        }
        for (SentinelIntegration integration : SentinelPlugin.integrations) {
            for (String text : sentinel.otherTargets) {
                if (integration.isTarget(entity, text)) {
                    return true;
                }
            }
        }
        if (entity.hasMetadata("NPC")) {
            return sentinel.targets.contains(SentinelTarget.NPCS.name()) ||
                    SentinelUtilities.isRegexTargeted(CitizensAPI.getNPCRegistry().getNPC(entity).getName(), sentinel.npcNameTargets);
        }
        if (entity instanceof Player) {
            if (SentinelUtilities.isRegexTargeted(((Player) entity).getName(), sentinel.playerNameTargets)) {
                return true;
            }
            if (SentinelPlugin.instance.vaultPerms != null) {
                for (String group : sentinel.groupTargets) {
                    if (SentinelPlugin.instance.vaultPerms.playerInGroup((Player) entity, group)) {
                        return true;
                    }
                }
            }
        }
        else if (SentinelUtilities.isRegexTargeted(entity.getCustomName() == null ? entity.getType().name() : entity.getCustomName(), sentinel.entityNameTargets)) {
            return true;
        }
        if (sentinel.targets.contains(SentinelTarget.OWNER.name()) && entity.getUniqueId().equals(getNPC().getTrait(Owner.class).getOwnerId())) {
            return true;
        }
        HashSet<SentinelTarget> possible = SentinelPlugin.entityToTargets.get(entity.getType());
        for (SentinelTarget poss : possible) {
            if (sentinel.targets.contains(poss.name())) {
                return true;
            }
        }
        return false;
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
