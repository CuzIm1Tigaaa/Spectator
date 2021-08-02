package de.cuzim1tigaaa.spectator.player;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import java.util.HashSet;
import java.util.Set;

public class PlayerAttributes {

    private final GameMode gameMode;
    private final Location location;
    private final Boolean isFlying;
    private final ItemStack[] playerInventory;
    private final Set<PotionEffect> effects;

    public PlayerAttributes(Player player) {
        gameMode = player.getGameMode();
        location = player.getLocation();
        isFlying = player.isFlying();
        playerInventory = player.getInventory().getContents();
        effects = new HashSet<>(player.getActivePotionEffects());
    }

    public GameMode getGameMode() { return gameMode; }
    public Location getLocation() { return location; }
    public Boolean getFlying() { return isFlying; }
    public ItemStack[] getPlayerInventory() { return playerInventory; }
    public Set<PotionEffect> getEffects() { return effects; }
}