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
    public boolean isTarget(LivingEntity ent, String text) {
        try {
            if (text.startsWith("war_team:") && ent instanceof Player) {
                Team team = Team.getTeamByPlayerName(ent.getName());
                String teamName = text.substring("war_team:".length());
                if (team.getName().equalsIgnoreCase(teamName)) {
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
