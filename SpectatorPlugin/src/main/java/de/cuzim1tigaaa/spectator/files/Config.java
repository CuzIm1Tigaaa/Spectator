package de.cuzim1tigaaa.spectator.files;

import de.cuzim1tigaaa.spectator.Spectator;
import lombok.Getter;
import org.bukkit.boss.BarColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.logging.Level;

public class Config {

	private static Config INSTANCE;
	private final Spectator plugin;
	private final int configVersion;

	private static FileConfiguration CONFIG_FILE_CONFIG;
	@Getter
	private File configFile;

	@Getter
	private String showTargetMode, notifyTargetMode;

	public Config(Spectator plugin) {
		this.plugin = plugin;
		this.configVersion = 13;
	}

	public static Config getConfig() {
		if(INSTANCE == null)
			INSTANCE = new Config(Spectator.getPlugin());
		return INSTANCE;
	}

	public void loadConfig() {
		int serverVersion = Integer.parseInt(plugin.getServer().getBukkitVersion().split("\\.")[1].substring(0, 2));

		if(serverVersion < 18) {
			saveDefaultConfig(plugin);
			if(CONFIG_FILE_CONFIG.getInt("ConfigVersion") < configVersion) replaceConfig(true);
			return;
		}

		try {
			configFile = new File(plugin.getDataFolder(), "config.yml");
			if(!configFile.exists()) {
				CONFIG_FILE_CONFIG = new YamlConfiguration();
				CONFIG_FILE_CONFIG.save(configFile);
			}
			CONFIG_FILE_CONFIG = YamlConfiguration.loadConfiguration(configFile);

			CONFIG_FILE_CONFIG.options().setHeader(List.of("This is the configuration file of the plugin. Everything should be self-explanatory",
					"If there is anything unclear, first take a look into the GitHub wiki:",
					"https://github.com/CuzIm1Tigaaa/Spectator/wiki"));

			set(Paths.CONFIG_VERSION, configVersion,
					"This is the current version of the config, DO NOT CHANGE!",
					"If the version changes, the plugin will automatically",
					"backup your current config and create a new one");

			set("Settings", null, (String) null);

			//region Settings

			set(Paths.CONFIG_DEBUG, false, null,
					"This prints different debug messages in the server console",
					"Can be useful for reporting problems");

			set(Paths.CONFIG_NOTIFY_UPDATE, true, null,
					"If the plugin gets updated, players with the following permission",
					"will receive a message when they join",
					"Permission: spectator.notify.update");

			set(Paths.CONFIG_LANGUAGE, "en_US", null,
					"Specify which language file should be used by the plugin",
					"You can also add new languages! :)");

			set(Paths.CONFIG_HIDE_PLAYERS_TAB, true, null,
					"Spectators with the first following permission will be hidden in the tablist",
					"Can be bypassed by players with the second permission.",
					"Permission 1: spectator.utils.hidetab",
					"Permission 2: spectator.bypass.tablist");

			set(Paths.CONFIG_CYCLE_KICK_PLAYERS, false, null,
					"Cycling players cannot be kicked by any other player.");

			set(Paths.CONFIG_NOTIFY_CURRENT_TARGET, "NONE", null,
					"Shows a message to target players that they are being spectated",
					"Possible values are (without quotation):",
					"\"CHAT\", \"ACTIONBAR\", \"TITLE\", \"SUBTITLE\", \"NONE\"");
			notifyTargetMode = getString(Paths.CONFIG_NOTIFY_CURRENT_TARGET);

			set(Paths.CONFIG_HIDE_ARMOR_STANDS, false, null,
					"Hides all invisible armor stands for spectators",
					"Can be toggled with the following command",
					"/spectate -armorstand");


			set("Settings.Save", null, (String) null);

			set(Paths.CONFIG_SAVE_PLAYERS_LOCATION, true, null,
					"The players' location (where he executed /spec) will be saved",
					"Otherwise when the player leaves spectator mode, he will be at",
					"his current location, equals to /spectatehere.");

			set(Paths.CONFIG_SAVE_PLAYERS_FLIGHT_MODE, true, null,
					"The players' flight mode will be saved. Otherwise, when the player",
					"leaves spectator mode, he won't be he won't be flying anymore.",
					"Requires allow-flight to true in server.properties!");

			set(Paths.CONFIG_SAVE_PLAYERS_DATA, true, null,
					"The players' data will be saved. This includes remaining air and the",
					"burning time. Otherwise, when the player leaves spectator mode, all",
					"these values reset to default.");

			set("Settings.Mirror", null, (String) null);

			set(Paths.CONFIG_MIRROR_TARGET_EFFECTS, true, null,
					"Get all effects a spectator target currently has",
					"Requires following permission: spectator.utils.mirroreffects");

			set(Paths.CONFIG_MIRROR_TARGETS_INVENTORY, true, null,
					"Adds all inventory content of your spectator target in your inventory",
					"Requires following permission: spectator.utils.mirrorinventory");

			set("Settings.Inventory", null, (String) null);

			set(Paths.CONFIG_INVENTORY_CONTAINERS, true, null,
					"Allows spectators with the following permission to see into containers their target opens",
					"Only available for the following types of containers:",
					"BARREL; BLAST_FURNACE; BREWING_STAND; (TRAPPED-)CHEST; DISPENSER; DROPPER; FURNACE; HOPPER; SMOKER; SHULKER_BOX; LECTERN",
					"Permission: spectator.utils.opencontainers");

			set(Paths.CONFIG_INVENTORY_ENDER_CHEST, false, null,
					"Allows spectators with the following permission to see into their target's enderchest",
					"Only when the target opens a physically enderchest!",
					"Permission: spectator.utils.openenderchest");

			set("Settings.Cycle", null, (String) null);

			set(Paths.CONFIG_CYCLE_NO_PLAYERS, true, null,
					"Allows starting cycling even with no players online",
					"Cycle will then work, when players are online!",
					"Might be useful when using the plugin as a \"camera\"");

			set(Paths.CONFIG_CYCLE_PAUSE_NO_PLAYERS, false, null,
					"The cycle gets paused if there are no longer any players online and will automatically restart",
					"Otherwise the cycle will simply be stopped");

			set(Paths.CONFIG_CYCLE_SHOW_TARGET, "BOSSBAR", null,
					"Shows a message to cycling players with the name of the current target",
					"Possible values are (without quotation):",
					"\"BOSSBAR\", \"ACTIONBAR\", \"TITLE\", \"SUBTITLE\", \"NONE\"");
			showTargetMode = getString(Paths.CONFIG_CYCLE_SHOW_TARGET);

			set(Paths.CONFIG_CYCLE_MIN_INTERVAL, 0, null,
					"Sets the minimum time, that the cycle mode interval has to be",
					"If set to 0, no minimum is set");

			set(Paths.CONFIG_CYCLE_MAX_INTERVAL, 0, null,
					"Sets the maximum time, that the cycle mode interval can be",
					"If set to 0, no maximum is set");

			set(Paths.CONFIG_CYCLE_BOSSBAR_COLOR, "BLUE", null,
					"Sets the color of the bossbar, that shows the current target",
					"Possible values are (without quotation):",
					"\"BLUE\", \"GREEN\", \"PINK\", \"PURPLE\", \"RED\", \"WHITE\", \"YELLOW\"");

			set(Paths.CONFIG_CYCLE_BOSSBAR_FACTOR, 1, null,
					"Sets the factor how fast the bossbar segments are going down",
					"1 is the default value, meaning the bar gets reduced by 1 segment every second",
					"The higher the value, the faster the bar segments are going down",
					"If the value is below zero or not an integer (e.g. 1.5), the default value is used");

			CONFIG_FILE_CONFIG.save(configFile);
		}catch(IOException exception) {
			plugin.getLogger().log(Level.SEVERE, "An error occurred while loading config", exception);
		}
		if(CONFIG_FILE_CONFIG.getInt(Paths.CONFIG_VERSION) < configVersion) replaceConfig(false);
	}

