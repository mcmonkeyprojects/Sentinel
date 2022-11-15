package org.mcmonkey.sentinel.targeting;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.trait.Owner;
import net.citizensnpcs.api.util.DataKey;
import org.bukkit.ChatColor;
import org.bukkit.entity.*;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.projectiles.ProjectileSource;
import org.mcmonkey.sentinel.SentinelIntegration;
import org.mcmonkey.sentinel.SentinelPlugin;
import org.mcmonkey.sentinel.SentinelTrait;
import org.mcmonkey.sentinel.SentinelUtilities;
import org.mcmonkey.sentinel.commands.SentinelCommand;

import java.util.*;

public class SentinelTargetList {

    /**
     * Returns a duplicate of the target list, with all inner arrays duplicated.
     */
    public SentinelTargetList duplicate() {
        SentinelTargetList result = new SentinelTargetList();
        result.targets.addAll(targets);
        result.byPlayerName.addAll(byPlayerName);
        result.byNpcName.addAll(byNpcName);
        result.byEntityName.addAll(byEntityName);
        result.byHeldItem.addAll(byHeldItem);
        result.byOffhandItem.addAll(byOffhandItem);
        result.byEquippedItem.addAll(byEquippedItem);
        result.byInventoryItem.addAll(byInventoryItem);
        result.byGroup.addAll(byGroup);
        result.byEvent.addAll(byEvent);
        result.byStatus.addAll(byStatus);
        result.byOther.addAll(byOther);
        result.byMultiple.addAll(byMultiple);
        result.byAllInOne.addAll(byAllInOne);
        return result;
    }

    /**
     * Initialize the targets list after loading it.
     */
    public void init() {
        recalculateTargetsCache();
        for (String str : new ArrayList<>(byEvent)) {
            if (str.startsWith("message,")) {
                byEvent.remove(str);
                byEvent.add("message:" + str.substring("message,".length()));
            }
        }
    }

    /**
     * Returns whether an entity is targeted by this target list on a specific Sentinel NPC.
     * Does not include target-list-specific handling, such as current temporary targets.
     */
    public boolean isTarget(LivingEntity entity, SentinelTrait sentinel) {
        checkRecalculateTargetsCache();
        if (targetsProcessed.contains(SentinelTarget.OWNER) && SentinelUtilities.uuidEquals(entity.getUniqueId(), sentinel.getNPC().getOrAddTrait(Owner.class).getOwnerId())) {
            return true;
        }
        return isTargetNoCache(entity);
    }

    /**
     * Returns whether an entity is targeted by this target list.
     * Does not account for NPC-specific target handlers, like 'owner' (which requires knowledge of who that owner is, based on which NPC is checking).
     * To include that, use isTarget(LivingEntity, SentinelTrait)
     */
    public boolean isTarget(LivingEntity entity) {
        checkRecalculateTargetsCache();
        return isTargetNoCache(entity);
    }

