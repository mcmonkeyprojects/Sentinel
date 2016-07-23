package org.mcmonkey.sentinel;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.ai.TargetType;
import net.citizensnpcs.api.ai.TeleportStuckAction;
import net.citizensnpcs.api.ai.speech.SpeechContext;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.trait.Inventory;
import net.citizensnpcs.api.trait.trait.Owner;
import net.citizensnpcs.trait.waypoint.Waypoint;
import net.citizensnpcs.trait.waypoint.WaypointProvider;
import net.citizensnpcs.trait.waypoint.Waypoints;
import net.citizensnpcs.util.PlayerAnimation;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.craftbukkit.v1_10_R1.CraftWorld;
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
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.regex.Pattern;

public class SentinelTrait extends Trait {

    public static final double healthMin = 0.01;

    public static final double healthMax = 2000;

    public static final int attackRateMin = 10;

    public static final int attackRateMax = 2000;

    public static final int healRateMin = 0;

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

    @Persist("stats_punches")
    public long stats_punches = 0;

    @Persist("stats_attackAttempts")
    public long stats_attackAttempts = 0;

    @Persist("stats_damageTaken")
    public double stats_damageTaken = 0;

    @Persist("stats_damageGiven")
    public double stats_damageGiven = 0;

    @Persist("targets")
    public HashSet<SentinelTarget> targets = new HashSet<SentinelTarget>();

    @Persist("ignores")
    public HashSet<SentinelTarget> ignores = new HashSet<SentinelTarget>();

    @Persist("playerNameTargets")
    public List<String> playerNameTargets = new ArrayList<String>();

    @Persist("playerNameIgnores")
    public List<String> playerNameIgnores = new ArrayList<String>();

    @Persist("npcNameTargets")
    public List<String> npcNameTargets = new ArrayList<String>();

    @Persist("npcNameIgnores")
    public List<String> npcNameIgnores = new ArrayList<String>();

    @Persist("entityNameTargets")
    public List<String> entityNameTargets = new ArrayList<String>();

    @Persist("entityNameIgnores")
    public List<String> entityNameIgnores = new ArrayList<String>();

    @Persist("heldItemTargets")
    public List<String> heldItemTargets = new ArrayList<String>();

    @Persist("heldItemIgnores")
    public List<String> heldItemIgnores = new ArrayList<String>();

    @Persist("groupTargets")
    public List<String> groupTargets = new ArrayList<String>();

    @Persist("groupIgnores")
    public List<String> groupIgnores = new ArrayList<String>();

    @Persist("eventTargets")
    public List<String> eventTargets = new ArrayList<String>();

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
    public List<ItemStack> drops = new ArrayList<ItemStack>();

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

