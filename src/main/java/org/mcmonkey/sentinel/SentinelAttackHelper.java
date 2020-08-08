package org.mcmonkey.sentinel;

import net.citizensnpcs.api.ai.TargetType;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.mcmonkey.sentinel.events.SentinelAttackEvent;
import org.mcmonkey.sentinel.utilities.SentinelVersionCompat;

/**
 * Helper for Sentinel NPCs attacking targets.
 */
public class SentinelAttackHelper extends SentinelHelperObject {

    /**
     * Causes the NPC to chase a target.
     */
    public void chase(LivingEntity entity) {
        sentinel.cleverTicks = 0;
        sentinel.chasing = entity;
        sentinel.chased = true;
        sentinel.needsSafeReturn = true;
        /*
        Location goal = entity.getLocation().clone().add(entity.getVelocity().clone());
        npc.getNavigator().setTarget(goal);
        bunny_goal = goal;
        */
        if (SentinelPlugin.instance.workaroundEntityChasePathfinder) {
            Location targetLocation = entity.getLocation().clone().add(SentinelUtilities.getVelocity(entity));
            if (getNPC().getNavigator().getTargetType() == TargetType.LOCATION
                    && getNPC().getNavigator().getTargetAsLocation() != null
                    && ((getNPC().getNavigator().getTargetAsLocation().getWorld().equals(entity.getWorld())
                    && getNPC().getNavigator().getTargetAsLocation().distanceSquared(targetLocation) < 2 * 2))) {
                return;
            }
            getNPC().getNavigator().setTarget(targetLocation);
            final Location entityEyeLoc = entity.getEyeLocation();
            getNPC().getNavigator().getLocalParameters().lookAtFunction(n -> entityEyeLoc);
        }
        else {
            if (getNPC().getNavigator().getTargetType() == TargetType.ENTITY
                    && SentinelUtilities.getTargetFor(getNPC().getNavigator().getEntityTarget()).getUniqueId().equals(entity.getUniqueId())) {
                return;
            }
            getNPC().getNavigator().setTarget(entity, false);
        }
        if (!sentinel.disableTeleporting) {
            getNPC().getNavigator().getLocalParameters().stuckAction(null);
        }
        sentinel.autoSpeedModifier();
    }

    /**
     * Repeats the last chase instruction (to ensure the NPC keeps going for a target).
     */
    public void rechase() {
        if (sentinel.chasing != null) {
            chase(sentinel.chasing);
        }
    }

    /**
     * Returns a boolean indicating whether the NPC sees a threat from the input entity (for use with shield blocking).
     */
    public boolean seesThreatFrom(LivingEntity entity) {
        if (!targetingHelper.canSee(entity)) {
            return false;
        }
        if (!SentinelUtilities.isLookingTowards(getLivingEntity().getEyeLocation(), entity.getLocation(), 60, 60)) {
            return false;
        }
        double dist = getLivingEntity().getEyeLocation().distanceSquared(entity.getEyeLocation());
        if (dist < 5 * 5) {
            return true;
        }
        else if (SentinelVersionCompat.isRangedWeapon(SentinelUtilities.getHeldItem(entity))) {
            return true;
        }
        else {
            return false;
        }
    }

    /**
     * Causes the entity to prepare to defend against a dangerous opponent (if it sees the threat).
     * This will raise the NPC's shield (if it has one).
     */
    public void tryDefendFrom(LivingEntity entity) {
        if (!itemHelper.hasShield()) {
            return;
        }
        if (seesThreatFrom(entity)) {
            sentinel.startBlocking();
        }
        else {
            sentinel.stopBlocking();
        }
    }

    /**
     * Causes the NPC to attempt an attack on a target.
     * Returns whether any attack occurred.
     */
    public boolean tryAttack(LivingEntity target) {
        if (tryAttackInternal(target)) {
            return true;
        }
        LivingEntity quickTarget = targetingHelper.findQuickMeleeTarget();
        if (quickTarget != null) {
            if (itemHelper.isRanged()) {
                if (!sentinel.autoswitch) {
                    return false;
                }
                itemHelper.swapToMelee();
                if (itemHelper.isRanged()) {
                    return false;
                }
            }
            if (tryAttackInternal(quickTarget)) {
                chase(target);
                return true;
            }
            chase(target);
        }
        return false;
    }

    /**
     * Pre-calculation for ranged attacks.
     * Returns 'true' when the attack should be cancelled.
     */
    public boolean rangedPreCalculation(LivingEntity entity) {
        double dist = getLivingEntity().getEyeLocation().distanceSquared(entity.getEyeLocation());
        if (dist < sentinel.projectileRange * sentinel.projectileRange && targetingHelper.canSee(entity)) {
            if (sentinel.timeSinceAttack < sentinel.attackRateRanged) {
                if (SentinelPlugin.debugMe) {
                    debug("tryAttack refused, timeSinceAttack");
                }
                if (sentinel.rangedChase) {
                    rechase();
                }
                return true;
            }
            sentinel.timeSinceAttack = 0;
            return false;
        }
        else if (sentinel.rangedChase) {
            if (SentinelPlugin.debugMe) {
                debug("tryAttack refused, range or visibility");
            }
            chase(entity);
            return true;
        }
        return true;
    }

    /**
     * Post-calculation for ammo handling.
     */
    public void rangedAmmoCalculation() {
        if (sentinel.needsAmmo) {
            itemHelper.takeOne();
            itemHelper.grabNextItem();
        }
    }

