package de.cuzim1tigaaa.spectator;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.nio.file.*;

import static org.junit.jupiter.api.Assertions.*;

public class SpectateTest {

	public static ServerMock server;
	public static Spectator plugin;

	@BeforeEach
	public void load() throws IOException {
		server = MockBukkit.mock();

		Path langDir = Paths.get("target/test-classes/lang");
		if (!Files.exists(langDir))
			Files.createDirectories(langDir);

		try {
			plugin = MockBukkit.load(Spectator.class);
		}catch(Exception ignored) {
		}
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

		SpectateAPI spectateAPI = plugin.getSpectateAPI();
		spectateAPI.getSpectateGeneral().spectate(player, targets[0]);
		assertTrue(spectateAPI.isSpectator(player), "Player is not in spectator mode");
		assertEquals(spectateAPI.getTargetOf(player), targets[0], "Wrong target " + spectateAPI.getTargetOf(player).getName());

		spectateAPI.getSpectateGeneral().spectate(player, null);
		assertTrue(spectateAPI.isSpectator(player), "Player is not in spectator mode");
		assertNull(spectateAPI.getTargetOf(player), "Player target is not null");
	}
}