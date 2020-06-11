package de.cuzim1tigaaa.spectate.commands;

import de.cuzim1tigaaa.spectate.Main;
import de.cuzim1tigaaa.spectate.files.Config;
import de.cuzim1tigaaa.spectate.files.Permissions;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class spectatecycle implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(sender instanceof Player) {
            Player player = (Player) sender;
            if(player.hasPermission(Permissions.CYCLE) || player.hasPermission(Permissions.CYCLEONLY)) {
                if(args.length > 0) {
                    if(args[0].equalsIgnoreCase("start")) {
                        if(args.length == 2) {
                            if(!Main.getInstance().getCycleHandler().isPlayerCycling(player)) {
                                try {
                                    int interval = Integer.parseInt(args[1]);
                                    Main.getInstance().getCycleHandler().startCycle(player, interval * 20);
                                    return true;
                                }catch(NumberFormatException exception) {
                                    return false;
                                }
                            }else {
                                player.sendMessage(Config.getMessage("Config.Spectate.Cycle.running"));
                                return true;
                            }
                        }else {
                            player.sendMessage("§cUsage: /spectatecycle start <interval>");
                            return true;
                        }
                    }else if(args[0].equalsIgnoreCase("stop") && !player.hasPermission(Permissions.CYCLEONLY)) {
                        if(Main.getInstance().getCycleHandler().isPlayerCycling(player)) {
                            Main.getInstance().getCycleHandler().stopCycle(player);
                            player.sendMessage(Config.getMessage("Config.Spectate.Cycle.stop"));
                        }else {
                            player.sendMessage(Config.getMessage("Config.Spectate.Cycle.notRunning"));
                        }
                        return true;
                        /*
                    }else if(args[0].equalsIgnoreCase("pause")) {
                        if(Main.getInstance().getCycleHandler().isPlayerCycling(player)) {
                            Main.getInstance().getCycleHandler().pauseCycle(player);
                            player.sendMessage(Config.getMessage("Config.Spectate.Cycle.pause"));
                        }else {
                            player.sendMessage(Config.getMessage("Config.Spectate.Cycle.notRunning"));
                        }
                        return true;
                    }else if(args[0].equalsIgnoreCase("resume")) {
                        if(Main.getInstance().getCycleHandler().isPlayerPaused(player)) {
                            Main.getInstance().getCycleHandler().resumeCycle(player);
                        }else {
                            player.sendMessage(Config.getMessage("Config.Spectate.Cycle.notPaused"));
                        }
                        return true;
                        */
                    }else if(player.hasPermission(Permissions.CYCLEONLY)) {
                        player.sendMessage(Config.getMessage("Config.Error.cycleStop"));
                    }else {
                        return false;
                    }
                }
                return false;
            }else player.sendMessage(Config.getMessage("Config.Permission"));
        }else sender.sendMessage(Config.getMessage("Config.Error.isNot"));
        return true;
    }
}