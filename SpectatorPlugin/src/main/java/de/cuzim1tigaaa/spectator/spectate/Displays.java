package de.cuzim1tigaaa.spectator.spectate;

import de.cuzim1tigaaa.spectator.SpectateAPI;
import de.cuzim1tigaaa.spectator.Spectator;
import de.cuzim1tigaaa.spectator.cycle.Cycle;
import de.cuzim1tigaaa.spectator.cycle.CycleTask;
import de.cuzim1tigaaa.spectator.files.*;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.boss.*;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class Displays {

	private final Spectator plugin;
	private final SpectateAPI spectateAPI;

	public Displays(Spectator plugin) {
		this.plugin = plugin;
		this.spectateAPI = plugin.getSpectateAPI();
	}

	public void showCycleDisplay(Player spectator) {
		if(!spectateAPI.isCyclingSpectator(spectator))
			return;

		CycleTask task = spectateAPI.getCycleTask(spectator);
		if(task == null)
			return;

		Cycle cycle = task.getCycle();
		String mode = Config.getString(Paths.CONFIG_CYCLE_SHOW_TARGET).toUpperCase();

		if(mode.equalsIgnoreCase("ACTIONBAR")) {
			if(task.getShowTargetTask() != null)
				Bukkit.getScheduler().cancelTask(task.getShowTargetTask());

			task.setShowTargetTask(Bukkit.getScheduler().runTaskTimer(plugin, () -> {
				Player target = cycle.getLastPlayer();
				spectator.spigot().sendMessage(ChatMessageType.ACTION_BAR,
						target != null ? new TextComponent(Messages.getMessage(target, Paths.MESSAGES_CYCLING_CURRENT_TARGET, "TARGET", target.getName()))
								: new TextComponent(Messages.getMessage(spectator, Paths.MESSAGES_CYCLING_SEARCHING_TARGET)));
			}, 0L, 10L).getTaskId());
			return;
		}

		if(mode.contains("TITLE")) {
			if(task.getShowTargetTask() != null)
				Bukkit.getScheduler().cancelTask(task.getShowTargetTask());

			final int duration = (task.getInterval() - 1) * 20;

			task.setShowTargetTask(Bukkit.getScheduler().runTaskTimer(plugin, () -> {
				Player player = cycle.getLastPlayer();
				String title = player != null ? Messages.getMessage(player, Paths.MESSAGES_CYCLING_CURRENT_TARGET, "TARGET", player.getName())
						: Messages.getMessage(spectator, Paths.MESSAGES_CYCLING_SEARCHING_TARGET);

				if(mode.equals("SUBTITLE")) {
					spectator.sendTitle("", title, 10, duration, 10);
					return;
				}
				spectator.sendTitle(title, "", 10, duration, 10);
			}, 0L, duration).getTaskId());
			return;
		}

		if(mode.equalsIgnoreCase("BOSSBAR")) {
			if(task.getShowTargetTask() != null)
				Bukkit.getScheduler().cancelTask(task.getShowTargetTask());

			BossBar bar = task.getBossBar() == null ? Bukkit.createBossBar("", BarColor.WHITE, BarStyle.SOLID) : task.getBossBar();
			bar.setVisible(true);
			bar.addPlayer(spectator);
			task.setBossBar(bar);

			Player target = cycle.getLastPlayer();
			if(target == null) {
				bar.setTitle(Messages.getMessage(spectator, Paths.MESSAGES_CYCLING_SEARCHING_TARGET));
				bar.setColor(BarColor.RED);
			}else {
				bar.setTitle(Messages.getMessage(target, Paths.MESSAGES_CYCLING_CURRENT_TARGET, "TARGET", target.getName()));
				bar.setColor(BarColor.BLUE);
			}

			task.setShowTargetTask(new BukkitRunnable() {
				final int inter = (task.getInterval() - 1);
				double counter = inter;

				@Override
				public void run() {
					final Cycle cycle = task.getCycle();
					Player target = cycle.getLastPlayer();
					if(counter == 0) {
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

					if(bar.getColor() == BarColor.RED && target != null) {
						bar.setTitle(Messages.getMessage(target, Paths.MESSAGES_CYCLING_CURRENT_TARGET, "TARGET", target.getName()));
						bar.setColor(BarColor.BLUE);
					}

					if(cycle.getOwner().getGameMode() == GameMode.SPECTATOR)
						cycle.getOwner().setSpectatorTarget(cycle.getLastPlayer());
					counter--;
					bar.setProgress(counter / inter);
				}
			}.runTaskTimer(plugin, 20L, 20L).getTaskId());
		}
	}
}