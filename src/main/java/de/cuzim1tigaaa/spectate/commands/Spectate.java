package de.cuzim1tigaaa.spectate.commands;

import de.cuzim1tigaaa.spectate.Main;
import de.cuzim1tigaaa.spectate.files.Config;
import de.cuzim1tigaaa.spectate.files.Paths;
import de.cuzim1tigaaa.spectate.files.Permissions;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Spectate implements CommandExecutor {

    private final Main instance;

    public Spectate(Main plugin) {
        this.instance = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(sender instanceof Player) {
            Player player = (Player) sender;
            if(args.length == 0) {
                if(!player.hasPermission(Permissions.COMMAND_SPECTATE_GENERAL)) {
                    player.sendMessage(Config.getMessage(Paths.MESSAGE_DEFAULT_PERMISSION));
                    return true;
                }
                if(instance.getSpectators().contains(player)) {
                    instance.getMethods().unSpectate(player, false);
                    player.sendMessage(Config.getMessage(Paths.MESSAGES_COMMANDS_SPECTATE_LEAVE_OWN));
                    return true;
                }
                if(player.hasPermission(Permissions.COMMANDS_SPECTATE_CYCLEONLY)) {
                    player.sendMessage(Config.getMessage(Paths.MESSAGES_GENERAL_CYCLEONLY));
                    return true;
                }
                instance.getMethods().spectate(player, null);
                player.sendMessage(Config.getMessage(Paths.MESSAGES_COMMANDS_SPECTATE_JOIN_OWN));
                return true;
            }
            if(!player.hasPermission(Permissions.COMMAND_SPECTATE_OTHERS)) {
                player.sendMessage(Config.getMessage(Paths.MESSAGE_DEFAULT_PERMISSION));
                return true;
            }
            if(player.hasPermission(Permissions.COMMANDS_SPECTATE_CYCLEONLY)) {
                player.sendMessage(Config.getMessage(Paths.MESSAGES_GENERAL_CYCLEONLY));
                return true;
            }
            Player target = Bukkit.getPlayer(args[0]);
            if(target == null || !target.isOnline()) {
                player.sendMessage(Config.getMessage(Paths.MESSAGES_GENERAL_OFFLINEPLAYER, "TARGET", args[0]));
                return true;
            }
            if(target.getUniqueId().equals(player.getUniqueId())) {
                player.sendMessage(Config.getMessage(Paths.MESSAGES_GENERAL_YOURSELF));
                return true;
            }
            if(instance.getRelation().get(player) == target) {
                player.sendMessage(Config.getMessage(Paths.MESSAGES_GENERAL_SAMEPLAYER, "TARGET", target.getDisplayName()));
                return true;
            }
            if(instance.getRelation().get(target) == player || target.hasPermission(Permissions.BYPASS_SPECTATED)) {
                if(!player.hasPermission(Permissions.BYPASS_SPECTATEALL)) {
                    player.sendMessage(Config.getMessage(Paths.MESSAGES_GENERAL_BYPASS, "TARGET", target.getDisplayName()));
                    return true;
                }
            }
            instance.getMethods().spectate(player, target);
            player.sendMessage(Config.getMessage(Paths.MESSAGES_COMMANDS_SPECTATE_PLAYER, "TARGET", target.getDisplayName()));
            return true;
        }
        if(args.length > 0) {
            Player player = Bukkit.getPlayer(args[0]);
            if(player == null) {
                sender.sendMessage(Config.getMessage(Paths.MESSAGES_GENERAL_OFFLINEPLAYER, "TARGET", args[0]));
                return true;
            }
            if(player.getGameMode().equals(GameMode.SPECTATOR)) {
                instance.getMethods().unSpectate(player, false);
                player.sendMessage(Config.getMessage(Paths.MESSAGES_COMMANDS_SPECTATE_LEAVE_OWN));
                sender.sendMessage(Config.getMessage(Paths.MESSAGES_COMMANDS_SPECTATE_LEAVE_OTHER, "TARGET", player.getDisplayName()));
                return true;
            }
            instance.getMethods().spectate(player, null);
            player.sendMessage(Config.getMessage(Paths.MESSAGES_COMMANDS_SPECTATE_JOIN_OWN));
            sender.sendMessage(Config.getMessage(Paths.MESSAGES_COMMANDS_SPECTATE_JOIN_OTHER, "TARGET", player.getDisplayName()));
            return true;
        }
        return true;
    }
}