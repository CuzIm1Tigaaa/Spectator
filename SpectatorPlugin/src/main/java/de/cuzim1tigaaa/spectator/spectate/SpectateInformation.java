package de.cuzim1tigaaa.spectator.spectate;

import de.cuzim1tigaaa.spectator.Spectator;
import de.cuzim1tigaaa.spectator.cycle.CycleTask;
import de.cuzim1tigaaa.spectator.player.PlayerAttributes;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;

import java.util.*;

@Getter
public class SpectateInformation {

	private final Player spectator;
	@Setter private Player target;
	@Setter private SpectateState state;
	@Setter private CycleTask cycleTask;
	@Setter private boolean hideArmorStands;
	private final Map<World, PlayerAttributes> attributes;

	public SpectateInformation(Player spectator, Player target) {
		this.spectator = spectator;
		this.target = target;
		this.state = SpectateState.SPECTATING;
		this.attributes = new HashMap<>();
		this.hideArmorStands = false;
	}

	public void saveAttributes() {
		this.attributes.put(spectator.getWorld(), new PlayerAttributes(spectator));
	}

	public void restoreAttributes(boolean gameModeChange) {
		PlayerAttributes.restorePlayerAttributes(spectator,
				this.attributes.remove(spectator.getWorld()), gameModeChange);
	}

	@Override
	public String toString() {
		return "SpectateInformation{" +
				"spectator=" + spectator +
				", target=" + target +
				", state=" + state +
				", attributes=" + attributes +
				'}';
	}
}