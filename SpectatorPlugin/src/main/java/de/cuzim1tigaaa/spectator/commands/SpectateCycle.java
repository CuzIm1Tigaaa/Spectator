package de.cuzim1tigaaa.spectator.commands;

import de.cuzim1tigaaa.spectator.Spectator;
import de.cuzim1tigaaa.spectator.cycle.Cycle;
import de.cuzim1tigaaa.spectator.cycle.CycleTask;
import de.cuzim1tigaaa.spectator.files.*;
import de.cuzim1tigaaa.spectator.spectate.SpectateUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import java.util.*;

import static de.cuzim1tigaaa.spectator.files.Permissions.*;

public class SpectateCycle implements CommandExecutor, TabCompleter {

    private final Spectator plugin;
    private final SpectateUtils spectateUtils;

    public SpectateCycle(Spectator plugin) {
        Objects.requireNonNull(plugin.getCommand("spectatecycle")).setExecutor(this);
        this.plugin = plugin;
        this.spectateUtils = plugin.getSpectateUtils();
    }

    @Override
    public boolean onCommand(@Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String label, @Nonnull String[] args) {
        if(!(sender instanceof Player player)) {
            sender.sendMessage(Messages.getMessage(sender, Paths.MESSAGE_DEFAULT_SENDER));
            return true;
        }

        if(!hasPermission(sender, COMMANDS_SPECTATE_CYCLE)) {
            player.sendMessage(Messages.getMessage(player, Paths.MESSAGE_DEFAULT_PERMISSION));
            return true;
        }

        if(args.length < 1) {
            player.sendMessage(Messages.getMessage(player, Paths.MESSAGE_DEFAULT_SYNTAX, "USAGE", "/spectatecycle [start|stop]"));
            return true;
        }

        switch(args[0].toLowerCase()) {
            case "start" -> {
                if(args.length < 2) {
                    player.sendMessage(Messages.getMessage(player, Paths.MESSAGE_DEFAULT_SYNTAX, "USAGE", "/spectatecycle start <interval> [alphabetical|random]"));
                    return true;
                }

                int seconds = -1;
                boolean alphabetical = args.length >= 3 && args[2].equalsIgnoreCase("alphabetical");

                try {
                    seconds = Integer.parseInt(args[1]);
                }catch(NumberFormatException ignored) {
                    player.sendMessage(Messages.getMessage(player, Paths.MESSAGES_GENERAL_NUMBERFORMAT));
                }

                int min = Config.getInt(Paths.CONFIG_CYCLE_MIN_INTERVAL);
                int max = Config.getInt(Paths.CONFIG_CYCLE_MAX_INTERVAL);

                if(min > 0 && seconds < min) {
                    player.sendMessage(Messages.getMessage(player, Paths.MESSAGES_COMMANDS_CYCLE_INTERVAL_TOO_SMALL, "MINIMUM", min));
                    return true;
                }

                if(max > 0 && seconds > max) {
                    player.sendMessage(Messages.getMessage(player, Paths.MESSAGES_COMMANDS_CYCLE_INTERVAL_TOO_BIG, "MAXIMUM", max));
                    return true;
                }

                spectateUtils.getSpectateStartLocation().put(player.getUniqueId(), player.getLocation());
                startSpectateCycle(player, seconds, alphabetical);
                return true;
            }

            case "stop" -> {
                if(!hasPermissions(sender, COMMAND_SPECTATE_GENERAL, COMMAND_SPECTATE_OTHERS, COMMAND_SPECTATE_HERE)) {
                    spectateUtils.Unspectate(player, true);
                    return true;
                }

                if(args.length == 1) {
                    if(!spectateUtils.isCycling(player)) {
                        player.sendMessage(Messages.getMessage(player, Paths.MESSAGES_COMMANDS_CYCLE_NOT_CYCLING));
                        return true;
                    }
                    spectateUtils.StopCycle(player);
                    player.sendMessage(Messages.getMessage(player, Paths.MESSAGES_COMMANDS_CYCLE_STOP));
                    return true;
                }

                if(hasPermission(player, COMMANDS_CYCLE_STOP_OTHERS)) {
                    Player target = Bukkit.getPlayer(args[1]);

                    if(target == null || !target.isOnline()) {
                        player.sendMessage(Messages.getMessage(player, Paths.MESSAGES_GENERAL_OFFLINEPLAYER, "TARGET", args[1]));
                        return true;
                    }

                    if(!spectateUtils.isCycling(target)) {
                        player.sendMessage(Messages.getMessage(player, Paths.MESSAGES_COMMANDS_CYCLE_TARGET_NOT_CYCLING, "TARGET", target.getName()));
                        return true;
                    }

                    spectateUtils.StopCycle(target);
                    target.sendMessage(Messages.getMessage(target, Paths.MESSAGES_COMMANDS_CYCLE_STOP));
                    return true;
                }
            }
        }
        player.sendMessage(Messages.getMessage(player, Paths.MESSAGE_DEFAULT_SYNTAX, "USAGE", "/spectatecycle [start|stop]"));
        return true;
    }

    private void startSpectateCycle(Player spectator, int seconds, boolean alphabetical) {
        if(!Config.getBoolean(Paths.CONFIG_CYCLE_NO_PLAYERS) && Bukkit.getOnlinePlayers().size() <= 1) {
            spectator.sendMessage(Messages.getMessage(spectator, Paths.MESSAGES_GENERAL_NOPLAYERS));
            return;
        }

        if(spectateUtils.isCycling(spectator)) {
            spectator.sendMessage(Messages.getMessage(spectator, Paths.MESSAGES_COMMANDS_CYCLE_CYCLING));
            return;
        }

        spectator.sendMessage(Messages.getMessage(spectator, Paths.MESSAGES_COMMANDS_CYCLE_START,
                "INTERVAL", seconds, "ORDER", alphabetical ? "Alphabetic" : "Random"));
        spectateUtils.StartCycle(spectator, new CycleTask(seconds, new Cycle(spectator, null, alphabetical)));
    }

    @Override
    public List<String> onTabComplete(@Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String s, @Nonnull String[] args) {
        return switch(args.length) {
            case 1 -> List.of("start", "stop");
            case 2 -> (args[0].equalsIgnoreCase("stop") && hasPermission(sender, COMMANDS_CYCLE_STOP_OTHERS)) ? plugin.getOnlinePlayerNames() : Collections.emptyList();
            case 3 -> List.of("alphabetical", "random");
            default -> Collections.emptyList();
        };
    }
}