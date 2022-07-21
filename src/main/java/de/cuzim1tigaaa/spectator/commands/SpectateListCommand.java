package de.cuzim1tigaaa.spectator.commands;

import de.cuzim1tigaaa.spectator.SpectatorPlugin;
import de.cuzim1tigaaa.spectator.cycle.CycleHandler;
import de.cuzim1tigaaa.spectator.files.Messages;
import de.cuzim1tigaaa.spectator.files.Paths;
import de.cuzim1tigaaa.spectator.files.Permissions;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class SpectateListCommand implements CommandExecutor {

    private final SpectatorPlugin plugin;

    public SpectateListCommand(SpectatorPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission(Permissions.COMMANDS_SPECTATE_LIST)) {
            sender.sendMessage(Messages.getMessage(Paths.MESSAGE_DEFAULT_PERMISSION));
            return true;
        }

        int spectators = this.plugin.getSpectators().size();

        if (spectators == 0) {
            sender.sendMessage(Messages.getMessage(Paths.MESSAGES_COMMANDS_LIST_NONE));
            return true;
        }

        sender.sendMessage(Messages.getMessage(Paths.MESSAGES_COMMANDS_LIST_TITLE, "AMOUNT", spectators));

        for (Player spectator : this.plugin.getSpectators()) {
            if (spectator != null) {
                String msg;

                if (this.plugin.getRelation().containsKey(spectator)) {
                    if (CycleHandler.isPlayerCycling(spectator))
                        msg = Messages.getMessage(Paths.MESSAGES_COMMANDS_LIST_CYCLING, "SPECTATOR", spectator.getDisplayName());
                    else
                        msg = Messages.getMessage(Paths.MESSAGES_COMMANDS_LIST_SPECTATING, "SPECTATOR", spectator.getDisplayName(), "TARGET", this.plugin.getRelation().get(spectator).getDisplayName());
                } else {
                    msg = Messages.getMessage(Paths.MESSAGES_COMMANDS_LIST_DEFAULT, "SPECTATOR", spectator.getDisplayName());
                }

                sender.sendMessage(msg);
            }
        }
        return true;
    }
}