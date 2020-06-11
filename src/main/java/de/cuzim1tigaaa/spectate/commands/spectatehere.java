package de.cuzim1tigaaa.spectate.commands;

import de.cuzim1tigaaa.spectate.Main;
import de.cuzim1tigaaa.spectate.files.Config;
import de.cuzim1tigaaa.spectate.files.Permissions;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class spectatehere implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(sender instanceof Player) {
            Player player = (Player) sender;
            if(player.hasPermission(Permissions.HERE)) {
                if(!player.hasPermission(Permissions.CYCLEONLY)) {
                    if(player.getGameMode().equals(GameMode.SPECTATOR)) {
                        Main.getInstance().getMethods().unSpectate(player, true);
                        if(Main.getInstance().getCycleHandler().isPlayerCycling(player)) {
                            Main.getInstance().getCycleHandler().stopCycle(player);
                        }
                        player.sendMessage(Config.getMessage("Config.Spectate.leave"));
                    }else {
                        Main.getInstance().getMethods().spectate(player, null);
                        player.sendMessage(Config.getMessage("Config.Spectate.use"));
                    }
                }else {
                    player.sendMessage(Config.getMessage("Config.Error.cycleOnly"));
                }
            }else player.sendMessage(Config.getMessage("Config.Permission"));
        }
        return true;
    }
}