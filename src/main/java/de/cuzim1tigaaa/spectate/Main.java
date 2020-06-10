package de.cuzim1tigaaa.spectate;

import de.cuzim1tigaaa.spectate.commands.*;
import de.cuzim1tigaaa.spectate.cycle.CycleHandler;
import de.cuzim1tigaaa.spectate.files.Config;
import de.cuzim1tigaaa.spectate.listener.PacketListener;
import de.cuzim1tigaaa.spectate.listener.PlayerListener;
import de.cuzim1tigaaa.spectate.player.Inventory;
import de.cuzim1tigaaa.spectate.player.Methods;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class Main extends JavaPlugin {

    private static Main instance;

    private final Set<Player> spectators = new HashSet<>();
    private final HashMap<Player, Player> relation = new HashMap<>();

    private Methods methods;
    private PlayerListener playerListener;
    private CycleHandler cycleHandler;

    @Override
    public void onEnable() {
        instance = this;

        methods = new Methods();
        playerListener = new PlayerListener();
        cycleHandler = new CycleHandler();
        PacketListener.register();

        info();
        register();
        reload();
    }
    @Override
    public void onDisable() {
        Inventory.restoreAll();
        methods.restoreAll();
    }

    private void info() {
        this.getLogger().info("-=-=-=-=-=-=-=-=-=-=-=-");
        this.getLogger().info("Plugin: " + instance.getDescription().getName());
        this.getLogger().info("Version: " + instance.getDescription().getVersion());
        this.getLogger().info("By " + instance.getDescription().getAuthors().get(0));
        this.getLogger().info("-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-");
        this.getLogger().info("This Plugin is a modified Version");
        this.getLogger().info("of kosakriszi's spectator Plugin!");
        this.getLogger().info("https://www.spigotmc.org/resources/spectator.16745/");
        this.getLogger().info("-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-");
    }
    private void register() {
        this.getLogger().info("Register Events & Commands...");
        getCommand("spectate").setExecutor(new spectate());
        getCommand("spectatecycle").setExecutor(new spectatecycle());
        getCommand("spectatehere").setExecutor(new spectatehere());
        getCommand("spectatereload").setExecutor(new spectatereload());
        getCommand("spectatelist").setExecutor(new spectatelist());
        getCommand("unspectate").setExecutor(new unspectate());

        PluginManager pm = instance.getServer().getPluginManager();
        pm.registerEvents(new PlayerListener(), instance);
    }
    public void reload() {
        this.getLogger().info("Reloading Config...");
        Config.loadConfig();
    }

    public static Main getInstance() {
        return instance;
    }
    public Set<Player> getSpectators() {
        return spectators;
    }
    public HashMap<Player, Player> getRelation() {
        return relation;
    }

    public Methods getMethods() {
        return methods;
    }
    public PlayerListener getPlayerListener() {
        return playerListener;
    }
    public CycleHandler getCycleHandler() {
        return cycleHandler;
    }
}