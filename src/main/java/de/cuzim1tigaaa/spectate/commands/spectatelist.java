package de.cuzim1tigaaa.spectate.commands;

import de.cuzim1tigaaa.spectate.Main;
import de.cuzim1tigaaa.spectate.files.Config;
import de.cuzim1tigaaa.spectate.files.Permissions;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class spectatelist implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Main instance = Main.getInstance();
        if(sender.hasPermission(Permissions.LIST)) {
            int i = instance.getSpectators().size();
            if(instance.getSpectators().size() != 0) {
                for(Player all : instance.getSpectators()) {
                    if(all != null) {
                        if(instance.getRelation().containsKey(all) && !instance.getCycleHandler().isPlayerCycling(all)) {
                            sender.sendMessage("§7- §e" + all.getDisplayName() + " §8[§c" + instance.getRelation().get(all) + "§8]");
                        }
                        if(instance.getCycleHandler().isPlayerCycling(all)) {
                            sender.sendMessage("§7- §e" + all.getDisplayName() + " §8[§cCycle§8]");
                        }
                        sender.sendMessage("§7- §e" + all.getDisplayName());
                    }
                }
            }else {
                System.out.println("No Spectators");
                sender.sendMessage(Config.getMessage("Config.Error.nobody"));
                return true;
            }
            System.out.println("Spectators: " + i);
        }else {
            sender.sendMessage(Config.getMessage("Config.Permission"));
        }
        return true;
    }
}