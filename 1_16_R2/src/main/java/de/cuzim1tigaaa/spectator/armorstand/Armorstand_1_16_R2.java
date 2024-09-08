package de.cuzim1tigaaa.spectator.armorstand;

import net.minecraft.server.v1_16_R2.*;
import org.bukkit.craftbukkit.v1_16_R2.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_16_R2.entity.CraftPlayer;

public class Armorstand_1_16_R2 implements Armorstand {

	@Override
	public void hideArmorstand(org.bukkit.entity.Player player, org.bukkit.entity.Entity entity) {
		PlayerConnection connection = ((CraftPlayer) player).getHandle().playerConnection;
		connection.sendPacket(new PacketPlayOutEntityDestroy(entity.getEntityId()));
	}

	@Override
	public void showArmorstand(org.bukkit.entity.Player player, org.bukkit.entity.Entity entity) {
		PlayerConnection connection = ((CraftPlayer) player).getHandle().playerConnection;
		Entity nmsEntity = ((CraftEntity) entity).getHandle();
		connection.sendPacket(new PacketPlayOutSpawnEntity(nmsEntity));
	}
}