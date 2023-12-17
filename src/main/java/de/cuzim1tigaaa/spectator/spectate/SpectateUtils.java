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

	private final Map<Long, Player> ignoreGameModeChange;
	private final Map<UUID, SpectateInformation> spectateInfo;

	public SpectateUtils(Spectator plugin) {
		this.plugin = plugin;

		this.ignoreGameModeChange = new HashMap<>();
		this.spectateInfo = new HashMap<>();

		this.run();
	}

	private void run() {
		Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
			for(Player spectator : getSpectators()) {
				SpectateInformation info = getSpectateInformation(spectator);
				if(info == null || info.getTarget() == null)
					continue;

				final Player target = info.getTarget();
				if(!target.isOnline())
					continue;

				Inventory.updateInventory(spectator, target);

				if(spectator.getGameMode() != GameMode.SPECTATOR) {
					Unspectate(spectator, true);
					continue;
				}
				spectator.setSpectatorTarget(null);

				Location spLoc = spectator.getLocation(), taLoc = target.getLocation();

				if(!Objects.equals(spLoc.getWorld(), taLoc.getWorld()) || spLoc.distanceSquared(taLoc) > 3)
					spectator.teleport(target, PlayerTeleportEvent.TeleportCause.PLUGIN);

				if(spectator.getGameMode() != GameMode.SPECTATOR) {
					Unspectate(spectator, true);
					continue;
				}

				spectator.setSpectatorTarget(target);
			}
		}, 0, 10);
	}


	public void Spectate(Player spectator, Player target) {
		getSpectatorsOf(spectator).forEach(this::Dismount);

		SpectateInformation info;
		if(isSpectator(spectator)) {
			info = getSpectateInformation(spectator);
			info.setTarget(target);
		}else
			info = new SpectateInformation(spectator, target);

		if(info.getState() == SpectateState.PAUSED)
			info.setState(SpectateState.SPECTATING);

		if(target != null && !Objects.equals(spectator.getWorld(), target.getWorld()))
			spectator.teleport(target);

		if(!info.getAttributes().containsKey(spectator.getWorld()))
			info.saveAttributes();

		changeGameMode(spectator, GameMode.SPECTATOR);

		if(target != null) {
			spectator.teleport(target);
			Bukkit.getScheduler().runTaskLater(plugin, () -> spectator.setSpectatorTarget(target), 5L);
		}

		this.spectateInfo.put(spectator.getUniqueId(), info);
		notifyTarget(target, spectator, true);

		ToggleTabList(spectator, true);
		Inventory.getInventory(spectator, target);
	}

	public void Unspectate(Player spectator, boolean oldLocation) {
		if(!isSpectator(spectator))
			return;

		if(isCycling(spectator))
			StopCycle(spectator);

		SpectateInformation info = getSpectateInformation(spectator);
		Location location = info.getLocation();

		Inventory.resetInventory(spectator);
		notifyTarget(getSpectateInformation(spectator).getTarget(), spectator, false);
		this.spectateInfo.remove(spectator.getUniqueId());

		if(location == null || !oldLocation || !Config.getBoolean(Paths.CONFIG_SAVE_PLAYERS_LOCATION))
			location = spectator.getLocation();

		plugin.Debug(String.format("Player %-16s unspectated!", spectator.getName()));
		info.getAttributes().forEach((w, p) -> {
			plugin.Debug(String.format("\tInventory saved for World [%s]:", w.getName()));
		plugin.Debug("\t - " + Arrays.stream(p.getInventory()).map(i -> i != null ? i.getType().name() : Material.AIR.name()).
				filter(i -> !i.equals(Material.AIR.name())).collect(Collectors.joining(", ")));
		});

		if(!Objects.equals(spectator.getWorld(), info.getLocation().getWorld()))
			info.restoreAttributes();

		spectator.teleport(location);

		ToggleTabList(spectator, false);
		info.restoreAttributes();
	}

	public void ToggleTabList(final Player spectator, final boolean hide) {
		if(!spectator.hasPermission(Permissions.UTILS_HIDE_IN_TAB)) {
			spectator.removeMetadata("vanished", this.plugin);
			return;
		}

		for(Player target : Bukkit.getOnlinePlayers()) {
			if(target.getUniqueId().equals(spectator.getUniqueId()) || target.hasPermission(Permissions.BYPASS_TABLIST))
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

	public void Dismount(Player spectator) {
		if(!isSpectator(spectator) || spectator.getGameMode() != GameMode.SPECTATOR)
			return;
		setRelation(spectator, null);
		spectator.setSpectatorTarget(null);
		Inventory.resetInventory(spectator);
	}

	public void Restore() {
		for(Player player : getSpectators())
			Unspectate(player, false);
	}

	public void setRelation(Player spectator, Player target) {
		if(!isSpectator(spectator))
			return;
		getSpectateInformation(spectator).setTarget(target);
	}


	public void StartCycle(Player spectator, CycleTask cycle) {
		if(isCycling(spectator))
			return;

		Spectate(spectator, null);
		SpectateInformation info = getSpectateInformation(spectator);
		info.setTarget(null);
		info.setState(SpectateState.CYCLING);
		info.setCycleTask(cycle);
		spectateInfo.replace(spectator.getUniqueId(), info);
		cycle.startTask();
	}

	public void StopCycle(Player spectator) {
		if(!isCycling(spectator))
			return;

		SpectateInformation info = getSpectateInformation(spectator);
		info.getCycleTask().stopTask();

		info.setState(SpectateState.SPECTATING);
		info.setCycleTask(null);
		spectateInfo.replace(spectator.getUniqueId(), info);
		Dismount(spectator);
	}

	public void RestartCycle(Player spectator) {
		if(!isPaused(spectator))
			return;

		SpectateInformation info = getSpectateInformation(spectator);
		CycleTask task = info.getCycleTask();
		spectator.sendMessage("Cycle restart.");
		spectateInfo.remove(spectator.getUniqueId());
		StartCycle(spectator, task);
	}

	public void PauseCycle(Player spectator) {
		if(!isCycling(spectator))
			return;

		SpectateInformation info = getSpectateInformation(spectator);
		CycleTask cycle = info.getCycleTask().stopTask();

		info.setCycleTask(cycle);
		info.setState(SpectateState.PAUSED);
		spectateInfo.replace(spectator.getUniqueId(), info);
		Dismount(spectator);
	}

	public void teleportNextPlayer(Player spectator) {
		if(!isCycling(spectator))
			return;

		SpectateInformation info = getSpectateInformation(spectator);
		if(info.getCycleTask() != null) {
			info.getCycleTask().stopTask();
			info.getCycleTask().startTask();
//			info.getCycleTask().selectNextPlayer();
		}
	}


	public boolean isCycling(Player spectator) {
		return getCyclingSpectators().contains(spectator);
	}

	public boolean isPaused(Player spectator) {
		return getPausedSpectators().contains(spectator);
	}



	public void changeGameMode(Player spectator, GameMode gamemode) {
		long l = System.currentTimeMillis();
		getIgnoreGameModeChange().put(l, spectator);
		spectator.setGameMode(gamemode);
		getIgnoreGameModeChange().remove(l);
	}


	public Set<Player> getSpectators() {
		return spectateInfo.values().stream().map(SpectateInformation::getSpectator).collect(Collectors.toSet());
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
		SpectateInformation specInfo = spectateInfo.values().stream().filter(info -> info.getSpectator().equals(spectator)).findFirst().orElse(null);
		if(specInfo == null)
			return null;
		return specInfo.getCycleTask();
	}

	public void notifyTarget(Player target, Player spectator, boolean spectate) {
		if(target == null)
			return;
        if(spectator.hasPermission(Permissions.BYPASS_NOTIFY))
			return;

        String message = Messages.getMessage(spectate ? Paths.MESSAGES_GENERAL_NOTIFY_SPECTATE :
		        Paths.MESSAGES_GENERAL_NOTIFY_UNSPECTATE, "TARGET", spectator.getDisplayName());

        switch(Config.getNotifyTargetMode().toLowerCase()) {
            case "chat" -> target.spigot().sendMessage(ChatMessageType.CHAT, new TextComponent(message));
            case "actionbar" -> target.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(message));
            case "title" -> target.sendTitle(message, "", 5, 50, 5);
            case "subtitle" -> target.sendTitle("", message, 5, 50, 5);
        }
    }

}