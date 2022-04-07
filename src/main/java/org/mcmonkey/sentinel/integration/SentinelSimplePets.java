package org.mcmonkey.sentinel.integration;

import org.bukkit.entity.LivingEntity;
import org.mcmonkey.sentinel.SentinelIntegration;
import org.mcmonkey.sentinel.SentinelUtilities;
import simplepets.brainsynder.api.entity.IEntityPet;
import simplepets.brainsynder.api.plugin.SimplePets;

import java.util.Optional;

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
            Optional<Object> optional = SimplePets.getSpawnUtil().getHandle(ent);
            if (optional.isPresent() && optional.get() instanceof IEntityPet) {
                String name = ((IEntityPet) optional.get()).getPetType().getName();
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
