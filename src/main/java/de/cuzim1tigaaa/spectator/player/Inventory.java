package de.cuzim1tigaaa.spectator.player;

import de.cuzim1tigaaa.spectator.SpectatorPlugin;
import de.cuzim1tigaaa.spectator.files.Config;
import de.cuzim1tigaaa.spectator.files.Paths;
import de.cuzim1tigaaa.spectator.files.Permissions;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import java.util.HashSet;
import java.util.Set;

public final class Inventory {

    private static final SpectatorPlugin plugin = SpectatorPlugin.getPlugin(SpectatorPlugin.class);

    private static void clearPotionEffects(Player player) {
        for (PotionEffect potionEffect : player.getActivePotionEffects())
            player.removePotionEffect(potionEffect.getType());
    }

    private static void addPotionEffects(Player player, Player target) {
        Set<PotionEffect> effects = target == null ? plugin.getSpectateManager().getPAttributes().get(player).getEffects() : new HashSet<>(target.getActivePotionEffects());
        player.addPotionEffects(effects);
    }

    public static void getInventory(Player player, Player target) {
        player.getInventory().clear();
        clearPotionEffects(player);

        if (target != null) {
            if (player.hasPermission(Permissions.UTILS_MIRROR_INVENTORY) && Config.getBoolean(Paths.CONFIG_MIRROR_TARGETS_INVENTORY))
                player.getInventory().setContents(target.getInventory().getContents());

            if (player.hasPermission(Permissions.UTILS_MIRROR_EFFECTS) && Config.getBoolean(Paths.CONFIG_MIRROR_TARGET_EFFECTS))
                addPotionEffects(player, target);
        }

        player.updateInventory();
    }

    public static void updateInventory(Player player, Player target) {
        clearPotionEffects(player);
        addPotionEffects(player, target);
        player.getInventory().setContents(target.getInventory().getContents());
        player.updateInventory();
    }

    public static void restoreInventory(Player player) {
        if (plugin.getSpectateManager().getPAttributes().containsKey(player)) {
            player.getInventory().clear();
            clearPotionEffects(player);

            ItemStack[] pInventory = plugin.getSpectateManager().getPAttributes().get(player).getPlayerInventory();
            Set<PotionEffect> effects = plugin.getSpectateManager().getPAttributes().get(player).getEffects();

            if (pInventory != null && pInventory.length != 0)
                player.getInventory().setContents(pInventory);

            if (effects != null && effects.size() != 0)
                addPotionEffects(player, null);
        }

        player.updateInventory();
    }

    public static void restoreAll() {
        if (!plugin.getSpectateManager().getPAttributes().isEmpty()) {
            for (Player all : plugin.getSpectateManager().getPAttributes().keySet())
                if (all.isOnline()) restoreInventory(all);
        }
    }
}