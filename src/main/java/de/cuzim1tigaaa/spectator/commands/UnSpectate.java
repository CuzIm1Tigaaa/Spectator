package de.cuzim1tigaaa.spectator.commands;

import de.cuzim1tigaaa.spectator.Spectator;
import de.cuzim1tigaaa.spectator.files.*;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import java.util.*;

public class UnSpectate implements CommandExecutor, TabCompleter {

    private final Spectator plugin;

    public UnSpectate(Spectator plugin) {
        Objects.requireNonNull(plugin.getCommand("unspectate")).setExecutor(this);
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String label, @Nonnull String[] args) {
        if(!sender.hasPermission(Permissions.COMMAND_UNSPECTATE)) {
            sender.sendMessage(Messages.getMessage(Paths.MESSAGE_DEFAULT_PERMISSION));
            return true;
        }
        if(args.length < 1) {
            if(this.plugin.getSpectators().size() <= 0) {
                sender.sendMessage(Messages.getMessage(Paths.MESSAGES_COMMANDS_LIST_NONE));
                return true;
            }
            this.plugin.getSpectateManager().restoreAll();
            for(Player player : this.plugin.getSpectators()) if(!player.equals(sender)) player.sendMessage(Messages.getMessage(Paths.MESSAGES_COMMANDS_SPECTATE_LEAVE_OWN));
            sender.sendMessage(Messages.getMessage(Paths.MESSAGES_COMMANDS_UNSPECTATE_ALL));
            return true;
        }
        Player target = Bukkit.getPlayer(args[0]);
        if(target == null) {
            sender.sendMessage(Messages.getMessage(Paths.MESSAGES_GENERAL_OFFLINEPLAYER, "TARGET", args[0]));
            return true;
        }
        if(!this.plugin.getSpectators().contains(target)) {
            sender.sendMessage(Messages.getMessage(Paths.MESSAGES_GENERAL_NOTSPECTATING, "TARGET", target.getDisplayName()));
            return true;
        }
        boolean useCurrentLocation = false;
        if(args.length > 1) useCurrentLocation = Boolean.parseBoolean(args[1]);

        this.plugin.getSpectateManager().unSpectate(target, useCurrentLocation);
        target.sendMessage(Messages.getMessage(Paths.MESSAGES_COMMANDS_SPECTATE_LEAVE_OWN));
        sender.sendMessage(Messages.getMessage(Paths.MESSAGES_COMMANDS_UNSPECTATE_PLAYER, "TARGET", target.getDisplayName()));
        return true;
    }

    @Override
    public List<String> onTabComplete(@Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String s, @Nonnull String[] args) {
        final List<String> tab = new ArrayList<>();
        if(args.length == 1) for(Player player : Bukkit.getOnlinePlayers()) tab.add(player.getDisplayName());
        if(args.length == 2) { tab.add("true"); tab.add("false"); }
        return tab;
    }
}