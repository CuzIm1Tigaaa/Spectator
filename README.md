# Spectate
Spectate other Players. Note: This is a modified version of kosakriszi's Spectator Plugin.

-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-

Commands:

/spectate [Player] - Toggle Spectator Mode or Spectate a specific Player.

/spectatehere - Unspectate at your current Position.

/spectatecycle <Start|Stop|Pause|Resume> [Interval] - SpectateCycle Mode. Interval is for the Start subcommand and must be specified in seconds

/spectatereload - Reload the Config..

Aliases:
/spec, /spechere, /spech, /speccycle, /specreload, /specrl

-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-

Permissions:

spectate.*: Gives access to all Commands and Features

spectate.use.*: Gives access to all Use-Commands

spectate.utils.*: Gives access to all utils

spectate.bypass.*: Gives access to all Bypass-Features

spectate.reload: Gives access to use the reload Command

-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-

spectate.use.* contains following subpermissions:

spectate.use: Gives access to use the /spec Command --- spectate.use.here: Gives access to use the /spechere Command
spectate.use.others: Gives access to spectate other Players --- spectate.use.cycle: Gives access to use the /speccycle Command.

-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-

spectate.utils.* contains following subpermissions:

spectate.inventory: Gives access to mirror the Inventory of a Player you are spectating --- spectate.tab: Gives access to Hide in Tablist

-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-

spectate.bypass.* contains following subpermissions:

spectate.tablist: This Permission let you show hidden Players --- spectate.cannot: With this Permission you cannot be spectated

-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-

Config:

hideTab: If true & Player has Permission [spectate.tab], Player is hidden in Tablist.

mirrorInventory: If true & Player has Permission [spectate.inventory], Player can see the Inventory of spectating Player.

saveLocation: If true, Player gets teleported back to his old Location when leaving Spectator-Mode.

kickOnCycle: If true, Player can get kickend while cycling.
