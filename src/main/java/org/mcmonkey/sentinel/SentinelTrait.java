package org.mcmonkey.sentinel;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.ai.TeleportStuckAction;
import net.citizensnpcs.api.ai.speech.SpeechContext;
import net.citizensnpcs.api.event.DespawnReason;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.persistence.PersistenceLoader;
import net.citizensnpcs.api.persistence.Persister;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.trait.Spawned;
import net.citizensnpcs.api.util.DataKey;
import net.citizensnpcs.trait.CurrentLocation;
import net.citizensnpcs.trait.waypoint.Waypoint;
import net.citizensnpcs.trait.waypoint.WaypointProvider;
import net.citizensnpcs.trait.waypoint.Waypoints;
import net.citizensnpcs.util.NMS;
import net.citizensnpcs.util.PlayerAnimation;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.mcmonkey.sentinel.targeting.SentinelTarget;
import org.mcmonkey.sentinel.targeting.SentinelTargetList;
import org.mcmonkey.sentinel.targeting.SentinelTargetingHelper;

import java.util.*;

/**
 * The main Sentinel trait.
 */
public class SentinelTrait extends Trait {

    /**
     * Constant: the smallest health value that can be given to an NPC.
     */
    public static final double healthMin = 0.01;

    /**
     * Constant: the maximum attack rate value (in ticks).
     */
    public static final int attackRateMax = 2000;

    /**
     * Constant: the maximum heal rate value (in ticks).
     */
    public static final int healRateMax = 2000;

    /**
     * Helper for targeting logic.
     */
    public SentinelTargetingHelper targetingHelper;

    /**
     * Helper for items.
     */
    public SentinelItemHelper itemHelper;

    /**
     * Helper for weapons.
     */
    public SentinelWeaponHelper weaponHelper;

    /**
     * Helper for attacking.
     */
    public SentinelAttackHelper attackHelper;

    /**
     * Constructs the Sentinel Trait object - should only be called by the Citizens API internal functionality.
     * To add Sentinel to an NPC, use {@code npc.addTrait(SentinelTrait.class)}.
     */
    public SentinelTrait() {
        super("sentinel");
        targetingHelper = new SentinelTargetingHelper();
        itemHelper = new SentinelItemHelper();
        weaponHelper = new SentinelWeaponHelper();
        attackHelper = new SentinelAttackHelper();
        targetingHelper.setTraitObject(this);
        itemHelper.setTraitObject(this);
        weaponHelper.setTraitObject(this);
        attackHelper.setTraitObject(this);
    }

    /**
     * Statistics value: how long (in ticks) this NPC has ever been in the world, in total.
     */
    @Persist("stats_ticksSpawned")
    public long stats_ticksSpawned = 0;

    /**
     * Statistics value: how many times this NPC has spawned into the world.
     */
    @Persist("stats_timesSpawned")
    public long stats_timesSpawned = 0;

    /**
     * Statistics value: how many arrows this NPC has fired.
     */
    @Persist("stats_arrowsFired")
    public long stats_arrowsFired = 0;

    /**
     * Statistics value: how many potions this NPC has thrown.
     */
    @Persist("stats_potionsThrow")
    public long stats_potionsThrown = 0;

    /**
     * Statistics value: how many fireballs this NPC has fired.
     */
    @Persist("stats_fireballsFired")
    public long stats_fireballsFired = 0;

    /**
     * Statistics value: how many snowballs this NPC has thrown.
     */
    @Persist("stats_snowballsThrown")
    public long stats_snowballsThrown = 0;

    /**
     * Statistics value: how many eggs this NPC has thrown.
     */
    @Persist("stats_eggsThrown")
    public long stats_eggsThrown = 0;

    /**
     * Statistics value: how many skulls this NPC has thrown.
     */
    @Persist("stats_skullsThrown")
    public long stats_skullsThrown = 0;

    /**
     * Statistics value: how many pearls this NPC has used.
     */
    @Persist("stats_pearlsUsed")
    public long stats_pearlsUsed = 0;

    /**
     * Statistics value: how many times this NPC has punched a target.
     */
    @Persist("stats_punches")
    public long stats_punches = 0;

    /**
     * Statistics value: how many attacks this NPC has attempted.
     */
    @Persist("stats_attackAttempts")
    public long stats_attackAttempts = 0;

    /**
     * Statistics value: how much damage this NPC has taken.
     */
    @Persist("stats_damageTaken")
    public double stats_damageTaken = 0;

    /**
     * Statistics value: how much damage this NPC has given.
     */
    @Persist("stats_damageGiven")
    public double stats_damageGiven = 0;

    /**
     * Updater for older Sentinel saves (up to 1.7.2)
     */
    @Override
    public void load(final DataKey key) {
        new BukkitRunnable() {
            @Override
            public void run() {
                updateOld(key);
            }
        }.runTaskLater(SentinelPlugin.instance, 1);
    }

    @Override
    public void save(DataKey key) {
        for (DataKey subkey : key.getSubKeys()) {
            if (subkey.name().equals("targets")
                    || subkey.name().equals("ignores")
                    || subkey.name().endsWith("Targets")
                    || subkey.name().endsWith("Ignores")) {
                key.removeKey(subkey.name());
            }
        }
    }

    /**
     * Updater for older Sentinel saves (up to 1.7.2)
     */
    public void updateOld(DataKey key) {
        for (DataKey subkey : key.getSubKeys()) {
            if (subkey.name().equals("targets")) {
                for (DataKey listEntry : subkey.getSubKeys()) {
                    allTargets.targets.add(listEntry.getRaw("").toString());
                }
                allTargets.recalculateTargetsCache();
            }
            else if (subkey.name().equals("ignores")) {
                for (DataKey listEntry : subkey.getSubKeys()) {
                    allIgnores.targets.add(listEntry.getRaw("").toString());
                }
                allIgnores.recalculateTargetsCache();
            }
            if (subkey.name().endsWith("Targets")) {
                allTargets.updateOld(subkey, subkey.name().substring(0, subkey.name().length() - "Targets".length()));
            }
            else if (subkey.name().endsWith("Ignores")) {
                allIgnores.updateOld(subkey, subkey.name().substring(0, subkey.name().length() - "Ignores".length()));
            }
        }
    }

