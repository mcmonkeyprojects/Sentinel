package org.mcmonkey.sentinel.targeting;

import org.bukkit.ChatColor;
import org.mcmonkey.sentinel.SentinelPlugin;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

/**
 * Helper for prefix labeled targets.
 */
public class SentinelTargetLabel {

    /**
     * The label prefix.
     */
    public String prefix;

    /**
     * The target value.
     */
    public String value;

    /**
     * Whether this label is regex based.
     */
    public boolean isRegex;

    /**
     * Prefixes that need regex validation.
     */
    public static HashSet<String> regexPrefixes = new HashSet<>(
            Arrays.asList("player", "npc", "entityname", "helditem", "offhand", "equipped", "in_inventory"));

    /**
     * All default prefixes (anything else handled by an integration object).
     */
    public static HashSet<String> corePrefixes = new HashSet<>(
            Arrays.asList("player", "npc", "entityname", "helditem", "offhand", "equipped",
                    "in_inventory", "group", "status", "event", "multi", "allinone")
    );

    /**
     * Prefixes that expect lowercased values.
     */
    public static HashSet<String> autoLowercasePrefixes = new HashSet<>(
            Arrays.asList("status", "event")
    );

    /**
     * Helper to ensure code won't be optimized away (Java isn't likely to do this anyway, but just in case).
     */
    public static long ignoreMe = 0;

    /**
     * Returns whether the RegEx is valid (or the target is not a RegEx target).
     */
    public boolean isValidRegex() {
        try {
            if (isRegex && "Sentinel".matches(value)) {
                ignoreMe++;
            }
            return true;
        }
        catch (Exception e) {
            return false;
        }
    }

    /**
     * Returns whether the target is valid.
     * True for all prefixed targets.
     */
    public boolean isValidTarget() {
        if (prefix != null) {
            if (prefix.equals("event")) {
                if (value.contains(":")) {
                    return SentinelPlugin.validEventTargets.contains(value.substring(0, value.indexOf(':')));
                }
                return SentinelPlugin.validEventTargets.contains(value);
            }
            return true;
        }
        return SentinelTarget.forName(value) != null;
    }

    /**
     * Returns whether the prefix is valid - if 'false', the prefix doesn't exist.
     * True for all non-prefixed targets.
     */
    public boolean isValidPrefix() {
        if (prefix == null) {
            return true;
        }
        if (corePrefixes.contains(prefix)) {
            return true;
        }
        if (SentinelPlugin.integrationPrefixMap.containsKey(prefix)) {
            return true;
        }
        return false;
    }

    /**
     * Gets the target collection from a list set for this label.
     */
    public Collection<String> getTargetsList(SentinelTargetList listSet) {
        if (prefix == null) {
            return listSet.targets;
        }
        else if (prefix.equals("player")) {
            return listSet.byPlayerName;
        }
        else if (prefix.equals("npc")) {
            return listSet.byNpcName;
        }
        else if (prefix.equals("entityname")) {
            return listSet.byEntityName;
        }
        else if (prefix.equals("helditem")) {
            return listSet.byHeldItem;
        }
        else if (prefix.equals("offhand")) {
            return listSet.byOffhandItem;
        }
        else if (prefix.equals("equipped")) {
            return listSet.byEquippedItem;
        }
        else if (prefix.equals("in_inventory")) {
            return listSet.byInventoryItem;
        }
        else if (prefix.equals("group")) {
            return listSet.byGroup;
        }
        else if (prefix.equals("event")) {
            return listSet.byEvent;
        }
        else if (prefix.equals("status")) {
            return listSet.byStatus;
        }
        else if (prefix.equals("multi")) {
            return null;
        }
        else if (prefix.equals("allinone")) {
            return null;
        }
        else {
            return listSet.byOther;
        }
    }

    private static SentinelTargetList helperList = new SentinelTargetList();

    /**
     * Gets the addable string for this label.
     */
    public String addable() {
        if (getTargetsList(helperList) == helperList.byOther) {
            return prefix + ":" + value;
        }
        return value;
    }

    /**
     * Returns whether the label is a valid multi-target.
     * True for all non-multi targets.
     */
    public boolean isValidMulti() {
        if (prefix == null) {
            return true;
        }
        if (prefix.equals("multi")) {
            return getMulti(",").totalTargetsCount() > 0;
        }
        else if (prefix.equals("allinone")) {
            return getMulti("|").totalTargetsCount() > 0;
        }
        return true;
    }

    /**
     * Gets the TargetList created by this multi-target label.
     */
    public SentinelTargetList getMulti(String splitter) {
        SentinelTargetList newList = new SentinelTargetList();
        for (String str : value.split(splitter)) {
            SentinelTargetLabel label = new SentinelTargetLabel(str);
            label.addToList(newList, false);
        }
        newList.recalculateCacheNoClear();
        return newList;
    }

    /**
     * Adds this target label to a list set.
     */
    public boolean addToList(SentinelTargetList listSet) {
        return addToList(listSet, true);
    }

    /**
     * Adds this target label to a list set.
     */
    public boolean addToList(SentinelTargetList listSet, boolean doRecache) {
        if (prefix != null && prefix.equals("multi")) {
            listSet.byMultiple.add(getMulti(","));
            return true;
        }
        if (prefix != null && prefix.equals("allinone")) {
            listSet.byAllInOne.add(getMulti("\\|"));
            return true;
        }
        Collection<String> list = getTargetsList(listSet);
        String addable = addable();
        if (doRecache && list.contains(addable)) {
            return false;
        }
        getTargetsList(listSet).add(addable());
        if (doRecache && (list == listSet.targets || list == listSet.byOther)) {
            listSet.recalculateTargetsCache();
        }
        return true;
    }

    /**
     * Removes this target label from a list set.
     */
    public boolean removeFromList(SentinelTargetList listSet) {
        if (prefix != null && prefix.equals("multi")) {
            try {
                int integerValue = Integer.parseInt(value);
                if (integerValue >= 0 && integerValue < listSet.byMultiple.size()) {
                    listSet.byMultiple.remove(integerValue);
                    return true;
                }
                return false;
            }
            catch (NumberFormatException ex) {
                return false;
            }
        }
        if (prefix != null && prefix.equals("allinone")) {
            try {
                int integerValue = Integer.parseInt(value);
                if (integerValue >= 0 && integerValue < listSet.byAllInOne.size()) {
                    listSet.byAllInOne.remove(integerValue);
                    return true;
                }
                return false;
            }
            catch (NumberFormatException ex) {
                return false;
            }
        }
        Collection<String> list = getTargetsList(listSet);
        String addable = addable();
        if (!list.contains(addable)) {
            return false;
        }
        list.remove(addable());
        if (list == listSet.targets || list == listSet.byOther) {
            listSet.recalculateTargetsCache();
        }
        return true;
    }

    /**
     * Constructs the target label instance.
     */
    public SentinelTargetLabel(String label) {
        int index = label.indexOf(':');
        if (index >= 0) {
            prefix = label.substring(0, index).toLowerCase();
            value = label.substring(index + 1);
            isRegex = regexPrefixes.contains(prefix);
            value = ChatColor.translateAlternateColorCodes('&', value);
            if (autoLowercasePrefixes.contains(prefix)) {
                value = value.toLowerCase();
            }
        }
        else {
            value = label.toUpperCase();
            SentinelTarget targetVersion = SentinelTarget.forName(value);
            if (targetVersion != null) {
                value = targetVersion.name();
            }
        }
    }
}
