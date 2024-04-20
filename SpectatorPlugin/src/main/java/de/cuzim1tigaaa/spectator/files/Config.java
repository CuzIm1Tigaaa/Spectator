package de.cuzim1tigaaa.spectator.files;

import de.cuzim1tigaaa.spectator.Spectator;
import lombok.Getter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public final class Config {

    private static FileConfiguration config;
    @Getter private static File configFile;

    private static final int configVersion = 12;

    @Getter private static String showTargetMode, notifyTargetMode;

    public static void loadConfig(Spectator plugin) {
        int serverVersion = Integer.parseInt(plugin.getServer().getBukkitVersion().split("\\.")[1].substring(0, 2));

        if(serverVersion < 18) {
            saveDefaultConfig(plugin);
            if(config.getInt("ConfigVersion") < configVersion) replaceConfig(plugin, true);
            showTargetMode = getString(Paths.CONFIG_CYCLE_SHOW_TARGET);
            notifyTargetMode = getString(Paths.CONFIG_NOTIFY_CURRENT_TARGET);
            return;
        }

        try {
            configFile = new File(plugin.getDataFolder(), "config.yml");
            if(!configFile.exists()) {
                config = new YamlConfiguration();
                config.save(configFile);
            }
            config = YamlConfiguration.loadConfiguration(configFile);

            config.options().setHeader(comments(false,
                    "This is the configuration file of the plugin. Everything should be self-explanatory",
                    "If there is anything unclear, first take a look into the GitHub wiki:",
                    "https://github.com/CuzIm1Tigaaa/Spectator/wiki"));

            set(Paths.CONFIG_VERSION, comments(false,
                    "This is the current version of the config, DO NOT CHANGE!",
                    "If the version changes, the plugin will automatically",
                    "backup your current config and create a new one"), configVersion);

            set("Settings", comments(true), null);

            set(Paths.CONFIG_DEBUG, comments(true,
                    "This prints different debug messages in the server console",
                    "Can be useful for reporting problems"), false);

            set(Paths.CONFIG_NOTIFY_UPDATE, comments(true,
                    "If the plugin gets updated, players with the following permission",
                    "will receive a message when they join",
                    "Permission: spectator.notify.update"), true);

            set(Paths.CONFIG_LANGUAGE, comments(true,
                    "Specify which language file should be used by the plugin",
                    "You can also add new languages! :)"), "en_US");

            set(Paths.CONFIG_HIDE_PLAYERS_TAB, comments(true,
                    "Spectators with the first following permission will be hidden in the tablist",
                    "Can be bypassed by players with the second permission.",
                    "Permission 1: spectator.utils.hidetab",
                    "Permission 2: spectator.bypass.tablist"), true);

            set(Paths.CONFIG_CYCLE_KICK_PLAYERS, comments(true,
                    "Cycling players cannot be kicked by any other player."), false);

            set(Paths.CONFIG_NOTIFY_CURRENT_TARGET, comments(true,
                    "Shows a message to target players that they are being spectated",
                    "Possible values are (without quotation):",
                    "\"CHAT\", \"ACTIONBAR\", \"TITLE\", \"SUBTITLE\", \"NONE\""), "NONE");
            notifyTargetMode = getString(Paths.CONFIG_NOTIFY_CURRENT_TARGET);

            set("Settings.Save", comments(true), null);

            set(Paths.CONFIG_SAVE_PLAYERS_LOCATION, comments(true,
                    "The players' location (where he executed /spec) will be saved",
                    "Otherwise when the player leaves spectator mode, he will be at",
                    "his current location, equals to /spectatehere."), true);

            set(Paths.CONFIG_SAVE_PLAYERS_FLIGHT_MODE, comments(true,
                    "The players' flight mode will be saved. Otherwise, when the player",
                    "leaves spectator mode, he won't be he won't be flying anymore.",
                    "Requires allow-flight to true in server.properties!"), true);

            set(Paths.CONFIG_SAVE_PLAYERS_DATA, comments(true,
                    "The players' data will be saved. This includes remaining air and the",
                    "burning time. Otherwise, when the player leaves spectator mode, all",
                    "these values reset to default."), true);

            set("Settings.Mirror", comments(true), null);

            set(Paths.CONFIG_MIRROR_TARGET_EFFECTS, comments(true,
                    "Get all effects a spectator target currently has",
                    "Requires following permission: spectator.utils.mirroreffects"), true);

            set(Paths.CONFIG_MIRROR_TARGETS_INVENTORY, comments(true,
                    "Adds all inventory content of your spectator target in your inventory",
                    "Requires following permission: spectator.utils.mirrorinventory"), true);

            set("Settings.Inventory", comments(true), null);

            set(Paths.CONFIG_INVENTORY_CONTAINERS, comments(true,
                    "Allows spectators with the following permission to see into containers their target opens",
                    "Only available for the following types of containers:",
                    "BARREL; BLAST_FURNACE; BREWING_STAND; (TRAPPED-)CHEST; DISPENSER; DROPPER; FURNACE; HOPPER; SMOKER; SHULKER_BOX; LECTERN",
                    "Permission: spectator.utils.opencontainers"), true);

            set(Paths.CONFIG_INVENTORY_ENDER_CHEST, comments(true,
                    "Allows spectators with the following permission to see into their target's enderchest",
                    "Only when the target opens a physically enderchest!",
                    "Permission: spectator.utils.openenderchest"), false);

            set("Settings.Cycle", comments(true), null);

            set(Paths.CONFIG_CYCLE_NO_PLAYERS, comments(true,
                    "Allows starting cycling even with no players online",
                    "Cycle will then work, when players are online!",
                    "Might be useful when using the plugin as a \"camera\""), true);

            set(Paths.CONFIG_CYCLE_PAUSE_NO_PLAYERS, comments(true,
                    "The cycle gets paused if there are no longer any players online and will automatically restart",
                    "Otherwise the cycle will simply be stopped"), false);

            set(Paths.CONFIG_CYCLE_SHOW_TARGET, comments(true,
                    "Shows a message to cycling players with the name of the current target",
                    "Possible values are (without quotation):",
                    "\"BOSSBAR\", \"ACTIONBAR\", \"TITLE\", \"SUBTITLE\", \"NONE\""), "BOSSBAR");
            showTargetMode = getString(Paths.CONFIG_CYCLE_SHOW_TARGET);

            set(Paths.CONFIG_CYCLE_MIN_INTERVAL, comments(true,
                    "Sets the minimum time, that the cycle mode interval has to be",
                    "If set to 0, no minimum is set"), 0);

            set(Paths.CONFIG_CYCLE_MAX_INTERVAL, comments(true,
                    "Sets the maximum time, that the cycle mode interval can be",
                    "If set to 0, no maximum is set"), 0);
            config.save(configFile);
        }catch(IOException exception) {
            plugin.getLogger().log(Level.SEVERE, "An error occurred while loading config", exception);
        }
        if(config.getInt(Paths.CONFIG_VERSION) < configVersion) replaceConfig(plugin, false);
    }


    private static void set(String path, List<String> comment, Object value) {
        if(value == null && config.getConfigurationSection(path) == null) config.createSection(path);
        else config.set(path, config.get(path, value));
        if(comment != null && !comment.isEmpty()) config.setComments(path, comment);
    }


    private static List<String> comments(boolean empty, String... comment) {
        List<String> comments = new ArrayList<>();
        if(empty) comments.add(null);
        if(comment != null && comment.length > 0) comments.addAll(List.of(comment));
        return comments;
    }


    public static boolean getBoolean(String path) {
        return config.getBoolean(path);
    }

    public static String getString(String path) {
        return config.getString(path);
    }

    public static int getInt(String path) {
        return config.getInt(path);
    }


    private static void replaceConfig(Spectator plugin, boolean old) {
        int i = 1;
        File backUp = new File(plugin.getDataFolder(), "configBackUp_" + i + ".yml");
        while(backUp.exists()) backUp = new File(plugin.getDataFolder(), "configBackUp_" + (i++) + ".yml");

        try {
            Files.copy(configFile.toPath(), backUp.toPath());
        }catch(IOException exception) {
            plugin.getLogger().log(Level.SEVERE, "An error occurred while creating backup config", exception);
        }

        //noinspection ResultOfMethodCallIgnored
        configFile.delete();
        if(old) saveDefaultConfig(plugin);
        else loadConfig(plugin);
    }

    public static void saveDefaultConfig(Spectator plugin) {
        if (configFile == null) configFile = new File(plugin.getDataFolder(), "config.yml");
        if (!configFile.exists()) plugin.saveResource("config.yml", false);
        config = YamlConfiguration.loadConfiguration(configFile);
    }
}