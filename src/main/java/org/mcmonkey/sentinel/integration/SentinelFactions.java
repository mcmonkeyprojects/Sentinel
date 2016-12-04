package org.mcmonkey.sentinel.integration;

import com.massivecraft.factions.Factions;
import com.massivecraft.factions.entity.Faction;
import com.massivecraft.factions.entity.FactionColl;
import com.massivecraft.factions.entity.MPlayer;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.mcmonkey.sentinel.SentinelIntegration;

public class SentinelFactions extends SentinelIntegration {

    @Override
    public String getTargetHelp() {
        return "factions:FACTION_NAME";
    }

    @Override
    public boolean isTarget(LivingEntity ent, String text) {
        try {
            if (text.startsWith("factions:") && ent instanceof Player) {
                String factionName = text.substring("factions:".length());
                Faction faction = FactionColl.get().getByName(factionName);
                for (MPlayer pl: faction.getMPlayers()) {
                    if (pl.getPlayer() != null && pl.getPlayer().getUniqueId() != null
                            && pl.getPlayer().getUniqueId().equals(ent.getUniqueId())) {
                        return true;
                    }
                }
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }
}
