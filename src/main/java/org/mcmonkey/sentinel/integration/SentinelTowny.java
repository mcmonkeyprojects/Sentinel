package org.mcmonkey.sentinel.integration;

import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownyUniverse;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.mcmonkey.sentinel.SentinelIntegration;

public class SentinelTowny extends SentinelIntegration {

    @Override
    public String getTargetHelp() {
        return "towny:TOWN_NAME, nation:NATION_NAME";
    }

    @Override
    public String[] getTargetPrefixes() {
        return new String[] { "towny", "nation" };
    }

    @Override
    public boolean isTarget(LivingEntity ent, String prefix, String value) {
        try {
            if (prefix.equals("towny") && ent instanceof Player) {
                if (TownyUniverse.getDataSource().hasTown(value)) {
                    Town t = TownyUniverse.getDataSource().getTown(value);
                    if (t.hasResident(ent.getName())) { // TODO: Why no UUID support?!
                        return true;
                    }
                }
                else {
                    // TODO: Error?
                    return false;
                }
            }
            else if (prefix.equals("nation") && ent instanceof Player) {
                if (TownyUniverse.getDataSource().hasNation(value)) {
                    Nation n = TownyUniverse.getDataSource().getNation(value);
                    if (n.hasResident(ent.getName())) { // TODO: Why no UUID support?!
                        return true;
                    }
                }
                else {
                    // TODO: Error?
                    return false;
                }
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }
}
