package de.cuzim1tigaaa.spectator.listener;

import de.cuzim1tigaaa.spectator.Spectator;
import de.cuzim1tigaaa.spectator.cycle.CycleTask;
import de.cuzim1tigaaa.spectator.files.*;
import de.cuzim1tigaaa.spectator.spectate.SpectateUtils;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.plugin.PluginManager;

import static de.cuzim1tigaaa.spectator.files.Permissions.*;

public class SpectatorListener implements Listener {

	private final Spectator plugin;
	private final SpectateUtils spectateUtils;

	public SpectatorListener(Spectator plugin) {
		this.plugin = plugin;
		this.spectateUtils = plugin.getSpectateUtils();

		PluginManager pluginManager = plugin.getServer().getPluginManager();
		pluginManager.registerEvents(new ContainerListener(plugin), plugin);
		pluginManager.registerEvents(new TeleportListener(plugin), plugin);
		pluginManager.registerEvents(new PaperListener(plugin), plugin);
		pluginManager.registerEvents(this, plugin);
	}

	/**
	 * Notify admins, if the plugin has a newer version
	 * Hide spectating players in the tabList if enabled / permissions allow it
	 * Restart a paused cycle, when no players were online before
	 */
	@EventHandler
	public void playerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();

		if(Config.getBoolean(Paths.CONFIG_NOTIFY_UPDATE) && hasPermission(player, NOTIFY_UPDATE_ON_JOIN)) {
			if(plugin.getUpdateChecker().isUpdate())
				this.sendUpdateNotification(player);
		}

		if(Config.getBoolean(Paths.CONFIG_HIDE_PLAYERS_TAB) && !hasPermission(player, BYPASS_TABLIST))
			spectateUtils.getSpectators().forEach(spectator -> player.hidePlayer(plugin, spectator));

		if(!Config.getBoolean(Paths.CONFIG_CYCLE_PAUSE_NO_PLAYERS))
			return;

		for(Player spectator : spectateUtils.getPausedSpectators()) {
			spectateUtils.RestartCycle(spectator);
			CycleTask task = spectateUtils.getCycleTask(spectator);
			spectator.sendMessage(Messages.getMessage(Paths.MESSAGES_COMMANDS_CYCLE_RESTART,
					"INTERVAL", task.getInterval(), "ORDER", task.getCycle().isAlphabetical() ? "Alphabetic" : "Random"));
		}
	}

	/**
	 * Send a message to the player to notify him that an update is available
	 */
	private void sendUpdateNotification(Player player) {
		player.sendMessage(ChatColor.translateAlternateColorCodes('&', String.format("""
			&cSpectator &8| &6&lAn Update is available! &8[&ev%s&8]
			 &8&l» &ehttps://www.spigotmc.org/resources/spectator.93051/
			""", this.plugin.getUpdateChecker().getVersion().replace("v", ""))));
	}


	/**
	 * unspectate, when quitting while spectating
	 * dismount target, when target quits
	 * Pause a running cycle, when no more players are online
	 */
	@EventHandler
	public void playerQuit(PlayerQuitEvent event) {
		Player player = event.getPlayer();

		if(spectateUtils.isSpectator(player)) {
			spectateUtils.Unspectate(player, true);
			return;
		}

		spectateUtils.getSpectators().forEach(spectator -> player.showPlayer(plugin, spectator));

		for(Player spectator : spectateUtils.getSpectatorsOf(player)) {
			spectateUtils.Dismount(spectator);

			if(!spectateUtils.isCycling(spectator))
				continue;

			if((Bukkit.getOnlinePlayers().size() - spectateUtils.getSpectators().size()) - 1 > 0)
				continue;

			if(!Config.getBoolean(Paths.CONFIG_CYCLE_PAUSE_NO_PLAYERS)) {
				spectateUtils.StopCycle(spectator);
				spectator.sendMessage(Messages.getMessage(Paths.MESSAGES_COMMANDS_CYCLE_STOP));
			}else {
				spectateUtils.PauseCycle(spectator);
				spectator.sendMessage(Messages.getMessage(Paths.MESSAGES_COMMANDS_CYCLE_PAUSE));
			}
		}
	}

	/**
	 * Normally a player can "leave" a spectator target with sneaking
	 * This is disabled during spectate cycle (or if the player has the "CycleOnly" permission)
	 */
	@EventHandler
	public void dismountTarget(PlayerToggleSneakEvent event) {
		if(!event.isSneaking())
			return;

		Player player = event.getPlayer();

		if(!spectateUtils.isSpectator(player))
			return;

		if(spectateUtils.isCycling(player)) {
			player.sendMessage(Messages.getMessage(Paths.MESSAGES_GENERAL_DISMOUNT));
			event.setCancelled(true);
			return;
		}

		spectateUtils.Dismount(player);
	}

	/**
	 * Disable changing gameMode while in plugins spectator mode
	 */
	@EventHandler
	public void spectatorChangesGameMode(PlayerGameModeChangeEvent event) {
		Player player = event.getPlayer();
		if(!spectateUtils.isSpectator(player))
			return;

		if(!spectateUtils.getIgnoreGameModeChange().containsValue(player)) {
			player.sendMessage(Messages.getMessage(Paths.MESSAGES_GENERAL_GAMEMODE_CHANGE));
			event.setCancelled(true);
		}
	}

	/**
	 * Do not allow to kick cycling players if configured so
	 */
	@EventHandler(priority = EventPriority.HIGHEST)
	public void kickCyclingPlayer(PlayerKickEvent event) {
		if(spectateUtils.isCycling(event.getPlayer())) {
			if(!Config.getBoolean(Paths.CONFIG_KICK_WHILE_CYCLING))
				event.setCancelled(true);

			else
				spectateUtils.StopCycle(event.getPlayer());
		}
	}

	/**
	 * Dismount from target, if the target died
	 */
	@EventHandler
	public void targetDeathEvent(PlayerDeathEvent event) {
		Player player = event.getEntity();
		if(spectateUtils.isSpectator(player)) {
			spectateUtils.Unspectate(player, false);
			return;
		}

		for(Player spectator : spectateUtils.getSpectatorsOf(player)) {
			spectateUtils.Dismount(spectator);

			if(spectateUtils.isCycling(spectator))
				spectateUtils.teleportNextPlayer(spectator);
		}
	}
}