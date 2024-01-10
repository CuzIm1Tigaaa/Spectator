package de.cuzim1tigaaa.spectator.files;

import de.cuzim1tigaaa.spectator.Spectator;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;

public final class Messages {

    private static FileConfiguration message;

    public static String getMessage(CommandSender sender, String path, Object... replace) {
        String msg = message.getString(path);
        if(msg == null) msg = ChatColor.RED + "Error: Path " + ChatColor.GRAY + "'" + path + "' " + ChatColor.RED + "does not exist!";
        for(int i = 0; i < replace.length; i++) {
            String target = replace[i] == null ? null : (String) replace[i];
            if(target == null)
                continue;
            i++;
            String replacement = replace[i] == null ? null : replace[i].toString();
            if(message != null) msg = replacement == null ? msg : msg.replace("%" + target + "%", replacement);
        }
        if(Spectator.isPapiInstalled() && sender instanceof Player player)
            return ChatColor.translateAlternateColorCodes('&', PlaceholderAPI.setPlaceholders(player, msg));
        return ChatColor.translateAlternateColorCodes('&', msg) ;
    }

    public static void loadLanguageFile(Spectator plugin) {
        createLangFiles(plugin);
        File messageFile = new File(plugin.getDataFolder() + "/lang", Config.getString(Paths.CONFIG_LANGUAGE) + ".yml");
        try {
            if(!messageFile.exists()) {
                loadDefaultMessages(plugin);
                return;
            }
            message = YamlConfiguration.loadConfiguration(messageFile);
            message.save(messageFile);
        }catch(IOException exception) {
            plugin.getLogger().log(Level.SEVERE, "An error occurred while loading language files", exception);
        }
    }

    private static void createLangFiles(Spectator plugin) {
        URL dirURL = Spectator.class.getClassLoader().getResource("lang");
        if(dirURL == null) return;

        String jarPath = dirURL.getPath().substring(5, dirURL.getPath().indexOf("!"));

        try(JarFile jarFile = new JarFile(URLDecoder.decode(jarPath, StandardCharsets.UTF_8))) {
	        Enumeration<JarEntry> entries = jarFile.entries();

            while(entries.hasMoreElements()) {
                String name = entries.nextElement().getName();
                if(name.startsWith("lang"))
                    if(!name.endsWith(File.separator) && name.endsWith(".yml")) {
                        File file = new File(plugin.getDataFolder(), name);
                        if(!file.exists()) plugin.saveResource(name, false);
                    }
            }
        }catch(IOException exception) {
            plugin.getLogger().log(Level.SEVERE, "An error occurred while creating language files", exception);
        }


    }

