package de.cuzim1tigaaa.spectate.player;

import de.cuzim1tigaaa.spectate.Main;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class Inventory {

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
        if(Main.getInstance().getMethods().getPlayerAttributes().containsKey(player)) {
            player.getInventory().clear();
            ItemStack[] inventory = Main.getInstance().getMethods().getPlayerAttributes().get(player).getInventory();
            ItemStack[] armor = Main.getInstance().getMethods().getPlayerAttributes().get(player).getArmor();
            if(inventory != null) {
                player.getInventory().setContents(inventory);
            }
            if(armor != null) {
                player.getInventory().setArmorContents(armor);
            }
        }
        player.updateInventory();
    }
    public static void restoreAll() {
        if(!Main.getInstance().getMethods().getPlayerAttributes().isEmpty()) {
            for(Player all : Main.getInstance().getMethods().getPlayerAttributes().keySet()) {
                if(all.isOnline()) {
                    restoreInventory(all);
                }
            }
        }
    }
}