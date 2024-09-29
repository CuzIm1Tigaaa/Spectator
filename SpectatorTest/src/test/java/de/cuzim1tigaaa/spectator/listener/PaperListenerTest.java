package de.cuzim1tigaaa.spectator.listener;

import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import de.cuzim1tigaaa.spectator.Constants;
import de.cuzim1tigaaa.spectator.Spectator;
import de.cuzim1tigaaa.spectator.spectate.SpectateUtilsGeneral;
import org.bukkit.*;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class PaperListenerTest {

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
	@DisplayName("Test if a spectating player does not receive advancements")
	void testSpectatingPlayerDoesNotReceiveAdvancements() {
	}

}