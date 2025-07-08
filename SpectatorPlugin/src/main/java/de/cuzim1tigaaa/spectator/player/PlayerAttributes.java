package de.cuzim1tigaaa.spectator.player;

import de.cuzim1tigaaa.spectator.Spectator;
import de.cuzim1tigaaa.spectator.extensions.MultiverseHandler;
import de.cuzim1tigaaa.spectator.files.Config;
import de.cuzim1tigaaa.spectator.files.Paths;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

@Getter
public class PlayerAttributes {

    @Setter private GameMode gameMode;
    private final boolean flying;
    private final int remainingAir;
    private final int fireTicks;

    public PlayerAttributes(Player player) {
        gameMode = player.getGameMode();
        flying = player.isFlying();

        remainingAir = player.getRemainingAir();
        fireTicks = player.getFireTicks();
    }

    public static void restorePlayerAttributes(Player player, PlayerAttributes pAttributes, boolean gameModeChange) {
        Spectator plugin = Spectator.getPlugin();
        GameMode gameMode = GameMode.SURVIVAL;

        if(MultiverseHandler.getInstance().isMultiverseCoreInstalled())
            gameMode = MultiverseHandler.getInstance().getGameMode(player.getWorld());

        boolean isFlying = false;
        int remainingAir = player.getMaximumAir(), fireTicks = 0;

        if(pAttributes != null) {
            gameMode = pAttributes.getGameMode();

            if(gameMode == GameMode.SPECTATOR || gameMode == GameMode.ADVENTURE)
                isFlying = pAttributes.isFlying() && Config.getBoolean(Paths.CONFIG_SAVE_PLAYERS_FLIGHT_MODE) && Bukkit.getServer().getAllowFlight();
            else
                isFlying = pAttributes.isFlying() && Config.getBoolean(Paths.CONFIG_SAVE_PLAYERS_FLIGHT_MODE) && player.getAllowFlight();

            if(Config.getBoolean(Paths.CONFIG_SAVE_PLAYERS_DATA)) {
                remainingAir = pAttributes.getRemainingAir();
                fireTicks = pAttributes.getFireTicks();
            }
        }
        if(gameModeChange)
            player.setGameMode(gameMode);
        player.setFlying(isFlying && player.getAllowFlight());
        player.setRemainingAir(remainingAir);
        player.setFireTicks(fireTicks);
    }
}