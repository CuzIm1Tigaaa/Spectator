package de.cuzim1tigaaa.spectate.commands;

import de.cuzim1tigaaa.spectate.Main;
import de.cuzim1tigaaa.spectate.files.Config;
import de.cuzim1tigaaa.spectate.files.Paths;
import de.cuzim1tigaaa.spectate.files.Permissions;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class UnSpectate implements CommandExecutor {

    private final Main instance;

    public UnSpectate(Main plugin) {
        this.instance = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!sender.hasPermission(Permissions.COMMAND_UNSPECTATE)) {
            sender.sendMessage(Config.getMessage(Paths.MESSAGE_DEFAULT_PERMISSION));
            return true;
        }
        if(args.length < 1) {
            if(instance.getSpectators().size() <= 0) {
                sender.sendMessage(Config.getMessage(Paths.MESSAGES_COMMANDS_LIST_NONE));
                return true;
            }
            instance.getMethods().restoreAll();
            for(Player player : instance.getSpectators()) if(!player.equals(sender)) player.sendMessage(Config.getMessage(Paths.MESSAGES_COMMANDS_SPECTATE_LEAVE_OWN));
            sender.sendMessage(Config.getMessage(Paths.MESSAGES_COMMANDS_UNSPECTATE_ALL));
            return true;
        }
        Player target = Bukkit.getPlayer(args[0]);
        if(target == null) {
            sender.sendMessage(Config.getMessage(Paths.MESSAGES_GENERAL_OFFLINEPLAYER, "TARGET", args[0]));
            return true;
        }
        if(!instance.getSpectators().contains(target)) {
            sender.sendMessage(Config.getMessage(Paths.MESSAGES_GENERAL_NOTSPECTATING, "TARGET", target.getDisplayName()));
            return true;
        }
        boolean useCurrentLocation = false;
        if(args.length > 1) useCurrentLocation = Boolean.parseBoolean(args[1]);

        instance.getMethods().unSpectate(target, useCurrentLocation);
        target.sendMessage(Config.getMessage(Paths.MESSAGES_COMMANDS_SPECTATE_LEAVE_OWN));
        sender.sendMessage(Config.getMessage(Paths.MESSAGES_COMMANDS_UNSPECTATE_PLAYER, "TARGET", target.getDisplayName()));
        return true;
    }
}