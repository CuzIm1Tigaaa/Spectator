package de.cuzim1tigaaa.spectator.files;

import de.cuzim1tigaaa.spectator.Spectator;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.*;
import java.nio.file.Files;
import java.util.logging.Level;

import static org.bukkit.Bukkit.getLogger;

@SuppressWarnings("ResultOfMethodCallIgnored")
public class Config {

    private FileConfiguration config;
    private File configFile;

    public Config() {
        int currentVersion = 2;
        saveDefaultConfig();
        if(this.getConfig().getInt(Paths.CONFIG_VERSION) < currentVersion) this.replaceConfig();
    }

    public void reloadConfig() {
        if(configFile == null) configFile = new File(Spectator.getPlugin().getDataFolder(), "config.yml");
        config = YamlConfiguration.loadConfiguration(configFile);

        InputStream stream = Spectator.getPlugin().getResource("config.yml");

        if(stream != null) {
            YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(stream));
            config.setDefaults(defaultConfig);
        }
    }

    private void replaceConfig() {
        File backUp = new File(Spectator.getPlugin().getDataFolder(), "configBackUp.yml");
        if(backUp.exists()) backUp.delete();

        try { Files.copy(configFile.toPath(), backUp.toPath());
        }catch(IOException e) { e.printStackTrace(); }

        configFile.delete();
        this.saveDefaultConfig();
    }

    public FileConfiguration getConfig() {
        if(config == null) reloadConfig();
        return config;
    }
    public void saveConfig() {
        if(config == null || configFile == null) return;

        try { getConfig().save(configFile);
        }catch(IOException exception) {
            getLogger().log(Level.SEVERE, "An error occurred while trying to load config.yml", exception);
        }
    }
    public void saveDefaultConfig() {
        if(configFile == null) configFile = new File(Spectator.getPlugin().getDataFolder(), "config.yml");
        if(!configFile.exists()) Spectator.getPlugin().saveResource("config.yml", false);
    }

    public Object get(String path) {
        return this.getConfig().get(path);
    }
    public Integer getInt(String path) {
        return this.getConfig().getInt(path);
    }
    public boolean getBoolean(String path) {
        return this.getConfig().getBoolean(path);
    }
}