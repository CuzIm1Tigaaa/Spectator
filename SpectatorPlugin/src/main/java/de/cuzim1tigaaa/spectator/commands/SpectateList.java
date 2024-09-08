package de.cuzim1tigaaa.spectator.commands;

import de.cuzim1tigaaa.spectator.Spectator;
import de.cuzim1tigaaa.spectator.files.*;
import de.cuzim1tigaaa.spectator.spectate.SpectateUtils;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.*;

public class SpectateList implements CommandExecutor, TabCompleter {

	private final SpectateUtils spectateUtils;

    public SpectateList(Spectator plugin) {
        Objects.requireNonNull(plugin.getCommand("spectatelist")).setExecutor(this);
	    this.spectateUtils = plugin.getSpectateUtils();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!Permissions.hasPermission(sender, Permissions.COMMANDS_SPECTATE_LIST)) {
            sender.sendMessage(Messages.getMessage(sender, Paths.MESSAGE_DEFAULT_PERMISSION));
            return true;
        }

        int spectators = spectateUtils.getSpectators().size();
        if(spectators == 0) {
            sender.sendMessage(Messages.getMessage(sender, Paths.MESSAGES_COMMANDS_LIST_NONE));
            return true;
        }

        sender.sendMessage(Messages.getMessage(sender, Paths.MESSAGES_COMMANDS_LIST_TITLE, "AMOUNT", spectators));

        for(Player spectator : spectateUtils.getSpectators()) {
            if(spectator == null || !spectator.isOnline()) continue;
            Player target = spectateUtils.getTargetOf(spectator);

            if(target == null || !target.isOnline()) {
                sender.sendMessage(Messages.getMessage(sender, Paths.MESSAGES_COMMANDS_LIST_DEFAULT, "SPECTATOR", spectator.getName()));
                continue;
            }

            boolean cycling = spectateUtils.isCycling(spectator);
            boolean paused = spectateUtils.isPaused(spectator);

            String message;
            if(paused) message = Paths.MESSAGES_COMMANDS_LIST_PAUSED;
            else if(cycling) message = Paths.MESSAGES_COMMANDS_LIST_CYCLING;
            else message = Paths.MESSAGES_COMMANDS_LIST_SPECTATING;

            sender.sendMessage(Messages.getMessage(sender, message, "SPECTATOR", spectator.getName(),
                    "TARGET", spectateUtils.getTargetOf(spectator).getName()));
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String s, String[] args) {
        return Collections.emptyList();
    }
}