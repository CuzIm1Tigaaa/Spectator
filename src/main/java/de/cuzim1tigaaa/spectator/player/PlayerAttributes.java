package de.cuzim1tigaaa.spectator.player;

import de.cuzim1tigaaa.spectator.files.Config;
import de.cuzim1tigaaa.spectator.files.Paths;
import lombok.Getter;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import java.util.HashSet;
import java.util.Set;

public class PlayerAttributes {

    @Getter private final GameMode gameMode;
    @Getter private final Location location;
    @Getter private final boolean flying;
    @Getter private final ItemStack[] playerInventory;
    @Getter private final Set<PotionEffect> effects;

    @Getter private final int remainingAir, fireTicks;

    public PlayerAttributes(Player player) {
        gameMode = player.getGameMode();
        location = player.getLocation();
        flying = player.isFlying();
        playerInventory = player.getInventory().getContents();
        effects = new HashSet<>(player.getActivePotionEffects());

        remainingAir = player.getRemainingAir();
        fireTicks = player.getFireTicks();
    }

    public static void restorePlayerAttributes(Player player, PlayerAttributes pAttributes) {
        GameMode gameMode = GameMode.SURVIVAL;
        boolean isFlying = false;
        int remainingAir = player.getMaximumAir(), fireTicks = 0;

        if(pAttributes != null) {
            gameMode = pAttributes.getGameMode();
            isFlying = pAttributes.isFlying();

            if(Config.getBoolean(Paths.CONFIG_SAVE_PLAYERS_DATA)) {
                remainingAir = pAttributes.getRemainingAir();
                fireTicks = pAttributes.getFireTicks();
            }
        }
        player.setGameMode(gameMode);
        player.setFlying(isFlying);
        player.setRemainingAir(remainingAir);
        player.setFireTicks(fireTicks);
    }
}