package org.mcmonkey.sentinel.events;

import net.citizensnpcs.api.event.NPCEvent;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.Location;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

/**
 * Event fired when a Sentinel NPC wants to path somewhere.
 */
public class SentinelWantsToPathEvent extends NPCEvent implements Cancellable {

    /**
     * Handler objects, for Bukkit internal usage.
     */
    private static final HandlerList handlers = new HandlerList();

    /**
     * Constructs the wants-to-path event.
     */
    public SentinelWantsToPathEvent(NPC npc, Location destination) {
        super(npc);
        this.destination = destination;
    }

    /**
     * Where the NPC is trying to go.
     */
    public Location destination;

    /**
     * Whether the event is cancelled.
     */
    private boolean cancelled = false;

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
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
