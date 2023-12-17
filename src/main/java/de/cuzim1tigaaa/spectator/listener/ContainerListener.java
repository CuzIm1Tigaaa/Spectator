package de.cuzim1tigaaa.spectator.listener;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import de.cuzim1tigaaa.spectator.Spectator;
import de.cuzim1tigaaa.spectator.files.*;
import de.cuzim1tigaaa.spectator.spectate.SpectateUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.Inventory;

import java.util.*;

public class ContainerListener implements Listener {

	private final SpectateUtils spectateUtils;

	private final ImmutableSet<InventoryType> container = Sets.immutableEnumSet(InventoryType.BARREL, InventoryType.BLAST_FURNACE,
			InventoryType.BREWING, InventoryType.CHEST, InventoryType.DISPENSER, InventoryType.DROPPER, InventoryType.FURNACE,
			InventoryType.HOPPER, InventoryType.SMOKER, InventoryType.SHULKER_BOX, InventoryType.LECTERN);

	public ContainerListener(Spectator plugin) {
		this.spectateUtils = plugin.getSpectateUtils();
	}

	/**
	 * Just in case, cancel clicking in inventories when in spectator mode
	 */
	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
		Player player = (Player) event.getWhoClicked();
		if(!spectateUtils.isSpectator(player)) return;
		event.setCancelled(true);
	}

	/**
	 * Just in case, cancel dragging items around inventories when in spectator mode
	 */
	@EventHandler
	public void onInventoryDrag(InventoryDragEvent event) {
		Player player = (Player) event.getWhoClicked();
		if(!spectateUtils.isSpectator(player)) return;
		event.setCancelled(true);
	}

	/**
	 * Open container inventories for spectating players too
	 */
	@EventHandler
	public void onChestOpen(InventoryOpenEvent event) {
		Player player = (Player) event.getPlayer();
		if(spectateUtils.isNotSpectated(player)) return;

		Set<Player> spectators = new HashSet<>(spectateUtils.getSpectators());
		spectators.removeIf(p -> !Objects.equals(p.getSpectatorTarget(), player));

		Inventory inventory = null, openedInventory = event.getInventory();

		if(openedInventory.getType() == InventoryType.ENDER_CHEST) {
			spectators.removeIf(p -> !p.hasPermission(Permissions.UTILS_OPEN_ENDERCHEST));
			if(Config.getBoolean(Paths.CONFIG_INVENTORY_ENDER_CHEST)) inventory = player.getEnderChest();
		}
		if(this.container.contains(openedInventory.getType())) {
			spectators.removeIf(p -> !p.hasPermission(Permissions.UTILS_OPEN_CONTAINER));
			if(Config.getBoolean(Paths.CONFIG_INVENTORY_CONTAINERS))
				inventory = openedInventory;
		}
		if(inventory != null) {
			for(Player spec : spectators)
				spec.openInventory(inventory);
		}
	}

	/**
	 * Close container inventories for spectating players too
	 */
	@EventHandler
	public void onChestClose(InventoryCloseEvent event) {
		Player player = (Player) event.getPlayer();
		if(spectateUtils.isNotSpectated(player)) return;

		Inventory openedInventory = event.getInventory();

		if(openedInventory.getType() == InventoryType.ENDER_CHEST) {
			if(!Config.getBoolean(Paths.CONFIG_INVENTORY_ENDER_CHEST))
				return;
		}

		if(!this.container.contains(openedInventory.getType()))
			return;

		if(!Config.getBoolean(Paths.CONFIG_INVENTORY_CONTAINERS))
			return;

		Set<Player> spectators = new HashSet<>(spectateUtils.getSpectators());
		spectators.removeIf(p -> !Objects.equals(p.getSpectatorTarget(), player));

		for(Player spec : spectators)
			spec.closeInventory();
	}
}