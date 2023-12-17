package de.cuzim1tigaaa.spectator.player;

import de.cuzim1tigaaa.spectator.Spectator;
import de.cuzim1tigaaa.spectator.files.Config;
import de.cuzim1tigaaa.spectator.files.Paths;
import de.cuzim1tigaaa.spectator.spectate.SpectateUtils;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;

import java.util.HashSet;
import java.util.Set;

import static de.cuzim1tigaaa.spectator.files.Permissions.*;

public class Inventory {

    private static final SpectateUtils spectateUtils = Spectator.getPlugin(Spectator.class).getSpectateUtils();

    private static void clearActivePotionEffects(Player spectator) {
        for(PotionEffect activePotionEffect : spectator.getActivePotionEffects())
            spectator.removePotionEffect(activePotionEffect.getType());
    }

    private static void addPotionEffectsOfTarget(Player spectator, Player target) {
        if(target != null)
            spectator.addPotionEffects(new HashSet<>(target.getActivePotionEffects()));
    }

    public static void getInventory(Player spectator, Player target) {
        spectator.getInventory().clear();
        clearActivePotionEffects(spectator);

        if(target == null)
            return;

        if(hasPermission(target, BYPASS_SPECTATED))
            return;

        if(hasPermission(spectator, UTILS_MIRROR_INVENTORY) && Config.getBoolean(Paths.CONFIG_MIRROR_TARGETS_INVENTORY))
            spectator.getInventory().setContents(target.getInventory().getContents());

        if(hasPermission(spectator, UTILS_MIRROR_EFFECTS) && Config.getBoolean(Paths.CONFIG_MIRROR_TARGET_EFFECTS))
            addPotionEffectsOfTarget(spectator, target);
    }

    public static void updateInventory(Player spectator, Player target) {
        clearActivePotionEffects(spectator);

        if(target != null) {
            spectator.getInventory().setContents(target.getInventory().getContents());
            addPotionEffectsOfTarget(spectator, target);
        }
    }

    public static void resetInventory(Player spectator) {
        if(!spectateUtils.isSpectator(spectator))
            return;

        spectator.getInventory().clear();
        clearActivePotionEffects(spectator);
    }

    public static void restoreInventory(Player spectator, PlayerAttributes pAttributes) {
        if(pAttributes == null)
            return;

        if(pAttributes.getInventory() != null)
            spectator.getInventory().setContents(pAttributes.getInventory());
        if(pAttributes.getEffects() != null)
            spectator.addPotionEffects(pAttributes.getEffects());
    }

    public static void restoreAll() {
        Set<Player> players = new HashSet<>(spectateUtils.getSpectators());

        for(Player spectator : players)
            if(spectator.isOnline()) restoreInventory(spectator, spectateUtils.getPlayerAttributes(spectator));
    }
}