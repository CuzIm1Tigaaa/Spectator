package de.cuzim1tigaaa.spectator.spectate;

import de.cuzim1tigaaa.spectator.SpectateAPI;
import de.cuzim1tigaaa.spectator.Spectator;
import de.cuzim1tigaaa.spectator.cycle.CycleTask;
import org.bukkit.entity.Player;

public class SpectateUtilsCycle {

    private final Spectator plugin;
    private final SpectateAPI spectateAPI;

    public SpectateUtilsCycle(Spectator plugin) {
        this.plugin = plugin;
        this.spectateAPI = plugin.getSpectateAPI();
    }

    public void startCycle(Player spectator, CycleTask cycle) {
        if(spectateAPI.isCyclingSpectator(spectator))
            return;

        spectate(spectator, null);
        SpectateInformation info = spectateAPI.getSpectateInfo(spectator);
        info.setTarget(null);
        info.setState(SpectateState.CYCLING);
        this.spectateCycle.put(spectator.getUniqueId(), cycle);
        spectateInfo.replace(spectator.getUniqueId(), info);
        cycle.startTask(this.plugin);
    }

    public void stopCycle(Player spectator) {
        if(!isCycling(spectator))
            return;

        SpectateInformation info = getSpectateInformation(spectator);
        this.spectateCycle.get(spectator.getUniqueId()).stopTask();

        info.setState(SpectateState.SPECTATING);
        this.spectateCycle.remove(spectator.getUniqueId());
        spectateInfo.replace(spectator.getUniqueId(), info);
        dismount(spectator);
    }

    public void restartCycle(Player spectator) {
        if(!isPaused(spectator))
            return;

        SpectateInformation info = getSpectateInformation(spectator);
        info.setTarget(null);
        info.setState(SpectateState.CYCLING);
        spectateInfo.replace(spectator.getUniqueId(), info);
        this.spectateCycle.get(spectator.getUniqueId()).startTask(this.plugin);
    }

    public void pauseCycle(Player spectator) {
        if(!isCycling(spectator))
            return;

        SpectateInformation info = getSpectateInformation(spectator);
        CycleTask cycle = this.spectateCycle.get(spectator.getUniqueId()).stopTask();

        this.spectateCycle.put(spectator.getUniqueId(), cycle);
        info.setState(SpectateState.PAUSED);
        spectateInfo.replace(spectator.getUniqueId(), info);
        dismount(spectator);
    }

    public void teleportNextPlayer(Player spectator) {
        if(!isCycling(spectator))
            return;

        if(getCycleTask(spectator) != null) {
            this.spectateCycle.get(spectator.getUniqueId()).stopTask();
            this.spectateCycle.get(spectator.getUniqueId()).startTask(this.plugin);
        }
    }

}
