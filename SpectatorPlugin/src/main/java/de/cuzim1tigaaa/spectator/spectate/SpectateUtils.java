package de.cuzim1tigaaa.spectator.spectate;

import de.cuzim1tigaaa.spectator.Spectator;
import de.cuzim1tigaaa.spectator.cycle.CycleTask;
import de.cuzim1tigaaa.spectator.files.*;
import de.cuzim1tigaaa.spectator.player.Inventory;
import de.cuzim1tigaaa.spectator.player.PlayerAttributes;
import lombok.Getter;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.*;
import java.util.stream.Collectors;

@Getter
public class SpectateUtils {

	private final Spectator plugin;

	private final Map<UUID, Location> spectateStartLocation = new HashMap<>();
	private final Map<UUID, SpectateInformation> spectateInfo;
	private final Map<UUID, CycleTask> spectateCycle;

	public SpectateUtils(Spectator plugin) {
		this.plugin = plugin;
		this.spectateInfo = new HashMap<>();
		this.spectateCycle = new HashMap<>();

		this.run();
	}

	private void run() {
		Bukkit.getScheduler().runTaskTimer(plugin, () -> {
			for(Player spectator : getSpectators()) {
				SpectateInformation info = getSpectateInformation(spectator);

				if(info == null || info.getTarget() == null)
					continue;

				if(spectateCycle.containsKey(spectator.getUniqueId()))
					getSpectateInformation(spectator).setState(SpectateState.CYCLING);

				final Player target = info.getTarget();
				if(!target.isOnline())
					continue;

				Inventory.updateInventory(spectator, target);

				if(spectator.getGameMode() != GameMode.SPECTATOR) {
					unspectate(spectator, true);
					continue;
				}

				if(spectator.getSpectatorTarget() == null) {
					dismount(spectator);
					Bukkit.getScheduler().runTaskLater(plugin, () -> spectator.setSpectatorTarget(target), 5);
				}

				Location spLoc = spectator.getLocation(), taLoc = target.getLocation();
				if(!Objects.equals(spLoc.getWorld(), taLoc.getWorld()) || spLoc.distanceSquared(taLoc) > 3) {
					spectator.setSpectatorTarget(null);
					Bukkit.getScheduler().runTaskLater(plugin, () -> {
						spectator.teleport(target, PlayerTeleportEvent.TeleportCause.PLUGIN);
						spectator.setSpectatorTarget(target);
					}, 1);
				}

				if(spectator.getGameMode() != GameMode.SPECTATOR)
					unspectate(spectator, true);
			}
		}, 0, 10);
	}


	public void spectate(Player spectator, Player target) {
		getSpectatorsOf(spectator).forEach(this::dismount);

		SpectateInformation info;
		if(isSpectator(spectator)) {
			info = getSpectateInformation(spectator);
			info.setTarget(target);
		}else
			info = new SpectateInformation(spectator, target);

		boolean switchWorld = false;
		toggleTabList(spectator, true);
		if(target != null && !Objects.equals(spectator.getWorld(), target.getWorld())) {
			Bukkit.getScheduler().runTaskLater(plugin, () -> spectator.teleport(target, PlayerTeleportEvent.TeleportCause.PLUGIN), 1);
			switchWorld = true;
		}

		if(switchWorld) {
			Bukkit.getScheduler().runTaskLater(plugin, () -> spectate(spectator, target, info), 10L);
			return;
		}
		spectate(spectator, target, info);
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

	public void unspectate(Player spectator, boolean oldLocation) {
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
			Spectator.Debug(String.format("Saved Location: %s", location));
			Spectator.Debug("Using current location of player");
		}

		if(!Objects.equals(spectator.getWorld(), location.getWorld()))
			info.restoreAttributes(true);

		final Location finalLocation = location;
		spectator.teleport(finalLocation, PlayerTeleportEvent.TeleportCause.PLUGIN);

		toggleTabList(spectator, false);
		info.restoreAttributes(true);
		info.restoreArmorstands();
	}

	public void toggleTabList(final Player spectator, final boolean hide) {
		if(!spectator.hasPermission(Permissions.UTILS_HIDE_IN_TAB)) {
			spectator.removeMetadata("vanished", this.plugin);
			return;
		}

		for(Player target : Bukkit.getOnlinePlayers()) {
			if(target.getUniqueId().equals(spectator.getUniqueId()) || target.hasPermission(Permissions.BYPASS_TABLIST))
				continue;

			if(Permissions.hasPermission(target, Permissions.BYPASS_TABLIST))
				continue;

			if(hide) {
				target.hidePlayer(this.plugin, spectator);
				spectator.setMetadata("vanished", new FixedMetadataValue(this.plugin, true));
				continue;
			}
			target.showPlayer(this.plugin, spectator);
			spectator.removeMetadata("vanished", this.plugin);
		}
	}

	public void dismount(Player spectator) {
		if(!isSpectator(spectator) || spectator.getGameMode() != GameMode.SPECTATOR)
			return;
		setRelation(spectator, null);
		spectator.setSpectatorTarget(null);
		Inventory.resetInventory(spectator);
	}

	public void restore() {
		for(Player player : getSpectators())
			unspectate(player, false);
	}

