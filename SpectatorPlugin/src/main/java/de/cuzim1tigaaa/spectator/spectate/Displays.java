package de.cuzim1tigaaa.spectator.spectate;

import de.cuzim1tigaaa.spectator.SpectateAPI;
import de.cuzim1tigaaa.spectator.Spectator;
import de.cuzim1tigaaa.spectator.cycle.CycleTask;
import de.cuzim1tigaaa.spectator.files.*;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

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

		switch(Config.getString(Paths.CONFIG_CYCLE_SHOW_TARGET).toUpperCase()) {
			case "ACTIONBAR" -> {
				if(task.getShowTargetTask() != null)
					Bukkit.getScheduler().cancelTask(task.getShowTargetTask());

				task.setShowTargetTask(Bukkit.getScheduler().runTaskTimer(plugin, () -> {
					Player target = task.getCycle().getLastPlayer();
					spectator.spigot().sendMessage(ChatMessageType.ACTION_BAR,
							target != null ? new TextComponent(Messages.getMessage(target, Paths.MESSAGES_CYCLING_CURRENT_TARGET, "TARGET", target.getName()))
									: new TextComponent(Messages.getMessage(spectator, Paths.MESSAGES_CYCLING_SEARCHING_TARGET)));
				}, 0L, 10L).getTaskId());
			}
			case "SUBTITLE" -> {
				if(task.getShowTargetTask() != null)
					Bukkit.getScheduler().cancelTask(task.getShowTargetTask());

				task.setShowTargetTask(Bukkit.getScheduler().runTaskTimer(plugin, () -> {
					Player target = task.getCycle().getLastPlayer();
					spectator.sendTitle("", target != null ? Messages.getMessage(target, Paths.MESSAGES_CYCLING_CURRENT_TARGET, "TARGET", target.getName())
							: Messages.getMessage(spectator, Paths.MESSAGES_CYCLING_SEARCHING_TARGET), 0, 20, 0);
				}, 0L, 10L).getTaskId());
			}
			case "TITLE" -> {
				if(task.getShowTargetTask() != null)
					Bukkit.getScheduler().cancelTask(task.getShowTargetTask());

				task.setShowTargetTask(Bukkit.getScheduler().runTaskTimer(plugin, () -> {
					Player target = task.getCycle().getLastPlayer();
					spectator.sendTitle(target != null ? Messages.getMessage(target, Paths.MESSAGES_CYCLING_CURRENT_TARGET, "TARGET", target.getName())
							: Messages.getMessage(spectator, Paths.MESSAGES_CYCLING_SEARCHING_TARGET), "", 0, 20, 0);
				}, 0L, 10L).getTaskId());
			}
			default -> {
				if(task.getShowTargetTask() != null)
					Bukkit.getScheduler().cancelTask(task.getShowTargetTask());
			}
		}
	}
}