    /**
     * Returns whether an entity is targeted by this target list.
     * Does not account for NPC-specific target handlers, like 'owner' (which requires knowledge of who that owner is, based on which NPC is checking).
     * To include that, use isTarget(LivingEntity, SentinelTrait)
     *
     * Explicitly does not reprocess the cache.
     */
    public boolean isTargetNoCache(LivingEntity entity) {
        if (totalTargetsCount() == 0) { // Opti
            return false;
        }
        if (entity.getEquipment() != null) {
            if (SentinelUtilities.isItemTarget(SentinelUtilities.getHeldItem(entity), byHeldItem)) {
                return true;
            }
            if (SentinelUtilities.isItemTarget(SentinelUtilities.getOffhandItem(entity), byOffhandItem)) {
                return true;
            }
            if (!byEquippedItem.isEmpty()) {
                if (SentinelUtilities.isItemTarget(entity.getEquipment().getHelmet(), byEquippedItem)) {
                    return true;
                }
                if (SentinelUtilities.isItemTarget(entity.getEquipment().getChestplate(), byEquippedItem)) {
                    return true;
                }
                if (SentinelUtilities.isItemTarget(entity.getEquipment().getLeggings(), byEquippedItem)) {
                    return true;
                }
                if (SentinelUtilities.isItemTarget(entity.getEquipment().getBoots(), byEquippedItem)) {
                    return true;
                }
            }
        }
        if (entity instanceof InventoryHolder && !byInventoryItem.isEmpty()) {
            for (ItemStack item : ((InventoryHolder) entity).getInventory().getStorageContents()) {
                if (SentinelUtilities.isItemTarget(item, byInventoryItem)) {
                    return true;
                }
            }
        }
        for (ArrayList<CachedOtherTarget> targets : otherTargetCache.values()) {
            for (CachedOtherTarget target : targets) {
                if (target.integration.isTarget(entity, target.prefix, target.value)) {
                    return true;
                }
            }
        }
        for (SentinelTargetList allInOne : byAllInOne) {
            SentinelTargetList subList = allInOne.duplicate();
            subList.recalculateCacheNoClear();
            if (SentinelPlugin.debugMe) {
                SentinelPlugin.instance.getLogger().info("All-In-One Debug: " + subList.totalTargetsCount() + " at start: " + subList.toMultiTargetString());
            }
            while (subList.ifIsTargetDeleteTarget(entity)) {
            }
            if (subList.totalTargetsCount() == 0) {
                return true;
            }
            if (SentinelPlugin.debugMe) {
                SentinelPlugin.instance.getLogger().info("All-In-One Debug: " + subList.totalTargetsCount() + " left: " + subList.toMultiTargetString());
            }
        }
        // Any NPCs cause instant return - things below should be non-NPC only target types
        if (entity.hasMetadata("NPC")) {
            if (targetsProcessed.contains(SentinelTarget.NPCS)) {
                return true;
            }
            NPC theNPC = CitizensAPI.getNPCRegistry().getNPC(entity);
            if (theNPC == null) { // ???
                return false;
            }
            return SentinelUtilities.isRegexTargeted(theNPC.getName(), byNpcName);
        }
        if (entity instanceof Player) {
            if (SentinelUtilities.isRegexTargeted(((Player) entity).getName(), byPlayerName)) {
                return true;
            }
            if (SentinelPlugin.instance.vaultPerms != null) {
                for (String group : byGroup) {
                    if (SentinelPlugin.instance.vaultPerms.playerInGroup((Player) entity, group)) {
                        return true;
                    }
                }
            }
        }
        else if (SentinelUtilities.isRegexTargeted(entity.getCustomName() == null ? entity.getType().name() : entity.getCustomName(), byEntityName)) {
            return true;
        }
        if (byStatus.contains("angry") && entity instanceof Mob && ((Mob) entity).getTarget() != null) {
            return true;
        }
        if (byStatus.contains("passive") && entity instanceof Mob && ((Mob) entity).getTarget() == null) {
            return true;
        }
        HashSet<SentinelTarget> possible = SentinelPlugin.entityToTargets.get(entity.getType());
        if (possible != null) {
            for (SentinelTarget poss : possible) {
                if (targetsProcessed.contains(poss)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * This is a special target method, that will remove the target from the targets list if it's matched.
     * Primarily for the multi-targets system.
     */
    public boolean ifIsTargetDeleteTarget(LivingEntity entity) {
        if (totalTargetsCount() == 0) { // Opti
            return false;
        }
        if (entity.getEquipment() != null) {
            String match = SentinelUtilities.getItemTarget(SentinelUtilities.getHeldItem(entity), byHeldItem);
            if (match != null) {
                byHeldItem.remove(match);
                return true;
            }
            match = SentinelUtilities.getItemTarget(SentinelUtilities.getOffhandItem(entity), byOffhandItem);
            if (match != null) {
                byOffhandItem.remove(match);
                return true;
            }
            if (!byEquippedItem.isEmpty()) {
                match = SentinelUtilities.getItemTarget(entity.getEquipment().getHelmet(), byEquippedItem);
                if (match != null) {
                    byEquippedItem.remove(match);
                    return true;
                }
                match = SentinelUtilities.getItemTarget(entity.getEquipment().getChestplate(), byEquippedItem);
                if (match != null) {
                    byEquippedItem.remove(match);
                    return true;
                }
                match = SentinelUtilities.getItemTarget(entity.getEquipment().getLeggings(), byEquippedItem);
                if (match != null) {
                    byEquippedItem.remove(match);
                    return true;
                }
                match = SentinelUtilities.getItemTarget(entity.getEquipment().getBoots(), byEquippedItem);
                if (match != null) {
                    byEquippedItem.remove(match);
                    return true;
                }
            }
        }
        if (entity instanceof InventoryHolder && !byInventoryItem.isEmpty()) {
            for (ItemStack item : ((InventoryHolder) entity).getInventory().getStorageContents()) {
                String match = SentinelUtilities.getItemTarget(item, byInventoryItem);
                if (match != null) {
                    byInventoryItem.remove(match);
                    return true;
                }
            }
        }
        for (Map.Entry<String, ArrayList<CachedOtherTarget>> targets : otherTargetCache.entrySet()) {
            for (CachedOtherTarget target : targets.getValue()) {
                if (target.integration.isTarget(entity, target.prefix, target.value)) {
                    byOther.remove(target.prefix + ":" + target.value);
                    recalculateCacheNoClear();
                    return true;
                }
            }
        }
        for (SentinelTargetList allInOne : byAllInOne) {
            SentinelTargetList subList = allInOne.duplicate();
            subList.recalculateCacheNoClear();
            while (subList.ifIsTargetDeleteTarget(entity)) {
            }
            if (subList.totalTargetsCount() == 0) {
                byAllInOne.remove(allInOne);
                return true;
            }
        }
        // Any NPCs cause instant return - things below should be non-NPC only target types
        if (entity.hasMetadata("NPC")) {
            if (targetsProcessed.contains(SentinelTarget.NPCS)) {
                for (String target : targets) {
                    if (SentinelTarget.forName(target) == SentinelTarget.NPCS) {
                        targets.remove(target);
                        recalculateCacheNoClear();
                        return true;
                    }
                }
                targetsProcessed.remove(SentinelTarget.NPCS);
                return true;
            }
            String match = SentinelUtilities.getRegexTarget(CitizensAPI.getNPCRegistry().getNPC(entity).getName(), byNpcName);
            if (match != null) {
                byNpcName.remove(match);
                return true;
            }
            return false;
        }
        if (entity instanceof Player) {
            String match = SentinelUtilities.getRegexTarget(((Player) entity).getName(), byPlayerName);
            if (match != null) {
                byPlayerName.remove(match);
                return true;
            }
            if (SentinelPlugin.instance.vaultPerms != null) {
                for (String group : byGroup) {
                    if (SentinelPlugin.instance.vaultPerms.playerInGroup((Player) entity, group)) {
                        byGroup.remove(group);
                        return true;
                    }
                }
            }
        }
        else {
            String match = SentinelUtilities.getRegexTarget(entity.getCustomName() == null ? entity.getType().name() : entity.getCustomName(), byEntityName);
            if (match != null) {
                byEntityName.remove(match);
                return true;
            }
        }
        if (byStatus.contains("angry") && entity instanceof Mob && ((Mob) entity).getTarget() != null) {
            byStatus.remove("angry");
            return true;
        }
        if (byStatus.contains("passive") && entity instanceof Mob && ((Mob) entity).getTarget() == null) {
            byStatus.remove("passive");
            return true;
        }
        HashSet<SentinelTarget> possible = SentinelPlugin.entityToTargets.get(entity.getType());
        for (SentinelTarget poss : possible) {
            if (targetsProcessed.contains(poss)) {
                for (String target : targets) {
                    if (SentinelTarget.forName(target) == poss) {
                        targets.remove(target);
                        recalculateCacheNoClear();
                        return true;
                    }
                }
                targetsProcessed.remove(poss);
                return true;
            }
        }
        return false;
    }

    /**
     * Fills the target list from a Citizens data key, during load time.
     */
    public void fillListFromKey(ArrayList<String> list, DataKey key) {
        for (DataKey listEntry : key.getSubKeys()) {
            list.add(listEntry.getRaw("").toString());
        }
    }

    /**
     * Updates old (Sentinel 1.6 or lower) saves to new (Sentinel 1.7 or higher) saves.
     */
    public void updateOld(DataKey key, String name) {
        if (name.equals("playerName")) {
            fillListFromKey(byPlayerName, key);
        }
        else if (name.equals("npcName")) {
            fillListFromKey(byNpcName, key);
        }
        else if (name.equals("entityName")) {
            fillListFromKey(byEntityName, key);
        }
        else if (name.equals("heldItem")) {
            fillListFromKey(byHeldItem, key);
        }
        else if (name.equals("group")) {
            fillListFromKey(byGroup, key);
        }
        else if (name.equals("event")) {
            fillListFromKey(byEvent, key);
        }
        else if (name.equals("other")) {
            fillListFromKey(byOther, key);
        }
    }

    /**
     * Returns whether a chat event is targeted by this list.
     */
    public boolean isEventTarget(SentinelTrait sentinel, AsyncPlayerChatEvent event) {
        if (!sentinel.targetingHelper.canSee(event.getPlayer())) {
            return false;
        }
        for (String str : byEvent) {
            if (str.startsWith("message:")) {
                String messageCheck = str.substring("message:".length());
                if (event.getMessage().toLowerCase().contains(messageCheck.toLowerCase())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns whether the victim in a damage event is targeted by this list.
     */
    public boolean isReverseEventTarget(SentinelTrait sentinel, EntityDamageByEntityEvent event) {
        if (byEvent.contains("guarded_fight")
                && sentinel.getGuarding() != null
                && SentinelUtilities.uuidEquals(event.getDamager().getUniqueId(), sentinel.getGuarding())) {
            return true;
        }
        return false;
    }

    /**
     * Returns whether the damager in a damage event is targeted by this list.
     */
    public boolean isEventTarget(EntityDamageByEntityEvent event) {
        Entity damager = event.getDamager();
        if (event.getDamager() instanceof Projectile) {
            ProjectileSource source = ((Projectile) event.getDamager()).getShooter();
            if (source instanceof Entity) {
                damager = (Entity) source;
            }
        }
        if (CitizensAPI.getNPCRegistry().isNPC(damager)) {
            return false;
        }
        if (damager.equals(event.getEntity())) {
            return false; // Players can accidentally hurt themselves - that's not PvP
        }
        for (String evt : byEvent) {
            if (evt.equals("pvp")
                    && event.getEntity() instanceof Player
                    && damager instanceof Player
                    && !CitizensAPI.getNPCRegistry().isNPC(event.getEntity())) {
                return true;
            }
            else if (evt.equals("pve")
                    && !(event.getEntity() instanceof Player)
                    && damager instanceof Player
                    && event.getEntity() instanceof LivingEntity) {
                return true;
            }
            else if (evt.equals("eve")
                    && !(damager instanceof Player)
                    && !(event.getEntity() instanceof Player)
                    && event.getEntity() instanceof LivingEntity) {
                return true;
            }
            else if (evt.equals("pvnpc")
                    && event.getEntity() instanceof LivingEntity
                    && damager instanceof Player
                    && CitizensAPI.getNPCRegistry().isNPC(event.getEntity())) {
                return true;
            }
            else if (evt.equals("pvsentinel")
                    && event.getEntity() instanceof LivingEntity
                    && damager instanceof Player
                    && CitizensAPI.getNPCRegistry().isNPC(event.getEntity())
                    && CitizensAPI.getNPCRegistry().getNPC(event.getEntity()).hasTrait(SentinelTrait.class)) {
                return true;
            }
            if (evt.contains(":")) {
                int colon = evt.indexOf(':');
                String prefix = evt.substring(0, colon);
                String value = evt.substring(colon + 1);
                if (prefix.equals("pv")) {
                    SentinelTarget target = SentinelTarget.forName(value);
                    if (target != null) {
                        if (damager instanceof Player
                                && event.getEntity() instanceof LivingEntity
                                && target.isTarget((LivingEntity) event.getEntity())) {
                            return true;
                        }
                    }
                }
                else if (prefix.equals("ev")) {
                    SentinelTarget target = SentinelTarget.forName(value);
                    if (target != null) {
                        if (!(damager instanceof Player)
                                && event.getEntity() instanceof LivingEntity
                                && target.isTarget((LivingEntity) event.getEntity())) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * Cache of target objects.
     */
    public HashSet<SentinelTarget> targetsProcessed = new HashSet<>();

    /**
     * Represents an "other target" for use with caching.
     */
    public static class CachedOtherTarget {

        /**
         * The integration object.
         */
        public SentinelIntegration integration;

        /**
         * The "other" target prefix.
         */
        public String prefix;

        /**
         * The "other" target value.
         */
        public String value;
    }

    /**
     * Cache of "other" targets.
     */
    public HashMap<String, ArrayList<CachedOtherTarget>> otherTargetCache = new HashMap<>();

    private int otherTargetSize = 0;

    /**
     * Checks if the targets cache ('targetsProcessed') needs to be reprocessed, and refills it if so.
     */
    public void checkRecalculateTargetsCache() {
        if (targets.size() != targetsProcessed.size() || byOther.size() != otherTargetSize) {
            recalculateTargetsCache();
        }
    }

    /**
     * Fills the cache 'targetsProcessed' set and does not deduplicate the source list.
     * Also fills the 'otherTargetCache'.
     */
    public void recalculateCacheNoClear() {
        targetsProcessed.clear();
        for (String target : targets) {
            targetsProcessed.add(SentinelTarget.forName(target));
        }
        otherTargetCache.clear();
        for (String otherTarget : byOther) {
            int colon = otherTarget.indexOf(':');
            String before = otherTarget.substring(0, colon);
            String after = otherTarget.substring(colon + 1);
            SentinelIntegration integration = SentinelPlugin.integrationPrefixMap.get(before);
            if (integration != null) {
                ArrayList<CachedOtherTarget> subList = otherTargetCache.get(before);
                if (subList == null) {
                    subList = new ArrayList<>();
                    otherTargetCache.put(before, subList);
                }
                CachedOtherTarget targ = new CachedOtherTarget();
                targ.integration = integration;
                targ.prefix = before;
                targ.value = after;
                subList.add(targ);
            }
        }
        otherTargetSize = byOther.size();
    }

    /**
     * Fills the cache 'targetsProcessed' set then uses that set to deduplicate the source 'targets' set.
     * Also fills the 'otherTargetCache'.
     */
    public void recalculateTargetsCache() {
        recalculateCacheNoClear();
        targets.clear();
        for (SentinelTarget target : targetsProcessed) {
            targets.add(target.name());
        }
    }

    /**
     * Returns the total count of targets (other than multi-targets).
     */
    public int totalTargetsCount() {
        return targets.size() + byPlayerName.size() + byNpcName.size() + byEntityName.size()
                + byHeldItem.size() + byOffhandItem.size() + byEquippedItem.size() + byInventoryItem.size()
                + byGroup.size() + byEvent.size() + byStatus.size() + byOther.size() + byAllInOne.size();
    }

    private static void addList(StringBuilder builder, ArrayList<String> strs, String prefix) {
        if (!strs.isEmpty()) {
            for (String str : strs) {
                if (prefix != null) {
                    builder.append(prefix).append(":");
                }
                builder.append(str).append(SentinelCommand.colorBasic).append(" ").append((char) 0x01).append(" ").append(SentinelCommand.colorEmphasis);
            }
        }
    }

    /**
     * Forms a \0x01-separated list for all-in-one-target output.
     */
    public String toComboString() {
        StringBuilder sb = new StringBuilder();
        addList(sb, targets, null);
        addList(sb, byPlayerName, "player");
        addList(sb, byNpcName, "npc");
        addList(sb, byEntityName, "entityname");
        addList(sb, byHeldItem, "helditem");
        addList(sb, byOffhandItem, "offhand");
        addList(sb, byEquippedItem, "equipped");
        addList(sb, byInventoryItem, "in_inventory");
        addList(sb, byGroup, "group");
        addList(sb, byEvent, "event");
        addList(sb, byStatus, "status");
        addList(sb, byOther, null);
        if (!byAllInOne.isEmpty()) {
            for (SentinelTargetList list : byAllInOne) {
                sb.append("allinone:").append(list.toAllInOneString()).append(SentinelCommand.colorBasic)
                        .append(" ").append((char) 0x01).append(" ").append(SentinelCommand.colorEmphasis);
            }
        }
        if (sb.length() == 0) {
            return "";
        }
        return sb.substring(0, sb.length() - (SentinelCommand.colorBasic + " . " + SentinelCommand.colorEmphasis).length());
    }

    private static void addRemovableString(ArrayList<String> output, ArrayList<String> targets, String prefix) {
        for (String target : targets) {
            if (prefix == null) {
                output.add(target.toLowerCase());
            }
            else {
                output.add(prefix + ":" + target.toLowerCase());
            }
        }
    }

    /**
     * Gets a list of target strings, fit for input to a remove command.
     */
    public List<String> getTargetRemovableStrings() {
        ArrayList<String> output = new ArrayList<>();
        addRemovableString(output, targets, null);
        addRemovableString(output, byPlayerName, "player");
        addRemovableString(output, byNpcName, "npc");
        addRemovableString(output, byEntityName, "entityname");
        addRemovableString(output, byHeldItem, "helditem");
        addRemovableString(output, byOffhandItem, "offhand");
        addRemovableString(output, byEquippedItem, "equipped");
        addRemovableString(output, byInventoryItem, "in_inventory");
        addRemovableString(output, byGroup, "group");
        addRemovableString(output, byEvent, "event");
        addRemovableString(output, byStatus, "status");
        addRemovableString(output, byOther, null);
        for (SentinelTargetList list : byAllInOne) {
            output.add("allinone:" + ChatColor.stripColor(list.toAllInOneString().toLowerCase().replace(" ", "")));
        }
        for (SentinelTargetList list : byMultiple) {
            output.add("multi:" + ChatColor.stripColor(list.toMultiTargetString().toLowerCase().replace(" ", "")));
        }
        return output;
    }

    /**
     * Forms a comma-separated list for multi-target output.
     */
    public String toMultiTargetString() {
        return toComboString().replace((char) 0x01, ',');
    }

    /**
     * Forms a pipe-separated list for all-in-one-target output.
     */
    public String toAllInOneString() {
        return toComboString().replace((char) 0x01, '|');
    }

    /**
     * Helper list, general ignorable.
     */
    public ArrayList<LivingEntity> tempTargeted = new ArrayList<>();

    /**
     * List of target-type-based targets.
     */
    @Persist("targets")
    public ArrayList<String> targets = new ArrayList<>();

    /**
     * List of player-name-based targets.
     */
    @Persist("byPlayerName")
    public ArrayList<String> byPlayerName = new ArrayList<>();

    /**
     * List of NPC-name-based targets.
     */
    @Persist("byNpcName")
    public ArrayList<String> byNpcName = new ArrayList<>();

    /**
     * List of entity-name-based targets.
     */
    @Persist("byEntityName")
    public ArrayList<String> byEntityName = new ArrayList<>();

    /**
     * List of held-item-based targets.
     */
    @Persist("byHeldItem")
    public ArrayList<String> byHeldItem = new ArrayList<>();

    /**
     * List of offhand-item-based targets.
     */
    @Persist("byOffhandItem")
    public ArrayList<String> byOffhandItem = new ArrayList<>();

    /**
     * List of equipped-item-based targets.
     */
    @Persist("byEquippedItem")
    public ArrayList<String> byEquippedItem = new ArrayList<>();

    /**
     * List of inventory-item-based targets.
     */
    @Persist("byInventoryItem")
    public ArrayList<String> byInventoryItem = new ArrayList<>();

    /**
     * List of scoreboard-group-based targets.
     */
    @Persist("byGroup")
    public ArrayList<String> byGroup = new ArrayList<>();

    /**
     * List of event-based targets.
     */
    @Persist("byEvent")
    public ArrayList<String> byEvent = new ArrayList<>();

    /**
     * List of targets handled by special status.
     */
    @Persist("byStatus")
    public ArrayList<String> byStatus = new ArrayList<>();

    /**
     * List of targets not handled by any other target type list.
     */
    @Persist("byOther")
    public ArrayList<String> byOther = new ArrayList<>();

    /**
     * List of target lists that need to be matched in full on exactly one entity to qualify as a match.
     */
    @Persist("byAllInOne")
    public ArrayList<SentinelTargetList> byAllInOne = new ArrayList<>();

    /**
     * List of target lists that need to be matched in full to qualify as a match.
     */
    @Persist("byMultiple")
    public ArrayList<SentinelTargetList> byMultiple = new ArrayList<>();
}
