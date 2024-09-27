package de.cuzim1tigaaa.spectator.player;

import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import java.util.HashSet;
import java.util.Set;

@Getter
public class PlayerInventory {

    private final Player player;
    private final ItemStack[] contents;
    private final Set<PotionEffect> effects;

    public PlayerInventory(Player player) {
        this.player = player;
        this.contents = player.getInventory().getContents();
        this.effects = new HashSet<>(player.getActivePotionEffects());
    }

}
