package org.mcmonkey.sentinel;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class SentinelEventHandler implements Listener {

    /**
     * Called when players chat, to process event message targets.
     */
    @EventHandler
    public void onAsyncPlayerChat(final AsyncPlayerChatEvent event) {
        if (event.isCancelled()) {
            return;
        }
        Bukkit.getScheduler().scheduleSyncDelayedTask(SentinelPlugin.instance, new Runnable() {
            @Override
            public void run() {
                if (!event.getPlayer().isOnline()) {
                    return;
                }
                for (NPC npc : CitizensAPI.getNPCRegistry()) {
                    if (npc.hasTrait(SentinelTrait.class)) {
                        SentinelTrait sentinel = npc.getTrait(SentinelTrait.class);
                        if (sentinel.allTargets.isEventTarget(event)) {
                            sentinel.targetingHelper.addTarget(event.getPlayer().getUniqueId());
                        }
                        if (sentinel.allAvoids.isEventTarget(event)) {
                            sentinel.targetingHelper.addAvoid(event.getPlayer().getUniqueId());
                        }
                    }
                }
            }
        });
    }
}
