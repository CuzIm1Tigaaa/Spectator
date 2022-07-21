package de.cuzim1tigaaa.spectator.cycle;

import de.cuzim1tigaaa.spectator.SpectatorPlugin;
import de.cuzim1tigaaa.spectator.files.Config;
import de.cuzim1tigaaa.spectator.files.Messages;
import de.cuzim1tigaaa.spectator.files.Paths;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;

public final class CycleHandler {
    private static final Map<Player, CycleTask> cycleTasks = new HashMap<>();
    private static final Map<Player, Integer> pausedCycles = new HashMap<>();

    public static boolean isPlayerCycling(Player player) {
        return cycleTasks.containsKey(player);
    }

    private static void sendBossBar(Player player, Player target) {
        if (!Config.getBoolean(Paths.CONFIG_SHOW_BOSS_BAR))
            return;

        CycleTask cTask = cycleTasks.get(player);
        BossBar bossBar = cTask.getBossBar() == null ? Bukkit.createBossBar("", BarColor.WHITE, BarStyle.SOLID) : cTask.getBossBar();
        bossBar.setTitle(target == null ? Messages.getMessage(Paths.MESSAGES_GENERAL_BOSS_BAR_WAITING) :
                Messages.getMessage(Paths.MESSAGES_COMMANDS_CYCLE_BOSS_BAR, "TARGET", target.getDisplayName()));
        bossBar.setColor(target == null ? BarColor.RED : BarColor.BLUE);

        bossBar.setVisible(true);
        bossBar.addPlayer(player);
        cTask.setBossBar(bossBar);
    }

    public static Map<Player, Integer> getPausedCycles() {
        return pausedCycles;
    }

    public static void breakCycle(Player player, boolean bossBar) {
        if (cycleTasks.containsKey(player)) {
            CycleTask task = cycleTasks.get(player);
            Bukkit.getScheduler().cancelTask(task.getTask().getTaskId());

            if (bossBar)
                resetBossBar(player);
        }

        SpectatorPlugin.getPlugin(SpectatorPlugin.class).getSpectateManager().dismountTarget(player);
    }

    private static void resetBossBar(Player player) {
        if (cycleTasks.containsKey(player) && cycleTasks.get(player).getBossBar() != null)
            cycleTasks.get(player).getBossBar().removeAll();
    }

    public static void next(Player player) {
        if (!cycleTasks.containsKey(player))
            return;

        Cycle cycle = cycleTasks.get(player).getCycle();

        if (!cycle.hasNextPlayer())
            cycleTasks.get(player).setCycle(new Cycle(player, cycle.getLastPlayer() != null ? cycle.getLastPlayer() : null));

        Player next = cycle.getNextPlayer(player);

        if (next == null || next.isDead())
            next = null;

        SpectatorPlugin.getPlugin(SpectatorPlugin.class).getSpectateManager().spectate(player, next);
        sendBossBar(player, next);
    }

    public static void startCycle(final Player player, int seconds, boolean restart) {
        breakCycle(player, true);
        cycleTasks.remove(player);

        int ticks = seconds * 20;

        BukkitTask task = Bukkit.getScheduler().runTaskTimer(SpectatorPlugin.getPlugin(SpectatorPlugin.class), () -> next(player), 0, ticks);
        cycleTasks.put(player, new CycleTask(seconds, new Cycle(player, null), task));

        if (restart)
            player.sendMessage(Messages.getMessage(Paths.MESSAGES_COMMANDS_CYCLE_RESTART, "INTERVAL", seconds));
        else
            player.sendMessage(Messages.getMessage(Paths.MESSAGES_COMMANDS_CYCLE_START, "INTERVAL", seconds));
    }

    public static void stopCycle(Player player) {
        breakCycle(player, true);

        if (cycleTasks.containsKey(player)) {
            resetBossBar(player);
            cycleTasks.remove(player);
        }

        player.sendMessage(Messages.getMessage(Paths.MESSAGES_COMMANDS_CYCLE_STOP));
    }

    public static void pauseCycle(Player player) {
        int interval = cycleTasks.get(player).getInterval();

        pausedCycles.put(player, interval);
        breakCycle(player, false);
        sendBossBar(player, null);

        player.sendMessage(Messages.getMessage(Paths.MESSAGES_COMMANDS_CYCLE_PAUSE));
    }

    public static void restartCycle(Player player) {
        int seconds = pausedCycles.getOrDefault(player, 0);
        pausedCycles.remove(player);

        if (seconds == 0)
            breakCycle(player, true);
        else
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

        public int getInterval() {
            return interval;
        }

        public BukkitTask getTask() {
            return task;
        }

        public Cycle getCycle() {
            return cycle;
        }

        public void setCycle(Cycle cycle) {
            this.cycle = cycle;
        }

        public BossBar getBossBar() {
            return bossBar;
        }

        public void setBossBar(BossBar bossBar) {
            this.bossBar = bossBar;
        }
    }
}