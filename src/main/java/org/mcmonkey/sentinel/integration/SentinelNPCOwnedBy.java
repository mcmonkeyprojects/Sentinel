package org.mcmonkey.sentinel.integration;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.trait.Owner;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.mcmonkey.sentinel.SentinelIntegration;
import org.mcmonkey.sentinel.targeting.SentinelTargetLabel;
import org.mcmonkey.sentinel.targeting.SentinelTargetList;

import java.util.UUID;

public class SentinelNPCOwnedBy extends SentinelIntegration {

    @Override
    public String getTargetHelp() {
        return "npc_owned_by:TARGETER";
    }

    @Override
    public String[] getTargetPrefixes() {
        return new String[] { "npc_owned_by" };
    }

    @Override
    public boolean isTarget(LivingEntity ent, String prefix, String value) {
        if (!prefix.equals("npc_owned_by")) {
            return false;
        }
        NPC target = CitizensAPI.getNPCRegistry().getNPC(ent);
        if (target == null) {
            return false;
        }
        Owner owner = target.getTraitNullable(Owner.class);
        if (owner == null) {
            return false;
        }
        UUID ownerID = owner.getOwnerId();
        if (ownerID == null) {
            return false;
        }
        Player player = Bukkit.getPlayer(ownerID);
        if (player == null) {
            return false;
        }
        SentinelTargetList listHelper = new SentinelTargetList();
        new SentinelTargetLabel(value).addToList(listHelper);
        return listHelper.isTarget(player);
    }
}
