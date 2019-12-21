package org.mcmonkey.sentinel;

import net.citizensnpcs.api.ai.TargetType;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.mcmonkey.sentinel.events.SentinelAttackEvent;
import org.mcmonkey.sentinel.targeting.SentinelTarget;

/**
 * Helper for Sentinel NPCs attacking targets.
 */
public class SentinelAttackHelper extends SentinelHelperObject {

    /**
     * Causes the NPC to chase a target.
     */
    public void chase(LivingEntity entity) {
        if (getNPC().getNavigator().getTargetType() == TargetType.LOCATION
                && getNPC().getNavigator().getTargetAsLocation() != null
                && ((getNPC().getNavigator().getTargetAsLocation().getWorld().equals(entity.getWorld())
                && getNPC().getNavigator().getTargetAsLocation().distanceSquared(entity.getLocation()) < 2 * 2))) {
            return;
        }
        sentinel.cleverTicks = 0;
        sentinel.chasing = entity;
        sentinel.chased = true;
        sentinel.needsSafeReturn = true;
        if (getNPC().getNavigator().getTargetType() == TargetType.ENTITY
                && SentinelUtilities.getTargetFor(getNPC().getNavigator().getEntityTarget()).getUniqueId().equals(entity.getUniqueId())) {
            return;
        }
        /*
        Location goal = entity.getLocation().clone().add(entity.getVelocity().clone());
        npc.getNavigator().setTarget(goal);
        bunny_goal = goal;
        */
        if (SentinelPlugin.instance.workaroundEntityChasePathfinder) {
            final Location entityEyeLoc = entity.getEyeLocation();
            getNPC().getNavigator().setTarget(entity.getLocation());
            getNPC().getNavigator().getLocalParameters().lookAtFunction(n -> entityEyeLoc);
        }
        else {
            getNPC().getNavigator().setTarget(entity, false);
        }
        getNPC().getNavigator().getLocalParameters().stuckAction(null);
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
        else if (SentinelTarget.isRangedWeapon(SentinelUtilities.getHeldItem(entity))) {
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
     * Internal attack attempt logic.
     */
    public boolean tryAttackInternal(LivingEntity entity) {
        if (!entity.getWorld().equals(getLivingEntity().getWorld())) {
            return false;
        }
        if (!getLivingEntity().hasLineOfSight(entity)) {
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
        SentinelAttackEvent sat = new SentinelAttackEvent(getNPC());
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
            if (targetingHelper.canSee(entity)) {
                if (sentinel.timeSinceAttack < sentinel.attackRateRanged) {
                    if (sentinel.rangedChase) {
                        rechase();
                    }
                    return false;
                }
                sentinel.timeSinceAttack = 0;
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
            else if (sentinel.rangedChase) {
                chase(entity);
                return false;
            }
        }
        else if (itemHelper.usesSnowball()) {
            if (targetingHelper.canSee(entity)) {
                if (sentinel.timeSinceAttack < sentinel.attackRateRanged) {
                    if (sentinel.rangedChase) {
                        rechase();
                    }
                    return false;
                }
                sentinel.timeSinceAttack = 0;
                weaponHelper.fireSnowball(entity.getEyeLocation());
                if (sentinel.needsAmmo) {
                    itemHelper.takeOne();
                    itemHelper.grabNextItem();
                }
                return true;
            }
            else if (sentinel.rangedChase) {
                chase(entity);
                return false;
            }
        }
        else if (itemHelper.usesTrident()) {
            if (targetingHelper.canSee(entity)) {
                if (sentinel.timeSinceAttack < sentinel.attackRateRanged) {
                    if (sentinel.rangedChase) {
                        rechase();
                    }
                    return false;
                }
                sentinel.timeSinceAttack = 0;
                weaponHelper.fireTrident(entity.getEyeLocation());
                if (sentinel.needsAmmo) {
                    itemHelper.takeOne();
                    itemHelper.grabNextItem();
                }
                return true;
            }
            else if (sentinel.rangedChase) {
                chase(entity);
                return false;
            }
        }
        else if (itemHelper.usesPotion()) {
            if (targetingHelper.canSee(entity)) {
                if (sentinel.timeSinceAttack < sentinel.attackRateRanged) {
                    if (sentinel.rangedChase) {
                        rechase();
                    }
                    return false;
                }
                sentinel.timeSinceAttack = 0;
                if (SentinelTarget.v1_9) {
                    weaponHelper.firePotion(getLivingEntity().getEquipment().getItemInMainHand(),
                            entity.getEyeLocation(), entity.getVelocity());
                }
                else {
                    weaponHelper.firePotion(getLivingEntity().getEquipment().getItemInHand(),
                            entity.getEyeLocation(), entity.getVelocity());
                }
                if (sentinel.needsAmmo) {
                    itemHelper.takeOne();
                    itemHelper.grabNextItem();
                }
                return true;
            }
            else if (sentinel.rangedChase) {
                chase(entity);
                return false;
            }
        }
        else if (itemHelper.usesEgg()) {
            if (targetingHelper.canSee(entity)) {
                if (sentinel.timeSinceAttack < sentinel.attackRateRanged) {
                    if (sentinel.rangedChase) {
                        rechase();
                    }
                    return false;
                }
                sentinel.timeSinceAttack = 0;
                weaponHelper.fireEgg(entity.getEyeLocation());
                if (sentinel.needsAmmo) {
                    itemHelper.takeOne();
                    itemHelper.grabNextItem();
                }
                return true;
            }
            else if (sentinel.rangedChase) {
                chase(entity);
                return false;
            }
        }
        else if (itemHelper.usesPearl()) {
            if (targetingHelper.canSee(entity)) {
                if (sentinel.timeSinceAttack < sentinel.attackRateRanged) {
                    if (sentinel.rangedChase) {
                        rechase();
                    }
                    return false;
                }
                sentinel.timeSinceAttack = 0;
                weaponHelper.firePearl(entity);
                if (sentinel.needsAmmo) {
                    itemHelper.takeOne();
                    itemHelper.grabNextItem();
                }
                return true;
            }
            else if (sentinel.rangedChase) {
                chase(entity);
                return false;
            }
        }
        else if (itemHelper.usesWitherSkull()) {
            if (targetingHelper.canSee(entity)) {
                if (sentinel.timeSinceAttack < sentinel.attackRateRanged) {
                    if (sentinel.rangedChase) {
                        rechase();
                    }
                    return false;
                }
                sentinel.timeSinceAttack = 0;
                weaponHelper.fireSkull(entity.getEyeLocation());
                if (sentinel.needsAmmo) {
                    itemHelper.takeOne();
                    itemHelper.grabNextItem();
                }
                return true;
            }
            else if (sentinel.rangedChase) {
                chase(entity);
                return false;
            }
        }
        else if (itemHelper.usesFireball()) {
            if (targetingHelper.canSee(entity)) {
                if (sentinel.timeSinceAttack < sentinel.attackRateRanged) {
                    if (sentinel.rangedChase) {
                        rechase();
                    }
                    return false;
                }
                sentinel.timeSinceAttack = 0;
                weaponHelper.fireFireball(entity.getEyeLocation());
                if (sentinel.needsAmmo) {
                    itemHelper.takeOne();
                    itemHelper.grabNextItem();
                }
                return true;
            }
            else if (sentinel.rangedChase) {
                chase(entity);
                return false;
            }
        }
        else if (itemHelper.usesLightning()) {
            if (targetingHelper.canSee(entity)) {
                if (sentinel.timeSinceAttack < sentinel.attackRateRanged) {
                    if (sentinel.rangedChase) {
                        rechase();
                    }
                    return false;
                }
                sentinel.timeSinceAttack = 0;
                sentinel.swingWeapon();
                entity.getWorld().strikeLightningEffect(entity.getLocation());
                if (SentinelPlugin.debugMe) {
                    debug("Lightning hits for " + sentinel.getDamage());
                }
                entity.damage(sentinel.getDamage());
                if (sentinel.needsAmmo) {
                    itemHelper.takeOne();
                    itemHelper.grabNextItem();
                }
                return true;
            }
            else if (sentinel.rangedChase) {
                chase(entity);
                return false;
            }
        }
        else if (itemHelper.usesSpectral()) {
            if (targetingHelper.canSee(entity)) {
                if (sentinel.timeSinceAttack < sentinel.attackRateRanged) {
                    if (sentinel.rangedChase) {
                        rechase();
                    }
                    return false;
                }
                sentinel.timeSinceAttack = 0;
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
                    if (sentinel.needsAmmo) {
                        itemHelper.takeOne();
                        itemHelper.grabNextItem();
                    }
                    return true;
                }
            }
            else if (sentinel.rangedChase) {
                chase(entity);
                return false;
            }
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
