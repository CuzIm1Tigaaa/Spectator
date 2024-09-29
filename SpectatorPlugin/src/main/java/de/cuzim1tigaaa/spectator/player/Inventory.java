package de.cuzim1tigaaa.spectator.player;

import de.cuzim1tigaaa.spectator.SpectateAPI;
import de.cuzim1tigaaa.spectator.Spectator;
import de.cuzim1tigaaa.spectator.files.Config;
import de.cuzim1tigaaa.spectator.files.Paths;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;

import java.util.HashSet;
import java.util.Set;

import static de.cuzim1tigaaa.spectator.files.Permissions.*;

public class Inventory {

    private final SpectateAPI spectateAPI;
    @Getter private final Set<PlayerInventory> playerInventories;

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

        if(playerInventories.stream().noneMatch(p -> p.getPlayer().equals(spectator)))
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
    }

    private void clearActivePotionEffects(Player spectator) {
        for(PotionEffect potionEffect : spectator.getActivePotionEffects())
            spectator.removePotionEffect(potionEffect.getType());
    }

    private void addPotionEffectsOfTarget(Player spectator, Player target) {
        if(target != null)
            spectator.addPotionEffects(new HashSet<>(target.getActivePotionEffects()));
    }

    public void resetInventory(Player spectator) {
        if(!spectateAPI.isSpectator(spectator))
            return;

        if(playerInventories.stream().noneMatch(p -> p.getPlayer().equals(spectator)))
            return;

        spectator.getInventory().clear();
        clearActivePotionEffects(spectator);
        restorePlayerInventory(spectator);
        playerInventories.removeIf(p -> p.getPlayer().equals(spectator));
    }
}