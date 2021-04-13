package de.cuzim1tigaaa.spectate;

import de.cuzim1tigaaa.spectate.commands.*;
import de.cuzim1tigaaa.spectate.files.Config;
import de.cuzim1tigaaa.spectate.listener.PacketListener;
import de.cuzim1tigaaa.spectate.listener.PlayerListener;
import de.cuzim1tigaaa.spectate.player.Inventory;
import de.cuzim1tigaaa.spectate.player.Methods;
import org.bukkit.GameMode;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class Main extends JavaPlugin {

    private static Main instance;

    private final Set<Player> spectators = new HashSet<>();
    private final HashMap<Player, Player> relation = new HashMap<>();

    private Methods methods;

    @Override
    public void onEnable() {
        instance = this;
        methods = new Methods();

        info();
        register();
        reload();
    }
    @Override
    public void onDisable() {
        this.disable();
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
    public static Main getInstance() { return instance; }
    public void reload() {
        this.getLogger().info("Reloading Config...");
        Config.loadConfig();
    }
    public void disable() {
        Inventory.restoreAll();
        methods.restoreAll();
    }

    private void register() {
        this.getLogger().info("Register Events & Commands...");
        new PlayerListener(instance);
        new PacketListener(instance);

        Objects.requireNonNull(this.getCommand("spectate")).setExecutor(new Spectate(instance));
        Objects.requireNonNull(this.getCommand("spectatecycle")).setExecutor(new SpectateCycle(instance));
        Objects.requireNonNull(this.getCommand("spectatehere")).setExecutor(new SpectateHere(instance));
        Objects.requireNonNull(this.getCommand("spectatelist")).setExecutor(new SpectateList(instance));
        Objects.requireNonNull(this.getCommand("spectatereload")).setExecutor(new SpectateReload(instance));
        Objects.requireNonNull(this.getCommand("unspectate")).setExecutor(new UnSpectate(instance));

        new SpectateCycle(instance);
        new SpectateHere(instance);
        new SpectateReload(instance);
        new SpectateList(instance);
        new UnSpectate(instance);
    }

    public void dismountTarget(Player player) {
        if(!player.getGameMode().equals(GameMode.SPECTATOR)) return;
        if(player.getSpectatorTarget() == null || !player.getSpectatorTarget().getType().equals(EntityType.PLAYER)) return;
        instance.getRelation().remove(player);
        player.getInventory().clear();
        player.setSpectatorTarget(null);
    }

    public Set<Player> getSpectators() { return spectators; }
    public HashMap<Player, Player> getRelation() { return relation; }

    public Methods getMethods() { return methods; }
}