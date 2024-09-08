package de.cuzim1tigaaa.spectator.commands;

import de.cuzim1tigaaa.spectator.Spectator;
import de.cuzim1tigaaa.spectator.files.*;
import org.bukkit.command.*;

import java.util.*;

public class SpectateReload implements CommandExecutor, TabCompleter {

    private final Spectator plugin;

    public SpectateReload(Spectator plugin) {
        Objects.requireNonNull(plugin.getCommand("spectatereload")).setExecutor(this);
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!Permissions.hasPermission(sender, Permissions.COMMANDS_SPECTATE_RELOAD)) {
            sender.sendMessage(Messages.getMessage(sender, Paths.MESSAGE_DEFAULT_PERMISSION));
            return true;
        }

        this.plugin.reload();
        sender.sendMessage(Messages.getMessage(sender, Paths.MESSAGE_DEFAULT_RELOAD));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String s, String[] args) {
        return Collections.emptyList();
    }
}