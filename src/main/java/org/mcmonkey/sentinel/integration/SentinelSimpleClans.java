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
    public boolean isTarget(LivingEntity ent, String text) {
        try {
            if (text.startsWith("simpleclan:") && ent instanceof Player) {
                String clanName = text.substring("simpleclan:".length());
                Clan clan = SimpleClans.getInstance().getClanManager().getClanByPlayerUniqueId(ent.getUniqueId());
                if (clan.getName().equalsIgnoreCase(clanName)) {
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
