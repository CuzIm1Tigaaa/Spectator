package de.cuzim1tigaaa.spectator.armorstand;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public interface Armorstand {

	void hideArmorstand(Player player, Entity entity);

	void showArmorstand(Player player, Entity entity);

}