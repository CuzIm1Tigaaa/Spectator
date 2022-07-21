package de.cuzim1tigaaa.spectator.commands;

import de.cuzim1tigaaa.spectator.SpectatorPlugin;
import de.cuzim1tigaaa.spectator.files.Messages;
import de.cuzim1tigaaa.spectator.files.Paths;
import de.cuzim1tigaaa.spectator.files.Permissions;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public final class SpectateReloadCommand implements CommandExecutor {

    private final SpectatorPlugin plugin;

    public SpectateReloadCommand(SpectatorPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission(Permissions.COMMANDS_SPECTATE_RELOAD)) {
            sender.sendMessage(Messages.getMessage(Paths.MESSAGE_DEFAULT_PERMISSION));
            return true;
        }

        this.plugin.reload();
        sender.sendMessage(Messages.getMessage(Paths.MESSAGE_DEFAULT_RELOAD));
        return true;
    }
}