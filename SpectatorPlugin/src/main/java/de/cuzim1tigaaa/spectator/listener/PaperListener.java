package de.cuzim1tigaaa.spectator.listener;

import com.destroystokyo.paper.event.player.PlayerAdvancementCriterionGrantEvent;
import de.cuzim1tigaaa.spectator.SpectateAPI;
import de.cuzim1tigaaa.spectator.Spectator;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class PaperListener implements Listener {

    private final SpectateAPI spectateAPI;

    public PaperListener(Spectator plugin) {
        this.spectateAPI = plugin.getSpectateAPI();
    }

    /**
     * Used to cancel advancement progress while spectating
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void advancementCriteriaGrant(PlayerAdvancementCriterionGrantEvent event) {
        Player player = event.getPlayer();
        if(spectateAPI.isSpectator(player))
            event.setCancelled(true);
    }
}