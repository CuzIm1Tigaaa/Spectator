package de.cuzim1tigaaa.spectator.commands;

import de.cuzim1tigaaa.spectator.Main;
import de.cuzim1tigaaa.spectator.cycle.CycleHandler;
import de.cuzim1tigaaa.spectator.files.*;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

public class SpectateCycle implements CommandExecutor {

    private final Main instance;

    public SpectateCycle(Main plugin) {
        this.instance = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!(sender instanceof Player)) {
            sender.sendMessage(Config.getMessage(Paths.MESSAGE_DEFAULT_SENDER));
            return true;
        }
        Player player = (Player) sender;
        if(!player.hasPermission(Permissions.COMMANDS_SPECTATE_CYCLE) && !player.hasPermission(Permissions.COMMANDS_SPECTATE_CYCLEONLY)) {
            player.sendMessage(Config.getMessage(Paths.MESSAGE_DEFAULT_PERMISSION));
            return true;
        }
        if(args.length < 1) {
            player.sendMessage(Config.getMessage(Paths.MESSAGE_DEFAULT_SYNTAX, "USAGE", "/spectatecycle [start|stop]"));
            return true;
        }
        if(args[0].equalsIgnoreCase("start")) {
            if(args.length < 2) {
                player.sendMessage(Config.getMessage(Paths.MESSAGE_DEFAULT_SYNTAX, "USAGE", "/spectatecycle start <Interval>"));
                return true;
            }
            if(Bukkit.getOnlinePlayers().size() <= 1) {
                player.sendMessage(Config.getMessage(Paths.MESSAGES_GENERAL_NOPLAYERS));
                return true;
            }
            if(CycleHandler.isPlayerCycling(player)) {
                player.sendMessage(Config.getMessage(Paths.MESSAGES_COMMANDS_CYCLE_CYCLING));
                return true;
            }
            try {
                CycleHandler.startCycle(player, Integer.parseInt(args[1]));
                return true;
            }catch(NumberFormatException exception) {
                player.sendMessage(Config.getMessage(Paths.MESSAGES_GENERAL_NUMBERFORMAT));
                return true;
            }
        }
        if(args[0].equalsIgnoreCase("stop")) {
            if(!player.hasPermission(Permissions.COMMANDS_SPECTATE_CYCLE) && player.hasPermission(Permissions.COMMANDS_SPECTATE_CYCLEONLY)) {
                instance.getMethods().unSpectate(player, false);
                return true;
            }
            if(!CycleHandler.isPlayerCycling(player)) {
                player.sendMessage(Config.getMessage(Paths.MESSAGES_COMMANDS_CYCLE_NOTCYCLING));
                return true;
            }
            CycleHandler.stopCycle(player);
            player.sendMessage(Config.getMessage(Paths.MESSAGES_COMMANDS_CYCLE_STOP));
            return true;
        }
        player.sendMessage(Config.getMessage(Paths.MESSAGE_DEFAULT_SYNTAX, "USAGE", "/spectatecycle [start|stop]"));
        return true;
    }
}