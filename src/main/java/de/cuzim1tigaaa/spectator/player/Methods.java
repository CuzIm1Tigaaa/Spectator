package de.cuzim1tigaaa.spectator.player;

import de.cuzim1tigaaa.spectator.Spectator;
import de.cuzim1tigaaa.spectator.cycle.CycleHandler;
import de.cuzim1tigaaa.spectator.files.Paths;
import de.cuzim1tigaaa.spectator.files.Permissions;
import org.bukkit.*;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.*;

public class Methods {

    private final Spectator plugin = Spectator.getPlugin();

    private final Set<Player> hidden = new HashSet<>();
    public Methods() {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            for(Map.Entry<Player, Player> entry : plugin.getRelation().entrySet()) {
                final Player player = entry.getKey();
                final Player target = entry.getValue();
                Inventory.updateInventory(player, target);
                if(!player.getWorld().equals(target.getWorld()) || player.getLocation().distanceSquared(target.getLocation()) > 1) {
                    player.setSpectatorTarget(null);
                    player.setSpectatorTarget(target);
                }
            }
        }, 0, 20);
    }
    private final HashMap<Player, PlayerAttributes> playerAttributes = new HashMap<>();

    public Set<Player> getHidden() { return hidden; }

    public HashMap<Player, PlayerAttributes> getPlayerAttributes() { return playerAttributes; }

    public void spectate(final Player player, final Player target) {
        if(!playerAttributes.containsKey(player)) playerAttributes.put(player, new PlayerAttributes(player));
        player.setGameMode(GameMode.SPECTATOR);
        plugin.getSpectators().add(player);
        Inventory.getInventory(player, null);

        if(player.hasPermission(Permissions.UTILS_HIDE_IN_TAB) && this.plugin.getConfiguration().getBoolean(Paths.CONFIG_HIDE_PLAYERS_TAB)) hideFromTab(player, true);
        player.setMetadata("vanished", new FixedMetadataValue(Spectator.getPlugin(), true));
        if(target != null) {
            Inventory.getInventory(player, target);
            player.setSpectatorTarget(null);
            plugin.getRelation().remove(player);
            plugin.getRelation().put(player, target);
            player.setSpectatorTarget(target);
        }
    }
    public void unSpectate(final Player player, final boolean loc) {
        Location location = null;
        if (this.plugin.getConfiguration().getBoolean(Paths.CONFIG_SAVE_PLAYERS_LOCATION) && !loc) {
            if(playerAttributes.containsKey(player)) {
                location = playerAttributes.get(player).getLocation();
            }
        }
        if (location == null) {
            location = player.getLocation();
            float pitch = location.getPitch();
            float yaw = location.getYaw();
            player.teleport(location);
            location.setPitch(pitch);
            location.setYaw(yaw);
        }
        player.teleport(location, PlayerTeleportEvent.TeleportCause.PLUGIN);
        plugin.getSpectators().remove(player);
        plugin.getRelation().remove(player);
        Inventory.restoreInventory(player);
        if(hidden.contains(player) && player.hasPermission(Permissions.UTILS_HIDE_IN_TAB) &&
                this.plugin.getConfiguration().getBoolean(Paths.CONFIG_HIDE_PLAYERS_TAB)) hideFromTab(player, false);
        GameMode gameMode = null;
        boolean isFlying = false;
        if(playerAttributes.containsKey(player)) {
            gameMode = playerAttributes.get(player).getGameMode();
            isFlying = this.plugin.getServer().getAllowFlight() && playerAttributes.get(player).getFlying();
            playerAttributes.remove(player);
        }
        if(!this.plugin.getConfiguration().getBoolean(Paths.CONFIG_SAVE_PLAYERS_FLIGHTMODE)) isFlying = false;
        if(gameMode == null) {
            gameMode = GameMode.SURVIVAL;
            isFlying = false;
        }
        player.setGameMode(gameMode);
        player.setFlying(isFlying);
        player.removeMetadata("vanished", Spectator.getPlugin());
        if(CycleHandler.isPlayerCycling(player)) CycleHandler.stopCycle(player);
    }
    public void restoreAll() {
        Set<Player> spectators = new HashSet<>(plugin.getSpectators());
        for(Player player : spectators) this.unSpectate(player, false);
    }

    private void hideFromTab(final Player player, final boolean hide) {
        for(Player target : Bukkit.getOnlinePlayers()) {
            if(target.getUniqueId().equals(player.getUniqueId())) continue;
            if(target.hasPermission(Permissions.BYPASS_TABLIST)) continue;
            if(hide) {
                hidden.add(player);
                target.hidePlayer(plugin, player);
            }else {
                hidden.remove(player);
                target.showPlayer(plugin, player);
            }
        }
    }

    public void dismountTarget(Player player) {
        if(!player.getGameMode().equals(GameMode.SPECTATOR)) return;
        if(player.getSpectatorTarget() == null || !player.getSpectatorTarget().getType().equals(EntityType.PLAYER)) return;
        plugin.getRelation().remove(player);
        player.getInventory().clear();
        player.setSpectatorTarget(null);
    }

}