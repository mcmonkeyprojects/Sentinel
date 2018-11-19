package org.mcmonkey.sentinel;

import net.citizensnpcs.api.ai.TargetType;
import org.bukkit.Bukkit;
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
        if (getNPC().getNavigator().getTargetType() == TargetType.ENTITY
                && SentinelUtilities.getTargetFor(getNPC().getNavigator().getEntityTarget()).getUniqueId().equals(entity.getUniqueId())) {
            return;
        }
        getNPC().getNavigator().getDefaultParameters().stuckAction(null);
        /*
        Location goal = entity.getLocation().clone().add(entity.getVelocity().clone());
        npc.getNavigator().setTarget(goal);
        bunny_goal = goal;
        */
        if (SentinelPlugin.instance.workaroundEntityChasePathfinder) {
            getNPC().getNavigator().setTarget(entity.getLocation());
        }
        else {
            getNPC().getNavigator().setTarget(entity, false);
        }
        getNPC().getNavigator().getLocalParameters().speedModifier((float) sentinel.speed);
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
     * Causes the NPC to attempt an attack on a target.
     */
    public void tryAttack(LivingEntity entity) {
        if (!entity.getWorld().equals(getLivingEntity().getWorld())) {
            return;
        }
        if (!getLivingEntity().hasLineOfSight(entity)) {
            return;
        }
        // TODO: Simplify this code!
        sentinel.stats_attackAttempts++;
        double dist = getLivingEntity().getEyeLocation().distanceSquared(entity.getEyeLocation());
        if (SentinelPlugin.debugMe) {
            debug("tryAttack at range " + dist);
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
            return;
        }
        targetingHelper.addTarget(entity.getUniqueId());
        for (SentinelIntegration si : SentinelPlugin.integrations) {
            if (si.tryAttack(sentinel, entity)) {
                return;
            }
        }
        if (itemHelper.usesBow()) {
            if (targetingHelper.canSee(entity)) {
                if (sentinel.timeSinceAttack < sentinel.attackRateRanged) {
                    if (sentinel.rangedChase) {
                        rechase();
                    }
                    return;
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
                }
            }
            else if (sentinel.rangedChase) {
                chase(entity);
            }
        }
        else if (itemHelper.usesSnowball()) {
            if (targetingHelper.canSee(entity)) {
                if (sentinel.timeSinceAttack < sentinel.attackRateRanged) {
                    if (sentinel.rangedChase) {
                        rechase();
                    }
                    return;
                }
                sentinel.timeSinceAttack = 0;
                ItemStack item = itemHelper.getArrow();
                if (item != null) {
                    weaponHelper.fireSnowball(entity.getEyeLocation());
                    if (sentinel.needsAmmo) {
                        itemHelper.takeSnowball();
                        itemHelper.grabNextItem();
                    }
                }
            }
            else if (sentinel.rangedChase) {
                chase(entity);
            }
        }
        else if (itemHelper.usesPotion()) {
            if (targetingHelper.canSee(entity)) {
                if (sentinel.timeSinceAttack < sentinel.attackRateRanged) {
                    if (sentinel.rangedChase) {
                        rechase();
                    }
                    return;
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
            }
            else if (sentinel.rangedChase) {
                chase(entity);
            }
        }
        else if (itemHelper.usesEgg()) {
            if (targetingHelper.canSee(entity)) {
                if (sentinel.timeSinceAttack < sentinel.attackRateRanged) {
                    if (sentinel.rangedChase) {
                        rechase();
                    }
                    return;
                }
                sentinel.timeSinceAttack = 0;
                weaponHelper.fireEgg(entity.getEyeLocation());
                if (sentinel.needsAmmo) {
                    itemHelper.takeOne();
                    itemHelper.grabNextItem();
                }
            }
            else if (sentinel.rangedChase) {
                chase(entity);
            }
        }
        else if (itemHelper.usesPearl()) {
            if (targetingHelper.canSee(entity)) {
                if (sentinel.timeSinceAttack < sentinel.attackRateRanged) {
                    if (sentinel.rangedChase) {
                        rechase();
                    }
                    return;
                }
                sentinel.timeSinceAttack = 0;
                weaponHelper.firePearl(entity);
                if (sentinel.needsAmmo) {
                    itemHelper.takeOne();
                    itemHelper.grabNextItem();
                }
            }
            else if (sentinel.rangedChase) {
                chase(entity);
            }
        }
        else if (itemHelper.usesWitherSkull()) {
            if (targetingHelper.canSee(entity)) {
                if (sentinel.timeSinceAttack < sentinel.attackRateRanged) {
                    if (sentinel.rangedChase) {
                        rechase();
                    }
                    return;
                }
                sentinel.timeSinceAttack = 0;
                weaponHelper.fireSkull(entity.getEyeLocation());
                if (sentinel.needsAmmo) {
                    itemHelper.takeOne();
                    itemHelper.grabNextItem();
                }
            }
            else if (sentinel.rangedChase) {
                chase(entity);
            }
        }
        else if (itemHelper.usesFireball()) {
            if (targetingHelper.canSee(entity)) {
                if (sentinel.timeSinceAttack < sentinel.attackRateRanged) {
                    if (sentinel.rangedChase) {
                        rechase();
                    }
                    return;
                }
                sentinel.timeSinceAttack = 0;
                weaponHelper.fireFireball(entity.getEyeLocation());
                if (sentinel.needsAmmo) {
                    itemHelper.takeOne();
                    itemHelper.grabNextItem();
                }
            }
            else if (sentinel.rangedChase) {
                chase(entity);
            }
        }
        else if (itemHelper.usesLightning()) {
            if (targetingHelper.canSee(entity)) {
                if (sentinel.timeSinceAttack < sentinel.attackRateRanged) {
                    if (sentinel.rangedChase) {
                        rechase();
                    }
                    return;
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
            }
            else if (sentinel.rangedChase) {
                chase(entity);
            }
        }
        else if (itemHelper.usesSpectral()) {
            if (targetingHelper.canSee(entity)) {
                if (sentinel.timeSinceAttack < sentinel.attackRateRanged) {
                    if (sentinel.rangedChase) {
                        rechase();
                    }
                    return;
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
                }
            }
            else if (sentinel.rangedChase) {
                chase(entity);
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
                    return;
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
            }
            else if (sentinel.closeChase) {
                if (SentinelPlugin.debugMe) {
                    debug("tryAttack refused, range");
                }
                chase(entity);
            }
        }
    }
}
