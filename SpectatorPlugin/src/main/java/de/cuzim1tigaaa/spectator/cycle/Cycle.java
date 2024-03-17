package de.cuzim1tigaaa.spectator.cycle;

import com.onarandombox.MultiverseCore.api.MultiverseWorld;
import de.cuzim1tigaaa.spectator.Spectator;
import de.cuzim1tigaaa.spectator.files.Permissions;
import lombok.Getter;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class Cycle {

	private static final ThreadLocalRandom random = ThreadLocalRandom.current();

	@Getter
	private final Player owner;
	@Getter
	private final boolean alphabetical;
	@Getter
	private Player lastPlayer;

	private final List<Player> alreadyVisited;
	private List<Player> toVisit;

	public Cycle(Player owner, Player last, boolean alphabetical) {
		this.owner = owner;
		this.lastPlayer = last;
		this.alphabetical = alphabetical;

		this.alreadyVisited = new ArrayList<>();
		this.toVisit = new ArrayList<>();
	}

	public Player getNextTarget(Spectator plugin) {
		updateLists(plugin);
		if(toVisit.isEmpty()) {
			alreadyVisited.clear();
			updateLists(plugin);

			if(toVisit.isEmpty())
				return null;
		}

		Player target = alphabetical ? toVisit.stream().sorted((t1, t2) -> t1.getName().compareToIgnoreCase(t2.getName())).toList().get(0) :
				toVisit.get(random.nextInt(toVisit.size()));

		Spectator.Debug(String.format("Next Target: %-16s\t\ttoVisit: %s", target.getName(),
				toVisit.stream().map(Player::getName).collect(Collectors.joining(", "))));

		if(toVisit.size() > 1 && target.equals(lastPlayer))
			return getNextTarget(plugin);

		return visit(target);
	}

	private Player visit(Player player) {
		this.lastPlayer = player;
		this.alreadyVisited.add(player);
		return player;
	}

	private void updateLists(Spectator plugin) {
		toVisit.addAll(plugin.getSpectateUtils().getSpectateablePlayers());
		toVisit.remove(owner);
		toVisit.removeIf(p -> p == null || !p.isOnline());

		if(!owner.hasPermission(Permissions.BYPASS_SPECTATEALL))
			toVisit.removeIf(p -> p.hasPermission(Permissions.BYPASS_SPECTATED));

		if(plugin.getMultiverseCore() != null && plugin.getMultiverseCore().getMVConfig().getEnforceAccess()) {
			toVisit.removeIf(p -> {
				MultiverseWorld world = plugin.getMultiverseCore().getMVWorldManager().getMVWorld(p.getWorld());
				return !owner.hasPermission(world.getAccessPermission());
			});
		}

		alreadyVisited.removeIf(p -> !p.isOnline());
		toVisit.removeAll(alreadyVisited);
		toVisit = new ArrayList<>(new HashSet<>(toVisit));
	}
}