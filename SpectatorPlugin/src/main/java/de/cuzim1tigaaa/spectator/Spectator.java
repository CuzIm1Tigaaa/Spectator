package de.cuzim1tigaaa.spectator;

import de.cuzim1tigaaa.spectator.commands.*;
import de.cuzim1tigaaa.spectator.extensions.*;
import de.cuzim1tigaaa.spectator.files.*;
import de.cuzim1tigaaa.spectator.listener.SpectatorListener;
import de.cuzim1tigaaa.spectator.player.Inventory;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

@Getter
public class Spectator extends JavaPlugin {

	@Getter
	private static Spectator plugin;

	private SpectateAPI spectateAPI;
	private UpdateChecker updateChecker;

	private boolean papiInstalled;
	private Inventory inventory;

	@Override
	public void onEnable() {
		plugin = this;

		this.info();

		this.spectateAPI = new SpectateAPI(this);
		this.updateChecker = new UpdateChecker(this);

		register();
	}

	@Override
	public void onDisable() {
		this.spectateAPI.getSpectateGeneral().restore();
		plugin = null;
	}

	private void register() {
		papiInstalled = false;
		if(Bukkit.getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
			this.getLogger().info("PlaceholderAPI is installed on this server!");
			this.getLogger().info("Registering plugins placeholders…");
			new Placeholders(this).register();
			papiInstalled = true;
		}

		this.reload();
		new Metrics(this);

		this.inventory = new Inventory(this);
		this.getLogger().info("Register Events & Commands…");

		new SpectatorListener(this);

		new MultiverseHandler(this);

		new Spectate(this);
		new SpectateCycle(this);
		new SpectateHere(this);
		//new SpectateInfo(this);
		new SpectateReload(this);
		new SpectateList(this);
		new UnSpectate(this);
	}

	public void reload() {
		this.getLogger().info("Loading config settings…");
		Config.getConfig().loadConfig();

		this.getLogger().info("Loading plugin messages…");
		Messages.getMessages().loadLanguageFile();
	}

	private void info() {
		getServer().getLogger().info("-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-");
		getServer().getLogger().info(String.format("Plugin %s, v%s by %s",
				this.getDescription().getName(),
				this.getDescription().getVersion(),
				this.getDescription().getAuthors().getFirst()));
		getServer().getLogger().info("-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-");
		getServer().getLogger().info("This Plugin is a modified Version");
		getServer().getLogger().info("of kosakriszi's spectator Plugin!");
		getServer().getLogger().info("spigotmc.org/resources/spectator.16745/");
		getServer().getLogger().info("-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-");
	}

	public List<String> getOnlinePlayerNames() {
		List<String> names = new ArrayList<>();
		Bukkit.getOnlinePlayers().forEach(player -> names.add(player.getName()));
		return names;
	}

	public static void debug(String message) {
		if(Config.getBoolean(Paths.CONFIG_DEBUG))
    		getPlugin().getLogger().info(message);
	}
}