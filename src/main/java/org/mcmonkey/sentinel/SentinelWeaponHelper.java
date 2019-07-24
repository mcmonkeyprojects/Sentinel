package org.mcmonkey.sentinel;

import net.citizensnpcs.api.trait.trait.Inventory;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionType;
import org.bukkit.util.Vector;
import org.mcmonkey.sentinel.targeting.SentinelTarget;

import java.util.HashMap;

/**
 * Helper for weapon management, particular attacking.
 */
public class SentinelWeaponHelper extends SentinelHelperObject {

    public static final EntityType LINGERING_POTION, TIPPED_ARROW;

    static {
        if (SentinelTarget.v1_14) {
            LINGERING_POTION = EntityType.SPLASH_POTION;
            TIPPED_ARROW = EntityType.ARROW;
        }
        else if (SentinelTarget.v1_9) {
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
        if (SentinelTarget.v1_14) {
            entpotion = start.getKey().getWorld().spawnEntity(start.getKey(), EntityType.SPLASH_POTION);
        }
        else if (SentinelTarget.v1_9) {
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
        HashMap.SimpleEntry<Location, Vector> start = sentinel.getLaunchDetail(target, lead);
        if (start == null || start.getKey() == null) {
            return;
        }
        sentinel.stats_arrowsFired++;
        Entity arrow;
        if (SentinelTarget.v1_9) {
            if (SentinelTarget.v1_14) {
                Class toShoot;
                toShoot = type.getType() == Material.SPECTRAL_ARROW ? SpectralArrow.class :
                        (type.getType() == Material.TIPPED_ARROW ? TippedArrow.class : Arrow.class);
                Vector dir = sentinel.fixForAcc(start.getValue());
                double length = Math.max(1.0, dir.length());
                arrow = start.getKey().getWorld().spawnArrow(start.getKey(), dir.multiply(1.0 / length), (float) length, 0f, toShoot);
                ((Projectile) arrow).setShooter(getLivingEntity());
                ((Arrow) arrow).setPickupStatus(Arrow.PickupStatus.DISALLOWED);
            }
            else {
                arrow = start.getKey().getWorld().spawnEntity(start.getKey(),
                        type.getType() == Material.SPECTRAL_ARROW ? EntityType.SPECTRAL_ARROW :
                                (type.getType() == Material.TIPPED_ARROW ? TIPPED_ARROW : EntityType.ARROW));
                arrow.setVelocity(sentinel.fixForAcc(start.getValue()));
                ((Projectile) arrow).setShooter(getLivingEntity());
            }
            if (arrow instanceof TippedArrow && type.getItemMeta() instanceof PotionMeta) {
                PotionData data = ((PotionMeta) type.getItemMeta()).getBasePotionData();
                if (data.getType() == null || data.getType() == PotionType.UNCRAFTABLE) {
                    // TODO: Perhaps a **single** warning?
                }
                else {
                    ((TippedArrow) arrow).setBasePotionData(data);
                    for (PotionEffect effect : ((PotionMeta) type.getItemMeta()).getCustomEffects()) {
                        ((TippedArrow) arrow).addCustomEffect(effect, true);
                    }
                }
            }
        }
        else {
            arrow = start.getKey().getWorld().spawnEntity(start.getKey(), EntityType.ARROW);
            ((Projectile) arrow).setShooter(getLivingEntity());
            arrow.setVelocity(sentinel.fixForAcc(start.getValue()));
        }
        if (getNPC().getTrait(Inventory.class).getContents()[0].containsEnchantment(Enchantment.ARROW_FIRE)) {
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
        if (!SentinelTarget.v1_13) {
            return;
        }
        sentinel.swingWeapon();
        sentinel.stats_arrowsFired++;
        sentinel.faceLocation(target);
        Vector forward = getLivingEntity().getEyeLocation().getDirection();
        Location spawnAt = getLivingEntity().getEyeLocation().clone().add(forward.clone().multiply(sentinel.firingMinimumRange() + 2));
        Trident ent = (Trident) spawnAt.getWorld().spawnEntity(spawnAt, EntityType.TRIDENT);
        if (SentinelTarget.v1_14) {
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
        // TODO: Maybe require entity is-on-ground?
        sentinel.stats_pearlsUsed++;
        target.setVelocity(target.getVelocity().add(new Vector(0, sentinel.getDamage(), 0)));
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
        sentinel.faceLocation(entity.getLocation());
        sentinel.swingWeapon();
        sentinel.stats_punches++;
        if (SentinelPlugin.instance.workaroundDamage) {
            if (SentinelPlugin.debugMe) {
                debug("workaround damage value at " + sentinel.getDamage() + " yields "
                        + ((sentinel.getDamage() * (1.0 - sentinel.getArmor(entity)))));
            }
            entity.damage(sentinel.getDamage() * (1.0 - sentinel.getArmor(entity)));
            knockback(entity);
            if (!sentinel.enemyDrops) {
                sentinel.needsDropsClear.add(entity.getUniqueId());
            }
        }
        else {
            if (SentinelPlugin.debugMe) {
                debug("Punch/natural for " + sentinel.getDamage());
            }
            entity.damage(sentinel.getDamage(), getLivingEntity());
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
