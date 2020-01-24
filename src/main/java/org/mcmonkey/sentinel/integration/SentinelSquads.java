package org.mcmonkey.sentinel.integration;

import net.citizensnpcs.api.CitizensAPI;
import org.bukkit.entity.LivingEntity;
import org.mcmonkey.sentinel.SentinelIntegration;
import org.mcmonkey.sentinel.SentinelTrait;

import java.util.Locale;

public class SentinelSquads extends SentinelIntegration {

    @Override
    public String getTargetHelp() {
        return "squad:SENTINEL_SQUAD_NAME";
    }

    @Override
    public String[] getTargetPrefixes() {
        return new String[] { "squad" };
    }

    @Override
    public boolean shouldLowerCaseValue() {
        return true;
    }

    @Override
    public boolean isTarget(LivingEntity ent, String prefix, String value) {
        try {
            if (prefix.equals("squad") && CitizensAPI.getNPCRegistry().isNPC(ent)
                    && CitizensAPI.getNPCRegistry().getNPC(ent).hasTrait(SentinelTrait.class)) {
                SentinelTrait sentinel = CitizensAPI.getNPCRegistry().getNPC(ent).getTrait(SentinelTrait.class);
                if (sentinel.squad != null) {
                    String squadName = value.toLowerCase(Locale.ENGLISH);
                    if (squadName.equals(sentinel.squad)) {
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
