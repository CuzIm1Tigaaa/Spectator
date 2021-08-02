package de.cuzim1tigaaa.spectator.commands;

import de.cuzim1tigaaa.spectator.Spectator;
import de.cuzim1tigaaa.spectator.cycle.CycleHandler;
import de.cuzim1tigaaa.spectator.files.*;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.*;

public class SpectateCycle implements CommandExecutor, TabCompleter {

    private final Spectator instance;

    public SpectateCycle(Spectator plugin) {
        Objects.requireNonNull(plugin.getCommand("spectatecycle")).setExecutor(this);
        this.instance = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!(sender instanceof Player player)) {
            sender.sendMessage(Messages.getMessage(Paths.MESSAGE_DEFAULT_SENDER));
            return true;
        }
        if(!player.hasPermission(Permissions.COMMANDS_SPECTATE_CYCLE) && !player.hasPermission(Permissions.COMMANDS_SPECTATE_CYCLEONLY)) {
            player.sendMessage(Messages.getMessage(Paths.MESSAGE_DEFAULT_PERMISSION));
            return true;
        }
        if(args.length < 1) {
            player.sendMessage(Messages.getMessage(Paths.MESSAGE_DEFAULT_SYNTAX, "USAGE", "/spectatecycle [start|stop]"));
            return true;
        }
        if(args[0].equalsIgnoreCase("start")) {
            if(args.length < 2) {
                player.sendMessage(Messages.getMessage(Paths.MESSAGE_DEFAULT_SYNTAX, "USAGE", "/spectatecycle start <Interval>"));
                return true;
            }
            if(Bukkit.getOnlinePlayers().size() <= 1) {
                player.sendMessage(Messages.getMessage(Paths.MESSAGES_GENERAL_NOPLAYERS));
                return true;
            }
            if(CycleHandler.isPlayerCycling(player)) {
                player.sendMessage(Messages.getMessage(Paths.MESSAGES_COMMANDS_CYCLE_CYCLING));
                return true;
            }
            try {
                CycleHandler.startCycle(player, Integer.parseInt(args[1]));
                return true;
            }catch(NumberFormatException exception) {
                player.sendMessage(Messages.getMessage(Paths.MESSAGES_GENERAL_NUMBERFORMAT));
                return true;
            }
        }
        if(args[0].equalsIgnoreCase("stop")) {
            Player target = player;

            if(args.length >= 2 && player.hasPermission(Permissions.COMMANDS_CYCLE_STOP_OTHERS)) {
                target = Bukkit.getPlayer(args[1]);
                if(target == null || !target.isOnline()) {
                    player.sendMessage(Messages.getMessage(Paths.MESSAGES_GENERAL_OFFLINEPLAYER, "TARGET", args[0]));
                    return true;
                }
            }

            if(!player.hasPermission(Permissions.COMMANDS_SPECTATE_CYCLE) && player.hasPermission(Permissions.COMMANDS_SPECTATE_CYCLEONLY)) {
                instance.getMethods().unSpectate(player, false);
                return true;
            }
            if(!CycleHandler.isPlayerCycling(target)) {
                String message = Messages.getMessage((player.equals(target) ? Paths.MESSAGES_COMMANDS_CYCLE_NOT_CYCLING :
                        Paths.MESSAGES_COMMANDS_CYCLE_TARGET_NOT_CYCLING), "TARGET", target.getDisplayName());
                player.sendMessage(message);
                return true;
            }
            CycleHandler.stopCycle(target);
            target.sendMessage(Messages.getMessage(Paths.MESSAGES_COMMANDS_CYCLE_STOP));
            return true;
        }
        player.sendMessage(Messages.getMessage(Paths.MESSAGE_DEFAULT_SYNTAX, "USAGE", "/spectatecycle [start|stop]"));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String s, String[] args) {
        final List<String> tab = new ArrayList<>();
        if(args.length == 1) { tab.add("start"); tab.add("stop"); }
        if(args.length == 2 && args[0].equalsIgnoreCase("stop")) {
            for(Player all : Bukkit.getOnlinePlayers()) tab.add(all.getDisplayName());
        }
        return tab;
    }
}