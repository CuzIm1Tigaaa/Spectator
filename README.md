# Spectate
Spectate other Players. 

Note: This is a modified version of kosakriszi's Spectator Plugin. https://www.spigotmc.org/resources/spectator.16745/

-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-

# Commands
- /spectate - Enter or leave Spectator-Mode
- /spectatehere - Enter Spectator-Mode or leave at current position (Info: Teleports you to the highest block at Position.
- /spectate [Player] - Spectate a specific Player
- /spectatecycle start <Seconds> - Start a speccycle with given Interval
- /spectatecycle stop - Stop a running speccycle
- /spectatecycle pause - Pause a running speccycle
- /spectatecycle resume - Resume a paused speccycle
- /spectatereload - Reload the Config
- /spectatelist - List all active spectators
- /unspectate [Player] - Unspectate all spectators or specific player

# Permissions
- spectate.*: Grants a Player all Spectate Permissions
	- spectate.use.*: Grants a Player following Permissions
		- spectate.use: Use the /spectate Command
		- spectate.use.here: Use the /spectatehere Command
		- spectate.use.others: Spectate other Players
		- spectate.use.cycle: Us the /spectatecycle Command
		- spectate.list: List all active spectators
	- spectate.utils.*: Grants a Player following Permissions
		- spectate.inventory: Let a Spectator see the target Inventory
		- spectate.tab: Let a Spectator be hidden in Tablist
	- spectate.bypass.*: Grannts a Player following Permission
		- spectate.tablist: Player with this Permission can see hidden Players in Tab
		- spectate.cannot: Player with this Permission cannot be spectated
	- spectate.admin:
		- spectate.reload: Reload the Configuration
		- spectate.unspectate: Unspectate All spectators or specific
- spectate.useonly.cycle: Grants a Player permissions for speccycle, Player cannot leave the view

# Config
In the Config you can edit the Messages of the Plugin and 5 other settings:

- hideTab, default: true
	- Controll the visibility of a Player in the Tablist
- mirrrorInventory, default: true
	- Controll if a Player can see Target Inventory
- saveLocation, default: true
	- Controll if Player will be teleported to his old Location or his current
	- Note: If saveLocation = false, /spec & /spechere are same
- saveFlying, default: false
	- If true, this will save if a Player was flying or not
- kickOnCycle, default: false
	- Controll if Player can get kicked when Cycling
