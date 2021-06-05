package de.cuzim1tigaaa.spectator.player;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class PlayerAttributes {

    private final GameMode gameMode;
    private final Location location;
    private final Boolean isFlying;
    private final ItemStack[] inventory;
    private final ItemStack[] armor;

    public PlayerAttributes(Player player) {
        gameMode = player.getGameMode();
        location = player.getLocation();
        isFlying = player.isFlying();
        inventory = player.getInventory().getContents();
        armor = player.getInventory().getArmorContents();
    }

    public GameMode getGameMode() {
        return gameMode;
    }
    public Location getLocation() {
        return location;
    }
    public Boolean getFlying() {
        return isFlying;
    }
    public ItemStack[] getInventory() {
        return inventory;
    }
    public ItemStack[] getArmor() {
        return armor;
    }
}