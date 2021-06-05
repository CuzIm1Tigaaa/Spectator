package de.cuzim1tigaaa.spectate.cycle;

import de.cuzim1tigaaa.spectate.Main;
import de.cuzim1tigaaa.spectate.files.Config;
import de.cuzim1tigaaa.spectate.files.Paths;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;

public class CycleHandler {

    private static final Map<Player, CycleTask> cycleTasks = new HashMap<>();
    private static final Map<Player, Cycle> playerCycles = new HashMap<>();

    public static boolean isPlayerCycling(Player player) {
        return playerCycles.containsKey(player);
    }

    public static void startCycle(final Player player, int seconds) {
        int ticks = seconds * 20;
        playerCycles.put(player, new Cycle(player, null));
        BukkitTask task = Bukkit.getScheduler().runTaskTimer(Main.getInstance(), () -> {
            Cycle cycle = playerCycles.get(player);
            if(!cycle.hasNextPlayer()) {
                Player last = cycle.getLastPlayer();
                cycle = new Cycle(player, last);
                playerCycles.put(player, cycle);
            }
            Player next = cycle.getNextPlayer(player);
            if(next != null) Main.getInstance().getMethods().spectate(player, next);
            else stopCycle(player);
        }, 0, ticks);
        cycleTasks.put(player, new CycleTask(task, ticks));
        player.sendMessage(Config.getMessage(Paths.MESSAGES_COMMANDS_CYCLE_START, "INTERVAL", seconds));
    }
    public static void stopCycle(Player player) {
        cycleTasks.get(player).getTask().cancel();
        cycleTasks.remove(player);
        playerCycles.remove(player);
        Main.getInstance().dismountTarget(player);
    }
    public static void restartCycle(Player player) {
        CycleTask task = cycleTasks.get(player);
        task.getTask().cancel();
        cycleTasks.remove(player);
        startCycle(player, task.getInterval());
    }

    private static class CycleTask {

        private final BukkitTask task;
        private final int interval;

        public CycleTask(BukkitTask task, int interval) {
            this.task = task;
            this.interval = interval;
        }

        public BukkitTask getTask() {
            return task;
        }
        public int getInterval() {
            return interval;
        }
    }
}