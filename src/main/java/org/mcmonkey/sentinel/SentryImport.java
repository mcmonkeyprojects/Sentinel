package org.mcmonkey.sentinel;

import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.Listener;

import net.aufdemrand.sentry.SentryInstance;
import net.aufdemrand.sentry.SentryTrait;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;

public class SentryImport implements Listener {
    
    // Converts sentries to sentinels. Returns the number of NPCs converted
    @SuppressWarnings("deprecation")
    public static int PerformImport() {
        int convertedCount = 0;
        for (NPC npc : CitizensAPI.getNPCRegistry()) {
            if (!npc.hasTrait(SentryTrait.class)) {
                continue;
            }
            
            SentryInstance sentry = npc.getTrait(SentryTrait.class).getInstance();
            // This can happen if citizens was reloaded
            if (sentry == null) {
                continue;
            }
            convertedCount++;
            
            // Now we've got our instance, lets create the sentinel trait instead
            npc.removeTrait(SentryTrait.class);
            if (!npc.hasTrait(SentinelTrait.class)) {
                npc.addTrait(SentinelTrait.class);
            }
            SentinelTrait sentinel = npc.getTrait(SentinelTrait.class);
            
            // Import time, import everything as best as we can
            sentinel.armor = sentry.Armor;
            sentinel.attackRate = (int)Math.ceil(sentry.AttackRateSeconds * 20);
            if (sentinel.attackRate < SentinelTrait.attackRateMin) {
                sentinel.attackRate = SentinelTrait.attackRateMin;
            }
            else if (sentinel.attackRate > SentinelTrait.attackRateMax) {
                sentinel.attackRate = SentinelTrait.attackRateMax;
            }
            sentinel.chaseRange = sentry.sentryRange;
            sentinel.closeChase = true;
            sentinel.damage = sentry.Strength;
            sentinel.enemyDrops = sentry.KillsDropInventory;

            // Import targets
            for (String t: sentry.validTargets){
                if (t.contains("ENTITY:ALL")) {
                    sentinel.targets.add(SentinelTarget.MOBS);
                    sentinel.targets.add(SentinelTarget.PLAYERS);
                    sentinel.targets.add(SentinelTarget.NPCS);
                }
                else if(t.contains("ENTITY:MONSTER")) {
                    sentinel.targets.add(SentinelTarget.MONSTERS);
                }
                else if(t.contains("ENTITY:PLAYER")) {
                    sentinel.targets.add(SentinelTarget.PLAYERS);
                }
                else if(t.contains("ENTITY:NPC")) {
                    sentinel.targets.add(SentinelTarget.NPCS);
                }
                else{
                    String[] sections = t.split(":");
                    if (sections.length != 2) {
                        // Invalid target identifier?
                        continue;
                    }
                    // Sentry was spacing tolerant, so we should be too.
                    sections[0] = sections[0].trim();
                    sections[1] = sections[1].trim();
                    if(sections[0].equals("NPC")) {
                        sentinel.npcNameTargets.add(sections[1]);
                    }
                    else if (sections[0].equals("GROUP")) {
                        sentinel.groupTargets.add(sections[1]);
                    }
                    else if(sections[0].equals("PLAYER")) {
                        sentinel.playerNameTargets.add(sections[1]);
                    }
                    else if(sections[0].equals("ENTITY")) {
                        SentinelTarget target = SentinelTarget.forName(sections[1]);
                        if (target != null) {
                            sentinel.targets.add(target);
                        }
                    }
                    /*
                    These target specifiers are not implemented in Sentinel yet and so can't be imported
                    else if (sections[0].equals("EVENT"))
                    else if (sections[0].equals("FACTION"))
                    else if (sections[0].equals("FACTIONENEMIES"))
                    else if (sections[0].equals("TOWN"))
                    else if (sections[0].equals("NATIONENEMIES"))
                    else if (sections[0].equals("NATION"))
                    else if (sections[0].equals("WARTEAM"))
                    else if (sections[0].equals("TEAM"))
                    else if (sections[0].equals("CLAN"))
                    */
                }
            }        
            // Import ignores, remove default ignore OWNER
            sentinel.ignores.remove(SentinelTarget.OWNER);
            for (String t: sentry.ignoreTargets){
                if (t.contains("ENTITY:ALL")) {
                    sentinel.ignores.add(SentinelTarget.MOBS);
                    sentinel.ignores.add(SentinelTarget.PLAYERS);
                    sentinel.ignores.add(SentinelTarget.NPCS);
                }
                else if(t.contains("ENTITY:MONSTER")) {
                    sentinel.ignores.add(SentinelTarget.MONSTERS);
                }
                else if(t.contains("ENTITY:PLAYER")) {
                    sentinel.ignores.add(SentinelTarget.PLAYERS);
                }
                else if(t.contains("ENTITY:NPC")) {
                    sentinel.ignores.add(SentinelTarget.NPCS);
                }
                else if(t.contains("ENTITY:OWNER")) {
                    sentinel.ignores.add(SentinelTarget.OWNER);
                }
                else{
                    String[] sections = t.split(":");
                    if (sections.length != 2) {
                        // Invalid target identifier?
                        continue;
                    }
                    // Sentry was spacing tolerant, so we should be too.
                    sections[0] = sections[0].trim();
                    sections[1] = sections[1].trim();
                    if(sections[0].equals("NPC")) {
                        sentinel.npcNameIgnores.add(sections[1]);
                    }
                    else if (sections[0].equals("GROUP")) {
                        sentinel.groupIgnores.add(sections[1]);
                    }
                    else if(sections[0].equals("PLAYER")) {
                        sentinel.playerNameIgnores.add(sections[1]);
                    }
                    else if(sections[0].equals("ENTITY")) {
                        SentinelTarget target = SentinelTarget.forName(sections[1]);
                        if (target != null) {
                            sentinel.ignores.add(target);
                        }
                    }
                    /*
                    These target specifiers are not implemented in Sentinel yet and so can't be imported
                    else if (sections[0].equals("EVENT"))
                    else if (sections[0].equals("FACTION"))
                    else if (sections[0].equals("TOWN"))
                    else if (sections[0].equals("NATION"))
                    else if (sections[0].equals("WARTEAM"))
                    else if (sections[0].equals("TEAM"))
                    else if (sections[0].equals("CLAN"))
                    */
                }
            }
            sentinel.fightback = sentry.Retaliate;
            
            
            // We need to convert the absolutely insane way sentry handled heal rate, into seconds per health
            double hpHealedPerPeriod = 1;
            if (sentry.HealRate < .5) {
                hpHealedPerPeriod = .5 / sentry.HealRate;
            }
            // The healRate is both used to calculate the hp healed per period, as well as defining
            // the period itself.
            double secondsPerHpPoint = sentry.HealRate / hpHealedPerPeriod;
            // Finally convert to ticks for sentinel and check bounds
            sentinel.healRate = (int)Math.ceil(20 * secondsPerHpPoint);
            if (sentinel.healRate < SentinelTrait.healRateMin) {
                sentinel.healRate = SentinelTrait.healRateMin;
            }
            else if (sentinel.healRate > SentinelTrait.healRateMax) {
                sentinel.healRate = SentinelTrait.healRateMax;
            }
            
            double health = sentry.sentryHealth;
            if (health < SentinelTrait.healthMin) {
                health = SentinelTrait.healthMin;
            }
            else if (health > SentinelTrait.healthMax) {
                health = SentinelTrait.healthMax;
            }     
            sentinel.setHealth(health);
            sentinel.setInvincible(sentry.Invincible);
            sentinel.needsAmmo = false;
            // With sentries, they'll always attack anything they can see
            // Projectile range and chaseRange are always equal for them
            sentinel.range = sentry.sentryRange;
            sentinel.rangedChase = false;
            sentinel.respawnTime = sentry.RespawnDelaySeconds * 20;
            sentinel.safeShot = false;
            sentinel.spawnPoint = sentry.Spawn;
            
            // Sentry uses player name to determine who its guarding, we'll need to
            // convert to UUID if we can
            if (sentry.guardTarget != null && sentry.guardTarget.length() > 0) {
                OfflinePlayer op = Bukkit.getOfflinePlayer(sentry.guardTarget);
                if (op != null) {
                    UUID playerId = op.getUniqueId();
                    if (playerId != null) {
                        sentinel.setGuarding(playerId);
                    }         
                }
            }
        }
        return convertedCount;
    }
    
    
}
