package de.cuzim1tigaaa.spectator.cycle;

import de.cuzim1tigaaa.spectator.Spectator;
import de.cuzim1tigaaa.spectator.files.*;
import org.bukkit.Bukkit;
import org.bukkit.boss.*;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;

public class CycleHandler {

    public static boolean isPlayerCycling(Player player) {
        return cycleTasks.containsKey(player);
    }

    private static final Spectator plugin = Spectator.getPlugin(Spectator.class);

    private static final Map<Player, CycleTask> cycleTasks = new HashMap<>();

    private static void sendBossBar(Player player, Player target) {
        CycleTask cTask = cycleTasks.get(player);
        BossBar bossBar = cTask.getBossBar() == null ? Bukkit.createBossBar("", BarColor.BLUE, BarStyle.SOLID) : cTask.getBossBar();
        bossBar.setTitle(Messages.getMessage(Paths.MESSAGES_COMMANDS_CYCLE_BOSS_BAR, "TARGET", target.getDisplayName()));
        bossBar.setVisible(true);
        bossBar.addPlayer(player);
        cTask.setBossBar(bossBar);
    }

    private static final Map<Player, Integer> pausedCycles = new HashMap<>();
    public static Map<Player, Integer> getPausedCycles() { return pausedCycles; }

    public static void breakCycle(Player player) {
        if(cycleTasks.containsKey(player)) {
            CycleTask task = cycleTasks.get(player);
            Bukkit.getScheduler().cancelTask(task.getTask().getTaskId());
            if(task.getBossBar() != null) cycleTasks.get(player).getBossBar().removeAll();
            cycleTasks.remove(player);
        }
        plugin.getSpectateManager().dismountTarget(player);
    }

    public static void startCycle(final Player player, int seconds, boolean restart) {
        breakCycle(player);
        int ticks = seconds * 20;
        BukkitTask task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            Cycle cycle = cycleTasks.get(player).getCycle();
            if(!cycle.hasNextPlayer()) cycleTasks.get(player).setCycle(new Cycle(player, cycle.getLastPlayer() != null ? cycle.getLastPlayer() : null));
            Player next = cycle.getNextPlayer(player);
            if(next != null && !next.isDead()) {
                plugin.getSpectateManager().spectate(player, next);
                if(Config.getBoolean(Paths.CONFIG_SHOW_BOSS_BAR)) sendBossBar(player, next);
            }
        }, 0, ticks);
        cycleTasks.put(player, new CycleTask(seconds, new Cycle(player, null), task));

        if(restart) player.sendMessage(Messages.getMessage(Paths.MESSAGES_COMMANDS_CYCLE_RESTART, "INTERVAL", seconds));
        else player.sendMessage(Messages.getMessage(Paths.MESSAGES_COMMANDS_CYCLE_START, "INTERVAL", seconds));
    }

    public static void stopCycle(Player player) {
        breakCycle(player);
        player.sendMessage(Messages.getMessage(Paths.MESSAGES_COMMANDS_CYCLE_STOP));
    }

    public static void pauseCycle(Player player) {
        pausedCycles.put(player, cycleTasks.get(player).getInterval());
        breakCycle(player);
        player.sendMessage(Messages.getMessage(Paths.MESSAGES_COMMANDS_CYCLE_PAUSE));
    }

    public static void restartCycle(Player player) {
        int seconds = pausedCycles.containsKey(player) ? pausedCycles.get(player) : cycleTasks.get(player).getInterval();
        breakCycle(player);
        pausedCycles.remove(player);
        startCycle(player, seconds, true);
    }

    private static class CycleTask {

        private final int interval;
        private final BukkitTask task;
        private Cycle cycle;
        private BossBar bossBar;

        public CycleTask(int interval, Cycle cycle, BukkitTask task) {
            this.interval = interval;
            this.cycle = cycle;
            this.task = task;
        }

        public int getInterval() { return interval; }
        public BukkitTask getTask() { return task; }

        public Cycle getCycle() { return cycle; }
        public void setCycle(Cycle cycle) { this.cycle = cycle; }

        public BossBar getBossBar() { return bossBar; }
        public void setBossBar(BossBar bossBar) { this.bossBar = bossBar; }
    }
}