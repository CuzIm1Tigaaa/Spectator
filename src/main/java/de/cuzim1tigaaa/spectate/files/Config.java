package de.cuzim1tigaaa.spectate.files;

import de.cuzim1tigaaa.spectate.Main;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class Config {

    private static FileConfiguration message;
    public static boolean hideTab, mirrorInventory, saveLocation, saveFlying, kickOnCycle;

    public static String getMessage(String path, Object... replace) {
        String msg = message.getString(path);
        for(int i = 0; i < replace.length; i++) {
            String target = (String) replace[i];
            String replacement = replace[i += 1].toString();
            assert msg != null;
            msg = msg.replace("%" + target + "%", replacement);
            if(target == null) msg = null;
            assert msg != null;
        }
        if(msg == null) {
            return "§cError: Message §7" + path + " §cdoes not exist!";
        }
        return ChatColor.translateAlternateColorCodes('&', msg);
    }

    public static void loadConfig() {
        File messageFile = new File(Main.getInstance().getDataFolder(), "config.yml");
        try {
            if(!messageFile.exists()) {
                message = new YamlConfiguration();
                message.save(messageFile);
            }
            message = YamlConfiguration.loadConfiguration(messageFile);
            message.set(Paths.CONFIG_HIDE_TAB, hideTab = message.getBoolean(Paths.CONFIG_HIDE_TAB, true));
            message.set(Paths.CONFIG_MIRROR_INVENTORY, mirrorInventory = message.getBoolean(Paths.CONFIG_MIRROR_INVENTORY, true));
            message.set(Paths.CONFIG_SAVE_LOCATION, saveLocation = message.getBoolean(Paths.CONFIG_SAVE_LOCATION, true));
            message.set(Paths.CONFIG_SAVE_FLIGHT_MODE, saveFlying = message.getBoolean(Paths.CONFIG_SAVE_FLIGHT_MODE, false));
            message.set(Paths.CONFIG_KICK_CYCLING_PLAYER, kickOnCycle = message.getBoolean(Paths.CONFIG_KICK_CYCLING_PLAYER, false));

            message.set(Paths.MESSAGE_DEFAULT_SENDER, message.getString(Paths.MESSAGE_DEFAULT_SENDER, "&cYou have to be a Player to perform this command!"));
            message.set(Paths.MESSAGE_DEFAULT_PERMISSION, message.getString(Paths.MESSAGE_DEFAULT_PERMISSION, "&cYou do not have Permission to execute this command!"));
            message.set(Paths.MESSAGE_DEFAULT_SYNTAX, message.getString(Paths.MESSAGE_DEFAULT_SYNTAX, "&cThere's a syntax error: %USAGE%"));
            message.set(Paths.MESSAGE_DEFAULT_RELOAD, message.getString(Paths.MESSAGE_DEFAULT_RELOAD, "&7The plugin was successfully reloaded."));

            message.set(Paths.MESSAGES_GENERAL_BYPASS, message.getString(Paths.MESSAGES_GENERAL_BYPASS, "&7%TARGET% &ccannot be spectated at the moment!"));
            message.set(Paths.MESSAGES_GENERAL_DISMOUNT, message.getString(Paths.MESSAGES_GENERAL_DISMOUNT, "&cYou cannot dismount while in Speccycle-Mode! Use &7/SpectateCycle stop &cto leave Speccycle-Mode."));
            message.set(Paths.MESSAGES_GENERAL_GAMEMODE_CHANGE, message.getString(Paths.MESSAGES_GENERAL_GAMEMODE_CHANGE, "&cYou cannot change your GameMode while spectating! Use &7/spec &cto leave Spectator-Mode."));
            message.set(Paths.MESSAGES_GENERAL_NOPLAYERS, message.getString(Paths.MESSAGES_GENERAL_NOPLAYERS, "&cThere are not enough Players online."));
            message.set(Paths.MESSAGES_GENERAL_OFFLINEPLAYER, message.getString(Paths.MESSAGES_GENERAL_OFFLINEPLAYER, "&7%TARGET% &cis not Online!"));
            message.set(Paths.MESSAGES_GENERAL_NOTSPECTATING, message.getString(Paths.MESSAGES_GENERAL_NOTSPECTATING, "&7%TARGET% &cis currently not spectating!"));
            message.set(Paths.MESSAGES_GENERAL_SAMEPLAYER, message.getString(Paths.MESSAGES_GENERAL_SAMEPLAYER, "&cYou are already spectating &7%TARGET&c!"));
            message.set(Paths.MESSAGES_GENERAL_YOURSELF, message.getString(Paths.MESSAGES_GENERAL_YOURSELF, "&cYou cannot Spectate yourself!"));
            message.set(Paths.MESSAGES_GENERAL_CYCLEONLY, message.getString(Paths.MESSAGES_GENERAL_CYCLEONLY, "&cYou can only use &7/SpectateCycle start <Interval>"));
            message.set(Paths.MESSAGES_GENERAL_NUMBERFORMAT, message.getString(Paths.MESSAGES_GENERAL_NUMBERFORMAT, "&cPlease enter a valid number!"));

            message.set(Paths.MESSAGES_COMMANDS_LIST_NONE, message.getString(Paths.MESSAGES_COMMANDS_LIST_NONE, "&cThere are no spectators at the moment!"));
            message.set(Paths.MESSAGES_COMMANDS_LIST_TITLE, message.getString(Paths.MESSAGES_COMMANDS_LIST_TITLE, "&7There are currently &e%AMOUNT% &7Spectators:"));
            message.set(Paths.MESSAGES_COMMANDS_LIST_CYCLING, message.getString(Paths.MESSAGES_COMMANDS_LIST_CYCLING, "&7- %SPECTATOR &8[&bCYCLING&8]"));
            message.set(Paths.MESSAGES_COMMANDS_LIST_DEFAULT, message.getString(Paths.MESSAGES_COMMANDS_LIST_DEFAULT, "&7- %SPECTATOR%"));
            message.set(Paths.MESSAGES_COMMANDS_LIST_SPECTATING, message.getString(Paths.MESSAGES_COMMANDS_LIST_SPECTATING, "&7- %SPECTATOR% &8[&e%TARGET%&8]"));

            message.set(Paths.MESSAGES_COMMANDS_UNSPECTATE_ALL, message.getString(Paths.MESSAGES_COMMANDS_UNSPECTATE_ALL, "&7All Spectators have been resend!"));
            message.set(Paths.MESSAGES_COMMANDS_UNSPECTATE_PLAYER, message.getString(Paths.MESSAGES_COMMANDS_UNSPECTATE_PLAYER, "&e%TARGET% &7has been resend!"));

            message.set(Paths.MESSAGES_COMMANDS_CYCLE_START, message.getString(Paths.MESSAGES_COMMANDS_CYCLE_START, "&7Speccycle started with interval &c%INTERVAL%&7."));
            message.set(Paths.MESSAGES_COMMANDS_CYCLE_STOP, message.getString(Paths.MESSAGES_COMMANDS_CYCLE_STOP, "&7Speccycle stopped."));
            message.set(Paths.MESSAGES_COMMANDS_CYCLE_CYCLING, message.getString(Paths.MESSAGES_COMMANDS_CYCLE_CYCLING, "&cYou are already in Speccycle-Mode!"));
            message.set(Paths.MESSAGES_COMMANDS_CYCLE_NOTCYCLING, message.getString(Paths.MESSAGES_COMMANDS_CYCLE_NOTCYCLING, "&cYou are not in Speccycle-Mode!"));

            message.set(Paths.MESSAGES_COMMANDS_SPECTATE_JOIN_OWN, message.getString(Paths.MESSAGES_COMMANDS_SPECTATE_JOIN_OWN, "&7You are now in Spectator-Mode."));
            message.set(Paths.MESSAGES_COMMANDS_SPECTATE_LEAVE_OTHER, message.getString(Paths.MESSAGES_COMMANDS_SPECTATE_JOIN_OTHER, "&e%TARGET &7is now in Spectator-Mode."));
            message.set(Paths.MESSAGES_COMMANDS_SPECTATE_LEAVE_OWN, message.getString(Paths.MESSAGES_COMMANDS_SPECTATE_LEAVE_OWN, "&7You are no longer in Spectator-Mode."));
            message.set(Paths.MESSAGES_COMMANDS_SPECTATE_JOIN_OTHER, message.getString(Paths.MESSAGES_COMMANDS_SPECTATE_LEAVE_OTHER, "&e%TARGET% &7is no longer in Spectator-Mode."));
            message.set(Paths.MESSAGES_COMMANDS_SPECTATE_PLAYER, message.getString(Paths.MESSAGES_COMMANDS_SPECTATE_PLAYER, "&7You are now spectating &c%TARGET%&7."));
            message.save(messageFile);
        }catch (IOException exception) { exception.printStackTrace(); }
    }
}