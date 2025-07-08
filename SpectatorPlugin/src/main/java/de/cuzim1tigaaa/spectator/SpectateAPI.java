package de.cuzim1tigaaa.spectator;

import de.cuzim1tigaaa.spectator.cycle.CycleTask;
import de.cuzim1tigaaa.spectator.extensions.MultiverseHandler;
import de.cuzim1tigaaa.spectator.files.*;
import de.cuzim1tigaaa.spectator.player.PlayerAttributes;
import de.cuzim1tigaaa.spectator.spectate.SpectateInformation;
import de.cuzim1tigaaa.spectator.spectate.SpectateState;
import de.cuzim1tigaaa.spectator.spectate.SpectateUtilsCycle;
import de.cuzim1tigaaa.spectator.spectate.SpectateUtilsGeneral;
import lombok.Getter;
import org.bukkit.*;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.*;
import java.util.stream.Collectors;

@Getter
public class SpectateAPI {

    private final Spectator plugin;
    private final Set<SpectateInformation> spectateInfo;

    private final SpectateUtilsGeneral spectateGeneral;
    private final SpectateUtilsCycle spectateCycle;

    private final Map<UUID, List<ArmorStand>> hiddenArmorStands;

    public SpectateAPI(Spectator plugin) {
        this.plugin = plugin;
        this.spectateInfo = new HashSet<>();

        this.spectateGeneral = new SpectateUtilsGeneral(this);
        this.spectateCycle = new SpectateUtilsCycle(plugin, this);

        this.hiddenArmorStands = new HashMap<>();
    }

    public Set<Player> getSpectators() {
        return spectateInfo.stream().map(SpectateInformation::getSpectator).collect(Collectors.toSet());
    }

    public Set<Player> getSpectateablePlayers() {
        Set<Player> spectateable = new HashSet<>(Bukkit.getOnlinePlayers());
        spectateable.removeAll(getSpectators());
        spectateable.removeIf(player -> player.hasPermission(Permissions.BYPASS_SPECTATED));
        return spectateable;
    }

    public Set<Player> getSpectatorsOf(Player target) {
        return spectateInfo.stream().filter(info -> info.getTarget() != null && info.getTarget().equals(target)).
                map(SpectateInformation::getSpectator).collect(Collectors.toSet());
    }


    public boolean isSpectator(Player spectator) {
        return spectateInfo.stream().anyMatch(info -> info.getSpectator().equals(spectator));
    }

    public SpectateInformation getSpectateInfo(Player spectator) {
        return spectateInfo.stream().filter(info -> info.getSpectator().equals(spectator)).findFirst().orElse(null);
    }

    public PlayerAttributes getPlayerAttributes(Player spectator) {
        if(getSpectateInfo(spectator) == null)
            return null;
        if(getSpectateInfo(spectator).getAttributes().isEmpty())
            return null;
        return getSpectateInfo(spectator).getAttributes().get(spectator.getWorld());
    }

    public boolean isNotSpectated(Player target) {
        return spectateInfo.stream().noneMatch(i -> i.getTarget() != null && i.getTarget().equals(target));
    }

    public boolean isSpectating(Player spectator, Player target) {
        return spectateInfo.stream().anyMatch(info -> info.getSpectator().equals(spectator) && info.getTarget() != null && info.getTarget().equals(target));
    }

    public Player getTargetOf(Player spectator) {
        SpectateInformation specInfo = spectateInfo.stream().filter(info -> info.getSpectator().equals(spectator)).findFirst().orElse(null);
        if(specInfo == null)
            return null;

        if(specInfo.getState() == SpectateState.CYCLING)
            return (Player) spectator.getSpectatorTarget();

        return specInfo.getTarget();
    }


    public void setRelation(Player spectator, Player target) {
        if(!isSpectator(spectator))
            return;
        getSpectateInfo(spectator).setTarget(target);
        spectator.setSpectatorTarget(target);
    }

    public void dismount(Player spectator) {
        if(!isSpectator(spectator) || spectator.getGameMode() != GameMode.SPECTATOR)
            return;
        setRelation(spectator, null);
        spectator.setSpectatorTarget(null);
        plugin.getInventory().resetInventory(spectator);
    }

    public void hideArmorstands(Player spectator) {
        if(!Config.getBoolean(Paths.CONFIG_HIDE_ARMOR_STANDS))
            return;

        SpectateInformation info = getSpectateInfo(spectator);
        if(info == null)
            return;

        spectator.getNearbyEntities(3, 3, 3).forEach(entity -> {
            if (entity instanceof ArmorStand armorStand) {
                spectator.hideEntity(Spectator.getPlugin(), armorStand);
                hiddenArmorStands.computeIfAbsent(spectator.getUniqueId(), k -> new ArrayList<>()).add(armorStand);
            }
        });
    }

    public void restoreArmorstands() {

    }

    public void showArmorstands(Player spectator) {
        if(hiddenArmorStands.isEmpty())
            return;

        SpectateInformation info = getSpectateInfo(spectator);
        if(info == null)
            return;

        hiddenArmorStands.get(spectator.getUniqueId()).forEach(armorStand ->
                spectator.showEntity(Spectator.getPlugin(), armorStand));
        hiddenArmorStands.remove(spectator.getUniqueId());
    }


    public Set<Player> getCyclingSpectators() {
        return spectateInfo.stream().filter(info -> info.getState() == SpectateState.CYCLING).
                map(SpectateInformation::getSpectator).collect(Collectors.toSet());
    }

    public boolean isCyclingSpectator(Player spectator) {
        return getCyclingSpectators().contains(spectator);
    }

    public Set<Player> getPausedSpectators() {
        return spectateInfo.stream().filter(info -> info.getState() == SpectateState.PAUSED).
                map(SpectateInformation::getSpectator).collect(Collectors.toSet());
    }

    public boolean isPausedSpectator(Player spectator) {
        return getPausedSpectators().contains(spectator);
    }

    public CycleTask getCycleTask(Player spectator) {
        SpectateInformation info = spectateInfo.stream().filter(i ->
                i.getSpectator().equals(spectator)).findFirst().orElse(null);

        return info == null ? null : info.getCycleTask();
    }


    public void toggleTabList(final Player spectator, final boolean hide) {
        if(!spectator.hasPermission(Permissions.UTILS_HIDE_IN_TAB)) {
            spectator.removeMetadata("vanished", this.plugin);
            return;
        }

        for(Player target : Bukkit.getOnlinePlayers()) {
            if(target.getUniqueId().equals(spectator.getUniqueId()) || target.hasPermission(Permissions.BYPASS_TABLIST))
                continue;

            if(Permissions.hasPermission(target, Permissions.BYPASS_TABLIST))
                continue;

            if(hide) {
                target.hidePlayer(this.plugin, spectator);
                spectator.setMetadata("vanished", new FixedMetadataValue(this.plugin, true));
                continue;
            }
            target.showPlayer(this.plugin, spectator);
            spectator.removeMetadata("vanished", this.plugin);
        }
    }

    public boolean hasPlayerAccessToWorld(Player player, World world) {
        if(player.getWorld().equals(world))
            return true;

        if(!MultiverseHandler.getInstance().isMultiverseCoreInstalled())
            return true;

        return player.hasPermission("multiverse.access." + world.getName());
    }
}