    @Persist("allTargets")
    public SentinelTargetList allTargets = new SentinelTargetList();

    @Persist("allIgnores")
    public SentinelTargetList allIgnores = new SentinelTargetList();

    @Persist("allAvoids")
    public SentinelTargetList allAvoids = new SentinelTargetList();

    public static class SentinelTargetListPersister implements Persister<SentinelTargetList> {
        @Override
        public SentinelTargetList create(DataKey dataKey) {
            return PersistenceLoader.load(new SentinelTargetList(), dataKey);
        }

        @Override
        public void save(SentinelTargetList o, DataKey dataKey) {
            PersistenceLoader.save(o, dataKey);
        }
    }

    static {
        PersistenceLoader.registerPersistDelegate(SentinelTargetList.class, SentinelTargetListPersister.class);
    }

    /**
     * How far to stay from avoid targets.
     */
    @Persist
    public double avoidRange = 10.0;

    /**
     * Maximum range to trigger attacks from.
     */
    @Persist("range")
    public double range = 20.0;

    /**
     * The NPC's damage value (-1 means automatically calculated from weapon, anything else is equal to the HP lost by an unarmored target).
     */
    @Persist("damage")
    public double damage = -1.0;

    /**
     * The NPC's armor value (-1 means automatically calculated from equipment, 0 means no armor and 1 means invincible armor... decimals between 0 and 1 are normal).
     */
    @Persist("armor")
    public double armor = -1.0;

    /**
     * The NPC's maximum health (NOT its current health when the NPC is spawned and injured).
     */
    @Persist("health")
    public double health = 20.0;

    /**
     * Whether the NPC chases targets when using ranged weapons.
     */
    @Persist("ranged_chase")
    public boolean rangedChase = false;

    /**
     * Whether the NPC chases targets when using melee weapons.
     */
    @Persist("close_chase")
    public boolean closeChase = true;

    /**
     * Whether the NPC cannot be harmed (true = no harm, false = receives damage normally).
     */
    @Persist("invincible")
    public boolean invincible = false;

    /**
     * Whether the NPC "fights back" against attacks (targets anyone that damages it).
     */
    @Persist("fightback")
    public boolean fightback = true;

    /**
     * Whether the NPC runs away when attacked.
     */
    @Persist("runaway")
    public boolean runaway = false;

    /**
     * How long (in ticks) between using melee attacks.
     */
    @Persist("attackRate")
    public int attackRate = 30;

    /**
     * How long (in ticks) between firing ranged shots.
     */
    @Persist("attackRateRanged")
    public int attackRateRanged = 30;

    /**
     * How long (in ticks) before the NPC heals by 1 HP (when damaged).
     */
    @Persist("healRate")
    public int healRate = 30;

    /**
     * Upper 64 bits of the guarded player's UUID.
     */
    @Persist("guardingUpper")
    public long guardingUpper = 0;

    /**
     * Lower 64 bits of the guarded player's UUID.
     */
    @Persist("guardingLower")
    public long guardingLower = 0;

    /**
     * Whether the NPC needs ammo to fire ranged weapons (otherwise, infinite ammo).
     */
    @Persist("needsAmmo")
    public boolean needsAmmo = false;

    /**
     * Whether to protect NPC arrow shots from damaging targets that weren't meant to be hit.
     */
    @Persist("safeShot")
    public boolean safeShot = true;

    /**
     * How long (in ticks) after death before the NPC respawns.
     */
    @Persist("respawnTime")
    public long respawnTime = 100;

    /**
     * The maximum distance from a guard point the NPC can run (when chasing a target).
     */
    @Persist("chaseRange")
    public double chaseRange = 100;

    /**
     * The NPC's respawn location (null = respawn where the NPC died at).
     */
    @Persist("spawnPoint")
    public Location spawnPoint = null;

    /**
     * What the NPC drops when dead.
     */
    @Persist("drops")
    public ArrayList<ItemStack> drops = new ArrayList<>();

    /**
     * Whether mob targets killed by the NPC can drop items.
     */
    @Persist("enemyDrops")
    public boolean enemyDrops = false;

    /**
     * How long (in ticks) to retain an enemy target when out-of-view.
     */
    @Persist("enemyTargetTime")
    public long enemyTargetTime = 0;

    /**
     * How fast the NPC moves when chasing (1 = normal speed).
     */
    @Persist("speed")
    public double speed = 1;

    /**
     * The text to warn enemy players with (empty string = no greeting).
     */
    @Persist("warning_text")
    public String warningText = "";

    /**
     * The text to greet friendly players with (empty string = no greeting).
     */
    @Persist("greeting_text")
    public String greetingText = "";

    /**
     * The range this NPC gives greetings or warnings at.
     */
    @Persist("greet_range")
    public double greetRange = 10;

    /**
     * Whether this NPC automatically switches weapons.
     */
    @Persist("autoswitch")
    public boolean autoswitch = false;

    /**
     * The name of the squad this NPC is in (null for no squad).
     */
    @Persist("squad")
    public String squad = null;

    /**
     * The NPC's accuracy value (0 = perfectly accurate).
     */
    @Persist("accuracy")
    public double accuracy = 0;

    /**
     * Whether this NPC should have 'realistic' targeting.
     */
    @Persist("realistic")
    public boolean realistic = false;

    /**
     * How far this NPC's punches can reach.
     */
    @Persist("reach")
    public double reach = 3;

    /**
     * The target entity this NPC is chasing (if any).
     */
    public LivingEntity chasing = null;

    /**
     * Gets the UUID of the player this Sentinel is set to be guarding.
     * Null indicates not guarding anyone.
     */
    public UUID getGuarding() {
        if (guardingLower == 0 && guardingUpper == 0) {
            return null;
        }
        return new UUID(guardingUpper, guardingLower);
    }

