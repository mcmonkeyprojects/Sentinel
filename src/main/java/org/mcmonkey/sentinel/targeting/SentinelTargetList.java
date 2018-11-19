package org.mcmonkey.sentinel.targeting;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.util.DataKey;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.mcmonkey.sentinel.SentinelIntegration;
import org.mcmonkey.sentinel.SentinelPlugin;
import org.mcmonkey.sentinel.SentinelUtilities;

import java.util.ArrayList;
import java.util.HashSet;

public class SentinelTargetList {

    /**
     * Returns whether an entity is targeted by this target list.
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
        for (SentinelIntegration integration : SentinelPlugin.integrations) {
            for (String text : byOther) {
                if (integration.isTarget(entity, text)) {
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

    public void fillListFromKey(ArrayList<String> list, DataKey key) {
        for (DataKey listEntry : key.getSubKeys()) {
            list.add(listEntry.getRaw("").toString());
        }
    }

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
     * Cache of target objects.
     */
    public HashSet<SentinelTarget> targetsProcessed = new HashSet<>();

    /**
     * Checks if the targets cache ('targetsProcessed') needs to be reprocessed, and refills it if so.
     */
    public void checkRecalculateTargetsCache() {
        if (targets.size() != targetsProcessed.size()) {
            recalculateTargetsCache();
        }
    }

    /**
     * Fills the cache 'targetsProcessed' set then uses that set to deduplicate the source 'targets' set.
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
