package org.mcmonkey.sentinel.events;

import net.citizensnpcs.api.event.NPCEvent;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.event.HandlerList;

/**
 * Event fired when a Sentinel NPC has no targets remaining.
 */
public class SentinelNoMoreTargetsEvent extends NPCEvent {

    /**
     * Handler objects, for Bukkit internal usage.
     */
    private static final HandlerList handlers = new HandlerList();

    /**
     * Constructs the attack event.
     */
    public SentinelNoMoreTargetsEvent(NPC npc) {
        super(npc);
    }

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
