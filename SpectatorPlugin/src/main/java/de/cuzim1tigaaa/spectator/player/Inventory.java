package de.cuzim1tigaaa.spectator.player;

import de.cuzim1tigaaa.spectator.SpectateAPI;
import de.cuzim1tigaaa.spectator.Spectator;
import de.cuzim1tigaaa.spectator.files.Config;
import de.cuzim1tigaaa.spectator.files.Paths;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;

import java.util.HashSet;
import java.util.Set;

import static de.cuzim1tigaaa.spectator.files.Permissions.*;

public class Inventory {

    private final SpectateAPI spectateAPI;
    private final Set<PlayerInventory> playerInventories;

    public Inventory(Spectator plugin) {
        this.spectateAPI = plugin.getSpectateAPI();
        this.playerInventories = new HashSet<>();
    }

    public void getTargetInventory(Player spectator, Player target) {
        if(target == null) {
            restorePlayerInventory(spectator);
            return;
        }

        if(hasPermission(target, BYPASS_SPECTATED))
            return;

        playerInventories.add(new PlayerInventory(spectator));
        if(hasPermission(spectator, UTILS_MIRROR_INVENTORY) && Config.getBoolean(Paths.CONFIG_MIRROR_TARGETS_INVENTORY))
            spectator.getInventory().setContents(target.getInventory().getContents());

        if(hasPermission(spectator, UTILS_MIRROR_EFFECTS) && Config.getBoolean(Paths.CONFIG_MIRROR_TARGET_EFFECTS))
            addPotionEffectsOfTarget(spectator, target);
    }

    public void restoreAllPlayerInventory() {
        spectateAPI.getSpectators().forEach(this::restorePlayerInventory);
    }


    private void restorePlayerInventory(Player spectator) {
        PlayerInventory playerInventory = playerInventories.stream().filter(p -> p.getPlayer().equals(spectator)).findFirst().orElse(null);
        if(playerInventory == null)
            return;

        spectator.getInventory().setContents(playerInventory.getContents());
        spectator.addPotionEffects(playerInventory.getEffects());
        playerInventories.remove(playerInventory);
    }

    private void clearActivePotionEffects(Player spectator) {
        for(PotionEffect potionEffect : spectator.getActivePotionEffects())
            spectator.removePotionEffect(potionEffect.getType());
    }

    private void addPotionEffectsOfTarget(Player spectator, Player target) {
        if(target != null)
            spectator.addPotionEffects(new HashSet<>(target.getActivePotionEffects()));
    }



    public void getInventory(Player spectator, Player target) {
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

    public void resetInventory(Player spectator) {
        if(!spectateAPI.isSpectator(spectator))
            return;

        spectator.getInventory().clear();
        clearActivePotionEffects(spectator);
    }

    public void restoreInventory(Player spectator, PlayerAttributes pAttributes) {
        if(pAttributes == null)
            return;

        if(pAttributes.getInventory() != null)
            spectator.getInventory().setContents(pAttributes.getInventory());
        if(pAttributes.getEffects() != null)
            spectator.addPotionEffects(pAttributes.getEffects());
    }

    public void restoreAll() {
        Set<Player> players = new HashSet<>(spectateAPI.getSpectators());
        for(Player spectator : players)
            if(spectator.isOnline()) restoreInventory(spectator, spectateAPI.getPlayerAttributes(spectator));
    }
}