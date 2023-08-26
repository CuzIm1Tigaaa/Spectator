package de.cuzim1tigaaa.spectator.commands;

import de.cuzim1tigaaa.spectator.Spectator;
import de.cuzim1tigaaa.spectator.files.Paths;
import de.cuzim1tigaaa.spectator.files.Permissions;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import java.util.*;

import static de.cuzim1tigaaa.spectator.files.Messages.getMessage;

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
                spectateJoinSpectate(player, args);
                return true;
            }

            if(!player.hasPermission(Permissions.COMMAND_SPECTATE_OTHERS)) {
                if(player.hasPermission(Permissions.COMMANDS_SPECTATE_CYCLEONLY)) {
                    player.sendMessage(getMessage(Paths.MESSAGES_GENERAL_CYCLEONLY));
                    return true;
                }
                player.sendMessage(getMessage(Paths.MESSAGE_DEFAULT_PERMISSION));
                return true;
            }
            Player target = Bukkit.getPlayer(args[0]);
            if(target == null || !target.isOnline()) {
                player.sendMessage(getMessage(Paths.MESSAGES_GENERAL_OFFLINEPLAYER, "TARGET", args[0]));
                return true;
            }
            if(target.getUniqueId().equals(player.getUniqueId())) {
                player.sendMessage(getMessage(Paths.MESSAGES_GENERAL_YOURSELF));
                return true;
            }
            if(this.plugin.getRelation().get(player) == target) {
                player.sendMessage(getMessage(Paths.MESSAGES_GENERAL_SAMEPLAYER, "TARGET", target.getDisplayName()));
                return true;
            }
            if(this.plugin.getRelation().get(target) == player || target.hasPermission(Permissions.BYPASS_SPECTATED)) {
                if(!player.hasPermission(Permissions.BYPASS_SPECTATEALL)) {
                    player.sendMessage(getMessage(Paths.MESSAGES_GENERAL_BYPASS_TELEPORT, "TARGET", target.getDisplayName()));
                    return true;
                }
            }

            if(plugin.getMultiverse() != null) {
                if(!Objects.equals(player.getWorld(), target.getWorld())) {
                    String world = plugin.getMultiverse().getMVWorldManager().getMVWorld(target.getWorld()).getPermissibleName();
                    if(!player.hasPermission("multiverse.access." + world)) {
                        player.sendMessage(getMessage(Paths.MESSAGES_COMMANDS_SPECTATE_MULTIVERSE, "TARGET", target.getDisplayName()));
                        return true;
                    }
                }
            }

            this.plugin.getSpectateManager().spectate(player, target);
            this.plugin.getSpectateManager().notifyTarget(target, player, true);
            player.sendMessage(getMessage(Paths.MESSAGES_COMMANDS_SPECTATE_PLAYER, "TARGET", target.getDisplayName()));
            return true;
        }

        if(args.length > 0) {
            spectateFromConsole(sender, args);
            return true;
        }

        sender.sendMessage(getMessage(Paths.MESSAGE_DEFAULT_SENDER));
        return true;
    }

    private void spectateJoinSpectate(Player player, String[] args) {
        if(this.plugin.getSpectators().contains(player)) {
            this.plugin.getSpectateManager().unSpectate(player, false);
            player.sendMessage(getMessage(Paths.MESSAGES_COMMANDS_SPECTATE_LEAVE_OWN));
            return;
        }
        if(!player.hasPermission(Permissions.COMMAND_SPECTATE_GENERAL)) {
            if(player.hasPermission(Permissions.COMMANDS_SPECTATE_CYCLEONLY)) {
                player.sendMessage(getMessage(Paths.MESSAGES_GENERAL_CYCLEONLY));
                return;
            }
            player.sendMessage(getMessage(Paths.MESSAGE_DEFAULT_PERMISSION));
            return;
        }
        this.plugin.getSpectateManager().spectate(player, null);
        player.sendMessage(getMessage(Paths.MESSAGES_COMMANDS_SPECTATE_JOIN_OWN));
    }

    private void spectateFromConsole(CommandSender sender, String[] args) {
        Player player = Bukkit.getPlayer(args[0]);
        if(player == null) {
            sender.sendMessage(getMessage(Paths.MESSAGES_GENERAL_OFFLINEPLAYER, "TARGET", args[0]));
            return;
        }
        if(player.getGameMode().equals(GameMode.SPECTATOR)) {
            this.plugin.getSpectateManager().unSpectate(player, false);
            player.sendMessage(getMessage(Paths.MESSAGES_COMMANDS_SPECTATE_LEAVE_OWN));
            sender.sendMessage(getMessage(Paths.MESSAGES_COMMANDS_SPECTATE_LEAVE_OTHER, "TARGET", player.getDisplayName()));
            return;
        }
        this.plugin.getSpectateManager().spectate(player, null);
        player.sendMessage(getMessage(Paths.MESSAGES_COMMANDS_SPECTATE_JOIN_OWN));
        sender.sendMessage(getMessage(Paths.MESSAGES_COMMANDS_SPECTATE_JOIN_OTHER, "TARGET", player.getDisplayName()));
    }

    @Override
    public List<String> onTabComplete(@Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String s, @Nonnull String[] args) {
        if(args.length == 1)
            return plugin.getOnlinePlayerNames();
        return Collections.emptyList();
    }
}