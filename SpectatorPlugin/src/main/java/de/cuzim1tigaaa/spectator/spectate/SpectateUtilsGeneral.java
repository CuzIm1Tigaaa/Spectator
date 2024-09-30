package de.cuzim1tigaaa.spectator.spectate;

import de.cuzim1tigaaa.spectator.SpectateAPI;
import de.cuzim1tigaaa.spectator.Spectator;
import de.cuzim1tigaaa.spectator.cycle.CycleTask;
import de.cuzim1tigaaa.spectator.files.*;
import lombok.Getter;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.IllegalPluginAccessException;

import java.util.*;

@Getter
public class SpectateUtilsGeneral {

	private final Spectator plugin;
	private final SpectateAPI spectateAPI;

	private final Map<UUID, Location> spectateStartLocation = new HashMap<>();
	private final Map<UUID, CycleTask> spectateCycle;

	public SpectateUtilsGeneral(SpectateAPI spectateAPI) {
		this.plugin = spectateAPI.getPlugin();
		this.spectateAPI = spectateAPI;

		this.spectateCycle = new HashMap<>();

		this.run();
	}

	private void run() {
		Bukkit.getScheduler().runTaskTimer(plugin, () ->
				spectateAPI.getSpectators().forEach(spectator -> {
					SpectateInformation info = spectateAPI.getSpectateInfo(spectator);
					if(info == null) {
						unspectate(spectator, false);
						return;
					}

					if(info.getTarget() == null)
						return;

					plugin.getInventory().getTargetInventory(spectator, info.getTarget());
				}), 0, 15);
	}


	public void spectate(Player spectator, Player target) {
		spectateAPI.getSpectatorsOf(spectator).forEach(spectateAPI::dismount);

		SpectateInformation info;
		if(spectateAPI.isSpectator(spectator)) {
			info = spectateAPI.getSpectateInfo(spectator);
			info.setTarget(target);
		}else
			info = new SpectateInformation(spectator, target);

		spectateAPI.toggleTabList(spectator, true);

		if(!info.getAttributes().containsKey(spectator.getWorld()))
			info.saveAttributes();

		spectateAPI.hideArmorstands(spectator);
		spectator.setGameMode(GameMode.SPECTATOR);

		if(target != null) {
			Bukkit.getScheduler().runTask(plugin, () -> {
				spectator.teleport(target, PlayerTeleportEvent.TeleportCause.PLUGIN);
				spectator.setSpectatorTarget(target);
			});
		}

		plugin.getInventory().getTargetInventory(spectator, target);
		spectateAPI.getSpectateInfo().add(info);
		notifyTarget(target, spectator, true);
	}

	public void unspectate(Player spectator, boolean oldLocation) {
		SpectateInformation info = spectateAPI.getSpectateInfo(spectator);
		if(info == null)
			return;

		spectateAPI.dismount(spectator);
		spectateAPI.restoreArmorstands();

		Location location = spectateStartLocation.getOrDefault(spectator.getUniqueId(), spectator.getLocation());
		if(!oldLocation || !Config.getBoolean(Paths.CONFIG_SAVE_PLAYERS_LOCATION)) {
			Spectator.debug(String.format("Saved Location: %s", location));
			Spectator.debug("Using current location of player");
			location = spectator.getLocation();
		}
		final Location finalLocation = location;

		if(!Objects.equals(spectator.getWorld(), location.getWorld()))
			info.restoreAttributes(true);

		Bukkit.getScheduler().runTask(plugin, () -> spectator.teleport(finalLocation, PlayerTeleportEvent.TeleportCause.PLUGIN));
		spectateAPI.toggleTabList(spectator, false);
		spectateAPI.getSpectateInfo().remove(info);
		plugin.getInventory().resetInventory(spectator);
		info.restoreAttributes(true);
	}

	public void simulateUnspectate(Player spectator) {

	}

	public void restore() {
		try {
			spectateAPI.getSpectators().forEach(p ->
					unspectate(p, false));
		}catch(IllegalPluginAccessException e) {
			plugin.getLogger().warning("This exception might get thrown, when the server is shutting down");
		}
	}

	public void notifyTarget(Player target, Player spectator, boolean spectate) {
		if(target == null)
			return;
		if(spectator.hasPermission(Permissions.BYPASS_NOTIFY))
			return;

		String message = Messages.getMessage(target, spectate ? Paths.MESSAGES_GENERAL_NOTIFY_SPECTATE :
				Paths.MESSAGES_GENERAL_NOTIFY_UNSPECTATE, "TARGET", spectator.getName());

		switch(Config.getString(Paths.CONFIG_NOTIFY_CURRENT_TARGET).toLowerCase()) {
			case "chat" -> target.spigot().sendMessage(ChatMessageType.CHAT, new TextComponent(message));
			case "actionbar" -> target.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(message));
			case "title" -> target.sendTitle(message, "", 5, 50, 5);
			case "subtitle" -> target.sendTitle("", message, 5, 50, 5);
		}
	}

}