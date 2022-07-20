package de.cuzim1tigaaa.spectator;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Scanner;
import java.util.logging.Level;

public final class UpdateChecker {

    private final String url;
    private final SpectatorPlugin plugin;
    private final boolean update;
    private String version;

    public UpdateChecker(SpectatorPlugin plugin) {
        this.plugin = plugin;
        this.url = "https://api.spigotmc.org/legacy/update.php?resource=93051";
        this.update = this.checkUpdate();
    }

    public boolean isAvailable() {
        return this.update;
    }

    public String getVersion() {
        return version;
    }

    private boolean checkUpdate() {
        this.plugin.getLogger().log(Level.INFO, "Checking for Updates...");

        String versionString = this.plugin.getDescription().getVersion();

        try (InputStream input = new URL(url).openStream()) {

            Scanner scanner = new Scanner(input);

            if (!scanner.hasNext())
                return false;

            this.version = scanner.nextLine().replace("v", "");

            if (!versionString.equalsIgnoreCase(this.version)) {
                this.plugin.getLogger().log(Level.WARNING, "A new Version [v" + this.version + "] is available!");
                this.plugin.getLogger().log(Level.WARNING, "https://www.spigotmc.org/resources/spectator.93051/");
                return true;
            }

        } catch (IOException exception) {
            return false;
        }

        return false;
    }
}