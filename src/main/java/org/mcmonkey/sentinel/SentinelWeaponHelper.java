package org.mcmonkey.sentinel;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionType;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.util.Vector;
import org.mcmonkey.sentinel.utilities.SentinelVersionCompat;

import java.util.HashMap;

/**
 * Helper for weapon management, particular attacking.
 */
public class SentinelWeaponHelper extends SentinelHelperObject {

    public static final EntityType LINGERING_POTION, TIPPED_ARROW;

    static {
        if (SentinelVersionCompat.v1_14) {
            LINGERING_POTION = EntityType.SPLASH_POTION;
            TIPPED_ARROW = EntityType.ARROW;
        }
        else if (SentinelVersionCompat.v1_9) {
            LINGERING_POTION = EntityType.valueOf("LINGERING_POTION");
            TIPPED_ARROW = EntityType.valueOf("TIPPED_ARROW");
        }
        else {
            LINGERING_POTION = null;
            TIPPED_ARROW = null;
        }
    }

    /**
     * Fires a potion from the NPC at a target.
     */
    public void firePotion(ItemStack potion, Location target, Vector lead) {
        sentinel.stats_potionsThrown++;
        HashMap.SimpleEntry<Location, Vector> start = sentinel.getLaunchDetail(target, lead);
        Entity entpotion;
        if (SentinelVersionCompat.v1_14) {
            entpotion = start.getKey().getWorld().spawnEntity(start.getKey(), EntityType.SPLASH_POTION);
        }
        else if (SentinelVersionCompat.v1_9) {
            entpotion = start.getKey().getWorld().spawnEntity(start.getKey(),
                    potion.getType() == Material.SPLASH_POTION ? EntityType.SPLASH_POTION : LINGERING_POTION);
        }
        else {
            entpotion = start.getKey().getWorld().spawnEntity(start.getKey(), EntityType.SPLASH_POTION);
        }
        ((ThrownPotion) entpotion).setShooter(getLivingEntity());
        ((ThrownPotion) entpotion).setItem(potion);
        entpotion.setVelocity(sentinel.fixForAcc(start.getValue()));
        sentinel.swingWeapon();
    }

    /**
     * Fires an arrow from the NPC at a target.
     */
    public void fireArrow(ItemStack type, Location target, Vector lead) {
        Location launchStart;
        Vector baseVelocity;
        if (SentinelVersionCompat.v1_14 && type.getType() == Material.FIREWORK_ROCKET) {
            launchStart = sentinel.getLivingEntity().getEyeLocation();
            launchStart = launchStart.clone().add(launchStart.getDirection());
            baseVelocity = target.toVector().subtract(launchStart.toVector().add(lead));
            if (baseVelocity.lengthSquared() > 0) {
                baseVelocity = baseVelocity.normalize();
            }
            baseVelocity = baseVelocity.multiply(2);
        }
        else {
            HashMap.SimpleEntry<Location, Vector> start = sentinel.getLaunchDetail(target, lead);
            if (start == null || start.getKey() == null) {
                return;
            }
            launchStart = start.getKey();
            baseVelocity = start.getValue();
        }
        Vector velocity = sentinel.fixForAcc(baseVelocity);
        sentinel.stats_arrowsFired++;
        Entity arrow;
        if (SentinelVersionCompat.v1_9) {
            if (SentinelVersionCompat.v1_14) {
                double length = Math.max(1.0, velocity.length());
                if (type.getType() == Material.FIREWORK_ROCKET) {
                    FireworkMeta meta = (FireworkMeta) type.getItemMeta();
                    meta.setPower(3);
                    arrow = launchStart.getWorld().spawn(launchStart, EntityType.FIREWORK.getEntityClass(), (e) -> {
                        ((Firework) e).setShotAtAngle(true);
                        ((Firework) e).setFireworkMeta(meta);
                        e.setVelocity(velocity);
                    });
                }
                else {
                    Class toShoot;
                    toShoot = type.getType() == Material.SPECTRAL_ARROW ? SpectralArrow.class :
                            (type.getType() == Material.TIPPED_ARROW ? TippedArrow.class : Arrow.class);
                    arrow = launchStart.getWorld().spawnArrow(launchStart, velocity.multiply(1.0 / length), (float) length, 0f, toShoot);
                    ((Projectile) arrow).setShooter(getLivingEntity());
                    ((Arrow) arrow).setPickupStatus(Arrow.PickupStatus.DISALLOWED);
                    if (type.getItemMeta() instanceof PotionMeta) {
                        PotionData data = ((PotionMeta) type.getItemMeta()).getBasePotionData();
                        if (data.getType() == null || data.getType() == PotionType.UNCRAFTABLE) {
                            if (SentinelPlugin.debugMe) {
                                sentinel.debug("Potion data '" + data + "' for '" + type.toString() + "' is invalid.");
                            }
                        }
                        else {
                            ((Arrow) arrow).setBasePotionData(data);
                            for (PotionEffect effect : ((PotionMeta) type.getItemMeta()).getCustomEffects()) {
                                ((Arrow) arrow).addCustomEffect(effect, true);
                            }
                        }
                    }
                }
            }
            else {
                arrow = launchStart.getWorld().spawnEntity(launchStart,
                        type.getType() == Material.SPECTRAL_ARROW ? EntityType.SPECTRAL_ARROW :
                                (type.getType() == Material.TIPPED_ARROW ? TIPPED_ARROW : EntityType.ARROW));
                arrow.setVelocity(velocity);
                ((Projectile) arrow).setShooter(getLivingEntity());
            }
        }
        else {
            arrow = launchStart.getWorld().spawnEntity(launchStart, EntityType.ARROW);
            ((Projectile) arrow).setShooter(getLivingEntity());
            arrow.setVelocity(velocity);
        }
        if (sentinel.itemHelper.getHeldItem().containsEnchantment(Enchantment.ARROW_FIRE)) {
            arrow.setFireTicks(10000);
        }
        sentinel.useItem();
    }

