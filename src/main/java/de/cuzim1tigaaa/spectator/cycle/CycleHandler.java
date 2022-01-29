package de.cuzim1tigaaa.spectator.cycle;

import de.cuzim1tigaaa.spectator.Spectator;
import de.cuzim1tigaaa.spectator.files.Messages;
import de.cuzim1tigaaa.spectator.files.Paths;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;

public class CycleHandler {

    private static final Spectator plugin = Spectator.getPlugin(Spectator.class);

    private static final Map<Player, CycleTask> cycleTasks = new HashMap<>();
    private static final Map<Player, Cycle> playerCycles = new HashMap<>();

    private static final Map<Player, Integer> pausedCycles = new HashMap<>();
    public static Map<Player, Integer> getPausedCycles() { return pausedCycles; }

    public static boolean isPlayerCycling(Player player) {
        return playerCycles.containsKey(player);
    }

    public static void startCycle(final Player player, int seconds) {
        pausedCycles.remove(player);
        int ticks = seconds * 20;
        playerCycles.put(player, new Cycle(player, null));
        BukkitTask task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            Cycle cycle = playerCycles.get(player);
            if(!cycle.hasNextPlayer()) {
                Player last = cycle.getLastPlayer();
                playerCycles.put(player, new Cycle(player, last));
            }
            Player next = cycle.getNextPlayer(player);
            if(next != null) plugin.getSpectateManager().spectate(player, next);
            else pauseCycle(player);
        }, 0, ticks);
        cycleTasks.put(player, new CycleTask(task, ticks));
        player.sendMessage(Messages.getMessage(Paths.MESSAGES_COMMANDS_CYCLE_START, "INTERVAL", seconds));
    }
    public static void stopCycle(Player player) {
        cycleTasks.get(player).getTask().cancel();
        cycleTasks.remove(player);
        playerCycles.remove(player);
        plugin.getSpectateManager().dismountTarget(player);
    }

    public static void pauseCycle(Player player) {
        pausedCycles.put(player, cycleTasks.get(player).getInterval());
        stopCycle(player);
    }
    public static void restartCycle(Player player) {
        if(pausedCycles.containsKey(player)) {
            startCycle(player, pausedCycles.get(player));
            pausedCycles.remove(player);
        }else {
            int interval = cycleTasks.get(player).getInterval();
            stopCycle(player);
            startCycle(player, interval);
        }
    }

    private record CycleTask(BukkitTask task, int interval) {
        public BukkitTask getTask() { return task; }
        public int getInterval() { return interval; }
    }
}