	private void set(String path, Object value, String... comment) {
		if(value == null && CONFIG_FILE_CONFIG.getConfigurationSection(path) == null)
			CONFIG_FILE_CONFIG.createSection(path);
		CONFIG_FILE_CONFIG.set(path, CONFIG_FILE_CONFIG.get(path, value));
		if(comment != null && comment.length > 0) {
			List<String> comments = new ArrayList<>(Arrays.asList(comment));
			CONFIG_FILE_CONFIG.setComments(path, comments);
		}
	}

	private void replaceConfig(boolean old) {
		int i = 1;
		File backUp = new File(plugin.getDataFolder(), "configBackUp_" + i + ".yml");
		while(backUp.exists())
			backUp = new File(plugin.getDataFolder(), "configBackUp_" + (i++) + ".yml");

		try {
			Files.copy(configFile.toPath(), backUp.toPath());
		}catch(IOException exception) {
			plugin.getLogger().log(Level.SEVERE, "An error occurred while creating backup config", exception);
		}

		//noinspection ResultOfMethodCallIgnored
		configFile.delete();
		if(old) saveDefaultConfig(plugin);
		else loadConfig();
	}

	public void saveDefaultConfig(Spectator plugin) {
		if(configFile == null)
			configFile = new File(plugin.getDataFolder(), "config.yml");
		if(!configFile.exists())
			plugin.saveResource("config.yml", false);
		CONFIG_FILE_CONFIG = YamlConfiguration.loadConfiguration(configFile);
	}


	public static boolean getBoolean(String path) {
		return CONFIG_FILE_CONFIG.getBoolean(path);
	}

	public static String getString(String path) {
		return CONFIG_FILE_CONFIG.getString(path);
	}

	public static int getInt(String path) {
		return CONFIG_FILE_CONFIG.getInt(path);
	}

	public static BarColor getBarColor() {
		try {
			return BarColor.valueOf(CONFIG_FILE_CONFIG.getString(Paths.CONFIG_CYCLE_BOSSBAR_COLOR));
		}catch(Exception e) {
			return BarColor.BLUE;
		}
	}
}