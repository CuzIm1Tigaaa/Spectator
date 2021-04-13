package de.cuzim1tigaaa.spectate.player;

import de.cuzim1tigaaa.spectate.Main;
import de.cuzim1tigaaa.spectate.cycle.CycleHandler;
import de.cuzim1tigaaa.spectate.files.Config;
import de.cuzim1tigaaa.spectate.files.Permissions;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Methods {

    private final Set<Player> hidden = new HashSet<>();
    private final HashMap<Player, PlayerAttributes> playerAttributes = new HashMap<>();
    private final Main instance = Main.getInstance();

    public Methods() {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(instance, () -> {
            for(Map.Entry<Player, Player> entry : instance.getRelation().entrySet()) {
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

    public void spectate(final Player player, final Player target) {
        if(!playerAttributes.containsKey(player)) playerAttributes.put(player, new PlayerAttributes(player));
        player.setGameMode(GameMode.SPECTATOR);
        instance.getSpectators().add(player);
        player.getInventory().clear();
        player.updateInventory();
        if(player.hasPermission(Permissions.UTILS_HIDE_IN_TAB) && Config.hideTab) hideFromTab(player, true);
        if(target != null) {
            if(player.hasPermission(Permissions.UTILS_MIRROR_INVENTORY) && Config.mirrorInventory) Inventory.getInventory(player, target);
            player.setSpectatorTarget(null);
            instance.getRelation().remove(player);
            instance.getRelation().put(player, target);
            player.setSpectatorTarget(target);
        }
    }
    public void unSpectate(final Player player, final boolean loc) {
        Location location = null;
        if (Config.saveLocation && !loc) {
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
        instance.getSpectators().remove(player);
        instance.getRelation().remove(player);
        Inventory.restoreInventory(player);
        if(hidden.contains(player) && player.hasPermission(Permissions.UTILS_HIDE_IN_TAB) && Config.hideTab) hideFromTab(player, false);
        GameMode gameMode = null;
        Boolean isFlying = false;
        if(playerAttributes.containsKey(player)) {
            gameMode = playerAttributes.get(player).getGameMode();
            isFlying = playerAttributes.get(player).getFlying();
            playerAttributes.remove(player);
        }
        if(!Config.saveFlying) isFlying = false;
        if(gameMode == null) {
            gameMode = GameMode.SURVIVAL;
            isFlying = false;
        }
        player.setGameMode(gameMode);
        player.setFlying(isFlying);
        if(CycleHandler.isPlayerCycling(player)) CycleHandler.stopCycle(player);
    }
    public void restoreAll() { for(Player all : instance.getSpectators()) unSpectate(all, false); }

    public void hideFromTab(final Player player, final boolean hide) {
        for(Player target : Bukkit.getOnlinePlayers()) {
            if(target.getUniqueId().equals(player.getUniqueId())) continue;
            if(target.hasPermission(Permissions.BYPASS_TABLIST)) continue;
            if(hide) {
                hidden.add(player);
                target.hidePlayer(instance, player);
            }else {
                hidden.remove(player);
                target.showPlayer(instance, player);
            }
        }
    }

    public HashMap<Player, PlayerAttributes> getPlayerAttributes() { return playerAttributes; }
    public Set<Player> getHidden() { return hidden; }





    public void Methode() {

    }




}