package de.cuzim1tigaaa.spectator.listener;

import de.cuzim1tigaaa.spectator.Spectator;
import de.cuzim1tigaaa.spectator.cycle.CycleTask;
import de.cuzim1tigaaa.spectator.files.*;
import de.cuzim1tigaaa.spectator.spectate.SpectateUtils;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
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
			spectateUtils.restartCycle(spectator);
			CycleTask task = spectateUtils.getCycleTask(spectator);
			spectator.sendMessage(Messages.getMessage(spectator, Paths.MESSAGES_COMMANDS_CYCLE_RESTART,
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


	@EventHandler
	public void playerMove(PlayerMoveEvent event) {
		Player player = event.getPlayer();
		if(!spectateUtils.isSpectator(player) && !spectateUtils.isCycling(player))
			return;

		Location from = event.getFrom(),
				to = event.getTo();

		if(to == null)
			return;

		if(from.getBlockX() == to.getBlockX() && from.getBlockZ() == to.getBlockZ())
			return;

		player.getNearbyEntities(3, 3, 3).forEach(entity -> {
			if(entity instanceof ArmorStand) {
				player.hideEntity(plugin, entity);
				spectateUtils.getSpectateInformation(player).
						getHiddenArmorStands().add((ArmorStand) entity);
			}
		});
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
			spectateUtils.unspectate(player, true);
			return;
		}

		spectateUtils.getSpectators().forEach(spectator -> player.showPlayer(plugin, spectator));

		for(Player spectator : spectateUtils.getSpectatorsOf(player)) {
			spectateUtils.dismount(spectator);

			if(!spectateUtils.isCycling(spectator))
				continue;

			if(player.hasPermission(BYPASS_SPECTATED) || plugin.getSpectateUtils().getSpectateablePlayers().size() - 1 > 0)
				continue;

			if(!Config.getBoolean(Paths.CONFIG_CYCLE_PAUSE_NO_PLAYERS)) {
				spectateUtils.stopCycle(spectator);
				spectator.sendMessage(Messages.getMessage(spectator, Paths.MESSAGES_COMMANDS_CYCLE_STOP));
			}else {
				spectateUtils.pauseCycle(spectator);
				spectator.sendMessage(Messages.getMessage(spectator, Paths.MESSAGES_COMMANDS_CYCLE_PAUSE));
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
			player.sendMessage(Messages.getMessage(player, Paths.MESSAGES_GENERAL_DISMOUNT));
			event.setCancelled(true);
			return;
		}

		spectateUtils.dismount(player);
	}

	/**
	 * Disable changing gameMode while in plugins spectator mode
	 */
	@EventHandler
	public void spectatorChangesGameMode(PlayerGameModeChangeEvent event) {
		Player player = event.getPlayer();
		if(!spectateUtils.isSpectator(player))
			return;

		if(!gameModeChangeAllowed.contains(player.getUniqueId()) && !spectateUtils.isCycling(player))
			player.sendMessage(Messages.getMessage(player, Paths.MESSAGES_GENERAL_GAMEMODE_CHANGE));
		event.setCancelled(true);
		gameModeChangeAllowed.remove(player.getUniqueId());
	}

	/**
	 * Do not allow to kick cycling players if configured so
	 */
	@EventHandler(priority = EventPriority.HIGHEST)
	public void kickCyclingPlayer(PlayerKickEvent event) {
		Player spectator = event.getPlayer();
		if(spectateUtils.isCycling(spectator)) {
			event.setCancelled(true);
			if(!Config.getBoolean(Paths.CONFIG_CYCLE_KICK_PLAYERS))
				return;
			spectateUtils.stopCycle(spectator);
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
		if(spectateUtils.isSpectator(player)) {
			spectateUtils.unspectate(player, false);
			return;
		}

		for(Player spectator : spectateUtils.getSpectatorsOf(player)) {
			spectateUtils.dismount(spectator);

			if(spectateUtils.isCycling(spectator))
				spectateUtils.teleportNextPlayer(spectator);
		}
	}
}