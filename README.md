Sentinel NPCs: Combat NPCs for Spigot!
--------------------------------------

![AnimatedSentinel](http://i.imgur.com/VDwTzrs.gif)

**Version 1.7.6**: Compatible with Spigot 1.8.8(see Info section below), 1.9.4, 1.10.2, 1.11.2, 1.12.2, and 1.13.2

### Downloads

- **Download (Developmental builds)**: http://ci.citizensnpcs.co/job/Sentinel/
- **Download (Spigot releases)**: https://www.spigotmc.org/resources/sentinel.22017/
- **Download (Bukkit releases)**: http://dev.bukkit.org/bukkit-plugins/sentinel/

### Donations

- **Support Sentinel on Patreon!**: https://www.patreon.com/denizenscript
- Or, give a one-time donation via PayPal: https://one.denizenscript.com/donate

### Update Warnings

- Sentinel 1.7 **changes the API** structure of Sentinel - and adds documentation and **JavaDocs**, which can be found here: http://ci.citizensnpcs.co/job/Sentinel/javadoc/
    - Save data structure is also changed in the 1.7 updates.
    - If you need a pre-API rewrite version of Sentinel, you can get the last version of Sentinel (1.6.2) here: http://ci.citizensnpcs.co/job/Sentinel/191/
- Sentinel 1.7.2 is the last version that contained the **Sentry importer**. If you need to import old Sentry data, you must use a 1.7.2 build.
    - You can get that build here: http://ci.citizensnpcs.co/job/Sentinel/201/

### Info

Created by mcmonkey4eva on behalf of the Citizens and Denizen teams.

**Join us on Discord!**: https://discord.gg/Q6pZGSR in the `#sentinel-lobby` channel.

- **IMPORTANT:**
- NPC's won't do melee damage? In your config.yml file, toggle the setting that says "workaround damage" to true, and restart your server!
- NPC's won't do ranged damaged, or protection plugins causing problems? In your config.yml file, toggle the setting that says "enforce damage" to true, and restart your server!
- Note that back-support for older versions is sometimes limited. Compatibility is tested mainly on the most recent one or two versions. 1.8.8 servers are supported as the oldest option, but not fully functional. See notes here: https://wiki.citizensnpcs.co/Minecraft_1.8

### Usage

- First, get acquainted with Citizens in general for best luck using Sentinel.
- Second, download the plugin and put it into your server's `plugins` folder.
- Third, if you want to adjust the config, start the server to generate a config, then close the server and edit the config (open `plugins/Sentinel/config.yml` with a text editor) to your liking and finally restart the server.
- Now, to create your first Sentinel:
    - Select or create an NPC (`/npc sel` or `/npc create Bob`)
    - Run command: `/trait Sentinel`
    - Run command: `/npc equip`
    - Give the NPC items as needed, by right clicking the NPC with the wanted item. Generally you'll want to give the NPC armor and a weapon.
    - Run command: `/sentinel addtarget MONSTERS`
    - Spawn a zombie via creative inventory spawn eggs and watch it die!
    - Run command: `/sentinel help`
        - This will list all your options to edit the NPC's Sentinel settings.
            - Play with them freely, just be careful if you have other players around!
            - Do note, they won't attack you unless you run command: `/sentinel removeignore owner`.
- Examples:
    - To make your NPC attack sword wielders, use `/sentinel addtarget helditem:.*sword`

### Integrations

Sentinel integrates with a few external plugins, including:

- Vault, for permission group targets! (Use group:GROUP_HERE)
- Towny, for town targets! (Use towny:TOWN_HERE)
- Factions, for faction targets! (Use factions:FACTION_HERE, factionsenemy:NAME, factionsally:NAME)
- SimpleClans, for clan targets! (Use simpleclan:CLAN_NAME_HERE)
- War, for war team targets! (Use war_team:WAR_TEAM_NAME)
- CrackShot, to allow NPCs to fire CrackShot weapons.

Sentinel is integrated into by external plugins as well, including:

- Denizen (using Depenizen as a bridge), for scriptable targeting!
    - Use held_denizen_item:DENIZEN_ITEM_NAME for targeting based on targets holding a Denizen item
    - or use denizen_proc:PROCEDURE_SCRIPT_NAME to fire a procedure script with first procedure context (named 'entity' by default) being the entity that might be a target. Determine 'true' or 'false' to indicate whether the entity is a target.

### Commands

- **Informational commands...**
    - /sentinel help - Shows help info.
    - /sentinel info - Shows info on the current NPC.
    - /sentinel stats - Shows statistics about the current NPC.
    - /sentinel targets - Shows the targets of the current NPC.
    - /sentinel ignores - Shows the ignore targets of the current NPC.
    - /sentinel avoids - Shows the avoid targets of the current NPC.
- **Administrative commands...**
    - /sentinel debug - Toggles debug output to console.
    - /sentinel reload - Reloads the configuration file.
- **NPC control commands...**
    - /sentinel kill - Kills the NPC.
    - /sentinel respawn - Respawns the NPC.
    - /sentinel forgive - Forgives all current targets.
    - /sentinel guard [PLAYERNAME] - Makes the NPC guard a specific player. Don't specify a player to stop guarding.
- **NPC targeting commands...**
    - /sentinel addtarget TYPE - Adds a target.
    - /sentinel removetarget TYPE - Removes a target.
    - /sentinel addignore TYPE - Ignores a target.
    - /sentinel removeignore TYPE - Allows targeting a target.
    - /sentinel addavoid TYPE - Avoids a target.
    - /sentinel removeavoid TYPE - Stops avoiding a target.
- **NPC configuration commands...**
    - /sentinel avoidrange RANGE - Sets the distance to try to keep from threats.
    - /sentinel range RANGE - Sets the NPC's maximum attack range.
    - /sentinel damage DAMAGE - Sets the NPC's attack damage.
    - /sentinel armor ARMOR - Sets the NPC's armor level.
    - /sentinel health HEALTH - Sets the NPC's health level.
    - /sentinel attackrate RATE ['ranged'] - Changes the rate at which the NPC attacks, in seconds. Either ranged or close modes.
    - /sentinel healrate RATE - Changes the rate at which the NPC heals, in seconds.
    - /sentinel respawntime TIME - Changes the time it takes for the NPC to respawn, in seconds.
    - /sentinel chaserange RANGE - Changes the maximum distance an NPC will run before returning to base.
    - /sentinel drops - Changes the drops of the current NPC.
    - /sentinel targettime TIME - Sets the NPC's enemy target time limit in seconds.
    - /sentinel speed SPEED - Sets the NPC's movement speed modifier.
    - /sentinel guarddistance MINIMUM_DISTANCE [SELECTION_RANGE] - Sets the NPC's minimum guard distance (how far you must go before the NPC moves to keep up) and selection range (how close it will try to get to you).
    - /sentinel spawnpoint - Changes the NPC's spawn point to its current location, or removes it if it's already there.
    - /sentinel greeting GREETING - Sets a greeting message for the NPC to say.
    - /sentinel warning WARNING - Sets a warning message for the NPC to say.
    - /sentinel greetrange RANGE - Sets how far a player can be from an NPC before they are greeted.
    - /sentinel accuracy OFFSET - Sets the accuracy of an NPC.
    - /sentinel squad SQUAD - Sets the NPC's squad name (null for none).
    - /sentinel reach REACH - Sets the NPC's reach (how far it can punch).
- **Toggleable NPC configuration commands...**
    - /sentinel invincible ['true'/'false'] - Toggles whether the NPC is invincible.
    - /sentinel fightback ['true'/'false'] - Toggles whether the NPC will fight back.
    - /sentinel runaway ['true'/'false'] - Toggles whether the NPC will run away when attacked.
    - /sentinel needammo ['true'/'false'] - Toggles whether the NPC will need ammo.
    - /sentinel safeshot ['true'/'false'] - Toggles whether the NPC will avoid damaging non-targets.
    - /sentinel chaseclose ['true'/'false'] - Toggles whether the NPC will chase while in 'close quarters' fights.
    - /sentinel chaseranged ['true'/'false'] - Toggles whether the NPC will chase while in ranged fights.
    - /sentinel enemydrops ['true'/'false'] - Toggles whether enemy mobs of this NPC drop items.
    - /sentinel autoswitch ['true'/'false'] - Toggles whether the NPC automatically switches items.
    - /sentinel realistic ['true'/'false'] - Toggles whether the NPC should use "realistic" targeting logic (don't attack things you can't see).

### Sentry user?

- Type "/sentinel sentryimport" on a server running both Sentry and Sentinel to instantly transfer all data to Sentinel!
- Sentinel 1.7.2 is the last version that contained the **Sentry importer**. If you need to import old Sentry data, you must use a 1.7.2 build.
    - You can get that build here: http://ci.citizensnpcs.co/job/Sentinel/201/

### Permissions
- sentinel.basic for the /sentinel command
- sentinel.admin to edit other player's Sentinel NPCs.
- sentinel.greet for commands: greeting, warning, greetrange
- sentinel.info for commands: info, stats, targets
- Everything else is "sentinel.X" where "X" is the command name, EG "sentinel.damage".

### Targets

These are all valid targets and ignores:

- Common/basic targets: NPCS, OWNER, PASSIVE_MOB, MOBS, MONSTERS, PLAYERS
- Basic entities: PIGS, OCELOTS, COWS, RABBITS, SHEEP, CHICKENS, HORSES, MUSHROOM_COW, IRON_GOLEMS, SQUIDS, VILLAGER, WOLF, SNOWMEN, WITCH, GUARDIANS, SHULKERS, CREERERS, SKELETONS, ZOMBIES, MAGMA_CUBES, ZOMBIE_PIGMEN, SILVERFISH, BATS, BLAZES, GHASTS, GIANTS, SLIME, SPIDER, CAVE_SPIDERS, ENDERMEN, ENDERMITES, WITHER, ENDERDRAGON
- In 1.9 or higher: SHULKERS
- In 1.10 or higher: POLAR_BEARS
- In 1.11 or higher: VEXES, DONKEYS, LLAMAS, MULES, HUSKS, ELDER_GUARDIANS, EVOKERS, SKELETON_HORSES, STRAYS, ZOMBIE_VILLAGERS, ZOMBIE_HORSES, WITHER_SKELETONS, VINDICATORS
- In 1.12 or higher: PARROTS, ILLUSIONERS
- In 1.13 or higher: DOLPHIN, DROWNED, COD, SALMON, PUFFERFISH, TROPICAL_FISH, PHANTOM, TURTLE
- Also allowed: player:NAME(REGEX), npc:NAME(REGEX), entityname:NAME(REGEX), helditem:MATERIALNAME(REGEX), group:GROUPNAME(EXACT)
- Also, event:pvp/pvnpc/pve/pvsentinel
- Also, sbteam:SCOREBOARD\_TEAM\_HERE
- Also, healthabove:PERCENTAGE and healthbelow:PERCENTAGE
- Also, permission:PERM.KEY
- Also, squad:SENTINEL\_SQUAD\_NAME
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
    - CrackShot guns!
- Respawning can be set to "-1" to cause the NPC to delete itself on death, or "0" to prevent respawn.
- Sentinels will guard a point or path if either is set using the command "`/npc path`"
- To make a ghast or blaze fire fireballs, give them a blazerod!
- Damage value for a Sentinel NPC can be set to "-1" to auto-calculate from held item (otherwise, it will used as a raw HP damage amount).
- Armor value can be set to "-1" to auto-calculate from equipped armor (otherwise, set a value between 0.0 and 1.0 to indicate how much of any damage will be blocked).

### Integrating Your Plugin With Sentinel

If you're building a separate plugin you would like to integrate into Sentinel, you should:

- Use Maven to link the project properly...
- Use the Citizens repository:
```xml
        <repository>
            <id>citizens-repo</id>
            <url>http://repo.citizensnpcs.co</url>
        </repository>
```
- And add Sentinel as a `provided` dependency (be sure to change the version to match the current version available):
```xml
        <dependency>
            <groupId>org.mcmonkey</groupId>
            <artifactId>sentinel</artifactId>
            <version>1.7.6-SNAPSHOT</version>
            <type>jar</type>
            <scope>provided</scope>
        </dependency>
```
- You will also want to link Citizens in Maven if you haven't already - see https://wiki.citizensnpcs.co/API for relevant information on that.

----

- Add a `depend` or `softdepend` (as relevant) on `Sentinel` to your `plugin.yml` file. See sample of how Sentinel does this to depend on other plugins here: https://github.com/mcmonkeyprojects/Sentinel/blob/master/src/main/resources/plugin.yml
- When possible, take advantage of the `SentinelIntegration` class: http://ci.citizensnpcs.co/job/Sentinel/javadoc/org/mcmonkey/sentinel/SentinelIntegration.html
    - Extend the class (with your own custom class) and implement whichever methods you need. See samples of integrations available here: https://github.com/mcmonkeyprojects/Sentinel/tree/master/src/main/java/org/mcmonkey/sentinel/integration
    - Within your plugin's `onEnable`, Register the class by calling `SentinelPlugin.integrations.add(new YourIntegration());` where `YourIntegration` is the integration class you created.
- You might also benefit from events like the `SentinelAttackEvent` http://ci.citizensnpcs.co/job/Sentinel/javadoc/org/mcmonkey/sentinel/events/SentinelAttackEvent.html
- If you're lost, feel free to ask for help using the help channels listed below.

### Need help using Sentinel? Try one of these places:

- **Discord** (Modern): https://discord.gg/Q6pZGSR in the `#sentinel-lobby` channel.
- **Spigot Info Page** (Modern): https://www.spigotmc.org/resources/sentinel.22017/
- The GitHub issues page

### Dependencies

- **Spigot (Plugin-ready server mod)**: https://www.spigotmc.org/
- **Citizens2 (NPC engine)**: https://github.com/CitizensDev/Citizens2/

#### Also check out:

- **Denizen (Powerful script engine)**: https://github.com/DenizenScript/Denizen-For-Bukkit
