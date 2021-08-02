package de.cuzim1tigaaa.spectator.listener;

import de.cuzim1tigaaa.spectator.Spectator;
import de.cuzim1tigaaa.spectator.cycle.CycleHandler;
import de.cuzim1tigaaa.spectator.files.*;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.server.PluginDisableEvent;

import java.util.Map;

public class PlayerListener implements Listener {

    private final Spectator plugin;

    public PlayerListener(Spectator plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, this.plugin);
    }

    @EventHandler @SuppressWarnings("unused")
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if(player.hasPermission(Permissions.NOTIFY_UPDATE_ON_JOIN) && this.plugin.getConfiguration().getBoolean(Paths.CONFIG_NOTIFY_UPDATE)) {
            if(this.plugin.getUpdateChecker().isAvailable()) {
                player.sendMessage(ChatColor.RED + "Spectator " + ChatColor.DARK_GRAY + "|" + ChatColor.GOLD + ChatColor.BOLD + " An Update is available! "
                        + ChatColor.DARK_GRAY + "[" + ChatColor.YELLOW + "v" + plugin.getUpdateChecker().getVersion() + ChatColor.DARK_GRAY + "]");
                player.sendMessage(ChatColor.DARK_GRAY + ChatColor.BOLD.toString() + "» " + ChatColor.YELLOW + "https://www.spigotmc.org/resources/spectator.93051/");
            }
        }
        if(plugin.getConfiguration().getBoolean(Paths.CONFIG_PAUSE_WHEN_NO_PLAYERS)) {
            for(Player all : CycleHandler.getPausedCycles().keySet()) CycleHandler.restartCycle(player);
        }

        if(!player.hasPermission(Permissions.BYPASS_TABLIST) && this.plugin.getConfiguration().getBoolean(Paths.CONFIG_HIDE_PLAYERS_TAB)) for(Player hidden : plugin.getMethods().getHidden()) player.hidePlayer(plugin, hidden);
        else for(Player hidden : plugin.getMethods().getHidden()) player.showPlayer(plugin, hidden);
    }
    @EventHandler @SuppressWarnings("unused")
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if(plugin.getSpectators().contains(player)) plugin.getMethods().unSpectate(player, false);
        for (Map.Entry<Player, Player> entry : plugin.getRelation().entrySet()) {
            if (entry.getValue().equals(player)) {
                Player spectator = entry.getKey();
                if (!CycleHandler.isPlayerCycling(spectator)) this.plugin.getMethods().dismountTarget(player);
                else CycleHandler.restartCycle(spectator);
            }
        }
    }
    @EventHandler @SuppressWarnings("unused")
    public void onPluginDisable(PluginDisableEvent event) { if(event != null) plugin.disable(); }
    @EventHandler @SuppressWarnings("unused")
    public void onDismount(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();
        if(plugin.getSpectators().contains(player)) {
            if(!CycleHandler.isPlayerCycling(player) || !player.hasPermission(Permissions.COMMANDS_SPECTATE_CYCLEONLY)) {
                if(event.isSneaking()) this.plugin.getMethods().dismountTarget(player);
            }else {
                if(event.isSneaking()) {
                    player.sendMessage(Messages.getMessage(Paths.MESSAGES_GENERAL_DISMOUNT));
                    event.setCancelled(true);
                }
            }
        }
    }
    @EventHandler @SuppressWarnings("unused")
    public void onGameModeChange(PlayerGameModeChangeEvent event) {
        Player player = event.getPlayer();
        if(plugin.getSpectators().contains(player)) {
            player.sendMessage(Messages.getMessage(Paths.MESSAGES_GENERAL_GAMEMODE_CHANGE));
            event.setCancelled(true);
        }
    }

    @EventHandler (priority = EventPriority.HIGHEST) @SuppressWarnings("unused")
    public void onKick(PlayerKickEvent event) {
        Player player = event.getPlayer();
        if(!this.plugin.getConfiguration().getBoolean(Paths.CONFIG_KICK_WHILE_CYCLING) && CycleHandler.isPlayerCycling(player)) event.setCancelled(true);
    }
    @EventHandler @SuppressWarnings("unused")
    public void onInventoryClick(InventoryClickEvent event) { if (event.getWhoClicked().getGameMode().equals(GameMode.SPECTATOR)) event.setCancelled(true); }
    @EventHandler @SuppressWarnings("unused")
    public void onInventoryDrag(InventoryDragEvent event) { if (event.getWhoClicked().getGameMode().equals(GameMode.SPECTATOR)) event.setCancelled(true); }
}