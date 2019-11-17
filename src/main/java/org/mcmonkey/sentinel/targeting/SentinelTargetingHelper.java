package org.mcmonkey.sentinel.targeting;

import net.citizensnpcs.api.astar.AStarMachine;
import net.citizensnpcs.api.astar.pathfinder.*;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.mcmonkey.sentinel.*;
import org.mcmonkey.sentinel.events.SentinelNoMoreTargetsEvent;

import java.util.ArrayList;
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
        if (!getLivingEntity().getWorld().equals(entity.getWorld())) {
            return false;
        }
        if (getLivingEntity().getEyeLocation().distanceSquared(entity.getEyeLocation()) > sentinel.range * sentinel.range) {
            return false;
        }
        if (!getLivingEntity().hasLineOfSight(entity)) {
            return false;
        }
        if (sentinel.realistic) {
            if (!SentinelUtilities.isLookingTowards(getLivingEntity().getEyeLocation(), entity.getLocation(), 90, 110)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns whether the NPC should target a specific entity.
     */
    public boolean shouldTarget(LivingEntity entity) {
        if (entity.getUniqueId().equals(getLivingEntity().getUniqueId())) {
            return false;
        }
        return isTargeted(entity) && !isIgnored(entity);
    }

    /**
     * Returns whether the NPC should avoid a specific entity.
     */
    public boolean shouldAvoid(LivingEntity entity) {
        if (entity.getUniqueId().equals(getLivingEntity().getUniqueId())) {
            return false;
        }
        return isAvoided(entity) && !isIgnored(entity);
    }

    /**
     * The set of all current targets for this NPC.
     */
    public HashSet<SentinelCurrentTarget> currentTargets = new HashSet<>();

    /**
     * The set of all current avoids for this NPC.
     */
    public HashSet<SentinelCurrentTarget> currentAvoids = new HashSet<>();

    /**
     * Adds a temporary avoid to this NPC.
     */
    public void addAvoid(UUID id) {
        if (id.equals(getLivingEntity().getUniqueId())) {
            return;
        }
        if (!(SentinelUtilities.getEntityForID(id) instanceof LivingEntity)) {
            return;
        }
        SentinelCurrentTarget target = new SentinelCurrentTarget();
        target.targetID = id;
        target.ticksLeft = SentinelPlugin.instance.runAwayTime;
        currentAvoids.remove(target);
        currentAvoids.add(target);
    }

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
        SentinelCurrentTarget target = new SentinelCurrentTarget();
        target.targetID = id;
        target.ticksLeft = sentinel.enemyTargetTime;
        currentTargets.remove(target);
        currentTargets.add(target);
        if (sentinel.squad != null) {
            for (SentinelTrait squadMate : SentinelPlugin.instance.cleanCurrentList()) {
                if (squadMate.squad != null && squadMate.squad.equals(sentinel.squad)) {
                    squadMate.targetingHelper.addTargetNoBounce(id);
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
        boolean removed = removeTargetNoBounce(target);
        if (removed && sentinel.squad != null) {
            for (SentinelTrait squadMate : SentinelPlugin.instance.cleanCurrentList()) {
                if (squadMate.squad != null && squadMate.squad.equals(sentinel.squad)) {
                    squadMate.targetingHelper.removeTargetNoBounce(target);
                }
            }
        }
        return removed;
    }

    /**
     * Removes a target directly from the NPC. Prefer {@code removeTarget} over this in most cases.
     * Returns whether anything was removed.
     */
    public boolean removeTargetNoBounce(SentinelCurrentTarget target) {
        if (currentTargets.isEmpty()) {
            return false;
        }
        if (currentTargets.remove(target)) {
            if (currentTargets.isEmpty()) {
                Bukkit.getPluginManager().callEvent(new SentinelNoMoreTargetsEvent(getNPC()));
            }
            return true;
        }
        return false;
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
        if (isUntargetable(entity)) {
            return true;
        }
        if (isInvisible(entity)) {
            return true;
        }
        if (entity.getUniqueId().equals(getLivingEntity().getUniqueId())) {
            return true;
        }
        if (sentinel.getGuarding() != null && entity.getUniqueId().equals(sentinel.getGuarding())) {
            return true;
        }
        return sentinel.allIgnores.isTarget(entity, sentinel);
    }

    private SentinelCurrentTarget tempTarget = new SentinelCurrentTarget();

    /**
     * Returns whether an entity is targeted by this NPC's target lists.
     * Consider calling 'shouldTarget' instead.
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
        if (isUntargetable(entity)) {
            return false;
        }
        tempTarget.targetID = entity.getUniqueId();
        if (currentTargets.contains(tempTarget)) {
            return true;
        }
        return sentinel.allTargets.isTarget(entity, sentinel);
    }

    /**
     * Returns whether an entity is marked to be avoided by this NPC's avoid lists.
     */
    public boolean isAvoided(LivingEntity entity) {
        if (isInvisible(entity)) {
            return false;
        }
        if (entity.getUniqueId().equals(getLivingEntity().getUniqueId())) {
            return false;
        }
        if (sentinel.getGuarding() != null && entity.getUniqueId().equals(sentinel.getGuarding())) {
            return false;
        }
        tempTarget.targetID = entity.getUniqueId();
        if (currentAvoids.contains(tempTarget)) {
            return true;
        }
        return sentinel.allAvoids.isTarget(entity, sentinel);
    }

    private ArrayList<LivingEntity> avoidanceList = new ArrayList<>();

    /**
     * Process avoid necessary avoidance. Builds a list of things we need to run away from, and then runs.
     */
    public void processAvoidance() {
        avoidanceList.clear();
        double range = sentinel.avoidRange + 10;
        for (Entity entity : getLivingEntity().getWorld().getNearbyEntities(getLivingEntity().getLocation(), range, 16, range)) {
            if (!(entity instanceof LivingEntity)) {
                continue;
            }
            tempTarget.targetID = entity.getUniqueId();
            if (!canSee((LivingEntity) entity) && !targetingHelper.currentAvoids.contains(tempTarget)) {
                continue;
            }
            if (shouldAvoid((LivingEntity) entity)) {
                avoidanceList.add((LivingEntity) entity);
                targetingHelper.addAvoid(entity.getUniqueId());
            }
        }
        if (avoidanceList.isEmpty()) {
            return;
        }
        Location runTo = findBestRunSpot();
        if (runTo != null) {
            sentinel.pathTo(runTo);
            if (SentinelPlugin.debugMe) {
                sentinel.debug("Running from threats, movement vector: " +
                        runTo.clone().subtract(getLivingEntity().getLocation()).toVector().toBlockVector().toString());
            }
        }
        else {
            if (SentinelPlugin.debugMe) {
                sentinel.debug("I have nowhere to run!");
            }
        }
    }

    /**
     * Finds a spot this NPC should run to, to avoid threats. Returns null if there's nowhere to run.
     */
    public Location findBestRunSpot() {
        if (sentinel.avoidReturnPoint != null
                && sentinel.avoidReturnPoint.getWorld().equals(getLivingEntity().getWorld())) {
            return sentinel.avoidReturnPoint.clone();
        }
        Location pos = sentinel.getGuardZone();
        if (!pos.getWorld().equals(getLivingEntity().getWorld())) {
            // Emergency corrective measures...
            getNPC().getNavigator().cancelNavigation();
            getLivingEntity().teleport(sentinel.getGuardZone());
            return null;
        }
        LivingEntity closestThreat = null;
        double threatRangeSquared = 1000 * 1000;
        for (LivingEntity entity : avoidanceList) {
            double dist = entity.getLocation().distanceSquared(pos);
            if (dist < threatRangeSquared) {
                closestThreat = entity;
                threatRangeSquared = dist;
            }
        }
        if (closestThreat == null) {
            return null;
        }
        if (threatRangeSquared >= sentinel.avoidRange * sentinel.avoidRange) {
            if (SentinelPlugin.debugMe) {
                sentinel.debug("Threats are getting close... holding my post.");
            }
            return pos.clone();
        }
        return runDirection(pos);
    }

    private double[] threatDists = new double[36];

    private static Vector[] directionReferenceVectors = new Vector[36];

    static {
        for (int i = 0; i < 36; i++) {
            double yaw = i * 10;
            // negative yaw in x because Minecraft worlds are inverted
            directionReferenceVectors[i] = new Vector(Math.sin(-yaw * (Math.PI / 180)), 0, Math.cos(yaw * (Math.PI / 180)));
        }
    }

    private static AStarMachine ASTAR = AStarMachine.createWithDefaultStorage();

    private static BlockExaminer examiner = new MinecraftBlockExaminer();

    /**
     * Returns a spot to run to if running in a certain direction.
     * Returns null if can't reasonable run that direction.
     */
    public static Location findSpotForRunDirection(Location start, double distance, Vector direction) {
        VectorGoal goal = new VectorGoal(start.clone().add(direction.clone().multiply(distance)), 4);
        VectorNode startNode = new VectorNode(goal, start, new ChunkBlockSource(start, (float)distance + 10), examiner);
        Path resultPath = (Path) ASTAR.runFully(goal, startNode, (int)(distance * 50));
        if (resultPath == null || resultPath.isComplete()) {
            return null;
        }
        Vector current = resultPath.getCurrentVector();
        while (!resultPath.isComplete()) {
            current = resultPath.getCurrentVector();
            resultPath.update(null);
        }
        return current.toLocation(start.getWorld());
    }

    /**
     * Returns a direction to run in, avoiding threatening entities as best as possible.
     * Returns a location of the spot to run to.
     * Returns null if nowhere to run.
     */
    public Location runDirection(Location center) {
        for (int i = 0; i < 36; i++) {
            threatDists[i] = 1000 * 1000;
        }
        double range = sentinel.avoidRange;
        Vector centerVec = center.toVector();
        for (LivingEntity entity : avoidanceList) {
            Vector relative = entity.getLocation().toVector().subtract(centerVec);
            for (int i = 0; i < 36; i++) {
                double dist = relative.distanceSquared(directionReferenceVectors[i].clone().multiply(range));
                if (dist < threatDists[i]) {
                    threatDists[i] = dist;
                }
            }
        }
        double longestDistance = 0;
        Location runTo = null;
        for (int i = 0; i < 36; i++) {
            if (threatDists[i] > longestDistance) {
                Location newRunTo = findSpotForRunDirection(center, range, directionReferenceVectors[i].clone());
                if (newRunTo != null) {
                    runTo = newRunTo;
                    longestDistance = threatDists[i];
                }
            }
        }
        if (SentinelPlugin.debugMe) {
            SentinelPlugin.instance.getLogger().info("(TEMP) Run to get threat distance: " + longestDistance + " to " + runTo + " from " + center.toVector());
        }
        return runTo;
    }

    /**
     * This method searches for the nearest targetable entity with direct line-of-sight.
     * Failing a direct line of sight, the nearest entity in range at all will be chosen.
     */
    public LivingEntity findBestTarget() {
        if (sentinel.chasing != null && SentinelPlugin.instance.retainTarget) {
            return sentinel.chasing;
        }
        boolean ignoreGlow = itemHelper.usesSpectral();
        double rangesquared = sentinel.range * sentinel.range;
        double crsq = sentinel.chaseRange * sentinel.chaseRange;
        Location pos = sentinel.getGuardZone();
        if (!pos.getWorld().equals(getLivingEntity().getWorld())) {
            // Emergency corrective measures...
            getNPC().getNavigator().cancelNavigation();
            getLivingEntity().teleport(sentinel.getGuardZone());
            return null;
        }
        LivingEntity closest = null;
        boolean wasLos = false;
        for (Entity loopEnt : getLivingEntity().getWorld().getNearbyEntities(pos, sentinel.range, sentinel.range, sentinel.range)) {
            if (!(loopEnt instanceof LivingEntity)) {
                continue;
            }
            LivingEntity ent = (LivingEntity) loopEnt;
            if ((ignoreGlow && ent.isGlowing()) || ent.isDead()) {
                continue;
            }
            double dist = ent.getEyeLocation().distanceSquared(pos);
            tempTarget.targetID = ent.getUniqueId();
            if ((dist < rangesquared && shouldTarget(ent) && canSee(ent)) || (dist < crsq && currentTargets.contains(tempTarget))) {
                boolean hasLos = canSee(ent);
                if (!wasLos || hasLos) {
                    rangesquared = dist;
                    closest = ent;
                    wasLos = hasLos;
                }
            }
        }
        if (closest != null) {
            addTarget(closest.getUniqueId());
        }
        return closest;
    }

    /**
     * Process all current multi-targets.
     */
    public void processAllMultiTargets() {
        processMultiTargets(sentinel.allTargets, TargetListType.TARGETS);
        processMultiTargets(sentinel.allAvoids, TargetListType.AVOIDS);
    }

    /**
     * The types of target lists.
     */
    public enum TargetListType {
        TARGETS, IGNORES, AVOIDS
    }

    /**
     * Process a specific set of multi-targets.
     */
    public void processMultiTargets(SentinelTargetList baseList, TargetListType type) {
        if (type == null) {
            return;
        }
        if (baseList.byMultiple.isEmpty()) {
            return;
        }
        ArrayList<SentinelTargetList> subList = new ArrayList<>(baseList.byMultiple.size());
        for (SentinelTargetList list : baseList.byMultiple) {
            SentinelTargetList toAdd = list.duplicate();
            toAdd.recalculateCacheNoClear();
            subList.add(toAdd);
            if (SentinelPlugin.debugMe) {
                SentinelPlugin.instance.getLogger().info("Multi-Target Debug: " + toAdd.totalTargetsCount() + " at start: " + toAdd.toMultiTargetString());
            }
        }
        Location pos = sentinel.getGuardZone();
        for (Entity loopEnt : getLivingEntity().getWorld().getNearbyEntities(pos, sentinel.range, sentinel.range, sentinel.range)) {
            if (!(loopEnt instanceof LivingEntity)) {
                continue;
            }
            LivingEntity ent = (LivingEntity) loopEnt;
            if (ent.isDead()) {
                continue;
            }
            if (isIgnored(ent)) {
                continue;
            }
            if (!canSee(ent)) {
                continue;
            }
            for (SentinelTargetList lister : subList) {
                if (lister.ifIsTargetDeleteTarget(ent)) {
                    if (SentinelPlugin.debugMe) {
                        SentinelPlugin.instance.getLogger().info("Multi-Target Debug: " + ent.getName() + " (" + ent.getType().name() + ") checked off for a list.");
                    }
                    lister.tempTargeted.add(ent);
                    if (lister.totalTargetsCount() == 0) {
                        if (SentinelPlugin.debugMe) {
                            SentinelPlugin.instance.getLogger().info("Multi-Target Debug: " + lister.totalTargetsCount() + " completed: " + lister.toMultiTargetString());
                        }
                        for (LivingEntity subEnt : lister.tempTargeted) {
                            if (type == TargetListType.TARGETS) {
                                addTarget(subEnt.getUniqueId());
                            }
                            else if (type == TargetListType.AVOIDS) {
                                addAvoid(subEnt.getUniqueId());
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Finds a nearby target that can be hit with a melee attack.
     */
    public LivingEntity findQuickMeleeTarget() {
        double range = sentinel.reach * 0.75;
        Location pos = getLivingEntity().getEyeLocation();
        for (Entity loopEnt : getLivingEntity().getWorld().getNearbyEntities(pos, range, range, range)) {
            if (loopEnt instanceof LivingEntity && shouldTarget((LivingEntity) loopEnt)
                    && canSee((LivingEntity) loopEnt)) {
                return (LivingEntity) loopEnt;
            }
        }
        return null;
    }

    /**
     * Updates the current avoids set for the NPC.
     */
    public void updateAvoids() {
        for (SentinelCurrentTarget curTarg : new HashSet<>(currentAvoids)) {
            Entity e = SentinelUtilities.getEntityForID(curTarg.targetID);
            if (e == null) {
                currentAvoids.remove(curTarg);
                continue;
            }
            if (e.isDead()) {
                currentAvoids.remove(curTarg);
                continue;
            }
            if (curTarg.ticksLeft > 0) {
                curTarg.ticksLeft -= SentinelPlugin.instance.tickRate;
                if (curTarg.ticksLeft <= 0) {
                    currentAvoids.remove(curTarg);
                }
            }
        }
    }

    /**
     * Returns whether an entity is not able to be targeted at all.
     */
    public static boolean isUntargetable(Entity e) {
        return e == null ||
                (e instanceof Player && (((Player) e).getGameMode() == GameMode.CREATIVE || ((Player) e).getGameMode() == GameMode.SPECTATOR)) ||
                e.isDead();
    }

    /**
     * Updates the current targets set for the NPC.
     */
    public void updateTargets() {
        for (SentinelCurrentTarget curTarg : new HashSet<>(currentTargets)) {
            Entity e = SentinelUtilities.getEntityForID(curTarg.targetID);
            if (isUntargetable(e)) {
                currentTargets.remove(curTarg);
                continue;
            }
            double d = e.getWorld().equals(getLivingEntity().getWorld()) ?
                    e.getLocation().distanceSquared(getLivingEntity().getLocation())
                    : 10000.0 * 10000.0;
            if (d > sentinel.range * sentinel.range * 4 && d > sentinel.chaseRange * sentinel.chaseRange * 4) {
                currentTargets.remove(curTarg);
                continue;
            }
            if (e instanceof LivingEntity && isIgnored((LivingEntity) e)) {
                currentTargets.remove(curTarg);
                continue;
            }
            if (curTarg.ticksLeft > 0) {
                curTarg.ticksLeft -= SentinelPlugin.instance.tickRate;
                if (curTarg.ticksLeft <= 0) {
                    currentTargets.remove(curTarg);
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
