package de.cuzim1tigaaa.spectator.cycle;

import de.cuzim1tigaaa.spectator.Spectator;
import de.cuzim1tigaaa.spectator.files.*;
import lombok.Getter;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.boss.*;
import org.bukkit.entity.Player;

import java.util.*;

public class CycleHandler {

	private final Spectator plugin;

	@Getter private final Map<UUID, CycleTask> cycles;
	@Getter private final Map<UUID, CycleTask> paused;

	public CycleHandler(Spectator plugin) {
		this.plugin = plugin;
		this.cycles = new HashMap<>();
		this.paused = new HashMap<>();
	}

	public boolean isPlayerCycling(Player player) {
		return cycles.containsKey(player.getUniqueId());
	}

	/**
	 * start a new cycle
	 *
	 * @param player  The player who wants to cycle
	 * @param seconds The interval in seconds in which the player should switch the target
	 */
	public void startNewCycle(Player player, int seconds, boolean restart, boolean alphabetical) {
		if(cycles.containsKey(player.getUniqueId())) {
			player.sendMessage(Messages.getMessage(Paths.MESSAGES_COMMANDS_CYCLE_CYCLING));
			return;
		}
		hideCurrentTargetMessage(player);
		plugin.getSpectateManager().dismountTarget(player);
		paused.remove(player.getUniqueId());

		int ticks = seconds * 20, taskId = Bukkit.getScheduler().runTaskTimer(plugin, () -> nextPlayer(player), 0L, ticks).getTaskId();
		cycles.put(player.getUniqueId(), new CycleTask(seconds, new Cycle(player, null, alphabetical), taskId));

		player.sendMessage(Messages.getMessage(restart ? Paths.MESSAGES_COMMANDS_CYCLE_RESTART : Paths.MESSAGES_COMMANDS_CYCLE_START,
				"INTERVAL", seconds, "ORDER", alphabetical ? "Alphabetic" : "Random"));
	}

	/**
	 * stop a running cycle
	 *
	 * @param player The player who wants to stop cycling
	 */
	public void stopRunningCycle(Player player, boolean message) {
		if(!cycles.containsKey(player.getUniqueId())) {
			player.sendMessage(Messages.getMessage(Paths.MESSAGES_COMMANDS_CYCLE_NOT_CYCLING));
			return;
		}
		CycleTask task = cycles.get(player.getUniqueId());
		Bukkit.getScheduler().cancelTask(task.getTaskId());

		hideCurrentTargetMessage(player);
		paused.remove(player.getUniqueId());
		cycles.remove(player.getUniqueId());

		plugin.getSpectateManager().dismountTarget(player);
		if(message) player.sendMessage(Messages.getMessage(Paths.MESSAGES_COMMANDS_CYCLE_STOP));
	}

	/**
	 * pause a running cycle
	 *
	 * @param player The player who wants to pause his current cycle
	 */
	public void pauseRunningCycle(Player player) {
		if(!cycles.containsKey(player.getUniqueId())) {
			player.sendMessage(Messages.getMessage(Paths.MESSAGES_COMMANDS_CYCLE_NOT_CYCLING));
			return;
		}
		CycleTask task = cycles.get(player.getUniqueId());
		Bukkit.getScheduler().cancelTask(task.getTaskId());

		hideCurrentTargetMessage(player);
		paused.put(player.getUniqueId(), task);
		cycles.remove(player.getUniqueId());

		plugin.getSpectateManager().dismountTarget(player);
	}

	/**
	 * restart a cycle
	 * when a player paused his cycle, this cycle will restart
	 * else the current cycle will be stopped and starts again with the old interval
	 *
	 * @param player The player who wants to restart his cycle
	 */
	public void restartCycle(Player player) {
		final UUID playerId = player.getUniqueId();
		if(paused.containsKey(playerId)) {
			CycleTask pausedTask = paused.get(playerId);
			startNewCycle(player, pausedTask.getInterval(), true, pausedTask.getCycle().isAlphabetical());
			return;
		}
		if(!cycles.containsKey(playerId)) return;

		CycleTask current = cycles.get(playerId);

		stopRunningCycle(player, false);
		startNewCycle(player, current.getInterval(), true, current.getCycle().isAlphabetical());
	}

