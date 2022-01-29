package de.cuzim1tigaaa.spectator.files;

import de.cuzim1tigaaa.spectator.Spectator;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

@SuppressWarnings("ResultOfMethodCallIgnored")
public class Config {

    private static FileConfiguration config;
    private static File configFile;

    public static void loadConfig(Spectator plugin, boolean enable) {
        int serverVersion = Integer.parseInt(plugin.getServer().getBukkitVersion().split("\\.")[1]), currentVersion;

        if(serverVersion < 18) {
            currentVersion = 3;
            saveDefaultConfig(plugin);
            if (getConfig(plugin).getInt("ConfigVersion") < currentVersion) replaceConfig(plugin, true);
            return;
        }

        currentVersion = 4;

        try {
            configFile = new File(plugin.getDataFolder(), "config.yml");
            if(!configFile.exists()) {
                config = new YamlConfiguration();
                config.save(configFile);

                // Workaround because if the file gets created, comments are not generated...
                if(enable) loadConfig(plugin, false);
                return;
            }
            config = null;
            config = YamlConfiguration.loadConfiguration(configFile);
            config.options().setHeader(comments(
                    "This is the configuration file of the plugin. Everything should be self-explanatory",
                    "If there is anything unclear, first take a look into the GitHub wiki:",
                    "https://github.com/CuzIm1Tigaaa/Spectator/wiki"));

            set("Settings", comments(), null);
            set("Settings.Save", comments(), null);
            set("Settings.Mirror", comments(), null);
            set("Settings.Inventory", comments(), null);
            set("Settings.Cycle", comments(), null);

            set(Paths.CONFIG_VERSION, comments("This is the current version of the config, DO NOT CHANGE!"), 3);

            set(Paths.CONFIG_NOTIFY_UPDATE, comments(null,
                    "Notify players with following permission if there is an update ",
                    "spectator.notify.update"), true);

            set(Paths.CONFIG_HIDE_PLAYERS_TAB, comments(null,
                    "Hide spectators for players without following permission:",
                    "spectator.bypass.tablist",
                    "Requires following permission for spectator: spectator.utils.hidetab"), true);

            set(Paths.CONFIG_KICK_WHILE_CYCLING, comments(null,
                    "Allows cycling players to be kicked"), false);

            set(Paths.CONFIG_SAVE_PLAYERS_LOCATION, comments(null,
                    "Should the Location of a Player be saved when executing /spec?"), true);

            set(Paths.CONFIG_SAVE_PLAYERS_FLIGHTMODE, comments(null,
                    "Should the FlightMode of a Player be saved when executing /spec?"), true);


            set(Paths.CONFIG_MIRROR_TARGET_EFFECTS, comments(null,
                    "Should a Player get all effects his target currently has while spectating?",
                    "Requires following permission: spectator.utils.mirroreffects"), true);

            set(Paths.CONFIG_MIRROR_TARGETS_INVENTORY, comments(null,
                    "Should a Player get all contents of his targets inventory while spectating?",
                    "Requires following permission: spectator.utils.mirrorinventory"), true);


            set(Paths.CONFIG_INVENTORY_CONTAINERS, comments(null,
                    "Should containers opened by a player also be opened for a spectator? Available types of containers:",
                    "BARREL; BLAST_FURNACE; BREWING_STAND; (TRAPPED-)CHEST; DISPENSER; DROPPER; FURNACE; HOPPER; SMOKER; SHULKER_BOX; LECTERN",
                    "Requires following permission: spectator.utils.opencontainers"), true);

            set(Paths.CONFIG_INVENTORY_ENDERCHEST, comments(null,
                    "Should enderchests opened by a player also be opened for a spectator?",
                    "Requires following permission: spectator.utils.openenderchest"), false);


            set(Paths.CONFIG_PAUSE_WHEN_NO_PLAYERS, comments(null,
                    "Should the Cycle be paused, when there are no players online?",
                    "Otherwise, Cycle will be stopped"), false);

            config.save(configFile);
        }catch (IOException exception) { exception.printStackTrace(); }
        if(getInt(Paths.CONFIG_VERSION) < currentVersion) replaceConfig(plugin, false);
    }

    private static List<String> comments(String... comment) {
        return new ArrayList<>(List.of(comment));
    }
    private static void set(String path, List<String> comment, Object value) {
        if(comment != null && comment.size() > 0) config.setComments(path, comment);
        if(value != null) config.set(path, config.get(path, value));
    }
    private static void replaceConfig(Spectator plugin, boolean old) {
        int i = 1;
        File backUp = new File(plugin.getDataFolder(), "configBackUp_" + i + ".yml");
        while(backUp.exists()) backUp = new File(plugin.getDataFolder(), "configBackUp_" + (i++) + ".yml");

        try { Files.copy(configFile.toPath(), backUp.toPath());
        }catch(IOException e) { e.printStackTrace(); }

        configFile.delete();
        if(old) saveDefaultConfig(plugin);
    }

    public static Object get(String path) { return config.get(path); }
    public static Integer getInt(String path) { return config.getInt(path); }
    public static boolean getBoolean(String path) { return config.getBoolean(path); }



    public static void reloadConfig(Spectator plugin) {
        if (configFile == null) configFile = new File(plugin.getDataFolder(), "config.yml");

        config = YamlConfiguration.loadConfiguration(configFile);
        InputStream stream = plugin.getResource("config.yml");
        if (stream != null) {
            YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(stream));
            config.setDefaults(defaultConfig);
        }
    }

    public static FileConfiguration getConfig(Spectator plugin) {
        if (config == null) reloadConfig(plugin);
        return config;
    }

    public static void saveConfig(Spectator plugin) {
        if (config != null && configFile != null) {
            try { getConfig(plugin).save(configFile);
            } catch (IOException exception) { Bukkit.getLogger().log(Level.SEVERE,
                    "An error occurred while trying to load config.yml", exception); }
        }
    }

    public static void saveDefaultConfig(Spectator plugin) {
        if (configFile == null) configFile = new File(plugin.getDataFolder(), "config.yml");
        if (!configFile.exists()) plugin.saveResource("config.yml", false);
    }

}