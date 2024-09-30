package de.cuzim1tigaaa.spectator;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import de.cuzim1tigaaa.spectator.cycle.Cycle;
import de.cuzim1tigaaa.spectator.cycle.CycleTask;
import org.junit.jupiter.api.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class SpectateTest {

	public static ServerMock server;
	public static Spectator plugin;

	@BeforeEach
	public void load() throws IOException {
		server = MockBukkit.mock();

		Path langDir = Paths.get("target/test-classes/lang");
		if (!Files.exists(langDir))
			Files.createDirectories(langDir);

		plugin = MockBukkit.load(Spectator.class);

		File f = new File("./src/test/resources/config.yml");
		assertTrue(f.exists(), f.getAbsolutePath());
		server.getScheduler().performTicks(100L);


	}

	@AfterEach
	public void unload() {
		MockBukkit.unmock();
	}

	@Test
	@DisplayName("Test")
	void test() {
		PlayerMock player = new PlayerMock(server, "CuzIm1Tigaaa");

		PlayerMock[] targets = new PlayerMock[5];
		for(int i = 0; i < 5; i++)
			targets[i] = new PlayerMock(server, "Player_0" + i);

		assertTrue(player.performCommand("speccycle start 10"), "Command failed");
		SpectateAPI spectateAPI = plugin.getSpectateAPI();
//		spectateAPI.getSpectateCycle().startCycle(player, new CycleTask(10, new Cycle(player, null, false)));
		server.getScheduler().performTicks(1000L);
		assertTrue(spectateAPI.isCyclingSpectator(player), "Player is not cycling");
	}
}