package de.cuzim1tigaaa.spectator.listener;

import de.cuzim1tigaaa.spectator.SpectatorPlugin;
import de.cuzim1tigaaa.spectator.cycle.CycleHandler;
import de.cuzim1tigaaa.spectator.files.Config;
import de.cuzim1tigaaa.spectator.files.Messages;
import de.cuzim1tigaaa.spectator.files.Paths;
import de.cuzim1tigaaa.spectator.files.Permissions;
import de.cuzim1tigaaa.spectator.player.SpectateManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.Inventory;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class PlayerListener implements Listener {

    private final SpectatorPlugin plugin;
    private final SpectateManager manager;

    public PlayerListener(SpectatorPlugin plugin) {
        this.plugin = plugin;
        this.manager = this.plugin.getSpectateManager();
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if (player.hasPermission(Permissions.NOTIFY_UPDATE_ON_JOIN) && Config.getBoolean(Paths.CONFIG_NOTIFY_UPDATE)) {
            if (this.plugin.getUpdateChecker().isAvailable()) {
                player.sendMessage(ChatColor.RED + "Spectator " + ChatColor.DARK_GRAY + "|" + ChatColor.GOLD + ChatColor.BOLD + " An Update is available! "
                        + ChatColor.DARK_GRAY + "[" + ChatColor.YELLOW + "v" + this.plugin.getUpdateChecker().getVersion().replace("v", "") + ChatColor.DARK_GRAY + "]");
                player.sendMessage(ChatColor.DARK_GRAY + ChatColor.BOLD.toString() + "» " + ChatColor.YELLOW + "https://www.spigotmc.org/resources/spectator.93051/");
            }
        }

        if (Config.getBoolean(Paths.CONFIG_CYCLE_PAUSE_NO_PLAYERS))
            for (Player paused : CycleHandler.getPausedCycles().keySet()) CycleHandler.restartCycle(paused);

        boolean hide = !player.hasPermission(Permissions.BYPASS_TABLIST) && Config.getBoolean(Paths.CONFIG_HIDE_PLAYERS_TAB);

        for (Player hidden : this.manager.getHidden()) {
            if (hide)
                player.hidePlayer(this.plugin, hidden);
            else
                player.showPlayer(this.plugin, hidden);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        if (this.plugin.getSpectators().contains(player))
            this.manager.unSpectate(player, false);

        for (Map.Entry<Player, Player> entry : this.plugin.getRelation().entrySet()) {

            if (entry.getValue().equals(player)) {

                Player spectator = entry.getKey();

                if (!CycleHandler.isPlayerCycling(spectator)) {
                    this.manager.dismountTarget(spectator);
                } else {
                    int onlineNonSpec = Bukkit.getOnlinePlayers().size() - 1 - this.plugin.getSpectators().size();

                    if (onlineNonSpec <= 0) {
                        if (Config.getBoolean(Paths.CONFIG_CYCLE_PAUSE_NO_PLAYERS))
                            CycleHandler.pauseCycle(spectator);
                        else
                            CycleHandler.stopCycle(spectator);
                    } else {
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

        if (this.plugin.getSpectators().contains(player)) {
            if (!CycleHandler.isPlayerCycling(player) || !player.hasPermission(Permissions.COMMANDS_SPECTATE_CYCLEONLY)) {
                if (event.isSneaking())
                    this.manager.dismountTarget(player);
            } else {
                if (event.isSneaking() && this.plugin.getRelation().getOrDefault(player, null) != null) {
                    player.sendMessage(Messages.getMessage(Paths.MESSAGES_GENERAL_DISMOUNT));
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onGameModeChange(PlayerGameModeChangeEvent event) {
        Player player = event.getPlayer();

        if (this.plugin.getSpectators().contains(player)) {
            player.sendMessage(Messages.getMessage(Paths.MESSAGES_GENERAL_GAMEMODE_CHANGE));
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onKick(PlayerKickEvent event) {
        Player player = event.getPlayer();

        if (!Config.getBoolean(Paths.CONFIG_KICK_WHILE_CYCLING) && CycleHandler.isPlayerCycling(player))
            event.setCancelled(true);
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();

        if (this.plugin.getSpectators().contains(player))
            this.plugin.getSpectateManager().unSpectate(player, false);

        for (Map.Entry<Player, Player> entry : this.plugin.getRelation().entrySet()) {
            if (entry.getValue().equals(player)) {
                Player spectator = entry.getKey();

                if (!CycleHandler.isPlayerCycling(spectator)) {
                    this.manager.dismountTarget(spectator);
                } else {
                    spectator.setSpectatorTarget(null);
                    CycleHandler.next(spectator);
                }
            }
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getWhoClicked().getGameMode().equals(GameMode.SPECTATOR))
            event.setCancelled(true);
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.getWhoClicked().getGameMode().equals(GameMode.SPECTATOR))
            event.setCancelled(true);
    }

    @EventHandler
    public void onChestOpen(InventoryOpenEvent event) {
        Player player = (Player) event.getPlayer();
        if (!this.plugin.getRelation().containsValue(player))
            return;

        Set<Player> spectators = new HashSet<>(this.plugin.getSpectators());
        spectators.removeIf(spectator -> !this.plugin.getRelation().containsKey(spectator) ||
                !this.plugin.getRelation().get(spectator).equals(player));

        Inventory inventory = null;

        switch (event.getInventory().getType()) {
            case BARREL:
            case BLAST_FURNACE:
            case BREWING:
            case CHEST:
            case DISPENSER:
            case DROPPER:
            case FURNACE:
            case HOPPER:
            case SMOKER:
            case SHULKER_BOX:
            case LECTERN: {
                spectators.removeIf(spectator -> !spectator.hasPermission(Permissions.UTILS_OPEN_CONTAINER));

                if (Config.getBoolean(Paths.CONFIG_INVENTORY_CONTAINERS))
                    inventory = event.getInventory();
            }
            case ENDER_CHEST: {
                spectators.removeIf(spectator -> !spectator.hasPermission(Permissions.UTILS_OPEN_ENDERCHEST));

                if (Config.getBoolean(Paths.CONFIG_INVENTORY_ENDERCHEST))
                    inventory = player.getEnderChest();
            }
        }

        if (inventory != null) {
            for (Player spec : spectators)
                spec.openInventory(inventory);
        }
    }

    @EventHandler
    public void onChestClose(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();

        if (!this.plugin.getRelation().containsValue(player))
            return;

        switch (event.getInventory().getType()) {
            case WORKBENCH:
            case STONECUTTER:
            case CARTOGRAPHY:
            case GRINDSTONE:
            case ENCHANTING:
            case COMPOSTER:
            case SMITHING:
            case CREATIVE:
            case BEACON:
            case ANVIL:
            case LOOM:
            case CRAFTING:
            case MERCHANT:
            case PLAYER: return;
            case ENDER_CHEST: {
                if (!Config.getBoolean(Paths.CONFIG_INVENTORY_ENDERCHEST))
                    return;
            }
            case BARREL:
            case BLAST_FURNACE:
            case BREWING:
            case CHEST:
            case DISPENSER:
            case DROPPER:
            case FURNACE:
            case HOPPER:
            case SMOKER:
            case SHULKER_BOX:
            case LECTERN: {
                if (!Config.getBoolean(Paths.CONFIG_INVENTORY_CONTAINERS))
                    return;
            }
        }

        Set<Player> spectators = new HashSet<>(this.plugin.getSpectators());
        spectators.removeIf(spectator -> spectator.getSpectatorTarget() == null ||
                !(spectator.getSpectatorTarget() instanceof Player)
                || !spectator.getSpectatorTarget().getUniqueId().equals(player.getUniqueId()));

        for (Player spec : spectators)
            spec.closeInventory();
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent event) {
        if (event.getCause() != PlayerTeleportEvent.TeleportCause.SPECTATE)
            return;

        Player player = event.getPlayer();

        if (player.getSpectatorTarget() == null || !(player.getSpectatorTarget() instanceof Player))
            return;

        Player target = (Player) player.getSpectatorTarget();

        if (!player.hasPermission(Permissions.COMMAND_SPECTATE_OTHERS))
            return;

        if (target.hasPermission(Permissions.BYPASS_SPECTATED)) {
            if (!player.hasPermission(Permissions.BYPASS_SPECTATEALL)) {
                player.sendMessage(Messages.getMessage(Paths.MESSAGES_GENERAL_BYPASS_INVENTORY, "TARGET", target.getName()));
                event.setCancelled(true);
                return;
            }
        }

        de.cuzim1tigaaa.spectator.player.Inventory.getInventory(player, target);
        this.plugin.getRelation().put(player, target);
    }
}