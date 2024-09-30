package de.cuzim1tigaaa.spectator.cycle;

import de.cuzim1tigaaa.spectator.SpectateAPI;
import de.cuzim1tigaaa.spectator.Spectator;
import de.cuzim1tigaaa.spectator.files.*;
import de.cuzim1tigaaa.spectator.listener.TeleportListener;
import de.cuzim1tigaaa.spectator.spectate.Displays;
import de.cuzim1tigaaa.spectator.spectate.SpectateState;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;

import java.util.*;

@Getter
public class CycleTask {

	@Getter
	private static final Map<UUID, SpectateState> stateChange = new HashMap<>();
	private static final Displays displays = new Displays(Spectator.getPlugin());

	private final int interval;

	@Setter
	private int taskId;
	@Setter
	private Cycle cycle;
	@Setter
	private BossBar bossBar;
	@Setter
	private Integer showTargetTask;

	public CycleTask(int interval, Cycle cycle) {
		this.interval = interval;
		this.cycle = cycle;
		this.showTargetTask = null;
		this.taskId = -1;
	}

	public void startTask(Spectator plugin) {
		if(taskId != -1)
			return;

		setTaskId(Bukkit.getScheduler().runTaskTimer(plugin, () -> selectNextPlayer(plugin), 0L, interval * 20L).getTaskId());
		displays.showCycleDisplay(cycle.getOwner());
	}

	public void selectNextPlayer(Spectator plugin) {
		final SpectateAPI spectateAPI = plugin.getSpectateAPI();
		Player spectator = cycle.getOwner();

		if(spectateAPI.getSpectateablePlayers().isEmpty()) {
			if(!Config.getBoolean(Paths.CONFIG_CYCLE_PAUSE_NO_PLAYERS)) {
				spectateAPI.getSpectateCycle().stopCycle(spectator);
				Messages.sendMessage(spectator, Paths.MESSAGES_COMMANDS_CYCLE_STOP);
				return;
			}
			spectateAPI.getSpectateCycle().pauseCycle(spectator);
			Messages.sendMessage(spectator, Paths.MESSAGES_COMMANDS_CYCLE_PAUSE);
			return;
		}

		Player next = cycle.getNextTarget(plugin);
		Player last = cycle.getLastPlayer();

		if(next == null || next.isDead() || !next.isOnline())
			return;

		if(last == null || !last.equals(next))
			spectateAPI.getSpectateGeneral().notifyTarget(last, spectator, false);

		if(next.getWorld() != spectator.getWorld()) {
			plugin.getSpectateAPI().dismount(spectator);
			TeleportListener.getWorldChange().put(spectator.getUniqueId(), next);
			getStateChange().put(spectator.getUniqueId(), SpectateState.CYCLING);
			return;
		}
		spectateAPI.getSpectateGeneral().spectate(spectator, next);
	}

	public CycleTask stopTask() {
		if(taskId == -1)
			return this;
		Bukkit.getScheduler().cancelTask(taskId);

		if(getBossBar() != null)
			getBossBar().removeAll();

		if(getShowTargetTask() != null) {
			Bukkit.getScheduler().cancelTask(getShowTargetTask());
			setShowTargetTask(null);
		}

		this.taskId = -1;
		return this;
	}
}