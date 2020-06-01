package de.cuzim1tigaaa.spectate.cycle;

import de.cuzim1tigaaa.spectate.Main;
import de.cuzim1tigaaa.spectate.files.Config;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;

public class CycleHandler {

    private Map<Player, CycleTask> cycleTasks = new HashMap<>();
    private Map<Player, Cycle> playerCycles = new HashMap<>();
    private Map<Player, Integer> pauseCycle = new HashMap<>();

    public boolean isPlayerCycling(Player player) {
        return playerCycles.containsKey(player);
    }
    public boolean isPlayerPaused(Player player) {
        return pauseCycle.containsKey(player);
    }

    public void startCycle(final Player player, int ticks) {
        playerCycles.put(player, new Cycle(player, null));
        if(Bukkit.getOnlinePlayers().size() > 1) {
            BukkitTask task = Bukkit.getScheduler().runTaskTimer(Main.getInstance(), new Runnable() {
                @Override
                public void run() {
                    Cycle cycle = playerCycles.get(player);
                    if(!cycle.hasNextPlayer()) {
                        Player last = cycle.getLastPlayer();
                        cycle = new Cycle(player, last);
                        playerCycles.put(player, cycle);
                    }
                    Player next = cycle.getNextPlayer();
                    if(next != null) {
                        Main.getInstance().getMethods().spectate(player, next);
                    }else {
                        stopCycle(player);
                    }
                }
            }, 0, ticks);
            cycleTasks.put(player, new CycleTask(task, ticks));
            player.sendMessage(Config.getMessage("Config.Spectate.Cycle.start", "interval", ticks/20));
        }else {
            player.sendMessage(Config.getMessage("Config.Error.noPlayers"));
        }
    }
    public void stopCycle(Player player) {
        if(isPlayerPaused(player)) {
            pauseCycle.remove(player);
        }else {
            cycleTasks.get(player).getTask().cancel();
            cycleTasks.remove(player);
            playerCycles.remove(player);
        }
        Main.getInstance().getPlayerListener().dismountTarget(player);
    }
    public void restartCycle(Player player) {
        CycleTask task = cycleTasks.get(player);
        task.getTask().cancel();
        cycleTasks.remove(player);
        startCycle(player, task.getInterval());
    }
    public void pauseCycle(Player player) {
        int interval = cycleTasks.get(player).getInterval();
        CycleTask task = cycleTasks.get(player);
        task.getTask().cancel();
        cycleTasks.remove(player);
        playerCycles.remove(player);
        pauseCycle.put(player, interval);
    }
    public void resumeCycle(Player player) {
        int interval = pauseCycle.get(player);
        startCycle(player, interval);
        player.sendMessage(Config.getMessage("Config.Spectate.Cycle.resume", "interval", interval));
    }

    private class CycleTask {

        private BukkitTask task;
        private int interval;

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
