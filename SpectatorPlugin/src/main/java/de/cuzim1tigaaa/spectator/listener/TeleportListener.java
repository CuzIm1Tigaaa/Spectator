package de.cuzim1tigaaa.spectator.listener;

import de.cuzim1tigaaa.spectator.SpectateAPI;
import de.cuzim1tigaaa.spectator.Spectator;
import de.cuzim1tigaaa.spectator.files.Messages;
import de.cuzim1tigaaa.spectator.files.Paths;
import lombok.Getter;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.*;

import static de.cuzim1tigaaa.spectator.files.Permissions.*;

public class TeleportListener implements Listener {

	private final Spectator plugin;
	private final SpectateAPI spectateAPI;

	@Getter
	private static final Map<UUID, Player> worldChange = new HashMap<>();

	public TeleportListener(Spectator plugin) {
		this.plugin = plugin;
		this.spectateAPI = plugin.getSpectateAPI();
	}

	/**
	 * When a spectator switches the world, the spectator will be unspectated and spectated again
	 * This handles the change of a world, which will happen, when teleported by a portal
	 * using multiverse
	 */
	@EventHandler
	public void spectatorSwitchingWorld(PlayerTeleportEvent event) {
		Player spectator = event.getPlayer();
		Location from = event.getFrom(), to = event.getTo();
		if(from.getWorld() == null || to == null || to.getWorld() == null)
			return;

		if(from.getWorld().equals(to.getWorld()))
			return;

		if(!spectateAPI.isSpectator(spectator) || spectateAPI.getTargetOf(spectator) != null)
			return;

		if(event.isCancelled())
			return;

		if(hasAccessToWorld(spectator, to.getWorld())) {
			Spectator.debug(String.format("Spectator %-16s switched world! From [%s] to [%s]", spectator.getName(), from.getWorld().getName(), to.getWorld().getName()));
			spectateAPI.toggleTabList(spectator, true);
			SpectatorListener.gameModeChangeAllowed.add(spectator.getUniqueId());

			Player target = worldChange.remove(spectator.getUniqueId());
			if(target != null)
				Bukkit.getScheduler().runTask(plugin, () -> spectator.teleport(target, PlayerTeleportEvent.TeleportCause.PLUGIN));
			Bukkit.getScheduler().runTaskLater(plugin, () -> spectateAPI.getSpectateGeneral().spectate(spectator, target), 5L);
		}
	}

	/**
	 * When a player quits the server, the player will be removed from the worldChange map
	 */
	@EventHandler
	public void playerQuitWorldChange(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		worldChange.remove(player.getUniqueId());
	}


	/**
	 * When a player teleports through a portal, the spectator does not seem to be teleported with the player
	 * This handles the change of a world, which will happen, when teleported by a portal
	 * using multiverse
	 */
	@EventHandler
	public void targetSwitchingWorld(PlayerTeleportEvent event) {
		Player player = event.getPlayer();
		Location from = event.getFrom(), to = event.getTo();

		if(from.getWorld() == null || to == null || to.getWorld() == null)
			return;

		if(from.getWorld().equals(to.getWorld()))
			return;

		if(spectateAPI.isSpectator(player) || spectateAPI.isNotSpectated(player))
			return;

		if(event.isCancelled())
			return;

		Spectator.debug(String.format("Player %-16s switched world! From [%s] to [%s]", player.getName(), from.getWorld().getName(), to.getWorld().getName()));
		spectateAPI.getSpectatorsOf(player).forEach(spectator -> {
			spectateAPI.dismount(spectator);
			if(!hasAccessToWorld(player, to.getWorld()))
				return;

			Spectator.debug(String.format("Spectator %-16s was spectating player %-16s", spectator.getName(), player.getName()));
			SpectatorListener.gameModeChangeAllowed.add(spectator.getUniqueId());
			worldChange.put(spectator.getUniqueId(), player);
		});
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
		if(!spectateAPI.isSpectator(player))
			return;

		if(player.getSpectatorTarget() == null || !(player.getSpectatorTarget() instanceof Player target))
			return;

		if(!hasPermission(player, COMMAND_SPECTATE_OTHERS)) {
			event.setCancelled(true);
			player.setSpectatorTarget(null);
			Messages.sendMessage(player, Paths.MESSAGES_GENERAL_BYPASS_TELEPORT, "TARGET", target.getName());
			return;
		}

		if(hasPermission(target, BYPASS_SPECTATED) && !hasPermission(player, BYPASS_SPECTATEALL)) {
			event.setCancelled(true);
			player.setSpectatorTarget(null);
			Messages.sendMessage(player, Paths.MESSAGES_GENERAL_BYPASS_TELEPORT, "TARGET", target.getName());
			return;
		}

		spectateAPI.setRelation(player, target);
		plugin.getInventory().getTargetInventory(player, target);
	}

	private boolean hasAccessToWorld(Player player, World world) {
		if(player.getWorld().equals(world))
			return true;
		if(plugin.getMultiverseCore() == null)
			return true;
		return player.hasPermission("multiverse.access." + plugin.getMultiverseCore().getMVWorldManager().getMVWorld(world).getPermissibleName());
	}
}