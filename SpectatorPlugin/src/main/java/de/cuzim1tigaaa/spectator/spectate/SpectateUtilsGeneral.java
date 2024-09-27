package de.cuzim1tigaaa.spectator.spectate;

import de.cuzim1tigaaa.spectator.SpectateAPI;
import de.cuzim1tigaaa.spectator.Spectator;
import de.cuzim1tigaaa.spectator.cycle.CycleTask;
import de.cuzim1tigaaa.spectator.files.Config;
import de.cuzim1tigaaa.spectator.files.Messages;
import de.cuzim1tigaaa.spectator.files.Paths;
import de.cuzim1tigaaa.spectator.files.Permissions;
import de.cuzim1tigaaa.spectator.player.Inventory;
import lombok.Getter;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

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
		Bukkit.getScheduler().runTaskTimer(plugin, () -> {
			spectateAPI.getSpectators().forEach(spectator -> {
				SpectateInformation info = spectateAPI.getSpectateInfo(spectator);
				if(info == null) {
					unspectate(spectator, false);
					return;
				}

				if(info.getTarget() == null)
					return;

				plugin.getInventory().getTargetInventory(spectator, info.getTarget());
			});
		}, 0, 15);
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

		spectator.setGameMode(GameMode.SPECTATOR);

		if(target != null) {
			Bukkit.getScheduler().runTask(plugin, () -> {
				spectator.teleport(target, PlayerTeleportEvent.TeleportCause.PLUGIN);
				spectator.setSpectatorTarget(target);
			});
		}

		info.hideArmorstands();
		info.saveAttributes();
		notifyTarget(target, spectator, true);
	}

	public void spectateNext(Player spectator, Player target) {


	}

	public void unspectate(Player spectator, boolean oldLocation) {
		SpectateInformation info = spectateAPI.getSpectateInfo(spectator);
		if(info == null)
			return;

		spectateAPI.dismount(spectator);
		info.restoreArmorstands();

		Location location = spectateStartLocation.getOrDefault(spectator.getUniqueId(), spectator.getLocation());
		if(!oldLocation || !Config.getBoolean(Paths.CONFIG_SAVE_PLAYERS_LOCATION)) {
			Spectator.debug(String.format("Saved Location: %s", location));
			Spectator.debug("Using current location of player");
		}

		if(!Objects.equals(spectator.getWorld(), location.getWorld()))
			info.restoreAttributes(true);

		Bukkit.getScheduler().runTask(plugin, () -> spectator.teleport(location, PlayerTeleportEvent.TeleportCause.PLUGIN));
		spectateAPI.toggleTabList(spectator, false);
		info.restoreAttributes(true);
	}



	private void spectate(Player spectator, Player target, SpectateInformation info) {
		if(info.getState() == SpectateState.SPECTATING && !info.getAttributes().containsKey(spectator.getWorld()))
			info.saveAttributes();

		spectator.setGameMode(GameMode.SPECTATOR);

		if(target != null) {
			target.getLocation().getChunk().load();
			if(spectator.getGameMode() == GameMode.SPECTATOR) {
				Bukkit.getScheduler().runTaskLater(plugin, () -> {
					spectator.teleport(target, PlayerTeleportEvent.TeleportCause.PLUGIN);
					spectator.setSpectatorTarget(target);
				}, 1);
			}
		}

		if(CycleTask.getStateChange().containsKey(spectator.getUniqueId()))
			info.setState(CycleTask.getStateChange().remove(spectator.getUniqueId()));

		this.spectateInfo.put(spectator.getUniqueId(), info);
		notifyTarget(target, spectator, true);
		Inventory.getInventory(spectator, target);
	}

	public void simulateUnspectate(Player spectator) {
		if(!isSpectator(spectator))
			return;

		Inventory.resetInventory(spectator);
		SpectateInformation info = getSpectateInformation(spectator);
		this.spectateInfo.remove(spectator.getUniqueId());
		info.restoreAttributes(false);
	}

	public void unspectate(Player spectator) {
		if(!isSpectator(spectator))
			return;

		if(isCycling(spectator))
			stopCycle(spectator);

		SpectateInformation info = getSpectateInformation(spectator);
		Location location = spectateStartLocation.getOrDefault(spectator.getUniqueId(), null);

		Inventory.resetInventory(spectator);
		notifyTarget(getSpectateInformation(spectator).getTarget(), spectator, false);
		this.spectateInfo.remove(spectator.getUniqueId());

		if(location == null || spectateStartLocation.getOrDefault(spectator.getUniqueId(), null) == null || !oldLocation || !Config.getBoolean(Paths.CONFIG_SAVE_PLAYERS_LOCATION)) {
			location = spectator.getLocation();
			Spectator.debug(String.format("Saved Location: %s", location));
			Spectator.debug("Using current location of player");
		}

		if(!Objects.equals(spectator.getWorld(), location.getWorld()))
			info.restoreAttributes(true);

		final Location finalLocation = location;
		spectator.teleport(finalLocation, PlayerTeleportEvent.TeleportCause.PLUGIN);

		toggleTabList(spectator, false);
		info.restoreAttributes(true);
		info.restoreArmorstands();
	}




	public void restore() {
		spectateAPI.getSpectators().forEach(p ->
				unspectate(p, false));
	}

	public void notifyTarget(Player target, Player spectator, boolean spectate) {
		if(target == null)
			return;
		if(spectator.hasPermission(Permissions.BYPASS_NOTIFY))
			return;

		String message = Messages.getMessage(target, spectate ? Paths.MESSAGES_GENERAL_NOTIFY_SPECTATE :
				Paths.MESSAGES_GENERAL_NOTIFY_UNSPECTATE, "TARGET", spectator.getName());

		switch(Config.getNotifyTargetMode().toLowerCase()) {
			case "chat" -> target.spigot().sendMessage(ChatMessageType.CHAT, new TextComponent(message));
			case "actionbar" -> target.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(message));
			case "title" -> target.sendTitle(message, "", 5, 50, 5);
			case "subtitle" -> target.sendTitle("", message, 5, 50, 5);
		}
	}

}