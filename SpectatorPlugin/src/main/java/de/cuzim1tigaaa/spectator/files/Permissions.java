package de.cuzim1tigaaa.spectator.files;

import org.bukkit.command.CommandSender;

public final class Permissions {

    public static final String COMMANDS_SPECTATE_RELOAD =       "spectator.commands.spectatereload";
    public static final String COMMAND_UNSPECTATE =             "spectator.commands.unspectate";
    public static final String COMMAND_SPECTATE_GENERAL =       "spectator.commands.spectate";
    public static final String COMMAND_SPECTATE_CHANGE_OTHERS = "spectator.commands.spectatechangeothers";
    public static final String COMMAND_SPECTATE_OTHERS =        "spectator.commands.spectateothers";
    public static final String COMMAND_SPECTATE_HERE =          "spectator.commands.spectatehere";
    public static final String COMMANDS_SPECTATE_LIST =         "spectator.commands.spectatelist";
    public static final String COMMANDS_SPECTATE_CYCLE =        "spectator.commands.spectatecycle.default";
    public static final String COMMANDS_CYCLE_STOP_OTHERS =     "spectator.commands.spectatecycle.stopOthers";

    public static final String BYPASS_TABLIST =                 "spectator.bypass.tablist";
    public static final String BYPASS_SPECTATED =               "spectator.bypass.spectated";
    public static final String BYPASS_UNSPECTATED =             "spectator.bypass.unspectated";
    public static final String BYPASS_SPECTATEALL =             "spectator.bypass.spectateall";
    public static final String BYPASS_NOTIFY =                  "spectator.bypass.notify";

    public static final String UTILS_OPEN_CONTAINER =           "spectator.utils.opencontainers";
    public static final String UTILS_OPEN_ENDERCHEST =          "spectator.utils.openenderchest";
    public static final String UTILS_MIRROR_INVENTORY =         "spectator.utils.mirrorinventory";
    public static final String UTILS_MIRROR_EFFECTS =           "spectator.utils.mirroreffects";
    public static final String UTILS_HIDE_IN_TAB =              "spectator.utils.hidetab";
    public static final String UTILS_HIDE_ARMORSTAND =          "spectator.utils.hidearmorstand";

    public static final String NOTIFY_UPDATE_ON_JOIN =          "spectator.notify.update";

    public static boolean hasPermission(CommandSender sender, String permission) {
        return sender.hasPermission(permission);
    }

    public static boolean hasPermissions(CommandSender sender, String... permissions) {
        for(String permission : permissions) {
            if(!sender.hasPermission(permission))
                return false;
        }
        return true;
    }

}