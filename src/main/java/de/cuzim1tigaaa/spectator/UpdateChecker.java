package de.cuzim1tigaaa.spectator;

import lombok.Getter;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public final class UpdateChecker {

    private final Spectator plugin;

    @Getter private boolean update;
    @Getter private String version;

    public UpdateChecker(Spectator plugin) {
        this.plugin = plugin;

        this.checkUpdate().thenAcceptAsync(result -> {
            this.update = result;
            if(result) this.updateAvailable();
        });
    }

    private void updateAvailable() {
        this.plugin.getLogger().log(Level.WARNING, "A new Version [v" + this.version + "] is available!");
        this.plugin.getLogger().log(Level.WARNING, "https://www.spigotmc.org/resources/spectator.93051/");
    }

    private CompletableFuture<Boolean> checkUpdate() {
        final CompletableFuture<Boolean> result = new CompletableFuture<>();

        this.plugin.getLogger().log(Level.INFO, "Checking for Updates…");
        String versionString = this.plugin.getDescription().getVersion();

        try {
            URL url = new URL("https://api.spigotmc.org/legacy/update.php?resource=93051");
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            if(connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                result.complete(false);
                return result;
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            this.version = reader.readLine().replace("v", "");
            result.complete(!versionString.equalsIgnoreCase(this.version));
            return result;
        }catch(IOException exception) {
            result.complete(false);
            return result;
        }
    }
}