    /**
     * Sets the NPC to be guarding a player.
     * Null indicates not guarding anyone.
     */
    public void setGuarding(UUID uuid) {
        if (uuid == null) {
            guardingUpper = 0;
            guardingLower = 0;
        }
        else {
            guardingUpper = uuid.getMostSignificantBits();
            guardingLower = uuid.getLeastSignificantBits();
        }
    }

    /**
     * Internally tracks whether the damage enforcement system can be used (protects against infinite loops).
     */
    private boolean canEnforce = false;

    /**
     * Called when combat occurs in the world (and has not yet been processed by other plugins),
     * to handle things like cancelling invalid damage to/from a Sentinel NPC,
     * changing damage values given to or received from an NPC,
     * and if relevant handling config options that require overriding damage events.
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void whenAttacksAreHappening(EntityDamageByEntityEvent event) {
        if (!npc.isSpawned()) {
            return;
        }
        if (event.isCancelled()) {
            return;
        }
        if (event.getEntity().getUniqueId().equals(getLivingEntity().getUniqueId())) {
            if (!event.isApplicable(EntityDamageEvent.DamageModifier.ARMOR)) {
                event.setDamage(EntityDamageEvent.DamageModifier.BASE, (1.0 - getArmor(getLivingEntity())) * event.getDamage(EntityDamageEvent.DamageModifier.BASE));
            }
            else {
                event.setDamage(EntityDamageEvent.DamageModifier.ARMOR, -getArmor(getLivingEntity()) * event.getDamage(EntityDamageEvent.DamageModifier.BASE));
            }
            return;
        }
        if (event.getDamager().getUniqueId().equals(getLivingEntity().getUniqueId())) {
            if (SentinelPlugin.instance.alternateDamage) {
                if (canEnforce) {
                    canEnforce = false;
                    whenAttacksHappened(event);
                    if (!event.isCancelled()) {
                        ((LivingEntity) event.getEntity()).damage(event.getFinalDamage());
                        if (event.getEntity() instanceof LivingEntity) {
                            weaponHelper.knockback((LivingEntity) event.getEntity());
                        }
                    }
                    if (SentinelPlugin.debugMe) {
                        debug("enforce damage value to " + event.getFinalDamage());
                    }
                }
                else {
                    if (SentinelPlugin.debugMe) {
                        debug("refuse damage enforcement");
                    }
                }
                event.setDamage(0);
                event.setCancelled(true);
                return;
            }
            event.setDamage(EntityDamageEvent.DamageModifier.BASE, getDamage());
        }
        if (event.getDamager() instanceof Projectile) {
            ProjectileSource source = ((Projectile) event.getDamager()).getShooter();
            if (source instanceof LivingEntity && ((LivingEntity) source).getUniqueId().equals(getLivingEntity().getUniqueId())) {
                if (SentinelPlugin.instance.alternateDamage) {
                    if (canEnforce) {
                        canEnforce = false;
                        whenAttacksHappened(event);
                        if (!event.isCancelled()) {
                            ((LivingEntity) event.getEntity()).damage(getDamage());
                            if (event.getEntity() instanceof LivingEntity) {
                                weaponHelper.knockback((LivingEntity) event.getEntity());
                            }
                        }
                        if (SentinelPlugin.debugMe) {
                            debug("enforce damage value to " + getDamage());
                        }
                    }
                    else {
                        if (SentinelPlugin.debugMe) {
                            debug("refuse damage enforcement");
                        }
                    }
                    event.setDamage(0);
                    event.setCancelled(true);
                    return;
                }
                double dam = getDamage();
                double modder = event.getDamage(EntityDamageEvent.DamageModifier.BASE);
                double rel = modder == 0.0 ? 1.0 : dam / modder;
                event.setDamage(EntityDamageEvent.DamageModifier.BASE, dam);
                for (EntityDamageEvent.DamageModifier mod : EntityDamageEvent.DamageModifier.values()) {
                    if (mod != EntityDamageEvent.DamageModifier.BASE && event.isApplicable(mod)) {
                        event.setDamage(mod, event.getDamage(mod) * rel);
                        if (SentinelPlugin.debugMe) {
                            debug("Set damage for " + mod + " to " + event.getDamage(mod));
                        }
                    }
                }
            }
        }
    }

    /**
     * Called when combat has occurred in the world (and has been processed by all other plugins), to handle things like cancelling invalid damage to/from a Sentinel NPC,
     * adding targets (if combat occurs near an NPC), and if relevant handling config options that require overriding damage events.
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void whenAttacksHappened(EntityDamageByEntityEvent event) {
        if (!npc.isSpawned()) {
            return;
        }
        if (event.isCancelled()) {
            return;
        }
        Entity damager = event.getDamager();
        LivingEntity projectileSource = null;
        if (event.getDamager() instanceof Projectile) {
            ProjectileSource source = ((Projectile) event.getDamager()).getShooter();
            if (source instanceof LivingEntity) {
                projectileSource = (LivingEntity) source;
                damager = projectileSource;
            }
        }
        boolean isMe = event.getEntity().getUniqueId().equals(getLivingEntity().getUniqueId());
        if (SentinelPlugin.instance.protectFromIgnores && isMe) {
            if (event.getDamager() instanceof LivingEntity && targetingHelper.isIgnored((LivingEntity) event.getDamager())) {
                event.setCancelled(true);
                return;
            }
            else if (projectileSource != null && targetingHelper.isIgnored(projectileSource)) {
                event.setCancelled(true);
                return;
            }
        }
        boolean isKilling = event.getEntity() instanceof LivingEntity && event.getFinalDamage() >= ((LivingEntity) event.getEntity()).getHealth();
        boolean isFriend = getGuarding() != null && event.getEntity().getUniqueId().equals(getGuarding());
        boolean attackerIsMe = event.getDamager().getUniqueId().equals(getLivingEntity().getUniqueId());
        if (projectileSource != null && projectileSource.getUniqueId().equals(getLivingEntity().getUniqueId())) {
            attackerIsMe = true;
        }
        if (isMe || isFriend) {
            if (attackerIsMe) {
                if (SentinelPlugin.debugMe) {
                    debug("Ignoring damage I did to " + (isMe ? "myself." : "my friend."));
                }
                event.setCancelled(true);
                return;
            }
            if (getGuarding() != null && damager.getUniqueId().equals(getGuarding())) {
                if (isMe) {
                    if (SentinelPlugin.debugMe) {
                        debug("Ignoring damage from the player we're guarding.");
                    }
                    event.setCancelled(true);
                }
                return;
            }
            if (isMe) {
                stats_damageTaken += event.getFinalDamage();
                if (runaway) {
                    if (SentinelPlugin.debugMe) {
                        debug("Ow! They hit me! Run!");
                    }
                    targetingHelper.addAvoid(event.getDamager().getUniqueId());
                }
            }
            if (fightback && (damager instanceof LivingEntity) && !targetingHelper.isIgnored((LivingEntity) damager)) {
                if (SentinelPlugin.debugMe) {
                    debug("Fighting back against attacker: " + event.getDamager().getUniqueId() + "! They hurt " + (isMe ? "me!" : "my friend!"));
                }
                targetingHelper.addTarget(event.getDamager().getUniqueId());
            }
            if (SentinelPlugin.debugMe && isMe) {
                debug("Took damage of " + event.getFinalDamage() + " with currently remaining health " + getLivingEntity().getHealth()
                    + (isKilling ? ". This will kill me." : "."));
            }
            if (isKilling && isMe && SentinelPlugin.instance.blockEvents) {
                // We're going to die but we've been requested to avoid letting a death event fire.
                // The solution at this point is to fake our death: remove the NPC entity from existence and trigger our internal death handling sequence.
                // We also mark the damage event cancelled to reduce trouble (ensure event doesn't try to apply damage to our now-gone entity).
                if (SentinelPlugin.debugMe) {
                    debug("Died! Applying death workaround (due to config setting)");
                }
                generalDeathHandler(getLivingEntity());
                npc.despawn(DespawnReason.PLUGIN);
                event.setCancelled(true);
                return;
            }
            return;
        }
        if (attackerIsMe) {
            if (safeShot && !targetingHelper.shouldTarget((LivingEntity) event.getEntity())) {
                event.setCancelled(true);
                return;
            }
            stats_damageGiven += event.getFinalDamage();
            if (!enemyDrops) {
                needsDropsClear.put(event.getEntity().getUniqueId(), true);
            }
            return;
        }
        if (allTargets.isEventTarget(event) && damager instanceof LivingEntity
                && targetingHelper.canSee((LivingEntity) damager) && !targetingHelper.isIgnored((LivingEntity) damager)) {
            targetingHelper.addTarget(damager.getUniqueId());
        }
        if (allAvoids.isEventTarget(event) && damager instanceof LivingEntity
                && targetingHelper.canSee((LivingEntity) damager) && !targetingHelper.isIgnored((LivingEntity) damager)) {
            targetingHelper.addTarget(damager.getUniqueId());
        }
    }

    private SentinelCurrentTarget tempTarget = new SentinelCurrentTarget();

    /**
     * Called when a target dies to remove them from the target list.
     */
    @EventHandler
    public void whenAnEnemyDies(EntityDeathEvent event) {
        tempTarget.targetID = event.getEntity().getUniqueId();
        targetingHelper.currentTargets.remove(tempTarget);
        targetingHelper.currentAvoids.remove(tempTarget);
    }

