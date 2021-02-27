package org.mcmonkey.sentinel.integration;

import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.TownyUniverse;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.mcmonkey.sentinel.SentinelIntegration;

public class SentinelTowny extends SentinelIntegration {

    @Override
    public String getTargetHelp() {
        return "towny:TOWN_NAME, nation:NATION_NAME, nationenemies:NATION_HERE, nationallies:NATION_HERE";
    }

    @Override
    public String[] getTargetPrefixes() {
        return new String[] { "towny", "nation", "nationenemies", "nationallies" };
    }

    @Override
    public boolean isTarget(LivingEntity ent, String prefix, String value) {
        try {
            if (prefix.equals("towny") && ent instanceof Player) {
                if (TownyUniverse.getInstance().hasTown(value)) {
                    Town t = TownyUniverse.getInstance().getTown(value);
                    Resident playerResident = TownyUniverse.getInstance().getResident(ent.getUniqueId());
                    if (t != null && t.hasResident(playerResident)) {
                        return true;
                    }
                }
                else {
                    // TODO: Error?
                    return false;
                }
            }
            else if (prefix.equals("nation") && ent instanceof Player) {
                if (TownyUniverse.getInstance().hasNation(value)) {
                    Nation n = TownyUniverse.getInstance().getNation(value);
                    Resident playerResident = TownyUniverse.getInstance().getResident(ent.getUniqueId());
                    if (n != null && n.hasResident(playerResident)) {
                        return true;
                    }
                }
                else {
                    // TODO: Error?
                    return false;
                }
            }
            else if (prefix.equals("nationenemies") && ent instanceof Player) {
                if (TownyUniverse.getInstance().hasNation(value)) {
                    Nation n = TownyUniverse.getInstance().getNation(value);
                    if (!TownyUniverse.getInstance().hasResident(ent.getName())) {
                        return false;
                    }
                    Resident playerResident = TownyUniverse.getInstance().getResident(ent.getUniqueId());
                    if (playerResident != null && playerResident.hasNation() && playerResident.getTown().getNation().hasEnemy(n)) {
                        return true;
                    }
                }
                else {
                    // TODO: Error?
                    return false;
                }
            }
            else if (prefix.equals("nationallies") && ent instanceof Player) {
                if (TownyUniverse.getInstance().hasNation(value)) {
                    Nation n = TownyUniverse.getInstance().getNation(value);
                    if (!TownyUniverse.getInstance().hasResident(ent.getName())) {
                        return false;
                    }
                    Resident playerResident = TownyUniverse.getInstance().getResident(ent.getUniqueId());
                    if (playerResident != null && playerResident.hasNation() && playerResident.getTown().getNation().hasAlly(n)) {
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
