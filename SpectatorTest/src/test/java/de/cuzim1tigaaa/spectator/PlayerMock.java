package de.cuzim1tigaaa.spectator;

import be.seeseemelk.mockbukkit.ServerMock;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;

public class PlayerMock extends be.seeseemelk.mockbukkit.entity.PlayerMock {

	private Entity spectatorTarget;

	public PlayerMock(@NotNull ServerMock server, @NotNull String name) {
		super(server, name);
	}

	@Override
	public void setSpectatorTarget(Entity entity) {
		this.spectatorTarget = entity;
	}

	@Override
	public Entity getSpectatorTarget() {
		return this.spectatorTarget;
	}
}