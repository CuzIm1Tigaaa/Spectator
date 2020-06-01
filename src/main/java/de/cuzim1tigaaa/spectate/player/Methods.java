package de.cuzim1tigaaa.spectate.player;

import de.cuzim1tigaaa.spectate.Main;
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

    public Methods() {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(Main.getInstance(), () -> {
            for(Map.Entry<Player, Player> entry : Main.getInstance().getRelation().entrySet()) {
                final Player player = entry.getKey();
                final Player target = entry.getValue();

                Inventory.updateInventory(player, target);

                if(!player.getWorld().equals(target.getWorld()) || player.getLocation().distanceSquared(target.getLocation()) > 1) {
                    player.setSpectatorTarget(null);
                    Bukkit.getScheduler().runTaskLater(Main.getInstance(), () -> player.setSpectatorTarget(target), 5);
                }
            }
        }, 0, 10);
    }

    public void spectate(Player player, Player target) {
        if(!playerAttributes.containsKey(player)) {
            playerAttributes.put(player, new PlayerAttributes(player));
        }
        player.setGameMode(GameMode.SPECTATOR);
        Main.getInstance().getSpectators().add(player);
        player.getInventory().clear();
        player.updateInventory();
        if(player.hasPermission(Permissions.TAB) && Config.hideTab) {
            HideFromTab(player, true);
        }
        if(target != null) {
            if(player.hasPermission(Permissions.INVENTORY) && Config.mirrorInventory) {
                Inventory.getInventory(player, target);
            }
            player.setSpectatorTarget(null);
            Main.getInstance().getRelation().remove(player);
            Main.getInstance().getRelation().put(player, target);
            player.setSpectatorTarget(target);
        }
    }
    public void unSpectate(Player player, boolean locate) {
        Location location = null;

        GameMode gameMode;
        Boolean isFlying;
        
        float yaw;
        float pitch;

        if(playerAttributes.containsKey(player)) {
            gameMode = playerAttributes.get(player).getGameMode();
            isFlying = playerAttributes.get(player).getFlying();
        }else {
            gameMode = GameMode.SURVIVAL;
            isFlying = false;
        }
        if(Config.saveLocation) {
            location = playerAttributes.get(player).getLocation();
        }
        if(location == null || (player.hasPermission(Permissions.HERE) && locate)) {
            location = player.getLocation();
        }
        yaw = location.getYaw();
        pitch = location.getPitch();
        location.setYaw(yaw);
        location.setPitch(pitch);
        Main.getInstance().getSpectators().remove(player);
        Main.getInstance().getRelation().remove(player);
        if(hidden.contains(player) && player.hasPermission(Permissions.TAB) && Config.hideTab) {
            HideFromTab(player, false);
        }
        player.teleport(location, PlayerTeleportEvent.TeleportCause.PLUGIN);
        Inventory.restoreInventory(player);
        playerAttributes.remove(player);
        player.setGameMode(gameMode);
        player.setFlying(isFlying);
    }
    public void restoreAll() {
        for(Player all : Main.getInstance().getSpectators()) {
            unSpectate(all, false);
        }
    }

    public void HideFromTab(Player player, boolean hide) {
        for(Player target : Bukkit.getOnlinePlayers()) {
            if(!target.getUniqueId().equals(player.getUniqueId())) {
                if(!target.hasPermission(Permissions.TABLIST)) {
                    if(hide) {
                        hidden.add(player);
                        target.hidePlayer(Main.getInstance(), player);

                    }else {
                        hidden.remove(player);
                        target.showPlayer(Main.getInstance(), player);
                    }
                }
            }
        }
    }

    public HashMap<Player, PlayerAttributes> getPlayerAttributes() {
        return playerAttributes;
    }
    public Set<Player> getHidden() {
        return hidden;
    }
}