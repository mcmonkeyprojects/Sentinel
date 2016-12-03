package org.mcmonkey.sentinel.integration;

import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;
import org.mcmonkey.sentinel.SentinelIntegration;

public class SentinelSBTeams extends SentinelIntegration {

    @Override
    public String getTargetHelp() {
        return "sbteam:SCOREBOARD_TEAM_NAME";
    }

    @Override
    public boolean isTarget(LivingEntity ent, String text) {
        try {
            if (text.startsWith("sbteam:") && ent instanceof Player) {
                String sbteamName = text.substring("sbteam:".length());
                Team t = Bukkit.getScoreboardManager().getMainScoreboard().getTeam(sbteamName);
                if (t != null) {
                    if (t.hasEntry(((Player) ent).getName())) {
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
