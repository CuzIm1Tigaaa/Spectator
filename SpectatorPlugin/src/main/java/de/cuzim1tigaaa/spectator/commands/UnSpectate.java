package de.cuzim1tigaaa.spectator.commands;

import de.cuzim1tigaaa.spectator.SpectateAPI;
import de.cuzim1tigaaa.spectator.Spectator;
import de.cuzim1tigaaa.spectator.files.Messages;
import de.cuzim1tigaaa.spectator.files.Paths;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import java.util.*;

import static de.cuzim1tigaaa.spectator.files.Permissions.*;

public class UnSpectate implements CommandExecutor, TabCompleter {

    private final Spectator plugin;
    private final SpectateAPI spectateAPI;

    public UnSpectate(Spectator plugin) {
        Objects.requireNonNull(plugin.getCommand("unspectate")).setExecutor(this);
        this.plugin = plugin;
        this.spectateAPI = plugin.getSpectateAPI();
    }

    @Override
    public boolean onCommand(@Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String label, @Nonnull String[] args) {
        if(!hasPermission(sender, COMMAND_UNSPECTATE)) {
            Messages.sendMessage(sender, Paths.MESSAGE_DEFAULT_PERMISSION);
            return true;
        }

        if(spectateAPI.getSpectators().isEmpty()) {
            Messages.sendMessage(sender, Paths.MESSAGES_COMMANDS_LIST_NONE);
            return true;
        }

        if(args.length == 0) {
            for(Player spectator : spectateAPI.getSpectators()) {
                if(spectator.hasPermission(BYPASS_UNSPECTATED))
                    continue;
                spectateAPI.getSpectateGeneral().unspectate(spectator, true);
                if(spectator.equals(sender)) continue;
                Messages.sendMessage(spectator, Paths.MESSAGES_COMMANDS_SPECTATE_LEAVE_OWN);
            }
            Messages.sendMessage(sender, Paths.MESSAGES_COMMANDS_UNSPECTATE_ALL);
            return true;
        }

        boolean oldLocation = true;
        if(args.length > 1)
            oldLocation = Boolean.parseBoolean(args[1]);

        if(args[0].equalsIgnoreCase("*")) {
            for(Player spectator : spectateAPI.getSpectators()) {
                if(spectator.hasPermission(BYPASS_UNSPECTATED))
                    continue;
                spectateAPI.getSpectateGeneral().unspectate(spectator, oldLocation);
                if(spectator.equals(sender)) continue;
                Messages.sendMessage(spectator, Paths.MESSAGES_COMMANDS_SPECTATE_LEAVE_OWN);
            }
            Messages.sendMessage(sender, Paths.MESSAGES_COMMANDS_UNSPECTATE_ALL);
            return true;
        }
        Player target = Bukkit.getPlayer(args[0]);
        if(target == null || !target.isOnline()) {
            Messages.sendMessage(sender, Paths.MESSAGES_GENERAL_OFFLINEPLAYER, "TARGET", args[0]);
            return true;
        }

        if(!spectateAPI.isSpectator(target)) {
            Messages.sendMessage(sender, Paths.MESSAGES_GENERAL_NOTSPECTATING, "TARGET", target.getName());
            return true;
        }
        if(target.hasPermission(BYPASS_UNSPECTATED))
            return true;

        spectateAPI.getSpectateGeneral().unspectate(target, oldLocation);
        Messages.sendMessage(target, Paths.MESSAGES_COMMANDS_SPECTATE_LEAVE_OWN);
        Messages.sendMessage(sender, Paths.MESSAGES_COMMANDS_UNSPECTATE_PLAYER, "TARGET", target.getName());
        return true;
    }

    @Override
    public List<String> onTabComplete(@Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String s, @Nonnull String[] args) {
        if(args.length == 1) {
            List<String> names = plugin.getOnlinePlayerNames();
            names.add("*");
            return names;
        }
        if(args.length == 2)
            return List.of("true", "false");

        return Collections.emptyList();
    }
}