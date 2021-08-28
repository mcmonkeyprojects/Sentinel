package org.mcmonkey.sentinel.events;

import net.citizensnpcs.api.event.NPCEvent;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.event.HandlerList;

/**
 * Event fired when a Sentinel NPC starts or stops being in combat.
 * See also SentinelTrait#otherBehaviorPaused
 */
public class SentinelCombatStateChangeEvent extends NPCEvent {

    /**
     * Handler objects, for Bukkit internal usage.
     */
    private static final HandlerList handlers = new HandlerList();

    /**
     * Constructs the combat-state-change event.
     */
    public SentinelCombatStateChangeEvent(NPC npc, boolean isInCombat) {
        super(npc);
        this.isInCombat = isInCombat;
    }

    /**
     * Whether the NPC is now in combat.
     */
    public boolean isInCombat;

    /**
     * Returns the handler list for use with Bukkit.
     */
    public HandlerList getHandlers() {
        return handlers;
    }

    /**
     * Returns the handler list for use with Bukkit.
     */
    public static HandlerList getHandlerList() {
        return handlers;
    }
}
