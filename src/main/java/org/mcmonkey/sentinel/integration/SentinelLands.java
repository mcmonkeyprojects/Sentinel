package org.mcmonkey.sentinel.integration;

import me.angeschossen.lands.api.MemberHolder;
import me.angeschossen.lands.api.integration.LandsIntegration;
import me.angeschossen.lands.api.land.Land;
import me.angeschossen.lands.api.player.LandPlayer;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.mcmonkey.sentinel.SentinelIntegration;
import org.mcmonkey.sentinel.SentinelPlugin;

public class SentinelLands extends SentinelIntegration {

    private final LandsIntegration integration;

    public SentinelLands(SentinelPlugin plugin) {
        this.integration = new LandsIntegration(plugin);
    }

    @Override
    public String getTargetHelp() {
        return "landstarget:LAND_OR_NATION_NAME, targetenemies:LAND_OR_NATION_NAME, targetallies:LAND_OR_NATION_NAME";
    }

    @Override
    public String[] getTargetPrefixes() {
        return new String[]{"landstarget", "targetenemies", "targetallies"};
    }

    @Override
    public boolean isTarget(LivingEntity ent, String prefix, String value) {
        if (ent instanceof Player) {
            try {
                LandPlayer landPlayer = integration.getLandPlayer(ent.getUniqueId());
                if (landPlayer != null) {
                    switch (prefix) {
                        case "landstarget": {
                            return getTarget(value, landPlayer) != null;
                        }

                        case "targetenemies": {
                            return checkRelation(landPlayer, value, false);
                        }

                        case "targetallies": {
                            return checkRelation(landPlayer, value, true);
                        }

                        default:
                            break; // ignore I guess?
                    }
                }
            } catch (Exception ex) { // they are being caught in all the other integrations, so I guess I'll do the same here to break nothing
                ex.printStackTrace();
            }
        }

        return false;
    }

    /**
     * Check a relation between the player's lands and the target.
     * @param landPlayer The player
     * @param name The target
     * @param ally Whether to check for ally or enemy relation
     * @return false, if the target does not exist, or it does not have the relation type.
     */
    private boolean checkRelation(LandPlayer landPlayer, String name, boolean ally) {
        MemberHolder memberHolder = getTarget(name);
        if (memberHolder != null) {
            for (Land land : landPlayer.getLands()) {
                if (ally ? land.isAlly(memberHolder) : land.isEnemy(memberHolder)) {
                    return true;
                }
            }
        }

        return false;
    }

    private MemberHolder getTarget(String name, LandPlayer landPlayer) {
        MemberHolder target = getTarget(name);
        return target == null ? null : target.isMember(landPlayer) ? target : null;
    }

    private MemberHolder getTarget(String name) {
        Land land = integration.getLand(name);
        return land == null ? integration.getNation(name) : land;
    }
}
