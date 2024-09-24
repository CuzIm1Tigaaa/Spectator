package de.cuzim1tigaaa.spectator;

import com.onarandombox.MultiverseCore.MultiverseCore;
import de.cuzim1tigaaa.spectator.commands.*;
import de.cuzim1tigaaa.spectator.extensions.Metrics;
import de.cuzim1tigaaa.spectator.extensions.Placeholders;
import de.cuzim1tigaaa.spectator.extensions.UpdateChecker;
import de.cuzim1tigaaa.spectator.files.Config;
import de.cuzim1tigaaa.spectator.files.Messages;
import de.cuzim1tigaaa.spectator.files.Paths;
import de.cuzim1tigaaa.spectator.listener.SpectatorListener;
import de.cuzim1tigaaa.spectator.player.Inventory;
import de.cuzim1tigaaa.spectator.spectate.SpectateUtils;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

@Getter
public class Spectator extends JavaPlugin {

    @Getter
    private static Spectator plugin;

    private SpectateAPI spectateAPI;
    private SpectateUtils spectateUtils;
    private UpdateChecker updateChecker;

    private MultiverseCore multiverseCore;
    private boolean papiInstalled;

    private Config spectatorConfig;
    private Inventory inventory;
    private Messages messages;

    @Override
    public void onEnable() {
        this.info();

        this.spectateAPI = new SpectateAPI(this);
        this.spectateUtils = new SpectateUtils(this);
        this.updateChecker = new UpdateChecker(this);

        register();

        plugin = this;
    }

    @Override
    public void onDisable() {
        this.spectateUtils.restore();
        this.inventory.restoreAll();

        plugin = null;
    }


    public void reload() {
        this.getLogger().info("Loading config settings…");
        spectatorConfig = new Config(this);
        spectatorConfig.loadConfig();

        this.getLogger().info("Loading plugin messages…");
        messages = new Messages(this);
        messages.loadLanguageFile();
    }

    private void register() {
        this.reload();
        new Metrics(this);

        this.inventory = new Inventory(this);
        this.getLogger().info("Register Events & Commands…");

        if((multiverseCore = (MultiverseCore) Bukkit.getServer().getPluginManager().getPlugin("Multiverse-Core")) != null)
            this.getLogger().info("Multiverse-Core is installed on this server!");

        papiInstalled = false;
        if(Bukkit.getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            this.getLogger().info("PlaceholderAPI is installed on this server!");
            this.getLogger().info("Registering plugins placeholders…");
            new Placeholders(this).register();
            papiInstalled = true;
        }

        new SpectatorListener(this);

        new Spectate(this);
        new SpectateCycle(this);
        new SpectateHere(this);
        new SpectateInfo(this);
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

    public List<String> getOnlinePlayerNames() {
        List<String> names = new ArrayList<>();
        Bukkit.getOnlinePlayers().forEach(player -> names.add(player.getName()));
        return names;
    }

    public void debug(String message) {
        if(!spectatorConfig.getBoolean(Paths.CONFIG_DEBUG))
            return;
        getPlugin().getLogger().info(message);
    }
}