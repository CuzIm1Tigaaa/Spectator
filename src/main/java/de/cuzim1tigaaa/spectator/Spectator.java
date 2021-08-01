package de.cuzim1tigaaa.spectator;

import de.cuzim1tigaaa.spectator.commands.*;
import de.cuzim1tigaaa.spectator.files.Config;
import de.cuzim1tigaaa.spectator.files.Messages;
import de.cuzim1tigaaa.spectator.listener.PacketListener;
import de.cuzim1tigaaa.spectator.listener.PlayerListener;
import de.cuzim1tigaaa.spectator.player.Inventory;
import de.cuzim1tigaaa.spectator.player.Methods;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.logging.Level;

public class Spectator extends JavaPlugin {

    private static Spectator plugin;
    private UpdateChecker updateChecker;

    private final Set<Player> spectators = new HashSet<>();
    private Config config;

    private final HashMap<Player, Player> relation = new HashMap<>();

    public static Spectator getPlugin() { return plugin; }

    private Methods methods;

    public Set<Player> getSpectators() { return spectators; }

    public HashMap<Player, Player> getRelation() { return relation; }

    public Methods getMethods() { return methods; }

    public UpdateChecker getUpdateChecker() { return updateChecker; }

    public Config getConfiguration() { return config; }

    @Override
    public void onEnable() {
        plugin = this;
        if(!isProtocolLibInstalled()) return;
        this.methods = new Methods();
        this.updateChecker = new UpdateChecker(this);

        this.info();
        this.bStats();
        this.register();
    }

    private boolean isProtocolLibInstalled() {
        if(this.getServer().getPluginManager().getPlugin("ProtocolLib") == null) {
            this.getLogger().log(Level.SEVERE, "ProtocolLib is not installed, disabling Plugin...!");
            this.getLogger().log(Level.SEVERE, "Please install ProtocolLib to use this Plugin!");
            this.getServer().getPluginManager().disablePlugin(this);
            return false;
        }
        return true;
    }

    private void info() {
        this.getLogger().info("-=-=-=-=-=-=-=-=-=-=-=-");
        this.getLogger().info("Plugin: " + plugin.getDescription().getName());
        this.getLogger().info("Version: " + plugin.getDescription().getVersion());
        this.getLogger().info("By " + plugin.getDescription().getAuthors().get(0));
        this.getLogger().info("-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-");
        this.getLogger().info("This Plugin is a modified Version");
        this.getLogger().info("of kosakriszi's spectator Plugin!");
        this.getLogger().info("https://www.spigotmc.org/resources/spectator.16745/");
        this.getLogger().info("-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-");
    }
    private void bStats() {
        int pluginID = 12235;
        Metrics metrics = new Metrics(this, pluginID);
    }

    private void register() {
        this.reload();
        this.getLogger().info("Register Events & Commands...");
        new PlayerListener(plugin);
        new PacketListener(plugin);

        new Spectate(plugin);
        new SpectateCycle(plugin);
        new SpectateHere(plugin);
        new SpectateReload(plugin);
        new SpectateList(plugin);
        new UnSpectate(plugin);
    }
    public void reload() {
        this.getLogger().info("(Re-)loading the Configuration file...");
        config = new Config();
        Messages.loadMessages();
    }

    @Override
    public void onDisable() {
        this.disable();
    }
    public void disable() {
        Inventory.restoreAll();
        methods.restoreAll();
    }
}