package de.cuzim1tigaaa.spectator.commands;

import de.cuzim1tigaaa.spectator.Spectator;
import de.cuzim1tigaaa.spectator.files.*;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import java.util.*;

public class Spectate implements CommandExecutor, TabCompleter {

    private final Spectator plugin;

    public Spectate(Spectator plugin) {
        Objects.requireNonNull(plugin.getCommand("spectate")).setExecutor(this);
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String label, @Nonnull String[] args) {
        if(sender instanceof Player player) {
            if(args.length == 0) {
                if(!player.hasPermission(Permissions.COMMAND_SPECTATE_GENERAL)) {
                    if(player.hasPermission(Permissions.COMMANDS_SPECTATE_CYCLEONLY)) {
                        player.sendMessage(Messages.getMessage(Paths.MESSAGES_GENERAL_CYCLEONLY));
                        return true;
                    }
                    player.sendMessage(Messages.getMessage(Paths.MESSAGE_DEFAULT_PERMISSION));
                    return true;
                }
                if(this.plugin.getSpectators().contains(player)) {
                    this.plugin.getSpectateManager().unSpectate(player, false);
                    player.sendMessage(Messages.getMessage(Paths.MESSAGES_COMMANDS_SPECTATE_LEAVE_OWN));
                    return true;
                }
                this.plugin.getSpectateManager().spectate(player, null);
                player.sendMessage(Messages.getMessage(Paths.MESSAGES_COMMANDS_SPECTATE_JOIN_OWN));
                return true;
            }
            if(!player.hasPermission(Permissions.COMMAND_SPECTATE_OTHERS)) {
                if(player.hasPermission(Permissions.COMMANDS_SPECTATE_CYCLEONLY)) {
                    player.sendMessage(Messages.getMessage(Paths.MESSAGES_GENERAL_CYCLEONLY));
                    return true;
                }
                player.sendMessage(Messages.getMessage(Paths.MESSAGE_DEFAULT_PERMISSION));
                return true;
            }
            Player target = Bukkit.getPlayer(args[0]);
            if(target == null || !target.isOnline()) {
                player.sendMessage(Messages.getMessage(Paths.MESSAGES_GENERAL_OFFLINEPLAYER, "TARGET", args[0]));
                return true;
            }
            if(target.getUniqueId().equals(player.getUniqueId())) {
                player.sendMessage(Messages.getMessage(Paths.MESSAGES_GENERAL_YOURSELF));
                return true;
            }
            if(this.plugin.getRelation().get(player) == target) {
                player.sendMessage(Messages.getMessage(Paths.MESSAGES_GENERAL_SAMEPLAYER, "TARGET", target.getDisplayName()));
                return true;
            }
            if(this.plugin.getRelation().get(target) == player || target.hasPermission(Permissions.BYPASS_SPECTATED)) {
                if(!player.hasPermission(Permissions.BYPASS_SPECTATEALL)) {
                    player.sendMessage(Messages.getMessage(Paths.MESSAGES_GENERAL_BYPASS_TELEPORT, "TARGET", target.getDisplayName()));
                    return true;
                }
            }
            this.plugin.getSpectateManager().spectate(player, target);
            player.sendMessage(Messages.getMessage(Paths.MESSAGES_COMMANDS_SPECTATE_PLAYER, "TARGET", target.getDisplayName()));
            return true;
        }
        if(args.length > 0) {
            Player player = Bukkit.getPlayer(args[0]);
            if(player == null) {
                sender.sendMessage(Messages.getMessage(Paths.MESSAGES_GENERAL_OFFLINEPLAYER, "TARGET", args[0]));
                return true;
            }
            if(player.getGameMode().equals(GameMode.SPECTATOR)) {
                this.plugin.getSpectateManager().unSpectate(player, false);
                player.sendMessage(Messages.getMessage(Paths.MESSAGES_COMMANDS_SPECTATE_LEAVE_OWN));
                sender.sendMessage(Messages.getMessage(Paths.MESSAGES_COMMANDS_SPECTATE_LEAVE_OTHER, "TARGET", player.getDisplayName()));
                return true;
            }
            this.plugin.getSpectateManager().spectate(player, null);
            player.sendMessage(Messages.getMessage(Paths.MESSAGES_COMMANDS_SPECTATE_JOIN_OWN));
            sender.sendMessage(Messages.getMessage(Paths.MESSAGES_COMMANDS_SPECTATE_JOIN_OTHER, "TARGET", player.getDisplayName()));
            return true;
        }
        sender.sendMessage(Messages.getMessage(Paths.MESSAGE_DEFAULT_SENDER));
        return true;
    }

    @Override
    public List<String> onTabComplete(@Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String s, @Nonnull String[] args) {
        final List<String> tab = new ArrayList<>();
        if(args.length == 1) for(Player player : Bukkit.getOnlinePlayers()) tab.add(player.getDisplayName());
        return tab;
    }
}