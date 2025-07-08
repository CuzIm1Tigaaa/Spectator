package de.cuzim1tigaaa.spectator.cycle;

import de.cuzim1tigaaa.spectator.Spectator;
import de.cuzim1tigaaa.spectator.extensions.MultiverseHandler;
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

	private final Set<Player> alreadyVisited;
	private List<Player> toVisit;

	public Cycle(Player owner, Player last, boolean alphabetical) {
		this.owner = owner;
		this.lastPlayer = last;
		this.alphabetical = alphabetical;

		this.alreadyVisited = new HashSet<>();
		this.toVisit = Collections.emptyList();
	}

	public Player getNextTarget(Spectator plugin) {
		if(toVisit.isEmpty())
			alreadyVisited.clear();

		updateLists(plugin);

		if(toVisit.isEmpty())
			return null;

		Player target;
		if(alphabetical) {
			target = toVisit.stream()
					.sorted((t1, t2) -> t1.getName().compareToIgnoreCase(t2.getName()))
					.filter(p -> !p.equals(lastPlayer))
					.findFirst().orElse(toVisit.getFirst());
		}else {
			do {
				target = toVisit.get(random.nextInt(toVisit.size()));
			}while(toVisit.size() > 1 && target.equals(lastPlayer));
		}

		Player next = visit(target);
		Spectator.debug(String.format("Next Target: %-16s\t\ttoVisit: %s", target.getName(),
				toVisit.stream().map(Player::getName).collect(Collectors.joining(", "))));
		return next;
	}

	private Player visit(Player player) {
		this.lastPlayer = player;
		this.alreadyVisited.add(player);
		this.toVisit.remove(player);
		return player;
	}

	private void updateLists(Spectator plugin) {
		Set<Player> updatedToVisit = new HashSet<>(plugin.getSpectateAPI().getSpectateablePlayers());
		updatedToVisit.remove(owner);
		updatedToVisit.removeIf(p -> {
			if(p == null || !p.isOnline())
				return true;

			if(!owner.hasPermission(Permissions.BYPASS_SPECTATEALL) && p.hasPermission(Permissions.BYPASS_SPECTATED))
				return true;

			return MultiverseHandler.getInstance().canPlayerJoinWorld(owner, p.getWorld());
		});

		alreadyVisited.removeIf(p -> !p.isOnline());
		updatedToVisit.removeAll(alreadyVisited);

		toVisit = new ArrayList<>(updatedToVisit);
	}
}