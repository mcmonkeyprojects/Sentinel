package org.mcmonkey.sentinel;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.ai.EntityTarget;
import net.citizensnpcs.api.ai.TargetType;
import net.citizensnpcs.api.ai.TeleportStuckAction;
import net.citizensnpcs.api.ai.speech.SpeechContext;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.trait.Inventory;
import net.citizensnpcs.api.trait.trait.Owner;
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
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.mcmonkey.sentinel.events.SentinelAttackEvent;

import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Pattern;

public class SentinelTrait extends Trait {

    public static final double healthMin = 0.01;

    public static final int attackRateMax = 2000;

    public static final int healRateMax = 2000;

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

    @Persist("stats_snowballsThrown")
    public long stats_snowballsThrown = 0;

    @Persist("stats_eggsThrown")
    public long stats_eggsThrown = 0;

    @Persist("stats_skullsThrown")
    public long stats_skullsThrown = 0;

    @Persist("stats_pearlsUsed")
    public long stats_pearlsUsed = 0;

    @Persist("stats_punches")
    public long stats_punches = 0;

    @Persist("stats_attackAttempts")
    public long stats_attackAttempts = 0;

    @Persist("stats_damageTaken")
    public double stats_damageTaken = 0;

    @Persist("stats_damageGiven")
    public double stats_damageGiven = 0;

    @Persist("targets")
    public HashSet<String> targets = new HashSet<>();

    @Persist("ignores")
    public HashSet<String> ignores = new HashSet<>();

    @Persist("playerNameTargets")
    public List<String> playerNameTargets = new ArrayList<>();

    @Persist("playerNameIgnores")
    public List<String> playerNameIgnores = new ArrayList<>();

    @Persist("npcNameTargets")
    public List<String> npcNameTargets = new ArrayList<>();

    @Persist("npcNameIgnores")
    public List<String> npcNameIgnores = new ArrayList<>();

    @Persist("entityNameTargets")
    public List<String> entityNameTargets = new ArrayList<>();

    @Persist("entityNameIgnores")
    public List<String> entityNameIgnores = new ArrayList<>();

    @Persist("heldItemTargets")
    public List<String> heldItemTargets = new ArrayList<>();

    @Persist("heldItemIgnores")
    public List<String> heldItemIgnores = new ArrayList<>();

    @Persist("groupTargets")
    public List<String> groupTargets = new ArrayList<>();

    @Persist("groupIgnores")
    public List<String> groupIgnores = new ArrayList<>();

    @Persist("eventTargets")
    public List<String> eventTargets = new ArrayList<>();

    @Persist("otherTargets")
    public List<String> otherTargets = new ArrayList<>();

    @Persist("otherIgnores")
    public List<String> otherIgnores = new ArrayList<>();

    @Persist("range")
    public double range = 20.0;

    @Persist("damage")
    public double damage = -1.0;

    @Persist("armor")
    public double armor = -1.0;

    @Persist("health")
    public double health = 20.0;

    @Persist("ranged_chase")
    public boolean rangedChase = false;

    @Persist("close_chase")
    public boolean closeChase = true;

    @Persist("invincible")
    public boolean invincible = false;

    @Persist("fightback")
    public boolean fightback = true;

    @Persist("attackRate")
    public int attackRate = 30;

    @Persist("attackRateRanged")
    public int attackRateRanged = 30;

    @Persist("healRate")
    public int healRate = 30;

    @Persist("guardingUpper")
    public long guardingUpper = 0;

    @Persist("guardingLower")
    public long guardingLower = 0;

    @Persist("needsAmmo")
    public boolean needsAmmo = false;

    @Persist("safeShot")
    public boolean safeShot = true;

    @Persist("respawnTime")
    public long respawnTime = 100;

    @Persist("chaseRange")
    public double chaseRange = 100;

    @Persist("spawnPoint")
    public Location spawnPoint = null;

    @Persist("drops")
    public List<ItemStack> drops = new ArrayList<>();

    @Persist("enemyDrops")
    public boolean enemyDrops = false;

    @Persist("enemyTargetTime")
    public long enemyTargetTime = 0;

    @Persist("speed")
    public double speed = 1;

    @Persist("warning_text")
    public String warningText = "";

    @Persist("greeting_text")
    public String greetingText = "";

    @Persist("greet_range")
    public double greetRange = 10;

    @Persist("autoswitch")
    public boolean autoswitch = false;

    @Persist("squad")
    public String squad = null;

    @Persist("accuracy")
    public double accuracy = 0;

    @Persist("realistic")
    public boolean realistic = false;

    @Persist("reach")
    public double reach = 3;

    public LivingEntity chasing = null;

    public UUID getGuarding() {
        if (guardingLower == 0 && guardingUpper == 0) {
            return null;
        }
        return new UUID(guardingUpper, guardingLower);
    }

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

