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
    public String[] getTargetPrefixes() {
        return new String[] { "permission" };
    }

    @Override
    public boolean isTarget(LivingEntity ent, String prefix, String value) {
        if (!(ent instanceof Player)) {
            return false;
        }
        if (prefix.equals("permission")) {
            if (((Player) ent).hasPermission(value)) {
                return true;
            }
        }
        return false;
    }
}
