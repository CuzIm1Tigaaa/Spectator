package de.cuzim1tigaaa.spectator.extensions;

import de.cuzim1tigaaa.spectator.Spectator;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SingleLineChart;

public class MetricsClass {

	public MetricsClass(Spectator plugin) {
		if(plugin.getServer().getVersion().contains("MockBukkit"))
			return;

		Metrics metrics = new Metrics(plugin, 12235);
		metrics.addCustomChart(new SingleLineChart("spectating_players", () ->
				plugin.getSpectateUtils().getSpectators().size()));
	}
}