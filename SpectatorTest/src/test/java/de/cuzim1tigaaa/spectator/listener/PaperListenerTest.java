package de.cuzim1tigaaa.spectator.listener;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import de.cuzim1tigaaa.spectator.Constants;
import de.cuzim1tigaaa.spectator.Spectator;
import de.cuzim1tigaaa.spectator.files.Config;
import de.cuzim1tigaaa.spectator.spectate.SpectateUtils;
import org.bukkit.*;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.*;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class PaperListenerTest {

	private static ServerMock server;
	private static Spectator plugin;

	@BeforeAll
	public static void load() {
		server = MockBukkit.mock();
		plugin = MockBukkit.load(Spectator.class);

		File f = new File("./src/test/resources/config.yml");
		assertTrue(f.exists(), f.getAbsolutePath());
		Config.loadConfig(plugin, f);
	}

	@AfterAll
	public static void unload() {
		MockBukkit.unmock();
	}

	@Test
	@DisplayName("Test if a spectating player does not receive advancements")
	void testSpectatingPlayerDoesNotReceiveAdvancements() {
		PlayerMock player = server.addPlayer(Constants.Player);
		assert player.getPlayer() != null;
		player.getPlayer().setOp(true);

		PlayerMock target = server.addPlayer(Constants.Target);
		assert target.getPlayer() != null;

		assertTrue(player.performCommand("spectate " + Constants.Target));
		final SpectateUtils utils = plugin.getSpectateUtils();
		assertTrue(utils.isSpectator(player), String.format("%s should be in spectator mode", player.getPlayer().getName()));
		assertTrue(utils.isSpectating(player, target), String.format("%s should be spectating %s", player.getPlayer().getName(), target.getPlayer().getName()));

		target.getInventory().addItem(new ItemStack(Material.DIAMOND));
//		System.out.println(Bukkit.getAdvancement(NamespacedKey.minecraft("story/mine_diamond")).getCriteria());
		// TODO: Test if the player does not receive the advancement story/mine_diamond
	}

}