    @EventHandler(priority = EventPriority.HIGH)
    public void whenAttacksAreHappening(EntityDamageByEntityEvent event) {
        if (!npc.isSpawned()) {
            return;
        }
        if (event.getEntity().getUniqueId().equals(getLivingEntity().getUniqueId())) {
            event.setDamage(EntityDamageEvent.DamageModifier.ARMOR, -getArmor(getLivingEntity()) * event.getDamage(EntityDamageEvent.DamageModifier.BASE));
            return;
        }
        if (event.getDamager().getUniqueId().equals(getLivingEntity().getUniqueId())) {
            event.setDamage(EntityDamageEvent.DamageModifier.BASE, getDamage());
        }
        if (event.getDamager() instanceof Projectile) {
            ProjectileSource source = ((Projectile) event.getDamager()).getShooter();
            if (source instanceof LivingEntity && ((LivingEntity) source).getUniqueId().equals(getLivingEntity().getUniqueId())) {
                event.setDamage(EntityDamageEvent.DamageModifier.BASE, getDamage());
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void whenAttacksHappened(EntityDamageByEntityEvent event) {
        if (!npc.isSpawned()) {
            return;
        }
        boolean isMe = event.getEntity().getUniqueId().equals(getLivingEntity().getUniqueId());
        boolean isFriend = getGuarding() != null && event.getEntity().getUniqueId().equals(getGuarding());
        if (isMe || isFriend) {
            if (isMe) {
                stats_damageTaken += event.getFinalDamage();
            }
            if (fightback && (event.getDamager() instanceof LivingEntity)) {
                addTarget(event.getDamager().getUniqueId());
            }
            else if (event.getDamager() instanceof Projectile) {
                ProjectileSource source = ((Projectile) event.getDamager()).getShooter();
                if (fightback && (source instanceof LivingEntity)) {
                    addTarget(((LivingEntity) source).getUniqueId());
                }
            }
            return;
        }
        if (event.getDamager().getUniqueId().equals(getLivingEntity().getUniqueId())) {
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
                ProjectileSource source = ((Projectile)e).getShooter();
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
        if (isEventTarget && e != null && e instanceof LivingEntity && canSee((LivingEntity) e)) {
            addTarget(e.getUniqueId());
        }
    }

    @EventHandler
    public void whenAnEnemyDies(EntityDeathEvent event) {
        SentinelCurrentTarget target = new SentinelCurrentTarget();
        target.targetID = event.getEntity().getUniqueId();
        currentTargets.remove(target);
    }

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
        ignores.add(SentinelTarget.OWNER);
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

    public double firingMinimumRange() {
        EntityType type = getLivingEntity().getType();
        if (type == EntityType.WITHER || type == EntityType.WITHER) {
            return 8; // Yikes!
        }
        return 2;
    }

    public HashMap.SimpleEntry<Location, Vector> getLaunchDetail(Location target, Vector lead) {
        double speeda;
        npc.faceLocation(target);
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
        return new HashMap.SimpleEntry<Location, Vector>(start, relative);
    }

    public void firePotion(ItemStack potion, Location target, Vector lead) {
        stats_potionsThrown++;
        HashMap.SimpleEntry<Location, Vector> start = getLaunchDetail(target, lead);
        Entity entpotion = start.getKey().getWorld().spawnEntity(start.getKey(),
                potion.getType() == Material.SPLASH_POTION ? EntityType.SPLASH_POTION: EntityType.LINGERING_POTION);
        ((ThrownPotion) entpotion).setShooter(getLivingEntity());
        ((ThrownPotion) entpotion).setItem(potion);
        entpotion.setVelocity(start.getValue());
        swingWeapon();
    }

    public void fireArrow(ItemStack type, Location target, Vector lead) {
        stats_arrowsFired++;
        HashMap.SimpleEntry<Location, Vector> start = getLaunchDetail(target, lead);
        Entity arrow = start.getKey().getWorld().spawnEntity(start.getKey(),
                type.getType() == Material.SPECTRAL_ARROW ? EntityType.SPECTRAL_ARROW :
                        (type.getType() == Material.TIPPED_ARROW ? EntityType.TIPPED_ARROW : EntityType.ARROW));
        ((Projectile)arrow).setShooter(getLivingEntity());
        if (arrow instanceof TippedArrow) {
            ((TippedArrow)arrow).setBasePotionData(((PotionMeta)type.getItemMeta()).getBasePotionData());
            for (PotionEffect effect: ((PotionMeta)type.getItemMeta()).getCustomEffects()) {
                ((TippedArrow)arrow).addCustomEffect(effect, true);
            }
        }
        arrow.setVelocity(start.getValue());
        // TODO: Prevent pick up if needed!
        useItem();
    }

    public void fireSnowball(Location target) {
        swingWeapon();
        stats_snowballsThrown++;
        npc.faceLocation(target);
        Vector forward = getLivingEntity().getEyeLocation().getDirection();
        Location spawnAt = getLivingEntity().getEyeLocation().clone().add(forward.clone().multiply(firingMinimumRange()));
        Entity ent = spawnAt.getWorld().spawnEntity(spawnAt, EntityType.SNOWBALL);
        ((Projectile) ent).setShooter(getLivingEntity());
        ent.setVelocity(target.clone().subtract(spawnAt).toVector().normalize().multiply(2.5)); // TODO: Fiddle with '2.5'.
    }

    public void fireFireball(Location target) {
        swingWeapon();
        stats_fireballsFired++;
        npc.faceLocation(target);
        Vector forward = getLivingEntity().getEyeLocation().getDirection();
        Location spawnAt = getLivingEntity().getEyeLocation().clone().add(forward.clone().multiply(firingMinimumRange()));
        Entity ent = spawnAt.getWorld().spawnEntity(spawnAt, EntityType.SMALL_FIREBALL);
        ((Projectile) ent).setShooter(getLivingEntity());
        ent.setVelocity(target.clone().subtract(spawnAt).toVector().normalize().multiply(4)); // TODO: Fiddle with '4'.
    }

    public double getDamage() {
        if (damage < 0) {
            ItemStack weapon = getLivingEntity().getEquipment().getItemInMainHand();
            if (weapon == null) {
                return 1;
            }
            // TODO: Less randomness, more game-like calculations.
            double multiplier = 1;
            multiplier += weapon.getItemMeta() == null ? 0: weapon.getItemMeta().getEnchantLevel(Enchantment.DAMAGE_ALL) * 0.2;
            switch (weapon.getType()) {
                case BOW:
                    return 6 * (1 + (weapon.getItemMeta() == null ? 0: weapon.getItemMeta().getEnchantLevel(Enchantment.ARROW_DAMAGE) * 0.3));
                case DIAMOND_SWORD:
                    return 7 * multiplier;
                case IRON_SWORD:
                    return 6 * multiplier;
                case STONE_SWORD:
                    return 5 * multiplier;
                case GOLD_SWORD:
                case WOOD_SWORD:
                    return 4 * multiplier;
                case DIAMOND_AXE:
                case IRON_AXE:
                case STONE_AXE:
                case GOLD_AXE:
                case WOOD_AXE:
                    return 3 * multiplier;
                case DIAMOND_PICKAXE:
                case IRON_PICKAXE:
                case STONE_PICKAXE:
                case GOLD_PICKAXE:
                case WOOD_PICKAXE:
                    return 2 * multiplier;
                default:
                    return 1 * multiplier;
            }
        }
        return damage;
    }

    public double getArmor(LivingEntity ent) {
        if (armor < 0) {
            // TODO: Enchantments!
            double baseArmor = 0;
            ItemStack helmet = ent.getEquipment().getHelmet();
            if (helmet != null && helmet.getType() == Material.DIAMOND_HELMET) {
                baseArmor += 0.12;
            }
            if (helmet != null && helmet.getType() == Material.GOLD_HELMET) {
                baseArmor += 0.08;
            }
            if (helmet != null && helmet.getType() == Material.IRON_HELMET) {
                baseArmor += 0.08;
            }
            if (helmet != null && helmet.getType() == Material.LEATHER_HELMET) {
                baseArmor += 0.04;
            }
            if (helmet != null && helmet.getType() == Material.CHAINMAIL_HELMET) {
                baseArmor += 0.08;
            }
            ItemStack chestplate = ent.getEquipment().getChestplate();
            if (chestplate != null && chestplate.getType() == Material.DIAMOND_CHESTPLATE) {
                baseArmor += 0.32;
            }
            if (chestplate != null && chestplate.getType() == Material.GOLD_CHESTPLATE) {
                baseArmor += 0.20;
            }
            if (chestplate != null && chestplate.getType() == Material.IRON_CHESTPLATE) {
                baseArmor += 0.24;
            }
            if (chestplate != null && chestplate.getType() == Material.LEATHER_CHESTPLATE) {
                baseArmor += 0.12;
            }
            if (chestplate != null && chestplate.getType() == Material.CHAINMAIL_CHESTPLATE) {
                baseArmor += 0.20;
            }
            ItemStack leggings = ent.getEquipment().getLeggings();
            if (leggings != null && leggings.getType() == Material.DIAMOND_LEGGINGS) {
                baseArmor += 0.24;
            }
            if (leggings != null && leggings.getType() == Material.GOLD_LEGGINGS) {
                baseArmor += 0.12;
            }
            if (leggings != null && leggings.getType() == Material.IRON_LEGGINGS) {
                baseArmor += 0.20;
            }
            if (leggings != null && leggings.getType() == Material.LEATHER_LEGGINGS) {
                baseArmor += 0.08;
            }
            if (leggings != null && leggings.getType() == Material.CHAINMAIL_LEGGINGS) {
                baseArmor += 0.16;
            }
            ItemStack boots = ent.getEquipment().getBoots();
            if (boots != null && boots.getType() == Material.DIAMOND_BOOTS) {
                baseArmor += 0.12;
            }
            if (boots != null && boots.getType() == Material.GOLD_BOOTS) {
                baseArmor += 0.04;
            }
            if (boots != null && boots.getType() == Material.IRON_BOOTS) {
                baseArmor += 0.08;
            }
            if (boots != null && boots.getType() == Material.LEATHER_BOOTS) {
                baseArmor += 0.04;
            }
            if (boots != null && boots.getType() == Material.CHAINMAIL_BOOTS) {
                baseArmor += 0.04;
            }
            return Math.min(baseArmor, 0.80);
        }
        return armor;
    }

    public void punch(LivingEntity entity) {
        npc.faceLocation(entity.getLocation());
        swingWeapon();
        stats_punches++;
        if (SentinelPlugin.instance.getConfig().getBoolean("random.workaround damage", false)) {
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
            entity.damage(getDamage(), getLivingEntity());
        }
    }

    public void chase(LivingEntity entity) {
        if (npc.getNavigator().getTargetType() == TargetType.LOCATION
                && npc.getNavigator().getTargetAsLocation() != null
                && npc.getNavigator().getTargetAsLocation().getWorld().equals(entity.getWorld())
                && npc.getNavigator().getTargetAsLocation().distanceSquared(entity.getLocation()) < 2 * 2) {
            return;
        }
        chasing = entity;
        npc.getNavigator().getDefaultParameters().stuckAction(null);
        npc.getNavigator().setTarget(entity.getLocation());
        npc.getNavigator().getLocalParameters().speedModifier((float)speed);
    }

    public ItemStack getArrow() {
        if (!npc.hasTrait(Inventory.class)) {
            return needsAmmo ? null : new ItemStack(Material.ARROW, 1);
        }
        Inventory inv = npc.getTrait(Inventory.class);
        ItemStack[] items = inv.getContents();
        for (int i = 0; i < items.length; i++) {
            ItemStack item = items[i];
            if (item != null) {
                Material mat = item.getType();
                if (mat == Material.ARROW || mat == Material.TIPPED_ARROW || mat == Material.SPECTRAL_ARROW) {
                    return item.clone();
                }
            }
        }
        return needsAmmo ? null : new ItemStack(Material.ARROW, 1);
    }

    public void reduceDurability() {
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
                if (mat == Material.ARROW || mat == Material.TIPPED_ARROW || mat == Material.SPECTRAL_ARROW) {
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
                if (mat == Material.SNOW_BALL) {
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

    public boolean isWeapon(Material mat) {
        switch (mat) {
            case SPLASH_POTION:
            case LINGERING_POTION:
            case SNOW_BALL:
            case BOW:
            case NETHER_STAR:
            case BLAZE_ROD:
            case DIAMOND_SWORD:
            case GOLD_SWORD:
            case IRON_SWORD:
            case WOOD_SWORD:
            case DIAMOND_PICKAXE:
            case GOLD_PICKAXE:
            case IRON_PICKAXE:
            case WOOD_PICKAXE:
            case DIAMOND_AXE:
            case GOLD_AXE:
            case IRON_AXE:
            case WOOD_AXE:
            case DIAMOND_SPADE:
            case GOLD_SPADE:
            case IRON_SPADE:
            case WOOD_SPADE:
                return true;
            default:
                return false;
        }
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
        ItemStack held = items[0] == null ? null: items[0].clone();
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
        ItemStack held = items[0] == null ? null: items[0].clone();
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
        // TODO: Simplify this code!
        stats_attackAttempts++;
        double dist = getLivingEntity().getEyeLocation().distanceSquared(entity.getEyeLocation());
        if (autoswitch && dist > 3 * 3) {
            swapToRanged();
        }
        else if (autoswitch && dist < 3 * 3) {
            swapToMelee();
        }
        if (usesBow()) {
            if (canSee(entity)) {
                if (timeSinceAttack < attackRate) {
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
                if (timeSinceAttack < attackRate) {
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
                if (timeSinceAttack < attackRate) {
                    if (rangedChase) {
                        rechase();
                    }
                    return;
                }
                timeSinceAttack = 0;
                firePotion(getLivingEntity().getEquipment().getItemInMainHand(),
                        entity.getEyeLocation(), entity.getVelocity());
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
                if (timeSinceAttack < attackRate) {
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
                if (timeSinceAttack < attackRate) {
                    if (rangedChase) {
                        rechase();
                    }
                    return;
                }
                timeSinceAttack = 0;
                swingWeapon();
                entity.getWorld().strikeLightning(entity.getLocation());
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
                if (timeSinceAttack < attackRate) {
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
            if (dist < 3 * 3) {
                if (timeSinceAttack < attackRate) {
                    if (closeChase) {
                        rechase();
                    }
                    return;
                }
                timeSinceAttack = 0;
                // TODO: Damage sword if needed!
                punch(entity);
                if (needsAmmo && shouldTakeDura()) {
                    reduceDurability();
                    grabNextItem();
                }
            }
            else if (closeChase) {
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
        return it != null && it.getType() == Material.BLAZE_ROD;
    }

    public boolean usesSnowball() {
        if (!npc.hasTrait(Inventory.class)) {
            return false;
        }
        ItemStack it = npc.getTrait(Inventory.class).getContents()[0];
        return it != null && it.getType() == Material.SNOW_BALL;
    }

    public boolean usesLightning() {
        if (!npc.hasTrait(Inventory.class)) {
            return false;
        }
        ItemStack it = npc.getTrait(Inventory.class).getContents()[0];
        return it != null && it.getType() == Material.NETHER_STAR;
    }

    public boolean usesSpectral() {
        if (!npc.hasTrait(Inventory.class)) {
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
        return it.getType() == Material.SPLASH_POTION || it.getType() == Material.LINGERING_POTION;
    }

    public boolean shouldTakeDura() {
        Material type = getLivingEntity().getEquipment().getItemInMainHand().getType();
        return type == Material.BOW || type == Material.DIAMOND_SWORD || type == Material.GOLD_SWORD
                || type == Material.IRON_SWORD || type == Material.WOOD_SWORD; // TODO: Tools?
    }

    public boolean shouldTarget(LivingEntity entity) {
        if (entity.getUniqueId().equals(getLivingEntity().getUniqueId())) {
            return false;
        }
        return isTargeted(entity) && !isIgnored(entity);
    }

    public HashSet<SentinelCurrentTarget> currentTargets = new HashSet<SentinelCurrentTarget>();

    private HashSet<UUID> greetedAlready = new HashSet<UUID>();

    public void addTarget(UUID id) {
        SentinelCurrentTarget target = new SentinelCurrentTarget();
        target.targetID = id;
        target.ticksLeft = enemyTargetTime;
        currentTargets.remove(target);
        currentTargets.add(target);
    }

    public boolean isRegexTargeted(String name, List<String> regexes) {
        for (String str: regexes) {
            Pattern pattern = Pattern.compile(str, Pattern.CASE_INSENSITIVE);
            if (pattern.matcher(name).matches()) {
                return true;
            }
        }
        return false;
    }

    public boolean isIgnored(LivingEntity entity) {
        if (entity.hasMetadata("NPC")) {
            return ignores.contains(SentinelTarget.NPCS) ||
                    isRegexTargeted(CitizensAPI.getNPCRegistry().getNPC(entity).getName(), npcNameIgnores);
        }
        else if (entity instanceof Player) {
            if (((Player) entity).getGameMode() == GameMode.CREATIVE || ((Player) entity).getGameMode() == GameMode.SPECTATOR) {
                return true;
            }
            if (getGuarding() != null && entity.getUniqueId().equals(getGuarding())) {
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
        else if (isRegexTargeted(entity.getCustomName() == null ? entity.getType().name(): entity.getCustomName(), entityNameIgnores)) {
            return true;
        }
        if (ignores.contains(SentinelTarget.OWNER) && entity.getUniqueId().equals(npc.getTrait(Owner.class).getOwnerId())) {
            return true;
        }
        HashSet<SentinelTarget> possible = SentinelPlugin.entityToTargets.get(entity.getType());
        for (SentinelTarget poss: possible) {
            if (ignores.contains(poss)) {
                return true;
            }
        }
        if (entity.getEquipment() != null && entity.getEquipment().getItemInMainHand() != null
                && isRegexTargeted(entity.getEquipment().getItemInMainHand().getType().name(), heldItemIgnores)) {
            return true;
        }
        return false;
    }

    public boolean isTargeted(LivingEntity entity) {
        SentinelCurrentTarget target = new SentinelCurrentTarget();
        target.targetID = entity.getUniqueId();
        if (currentTargets.contains(target)) {
            return true;
        }
        if (entity.hasMetadata("NPC")) {
            return targets.contains(SentinelTarget.NPCS) ||
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
        else if (isRegexTargeted(entity.getCustomName() == null ? entity.getType().name(): entity.getCustomName(), entityNameTargets)) {
            return true;
        }
        if (targets.contains(SentinelTarget.OWNER) && entity.getUniqueId().equals(npc.getTrait(Owner.class).getOwnerId())) {
            return true;
        }
        HashSet<SentinelTarget> possible = SentinelPlugin.entityToTargets.get(entity.getType());
        for (SentinelTarget poss: possible) {
            if (targets.contains(poss)) {
                return true;
            }
        }
        if (entity.getEquipment() != null && entity.getEquipment().getItemInMainHand() != null
                && isRegexTargeted(entity.getEquipment().getItemInMainHand().getType().name(), heldItemTargets)) {
            return true;
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
        LivingEntity closest = null;
        for (LivingEntity ent: getLivingEntity().getWorld().getLivingEntities()) {
            if ((ignoreGlow && ent.isGlowing()) || ent.isDead()) {
                continue;
            }
            double dist = ent.getEyeLocation().distanceSquared(pos);
            SentinelCurrentTarget sct = new SentinelCurrentTarget();
            sct.targetID = ent.getUniqueId();
            if ((dist < rangesquared && shouldTarget(ent) && canSee(ent)) || (dist < crsq && currentTargets.contains(sct)) && canSee(ent)) {
                rangesquared = dist;
                closest = ent;
            }
        }
        return closest;
    }

    public long timeSinceAttack = 0;

    public long timeSinceHeal = 0;

    private Entity getEntityForID(UUID id) {
        // TODO: Remove NMS here!
        net.minecraft.server.v1_10_R1.Entity nmsEntity = ((CraftWorld) getLivingEntity().getWorld()).getHandle().getEntity(id);
        if (nmsEntity != null) {
            return nmsEntity.getBukkitEntity();
        }
        return null;
    }

    private void updateTargets() {
        for (SentinelCurrentTarget uuid : new HashSet<SentinelCurrentTarget>(currentTargets)) {
            Entity e = getEntityForID(uuid.targetID);
            if (e == null) {
                currentTargets.remove(uuid);
                continue;
            }
            double d = e.getLocation().distanceSquared(getLivingEntity().getLocation());
            if (d > range * range * 4 && d > chaseRange * chaseRange * 4) {
                currentTargets.remove(uuid);
            }
            else if (uuid.ticksLeft > 0) {
                uuid.ticksLeft -= SentinelPlugin.instance.tickRate;
                if (uuid.ticksLeft <= 0) {
                    currentTargets.remove(uuid);
                }
            }
        }
    }

    public void runUpdate() {
        timeSinceAttack += SentinelPlugin.instance.tickRate;
        timeSinceHeal += SentinelPlugin.instance.tickRate;
        if (getLivingEntity().getLocation().getY() <= 0) {
            getLivingEntity().damage(1);
            if (!npc.isSpawned()) {
                return;
            }
        }
        if (healRate > 0 && timeSinceHeal > healRate && getLivingEntity().getHealth() < health) {
            getLivingEntity().setHealth(Math.min(getLivingEntity().getHealth() + 1.0, health));
            timeSinceHeal = 0;
        }
        LivingEntity target = findBestTarget();
        chasing = target;
        if (target != null) {
            tryAttack(target);
        }
        updateTargets();
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
                    npc.getNavigator().getDefaultParameters().range(100);
                    npc.getNavigator().getDefaultParameters().stuckAction(TeleportStuckAction.INSTANCE);
                    npc.getNavigator().setTarget(player.getLocation());
                    npc.getNavigator().getLocalParameters().speedModifier((float)speed);
                }
            }
        }
        else if (chaseRange > 0) {
            Location near = nearestPathPoint();
            if (near != null && near.distanceSquared(getLivingEntity().getLocation()) > chaseRange * chaseRange) {
                npc.getNavigator().getDefaultParameters().stuckAction(TeleportStuckAction.INSTANCE);
                npc.getNavigator().setTarget(near);
                npc.getNavigator().getLocalParameters().speedModifier((float)speed);
            }
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
        if (!npc.hasTrait(Waypoints.class)) {
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
            boolean enemy = isTargeted(event.getPlayer()) && !isIgnored(event.getPlayer());
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

    public HashMap<UUID, Boolean> needsDropsClear = new HashMap<UUID, Boolean>();

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
                for (ItemStack item: drops) {
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
                            return;
                        }
                        timer += 10;
                    }
                    else {
                        this.cancel();
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
