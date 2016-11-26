package org.mcmonkey.sentinel;

import org.bukkit.entity.LivingEntity;

public class SentinelIntegration {

    public String getTargetHelp() {
        return "{{Error:UnimplementedGetTargetHelp}}";
    }

    public boolean isTarget(LivingEntity ent, String text) {
        return false;
    }
}
