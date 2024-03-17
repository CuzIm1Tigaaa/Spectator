package de.cuzim1tigaaa.spectator.extensions;

import de.cuzim1tigaaa.spectator.Spectator;
import org.bstats.charts.SingleLineChart;

public class Metrics {

	public Metrics(Spectator plugin) {
		if(plugin.getServer().getVersion().contains("MockBukkit"))
			return;

		org.bstats.bukkit.Metrics metrics = new org.bstats.bukkit.Metrics(plugin, 12235);
		metrics.addCustomChart(new SingleLineChart("spectating_players", () -> plugin.getSpectateUtils().getSpectators().size()));
	}
}