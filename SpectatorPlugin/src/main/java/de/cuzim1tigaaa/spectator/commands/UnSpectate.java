package de.cuzim1tigaaa.spectator.commands;

import de.cuzim1tigaaa.spectator.Spectator;
import de.cuzim1tigaaa.spectator.files.Messages;
import de.cuzim1tigaaa.spectator.files.Paths;
import de.cuzim1tigaaa.spectator.spectate.SpectateUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.*;

import static de.cuzim1tigaaa.spectator.files.Permissions.*;

public class UnSpectate implements CommandExecutor, TabCompleter {

    private final Spectator plugin;
    private final SpectateUtils spectateUtils;

    public UnSpectate(Spectator plugin) {
        Objects.requireNonNull(plugin.getCommand("unspectate")).setExecutor(this);
        this.plugin = plugin;
        this.spectateUtils = plugin.getSpectateUtils();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!hasPermission(sender, COMMAND_UNSPECTATE)) {
            sender.sendMessage(Messages.getMessage(sender, Paths.MESSAGE_DEFAULT_PERMISSION));
            return true;
        }

        if(spectateUtils.getSpectators().isEmpty()) {
            sender.sendMessage(Messages.getMessage(sender, Paths.MESSAGES_COMMANDS_LIST_NONE));
            return true;
        }

        if(args.length == 0) {
            for(Player spectator : spectateUtils.getSpectators()) {
                if(spectator.hasPermission(BYPASS_UNSPECTATED))
                    continue;
                plugin.getSpectateUtils().unspectate(spectator, true);
                if(spectator.equals(sender)) continue;
                spectator.sendMessage(Messages.getMessage(spectator, Paths.MESSAGES_COMMANDS_SPECTATE_LEAVE_OWN));
            }
            sender.sendMessage(Messages.getMessage(sender, Paths.MESSAGES_COMMANDS_UNSPECTATE_ALL));
            return true;
        }

        boolean oldLocation = true;
        if(args.length > 1)
            oldLocation = Boolean.parseBoolean(args[1]);

        if(args[0].equalsIgnoreCase("*")) {
            for(Player spectator : spectateUtils.getSpectators()) {
                if(spectator.hasPermission(BYPASS_UNSPECTATED))
                    continue;
                plugin.getSpectateUtils().unspectate(spectator, oldLocation);
                if(spectator.equals(sender)) continue;
                spectator.sendMessage(Messages.getMessage(spectator, Paths.MESSAGES_COMMANDS_SPECTATE_LEAVE_OWN));
            }
            sender.sendMessage(Messages.getMessage(sender, Paths.MESSAGES_COMMANDS_UNSPECTATE_ALL));
            return true;
        }
        Player target = Bukkit.getPlayer(args[0]);
        if(target == null || !target.isOnline()) {
            sender.sendMessage(Messages.getMessage(sender, Paths.MESSAGES_GENERAL_OFFLINEPLAYER, "TARGET", args[0]));
            return true;
        }

        if(!spectateUtils.isSpectator(target)) {
            sender.sendMessage(Messages.getMessage(sender, Paths.MESSAGES_GENERAL_NOTSPECTATING, "TARGET", target.getName()));
            return true;
        }
        if(target.hasPermission(BYPASS_UNSPECTATED))
            return true;

        spectateUtils.unspectate(target, oldLocation);
        target.sendMessage(Messages.getMessage(target, Paths.MESSAGES_COMMANDS_SPECTATE_LEAVE_OWN));
        sender.sendMessage(Messages.getMessage(sender, Paths.MESSAGES_COMMANDS_UNSPECTATE_PLAYER, "TARGET", target.getName()));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String s, String[] args) {
        if(args.length == 1) {
            List<String> names = plugin.getOnlinePlayerNames();
            names.add("*");
            return names;
        }
        if(args.length == 2)
            return List.of("true", "false");

        return Collections.emptyList();
    }
}