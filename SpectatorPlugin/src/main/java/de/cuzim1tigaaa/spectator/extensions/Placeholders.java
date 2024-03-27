package de.cuzim1tigaaa.spectator.extensions;

import de.cuzim1tigaaa.spectator.Spectator;
import de.cuzim1tigaaa.spectator.spectate.SpectateUtils;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Collectors;

public class Placeholders extends PlaceholderExpansion {

	private final Spectator plugin;
	private final SpectateUtils spectateUtils;

	public Placeholders(Spectator plugin) {
		this.plugin = plugin;
		this.spectateUtils = plugin.getSpectateUtils();
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
				if(spectateUtils.getTargetOf(player) == null)
					return null;
				return spectateUtils.getTargetOf(player).getName();
			}
			case "target_displayname" -> {
				if(spectateUtils.getTargetOf(player) == null)
					return null;
				return spectateUtils.getTargetOf(player).getDisplayName();
			}
			case "target_spectators" -> {
				if(spectateUtils.getSpectators().isEmpty())
					return null;
				return spectateUtils.getSpectatorsOf(player).stream().map(Player::getName).collect(Collectors.joining(", "));
			}
			case "state" -> {
				if(spectateUtils.getSpectateInformation(player) == null)
					return "NONE";
				return spectateUtils.getSpectateInformation(player).getState().name();
			}
			case "cycle_interval" -> {
				if(spectateUtils.isCycling(player))
					return String.valueOf(spectateUtils.getSpectateCycle().get(player.getUniqueId()).getInterval());
				return null;
			}
			case "spectators" -> {
				if(spectateUtils.getSpectators().isEmpty())
					return null;
				return spectateUtils.getSpectators().stream().map(Player::getName).collect(Collectors.joining(", "));
			}
			default -> {
				return null;
			}
		}
	}
}