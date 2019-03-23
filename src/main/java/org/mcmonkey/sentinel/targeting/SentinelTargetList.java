package org.mcmonkey.sentinel.targeting;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.trait.Owner;
import net.citizensnpcs.api.util.DataKey;
import org.bukkit.ChatColor;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.mcmonkey.sentinel.SentinelIntegration;
import org.mcmonkey.sentinel.SentinelPlugin;
import org.mcmonkey.sentinel.SentinelTrait;
import org.mcmonkey.sentinel.SentinelUtilities;
import org.mcmonkey.sentinel.commands.SentinelCommand;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

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
        result.byGroup.addAll(byGroup);
        result.byEvent.addAll(byEvent);
        result.byOther.addAll(byOther);
        result.byMultiple.addAll(byMultiple);
        result.byAllInOne.addAll(byAllInOne);
        return result;
    }

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
        if (entity.getEquipment() != null && SentinelUtilities.getHeldItem(entity) != null
                && SentinelUtilities.isRegexTargeted(SentinelUtilities.getHeldItem(entity).getType().name(), byHeldItem)) {
            return true;
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
     * This is a special target method, that will remove the target from the targets list if it's matched.
     * Primarily for the multi-targets system.
     */
    public boolean ifIsTargetDeleteTarget(LivingEntity entity) {
        if (entity.getEquipment() != null && SentinelUtilities.getHeldItem(entity) != null) {
            String match = SentinelUtilities.getRegexTarget(SentinelUtilities.getHeldItem(entity).getType().name(), byHeldItem);
            if (match != null) {
                byHeldItem.remove(match);
                return true;
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
    public boolean isEventTarget(AsyncPlayerChatEvent event) {
        for (String str : byEvent) {
            if (str.startsWith("message,")) {
                String messageCheck = str.substring("message,".length());
                if (event.getMessage().toLowerCase().contains(messageCheck.toLowerCase())) {
                    return true;
                }
            }
        }
        return false;
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
                + byHeldItem.size() + byGroup.size() + byEvent.size() + byOther.size() + byAllInOne.size();
    }

    private static void addList(StringBuilder builder, ArrayList<String> strs, String prefix) {
        if (!strs.isEmpty()) {
            for (String str : strs) {
                if (prefix != null) {
                    builder.append(prefix).append(":");
                }
                builder.append(str).append(SentinelCommand.colorBasic).append(" ").append((char) 0x01).append(" ").append(ChatColor.AQUA);
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
        addList(sb, byGroup, "group");
        addList(sb, byEvent, "event");
        addList(sb, byOther, null);
        if (!byAllInOne.isEmpty()) {
            for (SentinelTargetList list : byAllInOne) {
                sb.append("allinone:").append(list.toAllInOneString()).append(SentinelCommand.colorBasic)
                        .append(" ").append((char) 0x01).append(" ").append(ChatColor.AQUA);
            }
        }
        if (sb.length() == 0) {
            return "";
        }
        return sb.substring(0, sb.length() - (SentinelCommand.colorBasic + " . " + ChatColor.AQUA.toString()).length());
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
