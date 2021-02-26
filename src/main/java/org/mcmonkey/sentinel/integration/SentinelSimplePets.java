package org.mcmonkey.sentinel.integration;

import org.bukkit.entity.LivingEntity;
import org.mcmonkey.sentinel.SentinelIntegration;
import org.mcmonkey.sentinel.SentinelUtilities;
import simplepets.brainsynder.api.pet.IPet;
import simplepets.brainsynder.reflection.ReflectionUtil;

public class SentinelSimplePets extends SentinelIntegration {

    @Override
    public String getTargetHelp() {
        return "simplepet:PET_NAME_REGEX";
    }

    @Override
    public String[] getTargetPrefixes() {
        return new String[] { "simplepet" };
    }

    @Override
    public boolean isTarget(LivingEntity ent, String prefix, String value) {
        try {
            if (!prefix.equals("simplepet")) {
                return false;
            }
            Object handle = ReflectionUtil.getEntityHandle(ent);
            if (handle instanceof IPet) {
                String name = ((IPet) handle).getPetType().getName();
                return SentinelUtilities.regexFor(value).matcher(name).matches();
            }
            return false;
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }
}
