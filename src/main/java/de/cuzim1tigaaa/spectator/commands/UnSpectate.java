package de.cuzim1tigaaa.spectator.commands;

import de.cuzim1tigaaa.spectator.Spectator;
import de.cuzim1tigaaa.spectator.files.Messages;
import de.cuzim1tigaaa.spectator.files.Paths;
import de.cuzim1tigaaa.spectator.spectate.SpectateUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import java.util.*;

import static de.cuzim1tigaaa.spectator.files.Permissions.COMMAND_UNSPECTATE;
import static de.cuzim1tigaaa.spectator.files.Permissions.hasPermission;

public class UnSpectate implements CommandExecutor, TabCompleter {

    private final Spectator plugin;
    private final SpectateUtils spectateUtils;

    public UnSpectate(Spectator plugin) {
        Objects.requireNonNull(plugin.getCommand("unspectate")).setExecutor(this);
        this.plugin = plugin;
        this.spectateUtils = plugin.getSpectateUtils();
    }

    @Override
    public boolean onCommand(@Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String label, @Nonnull String[] args) {
        if(!hasPermission(sender, COMMAND_UNSPECTATE)) {
            sender.sendMessage(Messages.getMessage(Paths.MESSAGE_DEFAULT_PERMISSION));
            return true;
        }

        if(args.length == 0) {
            if(spectateUtils.getSpectators().isEmpty()) {
                sender.sendMessage(Messages.getMessage(Paths.MESSAGES_COMMANDS_LIST_NONE));
                return true;
            }

            for(Player spectator : spectateUtils.getSpectators()) {
                if(spectator.equals(sender)) continue;
                spectator.sendMessage(Messages.getMessage(Paths.MESSAGES_COMMANDS_SPECTATE_LEAVE_OWN));
            }

            // TODO plugin.getSpectateManager().restoreAll();
            sender.sendMessage(Messages.getMessage(Paths.MESSAGES_COMMANDS_UNSPECTATE_ALL));
            return true;
        }
        Player target = Bukkit.getPlayer(args[0]);
        if(target == null || !target.isOnline()) {
            sender.sendMessage(Messages.getMessage(Paths.MESSAGES_GENERAL_OFFLINEPLAYER, "TARGET", args[0]));
            return true;
        }

        if(!spectateUtils.isSpectator(target)) {
            sender.sendMessage(Messages.getMessage(Paths.MESSAGES_GENERAL_NOTSPECTATING, "TARGET", target.getDisplayName()));
            return true;
        }

        spectateUtils.Unspectate(target, !(args.length > 1 && Boolean.parseBoolean(args[1])));
        target.sendMessage(Messages.getMessage(Paths.MESSAGES_COMMANDS_SPECTATE_LEAVE_OWN));
        sender.sendMessage(Messages.getMessage(Paths.MESSAGES_COMMANDS_UNSPECTATE_PLAYER, "TARGET", target.getDisplayName()));
        return true;
    }

    @Override
    public List<String> onTabComplete(@Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String s, @Nonnull String[] args) {
        return switch(args.length) {
            case 1 -> plugin.getOnlinePlayerNames();
            case 2 -> List.of("true", "false");
            default -> Collections.emptyList();
        };
    }
}