package de.cuzim1tigaaa.spectate.commands;

import de.cuzim1tigaaa.spectate.Main;
import de.cuzim1tigaaa.spectate.files.Config;
import de.cuzim1tigaaa.spectate.files.Permissions;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class spectatereload implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!sender.hasPermission(Permissions.RELOAD)) {
            sender.sendMessage(Config.getMessage("Config.Permission"));
        }else {
            Main.getInstance().reload();
            sender.sendMessage(Config.getMessage("Config.Plugin.reload"));
        }
        return true;
    }
}