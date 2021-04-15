package org.mcmonkey.sentinel.integration;

import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.mcmonkey.sentinel.SentinelIntegration;
import org.mcmonkey.sentinel.SentinelTrait;
import com.shampaggon.crackshot.CSDirector;
import org.mcmonkey.sentinel.SentinelUtilities;

public class SentinelCrackShot extends SentinelIntegration {

    @Override
    public String getTargetHelp() {
        return "";
    }

    @Override
    public String[] getTargetPrefixes() {
        return new String[0];
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
        Vector faceAcc = ent.getEyeLocation().toVector().subtract(st.getLivingEntity().getEyeLocation().toVector());
        if (faceAcc.lengthSquared() > 0.0) {
            faceAcc = faceAcc.normalize();
        }
        faceAcc = st.fixForAcc(faceAcc);
        st.faceLocation(st.getLivingEntity().getEyeLocation().clone().add(faceAcc.multiply(10)));
        ItemStack itm = SentinelUtilities.getHeldItem(st.getLivingEntity());
        direc.csminion.weaponInteraction((Player) st.getLivingEntity(), node, false);
        ((Player) st.getLivingEntity()).setItemInHand(itm);
        if (st.rangedChase) {
            st.attackHelper.rechase();
        }
        return true;
    }
}