	public void setRelation(Player spectator, Player target) {
		if(!isSpectator(spectator))
			return;
		getSpectateInformation(spectator).setTarget(target);
	}


	public void startCycle(Player spectator, CycleTask cycle) {
		if(isCycling(spectator))
			return;

		spectate(spectator, null);
		SpectateInformation info = getSpectateInformation(spectator);
		info.setTarget(null);
		info.setState(SpectateState.CYCLING);
		this.spectateCycle.put(spectator.getUniqueId(), cycle);
		spectateInfo.replace(spectator.getUniqueId(), info);
		cycle.startTask(this.plugin);
	}

	public void stopCycle(Player spectator) {
		if(!isCycling(spectator))
			return;

		SpectateInformation info = getSpectateInformation(spectator);
		this.spectateCycle.get(spectator.getUniqueId()).stopTask();

		info.setState(SpectateState.SPECTATING);
		this.spectateCycle.remove(spectator.getUniqueId());
		spectateInfo.replace(spectator.getUniqueId(), info);
		dismount(spectator);
	}

	public void restartCycle(Player spectator) {
		if(!isPaused(spectator))
			return;

		SpectateInformation info = getSpectateInformation(spectator);
		info.setTarget(null);
		info.setState(SpectateState.CYCLING);
		spectateInfo.replace(spectator.getUniqueId(), info);
		this.spectateCycle.get(spectator.getUniqueId()).startTask(this.plugin);
	}

	public void pauseCycle(Player spectator) {
		if(!isCycling(spectator))
			return;

		SpectateInformation info = getSpectateInformation(spectator);
		CycleTask cycle = this.spectateCycle.get(spectator.getUniqueId()).stopTask();

		this.spectateCycle.put(spectator.getUniqueId(), cycle);
		info.setState(SpectateState.PAUSED);
		spectateInfo.replace(spectator.getUniqueId(), info);
		dismount(spectator);
	}

	public void teleportNextPlayer(Player spectator) {
		if(!isCycling(spectator))
			return;

		if(getCycleTask(spectator) != null) {
			this.spectateCycle.get(spectator.getUniqueId()).stopTask();
			this.spectateCycle.get(spectator.getUniqueId()).startTask(this.plugin);
		}
	}


	public boolean isCycling(Player spectator) {
		return getCyclingSpectators().contains(spectator);
	}

	public boolean isPaused(Player spectator) {
		return getPausedSpectators().contains(spectator);
	}


	public Set<Player> getSpectators() {
		return spectateInfo.values().stream().map(SpectateInformation::getSpectator).collect(Collectors.toSet());
	}

	public Set<Player> getSpectateablePlayers() {
		Set<Player> spectateable = new HashSet<>(Bukkit.getOnlinePlayers());
		spectateable.removeAll(getSpectators());
		spectateable.removeIf(player -> player.hasPermission(Permissions.BYPASS_SPECTATED));
		return spectateable;
	}

	public Set<Player> getSpectatorsOf(Player target) {
		return spectateInfo.values().stream().filter(info -> info.getTarget() != null && info.getTarget().equals(target)).
				map(SpectateInformation::getSpectator).collect(Collectors.toSet());
	}

	public Set<Player> getCyclingSpectators() {
		return spectateInfo.values().stream().filter(info -> info.getState() == SpectateState.CYCLING).
				map(SpectateInformation::getSpectator).collect(Collectors.toSet());
	}

	public Set<Player> getPausedSpectators() {
		return spectateInfo.values().stream().filter(info -> info.getState() == SpectateState.PAUSED).
				map(SpectateInformation::getSpectator).collect(Collectors.toSet());
	}


	public boolean isSpectator(Player spectator) {
		return spectateInfo.values().stream().anyMatch(info -> info.getSpectator().equals(spectator));
	}

	public SpectateInformation getSpectateInformation(Player spectator) {
		return spectateInfo.values().stream().filter(info -> info.getSpectator().equals(spectator)).findFirst().orElse(null);
	}

	public PlayerAttributes getPlayerAttributes(Player spectator) {
		if(getSpectateInformation(spectator) == null)
			return null;
		if(getSpectateInformation(spectator).getAttributes().isEmpty())
			return null;
		return getSpectateInformation(spectator).getAttributes().get(spectator.getWorld());
	}

	public boolean isNotSpectated(Player target) {
		return spectateInfo.values().stream().noneMatch(i -> i.getTarget() != null && i.getTarget().equals(target));
	}

	public boolean isSpectating(Player spectator, Player target) {
		return spectateInfo.values().stream().anyMatch(info -> info.getSpectator().equals(spectator) && info.getTarget() != null && info.getTarget().equals(target));
	}

	public Player getTargetOf(Player spectator) {
		SpectateInformation specInfo = spectateInfo.values().stream().filter(info -> info.getSpectator().equals(spectator)).findFirst().orElse(null);
		if(specInfo == null)
			return null;

		if(specInfo.getState() == SpectateState.CYCLING)
			return (Player) spectator.getSpectatorTarget();

		return specInfo.getTarget();
	}

	public CycleTask getCycleTask(Player spectator) {
		return spectateCycle.getOrDefault(spectator.getUniqueId(), null);
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