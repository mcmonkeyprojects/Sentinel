package org.mcmonkey.sentinel;

import net.citizensnpcs.api.npc.NPC;
import org.bukkit.entity.LivingEntity;
import org.mcmonkey.sentinel.targeting.SentinelTargetingHelper;

/**
 * Base class for Sentinel helper objects.
 */
public abstract class SentinelHelperObject {

    /**
     * The relevant SentinelTrait instance.
     */
    public SentinelTrait sentinel;

    protected SentinelItemHelper itemHelper;

    protected SentinelWeaponHelper weaponHelper;

    protected SentinelTargetingHelper targetingHelper;

    protected SentinelAttackHelper attackHelper;

    /**
     * Sets the Sentinel trait object (and fills other helper object values).
     */
    public void setTraitObject(SentinelTrait trait) {
        sentinel = trait;
        itemHelper = trait.itemHelper;
        weaponHelper = trait.weaponHelper;
        targetingHelper = sentinel.targetingHelper;
        attackHelper = sentinel.attackHelper;
    }

    /**
     * Gets the relevant NPC.
     */
    public NPC getNPC() {
        return sentinel.getNPC();
    }

    /**
     * Gets the NPC's living entity.
     */
    public LivingEntity getLivingEntity() {
        return sentinel.getLivingEntity();
    }

    /**
     * Outputs a debug message.
     */
    public void debug(String message) {
        sentinel.debug(message);
    }
}
