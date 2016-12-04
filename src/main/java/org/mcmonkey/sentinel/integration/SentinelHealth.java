package org.mcmonkey.sentinel.integration;

import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;
import org.mcmonkey.sentinel.SentinelIntegration;

public class SentinelHealth extends SentinelIntegration {

    @Override
    public String getTargetHelp() {
        return "healthabove:PERCENTAGE, healthbelow:PERCENTAGE";
    }

    @Override
    public boolean isTarget(LivingEntity ent, String text) {
        try {
            if (text.startsWith("healthabove:")) {
                String haText = text.substring("healthabove:".length());
                double haVal = Double.parseDouble(haText);
                if (ent.getHealth() / ent.getMaxHealth() > haVal * 0.01) {
                    return true;
                }
            }
            else if (text.startsWith("healthbelow:")) {
                String haText = text.substring("healthbelow:".length());
                double haVal = Double.parseDouble(haText);
                if (ent.getHealth() / ent.getMaxHealth() < haVal * 0.01) {
                    return true;
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
