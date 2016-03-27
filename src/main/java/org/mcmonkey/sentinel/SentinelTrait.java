package org.mcmonkey.sentinel;

import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.util.PlayerAnimation;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.craftbukkit.v1_9_R1.Overridden;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class SentinelTrait extends Trait {

    public SentinelTrait() {
        super("sentinel");
    }

    @Persist("stats_ticksSpawned")
    public long stats_ticksSpawned = 0;

    @Persist("stats_timesSpawned")
    public long stats_timesSpawned = 0;

    @Persist("stats_arrowsFired")
    public long stats_arrowsFired = 0;

    @Persist("stats_potionsThrow")
    public long stats_potionsThrown = 0;

    @Persist("stats_fireballsFired")
    public long stats_fireballsFired = 0;

    @Persist("stats_punches")
    public long stats_punches = 0;

    @Persist("stats_attackAttempts")
    public long stats_attackAttempts = 0;

    @Persist("targets")
    public HashSet<SentinelTarget> targets = new HashSet<SentinelTarget>();

    @Persist("range")
    public double range = 20;

    @Persist("damage")
    public double damage = -1;

    @Persist("armor")
    public double armor = -1;

    @Persist("health")
    public double health = 20;

    @Persist("ignores")
    public HashSet<SentinelTarget> ignores = new HashSet<SentinelTarget>();

    @Persist("ranged_chase")
    public boolean rangedChase = false;

    @Persist("close_chase")
    public boolean closeChase = true;

    @Persist("invincible")
    public boolean invincible = false;

    @Persist("attackRate")
    public int attackRate = 30;

    @Override
    public void onAttach() {
        FileConfiguration config = SentinelPlugin.instance.getConfig();
        attackRate = config.getInt("sentinel defaults.attack rate", 30);
        rangedChase = config.getBoolean("sentinel defaults.ranged chase target", false);
        closeChase = config.getBoolean("sentinel defaults.close chase target", true);
        armor = config.getDouble("sentinel defaults.armor", -1);
        damage = config.getDouble("sentinel defaults.damage", -1);
        health = config.getDouble("sentinel defaults.health", 20);
        if (npc.isSpawned()) {
            getLivingEntity().setMaxHealth(health);
            getLivingEntity().setHealth(health);
        }
        invincible = config.getBoolean("defaults.invincible", false);
        npc.setProtected(invincible);
    }

    public void useItem() {
        if (npc.isSpawned() && getLivingEntity() instanceof Player) {
            PlayerAnimation.START_USE_MAINHAND_ITEM.play((Player) getLivingEntity());
            BukkitRunnable runner = new BukkitRunnable() {
                @Override
                public void run() {
                    if (npc.isSpawned() && getLivingEntity() instanceof Player) {
                        PlayerAnimation.STOP_USE_ITEM.play((Player) getLivingEntity());
                    }
                }
            };
            runner.runTaskLater(SentinelPlugin.instance, 10);
        }
    }

    public void swingWeapon() {
        if (npc.isSpawned() && getLivingEntity() instanceof Player) {
            PlayerAnimation.ARM_SWING.play((Player) getLivingEntity());
        }
    }

    public HashMap.SimpleEntry<Location, Vector> getLaunchDetail(Location target, Vector lead) {
        double speed;
        npc.faceLocation(target);
        double angt = -1;
        Location start = getLivingEntity().getEyeLocation().clone().add(getLivingEntity().getEyeLocation().getDirection());
        for (speed = 20; speed <= 45; speed += 5) {
            angt = SentinelUtilities.getArrowAngle(start, target, speed, 20);
            if (!Double.isInfinite(angt)) {
                break;
            }
        }
        if (Double.isInfinite(angt)) {
            return null;
        }
        double hangT = SentinelUtilities.hangtime(angt, speed, target.getY() - start.getY(), 20);
        Location to = target.clone().add(lead.clone().multiply(hangT));
        Vector relative = to.clone().subtract(start.toVector()).toVector();
        double deltaXZ = Math.sqrt(relative.getX() * relative.getX() + relative.getZ() * relative.getZ());
        if (deltaXZ == 0) {
            deltaXZ = 0.1;
        }
        for (speed = 20; speed < 45; speed += 5) {
            angt = SentinelUtilities.getArrowAngle(start, to, speed, 20);
            if (!Double.isInfinite(angt)) {
                break;
            }
        }
        if (Double.isInfinite(angt)) {
            return null;
        }
        relative.setY(Math.tan(angt) * deltaXZ);
        relative = relative.normalize();
        Vector normrel = relative.clone();
        speed = speed + (1.188 * hangT * hangT);
        relative = relative.multiply(speed / 20.0);
        start.setDirection(normrel);
        return new HashMap.SimpleEntry<Location, Vector>(start, relative);
    }

    public void firePotion(ItemStack potion, Location target, Vector lead) {
        stats_potionsThrown++;
        HashMap.SimpleEntry<Location, Vector> start = getLaunchDetail(target, lead);
        Entity entpotion = start.getKey().getWorld().spawnEntity(start.getKey(), EntityType.SPLASH_POTION);
        ((SplashPotion) entpotion).setShooter(getLivingEntity());
        ((SplashPotion) entpotion).setItem(potion);
        entpotion.setVelocity(start.getValue());
        swingWeapon();
    }

    public void fireArrow(Location target, Vector lead) {
        stats_arrowsFired++;
        HashMap.SimpleEntry<Location, Vector> start = getLaunchDetail(target, lead);
        Entity arrow = start.getKey().getWorld().spawnEntity(start.getKey(), EntityType.ARROW);
        ((Projectile)arrow).setShooter(getLivingEntity());
        arrow.setVelocity(start.getValue());
        // TODO: Apply damage amount!
        // TODO: Prevent pick up if needed!
        useItem();
    }

    public void fireFireball(Location target) {
        swingWeapon();
        stats_fireballsFired++;
        npc.faceLocation(target);
        Vector forward = getLivingEntity().getEyeLocation().getDirection();
        Location spawnAt = getLivingEntity().getEyeLocation().clone().add(forward.clone().multiply(2));
        Entity ent = spawnAt.getWorld().spawnEntity(spawnAt, EntityType.SMALL_FIREBALL);
        ent.setVelocity(target.clone().subtract(spawnAt).toVector().normalize().multiply(4)); // TODO: Fiddle with '4'.
        // TODO: Apply damage amount!
    }

    public double getDamage() {
        if (damage < 0) {
            ItemStack weapon = getLivingEntity().getEquipment().getItemInMainHand();
            double baseDamage = 1;
            // TODO: Calculate damage!
            return baseDamage;
        }
        return damage;
    }

    // TODO: use this value!
    public double getArmor() {
        if (armor < 0) {
            double baseArmor = 0;
            // TODO: Calculate armor!
            return baseArmor;
        }
        return armor;
    }

    public void punch(LivingEntity entity) {
        npc.faceLocation(entity.getLocation());
        swingWeapon();
        stats_punches++;
        entity.damage(getDamage(), getLivingEntity());
    }

    public void chase(LivingEntity entity) {
        npc.getNavigator().setTarget(entity.getLocation());
    }

    public void tryAttack(LivingEntity entity) {
        stats_attackAttempts++;
        if (usesBow()) {
            if (canSee(entity)) {
                if (timeSinceAttack < attackRate) {
                    return;
                }
                timeSinceAttack = 0;
                // TODO: Consume ammo if needed!
                fireArrow(entity.getEyeLocation(), entity.getVelocity());
            }
            else if (rangedChase) {
                chase(entity);
            }
        }
        else if (usesPotion()) {
            double distsq = entity.getLocation().distanceSquared(getLivingEntity().getLocation());
            if (canSee(entity)) {
                if (timeSinceAttack < attackRate) {
                    return;
                }
                timeSinceAttack = 0;
                // TODO: Consume ammo if needed!
                firePotion(getLivingEntity().getEquipment().getItemInMainHand(),
                        entity.getEyeLocation(), entity.getVelocity());
            }
            else if (rangedChase) {
                chase(entity);
            }
        }
        else if (usesFireball()) {
            if (canSee(entity)) {
                if (timeSinceAttack < attackRate) {
                    return;
                }
                timeSinceAttack = 0;
                // TODO: Consume ammo if needed!
                fireFireball(entity.getEyeLocation());
            }
            else if (rangedChase) {
                chase(entity);
            }
        }
        else {
            double distsq = entity.getLocation().distanceSquared(getLivingEntity().getLocation());
            if (distsq < 3 * 3) {
                if (timeSinceAttack < attackRate) {
                    return;
                }
                timeSinceAttack = 0;
                // TODO: Damage sword if needed!
                punch(entity);
            }
            else {
                chase(entity);
            }
        }
    }

    public boolean canSee(LivingEntity entity) {
        return getLivingEntity().hasLineOfSight(entity);
    }

    public LivingEntity getLivingEntity() {
        // Not a good idea to turn a non-living NPC into a Sentinel for now.
        return (LivingEntity) npc.getEntity();
    }

    public boolean isRanged() {
        return usesBow() || usesFireball();
    }

    public boolean usesBow() {
        return getLivingEntity().getEquipment().getItemInMainHand().getType() == Material.BOW;
    }

    public boolean usesFireball() {
        return getLivingEntity().getEquipment().getItemInMainHand().getType() == Material.BLAZE_ROD;
    }

    public boolean usesFire() {
        // TODO: Use as valid weapon!
        return getLivingEntity().getEquipment().getItemInMainHand().getType() == Material.FLINT_AND_STEEL;
    }

    public boolean usesPotion() {
        // TODO: Use as valid weapon!
        return getLivingEntity().getEquipment().getItemInMainHand().getType() == Material.SPLASH_POTION;
    }

    public boolean shouldTarget(LivingEntity entity) {
        return isTargeted(entity) && !isIgnored(entity);
    }

    public boolean isIgnored(LivingEntity entity) {
        HashSet<SentinelTarget> possible = SentinelPlugin.entityToTargets.get(entity.getType());
        for (SentinelTarget poss: possible) {
            if (ignores.contains(poss)) {
                return true;
            }
        }
        return false;
    }

    public boolean isTargeted(LivingEntity entity) {
        HashSet<SentinelTarget> possible = SentinelPlugin.entityToTargets.get(entity.getType());
        for (SentinelTarget poss: possible) {
            if (targets.contains(poss)) {
                return true;
            }
        }
        return false;
    }

    public int cTick = 0;

    /**
     * This method searches for the nearest targetable entity with direct line-of-sight.
     * Failing a direct line of sight, the nearest entity in range at all will be chosen.
     */
    public LivingEntity findBestTarget() {
        double rangesquared = range * range;
        Location pos = getLivingEntity().getEyeLocation();
        LivingEntity closest = null;
        for (LivingEntity ent: getLivingEntity().getWorld().getLivingEntities()) {
            double dist = ent.getEyeLocation().distanceSquared(pos);
            if (dist < rangesquared && shouldTarget(ent) && canSee(ent)) {
                rangesquared = dist;
                closest = ent;
            }
        }
        if (closest == null) {
            // NOTE: Possibly can be optimized by retrieving a list from the above logic?
            for (LivingEntity ent: getLivingEntity().getWorld().getLivingEntities()) {
                double dist = ent.getEyeLocation().distanceSquared(pos);
                if (dist < rangesquared && shouldTarget(ent)) {
                    rangesquared = dist;
                    closest = ent;
                }
            }
        }
        return closest;
    }

    public long timeSinceAttack = 0;

    public long timeSinceHeal = 0;

    public void runUpdate() {
        timeSinceAttack += SentinelPlugin.instance.tickRate;
        timeSinceHeal += SentinelPlugin.instance.tickRate;
        // TODO: HealRate
        if (timeSinceHeal > 20 && getLivingEntity().getHealth() < health) {
            getLivingEntity().setHealth(getLivingEntity().getHealth() + 0.5);
        }
        LivingEntity target = findBestTarget();
        if (target == null) {
            return;
        }
        tryAttack(target);
    }

    @Override
    public void run() {
        if (!npc.isSpawned()) {
            return;
        }
        stats_ticksSpawned++;
        cTick++;
        if (cTick >= SentinelPlugin.instance.tickRate) {
            cTick = 0;
            runUpdate();
        }
    }

    @Override
    public void onSpawn() {
        stats_timesSpawned++;
        setHealth(health);
        setInvincible(invincible);
    }

    public void setHealth(double heal) {
        health = heal;
        if (npc.isSpawned()) {
            getLivingEntity().setMaxHealth(health);
        }
    }


    public void setInvincible(boolean inv) {
        invincible = inv;
        npc.setProtected(invincible);
    }
}
