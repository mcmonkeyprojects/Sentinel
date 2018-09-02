package org.mcmonkey.sentinel.external;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.Listener;

import net.aufdemrand.sentry.SentryInstance;
import net.aufdemrand.sentry.SentryTrait;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import org.mcmonkey.sentinel.SentinelPlugin;
import org.mcmonkey.sentinel.SentinelTarget;
import org.mcmonkey.sentinel.SentinelTrait;

public class SentryImport implements Listener {

    /**
     * Converts Sentry NPCs to Sentinel NPCs. Returns the number of NPCs converted.
     */
    public static int PerformImport() {
        int convertedCount = 0;
        for (NPC npc : CitizensAPI.getNPCRegistry()) {
            if (!npc.hasTrait(SentryTrait.class)) {
                continue;
            }
            SentryInstance sentry = npc.getTrait(SentryTrait.class).getInstance();
            if (sentry == null) {
                continue;
            }
            convertedCount++;
            if (!npc.hasTrait(SentinelTrait.class)) {
                npc.addTrait(SentinelTrait.class);
            }
            SentinelTrait sentinel = npc.getTrait(SentinelTrait.class);
            sentinel.armor = Math.min(sentry.Armor * 0.1, 1.0);
            sentinel.attackRate = (int) (sentry.AttackRateSeconds * 20);
            if (sentinel.attackRate < SentinelPlugin.instance.tickRate) {
                sentinel.attackRate = SentinelPlugin.instance.tickRate;
            }
            else if (sentinel.attackRate > SentinelTrait.attackRateMax) {
                sentinel.attackRate = SentinelTrait.attackRateMax;
            }
            sentinel.chaseRange = sentry.sentryRange;
            sentinel.closeChase = true;
            sentinel.rangedChase = false;
            sentinel.damage = sentry.Strength;
            sentinel.enemyDrops = sentry.KillsDropInventory;
            sentinel.fightback = sentry.Retaliate;
            double hpHealedPerPeriod = 1;
            if (sentry.HealRate < .5) {
                hpHealedPerPeriod = .5 / sentry.HealRate;
            }
            double secondsPerHpPoint = sentry.HealRate / hpHealedPerPeriod;
            sentinel.healRate = (int) (20 * secondsPerHpPoint);
            if (sentinel.healRate < SentinelPlugin.instance.tickRate) {
                sentinel.healRate = SentinelPlugin.instance.tickRate;
            }
            else if (sentinel.healRate > SentinelTrait.healRateMax) {
                sentinel.healRate = SentinelTrait.healRateMax;
            }

            double health = sentry.sentryHealth;
            if (health < SentinelTrait.healthMin) {
                health = SentinelTrait.healthMin;
            }
            else if (health > SentinelPlugin.instance.maxHealth) {
                health = SentinelPlugin.instance.maxHealth;
            }
            sentinel.setHealth(health);
            sentinel.setInvincible(sentry.Invincible);
            sentinel.needsAmmo = false;
            sentinel.range = sentry.sentryRange;
            sentinel.respawnTime = sentry.RespawnDelaySeconds * 20;
            sentinel.safeShot = false;
            sentinel.spawnPoint = sentry.Spawn;
            if (sentry.guardTarget != null && sentry.guardTarget.length() > 0) {
                OfflinePlayer op = Bukkit.getOfflinePlayer(sentry.guardTarget);
                if (op != null) {
                    UUID playerId = op.getUniqueId();
                    if (playerId != null) {
                        sentinel.setGuarding(playerId);
                    }
                }
            }
            sentinel.targets.clear();
            sentinel.eventTargets.clear();
            sentinel.playerNameTargets.clear();
            sentinel.npcNameTargets.clear();
            sentinel.groupTargets.clear();
            sentinel.ignores.clear();
            sentinel.playerNameIgnores.clear();
            sentinel.npcNameIgnores.clear();
            sentinel.groupIgnores.clear();
            for (String t : sentry.validTargets) {
                if (t.contains("ENTITY:ALL")) {
                    sentinel.targets.add(SentinelTarget.forName("MOBS").name());
                    sentinel.targets.add(SentinelTarget.PLAYERS.name());
                    sentinel.targets.add(SentinelTarget.NPCS.name());
                }
                else if (t.contains("ENTITY:MONSTER")) {
                    sentinel.targets.add(SentinelTarget.forName("MONSTERS").name());
                }
                else if (t.contains("ENTITY:PLAYER")) {
                    sentinel.targets.add(SentinelTarget.PLAYERS.name());
                }
                else if (t.contains("ENTITY:NPC")) {
                    sentinel.targets.add(SentinelTarget.NPCS.name());
                }
                else if (t.contains("EVENT:PVP")) {
                    sentinel.eventTargets.add("pvp");
                }
                else if (t.contains("EVENT:PVE")) {
                    sentinel.eventTargets.add("pve");
                }
                else if (t.contains("EVENT:PVNPC")) {
                    sentinel.eventTargets.add("pvnpc");
                }
                else {
                    String[] sections = t.split(":");
                    if (sections.length != 2) {
                        continue;
                    }
                    sections[0] = sections[0].trim();
                    sections[1] = sections[1].trim();
                    if (sections[0].equals("NPC")) {
                        sentinel.npcNameTargets.add(sections[1]);
                    }
                    else if (sections[0].equals("GROUP")) {
                        sentinel.groupTargets.add(sections[1]);
                    }
                    else if (sections[0].equals("PLAYER")) {
                        sentinel.playerNameTargets.add(sections[1]);
                    }
                    else if (sections[0].equals("ENTITY")) {
                        SentinelTarget target = SentinelTarget.forName(sections[1]);
                        if (target != null) {
                            sentinel.targets.add(target.name());
                        }
                    }
                }
            }
            for (String t : sentry.ignoreTargets) {
                if (t.contains("ENTITY:ALL")) {
                    sentinel.ignores.add(SentinelTarget.forName("MOBS").name());
                    sentinel.ignores.add(SentinelTarget.PLAYERS.name());
                    sentinel.ignores.add(SentinelTarget.NPCS.name());
                }
                else if (t.contains("ENTITY:MONSTER")) {
                    sentinel.ignores.add(SentinelTarget.forName("MONSTERS").name());
                }
                else if (t.contains("ENTITY:PLAYER")) {
                    sentinel.ignores.add(SentinelTarget.PLAYERS.name());
                }
                else if (t.contains("ENTITY:NPC")) {
                    sentinel.ignores.add(SentinelTarget.NPCS.name());
                }
                else if (t.contains("ENTITY:OWNER")) {
                    sentinel.ignores.add(SentinelTarget.OWNER.name());
                }
                else {
                    String[] sections = t.split(":");
                    if (sections.length != 2) {
                        // Invalid target identifier?
                        continue;
                    }
                    // Sentry was spacing tolerant, so we should be too.
                    sections[0] = sections[0].trim();
                    sections[1] = sections[1].trim();
                    if (sections[0].equals("NPC")) {
                        sentinel.npcNameIgnores.add(sections[1]);
                    }
                    else if (sections[0].equals("GROUP")) {
                        sentinel.groupIgnores.add(sections[1]);
                    }
                    else if (sections[0].equals("PLAYER")) {
                        sentinel.playerNameIgnores.add(sections[1]);
                    }
                    else if (sections[0].equals("ENTITY")) {
                        SentinelTarget target = SentinelTarget.forName(sections[1]);
                        if (target != null) {
                            sentinel.ignores.add(target.name());
                        }
                    }
                }
            }
            npc.removeTrait(SentryTrait.class);
        }
        return convertedCount;
    }
}
