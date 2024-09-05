package de.cuzim1tigaaa.spectator.listener;

import de.cuzim1tigaaa.spectator.Spectator;
import de.cuzim1tigaaa.spectator.files.Messages;
import de.cuzim1tigaaa.spectator.files.Paths;
import de.cuzim1tigaaa.spectator.player.Inventory;
import de.cuzim1tigaaa.spectator.spectate.SpectateUtils;
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
	private final SpectateUtils spectateUtils;

	@Getter private static final Map<UUID, Player> worldChange = new HashMap<>();

	public TeleportListener(Spectator plugin) {
		this.plugin = plugin;
		this.spectateUtils = plugin.getSpectateUtils();
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

		if(!spectateUtils.isSpectator(spectator) || spectateUtils.getTargetOf(spectator) != null)
			return;

		if(event.isCancelled())
			return;

		if(from.getWorld() == null || to == null || to.getWorld() == null)
			return;

		if(from.getWorld().equals(to.getWorld()))
			return;

		if(hasAccessToWorld(spectator, to.getWorld())) {
			Spectator.Debug(String.format("Spectator %-16s switched world! From [%s] to [%s]", spectator.getName(), from.getWorld().getName(), to.getWorld().getName()));
			spectateUtils.simulateUnspectate(spectator);
			spectateUtils.toggleTabList(spectator, true);
			SpectatorListener.gameModeChangeAllowed.add(spectator.getUniqueId());

			Bukkit.getScheduler().runTaskLater(plugin, () -> {
				Player target = null;
				if(worldChange.containsKey(spectator.getUniqueId()))
					target = worldChange.remove(spectator.getUniqueId());
				spectateUtils.spectate(spectator, target);
			}, 5L);
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

		if(spectateUtils.isSpectator(player) || spectateUtils.isNotSpectated(player))
			return;

		if(event.isCancelled())
			return;

		if(from.getWorld() == null || to == null || to.getWorld() == null)
			return;

		if(from.getWorld().equals(to.getWorld()))
			return;

		Spectator.Debug(String.format("Player %-16s switched world! From [%s] to [%s]", player.getName(), from.getWorld().getName(), to.getWorld().getName()));
		spectateUtils.getSpectatorsOf(player).forEach(spectator -> {
			spectateUtils.dismount(spectator);
			if(!hasAccessToWorld(player, to.getWorld()))
				return;

			Spectator.Debug(String.format("Spectator %-16s was spectating player %-16s", spectator.getName(), player.getName()));
			SpectatorListener.gameModeChangeAllowed.add(spectator.getUniqueId());
			Bukkit.getScheduler().runTaskLater(plugin, () -> {
				worldChange.put(spectator.getUniqueId(), player);
				spectator.teleport(player, PlayerTeleportEvent.TeleportCause.PLUGIN);
			}, 5L);
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
		if(!spectateUtils.isSpectator(player))
			return;

		if(player.getSpectatorTarget() == null || !(player.getSpectatorTarget() instanceof Player target))
			return;

		if(!hasPermission(player, COMMAND_SPECTATE_OTHERS)) {
			event.setCancelled(true);
			player.setSpectatorTarget(null);
			player.sendMessage(Messages.getMessage(player, Paths.MESSAGES_GENERAL_BYPASS_TELEPORT, "TARGET", target.getName()));
			return;
		}

		if(hasPermission(target, BYPASS_SPECTATED) && !hasPermission(player, BYPASS_SPECTATEALL)) {
			event.setCancelled(true);
			player.setSpectatorTarget(null);
			player.sendMessage(Messages.getMessage(player, Paths.MESSAGES_GENERAL_BYPASS_TELEPORT, "TARGET", target.getName()));
			return;
		}

		spectateUtils.setRelation(player, target);
		Inventory.getInventory(player, target);
	}

	private boolean hasAccessToWorld(Player player, World world) {
		if(player.getWorld().equals(world))
			return true;
		if(plugin.getMultiverseCore() == null)
			return true;
		return player.hasPermission("multiverse.access." + plugin.getMultiverseCore().getMVWorldManager().getMVWorld(world).getPermissibleName());
	}
}