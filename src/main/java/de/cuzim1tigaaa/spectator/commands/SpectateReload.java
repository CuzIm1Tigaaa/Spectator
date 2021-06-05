package de.cuzim1tigaaa.spectator.commands;

import de.cuzim1tigaaa.spectator.Main;
import de.cuzim1tigaaa.spectator.files.*;
import org.bukkit.command.*;

public class SpectateReload implements CommandExecutor {

    private final Main instance;

    public SpectateReload(Main plugin) {
        this.instance = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!sender.hasPermission(Permissions.COMMANDS_SPECTATE_RELOAD)) {
            sender.sendMessage(Config.getMessage(Paths.MESSAGE_DEFAULT_PERMISSION));
            return true;
        }
        instance.reload();
        sender.sendMessage(Config.getMessage(Paths.MESSAGE_DEFAULT_RELOAD));
        return true;
    }
}