package de.cuzim1tigaaa.spectator.commands;

import de.cuzim1tigaaa.spectator.Spectator;
import de.cuzim1tigaaa.spectator.files.Paths;
import de.cuzim1tigaaa.spectator.spectate.SpectateUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import java.util.*;

import static de.cuzim1tigaaa.spectator.files.Messages.getMessage;
import static de.cuzim1tigaaa.spectator.files.Permissions.*;

public class Spectate implements CommandExecutor, TabCompleter {

    private final Spectator plugin;
    private final SpectateUtils spectateUtils;

    public Spectate(Spectator plugin) {
        Objects.requireNonNull(plugin.getCommand("spectate")).setExecutor(this);
        this.plugin = plugin;
        this.spectateUtils = plugin.getSpectateUtils();
    }

    @Override
    public boolean onCommand(@Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String label, @Nonnull String[] args) {
        if(!(sender instanceof Player player)) {
            if(args.length == 0) {
                sender.sendMessage(getMessage(sender, Paths.MESSAGE_DEFAULT_SENDER));
                return true;
            }

            Player target = Bukkit.getPlayer(args[0]);

            if(target == null) {
                sender.sendMessage(getMessage(sender, Paths.MESSAGES_GENERAL_OFFLINEPLAYER, "TARGET", args[0]));
                return true;
            }

            if(spectateUtils.isSpectator(target)) {
                spectateUtils.Unspectate(target, true);
                target.sendMessage(getMessage(target, Paths.MESSAGES_COMMANDS_SPECTATE_LEAVE_OWN));
                sender.sendMessage(getMessage(sender, Paths.MESSAGES_COMMANDS_SPECTATE_LEAVE_OTHER, "TARGET", target.getName()));
                return true;
            }

            spectateUtils.Spectate(target, null);
            target.sendMessage(getMessage(target, Paths.MESSAGES_COMMANDS_SPECTATE_JOIN_OWN));
            sender.sendMessage(getMessage(sender, Paths.MESSAGES_COMMANDS_SPECTATE_JOIN_OTHER, "TARGET", target.getName()));
            return true;
        }

        if(!hasPermission(player, COMMAND_SPECTATE_GENERAL)) {
            player.sendMessage(getMessage(player, Paths.MESSAGE_DEFAULT_PERMISSION));
            return true;
        }

        if(args.length == 0 || !hasPermission(player, COMMAND_SPECTATE_OTHERS)) {
            if(spectateUtils.isSpectator(player)) {
                spectateUtils.Unspectate(player, true);
                player.sendMessage(getMessage(player, Paths.MESSAGES_COMMANDS_SPECTATE_LEAVE_OWN));
                return true;
            }

            spectateUtils.Spectate(player, null);
            player.sendMessage(getMessage(player, Paths.MESSAGES_COMMANDS_SPECTATE_JOIN_OWN));
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if(target == null || !target.isOnline()) {
            player.sendMessage(getMessage(player, Paths.MESSAGES_GENERAL_OFFLINEPLAYER, "TARGET", args[0]));
            return true;
        }

        if(target.getUniqueId().equals(player.getUniqueId())) {
            player.sendMessage(getMessage(player, Paths.MESSAGES_GENERAL_YOURSELF));
            return true;
        }

        if(spectateUtils.isSpectating(player, target)) {
            player.sendMessage(getMessage(player, Paths.MESSAGES_GENERAL_SAMEPLAYER, "TARGET", target.getName()));
            return true;
        }

        if(spectateUtils.isSpectating(target, player) || hasPermission(target, BYPASS_SPECTATED)) {
            if(!hasPermission(player, BYPASS_SPECTATEALL)) {
                player.sendMessage(getMessage(player, Paths.MESSAGES_GENERAL_BYPASS_TELEPORT, "TARGET", target.getName()));
                return true;
            }
        }

        if(plugin.getMultiverseCore() != null) {
            if(!player.getWorld().getUID().equals(target.getWorld().getUID())) {
                String world = plugin.getMultiverseCore().getMVWorldManager().getMVWorld(target.getWorld()).getPermissibleName();
                if(!player.hasPermission("multiverse.access." + world)) {
                    player.sendMessage(getMessage(player, Paths.MESSAGES_COMMANDS_SPECTATE_MULTIVERSE, "TARGET", target.getName()));
                    return true;
                }
            }
        }

        spectateUtils.Spectate(player, target);
        player.sendMessage(getMessage(player, Paths.MESSAGES_COMMANDS_SPECTATE_PLAYER, "TARGET", target.getName()));
        return true;
    }

    @Override
    public List<String> onTabComplete(@Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String s, @Nonnull String[] args) {
        if(args.length == 1) {
            if(!(sender instanceof Player player) || hasPermission(player, COMMAND_SPECTATE_OTHERS))
                return plugin.getOnlinePlayerNames();
        }
        return Collections.emptyList();
    }
}