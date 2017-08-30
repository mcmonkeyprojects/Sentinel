package org.mcmonkey.sentinel.integration;

import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.mcmonkey.sentinel.SentinelIntegration;
import org.mcmonkey.sentinel.SentinelTrait;
import com.shampaggon.crackshot.CSDirector;

public class SentinelCrackShot extends SentinelIntegration {

    @Override
    public String getTargetHelp() {
        return "";
    }

    @Override
    public boolean tryAttack(SentinelTrait st, LivingEntity ent) {
        if (!(st.getLivingEntity() instanceof Player)) {
            return false;
        }
        CSDirector direc = (CSDirector) Bukkit.getPluginManager().getPlugin("CrackShot");
        String node = direc.returnParentNode((Player) st.getLivingEntity());
        if (node == null) {
            return false;
        }
        st.faceLocation(ent.getEyeLocation());
        ItemStack itm = ((Player) st.getLivingEntity()).getItemInHand();
        direc.csminion.weaponInteraction((Player) st.getLivingEntity(), node, false);
        ((Player) st.getLivingEntity()).setItemInHand(itm);
        if (st.rangedChase) {
            st.rechase();
        }
        return true;
    }
}
