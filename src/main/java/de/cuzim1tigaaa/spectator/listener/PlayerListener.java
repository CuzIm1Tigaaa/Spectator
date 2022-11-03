package de.cuzim1tigaaa.spectator.listener;

import com.destroystokyo.paper.event.player.PlayerAdvancementCriterionGrantEvent;
import de.cuzim1tigaaa.spectator.Spectator;
import de.cuzim1tigaaa.spectator.files.*;
import de.cuzim1tigaaa.spectator.player.Inventory;
import de.cuzim1tigaaa.spectator.player.SpectateManager;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;

import java.util.Map;
import java.util.UUID;

public class PlayerListener implements Listener {

	private final Spectator plugin;
	private final SpectateManager manager;

	public PlayerListener(Spectator plugin) {
		this.plugin = plugin;
		this.plugin.getServer().getPluginManager().registerEvents(this, plugin);
		this.manager = this.plugin.getSpectateManager();
	}

	/**
	 * Notify admins, if the plugin has a newer version
	 * Restart a paused cycle, when no players were online before
	 * Hide spectating players in the tabList if enabled / set permissions allow it
	 */
	@EventHandler
	public void playerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();

		if(player.hasPermission(Permissions.NOTIFY_UPDATE_ON_JOIN) && Config.getBoolean(Paths.CONFIG_NOTIFY_UPDATE)) {
			if(this.plugin.getUpdateChecker().isUpdate())
				this.sendUpdateNotification(player);
		}
		if(Config.getBoolean(Paths.CONFIG_CYCLE_PAUSE_NO_PLAYERS)) {
			for(UUID paused : plugin.getCycleHandler().getPaused().keySet()) {
				Player pausedPlayer = Bukkit.getPlayer(paused);
				if(pausedPlayer == null || !pausedPlayer.isOnline()) {
					this.plugin.getCycleHandler().getPaused().remove(paused);
					continue;
				}
				this.plugin.getCycleHandler().restartCycle(pausedPlayer);
			}
		}
		boolean hideSpectators = Config.getBoolean(Paths.CONFIG_HIDE_PLAYERS_TAB) &&
				!player.hasPermission(Permissions.BYPASS_TABLIST);

