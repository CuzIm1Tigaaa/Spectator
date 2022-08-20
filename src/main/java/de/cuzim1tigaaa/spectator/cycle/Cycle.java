package de.cuzim1tigaaa.spectator.cycle;

import de.cuzim1tigaaa.spectator.Spectator;
import de.cuzim1tigaaa.spectator.files.Permissions;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class Cycle {

    private static final Spectator plugin = Spectator.getPlugin(Spectator.class);

    private final Player owner;
    private Player last;
    private final List<Player> alreadyVisited = new ArrayList<>();
    private final List<Player> toVisit = new ArrayList<>();

    public Cycle(Player owner, Player last) {
        this.owner = owner;
        this.last = last;
    }

    public boolean hasNextPlayer() { return toVisit.size() == 0; }
    public Player getLastPlayer() { return last; }

    public Player getNextPlayer(Player spectator) {
        this.updateLists(spectator);
        if(toVisit.size() == 0) return null;
        if(toVisit.size() == 1) return toVisit.get(0);
        Player player = toVisit.get(ThreadLocalRandom.current().nextInt(toVisit.size()));
        if(player.equals(last)) return this.getNextPlayer(spectator);
        return this.visit(player);
    }

    private Player visit(Player player) {
        this.last = player;
        this.alreadyVisited.add(player);
        this.toVisit.remove(player);
        return player;
    }

    private void updateLists(Player spectator) {
        toVisit.addAll(Bukkit.getOnlinePlayers());
        toVisit.remove(owner);
        toVisit.removeAll(plugin.getSpectators());

        toVisit.removeIf(p -> p.hasPermission(Permissions.BYPASS_SPECTATED) &&
                !spectator.hasPermission(Permissions.BYPASS_SPECTATEALL));

        alreadyVisited.removeIf(p -> !p.isOnline());
        toVisit.removeAll(alreadyVisited);
    }
}