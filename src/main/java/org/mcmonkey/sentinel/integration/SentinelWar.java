package org.mcmonkey.sentinel.integration;

import com.tommytony.war.Team;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.mcmonkey.sentinel.SentinelIntegration;

public class SentinelWar extends SentinelIntegration {

    @Override
    public String getTargetHelp() {
        return "war_team:WAR_TEAM_NAME";
    }

    @Override
    public String[] getTargetPrefixes() {
        return new String[] { "war_team" };
    }

    @Override
    public boolean isTarget(LivingEntity ent, String prefix, String value) {
        try {
            if (prefix.equals("war_team") && ent instanceof Player) {
                Team team = Team.getTeamByPlayerName(ent.getName());
                if (team.getName().equalsIgnoreCase(value)) {
                    return true;
                }
                else {
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
