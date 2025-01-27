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

	private int interval;

	@Setter private int taskId;
	@Setter private Cycle cycle;
	@Setter private BossBar bossBar;
	@Setter private Integer showTargetTask;

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

	public void startForcePlayer(Spectator plugin, Player forcedTarget, int interval) {
		if(taskId != -1)
			stopTask();

		if(interval == this.interval) {
			setTaskId(Bukkit.getScheduler().runTaskTimer(plugin, () -> selectNextPlayer(plugin, forcedTarget), 0L, this.interval * 20L).getTaskId());
			displays.showCycleDisplay(cycle.getOwner());
			return;
		}

		int oldInterval = this.interval;
		this.interval = interval;

		setTaskId(Bukkit.getScheduler().runTaskTimer(plugin, () -> {
			if(this.interval == oldInterval) {
				Spectator.debug("Interval has been reset to the old value.");
				stopTask();
				startTask(plugin);
			}

			selectNextPlayer(plugin, forcedTarget);
			this.interval = oldInterval;
		}, 0L, this.interval * 20L).getTaskId());

		displays.showCycleDisplay(cycle.getOwner());
	}

	public void selectNextPlayer(Spectator plugin) {
		this.selectNextPlayer(plugin, null);
	}

	public void selectNextPlayer(Spectator plugin, Player forcedTarget) {
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

		Player next = cycle.getNextTarget(plugin, forcedTarget);
		Player last = cycle.getLastPlayer();

		if(next == null || next.isDead() || !next.isOnline())
			return;

		if(last == null || !last.equals(next))
			spectateAPI.getSpectateGeneral().notifyTarget(last, spectator, false);

		if(next.getWorld() != spectator.getWorld()) {
			TeleportListener.getWorldChange().put(spectator.getUniqueId(), next);
			getStateChange().put(spectator.getUniqueId(), SpectateState.CYCLING);
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