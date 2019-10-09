package org.mcmonkey.sentinel.integration;

import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffectType;
import org.mcmonkey.sentinel.SentinelIntegration;

public class SentinelPotion extends SentinelIntegration {

    @Override
    public String getTargetHelp() {
        return "potion:POTION_EFFECT";
    }

    @Override
    public String[] getTargetPrefixes() {
        return new String[] { "potion" };
    }

    @Override
    public boolean isTarget(LivingEntity ent, String prefix, String value) {
        if (prefix.equals("potion")) {
            if (ent.hasPotionEffect(PotionEffectType.getByName(value))) {
                return true;
            }
        }
        return false;
    }
}
