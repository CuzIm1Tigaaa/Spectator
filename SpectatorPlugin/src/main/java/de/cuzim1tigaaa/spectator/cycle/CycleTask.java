package de.cuzim1tigaaa.spectator.cycle;

import de.cuzim1tigaaa.spectator.Spectator;
import de.cuzim1tigaaa.spectator.files.*;
import de.cuzim1tigaaa.spectator.listener.TeleportListener;
import de.cuzim1tigaaa.spectator.spectate.SpectateState;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.boss.*;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

@Getter
public class CycleTask {

	@Getter private static final Map<UUID, SpectateState> stateChange = new HashMap<>();

	private final int interval;

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

		Player spectator = cycle.getOwner();
		if(plugin.getSpectateUtils().getSpectateablePlayers().isEmpty()) {
			if(!Config.getBoolean(Paths.CONFIG_CYCLE_PAUSE_NO_PLAYERS)) {
				plugin.getSpectateUtils().StopCycle(spectator);
				spectator.sendMessage(Messages.getMessage(spectator, Paths.MESSAGES_COMMANDS_CYCLE_STOP));
				return;
			}
			plugin.getSpectateUtils().PauseCycle(spectator);
			spectator.sendMessage(Messages.getMessage(spectator, Paths.MESSAGES_COMMANDS_CYCLE_PAUSE));
			return;
		}

		if(!Config.getShowTargetMode().equalsIgnoreCase("BOSSBAR")) {
			plugin.getDisplays().showCycleDisplay(cycle.getOwner());
			setTaskId(Bukkit.getScheduler().runTaskTimer(plugin, () -> selectNextPlayer(plugin), 0, interval * 20L).getTaskId());
			return;
		}

		BossBar bar = getBossBar() == null ? Bukkit.createBossBar("", BarColor.WHITE, BarStyle.SOLID) : getBossBar();
		bar.setVisible(true);
		setBossBar(bar);

		selectNextPlayer(plugin);
		Player target = getCycle().getLastPlayer();
		if(target == null) {
			bar.setTitle(Messages.getMessage(spectator, Paths.MESSAGES_CYCLING_SEARCHING_TARGET));
			bar.setColor(BarColor.RED);
		}else {
			bar.setTitle(Messages.getMessage(target, Paths.MESSAGES_CYCLING_CURRENT_TARGET, "TARGET", target.getName()));
			bar.setColor(BarColor.BLUE);
		}
		bar.addPlayer(cycle.getOwner());

		setTaskId(new BukkitRunnable() {
			final int inter = (interval - 1);
			double counter = inter;

			@Override
			public void run() {
				if(counter == 0) {
					selectNextPlayer(plugin);

					Player target = getCycle().getLastPlayer();
					if(target == null) {
						bar.setTitle(Messages.getMessage(spectator, Paths.MESSAGES_CYCLING_SEARCHING_TARGET));
						bar.setColor(BarColor.RED);
					}else {
						bar.setTitle(Messages.getMessage(target, Paths.MESSAGES_CYCLING_CURRENT_TARGET, "TARGET", target.getName()));
						bar.setColor(BarColor.BLUE);
					}
					bar.removeAll();
					bar.addPlayer(cycle.getOwner());

					counter = inter;
					bar.setProgress(counter / inter);
					return;
				}
				if(cycle.getOwner().getGameMode() == GameMode.SPECTATOR)
					cycle.getOwner().setSpectatorTarget(cycle.getLastPlayer());
				counter--;
				bar.setProgress(counter / inter);
			}
		}.runTaskTimer(plugin, 20L, 20L).getTaskId());
	}

	public void selectNextPlayer(Spectator plugin) {
		Player spectator = cycle.getOwner();

		if(plugin.getSpectateUtils().getSpectateablePlayers().isEmpty()) {
			if(!Config.getBoolean(Paths.CONFIG_CYCLE_PAUSE_NO_PLAYERS)) {
				plugin.getSpectateUtils().StopCycle(spectator);
				spectator.sendMessage(Messages.getMessage(spectator, Paths.MESSAGES_COMMANDS_CYCLE_STOP));
				return;
			}
			plugin.getSpectateUtils().PauseCycle(spectator);
			spectator.sendMessage(Messages.getMessage(spectator, Paths.MESSAGES_COMMANDS_CYCLE_PAUSE));
			return;
		}

		Player next = cycle.getNextTarget(plugin);
		Player last = cycle.getLastPlayer();
		if(next == null || next.isDead() || !next.isOnline())
			return;

		if(last == null || !last.equals(next))
			plugin.getSpectateUtils().notifyTarget(last, spectator, false);

		if(next.getWorld() != spectator.getWorld()) {
			plugin.getSpectateUtils().Dismount(spectator);
			TeleportListener.getWorldChange().put(spectator.getUniqueId(), next);

			PlayerTeleportEvent event = new PlayerTeleportEvent(spectator, spectator.getLocation(), next.getLocation(), PlayerTeleportEvent.TeleportCause.PLUGIN);
			Bukkit.getPluginManager().callEvent(event);

			getStateChange().put(spectator.getUniqueId(), SpectateState.CYCLING);
			return;
		}
		plugin.getSpectateUtils().Spectate(spectator, next);
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