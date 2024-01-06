package de.cuzim1tigaaa.spectator.cycle;

import de.cuzim1tigaaa.spectator.Spectator;
import de.cuzim1tigaaa.spectator.files.Permissions;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class Cycle {

	private final Spectator plugin = Spectator.getPlugin(Spectator.class);

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

	public Player getNextTarget() {
		updateLists();
		if(toVisit.isEmpty()) {
			alreadyVisited.clear();
			updateLists();

			if(toVisit.isEmpty())
				return null;
		}

		Player target = alphabetical ? toVisit.stream().sorted((t1, t2) -> t1.getName().compareToIgnoreCase(t2.getName())).toList().get(0) :
				toVisit.get(random.nextInt(toVisit.size()));

		plugin.Debug(String.format("Next Target: %-16s\t\ttoVisit: %s", target.getName(),
				toVisit.stream().map(Player::getName).collect(Collectors.joining(", "))));

		if(toVisit.size() > 1 && target.equals(lastPlayer))
			return getNextTarget();

		return visit(target);
	}

	private Player visit(Player player) {
		this.lastPlayer = player;
		this.alreadyVisited.add(player);
		return player;
	}

	private void updateLists() {
		toVisit.addAll(Bukkit.getOnlinePlayers());
		toVisit.remove(owner);
		toVisit.removeAll(plugin.getSpectateUtils().getSpectators());
		toVisit.removeIf(p -> p == null || !p.isOnline());

		if(!owner.hasPermission(Permissions.BYPASS_SPECTATEALL))
			toVisit.removeIf(p -> p.hasPermission(Permissions.BYPASS_SPECTATED));

		if(plugin.getMultiverseCore() != null) {
			toVisit.removeIf(p -> {
				String world = plugin.getMultiverseCore().getMVWorldManager().getMVWorld(p.getWorld()).getPermissibleName();
				return !owner.hasPermission("multiverse.access." + world);
			});
		}

		alreadyVisited.removeIf(p -> !p.isOnline());
		toVisit.removeAll(alreadyVisited);
		toVisit = new ArrayList<>(new HashSet<>(toVisit));
	}
}