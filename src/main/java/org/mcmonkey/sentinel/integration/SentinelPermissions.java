package org.mcmonkey.sentinel.integration;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.mcmonkey.sentinel.SentinelIntegration;

public class SentinelPermissions extends SentinelIntegration {

    @Override
    public String getTargetHelp() {
        return "permission:PERM.KEY";
    }

    @Override
    public boolean isTarget(LivingEntity ent, String text) {
        if (!(ent instanceof Player)) {
            return false;
        }
        if (text.startsWith("permission:")) {
            String pText = text.substring("permission:".length());
            if (((Player) ent).hasPermission(pText)) {
                return true;
            }
        }
        return false;
    }
}
