package org.mcmonkey.sentinel.targeting;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.trait.Owner;
import net.citizensnpcs.api.util.DataKey;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.mcmonkey.sentinel.SentinelIntegration;
import org.mcmonkey.sentinel.SentinelPlugin;
import org.mcmonkey.sentinel.SentinelTrait;
import org.mcmonkey.sentinel.SentinelUtilities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class SentinelTargetList {

    /**
     * Returns whether an entity is targeted by this target list on a specific Sentinel NPC.
     * Does not include target-list-specific handling, such as current temporary targets.
     */
    public boolean isTarget(LivingEntity entity, SentinelTrait sentinel) {
        checkRecalculateTargetsCache();
        if (targetsProcessed.contains(SentinelTarget.OWNER) && entity.getUniqueId().equals(sentinel.getNPC().getTrait(Owner.class).getOwnerId())) {
            return true;
        }
        return isTarget(entity);
    }

    /**
     * Returns whether an entity is targeted by this target list.
     * Does not account for NPC-specific target handlers, like 'owner' (which requires knowledge of who that owner is, based on which NPC is checking).
     * To include that, use isTarget(LivingEntity, SentinelTrait)
     */
    public boolean isTarget(LivingEntity entity) {
        checkRecalculateTargetsCache();
        if (SentinelTarget.v1_9) {
            if (entity.getEquipment() != null && entity.getEquipment().getItemInMainHand() != null
                    && SentinelUtilities.isRegexTargeted(entity.getEquipment().getItemInMainHand().getType().name(), byHeldItem)) {
                return true;
            }
        }
        else {
            if (entity.getEquipment() != null && entity.getEquipment().getItemInHand() != null
                    && SentinelUtilities.isRegexTargeted(entity.getEquipment().getItemInHand().getType().name(), byHeldItem)) {
                return true;
            }
        }
        for (ArrayList<CachedOtherTarget> targets : otherTargetCache.values()) {
            for (CachedOtherTarget target : targets) {
                if (target.integration.isTarget(entity, target.prefix, target.value)) {
                    return true;
                }
            }
        }
        if (entity.hasMetadata("NPC")) {
            return targetsProcessed.contains(SentinelTarget.NPCS) ||
                    SentinelUtilities.isRegexTargeted(CitizensAPI.getNPCRegistry().getNPC(entity).getName(), byNpcName);
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
        HashSet<SentinelTarget> possible = SentinelPlugin.entityToTargets.get(entity.getType());
        for (SentinelTarget poss : possible) {
            if (targetsProcessed.contains(poss)) {
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
     * Returns whether a damage event is targeted by this list.
     */
    public boolean isEventTarget(EntityDamageByEntityEvent event) {
        if (byEvent.contains("pvp")
                && event.getEntity() instanceof Player
                && !CitizensAPI.getNPCRegistry().isNPC(event.getEntity())) {
            return true;
        }
        else if (byEvent.contains("pve")
                && !(event.getEntity() instanceof Player)
                && event.getEntity() instanceof LivingEntity) {
            return true;
        }
        else if (byEvent.contains("pvnpc")
                && event.getEntity() instanceof LivingEntity
                && CitizensAPI.getNPCRegistry().isNPC(event.getEntity())) {
            return true;
        }
        else if (byEvent.contains("pvsentinel")
                && event.getEntity() instanceof LivingEntity
                && CitizensAPI.getNPCRegistry().isNPC(event.getEntity())
                && CitizensAPI.getNPCRegistry().getNPC(event.getEntity()).hasTrait(SentinelTrait.class)) {
            return true;
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
     * Fills the cache 'targetsProcessed' set then uses that set to deduplicate the source 'targets' set.
     * Also fills the 'otherTargetCache'.
     */
    public void recalculateTargetsCache() {
        targetsProcessed.clear();
        for (String target : targets) {
            targetsProcessed.add(SentinelTarget.forName(target));
        }
        targets.clear();
        for (SentinelTarget target : targetsProcessed) {
            targets.add(target.name());
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
     * List of target-type-based targets.
     */
    @Persist("targets")
    public HashSet<String> targets = new HashSet<>();

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
     * List of targets not handled by any other target type list.
     */
    @Persist("byOther")
    public ArrayList<String> byOther = new ArrayList<>();
}