    /**
     * Called when the Sentinel trait is attached to the NPC - fills all default values from config.
     */
    @Override
    public void onAttach() {
        FileConfiguration config = SentinelPlugin.instance.getConfig();
        attackRate = config.getInt("sentinel defaults.attack rate", 30);
        healRate = config.getInt("sentinel defaults.heal rate", 30);
        respawnTime = config.getInt("sentinel defaults.respawn time", 100);
        rangedChase = config.getBoolean("sentinel defaults.ranged chase target", false);
        closeChase = config.getBoolean("sentinel defaults.close chase target", true);
        armor = config.getDouble("sentinel defaults.armor", -1);
        damage = config.getDouble("sentinel defaults.damage", -1);
        health = config.getDouble("sentinel defaults.health", 20);
        if (npc.isSpawned()) {
            getLivingEntity().setMaxHealth(health);
            getLivingEntity().setHealth(health);
        }
        setInvincible(config.getBoolean("sentinel defaults.invincible", false));
        fightback = config.getBoolean("sentinel defaults.fightback", true);
        needsAmmo = config.getBoolean("sentinel defaults.needs ammo", false);
        safeShot = config.getBoolean("sentinel defaults.safe shot", true);
        enemyDrops = config.getBoolean("sentinel defaults.enemy drops", false);
        enemyTargetTime = config.getInt("sentinel defaults.enemy target time", 0);
        speed = config.getInt("sentinel defaults.speed", 1);
        if (speed <= 0) {
            speed = 1;
        }
        autoswitch = config.getBoolean("sentinel defaults.autoswitch", false);
        allIgnores.targets.add(SentinelTarget.OWNER.name());
        allIgnores.recalculateTargetsCache();
        reach = config.getDouble("sentinel defaults.reach", 3);
        avoidRange = config.getDouble("sentinel defaults.avoid range", 10);
        runaway = config.getBoolean("sentinel defaults.runaway", false);
    }

