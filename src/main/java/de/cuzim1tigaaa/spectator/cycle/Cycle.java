package de.cuzim1tigaaa.spectator.cycle;

import de.cuzim1tigaaa.spectator.Spectator;
import de.cuzim1tigaaa.spectator.files.Permissions;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class Cycle {

    private final Spectator plugin = Spectator.getPlugin(Spectator.class);

    private final Player owner;
    private final List<Player> alreadyVisited;
    private final List<Player> toVisit;
    @Getter private Player lastPlayer;

    public Cycle(Player owner, Player last) {
        this.owner = owner;
        this.lastPlayer = last;

        this.alreadyVisited = new ArrayList<>();
        this.toVisit = new ArrayList<>();
    }

    public boolean hasNextPlayer() {
        return toVisit.size() > 0;
    }

    public Player getNextPlayer(Player spectator) {
        if(toVisit.size() == 0)
            alreadyVisited.clear();
        updateLists(spectator);
        if(toVisit.size() == 0) return null;
        Player player = toVisit.get(ThreadLocalRandom.current().nextInt(toVisit.size()));
        if(player.equals(lastPlayer)) return null;
        return this.visit(player);
    }

    private Player visit(Player player) {
        this.lastPlayer = player;
        this.alreadyVisited.add(player);
        return player;
    }

    private void updateLists(Player spectator) {
        toVisit.addAll(Bukkit.getOnlinePlayers());
        toVisit.remove(owner);
        toVisit.remove(lastPlayer);

        toVisit.removeAll(plugin.getSpectators());
        toVisit.removeIf(p -> p.hasPermission(Permissions.BYPASS_SPECTATED) &&
                !spectator.hasPermission(Permissions.BYPASS_SPECTATEALL));

        alreadyVisited.removeIf(p -> !p.isOnline());
        toVisit.removeAll(alreadyVisited);
    }
}