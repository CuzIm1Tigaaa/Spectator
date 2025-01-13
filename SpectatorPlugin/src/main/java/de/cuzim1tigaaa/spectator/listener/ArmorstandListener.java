package de.cuzim1tigaaa.spectator.listener;

import de.cuzim1tigaaa.spectator.SpectateAPI;
import de.cuzim1tigaaa.spectator.Spectator;
import de.cuzim1tigaaa.spectator.files.Config;
import de.cuzim1tigaaa.spectator.files.Paths;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.ArrayList;

import static de.cuzim1tigaaa.spectator.files.Permissions.UTILS_HIDE_ARMORSTAND;
import static de.cuzim1tigaaa.spectator.files.Permissions.hasPermission;

public class ArmorstandListener implements Listener {

	private final SpectateAPI spectateAPI;

	public ArmorstandListener(Spectator plugin) {
		this.spectateAPI = plugin.getSpectateAPI();
	}

	@EventHandler
	public void playerMove(PlayerMoveEvent event) {
		Player player = event.getPlayer();
		if(!Config.getBoolean(Paths.CONFIG_HIDE_ARMOR_STANDS) || !hasPermission(player, UTILS_HIDE_ARMORSTAND))
			return;

		if(!spectateAPI.isSpectator(player) && !spectateAPI.isCyclingSpectator(player))
			return;

		Location from = event.getFrom(),
				to = event.getTo();

		if(to == null)
			return;

		if(from.getBlockX() == to.getBlockX() && from.getBlockZ() == to.getBlockZ())
			return;

		if(spectateAPI.getSpectateInfo(player).isHideArmorStands())
			spectateAPI.hideArmorstands(player);
	}

	@EventHandler
	public void playerMove(PlayerTeleportEvent event) {
		Player player = event.getPlayer();
		if(!Config.getBoolean(Paths.CONFIG_HIDE_ARMOR_STANDS) || !hasPermission(player, UTILS_HIDE_ARMORSTAND))
			return;

		if(!spectateAPI.isSpectator(player) && !spectateAPI.isCyclingSpectator(player))
			return;

		Location from = event.getFrom(),
				to = event.getTo();

		if(to == null)
			return;

		if(from.getBlockX() == to.getBlockX() && from.getBlockZ() == to.getBlockZ())
			return;

		if(spectateAPI.getSpectateInfo(player).isHideArmorStands())
			spectateAPI.hideArmorstands(player);
	}

	@EventHandler
	public void armorstandSpawn(EntitySpawnEvent event) {
		if(!(event.getEntity() instanceof ArmorStand armorStand))
			return;

		armorStand.getNearbyEntities(3, 3, 3).forEach(entity -> {
			if (entity instanceof Player player) {
				if(spectateAPI.isSpectator(player) && spectateAPI.getSpectateInfo(player).isHideArmorStands()) {
					player.hideEntity(Spectator.getPlugin(), armorStand);
					spectateAPI.getHiddenArmorStands().computeIfAbsent(player.getUniqueId(), k -> new ArrayList<>()).add(armorStand);
				}
			}
		});
	}

}