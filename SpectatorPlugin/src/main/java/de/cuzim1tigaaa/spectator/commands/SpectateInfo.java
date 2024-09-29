package de.cuzim1tigaaa.spectator.commands;

import de.cuzim1tigaaa.spectator.SpectateAPI;
import de.cuzim1tigaaa.spectator.Spectator;
import de.cuzim1tigaaa.spectator.player.PlayerAttributes;
import de.cuzim1tigaaa.spectator.player.PlayerInventory;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.generator.WorldInfo;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import java.util.*;

public class SpectateInfo implements CommandExecutor, TabCompleter {

    private final Spectator plugin;
    private final SpectateAPI spectateAPI;

    public SpectateInfo(Spectator plugin) {
        Objects.requireNonNull(plugin.getCommand("spectateinfo")).setExecutor(this);
        this.plugin = plugin;
        this.spectateAPI = plugin.getSpectateAPI();
    }

    @Override
    public boolean onCommand(@Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String label, @Nonnull String[] args) {
        if(args.length > 0) {
            switch(args[0].toLowerCase()) {
                case "list" -> spectateAPI.getSpectateInfo().forEach(info -> {
                    final String msg = " > " + info.getSpectator().getName() +
                            " " +
                            info.getState().name() +
                            " " +
                            "Target: " + (info.getTarget() == null ? "NONE" : info.getTarget().getName()) +
                            "\n" + String.join(", ", info.getAttributes().keySet().stream().map(WorldInfo::getName).toList());
                    sender.sendMessage(msg);
                });
                case "attributes", "attr" -> {
                    if(!(sender instanceof Player player))
                        return true;
                    PlayerAttributes attributes = spectateAPI.getPlayerAttributes(player);
                    if(attributes == null) {
                        sender.sendMessage("You are not spectating");
                        return true;
                    }
                    sender.sendMessage("Your attributes:");
	                final String info = String.format("%-20s: %s", "GameMode", attributes.getGameMode().name()) + "\n" +
			                String.format("%-20s: %s", "Flying", attributes.isFlying()) + "\n" +
			                String.format("%-20s: %s", "Remaining Air", attributes.getRemainingAir()) + "\n" +
			                String.format("%-20s: %s", "Fire Ticks", attributes.getFireTicks()) + "\n";

                    PlayerInventory inventory = plugin.getInventory().getPlayerInventories()
                            .stream().filter(i -> i.getPlayer().equals(player)).findFirst().orElse(null);
                    if(inventory == null) {
                        player.sendMessage("No inventory found");
                        return true;
                    }

                    Inventory inv = Bukkit.createInventory(null, 45, "Inventory of " + player.getName());
                    int index = 0;
                    for(ItemStack item : inventory.getContents()) {
                        if(item != null)
                            inv.setItem(index, item);
                        index++;
                    }
                    player.openInventory(inv);
                    player.sendMessage(info);
                }
                case "location" -> {
                    if(!(sender instanceof Player player))
                        return true;
                    Location pL = player.getLocation();
                    player.sendMessage(String.format("Your location: %d / %d / %d", pL.getBlockX(), pL.getBlockY(), pL.getBlockZ()));

                    Player target;
                    if((target = spectateAPI.getTargetOf(player)) != null) {
                        Location tL = target.getLocation();
                        player.sendMessage(String.format("Target location: %d / %d / %d", tL.getBlockX(), tL.getBlockY(), tL.getBlockZ()));
                    }
                }
            }
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(@Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String s, @Nonnull String[] args) {
        if(args.length == 1)
            return Arrays.asList("list", "attributes", "location");
        return Collections.emptyList();
    }
}