package de.cuzim1tigaaa.spectate.cycle;

import de.cuzim1tigaaa.spectate.Main;
import de.cuzim1tigaaa.spectate.files.Permissions;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;

public class Cycle {

    private final Player owner;
    private Player last;
    private final List<Player> alreadyVisited = new ArrayList<>();
    private final Random random = new Random();
    private List<Player> toVisit;

    public Cycle(Player owner, Player last) {
        this.owner = owner;
        this.last = last;
    }

    public boolean hasNextPlayer() { return toVisit != null && alreadyVisited.size() != toVisit.size(); }
    public Player getLastPlayer() { return last; }
    public Player getNextPlayer(Player spectator) {
        updateLists(spectator);
        if (toVisit.size() == 0) return null;
        if (toVisit.size() == 1) return toVisit.get(0);
        Player player = toVisit.get(random.nextInt(toVisit.size()));
        if (player.equals(last)) return getNextPlayer(spectator);
        last = player;
        alreadyVisited.add(player);
        return player;
    }

    private void updateLists(Player spectator) {
        List<Player> toRemove = new ArrayList<>();
        toVisit = new ArrayList<>(Bukkit.getOnlinePlayers());
        for (Player player : toVisit) if (player.hasPermission(Permissions.BYPASS_SPECTATED) && !spectator.hasPermission(Permissions.BYPASS_SPECTATEALL)) toRemove.add(player);
        toVisit.removeAll(toRemove);
        // Clear the toVisit list of players that have been visited.
        for (Player player : alreadyVisited) {
            if (!player.isOnline()) alreadyVisited.remove(player);
            toVisit.remove(player);
        }
        toVisit.remove(owner);
        for (Player player : Main.getInstance().getSpectators()) toVisit.remove(player);
    }
}