    private boolean canEnforce = false;

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
            if (SentinelPlugin.instance.getConfig().getBoolean("random.enforce damage", false)) {
                if (canEnforce) {
                    canEnforce = false;
                    whenAttacksHappened(event);
                    if (!event.isCancelled()) {
                        ((LivingEntity) event.getEntity()).damage(event.getFinalDamage());
                    }
                    if (SentinelPlugin.debugMe) {
                        SentinelPlugin.instance.getLogger().info("Sentinel: enforce damage value to " + event.getFinalDamage());
                    }
                }
                else {
                    if (SentinelPlugin.debugMe) {
                        SentinelPlugin.instance.getLogger().info("Sentinel: refuse damage enforcement");
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
                if (SentinelPlugin.instance.getConfig().getBoolean("random.enforce damage", false)) {
                    if (canEnforce) {
                        canEnforce = false;
                        whenAttacksHappened(event);
                        if (!event.isCancelled()) {
                            ((LivingEntity) event.getEntity()).damage(getDamage());
                        }
                        if (SentinelPlugin.debugMe) {
                            SentinelPlugin.instance.getLogger().info("Sentinel: enforce damage value to " + getDamage());
                        }
                    }
                    else {
                        if (SentinelPlugin.debugMe) {
                            SentinelPlugin.instance.getLogger().info("Sentinel: refuse damage enforcement");
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
                            SentinelPlugin.instance.getLogger().info("Sentinel: Set damage for " + mod + " to " + event.getDamage(mod));
                        }
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void whenAttacksHappened(EntityDamageByEntityEvent event) {
        if (!npc.isSpawned()) {
            return;
        }
        if (event.isCancelled()) {
            return;
        }
        boolean isMe = event.getEntity().getUniqueId().equals(getLivingEntity().getUniqueId());
        if (sentinelProtected && isMe) {
            if (event.getDamager() instanceof LivingEntity && isIgnored((LivingEntity) event.getDamager())) {
                event.setCancelled(true);
                return;
            }
            else if (event.getDamager() instanceof Projectile) {
                ProjectileSource source = ((Projectile) event.getDamager()).getShooter();
                if (source instanceof LivingEntity && isIgnored((LivingEntity) source)) {
                    event.setCancelled(true);
                    return;
                }
            }
        }
        boolean isFriend = getGuarding() != null && event.getEntity().getUniqueId().equals(getGuarding());
        boolean attackerIsMe = event.getDamager().getUniqueId().equals(getLivingEntity().getUniqueId());
        if (isMe || isFriend) {
            if (attackerIsMe) {
                event.setCancelled(true);
                return;
            }
            if (isMe) {
                stats_damageTaken += event.getFinalDamage();
            }
            if (fightback && (event.getDamager() instanceof LivingEntity) && !isIgnored((LivingEntity) event.getDamager())) {
                addTarget(event.getDamager().getUniqueId());
            }
            else if (event.getDamager() instanceof Projectile) {
                ProjectileSource source = ((Projectile) event.getDamager()).getShooter();
                if (fightback && (source instanceof LivingEntity) && !isIgnored((LivingEntity) source)) {
                    if (((LivingEntity) source).getUniqueId().equals(getLivingEntity().getUniqueId())) {
                        event.setCancelled(true);
                        return;
                    }
                    addTarget(((LivingEntity) source).getUniqueId());
                }
            }
            return;
        }
        if (attackerIsMe) {
            if (safeShot && !shouldTarget((LivingEntity) event.getEntity())) {
                event.setCancelled(true);
                return;
            }
            stats_damageGiven += event.getFinalDamage();
            if (!enemyDrops) {
                needsDropsClear.put(event.getEntity().getUniqueId(), true);
            }
            return;
        }
        Entity e = event.getDamager();
        if (!(e instanceof LivingEntity)) {
            if (e instanceof Projectile) {
                ProjectileSource source = ((Projectile) e).getShooter();
                if (source instanceof LivingEntity) {
                    e = (LivingEntity) source;
                    if (e.getUniqueId().equals(getLivingEntity().getUniqueId())) {
                        if (safeShot && !shouldTarget((LivingEntity) event.getEntity())) {
                            event.setCancelled(true);
                            return;
                        }
                        stats_damageGiven += event.getFinalDamage();
                        if (!enemyDrops) {
                            needsDropsClear.put(event.getEntity().getUniqueId(), true);
                        }
                        return;
                    }
                }
            }
        }
        boolean isEventTarget = false;
        if (eventTargets.contains("pvp")
                && event.getEntity() instanceof Player
                && !CitizensAPI.getNPCRegistry().isNPC(event.getEntity())) {
            isEventTarget = true;
        }
        else if (eventTargets.contains("pve")
                && !(event.getEntity() instanceof Player)
                && event.getEntity() instanceof LivingEntity) {
            isEventTarget = true;
        }
        else if (eventTargets.contains("pvnpc")
                && event.getEntity() instanceof LivingEntity
                && CitizensAPI.getNPCRegistry().isNPC(event.getEntity())) {
            isEventTarget = true;
        }
        else if (eventTargets.contains("pvsentinel")
                && event.getEntity() instanceof LivingEntity
                && CitizensAPI.getNPCRegistry().isNPC(event.getEntity())
                && CitizensAPI.getNPCRegistry().getNPC(event.getEntity()).hasTrait(SentinelTrait.class)) {
            isEventTarget = true;
        }
        if (isEventTarget && e != null && e instanceof LivingEntity && canSee((LivingEntity) e) && !isIgnored((LivingEntity) e)) {
            addTarget(e.getUniqueId());
        }
    }

    @EventHandler
    public void whenAnEnemyDies(EntityDeathEvent event) {
        SentinelCurrentTarget target = new SentinelCurrentTarget();
        target.targetID = event.getEntity().getUniqueId();
        currentTargets.remove(target);
    }

    private boolean sentinelProtected;

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
        ignores.add(SentinelTarget.OWNER.name());
        sentinelProtected = config.getBoolean("random.protected", false);
        reach = config.getDouble("reach", 3);
    }

    public void useItem() {
        if (npc.isSpawned() && getLivingEntity() instanceof Player) {
            if (SentinelTarget.v1_9) {
                PlayerAnimation.START_USE_MAINHAND_ITEM.play((Player) getLivingEntity());
            }
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

    public double firingMinimumRange() {
        EntityType type = getLivingEntity().getType();
        if (type == EntityType.WITHER || type == EntityType.GHAST) {
            return 8; // Yikes!
        }
        return 2;
    }

    public HashMap.SimpleEntry<Location, Vector> getLaunchDetail(Location target, Vector lead) {
        double speeda;
        faceLocation(target);
        double angt = Double.POSITIVE_INFINITY;
        Location start = getLivingEntity().getEyeLocation().clone().add(getLivingEntity().getEyeLocation().getDirection().multiply(firingMinimumRange()));
        double sbase = SentinelPlugin.instance.getConfig().getDouble("random.shoot speed minimum", 20);
        for (speeda = sbase; speeda <= sbase + 15; speeda += 5) {
            angt = SentinelUtilities.getArrowAngle(start, target, speeda, 20);
            if (!Double.isInfinite(angt)) {
                break;
            }
        }
        if (Double.isInfinite(angt)) {
            return null;
        }
        double hangT = SentinelUtilities.hangtime(angt, speeda, target.getY() - start.getY(), 20);
        Location to = target.clone().add(lead.clone().multiply(hangT));
        Vector relative = to.clone().subtract(start.toVector()).toVector();
        double deltaXZ = Math.sqrt(relative.getX() * relative.getX() + relative.getZ() * relative.getZ());
        if (deltaXZ == 0) {
            deltaXZ = 0.1;
        }
        for (speeda = sbase; speeda <= sbase + 15; speeda += 5) {
            angt = SentinelUtilities.getArrowAngle(start, to, speeda, 20);
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
        speeda = speeda + (1.188 * hangT * hangT);
        relative = relative.multiply(speeda / 20.0);
        start.setDirection(normrel);
        return new HashMap.SimpleEntry<>(start, relative);
    }

    public double randomAcc() {
        return SentinelUtilities.random.nextDouble() * accuracy * 2 - accuracy;
    }

    public Vector fixForAcc(Vector input) {
        if (Double.isInfinite(input.getX()) || Double.isNaN(input.getX())) {
            return new Vector(0, 0, 0);
        }
        return new Vector(input.getX() + randomAcc(), input.getY() + randomAcc(), input.getZ() + randomAcc());
    }

    public void firePotion(ItemStack potion, Location target, Vector lead) {
        stats_potionsThrown++;
        HashMap.SimpleEntry<Location, Vector> start = getLaunchDetail(target, lead);
        Entity entpotion;
        if (SentinelTarget.v1_9) {
            entpotion = start.getKey().getWorld().spawnEntity(start.getKey(),
                    potion.getType() == Material.SPLASH_POTION ? EntityType.SPLASH_POTION : EntityType.LINGERING_POTION);
        }
        else {
            entpotion = start.getKey().getWorld().spawnEntity(start.getKey(), EntityType.SPLASH_POTION);
        }
        ((ThrownPotion) entpotion).setShooter(getLivingEntity());
        ((ThrownPotion) entpotion).setItem(potion);
        entpotion.setVelocity(fixForAcc(start.getValue()));
        swingWeapon();
    }

    public void fireArrow(ItemStack type, Location target, Vector lead) {
        HashMap.SimpleEntry<Location, Vector> start = getLaunchDetail(target, lead);
        if (start == null || start.getKey() == null) {
            return;
        }
        stats_arrowsFired++;
        Entity arrow;
        if (SentinelTarget.v1_9) {
            arrow = start.getKey().getWorld().spawnEntity(start.getKey(),
                    type.getType() == Material.SPECTRAL_ARROW ? EntityType.SPECTRAL_ARROW :
                            (type.getType() == Material.TIPPED_ARROW ? EntityType.TIPPED_ARROW : EntityType.ARROW));
            ((Projectile) arrow).setShooter(getLivingEntity());
            if (arrow instanceof TippedArrow) {
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
        }
        arrow.setVelocity(fixForAcc(start.getValue()));
        if (npc.getTrait(Inventory.class).getContents()[0].containsEnchantment(Enchantment.ARROW_FIRE)) {
            arrow.setFireTicks(10000);
        }
        useItem();
    }

    public void fireSnowball(Location target) {
        swingWeapon();
        stats_snowballsThrown++;
        faceLocation(target);
        Vector forward = getLivingEntity().getEyeLocation().getDirection();
        Location spawnAt = getLivingEntity().getEyeLocation().clone().add(forward.clone().multiply(firingMinimumRange()));
        Entity ent = spawnAt.getWorld().spawnEntity(spawnAt, EntityType.SNOWBALL);
        ((Projectile) ent).setShooter(getLivingEntity());
        ent.setVelocity(fixForAcc(target.clone().subtract(spawnAt).toVector().normalize().multiply(2.0))); // TODO: Fiddle with '2.0'.
    }

    public void fireEgg(Location target) {
        swingWeapon();
        stats_eggsThrown++;
        faceLocation(target);
        Vector forward = getLivingEntity().getEyeLocation().getDirection();
        Location spawnAt = getLivingEntity().getEyeLocation().clone().add(forward.clone().multiply(firingMinimumRange()));
        Entity ent = spawnAt.getWorld().spawnEntity(spawnAt, EntityType.EGG);
        ((Projectile) ent).setShooter(getLivingEntity());
        ent.setVelocity(fixForAcc(target.clone().subtract(spawnAt).toVector().normalize().multiply(2.0))); // TODO: Fiddle with '2.0'.
    }

    public void firePearl(LivingEntity target) {
        swingWeapon();
        faceLocation(target.getEyeLocation());
        // TODO: Maybe require entity is-on-ground?
        stats_pearlsUsed++;
        target.setVelocity(target.getVelocity().add(new Vector(0, getDamage(), 0)));
    }

    public void faceLocation(Location l) {
        npc.faceLocation(l.clone().subtract(0, getLivingEntity().getEyeHeight(), 0));
    }

    public void fireFireball(Location target) {
        swingWeapon();
        stats_fireballsFired++;
        faceLocation(target);
        Vector forward = getLivingEntity().getEyeLocation().getDirection();
        Location spawnAt = getLivingEntity().getEyeLocation().clone().add(forward.clone().multiply(firingMinimumRange()));
        Entity ent = spawnAt.getWorld().spawnEntity(spawnAt, EntityType.SMALL_FIREBALL);
        ((Projectile) ent).setShooter(getLivingEntity());
        ent.setVelocity(fixForAcc(target.clone().subtract(spawnAt).toVector().normalize().multiply(4))); // TODO: Fiddle with '4'.
    }

    public void fireSkull(Location target) {
        swingWeapon();
        stats_skullsThrown++;
        faceLocation(target);
        Vector forward = getLivingEntity().getEyeLocation().getDirection();
        Location spawnAt = getLivingEntity().getEyeLocation().clone().add(forward.clone().multiply(firingMinimumRange()));
        Entity ent = spawnAt.getWorld().spawnEntity(spawnAt, EntityType.WITHER_SKULL);
        ((Projectile) ent).setShooter(getLivingEntity());
        ent.setVelocity(fixForAcc(target.clone().subtract(spawnAt).toVector().normalize().multiply(4))); // TODO: Fiddle with '4'.
    }

    public double getDamage() {
        if (damage >= 0) {
            return damage;
        }
        ItemStack weapon;
        if (SentinelTarget.v1_9) {
            weapon = getLivingEntity().getEquipment().getItemInMainHand();
        }
        else {
            weapon = getLivingEntity().getEquipment().getItemInHand();
        }
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

    public void punch(LivingEntity entity) {
        faceLocation(entity.getLocation());
        swingWeapon();
        stats_punches++;
        if (SentinelPlugin.instance.getConfig().getBoolean("random.workaround damage", false)) {
            if (SentinelPlugin.debugMe) {
                SentinelPlugin.instance.getLogger().info("Sentinel: workaround damage value at " + getDamage() + " yields "
                 + ((getDamage() * (1.0 - getArmor(entity)))));
            }
            entity.damage(getDamage() * (1.0 - getArmor(entity)));
            Vector relative = entity.getLocation().toVector().subtract(getLivingEntity().getLocation().toVector());
            relative = relative.normalize();
            relative.setY(0.75);
            relative.multiply(0.5);
            entity.setVelocity(entity.getVelocity().add(relative));
            if (!enemyDrops) {
                needsDropsClear.put(entity.getUniqueId(), true);
            }
        }
        else {
            if (SentinelPlugin.debugMe) {
                SentinelPlugin.instance.getLogger().info("Sentinel: Punch/natural for " + getDamage());
            }
            entity.damage(getDamage(), getLivingEntity());
        }
    }

    Location bunny_goal = new Location(null, 0, 0, 0);

    public Entity getTargetFor(EntityTarget targ) {
        if (SentinelTarget.v1_9) {
            return targ.getTarget();
        }
        try {
            Method meth = EntityTarget.class.getMethod("getTarget");
            meth.setAccessible(true);
            return (LivingEntity) meth.invoke(targ);
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public void chase(LivingEntity entity) {
        if (npc.getNavigator().getTargetType() == TargetType.LOCATION
                && npc.getNavigator().getTargetAsLocation() != null
                && ((npc.getNavigator().getTargetAsLocation().getWorld().equals(entity.getWorld())
                && npc.getNavigator().getTargetAsLocation().distanceSquared(entity.getLocation()) < 2 * 2)
                || (npc.getNavigator().getTargetAsLocation().getWorld().equals(bunny_goal.getWorld())
                && npc.getNavigator().getTargetAsLocation().distanceSquared(bunny_goal) < 2 * 2))) {
            return;
        }
        cleverTicks = 0;
        chasing = entity;
        chased = true;
        if (npc.getNavigator().getTargetType() == TargetType.ENTITY
                && getTargetFor(npc.getNavigator().getEntityTarget()).getUniqueId().equals(entity.getUniqueId())) {
            return;
        }
        npc.getNavigator().getDefaultParameters().stuckAction(null);
        /*
        Location goal = entity.getLocation().clone().add(entity.getVelocity().clone());
        npc.getNavigator().setTarget(goal);
        bunny_goal = goal;
        */
        npc.getNavigator().setTarget(entity, false);
        npc.getNavigator().getLocalParameters().speedModifier((float) speed);
    }

    public ItemStack getArrow() {
        if (!npc.hasTrait(Inventory.class)) {
            return needsAmmo ? null : new ItemStack(Material.ARROW, 1);
        }
        Inventory inv = npc.getTrait(Inventory.class);
        ItemStack[] items = inv.getContents();
        for (ItemStack item : items) {
            if (item != null) {
                Material mat = item.getType();
                if (SentinelTarget.v1_9) {
                    if (mat == Material.ARROW || mat == Material.TIPPED_ARROW || mat == Material.SPECTRAL_ARROW) {
                        return item.clone();
                    }
                }
                else {
                    if (mat == Material.ARROW) {
                        return item.clone();
                    }
                }
            }
        }
        return needsAmmo ? null : new ItemStack(Material.ARROW, 1);
    }

    public void reduceDurability() {
        if (SentinelTarget.v1_9) {
            ItemStack item = getLivingEntity().getEquipment().getItemInMainHand();
            if (item != null && item.getType() != Material.AIR) {
                if (item.getDurability() >= item.getType().getMaxDurability() - 1) {
                    getLivingEntity().getEquipment().setItemInMainHand(null);
                }
                else {
                    item.setDurability((short) (item.getDurability() + 1));
                    getLivingEntity().getEquipment().setItemInMainHand(item);
                }
            }
        }
        else {
            ItemStack item = getLivingEntity().getEquipment().getItemInHand();
            if (item != null && item.getType() != Material.AIR) {
                if (item.getDurability() >= item.getType().getMaxDurability() - 1) {
                    getLivingEntity().getEquipment().setItemInHand(null);
                }
                else {
                    item.setDurability((short) (item.getDurability() + 1));
                    getLivingEntity().getEquipment().setItemInHand(item);
                }
            }
        }
    }

    public void takeArrow() {
        if (!npc.hasTrait(Inventory.class)) {
            return;
        }
        Inventory inv = npc.getTrait(Inventory.class);
        ItemStack[] items = inv.getContents();
        for (int i = 0; i < items.length; i++) {
            ItemStack item = items[i];
            if (item != null) {
                Material mat = item.getType();
                if (mat == Material.ARROW || (SentinelTarget.v1_9 && (mat == Material.TIPPED_ARROW || mat == Material.SPECTRAL_ARROW))) {
                    if (item.getAmount() > 1) {
                        item.setAmount(item.getAmount() - 1);
                        items[i] = item;
                        inv.setContents(items);
                        return;
                    }
                    else {
                        items[i] = null;
                        inv.setContents(items);
                        return;
                    }
                }
            }
        }
    }

    public void takeSnowball() {
        if (!npc.hasTrait(Inventory.class)) {
            return;
        }
        Inventory inv = npc.getTrait(Inventory.class);
        ItemStack[] items = inv.getContents();
        for (int i = 0; i < items.length; i++) {
            ItemStack item = items[i];
            if (item != null) {
                Material mat = item.getType();
                if (mat == SentinelTarget.MATERIAL_SNOW_BALL) {
                    if (item.getAmount() > 1) {
                        item.setAmount(item.getAmount() - 1);
                        items[i] = item;
                        inv.setContents(items);
                        return;
                    }
                    else {
                        items[i] = null;
                        inv.setContents(items);
                        return;
                    }
                }
            }
        }
    }

    public void takeOne() {
        if (SentinelTarget.v1_9) {
            ItemStack item = getLivingEntity().getEquipment().getItemInMainHand();
            if (item != null && item.getType() != Material.AIR) {
                if (item.getAmount() > 1) {
                    item.setAmount(item.getAmount() - 1);
                    getLivingEntity().getEquipment().setItemInMainHand(item);
                }
                else {
                    getLivingEntity().getEquipment().setItemInMainHand(null);
                }
            }
        }
        else {
            ItemStack item = getLivingEntity().getEquipment().getItemInHand();
            if (item != null && item.getType() != Material.AIR) {
                if (item.getAmount() > 1) {
                    item.setAmount(item.getAmount() - 1);
                    getLivingEntity().getEquipment().setItemInHand(item);
                }
                else {
                    getLivingEntity().getEquipment().setItemInHand(null);
                }
            }
        }
    }

    public boolean isWeapon(Material mat) {
        return SentinelTarget.WEAPON_DAMAGE_MULTIPLIERS.containsKey(mat)
                || SentinelTarget.POTION_MATERIALS.contains(mat)
                || SentinelTarget.BOW_MATERIALS.contains(mat)
                || SentinelTarget.SKULL_MATERIALS.contains(mat)
                || mat == SentinelTarget.MATERIAL_SNOW_BALL
                || mat == SentinelTarget.MATERIAL_BLAZE_ROD
                || mat == SentinelTarget.MATERIAL_NETHER_STAR;
    }

    public void grabNextItem() {
        if (!npc.hasTrait(Inventory.class)) {
            return;
        }
        Inventory inv = npc.getTrait(Inventory.class);
        ItemStack[] items = inv.getContents();
        ItemStack held = items[0];
        if (held != null && held.getType() != Material.AIR) {
            return;
        }
        for (int i = 0; i < items.length; i++) {
            ItemStack item = items[i];
            if (item != null) {
                item = item.clone();
                Material mat = item.getType();
                if (isWeapon(mat)) {
                    if (item.getAmount() > 1) {
                        item.setAmount(item.getAmount() - 1);
                        items[i] = item;
                        items[0] = item.clone();
                        items[0].setAmount(1);
                        inv.setContents(items);
                        item = item.clone();
                        item.setAmount(1);
                        return;
                    }
                    else {
                        items[i] = new ItemStack(Material.AIR);
                        items[0] = item.clone();
                        inv.setContents(items);
                        return;
                    }
                }
            }
        }
    }

    public void rechase() {
        if (chasing != null) {
            chase(chasing);
        }
    }

    public void swapToRanged() {
        if (!npc.hasTrait(Inventory.class)) {
            return;
        }
        int i = 0;
        Inventory inv = npc.getTrait(Inventory.class);
        ItemStack[] items = inv.getContents();
        ItemStack held = items[0] == null ? null : items[0].clone();
        boolean edit = false;
        while (!isRanged() && i < items.length - 1) {
            i++;
            if (items[i] != null && items[i].getType() != Material.AIR) {
                items[0] = items[i].clone();
                items[i] = new ItemStack(Material.AIR);
                inv.setContents(items);
                edit = true;
            }
        }
        if (edit) {
            items[i] = held;
            inv.setContents(items);
        }
    }

    public void swapToMelee() {
        if (!npc.hasTrait(Inventory.class)) {
            return;
        }
        int i = 0;
        Inventory inv = npc.getTrait(Inventory.class);
        ItemStack[] items = inv.getContents();
        ItemStack held = items[0] == null ? null : items[0].clone();
        boolean edit = false;
        while (isRanged() && i < items.length - 1) {
            i++;
            if (items[i] != null && items[i].getType() != Material.AIR) {
                items[0] = items[i].clone();
                items[i] = new ItemStack(Material.AIR);
                inv.setContents(items);
                edit = true;
            }
        }
        if (edit) {
            items[i] = held;
            inv.setContents(items);
        }
    }

    public void tryAttack(LivingEntity entity) {
        if (!entity.getWorld().equals(getLivingEntity().getWorld())) {
            return;
        }
        if (!getLivingEntity().hasLineOfSight(entity)) {
            return;
        }
        // TODO: Simplify this code!
        stats_attackAttempts++;
        double dist = getLivingEntity().getEyeLocation().distanceSquared(entity.getEyeLocation());
        if (SentinelPlugin.debugMe) {
            SentinelPlugin.instance.getLogger().info("Sentinel: tryAttack at range " + dist);
        }
        if (autoswitch && dist > reach * reach) {
            swapToRanged();
        }
        else if (autoswitch && dist < reach * reach) {
            swapToMelee();
        }
        SentinelAttackEvent sat = new SentinelAttackEvent(npc);
        Bukkit.getPluginManager().callEvent(sat);
        if (sat.isCancelled()) {
            if (SentinelPlugin.debugMe) {
                SentinelPlugin.instance.getLogger().info("Sentinel: tryAttack refused, event cancellation");
            }
            return;
        }
        addTarget(entity.getUniqueId());
        for (SentinelIntegration si : SentinelPlugin.integrations) {
            if (si.tryAttack(this, entity)) {
                return;
            }
        }
        if (usesBow()) {
            if (canSee(entity)) {
                if (timeSinceAttack < attackRateRanged) {
                    if (rangedChase) {
                        rechase();
                    }
                    return;
                }
                timeSinceAttack = 0;
                ItemStack item = getArrow();
                if (item != null) {
                    fireArrow(item, entity.getEyeLocation(), entity.getVelocity());
                    if (needsAmmo) {
                        reduceDurability();
                        takeArrow();
                        grabNextItem();
                    }
                }
            }
            else if (rangedChase) {
                chase(entity);
            }
        }
        else if (usesSnowball()) {
            if (canSee(entity)) {
                if (timeSinceAttack < attackRateRanged) {
                    if (rangedChase) {
                        rechase();
                    }
                    return;
                }
                timeSinceAttack = 0;
                ItemStack item = getArrow();
                if (item != null) {
                    fireSnowball(entity.getEyeLocation());
                    if (needsAmmo) {
                        takeSnowball();
                        grabNextItem();
                    }
                }
            }
            else if (rangedChase) {
                chase(entity);
            }
        }
        else if (usesPotion()) {
            if (canSee(entity)) {
                if (timeSinceAttack < attackRateRanged) {
                    if (rangedChase) {
                        rechase();
                    }
                    return;
                }
                timeSinceAttack = 0;
                if (SentinelTarget.v1_9) {
                    firePotion(getLivingEntity().getEquipment().getItemInMainHand(),
                            entity.getEyeLocation(), entity.getVelocity());
                }
                else {
                    firePotion(getLivingEntity().getEquipment().getItemInHand(),
                            entity.getEyeLocation(), entity.getVelocity());
                }
                if (needsAmmo) {
                    takeOne();
                    grabNextItem();
                }
            }
            else if (rangedChase) {
                chase(entity);
            }
        }
        else if (usesEgg()) {
            if (canSee(entity)) {
                if (timeSinceAttack < attackRateRanged) {
                    if (rangedChase) {
                        rechase();
                    }
                    return;
                }
                timeSinceAttack = 0;
                fireEgg(entity.getEyeLocation());
                if (needsAmmo) {
                    takeOne();
                    grabNextItem();
                }
            }
            else if (rangedChase) {
                chase(entity);
            }
        }
        else if (usesPearl()) {
            if (canSee(entity)) {
                if (timeSinceAttack < attackRateRanged) {
                    if (rangedChase) {
                        rechase();
                    }
                    return;
                }
                timeSinceAttack = 0;
                firePearl(entity);
                if (needsAmmo) {
                    takeOne();
                    grabNextItem();
                }
            }
            else if (rangedChase) {
                chase(entity);
            }
        }
        else if (usesWitherSkull()) {
            if (canSee(entity)) {
                if (timeSinceAttack < attackRateRanged) {
                    if (rangedChase) {
                        rechase();
                    }
                    return;
                }
                timeSinceAttack = 0;
                fireSkull(entity.getEyeLocation());
                if (needsAmmo) {
                    takeOne();
                    grabNextItem();
                }
            }
            else if (rangedChase) {
                chase(entity);
            }
        }
        else if (usesFireball()) {
            if (canSee(entity)) {
                if (timeSinceAttack < attackRateRanged) {
                    if (rangedChase) {
                        rechase();
                    }
                    return;
                }
                timeSinceAttack = 0;
                fireFireball(entity.getEyeLocation());
                if (needsAmmo) {
                    takeOne();
                    grabNextItem();
                }
            }
            else if (rangedChase) {
                chase(entity);
            }
        }
        else if (usesLightning()) {
            if (canSee(entity)) {
                if (timeSinceAttack < attackRateRanged) {
                    if (rangedChase) {
                        rechase();
                    }
                    return;
                }
                timeSinceAttack = 0;
                swingWeapon();
                entity.getWorld().strikeLightningEffect(entity.getLocation());
                if (SentinelPlugin.debugMe) {
                    SentinelPlugin.instance.getLogger().info("Sentinel: Lightning hits for " + getDamage());
                }
                entity.damage(getDamage());
                if (needsAmmo) {
                    takeOne();
                    grabNextItem();
                }
            }
            else if (rangedChase) {
                chase(entity);
            }
        }
        else if (usesSpectral()) {
            if (canSee(entity)) {
                if (timeSinceAttack < attackRateRanged) {
                    if (rangedChase) {
                        rechase();
                    }
                    return;
                }
                timeSinceAttack = 0;
                if (!entity.isGlowing()) {
                    swingWeapon();
                    try {
                        Sound snd = Sound.valueOf(SentinelPlugin.instance.getConfig().getString("random.spectral sound", "ENTITY_VILLAGER_YES"));
                        if (snd != null) {
                            entity.getWorld().playSound(entity.getLocation(), snd, 1f, 1f);
                        }
                    }
                    catch (Exception e) {
                        // Do nothing!
                    }
                    entity.setGlowing(true);
                    if (needsAmmo) {
                        takeOne();
                        grabNextItem();
                    }
                }
            }
            else if (rangedChase) {
                chase(entity);
            }
        }
        else {
            if (dist < reach * reach) {
                if (timeSinceAttack < attackRate) {
                    if (SentinelPlugin.debugMe) {
                        SentinelPlugin.instance.getLogger().info("Sentinel: tryAttack refused, timeSinceAttack");
                    }
                    if (closeChase) {
                        rechase();
                    }
                    return;
                }
                timeSinceAttack = 0;
                // TODO: Damage sword if needed!
                if (SentinelPlugin.debugMe) {
                    SentinelPlugin.instance.getLogger().info("Sentinel: tryAttack passed!");
                }
                punch(entity);
                if (needsAmmo && shouldTakeDura()) {
                    reduceDurability();
                    grabNextItem();
                }
            }
            else if (closeChase) {
                if (SentinelPlugin.debugMe) {
                    SentinelPlugin.instance.getLogger().info("Sentinel: tryAttack refused, range");
                }
                chase(entity);
            }
        }
    }

    public float getYaw(Vector vector) {
        double dx = vector.getX();
        double dz = vector.getZ();
        double yaw = 0;
        // Set yaw
        if (dx != 0) {
            // Set yaw start value based on dx
            if (dx < 0) {
                yaw = 1.5 * Math.PI;
            }
            else {
                yaw = 0.5 * Math.PI;
            }
            yaw -= Math.atan(dz / dx); // or atan2?
        }
        else if (dz < 0) {
            yaw = Math.PI;
        }
        return (float) (-yaw * 180 / Math.PI);
    }

    public boolean canSee(LivingEntity entity) {
        if (!getLivingEntity().hasLineOfSight(entity)) {
            return false;
        }
        if (realistic) {
            float yaw = getLivingEntity().getEyeLocation().getYaw();
            while (yaw < 0) {
                yaw += 360;
            }
            while (yaw >= 360) {
                yaw -= 360;
            }
            Vector rel = entity.getLocation().toVector().subtract(getLivingEntity().getLocation().toVector()).normalize();
            float yawHelp = getYaw(rel);
            if (!(Math.abs(yawHelp - yaw) < 90 ||
                    Math.abs(yawHelp + 360 - yaw) < 90 ||
                    Math.abs(yaw + 360 - yawHelp) < 90)) {
                return false;
            }
        }
        return true;
    }

    public LivingEntity getLivingEntity() {
        // Not a good idea to turn a non-living NPC into a Sentinel for now.
        return (LivingEntity) npc.getEntity();
    }

    public boolean isRanged() {
        return usesBow()
                || usesFireball()
                || usesSnowball()
                || usesLightning()
                || usesSpectral()
                || usesPotion();
    }

    public boolean usesBow() {
        if (!npc.hasTrait(Inventory.class)) {
            return false;
        }
        ItemStack it = npc.getTrait(Inventory.class).getContents()[0];
        return it != null && it.getType() == Material.BOW && getArrow() != null;
    }

    public boolean usesFireball() {
        if (!npc.hasTrait(Inventory.class)) {
            return false;
        }
        ItemStack it = npc.getTrait(Inventory.class).getContents()[0];
        return it != null && it.getType() == SentinelTarget.MATERIAL_BLAZE_ROD;
    }

    public boolean usesSnowball() {
        if (!npc.hasTrait(Inventory.class)) {
            return false;
        }
        ItemStack it = npc.getTrait(Inventory.class).getContents()[0];
        return it != null && it.getType() == SentinelTarget.MATERIAL_SNOW_BALL;
    }

    public boolean usesLightning() {
        if (!npc.hasTrait(Inventory.class)) {
            return false;
        }
        ItemStack it = npc.getTrait(Inventory.class).getContents()[0];
        return it != null && it.getType() == SentinelTarget.MATERIAL_NETHER_STAR;
    }

    public boolean usesEgg() {
        if (!npc.hasTrait(Inventory.class)) {
            return false;
        }
        ItemStack it = npc.getTrait(Inventory.class).getContents()[0];
        return it != null && it.getType() == Material.EGG;
    }

    public boolean usesPearl() {
        if (!npc.hasTrait(Inventory.class)) {
            return false;
        }
        ItemStack it = npc.getTrait(Inventory.class).getContents()[0];
        return it != null && it.getType() == Material.ENDER_PEARL;
    }

    public boolean usesWitherSkull() {
        if (!npc.hasTrait(Inventory.class)) {
            return false;
        }
        if (!SentinelPlugin.instance.canUseSkull) {
            return false;
        }
        ItemStack it = npc.getTrait(Inventory.class).getContents()[0];
        return it != null && SentinelTarget.SKULL_MATERIALS.contains(it.getType());
    }

    public boolean usesSpectral() {
        if (!npc.hasTrait(Inventory.class)) {
            return false;
        }
        if (!SentinelTarget.v1_10) {
            return false;
        }
        ItemStack it = npc.getTrait(Inventory.class).getContents()[0];
        return it != null && it.getType() == Material.SPECTRAL_ARROW;
    }

    public boolean usesPotion() {
        if (!npc.hasTrait(Inventory.class)) {
            return false;
        }
        ItemStack it = npc.getTrait(Inventory.class).getContents()[0];
        if (it == null) {
            return false;
        }
        if (!SentinelTarget.v1_9) {
            return it.getType() == Material.POTION;
        }
        return it.getType() == Material.SPLASH_POTION || it.getType() == Material.LINGERING_POTION;
    }

    public boolean shouldTakeDura() {
        Material type;
        if (SentinelTarget.v1_9) {
            type = getLivingEntity().getEquipment().getItemInMainHand().getType();
        }
        else {
            type = getLivingEntity().getEquipment().getItemInHand().getType();
        }
        return SentinelTarget.BOW_MATERIALS.contains(type) || SentinelTarget.SWORD_MATERIALS.contains(type)
                || SentinelTarget.PICKAXE_MATERIALS.contains(type) || SentinelTarget.AXE_MATERIALS.contains(type);
    }

    public boolean shouldTarget(LivingEntity entity) {
        if (entity.getUniqueId().equals(getLivingEntity().getUniqueId())) {
            return false;
        }
        return isTargeted(entity) && !isIgnored(entity);
    }

    public HashSet<SentinelCurrentTarget> currentTargets = new HashSet<>();

    private HashSet<UUID> greetedAlready = new HashSet<>();

    public void addTarget(UUID id) {
        if (id.equals(getLivingEntity().getUniqueId())) {
            return;
        }
        if (!(getEntityForID(id) instanceof LivingEntity)) {
            return;
        }
        addTargetNoBounce(id);
        if (squad != null) {
            for (NPC npc : CitizensAPI.getNPCRegistry()) {
                if (npc.hasTrait(SentinelTrait.class)) {
                    SentinelTrait sentinel = npc.getTrait(SentinelTrait.class);
                    if (sentinel.squad != null && sentinel.squad.equals(squad)) {
                        sentinel.addTargetNoBounce(id);
                    }
                }
            }
        }
    }

    public void addTargetNoBounce(UUID id) {
        SentinelCurrentTarget target = new SentinelCurrentTarget();
        target.targetID = id;
        target.ticksLeft = enemyTargetTime;
        currentTargets.remove(target);
        currentTargets.add(target);
    }

    public boolean isRegexTargeted(String name, List<String> regexes) {
        for (String str : regexes) {
            Pattern pattern = Pattern.compile(".*" + str + ".*", Pattern.CASE_INSENSITIVE);
            // TODO: Is this more efficient than .matches, or should we change it?
            if (pattern.matcher(name).matches()) {
                return true;
            }
        }
        return false;
    }

    public boolean isAir(ItemStack its) {
        return its == null || its.getType() == Material.AIR;
    }

    public boolean isInvisible(LivingEntity entity) {
        SentinelCurrentTarget sct = new SentinelCurrentTarget();
        sct.targetID = entity.getUniqueId();
        EntityEquipment eq = entity.getEquipment();
        return entity.hasPotionEffect(PotionEffectType.INVISIBILITY)
                && !currentTargets.contains(sct)
                && isAir(eq.getItemInHand())
                && isAir(eq.getBoots())
                && isAir(eq.getLeggings())
                && isAir(eq.getChestplate())
                && isAir(eq.getHelmet())
                && SentinelPlugin.instance.getConfig().getBoolean("random.ignore invisible targets");
    }

    public boolean isIgnored(LivingEntity entity) {
        if (isInvisible(entity)) {
            return true;
        }
        if (entity.getUniqueId().equals(getLivingEntity().getUniqueId())) {
            return true;
        }
        if (getGuarding() != null && entity.getUniqueId().equals(getGuarding())) {
            return true;
        }
        if (SentinelTarget.v1_9) {
            if (entity.getEquipment() != null && entity.getEquipment().getItemInMainHand() != null
                    && isRegexTargeted(entity.getEquipment().getItemInMainHand().getType().name(), heldItemIgnores)) {
                return true;
            }
        }
        else {
            if (entity.getEquipment() != null && entity.getEquipment().getItemInHand() != null
                    && isRegexTargeted(entity.getEquipment().getItemInHand().getType().name(), heldItemIgnores)) {
                return true;
            }
        }
        for (SentinelIntegration integration : SentinelPlugin.integrations) {
            for (String text : otherIgnores) {
                if (integration.isTarget(entity, text)) {
                    return true;
                }
            }
        }
        if (entity.hasMetadata("NPC")) {
            return ignores.contains(SentinelTarget.NPCS.name()) ||
                    isRegexTargeted(CitizensAPI.getNPCRegistry().getNPC(entity).getName(), npcNameIgnores);
        }
        else if (entity instanceof Player) {
            if (((Player) entity).getGameMode() == GameMode.CREATIVE || ((Player) entity).getGameMode() == GameMode.SPECTATOR) {
                return true;
            }
            if (isRegexTargeted(((Player) entity).getName(), playerNameIgnores)) {
                return true;
            }
            if (SentinelPlugin.instance.vaultPerms != null) {
                for (String group : groupIgnores) {
                    if (SentinelPlugin.instance.vaultPerms.playerInGroup((Player) entity, group)) {
                        return true;
                    }
                }
            }
        }
        else if (isRegexTargeted(entity.getCustomName() == null ? entity.getType().name() : entity.getCustomName(), entityNameIgnores)) {
            return true;
        }
        if (ignores.contains(SentinelTarget.OWNER.name()) && entity.getUniqueId().equals(npc.getTrait(Owner.class).getOwnerId())) {
            return true;
        }
        HashSet<SentinelTarget> possible = SentinelPlugin.entityToTargets.get(entity.getType());
        for (SentinelTarget poss : possible) {
            if (ignores.contains(poss.name())) {
                return true;
            }
        }
        return false;
    }

    public boolean isTargeted(LivingEntity entity) {
        if (isInvisible(entity)) {
            return false;
        }
        SentinelCurrentTarget target = new SentinelCurrentTarget();
        target.targetID = entity.getUniqueId();
        if (entity.getUniqueId().equals(getLivingEntity().getUniqueId())) {
            return false;
        }
        if (getGuarding() != null && entity.getUniqueId().equals(getGuarding())) {
            return false;
        }
        if (currentTargets.contains(target)) {
            return true;
        }
        if (SentinelTarget.v1_9) {
            if (entity.getEquipment() != null && entity.getEquipment().getItemInMainHand() != null
                    && isRegexTargeted(entity.getEquipment().getItemInMainHand().getType().name(), heldItemTargets)) {
                return true;
            }
        }
        else {
            if (entity.getEquipment() != null && entity.getEquipment().getItemInHand() != null
                    && isRegexTargeted(entity.getEquipment().getItemInHand().getType().name(), heldItemTargets)) {
                return true;
            }
        }
        for (SentinelIntegration integration : SentinelPlugin.integrations) {
            for (String text : otherTargets) {
                if (integration.isTarget(entity, text)) {
                    return true;
                }
            }
        }
        if (entity.hasMetadata("NPC")) {
            return targets.contains(SentinelTarget.NPCS.name()) ||
                    isRegexTargeted(CitizensAPI.getNPCRegistry().getNPC(entity).getName(), npcNameTargets);
        }
        if (entity instanceof Player) {
            if (isRegexTargeted(((Player) entity).getName(), playerNameTargets)) {
                return true;
            }
            if (SentinelPlugin.instance.vaultPerms != null) {
                for (String group : groupTargets) {
                    if (SentinelPlugin.instance.vaultPerms.playerInGroup((Player) entity, group)) {
                        return true;
                    }
                }
            }
        }
        else if (isRegexTargeted(entity.getCustomName() == null ? entity.getType().name() : entity.getCustomName(), entityNameTargets)) {
            return true;
        }
        if (targets.contains(SentinelTarget.OWNER.name()) && entity.getUniqueId().equals(npc.getTrait(Owner.class).getOwnerId())) {
            return true;
        }
        HashSet<SentinelTarget> possible = SentinelPlugin.entityToTargets.get(entity.getType());
        for (SentinelTarget poss : possible) {
            if (targets.contains(poss.name())) {
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
        boolean ignoreGlow = usesSpectral();
        double rangesquared = range * range;
        double crsq = chaseRange * chaseRange;
        Location pos = getGuardZone();
        if (!getGuardZone().getWorld().equals(getLivingEntity().getWorld())) {
            // Emergency corrective measures...
            npc.getNavigator().cancelNavigation();
            getLivingEntity().teleport(getGuardZone());
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

    public long timeSinceAttack = 0;

    public long timeSinceHeal = 0;

    private Entity getEntityForID(UUID id) {
        if (!SentinelTarget.v1_12) {
            for (Entity e : getLivingEntity().getWorld().getEntities()) {
                if (e.getUniqueId().equals(id)) {
                    return e;
                }
            }
            return null;
        }
        return Bukkit.getServer().getEntity(id);
    }

    private void updateTargets() {
        for (SentinelCurrentTarget uuid : new HashSet<>(currentTargets)) {
            Entity e = getEntityForID(uuid.targetID);
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
            if (d > range * range * 4 && d > chaseRange * chaseRange * 4) {
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
        if (chasing != null) {
            SentinelCurrentTarget cte = new SentinelCurrentTarget();
            cte.targetID = chasing.getUniqueId();
            if (!currentTargets.contains(cte)) {
                chasing = null;
                npc.getNavigator().cancelNavigation();
            }
        }
    }

    int cleverTicks = 0;

    public boolean chased = false;

    public void specialMarkVision() {
        if (SentinelPlugin.debugMe) {
            SentinelPlugin.instance.getLogger().info("Sentinel: Target! I see you, " + (chasing == null ? "(Unknown)" : chasing.getName()));
        }
        if (SentinelTarget.v1_11 && getLivingEntity().getType() == EntityType.SHULKER) {
            NMS.setPeekShulker(getLivingEntity(), 100);
        }
    }

    public void specialUnmarkVision() {
        if (SentinelPlugin.debugMe) {
            SentinelPlugin.instance.getLogger().info("Sentinel: Goodbye, visible target " + (chasing == null ? "(Unknown)" : chasing.getName()));
        }
        if (SentinelTarget.v1_11 && getLivingEntity().getType() == EntityType.SHULKER) {
            NMS.setPeekShulker(getLivingEntity(), 0);
        }
    }

    public static Random random = new Random();

    public static double randomDecimal(double min, double max) {
        return (random.nextDouble() * (max - min)) + min;
    }

    public int ticksCountGuard = 0;

    public static Location rayTrace(Location start, Location end) {
        double dSq = start.distanceSquared(end);
        if (dSq < 1) {
            if (end.getBlock().getType().isSolid()) {
                return start.clone();
            }
            return end.clone();
        }
        double dist = Math.sqrt(dSq);
        Vector move = end.toVector().subtract(start.toVector()).multiply(1.0 / dist);
        int iters = (int) Math.ceil(dist);
        Location cur = start.clone();
        Location next = cur.clone().add(move);
        for (int i = 0; i < iters; i++) {
            if (next.getBlock().getType().isSolid()) {
                return cur;
            }
            cur = cur.add(move);
            next = next.add(move);
        }
        return cur;
    }

    public static Location pickNear(Location start, double range) {
        Location hit = rayTrace(start.clone().add(0, 1.5, 0), start.clone().add(randomDecimal(-range, range), 1.5, randomDecimal(range, range)));
        if (hit.subtract(0, 1, 0).getBlock().getType().isSolid()) {
            return hit;
        }
        return hit.subtract(0, 1, 0);
    }

    public void runUpdate() {
        canEnforce = true;
        timeSinceAttack += SentinelPlugin.instance.tickRate;
        timeSinceHeal += SentinelPlugin.instance.tickRate;
        if (getLivingEntity().getLocation().getY() <= 0) {
            if (SentinelPlugin.debugMe) {
                SentinelPlugin.instance.getLogger().info("Sentinel: Injuring self, I'm below the map!");
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
        if (health != getLivingEntity().getMaxHealth()) {
            getLivingEntity().setMaxHealth(health);
        }
        if (healRate > 0 && timeSinceHeal > healRate && getLivingEntity().getHealth() < health) {
            getLivingEntity().setHealth(Math.min(getLivingEntity().getHealth() + 1.0, health));
            timeSinceHeal = 0;
        }
        if (getGuarding() != null && npc.hasTrait(Waypoints.class)) {
            Waypoints wp = npc.getTrait(Waypoints.class);
            wp.getCurrentProvider().setPaused(true);
        }
        else if (npc.hasTrait(Waypoints.class)) {
            Waypoints wp = npc.getTrait(Waypoints.class);
            wp.getCurrentProvider().setPaused(false);
        }
        double crsq = chaseRange * chaseRange;
        updateTargets();
        boolean goHome = chased;
        LivingEntity target = findBestTarget();
        if (target != null) {
            Location near = nearestPathPoint();
            if (SentinelPlugin.debugMe) {
                SentinelPlugin.instance.getLogger().info("Sentinel: target selected to be " + target.getName());
            }
            if (crsq <= 0 || near == null || near.distanceSquared(target.getLocation()) <= crsq) {
                if (SentinelPlugin.debugMe) {
                    SentinelPlugin.instance.getLogger().info("Sentinel: Attack target within range of safe zone: "
                            + (near == null ? "Any" : near.distanceSquared(target.getLocation())));
                }
                if (chasing == null) {
                    specialMarkVision();
                }
                chasing = target;
                cleverTicks = 0;
                tryAttack(target);
                goHome = false;
            }
            else {
                if (SentinelPlugin.debugMe) {
                    SentinelPlugin.instance.getLogger().info("Sentinel: Actually, that target is bad!");
                }
                specialUnmarkVision();
                target = null;
                chasing = null;
                cleverTicks = 0;
            }
        }
        else if (chasing != null && chasing.isValid()) {
            cleverTicks++;
            if (cleverTicks >= SentinelPlugin.instance.cleverTicks) {
                specialUnmarkVision();
                chasing = null;
            }
            else {
                Location near = nearestPathPoint();
                if (crsq <= 0 || near == null || near.distanceSquared(chasing.getLocation()) <= crsq) {
                    tryAttack(chasing);
                    goHome = false;
                }
            }
        }
        else if (chasing == null) {
            specialUnmarkVision();
        }
        if (getGuarding() != null) {
            Player player = Bukkit.getPlayer(getGuarding());
            if (player != null) {
                Location myLoc = getLivingEntity().getLocation();
                Location theirLoc = player.getLocation();
                double dist = theirLoc.getWorld().equals(myLoc.getWorld()) ? myLoc.distanceSquared(theirLoc) : MAX_DIST;
                if (dist > 60 * 60) {
                    npc.teleport(player.getLocation(), PlayerTeleportEvent.TeleportCause.PLUGIN);
                }
                if (dist > 7 * 7) {
                    // TODO: distance margins (7 above, 2 below, 4 below) configuration options?
                    ticksCountGuard += SentinelPlugin.instance.tickRate;
                    if (ticksCountGuard >= 30) {
                        ticksCountGuard = 0;
                        npc.getNavigator().getDefaultParameters().distanceMargin(2);
                        npc.getNavigator().getDefaultParameters().range(100);
                        npc.getNavigator().getDefaultParameters().stuckAction(TeleportStuckAction.INSTANCE);
                        npc.getNavigator().setTarget(pickNear(player.getLocation(), 4));
                        npc.getNavigator().getLocalParameters().speedModifier((float) speed);
                        chased = true;
                    }
                }
                goHome = false;
            }
        }
        if (goHome && chaseRange > 0 && target == null) {
            Location near = nearestPathPoint();
            if (near != null && (chasing == null || near.distanceSquared(chasing.getLocation()) > crsq)) {
                if (SentinelPlugin.debugMe) {
                    if (near.distanceSquared(getLivingEntity().getLocation()) > 3 * 3) {
                        SentinelPlugin.instance.getLogger().info("Sentinel: screw you guys, I'm going home!");
                    }
                }
                npc.getNavigator().getDefaultParameters().stuckAction(TeleportStuckAction.INSTANCE);
                npc.getNavigator().setTarget(near);
                npc.getNavigator().getLocalParameters().speedModifier((float) speed);
                chased = false;
            }
            else {
                if (npc.getNavigator().getEntityTarget() != null) {
                    npc.getNavigator().cancelNavigation();
                }
                if (SentinelPlugin.debugMe) {
                    if (near != null && near.distanceSquared(getLivingEntity().getLocation()) > 3 * 3) {
                        SentinelPlugin.instance.getLogger().info("Sentinel: I'll just stand here and hope they come out...");
                    }
                }
            }
        }
        else if (chasing == null && npc.getNavigator().getEntityTarget() != null) {
            npc.getNavigator().cancelNavigation();
        }
    }

    private final static double MAX_DIST = 100000000;

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

    public Location nearestPathPoint() {
        if (!SentinelTarget.v1_9) {
            return null; // TODO: !!!
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

    public BukkitRunnable respawnMe;

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

    public void sayTo(Player player, String message) {
        SpeechContext sc = new SpeechContext(npc, message, player);
        npc.getDefaultSpeechController().speak(sc, "chat");
    }

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

    @EventHandler
    public void onPlayerMovesInRange(PlayerMoveEvent event) {
        if (!npc.isSpawned()) {
            return;
        }
        if (!event.getTo().getWorld().equals(getLivingEntity().getLocation().getWorld())) {
            return;
        }
        double dist = event.getTo().distanceSquared(getLivingEntity().getLocation());
        boolean known = greetedAlready.contains(event.getPlayer().getUniqueId());
        if (dist < greetRange && !known && canSee(event.getPlayer())) {
            greetedAlready.add(event.getPlayer().getUniqueId());
            boolean enemy = shouldTarget(event.getPlayer());
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

    public HashMap<UUID, Boolean> needsDropsClear = new HashMap<>();

    @EventHandler(priority = EventPriority.HIGHEST)
    public void whenSomethingMightDie(EntityDamageByEntityEvent event) {
        needsDropsClear.remove(event.getEntity().getUniqueId());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void whenWeDie(EntityDeathEvent event) {
        if (CitizensAPI.getNPCRegistry().isNPC(event.getEntity())
                && CitizensAPI.getNPCRegistry().getNPC(event.getEntity()).getUniqueId().equals(npc.getUniqueId())) {
            event.getDrops().clear();
            if (event instanceof PlayerDeathEvent && !SentinelPlugin.instance.getConfig().getBoolean("random.death messages", true)) {
                ((PlayerDeathEvent) event).setDeathMessage("");
            }
            if (!SentinelPlugin.instance.getConfig().getBoolean("random.workaround drops", false)) {
                event.getDrops().addAll(drops);
            }
            else {
                for (ItemStack item : drops) {
                    event.getEntity().getWorld().dropItemNaturally(event.getEntity().getLocation(), item.clone());
                }
            }
            event.setDroppedExp(0);
            onDeath();
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void whenSomethingDies(EntityDeathEvent event) {
        if (event.getEntity().getType() != EntityType.PLAYER && needsDropsClear.containsKey(event.getEntity().getUniqueId())) {
            event.getDrops().clear();
            event.setDroppedExp(0);
        }
    }

    public void onDeath() {
        /*if (npc.hasTrait(Spawned.class)) {
            npc.getTrait(Spawned.class).setSpawned(false);
        }*/
        greetedAlready.clear();
        currentTargets.clear();
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
    }

    @Override
    public void onDespawn() {
        currentTargets.clear();
    }

    public void setHealth(double heal) {
        health = heal;
        if (npc.isSpawned()) {
            getLivingEntity().setMaxHealth(health);
            getLivingEntity().setHealth(health);
        }
    }

    public void setInvincible(boolean inv) {
        invincible = inv;
        npc.setProtected(invincible);
    }
}