    /**
     * Fires a snowball from the NPC at a target.
     */
    public void fireSnowball(Location target) {
        sentinel.swingWeapon();
        sentinel.stats_snowballsThrown++;
        sentinel.faceLocation(target);
        Vector forward = getLivingEntity().getEyeLocation().getDirection();
        Location spawnAt = getLivingEntity().getEyeLocation().clone().add(forward.clone().multiply(sentinel.firingMinimumRange()));
        Entity ent = spawnAt.getWorld().spawnEntity(spawnAt, EntityType.SNOWBALL);
        ((Projectile) ent).setShooter(getLivingEntity());
        ent.setVelocity(sentinel.fixForAcc(target.clone().subtract(spawnAt).toVector().normalize().multiply(2.0))); // TODO: Fiddle with '2.0'.
    }

    /**
     * Fires a trident from the NPC at a target.
     */
    public void fireTrident(Location target) {
        if (!SentinelVersionCompat.v1_13) {
            return;
        }
        sentinel.swingWeapon();
        sentinel.stats_arrowsFired++;
        sentinel.faceLocation(target);
        Vector forward = getLivingEntity().getEyeLocation().getDirection();
        Location spawnAt = getLivingEntity().getEyeLocation().clone().add(forward.clone().multiply(sentinel.firingMinimumRange() + 2));
        Trident ent = (Trident) spawnAt.getWorld().spawnEntity(spawnAt, EntityType.TRIDENT);
        if (SentinelVersionCompat.v1_14) {
            ent.setPickupStatus(AbstractArrow.PickupStatus.DISALLOWED);
        }
        ent.setShooter(getLivingEntity());
        ent.setVelocity(sentinel.fixForAcc(target.clone().subtract(spawnAt).toVector().normalize().multiply(2.0))); // TODO: Fiddle with '2.0'.
    }

    /**
     * Fires an egg from the NPC at a target.
     */
    public void fireEgg(Location target) {
        sentinel.swingWeapon();
        sentinel.stats_eggsThrown++;
        sentinel.faceLocation(target);
        Vector forward = getLivingEntity().getEyeLocation().getDirection();
        Location spawnAt = getLivingEntity().getEyeLocation().clone().add(forward.clone().multiply(sentinel.firingMinimumRange()));
        Entity ent = spawnAt.getWorld().spawnEntity(spawnAt, EntityType.EGG);
        ((Projectile) ent).setShooter(getLivingEntity());
        ent.setVelocity(sentinel.fixForAcc(target.clone().subtract(spawnAt).toVector().normalize().multiply(2.0))); // TODO: Fiddle with '2.0'.
    }

