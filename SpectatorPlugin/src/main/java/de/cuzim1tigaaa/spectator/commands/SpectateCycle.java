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
        if(!hasPermission(sender, COMMANDS_SPECTATE_CYCLE)) {
            Messages.sendMessage(sender, Paths.MESSAGE_DEFAULT_PERMISSION);
            return true;
        }

        if(args.length < 1) {
            Messages.sendMessage(sender, Paths.MESSAGE_DEFAULT_SYNTAX, "USAGE", "/spectatecycle [start|stop]");
            return true;
        }

        switch(args[0].toLowerCase()) {
            case "start" -> handleStartCycle(sender, args);
            case "stop" -> handleStopCycle(sender, args);
            case "next" -> handleNextCycle(sender, args);
            default -> {
                Messages.sendMessage(sender, Paths.MESSAGE_DEFAULT_SYNTAX, "USAGE", "/spectatecycle [start|stop]");
                return true;
            }
        }
        return true;
    }

    private void handleStartCycle(CommandSender sender, String[] args) {
        if(args.length < 2) {
            Messages.sendMessage(sender, Paths.MESSAGE_DEFAULT_SYNTAX, "USAGE", "/spectatecycle start <interval> [alphabetical|random]");
            return;
        }

        Integer seconds = spectateAPI.getInt(args[1]);
        if(seconds == null || seconds <= 0) {
            Messages.sendMessage(sender, Paths.MESSAGES_GENERAL_NUMBERFORMAT);
            return;
        }

        boolean alphabetical = args.length >= 3 && args[2].equalsIgnoreCase("alphabetical");
        int min = Config.getInt(Paths.CONFIG_CYCLE_MIN_INTERVAL);
        int max = Config.getInt(Paths.CONFIG_CYCLE_MAX_INTERVAL);

        if(min > 0 && seconds < min) {
            Messages.sendMessage(sender, Paths.MESSAGES_COMMANDS_CYCLE_INTERVAL_TOO_SMALL, "MINIMUM", min);
            return;
        }

        if(max > 0 && seconds > max) {
            Messages.sendMessage(sender, Paths.MESSAGES_COMMANDS_CYCLE_INTERVAL_TOO_BIG, "MAXIMUM", max);
            return;
        }

        Player player;
        if(args.length <= 3 || !hasPermission(sender, COMMAND_SPECTATE_CHANGE_OTHERS)) {
            if(!(sender instanceof Player)) {
                Messages.sendMessage(sender, Paths.MESSAGE_DEFAULT_SENDER);
                return;
            }
            player = (Player) sender;
            spectateAPI.getSpectateGeneral().getSpectateStartLocation().put(player.getUniqueId(), player.getLocation());
            startSpectateCycle(player, seconds, alphabetical);
            return;
        }

        player = Bukkit.getPlayer(args[3]);
        if(player == null || !player.isOnline()) {
            Messages.sendMessage(sender, Paths.MESSAGES_GENERAL_OFFLINEPLAYER, "TARGET", args[3]);
            return;
        }

        spectateAPI.getSpectateGeneral().getSpectateStartLocation().put(player.getUniqueId(), player.getLocation());
        startSpectateCycle(player, seconds, alphabetical);
        Messages.sendMessage(sender, Paths.MESSAGES_COMMANDS_CYCLE_START_OTHER,
                "INTERVAL", seconds, "TARGET", player.getName(), "ORDER", alphabetical ? "Alphabetic" : "Random");
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

    private void handleStopCycle(CommandSender sender, String[] args) {
        if(sender instanceof Player player) {
            if(!hasPermissions(player, COMMAND_SPECTATE_GENERAL, COMMAND_SPECTATE_OTHERS, COMMAND_SPECTATE_HERE)) {
                spectateAPI.getSpectateGeneral().unspectate(player, true);
                return;
            }

            if(args.length == 1) {
                if(!spectateAPI.isCyclingSpectator(player)) {
                    Messages.sendMessage(player, Paths.MESSAGES_COMMANDS_CYCLE_NOT_CYCLING);
                    return;
                }
                spectateCycle.stopCycle(player);
                Messages.sendMessage(player, Paths.MESSAGES_COMMANDS_CYCLE_STOP);
                return;
            }
        }

        if(hasPermission(sender, COMMANDS_CYCLE_STOP_OTHERS)) {
            Player target = Bukkit.getPlayer(args[1]);

            if(target == null || !target.isOnline()) {
                Messages.sendMessage(sender, Paths.MESSAGES_GENERAL_OFFLINEPLAYER, "TARGET", args[1]);
                return;
            }

            if(!spectateAPI.isCyclingSpectator(target)) {
                Messages.sendMessage(sender, Paths.MESSAGES_COMMANDS_CYCLE_TARGET_NOT_CYCLING, "TARGET", target.getName());
                return;
            }

            spectateCycle.stopCycle(target);
            Messages.sendMessage(target, Paths.MESSAGES_COMMANDS_CYCLE_STOP);
        }
    }

    private void handleNextCycle(CommandSender sender, String[] args) {
        if(!(sender instanceof Player player)) {
            Messages.sendMessage(sender, Paths.MESSAGE_DEFAULT_SENDER);
            return;
        }

        if(!hasPermission(player, COMMANDS_CYCLE_FORCE_NEXT)) {
            Messages.sendMessage(player, Paths.MESSAGE_DEFAULT_PERMISSION);
            return;
        }

        if(!spectateAPI.isCyclingSpectator(player)) {
            Messages.sendMessage(player, Paths.MESSAGES_COMMANDS_CYCLE_NOT_CYCLING);
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if(target == null || !target.isOnline()) {
            Messages.sendMessage(player, Paths.MESSAGES_GENERAL_OFFLINEPLAYER, "TARGET", args[1]);
            return;
        }

        spectateCycle.forceNextTarget(player, target);
        Messages.sendMessage(player, Paths.MESSAGES_COMMANDS_CYCLE_NEXT, "TARGET", target.getName());
    }

    @Override
    public List<String> onTabComplete(@Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String s, @Nonnull String[] args) {
        return switch(args.length) {
            case 1 -> List.of("start", "stop", "next");
            case 2 -> {
                if(args[0].equalsIgnoreCase("stop") && hasPermission(sender, COMMANDS_CYCLE_STOP_OTHERS))
                    yield plugin.getOnlinePlayerNames();
                if(args[0].equalsIgnoreCase("next") && hasPermission(sender, COMMANDS_CYCLE_FORCE_NEXT))
                    yield plugin.getOnlinePlayerNames();
                yield Collections.emptyList();
            }
            case 3 -> List.of("alphabetical", "random");
            case 4 -> {
                if(args[0].equalsIgnoreCase("start") && hasPermission(sender, COMMAND_SPECTATE_CHANGE_OTHERS))
                    yield plugin.getOnlinePlayerNames();
                else yield Collections.emptyList();
            }
            default -> Collections.emptyList();
        };
    }
}