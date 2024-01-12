package de.cuzim1tigaaa.spectator.cycle;

import de.cuzim1tigaaa.spectator.Spectator;
import de.cuzim1tigaaa.spectator.files.*;
import de.cuzim1tigaaa.spectator.spectate.Displays;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.boss.*;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

@Getter
public class CycleTask {

	private static final Spectator plugin = Spectator.getPlugin(Spectator.class);
	private static final Displays displays = new Displays(plugin);

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

	public void startTask() {
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

		if(Config.getShowTargetMode().equalsIgnoreCase("BOSSBAR")) {
			BossBar bar = getBossBar() == null ? Bukkit.createBossBar("", BarColor.WHITE, BarStyle.SOLID) : getBossBar();
			bar.setVisible(true);
			setBossBar(bar);

			selectNextPlayer();
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
				final int inter = (interval -1);
				double counter = inter;

				@Override
				public void run() {
					if(counter == 0) {
						selectNextPlayer();

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
					counter--;
					bar.setProgress(counter / inter);
				}
			}.runTaskTimer(plugin, 20L, 20L).getTaskId());
		}else {
			displays.showCycleDisplay(cycle.getOwner());
			setTaskId(Bukkit.getScheduler().runTaskTimer(plugin, this::selectNextPlayer, 0, interval * 20L).getTaskId());
		}
	}

	public void selectNextPlayer() {
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

		Player next = cycle.getNextTarget();
		Player last = cycle.getLastPlayer();
		if(next == null || next.isDead() || !next.isOnline())
			return;

		if(last == null || !last.equals(next))
			plugin.getSpectateUtils().notifyTarget(last, spectator, false);
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