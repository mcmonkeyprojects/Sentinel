package org.mcmonkey.sentinel.integration;

import org.bukkit.entity.LivingEntity;
import org.mcmonkey.sentinel.SentinelIntegration;
import org.mcmonkey.sentinel.SentinelUtilities;

import java.util.UUID;

public class SentinelUUID extends SentinelIntegration {

    @Override
    public String getTargetHelp() {
        return "uuid:UUID";
    }

    @Override
    public String[] getTargetPrefixes() {
        return new String[] { "uuid" };
    }

    @Override
    public boolean shouldLowerCaseValue() {
        return true;
    }

    @Override
    public boolean isTarget(LivingEntity ent, String prefix, String value) {
        try {
            if (prefix.equals("uuid")) {
                return SentinelUtilities.uuidEquals(ent.getUniqueId(), UUID.fromString(value));
            }
        }
        catch (IllegalArgumentException ex) {
            // Do nothing.
            // TODO: Maybe show a one-time warning?
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }
}
