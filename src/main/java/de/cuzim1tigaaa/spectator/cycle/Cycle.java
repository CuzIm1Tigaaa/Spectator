package de.cuzim1tigaaa.spectator.cycle;

import de.cuzim1tigaaa.spectator.Spectator;
import de.cuzim1tigaaa.spectator.files.Permissions;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class Cycle {

    private final Spectator plugin = Spectator.getPlugin(Spectator.class);

    private final Player owner;
    @Getter private final boolean alphabetical;
    @Getter private Player lastPlayer;

    private final List<Player> alreadyVisited;
    private List<Player> toVisit;

    public Cycle(Player owner, Player last, boolean alphabetical) {
        this.owner = owner;
        this.lastPlayer = last;
        this.alphabetical = alphabetical;

        this.alreadyVisited = new ArrayList<>();
        this.toVisit = new ArrayList<>();
    }

    public Player getNextPlayer() {
        if(toVisit.size() == 0)
            alreadyVisited.clear();
        updateLists();
        if(toVisit.size() == 0) return null;

        Player player;
        if(alphabetical) {
            this.toVisit.sort((o1, o2) -> o1.getName().compareToIgnoreCase(o2.getName()));
            player = toVisit.get(0);
            if(player == null) return null;
        }else {
            player = toVisit.get(ThreadLocalRandom.current().nextInt(toVisit.size()));
            if(player == null || player.equals(lastPlayer)) return null;
        }
        return this.visit(player);
    }

    private Player visit(Player player) {
        this.lastPlayer = player;
        this.alreadyVisited.add(player);
        return player;
    }

    private void updateLists() {
        toVisit.addAll(Bukkit.getOnlinePlayers());
        toVisit.remove(owner);
        toVisit.removeAll(plugin.getSpectators());

        if(!owner.hasPermission(Permissions.BYPASS_SPECTATEALL))
            toVisit.removeIf(p -> p.hasPermission(Permissions.BYPASS_SPECTATED));

        if(plugin.getMultiverse() != null) {
            toVisit.removeIf(p -> {
                String world = plugin.getMultiverse().getMVWorldManager().getMVWorld(p.getWorld()).getPermissibleName();
                return !owner.hasPermission("multiverse.access." + world);
            });
        }

        alreadyVisited.removeIf(p -> !p.isOnline());
        toVisit.removeAll(alreadyVisited);
        toVisit = new ArrayList<>(new HashSet<>(toVisit));
    }
}