		for(Player hidden : this.manager.getHidden()) {
			if(hideSpectators) player.hidePlayer(this.plugin, hidden);
			else player.showPlayer(this.plugin, hidden);
		}
	}

	/**
	 * Pause a running cycle, when no more players are online
	 */
	@EventHandler
	public void playerQuit(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		if(this.plugin.getSpectators().contains(player))
			this.manager.unSpectate(player, false);

		for(Map.Entry<Player, Player> entry : this.plugin.getRelation().entrySet()) {
			if(entry.getValue().equals(player)) {
				Player spectator = entry.getKey();
				Bukkit.getScheduler().runTaskLater(plugin, () -> this.manager.dismountTarget(spectator), 5L);
				if(plugin.getCycleHandler().isPlayerCycling(spectator)) {
					if((Bukkit.getOnlinePlayers().size() - this.plugin.getSpectators().size() - 1) <= 0)
						if(Config.getBoolean(Paths.CONFIG_CYCLE_PAUSE_NO_PLAYERS))
							plugin.getCycleHandler().pauseRunningCycle(spectator);
						else this.plugin.getCycleHandler().stopRunningCycle(spectator, true);
					return;
				}
			}
		}
	}

	/**
	 * Normally a player can "leave" a spectator target with sneaking
	 * This is disabled during spectate cycle (or if the player has the "CycleOnly" permission)
	 */
	@EventHandler
	public void dismountTarget(PlayerToggleSneakEvent event) {
		Player player = event.getPlayer();
		if(this.plugin.getSpectators().contains(player)) {
			if(plugin.getCycleHandler().isPlayerCycling(player) || player.hasPermission(Permissions.COMMANDS_SPECTATE_CYCLEONLY)) {
				if(plugin.getRelation().getOrDefault(player, null) == null) return;
				player.sendMessage(Messages.getMessage(Paths.MESSAGES_GENERAL_DISMOUNT));
				event.setCancelled(true);
				return;
			}
			this.manager.dismountTarget(player);
		}
	}

	/**
	 * Disable changing gameMode while in plugins spectator mode
	 */
	@EventHandler
	public void spectatorChangesGameMode(PlayerGameModeChangeEvent event) {
		Player player = event.getPlayer();
		if(this.plugin.getSpectators().contains(player)) {
			player.sendMessage(Messages.getMessage(Paths.MESSAGES_GENERAL_GAMEMODE_CHANGE));
			event.setCancelled(true);
		}
	}

	/**
	 * PaperMC only!
	 * Used to cancel advancement progress while spectating
	 */
	@EventHandler
	public void advancementCriteriaGrant(PlayerAdvancementCriterionGrantEvent event) {
		Player player = event.getPlayer();
		if(!plugin.getSpectators().contains(player)) return;
		event.setCancelled(true);
	}

	/**
	 * When a player teleports through a portal, the spectator does not seem to be teleported with the player
	 * This handles the change of a world, which will happen, when teleported by a portal
	 */
	@EventHandler
	public void targetSwitchingWorld(PlayerChangedWorldEvent event) {
		Player player = event.getPlayer();
		if(plugin.getSpectators().contains(player)) return;
		if(!plugin.getRelation().containsValue(player)) return;

		for(Player spectator : plugin.getRelation().keySet()) {
			if(plugin.getRelation().get(spectator).equals(player)) {
				manager.dismountTarget(spectator);
				spectator.teleport(player, PlayerTeleportEvent.TeleportCause.PLUGIN);
				plugin.getRelation().put(spectator, player);
			}
		}
	}

	/**
	 * Handles entering of a targets view (left click on player)
	 * if the player has the bypass permission and the spectator don't have the equivalent bypass permission
	 * the spectator cannot join the players view
	 */
	@EventHandler
	public void spectatorEnterTarget(PlayerTeleportEvent event) {
		if(event.getCause() != PlayerTeleportEvent.TeleportCause.SPECTATE) return;

		Player player = event.getPlayer();
		if(!plugin.getSpectators().contains(player)) return;

		if(player.getSpectatorTarget() == null || !(player.getSpectatorTarget() instanceof Player target)) return;
		if(!player.hasPermission(Permissions.COMMAND_SPECTATE_OTHERS)) {
			event.setCancelled(true);
			Bukkit.getScheduler().runTaskLater(plugin, () -> player.setSpectatorTarget(null), 5L);
			return;
		}
		if(target.hasPermission(Permissions.BYPASS_SPECTATED) && !player.hasPermission(Permissions.BYPASS_SPECTATEALL)) {
			event.setCancelled(true);
			player.sendMessage(Messages.getMessage(Paths.MESSAGES_GENERAL_BYPASS_TELEPORT, "TARGET", target.getName()));
			Bukkit.getScheduler().runTaskLater(plugin, () -> player.setSpectatorTarget(null), 5L);
			return;
		}
		Inventory.getInventory(player, target);
		plugin.getRelation().put(player, target);
	}

	/**
	 * Cancel kickEvent, if player is in speccycle
	 * mode AND the config option is enabled
	 */
	@EventHandler(priority = EventPriority.HIGHEST)
	public void kickCyclingPlayer(PlayerKickEvent event) {
		Player player = event.getPlayer();
		if(!Config.getBoolean(Paths.CONFIG_KICK_WHILE_CYCLING) && plugin.getCycleHandler().isPlayerCycling(player)) {
			event.setCancelled(true);
		}
	}

	/**
	 * Dismount from target, if the target died
	 */
	@EventHandler
	public void targetDeathEvent(PlayerDeathEvent event) {
		Player player = event.getEntity();
		if(this.plugin.getSpectators().contains(player))
			this.manager.unSpectate(player, false);

		for(Map.Entry<Player, Player> entry : this.plugin.getRelation().entrySet()) {
			if(entry.getValue().equals(player)) {
				Player spectator = entry.getKey();
				Bukkit.getScheduler().runTaskLater(plugin, () -> this.manager.dismountTarget(spectator), 5L);
			}
		}
	}

	/**
	 * Send a message to the player to notify him that an update is available
	 */
	private void sendUpdateNotification(Player player) {
		// &cSpectator &8| &6&lAn Update is available! &8[&ev1.2.3&8]
		//  &8&l» &ehttps://www.spigotmc.org/resources/spectator.93051/
		player.sendMessage(ChatColor.translateAlternateColorCodes('&', String.format("""
			&cSpectator &8| &6&lAn Update is available! &8[&ev%s&8]
			 &8&l» &ehttps://www.spigotmc.org/resources/spectator.93051/
			""", this.plugin.getUpdateChecker().getVersion().replace("v", ""))));
	}
}