    private static void loadDefaultMessages(Spectator plugin) {
        File messageFile = new File(plugin.getDataFolder() + "/lang", "en_US.yml");
        try {
            if(!messageFile.exists()) {
                message = new YamlConfiguration();
                message.save(messageFile);
            }
            message = YamlConfiguration.loadConfiguration(messageFile);
            set(Paths.MESSAGE_DEFAULT_SENDER,                       "&cYou have to be a Player to perform this command!");
            set(Paths.MESSAGE_DEFAULT_PERMISSION,                   "&cYou do not have Permission to execute this command!");
            set(Paths.MESSAGE_DEFAULT_SYNTAX,                       "&cThere's a syntax error: %USAGE%");
            set(Paths.MESSAGE_DEFAULT_RELOAD,                       "&7The plugin was successfully reloaded.");

            set(Paths.MESSAGES_GENERAL_BYPASS_TELEPORT,             "&7%TARGET% &ccannot be spectated at the moment!");
            set(Paths.MESSAGES_GENERAL_BYPASS_INVENTORY,            "&cYou cannot see the inventory of &7%TARGET% &cat the moment!");
            set(Paths.MESSAGES_GENERAL_DISMOUNT,                    "&cYou cannot dismount while in Speccycle-Mode! Use &7/spectatecycle stop &cto leave Speccycle-Mode.");
            set(Paths.MESSAGES_GENERAL_GAMEMODE_CHANGE,             "&cYou cannot change your GameMode while spectating! Use &7/spec &cto leave Spectator-Mode.");
            set(Paths.MESSAGES_GENERAL_NOPLAYERS,                   "&cThere are not enough Players online.");
            set(Paths.MESSAGES_GENERAL_OFFLINEPLAYER,               "&7%TARGET% &cis not Online!");
            set(Paths.MESSAGES_GENERAL_NOTSPECTATING,               "&7%TARGET% &cis currently not spectating!");
            set(Paths.MESSAGES_GENERAL_SAMEPLAYER,                  "&cYou are already spectating &7%TARGET%&c!");
            set(Paths.MESSAGES_GENERAL_YOURSELF,                    "&cYou cannot Spectate yourself!");
            set(Paths.MESSAGES_GENERAL_CYCLEONLY,                   "&cYou can only use &7/SpectateCycle start <Interval>&c!");
            set(Paths.MESSAGES_GENERAL_NUMBERFORMAT,                "&cPlease enter a valid number!");

            set(Paths.MESSAGES_CYCLING_SEARCHING_TARGET,            "&cSearching next Target…");
            set(Paths.MESSAGES_CYCLING_CURRENT_TARGET,              "&7Currently spectating &6%TARGET%");

            set(Paths.MESSAGES_COMMANDS_LIST_NONE,                  "&cThere are no spectators at the moment!");
            set(Paths.MESSAGES_COMMANDS_LIST_TITLE,                 "&7There are currently &e%AMOUNT% &7Spectators:");
            set(Paths.MESSAGES_COMMANDS_LIST_CYCLING,               "&7- &b%SPECTATOR% &8[&e%TARGET%&8]");
            set(Paths.MESSAGES_COMMANDS_LIST_PAUSED,                "&7- %SPECTATOR% &8[&cPAUSED&8]");
            set(Paths.MESSAGES_COMMANDS_LIST_DEFAULT,               "&7- %SPECTATOR%");
            set(Paths.MESSAGES_COMMANDS_LIST_SPECTATING,            "&7- %SPECTATOR% » &e%TARGET%");

            set(Paths.MESSAGES_COMMANDS_UNSPECTATE_ALL,             "&7All Spectators have been resend!");
            set(Paths.MESSAGES_COMMANDS_UNSPECTATE_PLAYER,          "&e%TARGET% &7has been resend!");

            set(Paths.MESSAGES_COMMANDS_CYCLE_START,                "&7Speccycle &astarted &7with interval &c%INTERVAL% &7in %ORDER% order.");
            set(Paths.MESSAGES_COMMANDS_CYCLE_STOP,                 "&7Speccycle &cstopped&7.");
            set(Paths.MESSAGES_COMMANDS_CYCLE_PAUSE,                "&7Speccycle &epaused&7.");
            set(Paths.MESSAGES_COMMANDS_CYCLE_RESTART,              "&7Speccycle &brestarted &7with interval &c%INTERVAL%&7.");
            set(Paths.MESSAGES_COMMANDS_CYCLE_CYCLING,              "&cYou are already in Speccycle-Mode!");
            set(Paths.MESSAGES_COMMANDS_CYCLE_NOT_CYCLING,          "&cYou are not in Speccycle-Mode!");
            set(Paths.MESSAGES_COMMANDS_CYCLE_TARGET_NOT_CYCLING,   "&e%TARGET% &cis not in Speccycle-Mode!");

            set(Paths.MESSAGES_COMMANDS_SPECTATE_JOIN_OWN,          "&7You are now in Spectator-Mode.");
            set(Paths.MESSAGES_COMMANDS_SPECTATE_JOIN_OTHER,        "&e%TARGET% &7is now in Spectator-Mode.");
            set(Paths.MESSAGES_COMMANDS_SPECTATE_LEAVE_OWN,         "&7You are no longer in Spectator-Mode.");
            set(Paths.MESSAGES_COMMANDS_SPECTATE_LEAVE_OTHER,       "&e%TARGET% &7is no longer in Spectator-Mode.");
            set(Paths.MESSAGES_COMMANDS_SPECTATE_PLAYER,            "&7You are now spectating &c%TARGET%&7.");
            set(Paths.MESSAGES_COMMANDS_SPECTATE_MULTIVERSE,        "&cYou do not have permission to spectate &e%TARGET% &cin this world!");
            message.save(messageFile);
        }catch (IOException exception) {
            plugin.getLogger().log(Level.SEVERE, "An error occurred while loading default messages", exception);
        }
    }

    private static void set(String path, String value) {
        message.set(path, message.get(path, value));
    }
}