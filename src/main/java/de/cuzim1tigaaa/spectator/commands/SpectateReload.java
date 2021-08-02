package de.cuzim1tigaaa.spectator.commands;

import de.cuzim1tigaaa.spectator.Spectator;
import de.cuzim1tigaaa.spectator.files.*;
import org.bukkit.command.*;

import java.util.*;

public class SpectateReload implements CommandExecutor, TabCompleter {

    private final Spectator instance;

    public SpectateReload(Spectator plugin) {
        Objects.requireNonNull(plugin.getCommand("spectatereload")).setExecutor(this);
        this.instance = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!sender.hasPermission(Permissions.COMMANDS_SPECTATE_RELOAD)) {
            sender.sendMessage(Messages.getMessage(Paths.MESSAGE_DEFAULT_PERMISSION));
            return true;
        }
        instance.reload();
        sender.sendMessage(Messages.getMessage(Paths.MESSAGE_DEFAULT_RELOAD));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
        return Collections.emptyList();
    }
}