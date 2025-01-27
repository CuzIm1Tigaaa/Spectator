package de.cuzim1tigaaa.spectator.spectate;

import de.cuzim1tigaaa.spectator.SpectateAPI;
import de.cuzim1tigaaa.spectator.Spectator;
import de.cuzim1tigaaa.spectator.cycle.CycleTask;
import org.bukkit.entity.Player;

public class SpectateUtilsCycle {

    private final Spectator plugin;
    private final SpectateAPI spectateAPI;
    private final SpectateUtilsGeneral spectateUtils;

    public SpectateUtilsCycle(Spectator plugin, SpectateAPI spectateAPI) {
        this.plugin = plugin;
        this.spectateAPI = spectateAPI;
        this.spectateUtils = spectateAPI.getSpectateGeneral();
    }

    public void startCycle(Player spectator, CycleTask cycle) {
        if(spectateAPI.isCyclingSpectator(spectator))
            return;

        spectateUtils.spectate(spectator, null);
        SpectateInformation info = spectateAPI.getSpectateInfo(spectator);
        info.setTarget(null);
        info.setState(SpectateState.CYCLING);
        info.setCycleTask(cycle);
        cycle.startTask(this.plugin);
    }

    public void stopCycle(Player spectator) {
        if(!spectateAPI.isCyclingSpectator(spectator))
            return;

        SpectateInformation info = spectateAPI.getSpectateInfo(spectator);
        info.setState(SpectateState.SPECTATING);
        info.getCycleTask().stopTask();
        info.setCycleTask(null);
        spectateAPI.dismount(spectator);
    }

    public void restartCycle(Player spectator) {
        if(!spectateAPI.isPausedSpectator(spectator))
            return;

        SpectateInformation info = spectateAPI.getSpectateInfo(spectator);
        info.setTarget(null);
        info.setState(SpectateState.CYCLING);
        info.getCycleTask().startTask(this.plugin);
    }

    public void pauseCycle(Player spectator) {
        if(!spectateAPI.isCyclingSpectator(spectator))
            return;

        SpectateInformation info = spectateAPI.getSpectateInfo(spectator);
        CycleTask cycle = info.getCycleTask().stopTask();
        info.setState(SpectateState.PAUSED);
        spectateAPI.dismount(spectator);
    }

    public void forceNextTarget(Player spectator, Player target) {
        if(!spectateAPI.isCyclingSpectator(spectator))
            return;

        SpectateInformation info = spectateAPI.getSpectateInfo(spectator);
        if(spectateAPI.getCycleTask(spectator) != null)
            info.getCycleTask().startForcePlayer(this.plugin, target);
    }

    public void teleportNextPlayer(Player spectator) {
        if(!spectateAPI.isCyclingSpectator(spectator))
            return;

        SpectateInformation info = spectateAPI.getSpectateInfo(spectator);
        if(spectateAPI.getCycleTask(spectator) != null) {
            info.getCycleTask().stopTask();
            info.getCycleTask().startTask(this.plugin);
        }
    }
}