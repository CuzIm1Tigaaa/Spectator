package de.cuzim1tigaaa.spectate.listener;

import de.cuzim1tigaaa.spectate.Main;
import de.cuzim1tigaaa.spectate.files.Config;
import de.cuzim1tigaaa.spectate.files.Permissions;
import org.bukkit.GameMode;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.*;

import java.util.Map;

public class PlayerListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if(!player.hasPermission(Permissions.TABLIST) && Config.hideTab) {
            for(Player hidden : Main.getInstance().getMethods().getHidden()) {
                player.hidePlayer(Main.getInstance(), hidden);
            }
        }
    }
    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if(Main.getInstance().getSpectators().contains(player)) {
            Main.getInstance().getMethods().unSpectate(player, false);
        }
        for (Map.Entry<Player, Player> entry : Main.getInstance().getRelation().entrySet()) {
            if (entry.getValue().equals(player)) {
                Player spectator = entry.getKey();
                if (!Main.getInstance().getCycleHandler().isPlayerCycling(spectator)) {
                    dismountTarget(spectator);
                }
                else {
                    Main.getInstance().getCycleHandler().restartCycle(spectator);
                }
            }
        }
    }
    @EventHandler
    public void onDismount(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();
        if(Main.getInstance().getSpectators().contains(player)) {
            if(!Main.getInstance().getCycleHandler().isPlayerCycling(player)) {
                if(event.isSneaking()) {
                    dismountTarget(player);
                }
            }else {
                if(event.isSneaking()) {
                    player.sendMessage(Config.getMessage("Config.Error.dismount"));
                    event.setCancelled(true);
                }
            }
        }
    }
    public void dismountTarget(Player player) {
        if(player.getGameMode().equals(GameMode.SPECTATOR)) {
            if(player.getSpectatorTarget() != null && player.getSpectatorTarget().getType().equals(EntityType.PLAYER)) {
                Main.getInstance().getRelation().remove(player);
                player.getInventory().clear();
                player.setSpectatorTarget(null);
            }
        }
    }
    @EventHandler
    public void onGameModeChange(PlayerGameModeChangeEvent event) {
        Player player = event.getPlayer();
        if(Main.getInstance().getSpectators().contains(player)) {
            player.sendMessage(Config.getMessage("Config.Error.gm"));
            event.setCancelled(true);
        }
    }

    @EventHandler (priority = EventPriority.HIGHEST)
    public void onKick(PlayerKickEvent event) {
        Player player = event.getPlayer();
        if(!Config.kickOnCycle && Main.getInstance().getCycleHandler().isPlayerCycling(player)) {
            event.setCancelled(true);
        }
    }
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getWhoClicked().getGameMode().equals(GameMode.SPECTATOR)) {
            event.setCancelled(true);
        }
    }
    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.getWhoClicked().getGameMode().equals(GameMode.SPECTATOR)) {
            event.setCancelled(true);
        }
    }
}