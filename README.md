Sentinel NPCs: Combat NPCs for Spigot!
--------------------------------------

### Table Of Contents
* [Downloads](#downloads)
* [Donations](#donations)
* [Info](#info)
* [Basic Usage](#basic-usage)
* [Frequently Asked Questions](#frequently-asked-questions)
* [Common Issues](#common-issues)
* [Example NPC Setups](#Example-NPC-Setups)
* [Useful Things To Know](#Useful-Things-To-Know)
* [Plugin Integrations](#Plugin-Integrations)
* [Commands](#Commands)
* [Permissions](#Permissions)
* [Targets](#Targets)
* [Supported Weapon Types](#Supported-Weapon-Types)
* [Integrating Your Plugin With Sentinel](#Integrating-Your-Plugin-With-Sentinel)
* [Need help using Sentinel?](#need-help-using-sentinel-try-one-of-these-places)
* [Dependencies](#Dependencies)
* [Sentry user?](#Sentry-user?)
* [Update Warnings](#Update-Warnings)
* [License](#licensing-pre-note)

![AnimatedSentinel](https://i.imgur.com/VDwTzrs.gif)

**Version 2.7.1**: Compatible with Spigot 1.8.8 through 1.19.2 (Primarily targeted at 1.19.2) - see 'Common Issues' section below if on older supported versions)

### Downloads

- **Download (Developmental builds)**: https://ci.citizensnpcs.co/job/Sentinel/
- **Download (Spigot releases)**: https://www.spigotmc.org/resources/sentinel.22017/

### Donations

- **Support Sentinel via GitHub Sponsors!**: https://github.com/sponsors/mcmonkey4eva
- Or, give a one-time donation via PayPal: https://one.denizenscript.com/donate

### Info

Created by mcmonkey4eva on behalf of the Citizens and Denizen teams.

**Join us on Discord!**: https://discord.gg/Q6pZGSR in the `#sentinel` channel.

### Basic Usage

- First, get acquainted with Citizens in general for best luck using Sentinel.
- Second, download the plugin and put it into your server's `plugins` folder.
- Third, if you want to adjust the config, start the server to generate a config, then close the server and edit the config (open `plugins/Sentinel/config.yml` with any text editor) to your liking and finally restart the server.
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
    - Look through the commands on this readme and the example NPC setups.

### Frequently Asked Questions

- **"How Do I Make The NPC Go Back To A Spot After It's Done Fighting?"**
    - Set a single point in `/npc path`. It will stand and guard that spot, and return there when not in combat. Consider also setting `/sentinel spawnpoint` there.
- **"How Do I Make An NPC Run Commands When It Dies?"** or **"How Do I Make An NPC Do Some Custom Behavior At Some Custom Time?"**
    - Sentinel is a plugin that adds combat features to Citizens NPCs. It is not a general purpose scripting engine and doesn't try to be. If you want custom scripted behavior with NPCs, use [Denizen](https://guide.denizenscript.com/) for that.

### Common Issues

- **"My Sentinel NPC won't attack me!"**
    - Sentinel NPC's do not attack their owner by default as a safety measure (to avoid you dying while setting the NPC up). To allow the NPC to attack you, use command `/sentinel removeignore owner`
- **"My NPCs aren't respawning at their `spawnpoint` value / are respawning just where they died!"**
    - You likely have an unrelated respawner taking control. The most common cause of this would be the Citizens native respawn functionality... if that's been activated, you can disable that by using command `/npc respawn -1`
- **"My NPCs aren't dropping their items when they die! I used `/sentinel drops` and everything!"**
    - You may have a plugin or world setting blocking drops. If this is the case, the easiest solution is to just enable `workaround drops` in the config (Find your `Sentinel/config.yml` file, open it in a text editor, and change that value from `false` to `true`, then use `/sentinel reload`).
- **"My NPCs aren't taking/giving damage in no-PvP zones!"**
    - If you're using WorldGuard for anti-PvP... update! WG 7.0.0 has patches to fix this issue! If you're using a different plugin or can't update, consider enabling the `workaround damage` (for melee damage issues) and/or `enforce damage` (for ranged damage issues) options in the config. You might also consider simply not putting combat NPCs in no-PvP regions (enable PvP in the region, or move them to dedicated combat areas).
- **"My NPC's aren't taking/giving damage but it's not a no-PvP zone!"**
    - You likely have *some plugin* or world setting screwing with PvP or damage in general, even if you don't realize it. Find out which plugin and fix it through [Plugin Conflict Testing & Fixing](https://wiki.citizensnpcs.co/Plugin_Conflict). If you can't fix it, consider enabling the `workaround damage` (for melee damage issues) and/or `enforce damage` (for ranged damage issues) options in the config.
- Also: Note that back-support for older versions is sometimes limited. Compatibility is tested mainly on the most recent one or two versions. 1.8.8 servers are supported as the oldest option, but not fully functional. See notes here: https://wiki.citizensnpcs.co/Minecraft_1.8

### Example NPC Setups

Here are a few examples of how you might setup and configure an NPC

- **Personal Guard:**
    - `/npc create MyGuard`
    - `/trait sentinel`
    - `/sentinel guard yournamehere` and fill 'yournamehere' with your in-game username.
    - `/sentinel addtarget monsters`
    - `/npc equip` then give the NPC any armor and items you want, then `/npc equip` again to close the editor.
    - `/sentinel addavoid creepers`
    - `/sentinel addignore npcs`
- **Town Guard:**
    - `/npc create &b-Guard-`
    - `/trait sentinel`
    - `/npc path` then click the block the guard should stand on, then `/npc path` again.
    - `/sentinel addtarget monsters`
    - `/sentinel addtarget event:pvp`
    - `/sentinel addtarget event:pvsentinel`
    - `/sentinel spawnpoint`
    - `/npc equip` then give the NPC any armor and items you want, then `/npc equip` again to close the editor.
    - `/sentinel squad town1_guards` (this will put all guards of the town in the same 'squad', so they will share target info)
    - `/sentinel greeting Welcome to town 1!`
    - `/sentinel warning You're not welcome here!`
    - `/sentinel greetrange 80`
    - `/sentinel range 50`
    - `/sentinel chaserange 70`
    - `/sentinel realistic true`
    - `/sentinel removeignore owner`
- If you want a guard that attacks anyone who holds up a sword, use:
    - `/sentinel addtarget helditem:.*sword`
- If you want a guard that uses either a bow or a sword depending on how close the target is, use:
    - `/npc inventory` and add the second weapon to their inventory
    - `/sentinel autoswitch true`
- **Town Healer NPC:**
    - `/npc create &b-Healer-`
    - `/trait sentinel`
    - `/npc equip` then give the NPC a health potion and any armor, then `/npc equip` again.
    - `/sentinel addtarget allinone:player|healthbelow:90` to target players below 90% health.
    - `/sentinel addignore allinone:player|healthabove:90` to ignore players who get above 90% health.
    - `/sentinel fightback false`
    - `/sentinel range 10`
    - `/sentinel removeignore owner`
    - `/sentinel greeting Hello there traveler!`
    - `/sentinel warning You look hurt, let me heal you!`
    - `/sentinel greetrange 10`
    - `/sentinel spawnpoint`
    - `/sentinel invincible true`

### Useful Things To Know

- Respawning can be set to "-1" to cause the NPC to delete itself on death, or "0" to prevent respawn.
- Sentinel NPCs will guard a single point or entire path if either is set using the command `/npc path`. This means they will still within their chaserange of that point or path, and return to it when out of combat.
- To give any non-equippable mob-type a weapon, use `/npc inventory` and add the item to the first (top-left) slot.
- To make a ghast or blaze fire fireballs, give them a blazerod!
- Damage value for a Sentinel NPC can be set to "-1" to auto-calculate from held item (otherwise, it will used as a raw HP damage amount).
- Armor value can be set to "-1" to auto-calculate from equipped armor (otherwise, set a value between 0.0 and 1.0 to indicate how much of any damage will be blocked).
- To make mobs target or ignore the Sentinel NPC, use `/npc targetable`
- To use spaces in target names, simply wrap the argument in "quotes", like `/sentinel addtarget "npc:some long name here"`

### Plugin Integrations

Sentinel integrates with a few external plugins, including:

- [Vault](https://www.spigotmc.org/resources/vault.34315/), for permission group targets! (Use `group:GROUP_HERE`)
- [Towny](https://www.spigotmc.org/resources/towny-advanced.72694/), for town targets! (Use `towny:TOWN_HERE`, `nation:NATION_HERE`, `nationenemies:NATION_HERE`, `nationallies:NATION_HERE`)
- [Factions](https://www.spigotmc.org/resources/factions.1900/), for faction targets! (Use `factions:FACTION_HERE`, `factionsenemy:NAME`, `factionsally:NAME`)
- [SimpleClans](https://www.spigotmc.org/resources/simpleclans.71242/), for clan targets! (Use `simpleclan:CLAN_NAME_HERE`)
- [War](https://www.spigotmc.org/resources/war.11413/), for war team targets! (Use `war_team:WAR_TEAM_NAME`)
- [SimplePets](https://www.spigotmc.org/resources/simplepets.14124/), for pet type targets! (Use `simplepet:PET_NAME_REGEX` ... in particular useful for `/sentinel addignore simplepet:.*` to ignore all pets)
- [CrackShot](https://www.spigotmc.org/resources/crackshot-guns.48301/), to allow NPCs to fire CrackShot weapons (just put the weapon in their hand).
- [WorldGuard](https://enginehub.org/worldguard/), to define WorldGuard region limits! Use `/sentinel wgregion [region name]` to force the NPC to stay inside that region.

Sentinel is integrated into by external plugins as well, including:

- [Denizen](https://denizenscript.com/) (using [Depenizen](https://github.com/DenizenScript/Depenizen/blob/master/README.md) as a bridge), for scriptable targeting!
    - Use `held_denizen_item:DENIZEN_ITEM_NAME` for targeting based on targets holding a Denizen item.
    - or use `denizen_proc:PROCEDURE_SCRIPT_NAME` to fire a procedure script.
        - First procedure context (named `entity`) is the entity that might be a target.
        - Second procedure context (named `context`) is the optional user-input context info.
            - To use this, do: `denizen_proc:PROCEDURE_SCRIPT_NAME:SOME_CONTEXT_HERE`
        - Recommended that you add to the proc `definitions: entity|context` to avoid script-checker errors.
        - Determine `true` or `false` to indicate whether the entity is a target.
    - Also check the Denizen meta docs - type `!search sentinel` in `#bot-spam` on the Denizen support Discord.
- [BeautyQuests](https://www.spigotmc.org/resources/beautyquests.39255/) for Quest-based targeting
    - Use `quest_in:QUEST_ID` to target players with the given quest started.
    - Use `quest_finished:QUEST_ID` to target players who have already finished the given quest.
- [QualityArmory](https://www.spigotmc.org/resources/quality-armory.47561/), to allow NPCs to fire QA weapons (just put the weapon in their hand).
- If you develop a publicly available Spigot plugin that has a Sentinel integration, please [let me known on Discord](https://discord.gg/Q6pZGSR) so I can add it here!

### Commands

- **Informational commands:**
    - /sentinel help - Shows help info.
    - /sentinel info - Shows info on the current NPC.
    - /sentinel stats - Shows statistics about the current NPC.
    - /sentinel targets - Shows the targets of the current NPC.
    - /sentinel ignores - Shows the ignore targets of the current NPC.
    - /sentinel avoids - Shows the avoid targets of the current NPC.
- **Administrative commands:**
    - /sentinel debug - Toggles debug output to console.
    - /sentinel reload - Reloads the configuration file.
- **NPC control commands:**
    - /sentinel kill - Kills the NPC.
    - /sentinel respawn - Respawns the NPC.
    - /sentinel forgive - Forgives all current targets.
    - /sentinel forgive \[id/name\] - Forgives the specified entity ID or player name.
    - /sentinel guard \[PLAYERNAME\]/npc:\[ID\] - Makes the NPC guard a specific player or NPC. Don't specify a player to stop guarding.
- **NPC targeting commands:**
    - /sentinel addtarget TYPE - Adds a target.
    - /sentinel removetarget TYPE - Removes a target.
    - /sentinel addignore TYPE - Ignores a target.
    - /sentinel removeignore TYPE - Allows targeting a target.
    - /sentinel addavoid TYPE - Avoids a target.
    - /sentinel removeavoid TYPE - Stops avoiding a target.
- **NPC configuration commands:**
    - /sentinel avoidrange RANGE - Sets the distance to try to keep from threats.
    - /sentinel range RANGE - Sets the NPC's maximum attack range.
    - /sentinel damage DAMAGE - Sets the NPC's attack damage. Set to -1 to automatically calculate from held weapon.
    - /sentinel weapondamage MATERIAL DAMAGE - Sets the NPC's attack damage for a specific weapon material.
    - /sentinel weaponredirect MATERIAL_ONE MATERIAL_TWO  - Sets the NPC to treat material one as though it's material two.
    - /sentinel armor ARMOR - Sets the NPC's armor level. Set to -1 to automatically calculate from equipment.
    - /sentinel health HEALTH - Sets the NPC's health level.
    - /sentinel attackrate RATE \['ranged'\] - Changes the rate at which the NPC attacks, in seconds. Either ranged or close modes.
    - /sentinel healrate RATE - Changes the rate at which the NPC heals, in seconds.
    - /sentinel respawntime TIME - Changes the time it takes for the NPC to respawn, in seconds. Set to 0 to disable automatic respawn, or -1 to make the NPC autodelete on death.
    - /sentinel chaserange RANGE - Changes the maximum distance an NPC will run before returning to base.
    - /sentinel drops - Changes the drops of the current NPC.
    - /sentinel dropchance ID CHANCE - Changes the chance of a drop. Use "/sentinel dropchance" to see the drops list with IDs, then do like "/sentinel dropchance 3 50" (that puts a 50% chance on item with ID 3).
    - /sentinel deathxp XP - Sets the amount of XP dropped when the NPC dies.
    - /sentinel targettime TIME - Sets the NPC's enemy target time limit in seconds.
    - /sentinel speed SPEED - Sets the NPC's movement speed modifier.
    - /sentinel guarddistance MINIMUM_DISTANCE \[SELECTION_RANGE\] - Sets the NPC's minimum guard distance (how far you must go before the NPC moves to keep up) and selection range (how close it will try to get to you).
    - /sentinel spawnpoint - Changes the NPC's spawn point to its current location, or removes it if it's already there.
    - /sentinel greeting GREETING - Sets a greeting message for the NPC to say.
    - /sentinel warning WARNING - Sets a warning message for the NPC to say.
    - /sentinel greetrange RANGE - Sets how far a player can be from an NPC before they are greeted.
    - /sentinel greetrate RATE - Sets how quickly (in seconds) the Sentinel may re-greet any player.
    - /sentinel accuracy OFFSET - Sets the accuracy of an NPC, as a decimal number value (0 means perfectly accurate, 5 means pretty inaccurate).
    - /sentinel squad SQUAD - Sets the NPC's squad name (null for none). NPCs with the same squad name share aggro (if a player angers one NPC, the rest get angry too).
    - /sentinel reach REACH - Sets the NPC's reach (how far it can punch).
    - /sentinel projectilerange RANGE - Sets the NPC's projectile range (how far it is willing to shoot projectiles).
    - /sentinel avoidreturnpoint - Changes the location the NPC runs to when avoid mode is activated, or removes it if the NPC is already there.
- **Toggleable NPC configuration commands:**
    - /sentinel invincible \['true'/'false'\] - Toggles whether the NPC is invincible.
    - /sentinel protected \['true'/'false'\] - Toggles whether the NPC is protected from damage by ignore targets.
    - /sentinel fightback \['true'/'false'\] - Toggles whether the NPC will fight back.
    - /sentinel runaway \['true'/'false'\] - Toggles whether the NPC will run away when attacked.
    - /sentinel needammo \['true'/'false'\] - Toggles whether the NPC will need ammo.
    - /sentinel safeshot \['true'/'false'\] - Toggles whether the NPC will avoid damaging non-targets.
    - /sentinel chaseclose \['true'/'false'\] - Toggles whether the NPC will chase while in 'close quarters' fights.
    - /sentinel chaseranged \['true'/'false'\] - Toggles whether the NPC will chase while in ranged fights.
    - /sentinel enemydrops \['true'/'false'\] - Toggles whether enemy mobs of this NPC drop items.
    - /sentinel autoswitch \['true'/'false'\] - Toggles whether the NPC automatically switches items.
    - /sentinel realistic \['true'/'false'\] - Toggles whether the NPC should use "realistic" targeting logic (don't notice things hiding behind the NPC).
    - /sentinel knockback \['true'/'false'\] - Toggles whether the NPC can receive knockback. If disabled, will try to force the NPC to remain in place after being hit.

### Permissions

- sentinel.basic for the /sentinel command
- sentinel.admin to edit other player's Sentinel NPCs.
- sentinel.greet for commands: greeting, warning, greetrange
- sentinel.info for commands: info, stats, targets
- Everything else is "sentinel.X" where "X" is the command name, EG "sentinel.damage".

### Targets

There's a huge list of valid target types that can be used in the target commands (`addtarget`, `removetarget`, `addignore`, `removeignore`, `addavoid`, `removeavoid`).
These are used, for example, like `/sentinel addtarget monsters` (to automatically attack the `MONSTERS` target type), or `/sentinel addavoid helditem:*_sword` (to run away from players holding swords using the `helditem:ITEM_MATCHER` target type).

These are all valid targets and ignores:

- Common/basic targets: NPCS, OWNER, PASSIVE_MOBS, MOBS, MONSTERS, PLAYERS
    - `owner` is the NPC's owner
    - `passive_mobs` are all mobs that generally don't attack players
    - `mobs` are both passive and monsters
    - `monsters` are all mobs likely to attack players
- Basic entities: PIGS, OCELOTS, COWS, RABBITS, SHEEP, CHICKENS, HORSES, MUSHROOM_COWS, IRON_GOLEMS, SQUIDS, VILLAGERS, WOLVES, SNOWMEN, WITCHES, GUARDIANS, SHULKERS, CREEPERS, SKELETONS, ZOMBIES, MAGMA_CUBES, SILVERFISH, BATS, BLAZES, GHASTS, GIANTS, SLIMES, SPIDERS, CAVE_SPIDERS, ENDERMEN, ENDERMITES, WITHERS, ENDERDRAGONS
- In 1.9 or higher: SHULKERS
- In 1.10 or higher: POLAR_BEARS
- In 1.11 or higher: VEXES, DONKEYS, LLAMAS, MULES, HUSKS, ELDER_GUARDIANS, EVOKERS, SKELETON_HORSES, STRAYS, ZOMBIE_VILLAGERS, ZOMBIE_HORSES, WITHER_SKELETONS, VINDICATORS
- In 1.12 or higher: PARROTS, ILLUSIONERS
- In 1.13 or higher: DOLPHINS, DROWNED, COD, SALMON, PUFFERFISH, TROPICAL_FISH, PHANTOMS, TURTLES
- 1.13 or higher special targets: FISH
- In 1.14 or higher: PILLAGERS, RAVAGERS, CATS, PANDAS, TRADER_LLAMAS, WANDERING_TRADERS, FOXES
- In 1.15 or higher: BEES
- In 1.15 or LOWER: ZOMBIE_PIGMEN
- In 1.16 or higher: HOGLINS, PIGLINS, STRIDERS, ZOGLINS, ZOMBIFIED_PIGLINS
- Also allowed: `player:NAME(REGEX)`, `npc:NAME(REGEX)`, `entityname:NAME(REGEX)`
    - These work like `player:bob` to target player named 'bob', or `npc:.*\sGuard` to target NPCs named "Space Guard" or "Town Guard" or anything else (uses [RegEx](https://www.rexegg.com/regex-quickstart.html)).
- Also: `group:GROUPNAME` to target a Vault-compatible permission group by name
    - Requires [Vault](https://www.spigotmc.org/resources/vault.34315/) and any vault-compatible permission plugin (such as [LuckPerms](https://www.spigotmc.org/resources/luckperms.28140/))
- Also: `helditem:ITEM_MATCHER`, `offhand:ITEM_MATCHER`, `equipped:ITEM_MATCHER`, `in_inventory:ITEM_MATCHER`
    - These all use "ITEM_MATCHER"s, which, at their simplest, are just a regex that matches the material name. So, `helditem:diamond_sword` targets enemies that are holding a diamond sword.
    - However, you can also do `lore:LORE(REGEX)` as a matcher for a line of lore, or `name:NAME(REGEX)` as a matcher for the item display name.
    - For example, `offhand:name:Stick\d+` would target players holding an item in their offhand named like "Stick123".
- Also, event:`pvp`/`pvnpc`/`pve`/`pvsentinel`/`eve` (pvp is Player-vs-Player, eve is Entity-vs-Entity, etc.)
- Also, event:`pv:ENTITY`/`ev:ENTITY` (`pv:ENTITY` is used like `event:pv:chicken` for players attacking chickens)
- Also, `event:guarded_fight` to attack whatever the guarded player attacks.
- Also, `event:message:SOMETEXT` will match chat messages that contain 'sometext'.
- Also, `status:STATUS_TYPE`. Current status types:
    - `status:angry` for mobs (wolves, endermen, spiders, etc.) that are currently angry. This is handy with a combo like `allinone:enderman|status:angry`
    - `status:passive` for non-angry
- Also, via internal sample integrations:
    - `sbteam:SCOREBOARD_TEAM_HERE` (for vanilla scoreboard teams) (WARNING: scoreboards in vanilla minecraft can be cheated by changing your username)
    - `sbscoreabove:OBJECTIVE:MIN_VALUE, sbscorebelow:OBJECTIVE:MAX_VALUE` (for vanilla scoreboard objective scores)
    - `healthabove:PERCENTAGE` and `healthbelow:PERCENTAGE` (for targeting based on current health, useful for example for a healer NPC)
    - `permission:PERM.KEY` (for permission based targets, requires a permissions plugin of course)
    - `squad:SENTINEL_SQUAD_NAME` (to target another Sentinel SQUAD)
    - `uuid:UUID` (to target one single specific entity)
    - `potion:POTION_EFFECT` (effect name must be on <https://hub.spigotmc.org/javadocs/spigot/org/bukkit/potion/PotionEffectType.html>)
    - `npc_owned_by:TARGETER` (to target NPCs that are owned by online players that match certain target data, like `npc_owned_by:player:Bob`)
- Also anything listed in the integrations section above!
- You can also add multi-targets - that is, `multi:TARGET_ONE,TARGET_TWO,...` to have multiple targets required together.
    - For example: `multi:PLAYER,PLAYER,CHICKEN` will make the NPC angry at 2 players and a chicken if they are all together.
    - As another example: `multi:npc:steve,player,healthabove:50` will make the player angry at the NPC named steve, any one player, and any entity with health above 50%, if all 3 are together.
    - Note that currently `multi` targets do not work with `ignores`.
- You can also add all-in-one list targets - that is, `allinone:REQUIRED_ONE|REQUIRED_TWO|...` to have multiple target inputs that all must match for a single entity.
    - For example, `allinone:player|healthabove:50` will target specifically a player with health above 50%.
    - As another example: `allinone:chicken|entityname:cluckers|healthbelow:75` will target specifically injured chickens who are named "cluckers".
    - Note that incompatible requirements don't work together (for example, `allinone:dolphin|parrot` doesn't work, because no single entity is both a dolphin *and* a parrot).
- Note that you can have `allinone` targets inside a `multi` target, but you cannot have `multi` targets inside an `allinone` target.
- Also, do not put `allinone` inside another `allinone`, and do not put a `multi` inside another `multi`.
- Note that to remove `allinone` and `multi` targets, you need to use the ID number (use `/sentinel targets` and related commands to find the ID), like `/sentinel removetarget allinone:0`.
- Note that `event` cannot currently be put inside `allinone` or `multi`.

### Supported Weapon Types

- Fists (empty hand)
- Swords/tools
- Bow
    - equip NPC with arrows of any given type in their `/npc inventory` to set fired arrow type!
- Trident (will be thrown)
- Blaze rod (shoots fire balls!)
- Potions (splash, lingering)
- Nether star (strikes lightning!)
- Spectral arrow (makes the target glow, without damaging it.)
    - (To make a target glow ++ damage it, equip a bow + arm it with spectral arrows!)
- Snowballs
- Eggs
- Ender Pearls (Causes the target to get flung into the air!)
- Wither skulls (Dangerous wither skull explosions!)
- White_Dye (Shoots llama spit!)
- Shulker_Shell (Fires a shulker bullet!)
- Books (Will do evoker fang spell attacks!)
- CrackShot guns!

### Integrating Your Plugin With Sentinel

If you're building a separate plugin you would like to integrate into Sentinel, you should:

- **Important note:** Sentinel is designed to be used from in-game, and the Java API provides ways to replicate in-game commands from code or expand upon them. If you have never tried using Sentinel from in-game, the API will not be helpful to you. Please do not guess at how Sentinel works, just try using it in-game so you can gain an actual understanding of what the different options are and how they work.
- Use Maven to link the project properly...
- Use the Citizens repository:
```xml
        <repository>
            <id>citizens-repo</id>
            <url>https://maven.citizensnpcs.co/repo</url>
        </repository>
```
- And add Sentinel as a `provided` dependency (be sure to change the version to match the current version available) (note that the `exclusions` block can help to prevent maven issues):
```xml
        <dependency>
            <groupId>org.mcmonkey</groupId>
            <artifactId>sentinel</artifactId>
            <version>2.7.1-SNAPSHOT</version>
            <type>jar</type>
            <scope>provided</scope>
            <exclusions>
                <exclusion>
                    <groupId>*</groupId>
                    <artifactId>*</artifactId>
                </exclusion>
            </exclusions>
        </dependency>
```
- You will also want to link Citizens in Maven if you haven't already - see https://wiki.citizensnpcs.co/API for relevant information on that.

----

- Add a `depend` or `softdepend` (as relevant) on `Sentinel` to your `plugin.yml` file. See sample of how Sentinel does this to depend on other plugins: [Sample Source Here](https://github.com/mcmonkeyprojects/Sentinel/blob/master/src/main/resources/plugin.yml)
- When possible, take advantage of the `SentinelIntegration` class: [JavaDoc Here](https://ci.citizensnpcs.co/job/Sentinel/javadoc/org/mcmonkey/sentinel/SentinelIntegration.html)
    - Extend the class (with your own custom class) and implement whichever methods you need. See samples of integrations available: [Sample Source Here](https://github.com/mcmonkeyprojects/Sentinel/tree/master/src/main/java/org/mcmonkey/sentinel/integration)
    - Within your plugin's `onEnable`, Register the class by calling `SentinelPlugin.instance.registerIntegration(new YourIntegration());` where `YourIntegration` is the integration class you created.
- You might also benefit from events like the `SentinelAttackEvent`: [JavaDoc Here](https://ci.citizensnpcs.co/job/Sentinel/javadoc/org/mcmonkey/sentinel/events/package-summary.html)
- Samples of a few common basic operations:
    - Check if NPC is a Sentinel: `if (npc.hasTrait(SentinelTrait.class)) { ...`
    - Get the Sentinel trait from an NPC: `SentinelTrait sentinel = npc.getOrAddTrait(SentinelTrait.class);`
    - Add a target to a Sentinel NPC by basic type: `sentinel.addTarget("monsters");`
    - Add a very specific target to a Sentinel NPC: `sentinel.addTarget("uuid:" + player.getUniqueId());`
    - Set the Sentinel NPC's max health: `sentinel.setHealth(500);`
    - Set the Sentinel NPC's damage output: `sentinel.damage = 10;`
- If you're lost, feel free to ask for help using the help channels listed below.

### Need help using Sentinel? Try one of these places:

- **Discord** (Modern): https://discord.gg/Q6pZGSR in the `#sentinel` channel.
- **Spigot Info Page** (Modern): https://www.spigotmc.org/resources/sentinel.22017/
- The GitHub issues page

### Dependencies

- **Spigot (Plugin-ready server mod)**: https://www.spigotmc.org/ or **Paper (high performance fork of Spigot)**: https://papermc.io/
- **Citizens2 (NPC engine)**: [GitHub](https://github.com/CitizensDev/Citizens2/) / [Spigot Resource](https://www.spigotmc.org/resources/citizens.13811/) / [Dev Builds](https://ci.citizensnpcs.co/job/Citizens2/)

#### Also check out:

- **Denizen (Powerful script engine)**: https://github.com/DenizenScript/Denizen-For-Bukkit

### Sentry user?

- Type "/sentinel sentryimport" on a server running both Sentry and Sentinel to instantly transfer all data to Sentinel!
- Sentinel 1.7.2 is the last version that contained the **Sentry importer**. If you need to import old Sentry data, you must use a 1.7.2 build.
    - You can get that build here: https://ci.citizensnpcs.co/job/Sentinel/201/

### Update Warnings

- Sentinel 1.7 **changes the API** structure of Sentinel - and adds documentation and **JavaDocs**, which can be found here: https://ci.citizensnpcs.co/job/Sentinel/javadoc/overview-summary.html
    - Save data structure is also changed in the 1.7 updates.
    - If you need a pre-API rewrite version of Sentinel, you can get the last version of Sentinel (1.6.2) here: https://ci.citizensnpcs.co/job/Sentinel/191/

### Licensing pre-note:

This is an open source project, provided entirely freely, for everyone to use and contribute to.

If you make any changes that could benefit the community as a whole, please contribute upstream.

### The short of the license is:

You can do basically whatever you want, except you may not hold any developer liable for what you do with the software.

### Previous License

Copyright (C) 2016-2019 Alex "mcmonkey" Goodwin, All Rights Reserved.

### The long version of the license follows:

The MIT License (MIT)

Copyright (c) 2019-2022 Alex "mcmonkey" Goodwin

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
