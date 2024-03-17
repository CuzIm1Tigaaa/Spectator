package de.cuzim1tigaaa.spectator.spectate;

import de.cuzim1tigaaa.spectator.cycle.CycleTask;
import de.cuzim1tigaaa.spectator.player.PlayerAttributes;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

@Getter
public class SpectateInformation {

	private final Player spectator;
	@Setter private Player target;
	@Setter private SpectateState state;
	@Setter private CycleTask cycleTask;
	private final Location location;
	private final Map<World, PlayerAttributes> attributes;

	public SpectateInformation(Player spectator, Player target) {
		this.spectator = spectator;
		this.target = target;
		this.state = SpectateState.SPECTATING;
		this.location = spectator.getLocation();
		this.attributes = new HashMap<>();
	}

	public void saveAttributes() {
		this.attributes.put(spectator.getWorld(), new PlayerAttributes(spectator));
	}

	public void restoreAttributes() {
		PlayerAttributes.restorePlayerAttributes(spectator,
				this.attributes.remove(spectator.getWorld()));
	}

	@Override
	public String toString() {
		return "SpectateInformation{" +
				"spectator=" + spectator +
				", target=" + target +
				", state=" + state +
				", cycleTask=" + cycleTask +
				", location=" + location +
				", attributes=" + attributes +
				'}';
	}
}