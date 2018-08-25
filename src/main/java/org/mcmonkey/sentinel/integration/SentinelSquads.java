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
    public boolean isTarget(LivingEntity ent, String text) {
        try {
            if (text.startsWith("squad:") && CitizensAPI.getNPCRegistry().isNPC(ent)
                    && CitizensAPI.getNPCRegistry().getNPC(ent).hasTrait(SentinelTrait.class)) {
                SentinelTrait sentinel = CitizensAPI.getNPCRegistry().getNPC(ent).getTrait(SentinelTrait.class);
                if (sentinel.squad != null) {
                    String squadName = text.substring("squad:".length()).toLowerCase(Locale.ENGLISH);
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
