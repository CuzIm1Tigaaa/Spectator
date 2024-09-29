package de.cuzim1tigaaa.spectator.listener;

import be.seeseemelk.mockbukkit.*;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import de.cuzim1tigaaa.spectator.Constants;
import de.cuzim1tigaaa.spectator.Spectator;
import de.cuzim1tigaaa.spectator.spectate.SpectateUtilsGeneral;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SpectatorListenerTest {

	ServerMock server = ContainerListenerTest.server;
	Spectator plugin = ContainerListenerTest.plugin;

//	@BeforeEach
//	public void load() {
//		server = MockBukkit.mock();
//		plugin = MockBukkit.load(Spectator.class);
//
//		File f = new File("./src/test/resources/config.yml");
//		assertTrue(f.exists(), f.getAbsolutePath());
//		Config.loadConfig(plugin, f);
//
//		server.getScheduler().performTicks(100L);
//	}
//
//	@AfterEach
//	public void unload() {
//		server.getScheduler().performTicks(100L);
//		MockBukkit.unmock();
//	}

	@Test
	@DisplayName("Test if a spectating player receives an update notification")
	void testPlayerJoinReceiveUpdate() {
		PlayerMock player = server.addPlayer(Constants.Player);
		assert player.getPlayer() != null;
		player.setOp(true);

		server.getPluginManager().assertEventFired(PlayerJoinEvent.class, event -> {
			String message;
			return event.getPlayer().equals(player);
		});
	}

	@Test
	@DisplayName("Test if a spectating players target is null after sneaking (dismounting)")
	void testPlayerSneakDismountTarget() throws UnimplementedOperationException {
	}
}