package de.cuzim1tigaaa.spectator.commands;

import de.cuzim1tigaaa.spectator.SpectateAPI;
import de.cuzim1tigaaa.spectator.Spectator;
import de.cuzim1tigaaa.spectator.cycle.Cycle;
import de.cuzim1tigaaa.spectator.cycle.CycleTask;
import de.cuzim1tigaaa.spectator.files.*;
import de.cuzim1tigaaa.spectator.spectate.SpectateUtilsCycle;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import java.util.*;

import static de.cuzim1tigaaa.spectator.files.Permissions.*;

public class SpectateCycle implements CommandExecutor, TabCompleter {

    private final Spectator plugin;
    private final SpectateAPI spectateAPI;
    private final SpectateUtilsCycle spectateCycle;

    public SpectateCycle(Spectator plugin) {
        Objects.requireNonNull(plugin.getCommand("spectatecycle")).setExecutor(this);
        this.plugin = plugin;
        this.spectateAPI = plugin.getSpectateAPI();
        this.spectateCycle = spectateAPI.getSpectateCycle();
    }

    @Override
    public boolean onCommand(@Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String label, @Nonnull String[] args) {
        if(!(sender instanceof Player player)) {
            Messages.sendMessage(sender, Paths.MESSAGE_DEFAULT_SENDER);
            return true;
        }

        if(!hasPermission(sender, COMMANDS_SPECTATE_CYCLE)) {
            Messages.sendMessage(player, Paths.MESSAGE_DEFAULT_PERMISSION);
            return true;
        }

        if(args.length < 1) {
            Messages.sendMessage(player, Paths.MESSAGE_DEFAULT_SYNTAX, "USAGE", "/spectatecycle [start|stop]");
            return true;
        }

        switch(args[0].toLowerCase()) {
            case "start" -> {
                if(args.length < 2) {
                    Messages.sendMessage(player, Paths.MESSAGE_DEFAULT_SYNTAX, "USAGE", "/spectatecycle start <interval> [alphabetical|random]");
                    return true;
                }

                int seconds = -1;
                boolean alphabetical = args.length >= 3 && args[2].equalsIgnoreCase("alphabetical");

                try {
                    seconds = Integer.parseInt(args[1]);
                }catch(NumberFormatException ignored) {
                    Messages.sendMessage(player, Paths.MESSAGES_GENERAL_NUMBERFORMAT);
                }

                int min = Config.getInt(Paths.CONFIG_CYCLE_MIN_INTERVAL);
                int max = Config.getInt(Paths.CONFIG_CYCLE_MAX_INTERVAL);

                if(min > 0 && seconds < min) {
                    Messages.sendMessage(player, Paths.MESSAGES_COMMANDS_CYCLE_INTERVAL_TOO_SMALL, "MINIMUM", min);
                    return true;
                }

                if(max > 0 && seconds > max) {
                    Messages.sendMessage(player, Paths.MESSAGES_COMMANDS_CYCLE_INTERVAL_TOO_BIG, "MAXIMUM", max);
                    return true;
                }

                spectateAPI.getSpectateGeneral().getSpectateStartLocation().put(player.getUniqueId(), player.getLocation());
                startSpectateCycle(player, seconds, alphabetical);
                return true;
            }

            case "stop" -> {
                if(!hasPermissions(sender, COMMAND_SPECTATE_GENERAL, COMMAND_SPECTATE_OTHERS, COMMAND_SPECTATE_HERE)) {
                    spectateAPI.getSpectateGeneral().unspectate(player, true);
                    return true;
                }

                if(args.length == 1) {
                    if(!spectateAPI.isCyclingSpectator(player)) {
                        Messages.sendMessage(player, Paths.MESSAGES_COMMANDS_CYCLE_NOT_CYCLING);
                        return true;
                    }
                    spectateCycle.stopCycle(player);
                    Messages.sendMessage(player, Paths.MESSAGES_COMMANDS_CYCLE_STOP);
                    return true;
                }

                if(hasPermission(player, COMMANDS_CYCLE_STOP_OTHERS)) {
                    Player target = Bukkit.getPlayer(args[1]);

                    if(target == null || !target.isOnline()) {
                        Messages.sendMessage(player, Paths.MESSAGES_GENERAL_OFFLINEPLAYER, "TARGET", args[1]);
                        return true;
                    }

                    if(!spectateAPI.isCyclingSpectator(target)) {
                        Messages.sendMessage(player, Paths.MESSAGES_COMMANDS_CYCLE_TARGET_NOT_CYCLING, "TARGET", target.getName());
                        return true;
                    }

                    spectateCycle.stopCycle(target);
                    Messages.sendMessage(target, Paths.MESSAGES_COMMANDS_CYCLE_STOP);
                    return true;
                }
            }
        }
        Messages.sendMessage(player, Paths.MESSAGE_DEFAULT_SYNTAX, "USAGE", "/spectatecycle [start|stop]");
        return true;
    }

    private void startSpectateCycle(Player spectator, int seconds, boolean alphabetical) {
        if(!Config.getBoolean(Paths.CONFIG_CYCLE_NO_PLAYERS) && Bukkit.getOnlinePlayers().size() <= 1) {
            Messages.sendMessage(spectator, Paths.MESSAGES_GENERAL_NOPLAYERS);
            return;
        }

        if(spectateAPI.isCyclingSpectator(spectator)) {
            Messages.sendMessage(spectator, Paths.MESSAGES_COMMANDS_CYCLE_CYCLING);
            return;
        }

        Messages.sendMessage(spectator, Paths.MESSAGES_COMMANDS_CYCLE_START,
                "INTERVAL", seconds, "ORDER", alphabetical ? "Alphabetic" : "Random");
        spectateCycle.startCycle(spectator, new CycleTask(seconds, new Cycle(spectator, null, alphabetical)));
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