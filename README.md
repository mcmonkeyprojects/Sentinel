Sentinel NPCs: Combat NPCs for Spigot!
--------------------------------------

![AnimatedSentinel](http://i.imgur.com/VDwTzrs.gif)

**Version 0.9 beta**: Compatible with Spigot 1.11

- **Download (Developmental builds)**: http://ci.citizensnpcs.co/job/Sentinel/
- **Download (Spigot releases)**: https://www.spigotmc.org/resources/sentinel.22017/
- **Download (Bukkit releases)**: http://dev.bukkit.org/bukkit-plugins/sentinel/

### Info

Created by mcmonkey4eva on behalf of the Citizens and Denizen teams.

- **IMPORTANT:**
- NPC's won't do melee damage? In your config.yml file, toggle the setting that says "workaround damage" to true, and restart your server!
- NPC's won't do ranged damaged, or protection plugins causing problems? In your config.yml file, toggle the setting that says "enforce damage" to true, and restart your server!

### Usage

- First, get acquainted with Citizens in general for best luck using Sentinel.
- Second, download the plugin and put it into your server's `plugins` folder.
- Third, start the server to generate a config, then close the server and edit the config to your liking and finally restart the server.
- Now, to create your first Sentinel:
	- Select or create an NPC (`/npc sel` or `/npc create Bob`)
	- Run command: `/trait Sentinel`
	- Run command: `/npc equip`
	- Give the NPC items as needed, by right click the NPC with the wanted item.
	- Run command: `/sentinel addtarget MONSTERS`
	- Spawn a zombie via creative inventory eggs and watch it die!
	- Run command: `/sentinel help`
		- This will list all your options to edit the NPC's Sentinel settings.
			- Play with them freely, just be careful if you have other players around!
			- Do note, they won't attack you unless you run command: `/sentinel removeignore owner`.
- Examples:
	- To make your NPC attack sword wielders, use `/sentinel addtarget helditem:.*sword`

### Integrations

Sentinel integrations with a few external plugins, including:

- Vault, for permission group targets! (Use group:GROUP_HERE)
- Towny, for town targets! (Use towny:TOWN_HERE)
- Factions, for faction targets! (Use factions:FACTION_HERE)

### Commands

- /sentinel help - Shows help info.
- /sentinel addtarget TYPE - Adds a target.
- /sentinel removetarget TYPE - Removes a target.
- /sentinel addignore TYPE - Ignores a target.
- /sentinel removeignore TYPE - Allows targetting a target.
- /sentinel range RANGE - Sets the NPC's maximum attack range.
- /sentinel damage DAMAGE - Sets the NPC's attack damage.
- /sentinel armor ARMOR - Sets the NPC's armor level.
- /sentinel health HEALTH - Sets the NPC's health level.
- /sentinel attackrate RATE ['ranged'] - Changes the rate at which the NPC attacks, in ticks. Either ranged or close modes.
- /sentinel healrate RATE - Changes the rate at which the NPC heals, in ticks.
- /sentinel respawntime TIME - Changes the time it takes for the NPC to respawn, in ticks.
- /sentinel chaserange RANGE - Changes the maximum distance an NPC will run before returning to base.
- /sentinel guard (PLAYERNAME) - Makes the NPC guard a specific player. Don't specify a player to stop guarding.
- /sentinel invincible - Toggles whether the NPC is invincible.
- /sentinel fightback - Toggles whether the NPC will fight back.
- /sentinel needammo - Toggles whether the NPC will need ammo.
- /sentinel safeshot - Toggles whether the NPC will avoid damaging non-targets.
- /sentinel chaseclose - Toggles whether the NPC will chase while in 'close quarters' fights.
- /sentinel chaseranged - Toggles whether the NPC will chase while in ranged fights.
- /sentinel drops - Changes the drops of the current NPC.
- /sentinel spawnpoint - Changes the NPC's spawn point to its current location, or removes it if it's already there.
- /sentinel forgive - Forgives all current targets.
- /sentinel enemydrops - Toggles whether enemy mobs of this NPC drop items.
- /sentinel info - Shows info on the current NPC.
- /sentinel stats - Shows statistics about the current NPC.
- /sentinel targets - Shows the targets of the current NPC.
- /sentinel kill - Kills the NPC.
- /sentinel respawn - Respawns the NPC.
- /sentinel targettime TIME - Sets the NPC's enemy target time limit.
- /sentinel speed - Sets the NPC's movement speed modifier.
- /sentinel autoswitch - Toggles whether the NPC automatically switches items.
- /sentinel targettime TIME - Sets the NPCs enemy target time limit.
- /sentinel greeting GREETING - Sets a greeting message for the NPC to say.
- /sentinel warning WARNING - Sets a warning message for the NPC to say.
- /sentinel greetrange RANGE - Sets how far a player can be from an NPC before they are greeted.
- /sentinel accuracy OFFSET - Sets the accuracy of an NPC.
- /sentinel squad SQUAD - Sets the NPC's squad name (null for none).

### Sentry user?

Type "/sentinel sentryimport" on a server running both Sentry and Sentinel to instantly transfer all data to Sentinel!

### Permissions
- sentinel.basic for the /sentinel command
- sentinel.admin to edit other player's Sentinel NPCs.
- sentinel.greet for commands: greeting, warning, greetrange
- sentinel.info for commands: info, stats, targets
- Everything else is "sentinel.X" where "X" is the command name, EG "sentinel.damage".

### Targets

These are all valid targets and ignores:

- Primary set: NPCS, OWNER, PASSIVE_MOB, MOBS, MONSTERS, PLAYERS, PIGS, OCELOTS, COWS, RABBITS, SHEEP, CHICKENS, HORSES, MUSHROOM_COW, IRON_GOLEMS, SQUIDS, VILLAGER, WOLF, SNOWMEN, WITCH, GUARDIANS, SHULKERS, CREERERS, SKELETONS, ZOMBIES, MAGMA_CUBES, ZOMBIE_PIGMEN, SILVERFISH, BATS, BLAZES, GHASTS, GIANTS, SLIME, SPIDER, CAVE_SPIDERS, ENDERMEN, ENDERMITES, WITHER, ENDERDRAGON
- Also allowed: player:NAME(REGEX), npc:NAME(REGEX), entityname:NAME(REGEX), helditem:MATERIALNAME(REGEX), group:GROUPNAME(EXACT)
- Also, event:pvp/pvnpc/pve/pvsentinel
- Also, sbteam:SCOREBOARD\_TEAM\_HERE
- Also, healthabove:PERCENTAGE and healthbelow:PERCENTAGE
- Also anything listed in the integrations section above!

### Some random supported things

- Weapons:
	- Fists
	- Swords/tools
	- Bow
		- equip NPC with arrows of any given type in their inventory to set fired arrow type!
	- Blaze rod (shoots fire balls!)
	- Potions (splash, lingering)
	- Nether star (strikes lightning!)
	- Spectral arrow (makes the target glow, without damaging it.)
		- (To make a target glow ++ damage it, equip a bow + arm it with spectral arrows!)
	- Snowballs
	- Eggs
	- Ender Pearls (Causes the target to get flung into the air!)
	- Skulls (Dangerous wither skull explosions!)
- Respawning can be set to "-1" to cause the NPC to delete itself on death, or "0" to prevent respawn.
- Sentinels will guard a point or path if either is set using the command "`/npc path`"
- To make a ghast or blaze fire fireballs, give them a blazerod!

### Need help using Sentinel? Try one of these places:

- **IRC** (Modern): http://webchat.esper.net/?channels=citizens
	- (irc.esper.net in the channel #citizens)
- **Spigot Info Page** (Modern): https://www.spigotmc.org/resources/sentinel.22017/

### Dependencies

- **Spigot (Plugin-ready server mod)**: https://www.spigotmc.org/
- **Citizens2 (NPC engine)**: https://github.com/CitizensDev/Citizens2/

#### Also check out:

- **Denizen (Powerful script engine)**: https://github.com/DenizenScript/Denizen-For-Bukkit
