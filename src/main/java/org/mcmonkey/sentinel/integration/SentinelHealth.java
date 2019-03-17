package org.mcmonkey.sentinel.integration;

import org.bukkit.entity.LivingEntity;
import org.mcmonkey.sentinel.SentinelIntegration;

public class SentinelHealth extends SentinelIntegration {

    @Override
    public String getTargetHelp() {
        return "healthabove:PERCENTAGE, healthbelow:PERCENTAGE";
    }

    @Override
    public String[] getTargetPrefixes() {
        return new String[] { "healthabove", "healthbelow" };
    }

    @Override
    public boolean isTarget(LivingEntity ent, String prefix, String value) {
        try {
            if (prefix.equals("healthabove")) {
                double haVal = Double.parseDouble(value);
                if (ent.getHealth() / ent.getMaxHealth() > haVal * 0.01) {
                    return true;
                }
            }
            else if (prefix.equals("healthbelow")) {
                double haVal = Double.parseDouble(value);
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
