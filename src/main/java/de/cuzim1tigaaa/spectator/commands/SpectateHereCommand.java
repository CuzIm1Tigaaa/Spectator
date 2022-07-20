package de.cuzim1tigaaa.spectator.commands;

import de.cuzim1tigaaa.spectator.SpectatorPlugin;
import de.cuzim1tigaaa.spectator.files.Messages;
import de.cuzim1tigaaa.spectator.files.Paths;
import de.cuzim1tigaaa.spectator.files.Permissions;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

//If always returning an empty list, why implementing TabCompletor?
public final class SpectateHereCommand implements CommandExecutor {

    private final SpectatorPlugin plugin;

    public SpectateHereCommand(SpectatorPlugin plugin) {
        plugin.getCommand("spectatehere").setExecutor(this);
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;

            if (!player.hasPermission(Permissions.COMMAND_SPECTATE_HERE)) {

                if (player.hasPermission(Permissions.COMMANDS_SPECTATE_CYCLEONLY)) {
                    player.sendMessage(Messages.getMessage(Paths.MESSAGES_GENERAL_CYCLEONLY));
                    return true;
                }

                player.sendMessage(Messages.getMessage(Paths.MESSAGE_DEFAULT_PERMISSION));
                return true;
            }

            if (!player.getGameMode().equals(GameMode.SPECTATOR)) {
                this.plugin.getSpectateManager().spectate(player, null);
                player.sendMessage(Messages.getMessage(Paths.MESSAGES_COMMANDS_SPECTATE_JOIN_OWN));
                return true;
            }

            this.plugin.getSpectateManager().unSpectate(player, true);
            player.sendMessage(Messages.getMessage(Paths.MESSAGES_COMMANDS_SPECTATE_LEAVE_OWN));
            return true;
        }

        sender.sendMessage(Messages.getMessage(Paths.MESSAGE_DEFAULT_SENDER));
        return true;
    }
}