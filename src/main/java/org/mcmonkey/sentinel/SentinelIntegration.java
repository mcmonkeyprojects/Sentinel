package org.mcmonkey.sentinel;

import org.bukkit.entity.LivingEntity;

/**
 * Represents an integration of an external plugin or system into Sentinel.
 */
public class SentinelIntegration {

    /**
     * Gets the 'target help' data for this integration (empty string if not relevant).
     * Example format is: "myintegration:MY_TARGET_IDENTIFIER" like "squad:SQUAD_NAME" or "healthabove:PERCENTAGE"
     */
    public String getTargetHelp() {
        return "{{Error:UnimplementedGetTargetHelp}}";
    }

    /**
     * Gets the list of target prefixes that this integration handles.
     * For a "squad:SQUAD_NAME" target, this should return: new String[] { "squad" }
     */
    public String[] getTargetPrefixes() {
        return new String[0];
    }

    /**
     * Returns whether an entity is a target of the integration label.
     */
    public boolean isTarget(LivingEntity ent, String prefix, String value) {
        return isTarget(ent, prefix + ":" + value);
    }

    /**
     * Returns whether an entity is a target of the integration label.
     */
    @Deprecated
    public boolean isTarget(LivingEntity ent, String text) {
        return false;
    }

    /**
     * Runs when an NPC intends to attack a target - return 'true' to indicate the integration ran its own attack methodology
     * (and no default attack handling is needed).
     */
    public boolean tryAttack(SentinelTrait st, LivingEntity ent) {
        return false;
    }
}
