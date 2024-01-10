package de.cuzim1tigaaa.spectator.listener;

import de.cuzim1tigaaa.spectator.Spectator;
import de.cuzim1tigaaa.spectator.files.Messages;
import de.cuzim1tigaaa.spectator.files.Paths;
import de.cuzim1tigaaa.spectator.player.Inventory;
import de.cuzim1tigaaa.spectator.spectate.SpectateUtils;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;

import static de.cuzim1tigaaa.spectator.files.Permissions.*;

public class TeleportListener implements Listener {

	private final Spectator plugin;
	private final SpectateUtils spectateUtils;

	public TeleportListener(Spectator plugin) {
		this.plugin = plugin;
		this.spectateUtils = plugin.getSpectateUtils();
	}

	/**
	 * When a player teleports through a portal, the spectator does not seem to be teleported with the player
	 * This handles the change of a world, which will happen, when teleported by a portal
	 */
	@EventHandler
	public void targetSwitchingWorld(PlayerTeleportEvent event) {
		Player player = event.getPlayer();
		Location from = event.getFrom(), to = event.getTo();

		if(event.isCancelled())
			return;

		if(from.getWorld() == null || to == null || to.getWorld() == null)
			return;

		if(from.getWorld().equals(to.getWorld()))
			return;

		if(spectateUtils.isSpectator(player)) {
			if(spectateUtils.getTargetOf(player) != null)
				spectateUtils.Dismount(player);

			if(hasAccessToWorld(player, to.getWorld())) {
				spectateUtils.getSpectateInformation(player).restoreAttributes();
				Bukkit.getScheduler().runTaskLater(plugin, () -> spectateUtils.Spectate(player, null), 5L);
			}
		}

		if(spectateUtils.isNotSpectated(player))
			return;

		plugin.Debug(String.format("Player %-16s switched world! From [%s] to [%s]", player.getName(), from.getWorld().getName(), to.getWorld().getName()));
		spectateUtils.getSpectatorsOf(player).forEach(spectator -> {
			if(!hasAccessToWorld(player, to.getWorld())) {
				spectateUtils.Dismount(spectator);
				return;
			}
			spectateUtils.Dismount(spectator);
			spectateUtils.getSpectateInformation(spectator).restoreAttributes();
			Bukkit.getScheduler().runTaskLater(plugin, () -> spectateUtils.Spectate(spectator, player), 5L);
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
		if(plugin.getMultiverseCore() != null)
			return player.hasPermission("multiverse.access." + plugin.getMultiverseCore().getMVWorldManager().getMVWorld(world).getPermissibleName());
		return true;
	}
}