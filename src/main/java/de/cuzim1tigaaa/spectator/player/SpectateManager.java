package de.cuzim1tigaaa.spectator.player;

import de.cuzim1tigaaa.spectator.Spectator;
import de.cuzim1tigaaa.spectator.files.*;
import lombok.Getter;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.*;

public class SpectateManager {

    private final Spectator plugin;

    @Getter private final HashMap<Player, PlayerAttributes> pAttributes;
    @Getter private final Set<Player> hidden;

    public SpectateManager(Spectator plugin) {
        this.plugin = plugin;
        this.pAttributes = new HashMap<>();
        this.hidden = new HashSet<>();
        this.run();
    }

    private void run() {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            for(Map.Entry<Player, Player> entry : plugin.getRelation().entrySet()) {
                final Player player = entry.getKey();
                if(player.getGameMode() != GameMode.SPECTATOR) continue;
                final Player target = entry.getValue();

                if(player.getSpectatorTarget() == null || !player.getSpectatorTarget().equals(target) || !player.getLocation().equals(target.getLocation()))
                    if(!player.getWorld().equals(target.getWorld())) {
                        player.teleport(target, PlayerTeleportEvent.TeleportCause.PLUGIN);
                        Bukkit.getScheduler().runTaskLater(plugin, () -> player.setSpectatorTarget(target), 10);
                    }
                Inventory.updateInventory(player, target);
            }
        }, 0, 20);
    }

    public void spectate(final Player player, final Player target) {
        Set<Player> spectators = new HashSet<>(this.plugin.getSpectators());
        spectators.removeIf(p -> !this.plugin.getRelation().containsKey(p) || !this.plugin.getRelation().get(p).equals(player));
        for(Player spec : spectators)
            spectate(spec, null);

        if(!this.pAttributes.containsKey(player))
            this.pAttributes.put(player, new PlayerAttributes(player));
        player.setGameMode(GameMode.SPECTATOR);
        this.plugin.getSpectators().add(player);
        Inventory.getInventory(player, null);

        if(player.hasPermission(Permissions.UTILS_HIDE_IN_TAB) &&
                Config.getBoolean(Paths.CONFIG_HIDE_PLAYERS_TAB)) hideFromTab(player, true);
        if(target != null) {
            Inventory.getInventory(player, target);
            player.setSpectatorTarget(null);
            player.teleport(target);
            this.plugin.getRelation().remove(player);
            this.plugin.getRelation().put(player, target);
            Bukkit.getScheduler().runTaskLater(plugin, () -> player.setSpectatorTarget(target), 10);
        }
    }

    public void unSpectate(final Player player, final boolean loc) {
        Location location = null;
        if (Config.getBoolean(Paths.CONFIG_SAVE_PLAYERS_LOCATION) && !loc)
            if(this.pAttributes.containsKey(player))
                location = this.pAttributes.get(player).getLocation();
        if (location == null)
            location = player.getLocation();

        player.teleport(location, PlayerTeleportEvent.TeleportCause.PLUGIN);
        this.plugin.getSpectators().remove(player);
        this.plugin.getRelation().remove(player);
        Inventory.restoreInventory(player);
        if(this.hidden.contains(player) && player.hasPermission(Permissions.UTILS_HIDE_IN_TAB) &&
                Config.getBoolean(Paths.CONFIG_HIDE_PLAYERS_TAB)) hideFromTab(player, false);
        GameMode gameMode = null;
        boolean isFlying = false;
        if(this.pAttributes.containsKey(player)) {
            gameMode = this.pAttributes.get(player).getGameMode();
            isFlying = this.plugin.getServer().getAllowFlight() && this.pAttributes.get(player).isFlying();
            this.pAttributes.remove(player);
        }
        if(!Config.getBoolean(Paths.CONFIG_SAVE_PLAYERS_FLIGHTMODE)) isFlying = false;
        if(gameMode == null) {
            gameMode = GameMode.SURVIVAL;
            isFlying = false;
        }
        player.setGameMode(gameMode);
        player.setFlying(isFlying);
        if(plugin.getCycleHandler().isPlayerCycling(player))
            plugin.getCycleHandler().stopRunningCycle(player, true);
    }

    public void restoreAll() {
        Set<Player> spectators = new HashSet<>(this.plugin.getSpectators());
        for(Player player : spectators)
            this.unSpectate(player, false);
    }

    private void hideFromTab(final Player player, final boolean hide) {
        for(Player target : Bukkit.getOnlinePlayers()) {
            if(target.getUniqueId().equals(player.getUniqueId()))
                continue;
            if(target.hasPermission(Permissions.BYPASS_TABLIST))
                continue;
            if(hide) {
                this.hidden.add(player);
                target.hidePlayer(this.plugin, player);
                player.setMetadata("vanished", new FixedMetadataValue(this.plugin, true));
            }else {
                this.hidden.remove(player);
                target.showPlayer(this.plugin, player);
                player.removeMetadata("vanished", this.plugin);
            }
        }
    }

    public void dismountTarget(Player player) {
        if(!player.getGameMode().equals(GameMode.SPECTATOR)) return;
        player.setSpectatorTarget(null);
        player.getInventory().clear();
        this.plugin.getRelation().remove(player);
    }

}