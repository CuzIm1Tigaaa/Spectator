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
import de.cuzim1tigaaa.spectator.spectate.SpectateUtilsGeneral;
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
    private SpectateUtilsGeneral spectateUtils;
    private UpdateChecker updateChecker;

    private MultiverseCore multiverseCore;
    private boolean papiInstalled;
    private Inventory inventory;

    @Override
    public void onEnable() {
        this.info();

        this.spectateAPI = new SpectateAPI(this);
        this.spectateUtils = new SpectateUtilsGeneral(this);
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

    public void reload() {
        this.getLogger().info("Loading config settings…");
        new Config(this).loadConfig();

        this.getLogger().info("Loading plugin messages…");
        new Messages(this).loadLanguageFile();
    }

    private void info() {
        this.getLogger().info(String.format("""
                -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-
                Plugin %s, v%s by %s
                -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-
                This Plugin is a modified Version
                of kosakriszi's spectator Plugin!
                spigotmc.org/resources/spectator.16745/
                -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-
                """, this.getDescription().getName(),
                this.getDescription().getVersion(),
                this.getDescription().getAuthors().get(0)));
    }

    public List<String> getOnlinePlayerNames() {
        List<String> names = new ArrayList<>();
        Bukkit.getOnlinePlayers().forEach(player -> names.add(player.getName()));
        return names;
    }

    public static void debug(String message) {
        if (Config.getBoolean(Paths.CONFIG_DEBUG))
            getPlugin().getLogger().info(message);
    }
}