    /**
     * Animates the NPC using their item, and stops the animation 10 ticks later (useful for replicating bow draws, etc).
     */
    public void useItem() {
        if (npc.isSpawned() && getLivingEntity() instanceof Player) {
            if (SentinelTarget.v1_9) {
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
    }

    /**
     * Swings the NPC's weapon (plays an ARM_SWING animation if possible - otherwise, does nothing).
     */
    public void swingWeapon() {
        if (npc.isSpawned() && getLivingEntity() instanceof Player) {
            PlayerAnimation.ARM_SWING.play((Player) getLivingEntity());
        }
    }

    /**
     * Gets the minimum distance from the NPC's head to launch a projectile from (to avoid it colliding with the NPC's own collision box).
     */
    public double firingMinimumRange() {
        EntityType type = getLivingEntity().getType();
        if (type == EntityType.WITHER || type == EntityType.GHAST) {
            return 8; // Yikes!
        }
        return 2;
    }

    /**
     * Gets a 'launch detail' (starting location for the projectile position, and a vector holding the exact launch vector, scaled to the correct speed).
     */
    public HashMap.SimpleEntry<Location, Vector> getLaunchDetail(Location target, Vector lead) {
        faceLocation(target);
        Location start = getLivingEntity().getEyeLocation().clone().add(getLivingEntity().getEyeLocation().getDirection().multiply(firingMinimumRange()));
        return SentinelUtilities.getLaunchDetail(start, target, lead);
    }

    /**
     * Returns a random decimal number within acceptable accuracy range (can be negative).
     */
    public double randomAcc() {
        return SentinelUtilities.random.nextDouble() * accuracy * 2 - accuracy;
    }

    /**
     * Alters a vector per accuracy potential (makes the vector less accurate).
     */
    public Vector fixForAcc(Vector input) {
        if (Double.isInfinite(input.getX()) || Double.isNaN(input.getX())) {
            return new Vector(0, 0, 0);
        }
        return new Vector(input.getX() + randomAcc(), input.getY() + randomAcc(), input.getZ() + randomAcc());
    }

    /**
     * Rotates an NPC to face a target location.
     */
    public void faceLocation(Location l) {
        npc.faceLocation(l.clone().subtract(0, getLivingEntity().getEyeHeight(), 0));
    }

    /**
     * Gets the NPC's current damage value (based on held weapon if calculation is required).
     */
    public double getDamage() {
        if (damage >= 0) {
            return damage;
        }
        ItemStack weapon = itemHelper.getHeldItem();
        if (weapon == null) {
            return 1;
        }
        // TODO: Less randomness, more game-like calculations.
        double multiplier = 1;
        multiplier += weapon.getItemMeta() == null || !weapon.getItemMeta().hasEnchant(Enchantment.DAMAGE_ALL)
                ? 0 : weapon.getItemMeta().getEnchantLevel(Enchantment.DAMAGE_ALL) * 0.2;
        Material weaponType = weapon.getType();
        if (SentinelTarget.BOW_MATERIALS.contains(weaponType)) {
            return 6 * (1 + (weapon.getItemMeta() == null || !weapon.getItemMeta().hasEnchant(Enchantment.ARROW_DAMAGE)
                    ? 0 : weapon.getItemMeta().getEnchantLevel(Enchantment.ARROW_DAMAGE) * 0.3));
        }
        Double damageMult = SentinelTarget.WEAPON_DAMAGE_MULTIPLIERS.get(weaponType);
        if (damageMult == null) {
            return multiplier;
        }
        return multiplier * damageMult;
    }

    /**
     * Gets the NPC's current armor value (based on worn armor if calculation is required).
     */
    public double getArmor(LivingEntity ent) {
        if (armor < 0) {
            // TODO: Enchantments!
            double baseArmor = 0;
            ItemStack helmet = ent.getEquipment().getHelmet();
            Double helmetAdder = helmet == null ? null : SentinelTarget.ARMOR_PROTECTION_MULTIPLIERS.get(helmet.getType());
            if (helmetAdder != null) {
                baseArmor += helmetAdder;
            }
            ItemStack chestplate = ent.getEquipment().getChestplate();
            Double chestplateAdder = chestplate == null ? null : SentinelTarget.ARMOR_PROTECTION_MULTIPLIERS.get(chestplate.getType());
            if (chestplateAdder != null) {
                baseArmor += chestplateAdder;
            }
            ItemStack leggings = ent.getEquipment().getLeggings();
            Double leggingsAdder = leggings == null ? null : SentinelTarget.ARMOR_PROTECTION_MULTIPLIERS.get(leggings.getType());
            if (leggingsAdder != null) {
                baseArmor += leggingsAdder;
            }
            ItemStack boots = ent.getEquipment().getBoots();
            Double bootsAdder = boots == null ? null : SentinelTarget.ARMOR_PROTECTION_MULTIPLIERS.get(boots.getType());
            if (bootsAdder != null) {
                baseArmor += bootsAdder;
            }
            return Math.min(baseArmor, 0.80);
        }
        return armor;
    }

    /**
     * Gets the living entity for the NPC.
     */
    public LivingEntity getLivingEntity() {
        // Not a good idea to turn a non-living NPC into a Sentinel for now.
        return (LivingEntity) npc.getEntity();
    }

    /**
     * Players in range of the NPC that have already been greeted.
     */
    private HashSet<UUID> greetedAlready = new HashSet<>();

    /**
     * Time since the last attack.
     */
    public long timeSinceAttack = 0;

    /**
     * Time since the last heal.
     */
    public long timeSinceHeal = 0;

    /**
     * Tick counter for use with giving up on targets that can't be seen
     * (to avoid being overly 'clever' and chasing a target the NPC shouldn't be able to locate).
     */
    int cleverTicks = 0;

    /**
     * Whether the NPC has chased a target during the most recent update.
     */
    public boolean chased = false;

    /**
     * Marks that the NPC can see a target (Changes the state of som entity types, eg opening a shulker box).
     */
    public void specialMarkVision() {
        if (SentinelPlugin.debugMe && !visionMarked) {
            debug("Target! I see you, " + (chasing == null ? "(Unknown)" : chasing.getName()));
        }
        if (SentinelTarget.v1_11 && getLivingEntity().getType() == EntityType.SHULKER) {
            NMS.setPeekShulker(getLivingEntity(), 100);
        }
        visionMarked = true;
    }

    private boolean visionMarked;

    /**
     * Marks that the NPC can no longer see a target (Changes the state of som entity types, eg closing a shulker box).
     */
    public void specialUnmarkVision() {
        if (SentinelPlugin.debugMe && visionMarked) {
            debug("Goodbye, visible target " + (chasing == null ? "(Unknown)" : chasing.getName()));
        }
        if (SentinelTarget.v1_11 && getLivingEntity().getType() == EntityType.SHULKER) {
            NMS.setPeekShulker(getLivingEntity(), 0);
        }
        visionMarked = false;
    }

    /**
     * Tick counter for the NPC guarding a player (to avoid updating positions too quickly).
     */
    public int ticksCountGuard = 0;

    /**
     * Set true when waypoints are paused by Sentinel, to indicate that an unpause is needed
     * (to avoid over-doing the unpause call, which can disrupt unrelated unpauses).
     */
    public boolean needsToUnpause = false;

    /**
     * Special case for where the NPC has been forced to run to in certain situations.
     */
    public Location pathingTo = null;

    /**
     * Causes the NPC to immediately path over to a position.
     */
    public void pathTo(Location target) {
        pauseWaypoints();
        pathingTo = target;
        npc.getNavigator().getDefaultParameters().distanceMargin(1.5);
        getNPC().getNavigator().setTarget(target);
        chasing = null;
    }

    /**
     * Pauses waypoint navigation if currrently navigating.
     */
    public void pauseWaypoints() {
        Waypoints wp = npc.getTrait(Waypoints.class);
        if (!wp.getCurrentProvider().isPaused()) {
            wp.getCurrentProvider().setPaused(true);
        }
        needsToUnpause = true;
    }

    /**
     * Runs a full update cycle on the NPC.
     */
    public void runUpdate() {
        // Basic prep and tracking
        canEnforce = true;
        timeSinceAttack += SentinelPlugin.instance.tickRate;
        timeSinceHeal += SentinelPlugin.instance.tickRate;
        // Protection against falling below the world
        if (getLivingEntity().getLocation().getY() <= 0) {
            if (SentinelPlugin.debugMe) {
                debug("Injuring self, I'm below the map!");
            }
            getLivingEntity().damage(1);
            if (!npc.isSpawned()) {
                if (getGuarding() != null && Bukkit.getPlayer(getGuarding()) != null) {
                    if (respawnTime > 0 && respawnMe == null) {
                        npc.spawn(Bukkit.getPlayer(getGuarding()).getLocation());
                    }
                }
                return;
            }
        }
        // Settings enforcement
        if (health != getLivingEntity().getMaxHealth()) {
            getLivingEntity().setMaxHealth(health);
        }
        // Healing
        if (healRate > 0 && timeSinceHeal > healRate && getLivingEntity().getHealth() < health) {
            getLivingEntity().setHealth(Math.min(getLivingEntity().getHealth() + 1.0, health));
            timeSinceHeal = 0;
        }
        // Pathing and waypoints management
        if (!npc.getNavigator().isNavigating()) {
            pathingTo = null;
        }
        if ((getGuarding() != null || chasing != null || pathingTo != null) && npc.hasTrait(Waypoints.class)) {
            pauseWaypoints();
        }
        else if (needsToUnpause && npc.hasTrait(Waypoints.class)) {
            Waypoints wp = npc.getTrait(Waypoints.class);
            wp.getCurrentProvider().setPaused(false);
            needsToUnpause = false;
        }
        // Targets updating
        targetingHelper.updateTargets();
        targetingHelper.updateAvoids();
        double crsq = chaseRange * chaseRange;
        boolean goHome = chased;
        LivingEntity target = targetingHelper.findBestTarget();
        if (target != null) {
            Location near = nearestPathPoint();
            if (SentinelPlugin.debugMe) {
                debug("target selected to be " + target.getName());
            }
            if (crsq <= 0 || near == null || near.distanceSquared(target.getLocation()) <= crsq) {
                if (SentinelPlugin.debugMe) {
                    debug("Attack target within range of safe zone: "
                            + (near == null ? "Any" : near.distanceSquared(target.getLocation())));
                }
                if (chasing == null) {
                    specialMarkVision();
                }
                chasing = target;
                cleverTicks = 0;
                attackHelper.tryAttack(target);
                goHome = false;
            }
            else {
                if (SentinelPlugin.debugMe) {
                    debug("Actually, that target is bad!");
                }
                specialUnmarkVision();
                target = null;
                chasing = null;
                cleverTicks = 0;
            }
        }
        else if (chasing != null && chasing.isValid()) {
            if (SentinelPlugin.instance.workaroundEntityChasePathfinder) {
                attackHelper.rechase();
            }
            cleverTicks++;
            if (cleverTicks >= SentinelPlugin.instance.cleverTicks) {
                specialUnmarkVision();
                chasing = null;
            }
            else {
                Location near = nearestPathPoint();
                if (crsq <= 0 || near == null || near.distanceSquared(chasing.getLocation()) <= crsq) {
                    attackHelper.tryAttack(chasing);
                    goHome = false;
                }
            }
        }
        else if (chasing == null) {
            specialUnmarkVision();
        }
        // Special guarding handling
        if (getGuarding() != null) {
            Player player = Bukkit.getPlayer(getGuarding());
            if (player != null) {
                Location myLoc = getLivingEntity().getLocation();
                Location theirLoc = player.getLocation();
                double dist = theirLoc.getWorld().equals(myLoc.getWorld()) ? myLoc.distanceSquared(theirLoc) : MAX_DIST;
                if (dist > 60 * 60) {
                    npc.teleport(player.getLocation(), PlayerTeleportEvent.TeleportCause.PLUGIN);
                }
                if (dist > SentinelPlugin.instance.guardDistanceMinimum * SentinelPlugin.instance.guardDistanceMinimum) {
                    ticksCountGuard += SentinelPlugin.instance.tickRate;
                    if (ticksCountGuard >= 30) {
                        ticksCountGuard = 0;
                        npc.getNavigator().getDefaultParameters().distanceMargin(SentinelPlugin.instance.guardDistanceMargin);
                        npc.getNavigator().getDefaultParameters().range(100);
                        npc.getNavigator().getDefaultParameters().stuckAction(TeleportStuckAction.INSTANCE);
                        npc.getNavigator().setTarget(SentinelUtilities.pickNear(player.getLocation(), SentinelPlugin.instance.guardDistanceSelectionRange));
                        npc.getNavigator().getLocalParameters().speedModifier((float) speed);
                        chased = true;
                    }
                }
                goHome = false;
            }
        }
        // Avoidance handling
        targetingHelper.processAvoidance();
        if (pathingTo != null) {
            goHome = false;
        }
        // Handling for when NPC has no targets
        if (goHome && chaseRange > 0 && target == null) {
            Location near = nearestPathPoint();
            if (near != null && (chasing == null || near.distanceSquared(chasing.getLocation()) > crsq)) {
                if (SentinelPlugin.debugMe) {
                    if (near.distanceSquared(getLivingEntity().getLocation()) > 3 * 3) {
                        debug("screw you guys, I'm going home!");
                    }
                }
                npc.getNavigator().getDefaultParameters().stuckAction(TeleportStuckAction.INSTANCE);
                npc.getNavigator().setTarget(near);
                npc.getNavigator().getLocalParameters().speedModifier((float) speed);
                chased = false;
            }
            else {
                if (pathingTo == null && npc.getNavigator().isNavigating()) {
                    npc.getNavigator().cancelNavigation();
                }
                if (SentinelPlugin.debugMe) {
                    if (near != null && near.distanceSquared(getLivingEntity().getLocation()) > 3 * 3) {
                        debug("I'll just stand here and hope they come out...");
                    }
                }
            }
        }
        else if (chasing == null && pathingTo == null && npc.getNavigator().isNavigating()) {
            npc.getNavigator().cancelNavigation();
        }
    }

    /**
     * The maximum distance value (squared) for some distance calculations. Equal to ten-thousand (10000) blocks, squared (so: 100000000).
     */
    private final static double MAX_DIST = 100000000;

    /**
     * Gets the location this NPC is guarding (the NPC's own location if nothing else to guard).
     */
    public Location getGuardZone() {
        if (getGuarding() != null) {
            Player player = Bukkit.getPlayer(getGuarding());
            if (player != null) {
                return player.getLocation();
            }
        }
        if (chaseRange > 0) {
            Location goal = nearestPathPoint();
            if (goal != null) {
                return goal;
            }
        }
        return getLivingEntity().getLocation();
    }

    /**
     * Whether the waypoints helper (up-to-date Citizens) is available.
     */
    private Boolean waypointHelperAvailable = null;

    /**
     * Gets the nearest pathing point to this NPC.
     */
    public Location nearestPathPoint() {
        if (!SentinelTarget.v1_9) {
            if (waypointHelperAvailable == null) {
                try {
                    Class.forName("net.citizensnpcs.trait.waypoint.WaypointProvider.EnumerableWaypointProvider");
                    waypointHelperAvailable = true;
                }
                catch (ClassNotFoundException ex) {
                    waypointHelperAvailable = false;
                    SentinelPlugin.instance.getLogger().warning("Citizens installation is **very outdated** and does not contain newer useful APIs. Please update your installation of the Citizens plugin!");
                }
            }
            if (!waypointHelperAvailable) {
                return null;
            }
        }
        if (!npc.hasTrait(Waypoints.class)) {
            return null;
        }
        if (getGuarding() != null) {
            return null;
        }
        Waypoints wp = npc.getTrait(Waypoints.class);
        if (!(wp.getCurrentProvider() instanceof WaypointProvider.EnumerableWaypointProvider)) {
            return null;
        }
        Location baseloc = getLivingEntity().getLocation();
        Location nearest = null;
        double dist = MAX_DIST;
        for (Waypoint wayp : ((WaypointProvider.EnumerableWaypointProvider) wp.getCurrentProvider()).waypoints()) {
            Location l = wayp.getLocation();
            if (!l.getWorld().equals(baseloc.getWorld())) {
                continue;
            }
            double d = baseloc.distanceSquared(l);
            if (d < dist) {
                dist = d;
                nearest = l;
            }
        }
        return nearest;
    }

    /**
     * Tick counter for the {@code run} method.
     */
    public int cTick = 0;

    /**
     * Called every tick to run Sentinel updates if needed.
     */
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

    /**
     * Runnable for respawning, if needed.
     */
    public BukkitRunnable respawnMe;

    /**
     * Called when the NPC spawns in.
     */
    @Override
    public void onSpawn() {
        stats_timesSpawned++;
        setHealth(health);
        setInvincible(invincible);
        if (respawnMe != null) {
            respawnMe.cancel();
            respawnMe = null;
        }
    }

    /**
     * Causes the NPC to speak a message to a player.
     */
    public void sayTo(Player player, String message) {
        SpeechContext sc = new SpeechContext(npc, message, player);
        npc.getDefaultSpeechController().speak(sc, "chat");
    }

    /**
     * Called whenever a player teleports, for use with NPC guarding logic.
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerTeleports(final PlayerTeleportEvent event) {
        if (event.isCancelled()) {
            return;
        }
        if (getGuarding() == null) {
            return;
        }
        if (!event.getPlayer().getUniqueId().equals(getGuarding())) {
            return;
        }
        if (!npc.isSpawned()) {
            return;
        }
        if (event.getFrom().getWorld().equals(event.getTo().getWorld())) {
            npc.teleport(event.getTo(), PlayerTeleportEvent.TeleportCause.PLUGIN);
        }
        else { // World loading up can cause glitches.
            event.getFrom().getChunk().load();
            event.getTo().getChunk().load();
            Bukkit.getScheduler().runTaskLater(SentinelPlugin.instance, new Runnable() {
                @Override
                public void run() {
                    if (!event.getPlayer().getWorld().equals(event.getTo().getWorld())) {
                        return;
                    }
                    event.getFrom().getChunk().load();
                    event.getTo().getChunk().load();
                    npc.spawn(event.getTo());
                }
            }, 1);
        }
    }

    /**
     * Called every time a player moves at all, for use with monitoring if players move into range of an NPC.
     */
    @EventHandler
    public void onPlayerMovesInRange(PlayerMoveEvent event) {
        if (!npc.isSpawned()) {
            return;
        }
        if (!event.getTo().getWorld().equals(getLivingEntity().getLocation().getWorld())) {
            return;
        }
        if (event.getTo().toVector().equals(event.getFrom().toVector())) {
            return;
        }
        double dist = event.getTo().distanceSquared(getLivingEntity().getLocation());
        boolean known = greetedAlready.contains(event.getPlayer().getUniqueId());
        if (dist < greetRange && !known && targetingHelper.canSee(event.getPlayer())) {
            greetedAlready.add(event.getPlayer().getUniqueId());
            boolean enemy = targetingHelper.shouldTarget(event.getPlayer());
            if (enemy && warningText != null && warningText.length() > 0) {
                sayTo(event.getPlayer(), warningText);
            }
            else if (!enemy && greetingText != null && greetingText.length() > 0) {
                sayTo(event.getPlayer(), greetingText);
            }
        }
        else if (dist >= greetRange + 1 && known) {
            greetedAlready.remove(event.getPlayer().getUniqueId());
            // TODO: Farewell text perhaps?
        }
    }

    /**
     * Entities that will need their drops cleared if they die soon (because they were killed by this NPC).
     */
    public HashMap<UUID, Boolean> needsDropsClear = new HashMap<>();

    /**
     * Called when an entity might die from damage (called before Sentinel detects that an NPC might have killed an entity).
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void whenSomethingMightDie(EntityDamageByEntityEvent event) {
        needsDropsClear.remove(event.getEntity().getUniqueId());
    }

    /**
     * Outputs a debug message (if debug is enabled).
     */
    public void debug(String message) {
        if (SentinelPlugin.debugMe) {
            SentinelPlugin.instance.getLogger().info("Sentinel Debug: " + npc.getId() + "/" + npc.getName() + ": " + message);
        }
    }

    /**
     * Called when the NPC dies.
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void whenWeDie(EntityDeathEvent event) {
        if (CitizensAPI.getNPCRegistry().isNPC(event.getEntity())
                && CitizensAPI.getNPCRegistry().getNPC(event.getEntity()).getUniqueId().equals(npc.getUniqueId())) {
            if (SentinelPlugin.debugMe) {
                debug("Died! Death event received.");
            }
            event.getDrops().clear();
            if (event instanceof PlayerDeathEvent && !SentinelPlugin.instance.deathMessages) {
                ((PlayerDeathEvent) event).setDeathMessage("");
            }
            if (!SentinelPlugin.instance.workaroundDrops) {
                event.getDrops().addAll(drops);
            }
            event.setDroppedExp(0);
            generalDeathHandler(event.getEntity());
        }
    }

    /**
     * Handles some basics for when the NPC died.
     */
    public void generalDeathHandler(LivingEntity entity) {
        if (spawnPoint != null) {
            npc.getTrait(CurrentLocation.class).setLocation(spawnPoint.clone());
        }
        if (SentinelPlugin.instance.workaroundDrops) {
            for (ItemStack item : drops) {
                entity.getWorld().dropItemNaturally(entity.getLocation(), item.clone());
            }
        }
        onDeath();
    }

    /**
     * Called when any entity dies.
     */
    @EventHandler(priority = EventPriority.LOW)
    public void whenSomethingDies(EntityDeathEvent event) {
        if (event.getEntity().getType() != EntityType.PLAYER && needsDropsClear.containsKey(event.getEntity().getUniqueId())) {
            event.getDrops().clear();
            event.setDroppedExp(0);
        }
        targetingHelper.removeTarget(event.getEntity().getUniqueId());
    }

    /**
     * Handler for when the NPC died.
     */
    public void onDeath() {
        /*if (npc.hasTrait(Spawned.class)) {
            npc.getTrait(Spawned.class).setSpawned(false);
        }*/
        greetedAlready.clear();
        targetingHelper.currentTargets.clear();
        targetingHelper.currentAvoids.clear();
        if (respawnTime < 0) {
            BukkitRunnable removeMe = new BukkitRunnable() {
                @Override
                public void run() {
                    npc.destroy();
                }
            };
            removeMe.runTaskLater(SentinelPlugin.instance, 1);
        }
        else if (respawnTime > 0) {
            final long rsT = respawnTime;
            respawnMe = new BukkitRunnable() {
                long timer = 0;

                @Override
                public void run() {
                    if (CitizensAPI.getNPCRegistry().getById(npc.getId()) != null) {
                        if (npc.isSpawned()) {
                            this.cancel();
                            respawnMe = null;
                            return;
                        }
                        if (timer >= rsT) {
                            if (spawnPoint == null && npc.getStoredLocation() == null) {
                                SentinelPlugin.instance.getLogger().warning("NPC " + npc.getId() + " has a null spawn point and can't be spawned. Perhaps the world was deleted?");
                                this.cancel();
                                return;
                            }
                            npc.spawn(spawnPoint == null ? npc.getStoredLocation() : spawnPoint);
                            this.cancel();
                            respawnMe = null;
                            return;
                        }
                        timer += 10;
                    }
                    else {
                        respawnMe = null;
                        this.cancel();
                        return;
                    }
                }
            };
            respawnMe.runTaskTimer(SentinelPlugin.instance, 10, 10);
        }
        else { // respawnTime == 0
            npc.getTrait(Spawned.class).setSpawned(false);
        }
    }

    /**
     * Called when the NPC despawns.
     */
    @Override
    public void onDespawn() {
        targetingHelper.currentTargets.clear();
        targetingHelper.currentAvoids.clear();
    }

    /**
     * Sets the NPC's maximum health.
     */
    public void setHealth(double heal) {
        health = heal;
        if (npc.isSpawned()) {
            getLivingEntity().setMaxHealth(health);
            getLivingEntity().setHealth(health);
        }
    }

    /**
     * Sets whether the NPC is invincible.
     */
    public void setInvincible(boolean inv) {
        invincible = inv;
        npc.setProtected(invincible);
    }
}
