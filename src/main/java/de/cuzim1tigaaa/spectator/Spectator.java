package de.cuzim1tigaaa.spectator;

import com.onarandombox.MultiverseCore.MultiverseCore;
import de.cuzim1tigaaa.spectator.commands.*;
import de.cuzim1tigaaa.spectator.cycle.CycleHandler;
import de.cuzim1tigaaa.spectator.files.Config;
import de.cuzim1tigaaa.spectator.files.Messages;
import de.cuzim1tigaaa.spectator.listener.ContainerListener;
import de.cuzim1tigaaa.spectator.listener.PlayerListener;
import de.cuzim1tigaaa.spectator.player.Inventory;
import de.cuzim1tigaaa.spectator.player.SpectateManager;
import lombok.Getter;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public final class Spectator extends JavaPlugin {

    public List<String> getOnlinePlayerNames() {
        List<String> names = new ArrayList<>();
        Bukkit.getOnlinePlayers().forEach(player -> names.add(player.getName()));
        return names;
    }

    @Getter private final Set<Player> spectators = new HashSet<>();
    @Getter private final HashMap<Player, Player> relation = new HashMap<>();
    @Getter private UpdateChecker updateChecker;
    @Getter private SpectateManager spectateManager;
    @Getter private CycleHandler cycleHandler;

    @Getter private MultiverseCore multiverse;

    @Override
    public void onEnable() {
        this.info();

        this.spectateManager = new SpectateManager(this);
        this.cycleHandler = new CycleHandler(this);

        register();
    }

    @Override
    public void onDisable() {
        Inventory.restoreAll();
        this.spectateManager.restoreAll();
    }

    public void reload() {
        this.getLogger().info("Loading config settings...");
        Config.loadConfig(this);
        this.getLogger().info("Loading plugin messages...");
        Messages.loadLanguageFile(this);

        this.updateChecker = new UpdateChecker(this);
    }


    private void register() {
        this.reload();
        new Metrics(this, 12235);
        this.getLogger().info("Register Events & Commands...");

        if((multiverse = (MultiverseCore) Bukkit.getServer().getPluginManager().getPlugin("Multiverse-Core")) != null)
            this.getLogger().info("Multiverse-Core was found!");

        new ContainerListener(this);
        new PlayerListener(this);

        new Spectate(this);
        new SpectateCycle(this);
        new SpectateHere(this);
        new SpectateReload(this);
        new SpectateList(this);
        new UnSpectate(this);
    }

    private void info() {
        this.getLogger().info("-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-");
        this.getLogger().info("Plugin: " + getDescription().getName() + ", " +
                "v" + getDescription().getVersion() + " by " + getDescription().getAuthors().get(0));
        this.getLogger().info("-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-");
        this.getLogger().info("This Plugin is a modified Version");
        this.getLogger().info("of kosakriszi's spectator Plugin!");
        this.getLogger().info("spigotmc.org/resources/spectator.16745/");
        this.getLogger().info("-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-");
    }
}