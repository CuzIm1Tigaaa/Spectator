package de.cuzim1tigaaa.spectate.player;

import de.cuzim1tigaaa.spectate.Main;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class Inventory {

    private static final Main instance = Main.getInstance();

    public static void getInventory(Player player, Player target) {
        player.getInventory().clear();
        if(target != null) {
            player.getInventory().setContents(target.getInventory().getContents());
            player.getInventory().setArmorContents(target.getInventory().getArmorContents());
        }
        player.updateInventory();
    }
    public static void updateInventory(Player player, Player target) {
        player.getInventory().clear();
        player.getInventory().setContents(target.getInventory().getContents());
        player.getInventory().setArmorContents(target.getInventory().getArmorContents());
        player.updateInventory();
    }
    public static void restoreInventory(Player player) {
        if(instance.getMethods().getPlayerAttributes().containsKey(player)) {
            player.getInventory().clear();
            ItemStack[] inventory = instance.getMethods().getPlayerAttributes().get(player).getInventory();
            ItemStack[] armor = instance.getMethods().getPlayerAttributes().get(player).getArmor();
            if(inventory != null) player.getInventory().setContents(inventory);
            if(armor != null) player.getInventory().setArmorContents(armor);
        }
        player.updateInventory();
    }
    public static void restoreAll() { if(!instance.getMethods().getPlayerAttributes().isEmpty()) for(Player all : instance.getMethods().getPlayerAttributes().keySet()) if(all.isOnline()) restoreInventory(all); }
}