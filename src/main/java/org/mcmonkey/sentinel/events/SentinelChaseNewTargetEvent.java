package org.mcmonkey.sentinel.events;

import net.citizensnpcs.api.event.NPCEvent;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

/**
 * Event fired when a Sentinel NPC is setting its current chase target to something new.
 */
public class SentinelChaseNewTargetEvent extends NPCEvent implements Cancellable {

    /**
     * Handler objects, for Bukkit internal usage.
     */
    private static final HandlerList handlers = new HandlerList();

    /**
     * Constructs the chase-new-target event.
     */
    public SentinelChaseNewTargetEvent(NPC npc, LivingEntity target) {
        super(npc);
        this.newTarget = target;
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

    /**
     * The new chase target. Can be modified if desired. Prefer "setCancelled" if you just want to cancel the change.
     */
    public LivingEntity newTarget;

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
