package de.cuzim1tigaaa.spectate.commands;

import de.cuzim1tigaaa.spectate.Main;
import de.cuzim1tigaaa.spectate.files.Config;
import de.cuzim1tigaaa.spectate.files.Permissions;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class spectate implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(sender instanceof Player) {
            Player player = (Player) sender;
            if(args.length == 1) {
                if(player.hasPermission(Permissions.OTHER)) {
                    Player target = Bukkit.getPlayer(args[0]);

                    if(target == null || !target.isOnline()) {
                        player.sendMessage(Config.getMessage("Config.Error.offline", "player", args[0]));
                        return true;
                    }
                    if(target.getUniqueId().equals(player.getUniqueId())) {
                        player.sendMessage(Config.getMessage("Config.Error.self"));
                        return true;
                    }
                    if(Main.getInstance().getRelation().get(player) == target) {
                        player.sendMessage(Config.getMessage("Config.Error.same", "player", target.getDisplayName()));
                        return true;
                    }
                    if(Main.getInstance().getRelation().get(target) == player || target.hasPermission(Permissions.CANNOT)) {
                        player.sendMessage(Config.getMessage("Config.Error.cannot", "player", target.getDisplayName()));
                        return true;
                    }
                    player.sendMessage(Config.getMessage("Config.Spectate.others", "player", target.getDisplayName()));
                    Main.getInstance().getMethods().spectate(player, target);
                }else player.sendMessage(Config.getMessage("Config.Permission"));
            }else {
                if(player.hasPermission(Permissions.USE)) {
                    if(player.getGameMode().equals(GameMode.SPECTATOR)) {
                        Main.getInstance().getMethods().unSpectate(player, false);
                        if(Main.getInstance().getCycleHandler().isPlayerCycling(player) || Main.getInstance().getCycleHandler().isPlayerPaused(player)) {
                            Main.getInstance().getCycleHandler().stopCycle(player);
                        }
                        player.sendMessage(Config.getMessage("Config.Spectate.leave"));
                    }else {
                        Main.getInstance().getMethods().spectate(player, null);
                        player.sendMessage(Config.getMessage("Config.Spectate.use"));
                    }
                }else player.sendMessage(Config.getMessage("Config.Permission"));
            }
        }else {
            if(args.length > 0) {
                Player player = Bukkit.getPlayer(args[0]);
                if(player == null) {
                    sender.sendMessage(Config.getMessage("Config.Error.offline", "player", args[0]));
                    return true;
                }
                if(player.getGameMode().equals(GameMode.SPECTATOR)) {
                    Main.getInstance().getMethods().unSpectate(player, false);
                    if(Main.getInstance().getCycleHandler().isPlayerCycling(player)) {
                        Main.getInstance().getCycleHandler().stopCycle(player);
                    }
                    player.sendMessage(Config.getMessage("Config.Spectate.leave"));
                    sender.sendMessage(Config.getMessage("Config.Spectator.give.leave", "player", player.getDisplayName()));
                }else {
                    Main.getInstance().getMethods().spectate(player, null);
                    player.sendMessage(Config.getMessage("Config.Spectate.use"));
                    sender.sendMessage(Config.getMessage("Config.Spectator.give.use", "player", player.getDisplayName()));
                }
                return true;
            }
            return false;
        }
        return true;
    }
}