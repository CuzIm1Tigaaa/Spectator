package de.cuzim1tigaaa.spectator.listener;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.block.state.ChestMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import de.cuzim1tigaaa.spectator.Constants;
import de.cuzim1tigaaa.spectator.Spectator;
import de.cuzim1tigaaa.spectator.files.Config;
import de.cuzim1tigaaa.spectator.spectate.SpectateUtils;
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
		PlayerMock player = server.addPlayer(Constants.Player);
		assert player.getPlayer() != null;
		player.getPlayer().setOp(true);

		player.getWorld().getBlockAt(0, 80, 0).setType(Material.CHEST);
		ChestMock c = (ChestMock) player.getWorld().getBlockAt(0, 80, 0).getState();

		PlayerMock target = server.addPlayer(Constants.Target);
		assert target.getPlayer() != null;

		assertTrue(player.performCommand("spectate " + Constants.Target));

		final SpectateUtils utils = plugin.getSpectateUtils();
		assertTrue(utils.isSpectator(player), String.format("%s should be in spectator mode", player.getPlayer().getName()));
		assertTrue(utils.isSpectating(player, target), String.format("%s should be spectating %s", player.getPlayer().getName(), target.getPlayer().getName()));

		target.openInventory(c.getInventory());
		assertEquals(target.getOpenInventory().getTopInventory(), c.getInventory());
		assertEquals(player.getOpenInventory().getTopInventory(), c.getInventory());
	}

}