    /**
     * Fires a pearl from the NPC at a target.
     */
    public void firePearl(LivingEntity target) {
        sentinel.swingWeapon();
        sentinel.faceLocation(target.getEyeLocation());
        sentinel.stats_pearlsUsed++;
        if(SentinelPlugin.instance.enableMagicPearls) {
            // TODO: Maybe require entity is-on-ground?
            target.setVelocity(target.getVelocity().add(new Vector(0, sentinel.getDamage(true), 0)));
        } else {
            getLivingEntity().launchProjectile(EnderPearl.class);
        }
    }

    /**
     * Fires a fireballs from the NPC at a target.
     */
    public void fireFireball(Location target) {
        sentinel.swingWeapon();
        sentinel.stats_fireballsFired++;
        sentinel.faceLocation(target);
        Vector forward = getLivingEntity().getEyeLocation().getDirection();
        Location spawnAt = getLivingEntity().getEyeLocation().clone().add(forward.clone().multiply(sentinel.firingMinimumRange()));
        Entity ent = spawnAt.getWorld().spawnEntity(spawnAt, EntityType.SMALL_FIREBALL);
        ((Projectile) ent).setShooter(getLivingEntity());
        ent.setVelocity(sentinel.fixForAcc(target.clone().subtract(spawnAt).toVector().normalize().multiply(4))); // TODO: Fiddle with '4'.
    }

    /**
     * Fires a skull from the NPC at a target.
     */
    public void fireSkull(Location target) {
        sentinel.swingWeapon();
        sentinel.stats_skullsThrown++;
        sentinel.faceLocation(target);
        Vector forward = getLivingEntity().getEyeLocation().getDirection();
        Location spawnAt = getLivingEntity().getEyeLocation().clone().add(forward.clone().multiply(sentinel.firingMinimumRange()));
        Entity ent = spawnAt.getWorld().spawnEntity(spawnAt, EntityType.WITHER_SKULL);
        ((Projectile) ent).setShooter(getLivingEntity());
        ent.setVelocity(sentinel.fixForAcc(target.clone().subtract(spawnAt).toVector().normalize().multiply(4))); // TODO: Fiddle with '4'.
    }

    /**
     * Makes an NPC punch a target.
     */
    public void punch(LivingEntity entity) {
        sentinel.faceLocation(entity.getEyeLocation());
        sentinel.swingWeapon();
        sentinel.stats_punches++;
        if (SentinelPlugin.instance.workaroundDamage) {
            if (SentinelPlugin.debugMe) {
                debug("workaround damage value at " + sentinel.getDamage(false) + " yields "
                        + ((sentinel.getDamage(false) * (1.0 - sentinel.getArmor(entity)))));
            }
            entity.damage(sentinel.getDamage(false) * (1.0 - sentinel.getArmor(entity)));
            knockback(entity);
            if (!sentinel.enemyDrops) {
                sentinel.needsDropsClear.add(entity.getUniqueId());
            }
        }
        else {
            if (SentinelPlugin.debugMe) {
                debug("Punch/natural for " + sentinel.getDamage(false));
            }
            entity.damage(sentinel.getDamage(false), getLivingEntity());
        }
    }

    /**
     * Knocks a target back from damage received (for hacked-in damage applications when required by config).
     */
    public void knockback(LivingEntity entity) {
        Vector relative = entity.getLocation().toVector().subtract(getLivingEntity().getLocation().toVector());
        if (relative.lengthSquared() > 0) {
            relative = relative.normalize();
        }
        relative.setY(0.75);
        relative.multiply(0.5 / Math.max(1.0, entity.getVelocity().length()));
        entity.setVelocity(entity.getVelocity().multiply(0.25).add(relative));
        if (SentinelPlugin.debugMe) {
            debug("applied knockback velocity adder of " + relative);
        }
    }
}
