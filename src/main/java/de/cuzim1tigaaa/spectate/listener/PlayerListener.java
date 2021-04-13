package de.cuzim1tigaaa.spectate.listener;

import de.cuzim1tigaaa.spectate.Main;
import de.cuzim1tigaaa.spectate.cycle.CycleHandler;
import de.cuzim1tigaaa.spectate.files.Config;
import de.cuzim1tigaaa.spectate.files.Paths;
import de.cuzim1tigaaa.spectate.files.Permissions;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.server.PluginDisableEvent;

import java.util.Map;

public class PlayerListener implements Listener {

    private final Main instance;

    public PlayerListener(Main plugin) {
        this.instance = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, instance);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if(!player.hasPermission(Permissions.BYPASS_TABLIST) && Config.hideTab) for(Player hidden : instance.getMethods().getHidden()) player.hidePlayer(instance, hidden);
        else for(Player hidden : instance.getMethods().getHidden()) player.showPlayer(instance, hidden);
    }
    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if(instance.getSpectators().contains(player)) instance.getMethods().unSpectate(player, false);
        for (Map.Entry<Player, Player> entry : instance.getRelation().entrySet()) {
            if (entry.getValue().equals(player)) {
                Player spectator = entry.getKey();
                if (!CycleHandler.isPlayerCycling(spectator)) instance.dismountTarget(spectator);
                else CycleHandler.restartCycle(spectator);
            }
        }
    }
    @EventHandler
    public void onPluginDisable(PluginDisableEvent event) { if(event != null) instance.disable(); }
    @EventHandler
    public void onDismount(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();
        if(instance.getSpectators().contains(player)) {
            if(!CycleHandler.isPlayerCycling(player) || !player.hasPermission(Permissions.COMMANDS_SPECTATE_CYCLEONLY)) {
                if(event.isSneaking()) instance.dismountTarget(player);
            }else {
                if(event.isSneaking()) {
                    player.sendMessage(Config.getMessage(Paths.MESSAGES_GENERAL_DISMOUNT));
                    event.setCancelled(true);
                }
            }
        }
    }
    @EventHandler
    public void onGameModeChange(PlayerGameModeChangeEvent event) {
        Player player = event.getPlayer();
        if(instance.getSpectators().contains(player)) {
            player.sendMessage(Config.getMessage(Paths.MESSAGES_GENERAL_GAMEMODE_CHANGE));
            event.setCancelled(true);
        }
    }

    @EventHandler (priority = EventPriority.HIGHEST)
    public void onKick(PlayerKickEvent event) {
        Player player = event.getPlayer();
        if(!Config.kickOnCycle && CycleHandler.isPlayerCycling(player)) event.setCancelled(true);
    }
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) { if (event.getWhoClicked().getGameMode().equals(GameMode.SPECTATOR)) event.setCancelled(true); }
    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) { if (event.getWhoClicked().getGameMode().equals(GameMode.SPECTATOR)) event.setCancelled(true); }
}