package org.mcmonkey.sentinel.integration;

import com.massivecraft.factions.Factions;
import com.massivecraft.factions.Rel;
import com.massivecraft.factions.entity.Faction;
import com.massivecraft.factions.entity.FactionColl;
import com.massivecraft.factions.entity.MPlayer;
import com.massivecraft.massivecore.store.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.mcmonkey.sentinel.SentinelIntegration;

public class SentinelFactions extends SentinelIntegration {

    @Override
    public String getTargetHelp() {
        return "factions:FACTION_NAME, factionsenemy:NAME, factionsally:NAME";
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
            if (text.startsWith("factionsenemy:") && ent instanceof Player) {
                String factionName = text.substring("factionsenemy:".length());
                Faction faction = FactionColl.get().getByName(factionName);
                Faction plf = MPlayer.get(((Player) ent).getUniqueId()).getFaction();
                if (faction.getRelationTo(plf).equals(Rel.ENEMY)) {
                    return true;
                }
            }
            if (text.startsWith("factionsally:") && ent instanceof Player) {
                String factionName = text.substring("factionsally:".length());
                Faction faction = FactionColl.get().getByName(factionName);
                Faction plf = MPlayer.get(((Player) ent).getUniqueId()).getFaction();
                if (faction.getRelationTo(plf).equals(Rel.ALLY)) {
                    return true;
                }
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }
}
