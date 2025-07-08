package de.cuzim1tigaaa.spectator.extensions;

import com.onarandombox.MultiverseCore.MultiverseCore;
import de.cuzim1tigaaa.spectator.Spectator;
import lombok.Getter;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.mvplugins.multiverse.core.MultiverseCoreApi;
import org.mvplugins.multiverse.core.world.MultiverseWorld;

public class MultiverseHandler {

	@Getter
	private static MultiverseHandler instance;

	private MultiverseCore multiverseCoreOld;
	private MultiverseCoreApi multiverseCoreApi;

	public MultiverseHandler(Spectator plugin) {
		if(instance != null)
			return;

		instance = this;
		Plugin pl = plugin.getServer().getPluginManager().getPlugin("Multiverse-Core");
		if(pl == null) {
			plugin.getLogger().warning("Multiverse-Core is not installed or not enabled.");
			this.multiverseCoreOld = null;
			this.multiverseCoreApi = null;
			return;
		}

		if(pl.getDescription().getVersion().startsWith("5.")) {
			var provider = Bukkit.getServer().getServicesManager().getRegistration(MultiverseCoreApi.class);
			if(provider == null) {
				plugin.getLogger().warning("Multiverse-Core API is not available. Please ensure you are using the correct version of Multiverse-Core.");
				this.multiverseCoreOld = null;
				this.multiverseCoreApi = null;
				return;
			}

			this.multiverseCoreApi = provider.getProvider();
			plugin.getLogger().info("Multiverse-Core is installed on this server!");
			return;
		}

		if(pl instanceof MultiverseCore mv) {
			multiverseCoreOld = mv;
			plugin.getLogger().info("Multiverse-Core is installed on this server!");
		}
	}

	public boolean isMultiverseCoreInstalled() {
		return this.multiverseCoreApi != null || this.multiverseCoreOld != null;
	}

	public GameMode getGameMode(World world) {
		if(!isMultiverseCoreInstalled())
			return GameMode.SURVIVAL;

		if(this.multiverseCoreOld != null) {
			return this.multiverseCoreOld.getMVWorldManager().getMVWorld(world)
					.getGameMode();
		}

		return this.multiverseCoreApi.getWorldManager().getWorld(world)
				.map(MultiverseWorld::getGameMode).getOrElse(GameMode.SURVIVAL);
	}

	public boolean canPlayerJoinWorld(Player player, World world) {
		if(!isMultiverseCoreInstalled()) {
			return true;
		}

		return player.hasPermission("multiverse.*") ||
				player.hasPermission("multiverse.access.*") ||
				player.hasPermission("multiverse.access." + world.getName());
	}
}