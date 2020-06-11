package de.cuzim1tigaaa.spectate.commands;

import de.cuzim1tigaaa.spectate.Main;
import de.cuzim1tigaaa.spectate.files.Config;
import de.cuzim1tigaaa.spectate.files.Permissions;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class unspectate implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(sender.hasPermission(Permissions.UNSPECTATE)) {
            if(args.length >= 1) {
                Player target = Bukkit.getPlayer(args[0]);
                if(target != null) {
                    if(Main.getInstance().getSpectators().contains(target)) {
                        if(args.length == 1) {
                            Main.getInstance().getMethods().unSpectate(target, false);
                        }else {
                            boolean loc = Boolean.parseBoolean(args[1]);
                            Main.getInstance().getMethods().unSpectate(target, loc);
                        }
                        target.sendMessage(Config.getMessage("Config.Spectate.leave"));
                        sender.sendMessage(Config.getMessage("Config.Spectate.unSpectate.others", "player", target.getDisplayName()));
                    }else sender.sendMessage(ChatColor.GRAY + target.getDisplayName() + " §cis not Spectating");
                    return true;
                }else sender.sendMessage(Config.getMessage("Config.Error.offline", "player", args[0]));
            }else {
                for(Player all : Main.getInstance().getSpectators()) {
                    Main.getInstance().getMethods().unSpectate(all, false);
                    all.sendMessage(Config.getMessage("Config.Spectate.leave"));
                }
                sender.sendMessage("§cAll Players have been resend.");
            }
        }else sender.sendMessage(Config.getMessage("Config.Permission"));
        return true;
    }
}