package de.cuzim1tigaaa.spectator.spectate;

import de.cuzim1tigaaa.spectator.Spectator;
import de.cuzim1tigaaa.spectator.cycle.CycleTask;
import de.cuzim1tigaaa.spectator.files.*;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class Displays {

	private final Spectator plugin;
	private final SpectateUtils spectateUtils;

	public Displays(Spectator plugin) {
		this.plugin = plugin;
		this.spectateUtils = plugin.getSpectateUtils();
	}

	public void showCycleDisplay(Player spectator) {
		if(!spectateUtils.isCycling(spectator))
			return;

		CycleTask task = spectateUtils.getCycleTask(spectator);
		if(task == null)
			return;

		switch(Config.getShowTargetMode().toUpperCase()) {
			case "ACTIONBAR" -> {
				if(task.getShowTargetTask() != null)
					Bukkit.getScheduler().cancelTask(task.getShowTargetTask());

				task.setShowTargetTask(Bukkit.getScheduler().runTaskTimer(plugin, () -> {
					Player target = task.getCycle().getLastPlayer();
					spectator.spigot().sendMessage(ChatMessageType.ACTION_BAR,
							target != null ? new TextComponent(Messages.getMessage(Paths.MESSAGES_CYCLING_CURRENT_TARGET, "TARGET", target.getDisplayName()))
									: new TextComponent(Messages.getMessage(Paths.MESSAGES_CYCLING_SEARCHING_TARGET)));
				}, 0L, 10L).getTaskId());
			}
			case "SUBTITLE" -> {
				if(task.getShowTargetTask() != null)
					Bukkit.getScheduler().cancelTask(task.getShowTargetTask());

				task.setShowTargetTask(Bukkit.getScheduler().runTaskTimer(plugin, () -> {
					Player target = task.getCycle().getLastPlayer();
					spectator.sendTitle("", target != null ? Messages.getMessage(Paths.MESSAGES_CYCLING_CURRENT_TARGET, "TARGET", target.getDisplayName())
							: Messages.getMessage(Paths.MESSAGES_CYCLING_SEARCHING_TARGET), 0, 20, 0);
				}, 0L, 10L).getTaskId());
			}
			case "TITLE" -> {
				if(task.getShowTargetTask() != null)
					Bukkit.getScheduler().cancelTask(task.getShowTargetTask());

				task.setShowTargetTask(Bukkit.getScheduler().runTaskTimer(plugin, () -> {
					Player target = task.getCycle().getLastPlayer();
					spectator.sendTitle(target != null ? Messages.getMessage(Paths.MESSAGES_CYCLING_CURRENT_TARGET, "TARGET", target.getDisplayName())
							: Messages.getMessage(Paths.MESSAGES_CYCLING_SEARCHING_TARGET), "", 0, 20, 0);
				}, 0L, 10L).getTaskId());
			}
			default -> {
				if(task.getShowTargetTask() != null)
					Bukkit.getScheduler().cancelTask(task.getShowTargetTask());
			}
		}
	}
}