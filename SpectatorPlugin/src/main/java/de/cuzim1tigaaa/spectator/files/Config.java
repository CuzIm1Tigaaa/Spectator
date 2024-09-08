package de.cuzim1tigaaa.spectator.files;

import de.cuzim1tigaaa.spectator.Spectator;
import lombok.Getter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.logging.Level;

public final class Config {

    private static FileConfiguration config;
    @Getter private static File configFile;

    private static final int configVersion = 13;
    @Getter private static String showTargetMode, notifyTargetMode;

    public static void loadConfig(Spectator plugin) {
        saveDefaultConfig(plugin);
        if(config.getInt("ConfigVersion") < configVersion)
            replaceConfig(plugin);
        showTargetMode = getString(Paths.CONFIG_CYCLE_SHOW_TARGET);
        notifyTargetMode = getString(Paths.CONFIG_NOTIFY_CURRENT_TARGET);
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


    private static void replaceConfig(Spectator plugin) {
        int i = 1;
        File backUp = new File(plugin.getDataFolder(), "configBackUp_" + i + ".yml");
        while(backUp.exists()) backUp = new File(plugin.getDataFolder(), "configBackUp_" + (i++) + ".yml");

        try {
            Files.copy(configFile.toPath(), backUp.toPath());
        }catch(IOException exception) {
            plugin.getLogger().log(Level.SEVERE, "An error occurred while creating backup config", exception);
        }
        configFile.delete();
	    saveDefaultConfig(plugin);
    }

    public static void saveDefaultConfig(Spectator plugin) {
        if (configFile == null) configFile = new File(plugin.getDataFolder(), "config.yml");
        if (!configFile.exists()) plugin.saveResource("config.yml", false);
        config = YamlConfiguration.loadConfiguration(configFile);
    }
}