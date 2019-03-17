package org.mcmonkey.sentinel.integration;

import com.massivecraft.factions.Rel;
import com.massivecraft.factions.entity.Faction;
import com.massivecraft.factions.entity.FactionColl;
import com.massivecraft.factions.entity.MPlayer;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.mcmonkey.sentinel.SentinelIntegration;

public class SentinelFactions extends SentinelIntegration {

    @Override
    public String getTargetHelp() {
        return "factions:FACTION_NAME, factionsenemy:NAME, factionsally:NAME";
    }

    @Override
    public String[] getTargetPrefixes() {
        return new String[] { "factions", "factionsenemy", "factionsally" };
    }

    @Override
    public boolean isTarget(LivingEntity ent, String prefix, String value) {
        try {
            if (prefix.equals("factions") && ent instanceof Player) {
                Faction faction = FactionColl.get().getByName(value);
                for (MPlayer pl: faction.getMPlayers()) {
                    if (pl.getPlayer() != null && pl.getPlayer().getUniqueId() != null
                            && pl.getPlayer().getUniqueId().equals(ent.getUniqueId())) {
                        return true;
                    }
                }
            }
            else if (prefix.equals("factionsenemy") && ent instanceof Player) {
                Faction faction = FactionColl.get().getByName(value);
                Faction plf = MPlayer.get(((Player) ent).getUniqueId()).getFaction();
                if (faction.getRelationTo(plf).equals(Rel.ENEMY)) {
                    return true;
                }
            }
            else if (prefix.equals("factionsally") && ent instanceof Player) {
                Faction faction = FactionColl.get().getByName(value);
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
