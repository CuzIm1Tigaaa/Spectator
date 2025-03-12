package de.cuzim1tigaaa.spectator.listener;

import de.cuzim1tigaaa.spectator.SpectateAPI;
import de.cuzim1tigaaa.spectator.Spectator;
import de.cuzim1tigaaa.spectator.cycle.CycleTask;
import de.cuzim1tigaaa.spectator.files.*;
import de.cuzim1tigaaa.spectator.spectate.SpectateUtilsGeneral;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.plugin.PluginManager;

import java.util.*;

import static de.cuzim1tigaaa.spectator.files.Permissions.*;

public class SpectatorListener implements Listener {

	public static final Set<UUID> gameModeChangeAllowed = new HashSet<>();

	private final Spectator plugin;
	private final SpectateAPI spectateAPI;
	private final SpectateUtilsGeneral spectateUtils;

	public SpectatorListener(Spectator plugin) {
		this.plugin = plugin;
		this.spectateAPI = plugin.getSpectateAPI();
		this.spectateUtils = spectateAPI.getSpectateGeneral();

		PluginManager pluginManager = plugin.getServer().getPluginManager();
		pluginManager.registerEvents(new ArmorstandListener(plugin), plugin);
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

		if(spectateUtils.getTeleportIfReLogin().containsKey(player.getUniqueId()))
			Bukkit.getScheduler().runTaskLater(plugin, () -> player.teleport(
					spectateUtils.getTeleportIfReLogin().remove(player.getUniqueId()),
					PlayerTeleportEvent.TeleportCause.PLUGIN), 20L);

		if(Config.getBoolean(Paths.CONFIG_HIDE_PLAYERS_TAB) && !hasPermission(player, BYPASS_TABLIST))
			spectateAPI.getSpectators().forEach(spectator -> player.hidePlayer(plugin, spectator));

		if(!Config.getBoolean(Paths.CONFIG_CYCLE_PAUSE_NO_PLAYERS))
			return;

		for(Player spectator : spectateAPI.getPausedSpectators()) {
			spectateAPI.getSpectateCycle().restartCycle(spectator);
			CycleTask task = spectateAPI.getCycleTask(spectator);
			Messages.sendMessage(spectator, Paths.MESSAGES_COMMANDS_CYCLE_RESTART,
					"INTERVAL", task.getInterval(), "ORDER", task.getCycle().isAlphabetical() ? "Alphabetic" : "Random");
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

	private final Set<UUID> cooldown = new HashSet<>();

	/**
	 * unspectate, when quitting while spectating
	 * dismount target, when target quits
	 * Pause a running cycle, when no more players are online
	 */
	@EventHandler
	public void playerQuit(PlayerQuitEvent event) {
		Player player = event.getPlayer();

		if(spectateAPI.isSpectator(player)) {
			Spectator.debug("Player " + player.getName() + " was spectating, unspectating...");
			spectateUtils.unspectate(player, true);
			return;
		}

		spectateAPI.getSpectators().forEach(spectator -> player.showPlayer(plugin, spectator));

		for(Player spectator : spectateAPI.getSpectatorsOf(player)) {
			spectateAPI.dismount(spectator);

			if(!spectateAPI.isCyclingSpectator(spectator))
				continue;

			if(player.hasPermission(BYPASS_SPECTATED) || spectateAPI.getSpectateablePlayers().size() - 1 > 0) {
				spectateAPI.getCycleTask(spectator).selectNextPlayer(plugin);
				continue;
			}

			if(!Config.getBoolean(Paths.CONFIG_CYCLE_PAUSE_NO_PLAYERS)) {
				spectateAPI.getSpectateCycle().stopCycle(spectator);
				Messages.sendMessage(spectator, Paths.MESSAGES_COMMANDS_CYCLE_STOP);
			}else {
				spectateAPI.getSpectateCycle().pauseCycle(spectator);
				Messages.sendMessage(spectator, Paths.MESSAGES_COMMANDS_CYCLE_PAUSE);
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

		if(!spectateAPI.isSpectator(player))
			return;

		if(spectateAPI.isCyclingSpectator(player)) {
			Messages.sendMessage(player, Paths.MESSAGES_GENERAL_DISMOUNT);
			event.setCancelled(true);
			return;
		}

		if(hasPermission(player, UTILS_DISMOUNT))
			spectateAPI.dismount(player);
	}

	/**
	 * Disable changing gameMode while in plugins spectator mode
	 */
	@EventHandler
	public void spectatorChangesGameMode(PlayerGameModeChangeEvent event) {
		Player player = event.getPlayer();
		if(!spectateAPI.isSpectator(player))
			return;

		if(!gameModeChangeAllowed.contains(player.getUniqueId()) && !spectateAPI.isCyclingSpectator(player))
			Messages.sendMessage(player, Paths.MESSAGES_GENERAL_GAMEMODE_CHANGE);
		event.setCancelled(true);
		gameModeChangeAllowed.remove(player.getUniqueId());
	}

	/**
	 * Do not allow to kick cycling players if configured so
	 */
	@EventHandler(priority = EventPriority.HIGHEST)
	public void kickCyclingPlayer(PlayerKickEvent event) {
		Player spectator = event.getPlayer();
		if(spectateAPI.isCyclingSpectator(spectator)) {
			event.setCancelled(true);
			if(!Config.getBoolean(Paths.CONFIG_CYCLE_KICK_PLAYERS))
				return;
			spectateAPI.isCyclingSpectator(spectator);
			spectateUtils.unspectate(spectator, true);
			Bukkit.getScheduler().runTaskLater(plugin, () -> spectator.kickPlayer(event.getReason()), 10L);
		}
	}

	/**
	 * Dismount from target, if the target died
	 */
	@EventHandler
	public void targetDeathEvent(PlayerDeathEvent event) {
		Player player = event.getEntity();
		if(spectateAPI.isSpectator(player)) {
			spectateUtils.unspectate(player, false);
			return;
		}

		for(Player spectator : spectateAPI.getSpectatorsOf(player)) {
			spectateAPI.dismount(spectator);

			if(spectateAPI.isCyclingSpectator(spectator))
				spectateAPI.getSpectateCycle().teleportNextPlayer(spectator);
		}
	}
}