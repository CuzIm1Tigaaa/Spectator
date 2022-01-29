package de.cuzim1tigaaa.spectator;

import de.cuzim1tigaaa.spectator.commands.*;
import de.cuzim1tigaaa.spectator.files.Config;
import de.cuzim1tigaaa.spectator.files.Messages;
import de.cuzim1tigaaa.spectator.listener.PlayerListener;
import de.cuzim1tigaaa.spectator.player.Inventory;
import de.cuzim1tigaaa.spectator.player.SpectateManager;
import org.bstats.bukkit.Metrics;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class Spectator extends JavaPlugin {

    private UpdateChecker updateChecker;
    private SpectateManager spectateManager;

    public UpdateChecker getUpdateChecker() { return updateChecker; }
    private final Set<Player> spectators = new HashSet<>();

    private final HashMap<Player, Player> relation = new HashMap<>();
    public HashMap<Player, Player> getRelation() { return relation; }

    public Set<Player> getSpectators() { return spectators; }

    public SpectateManager getSpectateManager() { return spectateManager; }

    @Override
    public void onEnable() {
        spectateManager = new SpectateManager(this);
        updateChecker = new UpdateChecker(this);

        info();
        new Metrics(this, 12235);
        register();
    }

    private void info() {
        getLogger().info("-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-");
        getLogger().info("Plugin: " + getDescription().getName() + ", " +
                "v" + getDescription().getVersion() + " by " + getDescription().getAuthors().get(0));
        getLogger().info("-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-");
        getLogger().info("This Plugin is a modified Version");
        getLogger().info("of kosakriszi's spectator Plugin!");
        getLogger().info("spigotmc.org/resources/spectator.16745/");
        getLogger().info("-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-");
    }
    private void register() {
        reload();
        getLogger().info("Register Events & Commands...");
        new PlayerListener(this);

        new Spectate(this);
        new SpectateCycle(this);
        new SpectateHere(this);
        new SpectateReload(this);
        new SpectateList(this);
        new UnSpectate(this);
    }
    public void reload() {
        getLogger().info("(Re-)loading the Configuration file...");
        Messages.loadMessages(this);
        Config.loadConfig(this, true);
    }

    @Override
    public void onDisable() {
        Inventory.restoreAll();
        spectateManager.restoreAll();
    }
}