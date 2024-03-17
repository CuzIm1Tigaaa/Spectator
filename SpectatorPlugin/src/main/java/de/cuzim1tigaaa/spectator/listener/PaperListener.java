package de.cuzim1tigaaa.spectator.listener;

import com.destroystokyo.paper.event.player.PlayerAdvancementCriterionGrantEvent;

import de.cuzim1tigaaa.spectator.Spectator;
import de.cuzim1tigaaa.spectator.spectate.SpectateUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.*;

public class PaperListener implements Listener {

	private final SpectateUtils spectateUtils;

	public PaperListener(Spectator plugin) {
		this.spectateUtils = plugin.getSpectateUtils();
	}

	/**
	 * Used to cancel advancement progress while spectating
	 */
	@EventHandler(priority = EventPriority.HIGHEST)
	public void advancementCriteriaGrant(PlayerAdvancementCriterionGrantEvent event) {
		Player player = event.getPlayer();
		if(!spectateUtils.isSpectator(player))
			return;

		event.setCancelled(true);
	}
}