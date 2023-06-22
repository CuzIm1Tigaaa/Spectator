package de.cuzim1tigaaa.spectator.commands;

import de.cuzim1tigaaa.spectator.Spectator;
import de.cuzim1tigaaa.spectator.files.*;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import java.util.Objects;

public class SpectateList implements CommandExecutor {

    private final Spectator plugin;

    public SpectateList(Spectator plugin) {
        Objects.requireNonNull(plugin.getCommand("spectatelist")).setExecutor(this);
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String label, @Nonnull String[] args) {
        if(!sender.hasPermission(Permissions.COMMANDS_SPECTATE_LIST)) {
            sender.sendMessage(Messages.getMessage(Paths.MESSAGE_DEFAULT_PERMISSION));
            return true;
        }
        int spectators = this.plugin.getSpectators().size();
        if(spectators == 0) {
            sender.sendMessage(Messages.getMessage(Paths.MESSAGES_COMMANDS_LIST_NONE));
            return true;
        }
        sender.sendMessage(Messages.getMessage(Paths.MESSAGES_COMMANDS_LIST_TITLE, "AMOUNT", spectators));
        for(Player all : this.plugin.getSpectators()) {
            if(all != null) {
                String msg;
                if(this.plugin.getRelation().containsKey(all))
                    msg = Messages.getMessage(
                            plugin.getCycleHandler().isPlayerCycling(all) ? Paths.MESSAGES_COMMANDS_LIST_CYCLING : Paths.MESSAGES_COMMANDS_LIST_SPECTATING,
                            "SPECTATOR", all.getDisplayName(), "TARGET", this.plugin.getRelation().get(all).getDisplayName());

                else msg = Messages.getMessage(Paths.MESSAGES_COMMANDS_LIST_DEFAULT, "SPECTATOR", all.getDisplayName());
                sender.sendMessage(msg);
            }
        }
        return true;
    }
}