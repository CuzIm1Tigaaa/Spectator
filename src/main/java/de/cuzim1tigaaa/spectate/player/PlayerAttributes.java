package de.cuzim1tigaaa.spectate.player;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class PlayerAttributes {

    private GameMode gameMode;
    private Location location;
    private Boolean isFlying;
    private ItemStack[] inventory;
    private ItemStack[] armor;

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