	public void nextPlayer(Player player) {
		final UUID uuid = player.getUniqueId();
		if(!cycles.containsKey(uuid)) return;
		Cycle cycle = cycles.get(uuid).getCycle();


		Player next = cycle.getNextPlayer();
		if(next == null || next.isDead() || !next.isOnline()) {
			if(!plugin.getSpectators().contains(player))
				plugin.getSpectateManager().spectate(player, null);
			return;
		}

		if(next.equals(player.getSpectatorTarget())) return;
		plugin.getSpectateManager().spectate(player, next);
		plugin.getSpectateManager().notifyTarget(cycle.getLastPlayer(), player, true);
		plugin.getSpectateManager().notifyTarget(next, player, true);
		showCurrentTargetMessage(player, next);
	}

	private void showCurrentTargetMessage(Player player, Player target) {
		CycleTask task = cycles.get(player.getUniqueId());
		switch(Config.getShowTargetMode().toLowerCase()) {
			case "bossbar" -> showBossBar(player, target);
			case "actionbar" -> {
				if(task.getShowTargetTask() != null)
					Bukkit.getScheduler().cancelTask(task.getShowTargetTask());

				task.setShowTargetTask(Bukkit.getScheduler().runTaskTimer(plugin, () ->
						player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
								target != null ? new TextComponent(Messages.getMessage(Paths.MESSAGES_CYCLING_CURRENT_TARGET, "TARGET", target.getDisplayName()))
										: new TextComponent(Messages.getMessage(Paths.MESSAGES_CYCLING_SEARCHING_TARGET))
						), 0L, 10L).getTaskId());
			}
			case "title" -> {
				if(task.getShowTargetTask() != null)
					Bukkit.getScheduler().cancelTask(task.getShowTargetTask());

				task.setShowTargetTask(Bukkit.getScheduler().runTaskTimer(plugin, () -> player.sendTitle(target != null ? Messages.getMessage(Paths.MESSAGES_CYCLING_CURRENT_TARGET, "TARGET", target.getDisplayName())
						: Messages.getMessage(Paths.MESSAGES_CYCLING_SEARCHING_TARGET), "", 0, 20, 0), 0L, 10L).getTaskId());
			}
			case "subtitle" -> {
				if(task.getShowTargetTask() != null)
					Bukkit.getScheduler().cancelTask(task.getShowTargetTask());

				task.setShowTargetTask(Bukkit.getScheduler().runTaskTimer(plugin, () -> player.sendTitle("", target != null ? Messages.getMessage(Paths.MESSAGES_CYCLING_CURRENT_TARGET, "TARGET", target.getDisplayName())
						: Messages.getMessage(Paths.MESSAGES_CYCLING_SEARCHING_TARGET), 0, 20, 0), 0L, 10L).getTaskId());
			}
			default -> hideCurrentTargetMessage(player);
		}
	}
	private void hideCurrentTargetMessage(Player player) {
		CycleTask task = cycles.getOrDefault(player.getUniqueId(), null);
		if(task != null && task.getBossBar() != null)
			task.getBossBar().removeAll();

		if(task != null && task.getShowTargetTask() != null)
			Bukkit.getScheduler().cancelTask(task.getShowTargetTask());
	}

	private void showBossBar(Player player, Player target) {
		CycleTask task = cycles.get(player.getUniqueId());
		BossBar bossBar = task.getBossBar() == null ? Bukkit.createBossBar("", BarColor.WHITE, BarStyle.SOLID) : task.getBossBar();

		bossBar.setTitle(target == null ? Messages.getMessage(Paths.MESSAGES_GENERAL_BOSS_BAR_WAITING) :
				Messages.getMessage(Paths.MESSAGES_CYCLING_CURRENT_TARGET, "TARGET", target.getDisplayName()));
		bossBar.setColor(target == null ? BarColor.RED : BarColor.BLUE);

		bossBar.setVisible(true);
		bossBar.addPlayer(player);
		task.setBossBar(bossBar);
	}
}