package de.cuzim1tigaaa.spectate.commands;

import de.cuzim1tigaaa.spectate.Main;
import de.cuzim1tigaaa.spectate.files.*;
import org.bukkit.GameMode;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

public class SpectateHere implements CommandExecutor {

    private final Main instance;

    public SpectateHere(Main plugin) {
        this.instance = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(sender instanceof Player) {
            Player player = (Player) sender;
            if(!player.hasPermission(Permissions.COMMAND_SPECTATE_HERE)) {
                if(player.hasPermission(Permissions.COMMANDS_SPECTATE_CYCLEONLY)) {
                    player.sendMessage(Config.getMessage(Paths.MESSAGES_GENERAL_CYCLEONLY));
                    return true;
                }
                player.sendMessage(Config.getMessage(Paths.MESSAGE_DEFAULT_PERMISSION));
                return true;
            }
            if(!player.getGameMode().equals(GameMode.SPECTATOR)) {
                instance.getMethods().spectate(player, null);
                player.sendMessage(Config.getMessage(Paths.MESSAGES_COMMANDS_SPECTATE_JOIN_OWN));
                return true;
            }
            instance.getMethods().unSpectate(player, true);
            player.sendMessage(Config.getMessage(Paths.MESSAGES_COMMANDS_SPECTATE_LEAVE_OWN));
        }
        return true;
    }
}