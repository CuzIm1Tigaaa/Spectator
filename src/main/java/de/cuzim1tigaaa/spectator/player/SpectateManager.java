package de.cuzim1tigaaa.spectator.player;

import de.cuzim1tigaaa.spectator.SpectatorPlugin;
import de.cuzim1tigaaa.spectator.cycle.CycleHandler;
import de.cuzim1tigaaa.spectator.files.Config;
import de.cuzim1tigaaa.spectator.files.Paths;
import de.cuzim1tigaaa.spectator.files.Permissions;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class SpectateManager {

    private final SpectatorPlugin plugin;
    private final Map<Player, PlayerAttributes> playerAttributes = new HashMap<>();
    private final Set<Player> hidden = new HashSet<>();

    public SpectateManager(SpectatorPlugin plugin) {
        this.plugin = plugin;

        Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            for (Map.Entry<Player, Player> entry : plugin.getRelation().entrySet()) {
                final Player player = entry.getKey();
                final Player target = entry.getValue();

                Inventory.updateInventory(player, target);

                if (!player.getWorld().equals(target.getWorld()) || player.getLocation().distanceSquared(target.getLocation()) > 1) {
                    player.setSpectatorTarget(null);
                    player.setSpectatorTarget(target);
                }
            }
        }, 0, 20);
    }

    public Map<Player, PlayerAttributes> getPAttributes() {
        return playerAttributes;
    }

    public Set<Player> getHidden() {
        return hidden;
    }

    public void spectate(final Player player, final Player target) {
        Set<Player> spectators = new HashSet<>(this.plugin.getSpectators());
        spectators.removeIf(p -> !this.plugin.getRelation().containsKey(p) || !this.plugin.getRelation().get(p).equals(player));

        for (Player spec : spectators)
            spectate(spec, null);

        if (!this.playerAttributes.containsKey(player))
            this.playerAttributes.put(player, new PlayerAttributes(player));

        player.setGameMode(GameMode.SPECTATOR);
        this.plugin.getSpectators().add(player);
        Inventory.getInventory(player, null);

        if (player.hasPermission(Permissions.UTILS_HIDE_IN_TAB) && Config.getBoolean(Paths.CONFIG_HIDE_PLAYERS_TAB))
            hideFromTab(player, true);

        if (target != null) {
            Inventory.getInventory(player, target);
            player.setSpectatorTarget(null);
            this.plugin.getRelation().remove(player);
            this.plugin.getRelation().put(player, target);
            player.setSpectatorTarget(target);
        }
    }

    public void unSpectate(final Player player, final boolean loc) {
        Location location = null;

        if (Config.getBoolean(Paths.CONFIG_SAVE_PLAYERS_LOCATION) && !loc && this.playerAttributes.containsKey(player))
            location = this.playerAttributes.get(player).getLocation();

        if (location == null) {
            location = player.getLocation();
            float pitch = location.getPitch();
            float yaw = location.getYaw();
            player.teleport(location);
            location.setPitch(pitch);
            location.setYaw(yaw);
        }

        player.teleport(location, PlayerTeleportEvent.TeleportCause.PLUGIN);
        this.plugin.getSpectators().remove(player);
        this.plugin.getRelation().remove(player);
        Inventory.restoreInventory(player);

        if (this.hidden.contains(player) && player.hasPermission(Permissions.UTILS_HIDE_IN_TAB) && Config.getBoolean(Paths.CONFIG_HIDE_PLAYERS_TAB))
            hideFromTab(player, false);

        GameMode gameMode = null;
        boolean isFlying = false;

        if (this.playerAttributes.containsKey(player)) {
            gameMode = this.playerAttributes.get(player).getGameMode();
            isFlying = this.plugin.getServer().getAllowFlight() && this.playerAttributes.get(player).getFlying();
            this.playerAttributes.remove(player);
        }

        if (!Config.getBoolean(Paths.CONFIG_SAVE_PLAYERS_FLIGHTMODE))
            isFlying = false;

        if (gameMode == null) {
            gameMode = GameMode.SURVIVAL;
            isFlying = false;
        }

        player.setGameMode(gameMode);
        player.setFlying(isFlying);

        if (CycleHandler.isPlayerCycling(player))
            CycleHandler.breakCycle(player, true);
    }

    public void restoreAll() {
        Set<Player> spectators = new HashSet<>(this.plugin.getSpectators());

        for (Player player : spectators)
            this.unSpectate(player, false);
    }

    private void hideFromTab(final Player player, final boolean hide) {
        for (Player target : Bukkit.getOnlinePlayers()) {
            if (target.getUniqueId().equals(player.getUniqueId()) || target.hasPermission(Permissions.BYPASS_TABLIST))
                continue;

            if (hide) {
                this.hidden.add(player);
                target.hidePlayer(this.plugin, player);
                player.setMetadata("vanished", new FixedMetadataValue(this.plugin, true));
            } else {
                this.hidden.remove(player);
                target.showPlayer(this.plugin, player);
                player.removeMetadata("vanished", this.plugin);
            }
        }
    }

    public void dismountTarget(Player player) {
        if (!player.getGameMode().equals(GameMode.SPECTATOR))
            return;

        this.plugin.getRelation().remove(player);
        player.getInventory().clear();
        player.setSpectatorTarget(null);
    }
}