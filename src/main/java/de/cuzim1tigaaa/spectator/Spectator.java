package de.cuzim1tigaaa.spectator;

import com.onarandombox.MultiverseCore.MultiverseCore;
import de.cuzim1tigaaa.spectator.commands.*;
import de.cuzim1tigaaa.spectator.extensions.*;
import de.cuzim1tigaaa.spectator.files.*;
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

    private SpectateUtils spectateUtils;
    private UpdateChecker updateChecker;

    private MultiverseCore multiverseCore;
    @Getter private static boolean papiInstalled;

    @Override
    public void onEnable() {
        this.info();
        this.spectateUtils = new SpectateUtils(this);
        register();
    }

    @Override
    public void onDisable() {
        Inventory.restoreAll();
        this.spectateUtils.Restore();
    }

    public void reload() {
        this.getLogger().info("Loading config settings…");
        Config.loadConfig(this);
        this.getLogger().info("Loading plugin messages…");
        Messages.loadLanguageFile(this);

        this.updateChecker = new UpdateChecker(this);
    }


    private void register() {
        this.reload();
        new Metrics(this);

        this.getLogger().info("Register Events & Commands…");

        if((multiverseCore = (MultiverseCore) Bukkit.getServer().getPluginManager().getPlugin("Multiverse-Core")) != null)
            this.getLogger().info("Multiverse-Core was found!");

        papiInstalled = false;
        if(Bukkit.getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new Placeholders(this).register();
            papiInstalled = true;
        }

        new SpectatorListener(this);

        new Spectate(this);
        new SpectateCycle(this);
        new SpectateHere(this);
        //new SpectateInfo(this);
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

    public void Debug(String message) {
        if(!Config.getBoolean(Paths.CONFIG_DEBUG))
            return;
        getLogger().info(message);
    }
}