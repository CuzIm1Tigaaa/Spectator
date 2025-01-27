package de.cuzim1tigaaa.spectator.files;

import de.cuzim1tigaaa.spectator.Spectator;
import me.clip.placeholderapi.PlaceholderAPI;
import net.md_5.bungee.api.ChatColor;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Messages {

    private static Messages INSTANCE;
    private final Spectator plugin;
    private static final Pattern hexPattern = Pattern.compile("#[a-fA-F0-9]{6}");

    private static boolean PAPI_INSTALLED;
    private static FileConfiguration MESSAGE_FILE_CONFIG;

    public Messages(Spectator plugin) {
        this.plugin = plugin;
        PAPI_INSTALLED = plugin.isPapiInstalled();
    }

    public static Messages getMessages() {
        if(INSTANCE == null)
            INSTANCE = new Messages(Spectator.getPlugin());
        return INSTANCE;
    }

    public static void sendMessage(CommandSender sender, String path, Object... replace) {
        String message;
        if(PAPI_INSTALLED && sender instanceof Player player) {
            message = getMessage(player, path, replace);
            if(message == null) return;
            player.sendMessage(message);
        }

        message = getMessage(sender, path, replace);
        if(message == null) return;
        sender.sendMessage(message);
    }

    public static String getMessage(CommandSender sender, String path, Object... replace) {
        String msg = MESSAGE_FILE_CONFIG.getString(path);
        if(msg == null) msg = ChatColor.RED + "Error: Path " + ChatColor.GRAY + "'" + path + "' " + ChatColor.RED + "does not exist!";
        if(msg.isEmpty())
            return null;

        for(int i = 0; i < replace.length; i++) {
            String target = replace[i] == null ? null : (String) replace[i];
            if(target == null)
                continue;
            i++;
            String replacement = replace[i] == null ? null : replace[i].toString();
            if(MESSAGE_FILE_CONFIG != null) msg = replacement == null ? msg : msg.replace("%" + target + "%", replacement);
        }

        String message = msg;
        if(PAPI_INSTALLED && sender instanceof Player player)
            message = PlaceholderAPI.setPlaceholders(player, msg);

        Matcher matcher = hexPattern.matcher(message);
        while(matcher.find()) {
            String hex = message.substring(matcher.start(), matcher.end());
            message = message.replace(hex, ChatColor.of(hex) + "");
            matcher = hexPattern.matcher(message);
        }

        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public void loadLanguageFile() {
        createLangFiles(plugin);
        File messageFile = new File(plugin.getDataFolder() + "/lang", plugin.getConfig().getString(Paths.CONFIG_LANGUAGE) + ".yml");
        try {
            Spectator.debug("Loading language file: " + messageFile.getName());
            if(!messageFile.exists()) {
                Spectator.debug("Language file does not exist. Loading default messages.");
                loadDefaultMessages(plugin);
                return;
            }
            MESSAGE_FILE_CONFIG = YamlConfiguration.loadConfiguration(messageFile);
            MESSAGE_FILE_CONFIG.save(messageFile);
        }catch(IOException exception) {
            plugin.getLogger().log(Level.SEVERE, "An error occurred while loading language files", exception);
        }
    }

    private void createLangFiles(Spectator plugin) {
        URL dirURL = Spectator.class.getClassLoader().getResource("lang");
        if(dirURL == null) return;

        String jarPath = dirURL.getPath();
        if(dirURL.getPath().contains("!"))
            jarPath = dirURL.getPath().substring(5, dirURL.getPath().indexOf("!"));

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

    private void loadDefaultMessages(Spectator plugin) {
        File messageFile = new File(plugin.getDataFolder() + "/lang", "en_US.yml");
        try {
            if(!messageFile.exists()) {
                MESSAGE_FILE_CONFIG = new YamlConfiguration();
                MESSAGE_FILE_CONFIG.save(messageFile);
            }
            MESSAGE_FILE_CONFIG = YamlConfiguration.loadConfiguration(messageFile);
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
            set(Paths.MESSAGES_GENERAL_NOTSPECTATOR,                "&cYou re currently not spectating!");
            set(Paths.MESSAGES_GENERAL_SAMEPLAYER,                  "&cYou are already spectating &7%TARGET%&c!");
            set(Paths.MESSAGES_GENERAL_YOURSELF,                    "&cYou cannot Spectate yourself!");
            set(Paths.MESSAGES_GENERAL_CYCLEONLY,                   "&cYou can only use &7/SpectateCycle start <Interval>&c!");
            set(Paths.MESSAGES_GENERAL_NUMBERFORMAT,                "&cPlease enter a valid number!");
            set(Paths.MESSAGES_GENERAL_NOTIFY_SPECTATE,             "&7Du wirst nun von &e%TARGET%&7 beobachtet.");
            set(Paths.MESSAGES_GENERAL_NOTIFY_UNSPECTATE,           "&7Du wirst nicht mehr von &e%TARGET%&7 beobachtet.");

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
            set(Paths.MESSAGES_COMMANDS_CYCLE_START_OTHER,          "&7Speccycle &astarted &7with interval &c%INTERVAL% &7in %ORDER% order for player &e%TARGET%&7.");
            set(Paths.MESSAGES_COMMANDS_CYCLE_STOP,                 "&7Speccycle &cstopped&7.");
            set(Paths.MESSAGES_COMMANDS_CYCLE_STOP_OTHER,           "&7Speccycle &cstopped &7for player &e%TARGET%&7.");
            set(Paths.MESSAGES_COMMANDS_CYCLE_NEXT,                 "&7Forcing Speccycle to visit &e%TARGET%&7.");
            set(Paths.MESSAGES_COMMANDS_CYCLE_PAUSE,                "&7Speccycle &epaused&7.");
            set(Paths.MESSAGES_COMMANDS_CYCLE_RESTART,              "&7Speccycle &brestarted &7with interval &c%INTERVAL%&7.");
            set(Paths.MESSAGES_COMMANDS_CYCLE_CYCLING,              "&cYou are already in Speccycle-Mode!");
            set(Paths.MESSAGES_COMMANDS_CYCLE_NOT_CYCLING,          "&cYou are not in Speccycle-Mode!");
            set(Paths.MESSAGES_COMMANDS_CYCLE_TARGET_NOT_CYCLING,   "&e%TARGET% &cis not in Speccycle-Mode!");
            set(Paths.MESSAGES_COMMANDS_CYCLE_INTERVAL_TOO_SMALL,   "&cThe interval must at least be &e%MINIMUM% &cseconds!");
            set(Paths.MESSAGES_COMMANDS_CYCLE_INTERVAL_TOO_BIG,     "&cThe interval cannot be bigger than &e%MAXIMUM% &cseconds!");

            set(Paths.MESSAGES_COMMANDS_SPECTATE_ARMORSTANDS_ON,    "&7You have hidden all ArmorStands.");
            set(Paths.MESSAGES_COMMANDS_SPECTATE_ARMORSTANDS_OFF,   "&7ArmorStands are now visible again.");
            set(Paths.MESSAGES_COMMANDS_SPECTATE_JOIN_OWN,          "&7You are now in Spectator-Mode.");
            set(Paths.MESSAGES_COMMANDS_SPECTATE_JOIN_OTHER,        "&e%TARGET% &7is now in Spectator-Mode.");
            set(Paths.MESSAGES_COMMANDS_SPECTATE_LEAVE_OWN,         "&7You are no longer in Spectator-Mode.");
            set(Paths.MESSAGES_COMMANDS_SPECTATE_LEAVE_OTHER,       "&e%TARGET% &7is no longer in Spectator-Mode.");
            set(Paths.MESSAGES_COMMANDS_SPECTATE_PLAYER,            "&7You are now spectating &c%TARGET%&7.");
            set(Paths.MESSAGES_COMMANDS_SPECTATE_MULTIVERSE,        "&cYou do not have permission to spectate &e%TARGET% &cin this world!");
            MESSAGE_FILE_CONFIG.save(messageFile);
        }catch (IOException exception) {
            plugin.getLogger().log(Level.SEVERE, "An error occurred while loading default messages", exception);
        }
    }

    private void set(String path, String value) {
        MESSAGE_FILE_CONFIG.set(path, MESSAGE_FILE_CONFIG.get(path, value));
    }
}