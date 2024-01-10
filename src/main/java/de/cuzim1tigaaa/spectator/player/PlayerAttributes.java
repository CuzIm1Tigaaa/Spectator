package de.cuzim1tigaaa.spectator.player;

import com.onarandombox.MultiverseCore.MultiverseCore;
import de.cuzim1tigaaa.spectator.Spectator;
import de.cuzim1tigaaa.spectator.files.Config;
import de.cuzim1tigaaa.spectator.files.Paths;
import de.cuzim1tigaaa.spectator.spectate.SpectateUtils;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import java.util.HashSet;
import java.util.Set;

@Getter
public class PlayerAttributes {

    private static final SpectateUtils spectateUtils = Spectator.getPlugin(Spectator.class).getSpectateUtils();

    private final GameMode gameMode;
    private final boolean flying;
    @Setter private ItemStack[] inventory;
    private final Set<PotionEffect> effects;
    private final int remainingAir, fireTicks;

    public PlayerAttributes(Player player) {
        gameMode = player.getGameMode();
        flying = player.isFlying();

        inventory = player.getInventory().getContents();
        effects = new HashSet<>(player.getActivePotionEffects());

        remainingAir = player.getRemainingAir();
        fireTicks = player.getFireTicks();
    }

    public static void restorePlayerAttributes(Player player, PlayerAttributes pAttributes) {
        GameMode gameMode = GameMode.SURVIVAL;
        MultiverseCore core;
        if((core = Spectator.getPlugin(Spectator.class).getMultiverseCore()) != null)
            gameMode = core.getMVWorldManager().getMVWorld(player.getWorld()).getGameMode();

        boolean isFlying = false;
        int remainingAir = player.getMaximumAir(), fireTicks = 0;

        if(pAttributes != null) {
            gameMode = pAttributes.getGameMode();
            isFlying = pAttributes.isFlying() && Config.getBoolean(Paths.CONFIG_SAVE_PLAYERS_FLIGHT_MODE) && Bukkit.getServer().getAllowFlight();

            if(Config.getBoolean(Paths.CONFIG_SAVE_PLAYERS_DATA)) {
                remainingAir = pAttributes.getRemainingAir();
                fireTicks = pAttributes.getFireTicks();
            }
            Inventory.restoreInventory(player, pAttributes);
        }

        spectateUtils.changeGameMode(player, gameMode);
        player.setFlying(isFlying && player.getAllowFlight());
        player.setRemainingAir(remainingAir);
        player.setFireTicks(fireTicks);
    }
}