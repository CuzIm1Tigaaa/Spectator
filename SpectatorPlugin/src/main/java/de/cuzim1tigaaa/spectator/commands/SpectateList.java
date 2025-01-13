package de.cuzim1tigaaa.spectator.commands;

import de.cuzim1tigaaa.spectator.SpectateAPI;
import de.cuzim1tigaaa.spectator.Spectator;
import de.cuzim1tigaaa.spectator.files.*;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import java.util.*;

public class SpectateList implements CommandExecutor, TabCompleter {

    private final SpectateAPI spectateAPI;

    public SpectateList(Spectator plugin) {
        Objects.requireNonNull(plugin.getCommand("spectatelist")).setExecutor(this);
        this.spectateAPI = plugin.getSpectateAPI();
    }

    @Override
    public boolean onCommand(@Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String label, @Nonnull String[] args) {
        if(!Permissions.hasPermission(sender, Permissions.COMMANDS_SPECTATE_LIST)) {
            Messages.sendMessage(sender, Paths.MESSAGE_DEFAULT_PERMISSION);
            return true;
        }

        int spectators = spectateAPI.getSpectators().size();
        if(spectators == 0) {
            Messages.sendMessage(sender, Paths.MESSAGES_COMMANDS_LIST_NONE);
            return true;
        }

        Messages.sendMessage(sender, Paths.MESSAGES_COMMANDS_LIST_TITLE, "AMOUNT", spectators);

        for(Player spectator : spectateAPI.getSpectators()) {
            if(spectator == null || !spectator.isOnline()) continue;
            Player target = spectateAPI.getTargetOf(spectator);

            if(target == null || !target.isOnline()) {
                Messages.sendMessage(sender, Paths.MESSAGES_COMMANDS_LIST_DEFAULT, "SPECTATOR", spectator.getName());
                continue;
            }

            boolean cycling = spectateAPI.isPausedSpectator(spectator);
            boolean paused = spectateAPI.isPausedSpectator(spectator);

            String message;
            if(paused) message = Paths.MESSAGES_COMMANDS_LIST_PAUSED;
            else if(cycling) message = Paths.MESSAGES_COMMANDS_LIST_CYCLING;
            else message = Paths.MESSAGES_COMMANDS_LIST_SPECTATING;

            Messages.sendMessage(sender, message, "SPECTATOR", spectator.getName(),
                    "TARGET", spectateAPI.getTargetOf(spectator).getName());
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(@Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String s, @Nonnull String[] args) {
        return Collections.emptyList();
    }
}