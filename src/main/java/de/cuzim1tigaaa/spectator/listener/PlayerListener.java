package de.cuzim1tigaaa.spectator.listener;

import com.destroystokyo.paper.event.player.PlayerAdvancementCriterionGrantEvent;
import de.cuzim1tigaaa.spectator.Spectator;
import de.cuzim1tigaaa.spectator.cycle.CycleHandler;
import de.cuzim1tigaaa.spectator.files.*;
import de.cuzim1tigaaa.spectator.player.SpectateManager;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.*;
import org.bukkit.inventory.Inventory;

import java.util.*;

public class PlayerListener implements Listener {

    private final Spectator plugin;
    private final SpectateManager manager;

    public PlayerListener(Spectator plugin) {
        this.plugin = plugin;
        this.plugin.getServer().getPluginManager().registerEvents(this, plugin);
        this.manager = this.plugin.getSpectateManager();
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if(player.hasPermission(Permissions.NOTIFY_UPDATE_ON_JOIN) && Config.getBoolean(Paths.CONFIG_NOTIFY_UPDATE)) {
            if(this.plugin.getUpdateChecker().isAvailable()) {
                player.sendMessage(ChatColor.RED + "Spectator " + ChatColor.DARK_GRAY + "|" + ChatColor.GOLD + ChatColor.BOLD + " An Update is available! "
                        + ChatColor.DARK_GRAY + "[" + ChatColor.YELLOW + "v" + this.plugin.getUpdateChecker().getVersion().replace("v", "") + ChatColor.DARK_GRAY + "]");
                player.sendMessage(ChatColor.DARK_GRAY + ChatColor.BOLD.toString() + "» " + ChatColor.YELLOW + "https://www.spigotmc.org/resources/spectator.93051/");
            }
        }
        if(Config.getBoolean(Paths.CONFIG_CYCLE_PAUSE_NO_PLAYERS))
            for(Player paused : CycleHandler.getPausedCycles().keySet()) CycleHandler.restartCycle(paused);

        boolean hide = !player.hasPermission(Permissions.BYPASS_TABLIST) && Config.getBoolean(Paths.CONFIG_HIDE_PLAYERS_TAB);
        for(Player hidden : this.manager.getHidden()) {
            if(hide) player.hidePlayer(this.plugin, hidden);
            else player.showPlayer(this.plugin, hidden);
        }
    }
    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if(this.plugin.getSpectators().contains(player)) this.manager.unSpectate(player, false);
        for (Map.Entry<Player, Player> entry : this.plugin.getRelation().entrySet()) {
            if (entry.getValue().equals(player)) {
                Player spectator = entry.getKey();
                if (!CycleHandler.isPlayerCycling(spectator)) this.manager.dismountTarget(spectator);
                else {
                    int onlineNonSpec = (Bukkit.getOnlinePlayers().size() - 1) - this.plugin.getSpectators().size();
                    if(onlineNonSpec <= 0) {
                        if(Config.getBoolean(Paths.CONFIG_CYCLE_PAUSE_NO_PLAYERS)) CycleHandler.pauseCycle(spectator);
                        else CycleHandler.stopCycle(spectator);
                    }else {
                        spectator.setSpectatorTarget(null);
                        CycleHandler.next(spectator);
                    }
                }
            }
        }
    }
    @EventHandler
    public void onDismount(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();
        if(this.plugin.getSpectators().contains(player)) {
            if(!CycleHandler.isPlayerCycling(player) || !player.hasPermission(Permissions.COMMANDS_SPECTATE_CYCLEONLY)) {
                if(event.isSneaking()) this.manager.dismountTarget(player);
            }else {
                if(event.isSneaking() && this.plugin.getRelation().getOrDefault(player, null) != null) {
                    player.sendMessage(Messages.getMessage(Paths.MESSAGES_GENERAL_DISMOUNT));
                    event.setCancelled(true);
                }
            }
        }
    }
    @EventHandler
    public void onGameModeChange(PlayerGameModeChangeEvent event) {
        Player player = event.getPlayer();
        if(this.plugin.getSpectators().contains(player)) {
            player.sendMessage(Messages.getMessage(Paths.MESSAGES_GENERAL_GAMEMODE_CHANGE));
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onAdvancementCriteriaGrant(PlayerAdvancementCriterionGrantEvent event) {
        Player player = event.getPlayer();
        if(!plugin.getSpectators().contains(player)) return;
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST) 
    public void onKick(PlayerKickEvent event) {
        Player player = event.getPlayer();
        if(!Config.getBoolean(Paths.CONFIG_KICK_WHILE_CYCLING) && CycleHandler.isPlayerCycling(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        if(this.plugin.getSpectators().contains(player)) this.plugin.getSpectateManager().unSpectate(player, false);
        for (Map.Entry<Player, Player> entry : this.plugin.getRelation().entrySet()) {
            if (entry.getValue().equals(player)) {
                Player spectator = entry.getKey();
                if (!CycleHandler.isPlayerCycling(spectator)) this.manager.dismountTarget(spectator);
                else {
                    spectator.setSpectatorTarget(null);
                    CycleHandler.next(spectator);
                }
            }
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getWhoClicked().getGameMode().equals(GameMode.SPECTATOR)) event.setCancelled(true);
    }
    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) { if (event.getWhoClicked().getGameMode().equals(GameMode.SPECTATOR)) event.setCancelled(true); }

    @EventHandler
    public void onChestOpen(InventoryOpenEvent event) {
        Player player = (Player) event.getPlayer();
        if(!this.plugin.getRelation().containsValue(player)) return;

        Set<Player> spectators = new HashSet<>(this.plugin.getSpectators());
        spectators.removeIf(p -> !this.plugin.getRelation().containsKey(p) || !this.plugin.getRelation().get(p).equals(player));

        Inventory inventory = null;
        switch(event.getInventory().getType()) {
            case BARREL, BLAST_FURNACE, BREWING, CHEST, DISPENSER, DROPPER, FURNACE, HOPPER, SMOKER, SHULKER_BOX, LECTERN -> {
                spectators.removeIf(p -> !p.hasPermission(Permissions.UTILS_OPEN_CONTAINER));
                if(Config.getBoolean(Paths.CONFIG_INVENTORY_CONTAINERS)) inventory = event.getInventory();
            }
            case ENDER_CHEST -> {
                spectators.removeIf(p -> !p.hasPermission(Permissions.UTILS_OPEN_ENDERCHEST));
                if(Config.getBoolean(Paths.CONFIG_INVENTORY_ENDERCHEST)) inventory = player.getEnderChest();
            }
        }
        if(inventory != null) for(Player spec : spectators) spec.openInventory(inventory);
    }
    @EventHandler
    public void onChestClose(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();
        if(!this.plugin.getRelation().containsValue(player)) return;

        switch(event.getInventory().getType()) {
            case WORKBENCH, STONECUTTER, CARTOGRAPHY, GRINDSTONE, ENCHANTING, COMPOSTER, SMITHING, CREATIVE, BEACON, ANVIL, LOOM, CRAFTING, MERCHANT, PLAYER -> { return; }
            case ENDER_CHEST -> { if(!Config.getBoolean(Paths.CONFIG_INVENTORY_ENDERCHEST)) return; }
            case BARREL, BLAST_FURNACE, BREWING, CHEST, DISPENSER, DROPPER, FURNACE, HOPPER, SMOKER, SHULKER_BOX, LECTERN -> { if(!Config.getBoolean(Paths.CONFIG_INVENTORY_CONTAINERS)) return; }
        }

        Set<Player> spectators = new HashSet<>(this.plugin.getSpectators());
        spectators.removeIf(p -> p.getSpectatorTarget() == null || !(p.getSpectatorTarget() instanceof Player)
                || !p.getSpectatorTarget().getUniqueId().equals(player.getUniqueId()));

        for(Player spec : spectators) spec.closeInventory();
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent event) {
        if(event.getCause() != PlayerTeleportEvent.TeleportCause.SPECTATE) return;
        Player player = event.getPlayer();
        if(player.getSpectatorTarget() == null || !(player.getSpectatorTarget() instanceof Player target)) return;

        if(!player.hasPermission(Permissions.COMMAND_SPECTATE_OTHERS)) return;
        if(target.hasPermission(Permissions.BYPASS_SPECTATED)) {
            if(!player.hasPermission(Permissions.BYPASS_SPECTATEALL)) {
                player.sendMessage(Messages.getMessage(Paths.MESSAGES_GENERAL_BYPASS_INVENTORY, "TARGET", target.getName()));
                event.setCancelled(true);
                return;
            }
        }
        de.cuzim1tigaaa.spectator.player.Inventory.getInventory(player, target);
        this.plugin.getRelation().put(player, target);
    }
}