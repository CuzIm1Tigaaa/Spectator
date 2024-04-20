package de.cuzim1tigaaa.spectator;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import de.cuzim1tigaaa.spectator.files.*;
import de.cuzim1tigaaa.spectator.listener.ContainerListenerTest;
import de.cuzim1tigaaa.spectator.spectate.SpectateState;
import de.cuzim1tigaaa.spectator.spectate.SpectateUtils;
import org.bukkit.Location;
import org.junit.jupiter.api.*;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

public class SpectateTest {

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
	@DisplayName("enter spectator mode using /spectate")
	void spectateUsingSpectateCommand() {
		PlayerMock player = server.addPlayer("CuzIm1Tigaaa");
		player.getPlayer().setOp(true);

		assertTrue(player.performCommand("spectate"));

		final SpectateUtils utils = plugin.getSpectateUtils();
		assertTrue(utils.isSpectator(player), String.format("%s should be in spectator mode", player.getPlayer().getName()));
		assertEquals(utils.getSpectateInfo().get(player.getUniqueId()).getState(), SpectateState.SPECTATING, String.format("%s should be in spectating state", player.getPlayer().getName()));
	}

	@Test
	@DisplayName("enter spectator mode using /spectatehere")
	void spectateUsingSpectatehereCommand() {
		PlayerMock player = server.addPlayer("CuzIm1Tigaaa");
		player.getPlayer().setOp(true);

		assertTrue(player.performCommand("spectatehere"));

		final SpectateUtils utils = plugin.getSpectateUtils();
		assertTrue(utils.isSpectator(player), String.format("%s should be in spectator mode", player.getPlayer().getName()));
		assertEquals(utils.getSpectateInfo().get(player.getUniqueId()).getState(), SpectateState.SPECTATING, String.format("%s should be in spectating state", player.getPlayer().getName()));
	}


	@Test
	@DisplayName("enter spectator mode, change position and unspectate using /spectate")
	void unspectateUsingSpectateCommand() {
		PlayerMock player = server.addPlayer("CuzIm1Tigaaa");
		player.getPlayer().setOp(true);

		Location oldLocation = new Location(player.getWorld(), 0, 100, 0),
				newLocation = new Location(player.getWorld(), 20, 100, 10);

		player.simulatePlayerMove(oldLocation);
		assertEquals(oldLocation, player.getLocation(),                                 String.format("%s should be at this location", player.getPlayer().getName()));


		assertTrue(Config.getBoolean(Paths.CONFIG_SAVE_PLAYERS_LOCATION));
		assertTrue(player.performCommand("spectate"));

		final SpectateUtils utils = plugin.getSpectateUtils();
		assertTrue(utils.isSpectator(player),                                           String.format("%s should be in spectator mode", player.getPlayer().getName()));
		assertEquals(utils.getSpectateStartLocation().get(player.getUniqueId()), oldLocation,   String.format("%s should have his old location saved", player.getPlayer().getName()));

		player.simulatePlayerMove(newLocation);
		assertEquals(newLocation, player.getLocation(),                                 String.format("%s should be at this location", player.getPlayer().getName()));
		assertTrue(player.performCommand("spectate"));

		assertFalse(utils.isSpectator(player),                                          String.format("%s should no longer be in spectator mode", player.getPlayer().getName()));

		server.getScheduler().performOneTick();
		assertEquals(oldLocation, player.getLocation(),                             String.format("%s should be at his old location", player.getPlayer().getName()));
	}

	@Test
	@DisplayName("enter spectator mode, change position and unspectate using /spectatehere")
	void unspectateUsingSpectatehereCommand() {
		PlayerMock player = server.addPlayer("CuzIm1Tigaaa");
		player.getPlayer().setOp(true);

		Location oldLocation = new Location(player.getWorld(), 0, 100, 0),
				newLocation = new Location(player.getWorld(), 20, 100, 10);

		player.simulatePlayerMove(oldLocation);
		assertEquals(player.getLocation(), oldLocation,                                 String.format("%s should be at this location", player.getPlayer().getName()));

		assertTrue(player.performCommand("spectatehere"));

		final SpectateUtils utils = plugin.getSpectateUtils();
		assertTrue(utils.isSpectator(player),                                           String.format("%s should be in spectator mode", player.getPlayer().getName()));
		assertEquals(utils.getSpectateStartLocation().get(player.getUniqueId()), oldLocation,   String.format("%s should have his old location saved", player.getPlayer().getName()));

		player.simulatePlayerMove(newLocation);
		assertEquals(player.getLocation(), newLocation,                                 String.format("%s should be at this location", player.getPlayer().getName()));
		assertTrue(player.performCommand("spectatehere"));

		assertFalse(utils.isSpectator(player),                                          String.format("%s should no longer be in spectator mode", player.getPlayer().getName()));
		assertEquals(player.getLocation(), newLocation,                                 String.format("%s should be at his old location", player.getPlayer().getName()));
	}


	@Test
	@DisplayName("spectate another player")
	void spectatePlayer() {
		PlayerMock player = server.addPlayer("CuzIm1Tigaaa");
		player.getPlayer().setOp(true);

		assertTrue(player.hasPermission(Permissions.COMMAND_SPECTATE_OTHERS));
		PlayerMock target = server.addPlayer("Phurunus");

		assertTrue(player.performCommand("spectate Phurunus"));

		final SpectateUtils utils = plugin.getSpectateUtils();
		assertTrue(utils.isSpectator(player), String.format("%s should be in spectator mode", player.getPlayer().getName()));
		assertFalse(utils.isSpectator(target), String.format("%s should not be in spectator mode", target.getPlayer().getName()));
		assertTrue(utils.isSpectating(player, target), String.format("%s should be spectating %s", player.getPlayer().getName(), target.getPlayer().getName()));
		assertEquals(utils.getTargetOf(player), target, String.format("%s's target should be %s", player.getPlayer().getName(), target.getPlayer().getName()));
		assertTrue(utils.getSpectatorsOf(target).contains(player), String.format("%s should have %s as spectator", target.getPlayer().getName(), player.getPlayer().getName()));
	}
}