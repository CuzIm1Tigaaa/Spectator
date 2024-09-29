package de.cuzim1tigaaa.spectator.listener;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.block.state.ChestMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import de.cuzim1tigaaa.spectator.Constants;
import de.cuzim1tigaaa.spectator.Spectator;
import de.cuzim1tigaaa.spectator.spectate.SpectateUtilsGeneral;
import org.bukkit.Material;
import org.junit.jupiter.api.*;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

public class ContainerListenerTest {

	public static ServerMock server;
	public static Spectator plugin;

	@BeforeEach
	public void load() {
		server = MockBukkit.mock();
		plugin = MockBukkit.load(Spectator.class);

		File f = new File("./src/test/resources/config.yml");
		assertTrue(f.exists(), f.getAbsolutePath());
//		Config.loadConfig(plugin, f);

		server.getScheduler().performTicks(100L);
	}

	@AfterEach
	public void unload() {
		server.getScheduler().performTicks(100L);
		MockBukkit.unmock();
	}

	@Test
	@DisplayName("Test if a spectating player also has a chest inventory open when a target opens a chest")
	void test_() {
	}
}