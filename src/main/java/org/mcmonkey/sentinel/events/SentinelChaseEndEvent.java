package org.mcmonkey.sentinel.events;

import net.citizensnpcs.api.event.NPCEvent;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

/**
 * Event fired when a Sentinel NPC is setting its current chase target to none.
 */
public class SentinelChaseEndEvent extends NPCEvent implements Cancellable {

    /**
     * Handler objects, for Bukkit internal usage.
     */
    private static final HandlerList handlers = new HandlerList();

    /**
     * Constructs the chase-end event.
     */
    public SentinelChaseEndEvent(NPC npc) {
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

    /** Whether the event is cancelled. */
    public boolean cancelled;

    /** Returns whether the event is cancelled. */
    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    /** Sets whether the event is cancelled. */
    @Override
    public void setCancelled(boolean b) {
        cancelled = b;
    }
}
