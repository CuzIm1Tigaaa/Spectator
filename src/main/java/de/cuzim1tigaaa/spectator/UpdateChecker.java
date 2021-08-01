package de.cuzim1tigaaa.spectator;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.URL;
import java.util.logging.Level;

public class UpdateChecker {

    private final String url;
    private final Spectator plugin;
    private final boolean update;
    private String version;

    public UpdateChecker(Spectator plugin) {
        this.plugin = plugin;
        this.url = "https://api.spigotmc.org/legacy/update.php?resource=93051";

        this.update = this.checkUpdate();
    }

    public boolean isAvailable() { return this.update; }
    public String getVersion() { return version; }

    private boolean checkUpdate() {
        this.plugin.getLogger().log(Level.INFO, "Checking for Updates...");

        String versionString = this.plugin.getDescription().getVersion().replace(".", "");
        try {
            HttpsURLConnection connection = (HttpsURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("GET");

            String raw = new BufferedReader(new InputStreamReader(connection.getInputStream())).readLine();

            if(raw.contains("-")) this.version = raw.split("-")[0].trim();
            else this.version = raw;
            this.version = this.version.replace(".", "");

            if(!versionString.equalsIgnoreCase(this.version)) {
                this.plugin.getLogger().log(Level.WARNING, "A new Version [v" + this.version + "] is available!");
                this.plugin.getLogger().log(Level.WARNING, "https://www.spigotmc.org/resources/spectator.93051/");
                return true;
            }
            connection.disconnect();

        }catch(IOException exception) { return false; }
        return false;
    }
}