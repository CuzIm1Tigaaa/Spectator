package de.cuzim1tigaaa.spectator.commands;

import de.cuzim1tigaaa.spectator.Spectator;
import de.cuzim1tigaaa.spectator.files.Messages;
import de.cuzim1tigaaa.spectator.files.Paths;
import de.cuzim1tigaaa.spectator.spectate.SpectateUtils;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import java.util.*;

import static de.cuzim1tigaaa.spectator.files.Permissions.*;

public class SpectateHere implements CommandExecutor, TabCompleter {

	private final SpectateUtils spectateUtils;

    public SpectateHere(Spectator plugin) {
        Objects.requireNonNull(plugin.getCommand("spectatehere")).setExecutor(this);
	    this.spectateUtils = plugin.getSpectateUtils();
    }

    @Override
    public boolean onCommand(@Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String label, @Nonnull String[] args) {
        if(!(sender instanceof Player player)) {
            sender.sendMessage(Messages.getMessage(null, Paths.MESSAGE_DEFAULT_SENDER));
            return true;
        }

        if(!hasPermission(player, COMMAND_SPECTATE_HERE)) {
            player.sendMessage(Messages.getMessage(player, Paths.MESSAGE_DEFAULT_PERMISSION));
            return true;
        }

        if(spectateUtils.isSpectator(player)) {
            spectateUtils.Unspectate(player, false);
            player.sendMessage(Messages.getMessage(player, Paths.MESSAGES_COMMANDS_SPECTATE_LEAVE_OWN));
            return true;
        }

        spectateUtils.Spectate(player, null);
        player.sendMessage(Messages.getMessage(player, Paths.MESSAGES_COMMANDS_SPECTATE_JOIN_OWN));
        return true;
    }

    @Override
    public List<String> onTabComplete(@Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String s, @Nonnull String[] args) {
        return Collections.emptyList();
    }
}