package de.cuzim1tigaaa.spectator.commands;

import de.cuzim1tigaaa.spectator.Spectator;
import de.cuzim1tigaaa.spectator.files.*;
import org.bukkit.GameMode;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.*;

public class SpectateHere implements CommandExecutor, TabCompleter {

    private final Spectator instance;

    public SpectateHere(Spectator plugin) {
        Objects.requireNonNull(plugin.getCommand("spectatehere")).setExecutor(this);
        this.instance = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(sender instanceof Player player) {
            if(!player.hasPermission(Permissions.COMMAND_SPECTATE_HERE)) {
                if(player.hasPermission(Permissions.COMMANDS_SPECTATE_CYCLEONLY)) {
                    player.sendMessage(Messages.getMessage(Paths.MESSAGES_GENERAL_CYCLEONLY));
                    return true;
                }
                player.sendMessage(Messages.getMessage(Paths.MESSAGE_DEFAULT_PERMISSION));
                return true;
            }
            if(!player.getGameMode().equals(GameMode.SPECTATOR)) {
                instance.getMethods().spectate(player, null);
                player.sendMessage(Messages.getMessage(Paths.MESSAGES_COMMANDS_SPECTATE_JOIN_OWN));
                return true;
            }
            instance.getMethods().unSpectate(player, true);
            player.sendMessage(Messages.getMessage(Paths.MESSAGES_COMMANDS_SPECTATE_LEAVE_OWN));
            return true;
        }
        sender.sendMessage(Messages.getMessage(Paths.MESSAGE_DEFAULT_SENDER));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
        return Collections.emptyList();
    }
}