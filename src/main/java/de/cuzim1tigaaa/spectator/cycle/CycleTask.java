package de.cuzim1tigaaa.spectator.cycle;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.boss.BossBar;

class CycleTask {

	@Getter private final int interval, taskId;
	@Getter @Setter private Cycle cycle;
	@Getter @Setter private BossBar bossBar;
	@Getter @Setter private Integer actionBar;

	public CycleTask(int interval, Cycle cycle, int taskId) {
		this.interval = interval;
		this.cycle = cycle;
		this.taskId = taskId;
		this.actionBar = null;
	}
}