    /**
     * Internal attack attempt logic.
     */
    public boolean tryAttackInternal(LivingEntity entity) {
        if (!entity.getWorld().equals(getLivingEntity().getWorld())) {
            return false;
        }
        if (!getLivingEntity().hasLineOfSight(entity)) {
            if (sentinel.ignoreLOS) {
                chase(entity);
            }
            return false;
        }
        // TODO: Simplify this code!
        sentinel.stats_attackAttempts++;
        double dist = getLivingEntity().getEyeLocation().distanceSquared(entity.getEyeLocation());
        if (SentinelPlugin.debugMe) {
            debug("tryAttack at range " + (((int) (Math.sqrt(dist) * 0.1)) * 10));
        }
        if (sentinel.autoswitch && dist > sentinel.reach * sentinel.reach) {
            itemHelper.swapToRanged();
        }
        else if (sentinel.autoswitch && dist < sentinel.reach * sentinel.reach) {
            itemHelper.swapToMelee();
        }
        sentinel.chasing = entity;
        SentinelAttackEvent sat = new SentinelAttackEvent(getNPC(), entity);
        Bukkit.getPluginManager().callEvent(sat);
        if (sat.isCancelled()) {
            if (SentinelPlugin.debugMe) {
                debug("tryAttack refused, event cancellation");
            }
            return false;
        }
        targetingHelper.addTarget(entity.getUniqueId());
        for (SentinelIntegration si : SentinelPlugin.integrations) {
            if (si.tryAttack(sentinel, entity)) {
                return true;
            }
        }
        if (itemHelper.usesBow()) {
            if (rangedPreCalculation(entity)) {
                return false;
            }
            ItemStack item = itemHelper.getArrow();
            if (item != null) {
                weaponHelper.fireArrow(item, entity.getEyeLocation(), entity.getVelocity());
                if (sentinel.needsAmmo) {
                    itemHelper.reduceDurability();
                    itemHelper.takeArrow();
                    itemHelper.grabNextItem();
                }
                return true;
            }
        }
        else if (itemHelper.usesSnowball()) {
            if (rangedPreCalculation(entity)) {
                return false;
            }
            weaponHelper.fireSnowball(entity.getEyeLocation());
            rangedAmmoCalculation();
            return true;
        }
        else if (itemHelper.usesTrident()) {
            if (rangedPreCalculation(entity)) {
                return false;
            }
            weaponHelper.fireTrident(entity.getEyeLocation());
            rangedAmmoCalculation();
            return true;
        }
        else if (itemHelper.usesPotion()) {
            if (rangedPreCalculation(entity)) {
                return false;
            }
            weaponHelper.firePotion(SentinelUtilities.getHeldItem(getLivingEntity()), entity.getEyeLocation(), entity.getVelocity());
            rangedAmmoCalculation();
            return true;
        }
        else if (itemHelper.usesEgg()) {
            if (rangedPreCalculation(entity)) {
                return false;
            }
            weaponHelper.fireEgg(entity.getEyeLocation());
            rangedAmmoCalculation();
            return true;
        }
        else if (itemHelper.usesPearl()) {
            if (rangedPreCalculation(entity)) {
                return false;
            }
            weaponHelper.firePearl(entity);
            rangedAmmoCalculation();
            return true;
        }
        else if (itemHelper.usesWitherSkull()) {
            if (rangedPreCalculation(entity)) {
                return false;
            }
            weaponHelper.fireSkull(entity.getEyeLocation());
            rangedAmmoCalculation();
            return true;
        }
        else if (itemHelper.usesFireball()) {
            if (rangedPreCalculation(entity)) {
                return false;
            }
            weaponHelper.fireFireball(entity.getEyeLocation());
            rangedAmmoCalculation();
            return true;
        }
        else if (itemHelper.usesLightning()) {
            if (rangedPreCalculation(entity)) {
                return false;
            }
            sentinel.swingWeapon();
            entity.getWorld().strikeLightningEffect(entity.getLocation());
            if (SentinelPlugin.debugMe) {
                debug("Lightning hits for " + sentinel.getDamage(false));
            }
            entity.damage(sentinel.getDamage(false));
            rangedAmmoCalculation();
            return true;
        }
        else if (itemHelper.usesSpectral()) {
            if (rangedPreCalculation(entity)) {
                return false;
            }
            if (!entity.isGlowing()) {
                sentinel.swingWeapon();
                try {
                    Sound snd = SentinelPlugin.instance.spectralSound;
                    if (snd != null) {
                        entity.getWorld().playSound(entity.getLocation(), snd, 1f, 1f);
                    }
                }
                catch (Exception e) {
                    // Do nothing!
                }
                entity.setGlowing(true);
            }
            rangedAmmoCalculation();
            return true;
        }
        else {
            if (dist < sentinel.reach * sentinel.reach) {
                if (sentinel.timeSinceAttack < sentinel.attackRate) {
                    if (SentinelPlugin.debugMe) {
                        debug("tryAttack refused, timeSinceAttack");
                    }
                    if (sentinel.closeChase) {
                        rechase();
                    }
                    return false;
                }
                sentinel.timeSinceAttack = 0;
                // TODO: Damage sword if needed!
                if (SentinelPlugin.debugMe) {
                    debug("tryAttack passed!");
                }
                weaponHelper.punch(entity);
                if (sentinel.needsAmmo && itemHelper.shouldTakeDura()) {
                    itemHelper.reduceDurability();
                    itemHelper.grabNextItem();
                }
                return true;
            }
            else if (sentinel.closeChase) {
                if (SentinelPlugin.debugMe) {
                    debug("tryAttack refused, range");
                }
                chase(entity);
                return false;
            }
        }
        return false;
    }
}
