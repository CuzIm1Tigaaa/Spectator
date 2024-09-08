package de.cuzim1tigaaa.spectator.armorstand;

import net.minecraft.network.protocol.game.PacketPlayOutEntityDestroy;
import net.minecraft.network.protocol.game.PacketPlayOutSpawnEntity;
import net.minecraft.server.network.PlayerConnection;
import net.minecraft.world.entity.Entity;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;

public class Armorstand_1_17_R1 implements Armorstand {

	@Override
	public void hideArmorstand(org.bukkit.entity.Player player, org.bukkit.entity.Entity entity) {
		PlayerConnection connection = ((CraftPlayer) player).getHandle().b;
		connection.sendPacket(new PacketPlayOutEntityDestroy(entity.getEntityId()));
	}

	@Override
	public void showArmorstand(org.bukkit.entity.Player player, org.bukkit.entity.Entity entity) {
		PlayerConnection connection = ((CraftPlayer) player).getHandle().b;
		Entity nmsEntity = ((CraftEntity) entity).getHandle();
		connection.sendPacket(new PacketPlayOutSpawnEntity(nmsEntity));
	}
}