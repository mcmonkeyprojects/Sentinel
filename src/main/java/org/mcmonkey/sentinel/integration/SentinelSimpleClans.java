package org.mcmonkey.sentinel.integration;

import net.sacredlabyrinth.phaed.simpleclans.Clan;
import net.sacredlabyrinth.phaed.simpleclans.SimpleClans;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.mcmonkey.sentinel.SentinelIntegration;

public class SentinelSimpleClans extends SentinelIntegration {

    @Override
    public String getTargetHelp() {
        return "simpleclan:CLAN_NAME";
    }

    @Override
    public String[] getTargetPrefixes() {
        return new String[] { "simpleclan" };
    }

    @Override
    public boolean isTarget(LivingEntity ent, String prefix, String value) {
        try {
            if (prefix.equals("simpleclan") && ent instanceof Player) {
                Clan clan = SimpleClans.getInstance().getClanManager().getClanByPlayerUniqueId(ent.getUniqueId());
                if (clan.getName().equalsIgnoreCase(value)) {
                    return true;
                }
                else {
                    return false;
                }
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }
}
