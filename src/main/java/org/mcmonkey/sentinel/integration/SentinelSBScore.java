package org.mcmonkey.sentinel.integration;

import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.mcmonkey.sentinel.SentinelIntegration;

public class SentinelSBScore extends SentinelIntegration {

    @Override
    public String getTargetHelp() {
        return "sbscoreabove:OBJECTIVE:MIN_VALUE, sbscorebelow:OBJECTIVE:MAX_VALUE";
    }

    @Override
    public String[] getTargetPrefixes() {
        return new String[] { "sbscoreabove", "sbscorebelow" };
    }

    @Override
    public boolean isTarget(LivingEntity ent, String prefix, String value) {
        try {
            if ((prefix.equals("sbscoreabove") || prefix.equals("sbscorebelow")) && ent instanceof Player) {
                int colon = value.indexOf(':');
                if (colon == -1) {
                    return false;
                }
                String objectiveName = value.substring(0, colon);
                Objective objective = Bukkit.getScoreboardManager().getMainScoreboard().getObjective(objectiveName);
                if (objective == null) {
                    return false;
                }
                Score score = objective.getScore(ent.getName());
                if (!score.isScoreSet()) {
                    return false;
                }
                String scoreText = value.substring(colon + 1);
                int scoreInt = Integer.parseInt(scoreText);
                if (prefix.equals("sbscoreabove")) {
                    return score.getScore() > scoreInt;
                }
                else {
                    return score.getScore() < scoreInt;
                }
            }
        }
        catch (NumberFormatException ex) {
            // Do nothing.
            // TODO: Maybe show a one-time warning?
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }
}
