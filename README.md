Sentinel NPCs: Combat NPCs for Spigot!
--------------------------------------

**Version 0.1**: Compatible with Spigot 1.9

**Download (Developmental builds)**: http://ci.citizensnpcs.co/job/Sentinel/  
**Download (Spigot releases)**: (COMING SOON)  

### Info

Created by mcmonkey4eva on behalf of the Citizens and Denizen teams.

### Usage

- First, get acquainted with Citizens in general for best luck using.
- Second, download the plugin and put it into your server's `plugins` folder.
- Third, start the server to generate a config, then close the server and edit the config to your liking and finally restart the server.
- Now, to create your first Sentinel:
	- Select or create an NPC (`/npc sel` or `/npc create Bob`)
	- Run command: /trait Sentinel
	- Run command: /npc equip
	- Give the NPC items as needed, by right click the NPC with the wanted item.
	- Run command: /sentinel addtarget MONSTERS
	- Spawn a zombie via creative inventory eggs and watch it die!
	- Run command: /sentinel help
		- This will list all your options to edit the NPC's Sentinel settings.
			- Play with them freely, just be careful if you have other players around!

### Supported things

- View `/sentinel addtarget` to view a list of currently supported targets.
- Weapons:
	- Fists
	- Bow
	- Blaze rod (shoots fire balls!)

### TODO

- Weapons:
	- splash potions
		- Just... throw the potions at things
- Ammo:
	- Basically, optionally require ammo be fed into the NPC for bows/fireballs, or spare swords if the current sword breaks.
- Damage calculation:
	- Caculate damage from the item rather than enforcing a specific value
- Armor calculation:
	- Calculate armor protection from equipment rather than enforcing a specific value.
- Mounting:
	- Ride a horse or whatever other NPC
	- maybe support this in base Citizens rather than Sentinel?

### Need help using Sentinel? Try one of these places:

**IRC** (Modern): http://webchat.esper.net/?channels=citizens  
(irc.esper.net in the channel #citizens)  
**Spigot Info Page** (Modern): (COMING SOON)  

### Dependencies

**Spigot (Plugin-ready server mod)**: https://www.spigotmc.org/  
**Citizens2 (NPC engine)**: https://github.com/CitizensDev/Citizens2/  

#### Also check out:

**Denizen (Powerful script engine)**: https://github.com/DenizenScript/Denizen-For-Bukkit  
