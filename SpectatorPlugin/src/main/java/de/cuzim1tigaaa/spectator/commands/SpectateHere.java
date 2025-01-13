package de.cuzim1tigaaa.spectator.commands;

import de.cuzim1tigaaa.spectator.SpectateAPI;
import de.cuzim1tigaaa.spectator.Spectator;
import de.cuzim1tigaaa.spectator.files.Messages;
import de.cuzim1tigaaa.spectator.files.Paths;
import de.cuzim1tigaaa.spectator.spectate.SpectateUtilsGeneral;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import java.util.*;

import static de.cuzim1tigaaa.spectator.files.Permissions.*;

public class SpectateHere implements CommandExecutor, TabCompleter {

    private final SpectateAPI spectateAPI;
	private final SpectateUtilsGeneral spectateUtils;

    public SpectateHere(Spectator plugin) {
        Objects.requireNonNull(plugin.getCommand("spectatehere")).setExecutor(this);
        this.spectateAPI = plugin.getSpectateAPI();
	    this.spectateUtils = spectateAPI.getSpectateGeneral();
    }

    @Override
    public boolean onCommand(@Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String label, @Nonnull String[] args) {
        if(!(sender instanceof Player player)) {
            Messages.sendMessage(sender, Paths.MESSAGE_DEFAULT_SENDER);
            return true;
        }

        if(!hasPermission(player, COMMAND_SPECTATE_HERE)) {
            Messages.sendMessage(player, Paths.MESSAGE_DEFAULT_PERMISSION);
            return true;
        }

        if(spectateAPI.isSpectator(player)) {
            spectateUtils.unspectate(player, false);
            Messages.sendMessage(player, Paths.MESSAGES_COMMANDS_SPECTATE_LEAVE_OWN);
            return true;
        }
        spectateUtils.getSpectateStartLocation().put(player.getUniqueId(), player.getLocation());
        spectateUtils.spectate(player, null);
        Messages.sendMessage(player, Paths.MESSAGES_COMMANDS_SPECTATE_JOIN_OWN);
        return true;
    }

    @Override
    public List<String> onTabComplete(@Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String s, @Nonnull String[] args) {
        return Collections.emptyList();
    }
}