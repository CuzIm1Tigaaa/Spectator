package de.cuzim1tigaaa.spectator.extensions;

import de.cuzim1tigaaa.spectator.SpectateAPI;
import de.cuzim1tigaaa.spectator.Spectator;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Collectors;

public class Placeholders extends PlaceholderExpansion {

	private final Spectator plugin;
	private final SpectateAPI spectateAPI;

	public Placeholders(Spectator plugin) {
		this.plugin = plugin;
		this.spectateAPI = plugin.getSpectateAPI();
	}

	@Override
	public @NotNull String getIdentifier() {
		return plugin.getDescription().getName();
	}

	@Override
	public @NotNull String getAuthor() {
		return plugin.getDescription().getAuthors().get(0);
	}

	@Override
	public @NotNull String getVersion() {
		return plugin.getDescription().getVersion();
	}

	@Override
	public boolean persist() {
		return true;
	}

	@Override
	public String onRequest(OfflinePlayer oPlayer, @NotNull String params) {
		if(!(oPlayer instanceof Player player))
			return null;

		switch(params.toLowerCase()) {
			case "target" -> {
				if(spectateAPI.getTargetOf(player) == null)
					return null;
				return spectateAPI.getTargetOf(player).getName();
			}
			case "target_displayname" -> {
				if(spectateAPI.getTargetOf(player) == null)
					return null;
				return spectateAPI.getTargetOf(player).getDisplayName();
			}
			case "target_spectators" -> {
				if(spectateAPI.getSpectators().isEmpty())
					return null;
				return spectateAPI.getSpectatorsOf(player).stream().map(Player::getName).collect(Collectors.joining(", "));
			}
			case "state" -> {
				if(spectateAPI.getSpectateInfo(player) == null)
					return "NONE";
				return spectateAPI.getSpectateInfo(player).getState().name();
			}
			case "cycle_interval" -> {
				if(spectateAPI.isCyclingSpectator(player))
					return String.valueOf(spectateAPI.getCycleTask(player).getInterval());
				return null;
			}
			case "spectators" -> {
				if(spectateAPI.getSpectators().isEmpty())
					return null;
				return spectateAPI.getSpectators().stream().map(Player::getName).collect(Collectors.joining(", "));
			}
			default -> {
				return null;
			}
		}
	}
}