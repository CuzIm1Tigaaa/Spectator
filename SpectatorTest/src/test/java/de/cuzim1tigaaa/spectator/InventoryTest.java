package de.cuzim1tigaaa.spectator;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import com.onarandombox.MultiverseCore.MultiverseCore;
import de.cuzim1tigaaa.spectator.files.Config;
import de.cuzim1tigaaa.spectator.listener.ContainerListenerTest;
import org.bukkit.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.InvalidPluginException;
import org.junit.jupiter.api.*;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class InventoryTest {

	ServerMock server = ContainerListenerTest.server;
	Spectator plugin = ContainerListenerTest.plugin;

	MultiverseCore mvCore;

//	@BeforeEach
//	public void load() {
//		server = MockBukkit.mock();
//		plugin = MockBukkit.load(Spectator.class);
//
//		File f = new File("./src/test/resources/config.yml");
//		assertTrue(f.exists(), f.getAbsolutePath());
//		Config.loadConfig(plugin, f);
//
//		File worldsFile = new File("./src/test/resources/plugins/multiversecore/worlds.yml");
//		assertTrue(worldsFile.exists(), worldsFile.getAbsolutePath());
//
//		mvCore = MockBukkit.load(MultiverseCore.class);
//		mvCore.getMVWorldManager().loadWorldConfig(worldsFile);
//
//		server.getScheduler().performTicks(100L);
//	}
//
//	@AfterEach
//	public void unload() {
//		server.getScheduler().performTicks(100L);
//		MockBukkit.unmock();
//	}

	private final Map<World, ItemStack[]> inventories = new HashMap<>();

	void init() {
		inventories.put(server.getWorld("world"), new ItemStack[]{
				new ItemStack(Material.DIAMOND_SWORD),
				new ItemStack(Material.DIAMOND_PICKAXE),
				new ItemStack(Material.DIAMOND_AXE),
				new ItemStack(Material.DIAMOND_HOE)
		});

		inventories.put(server.getWorld("other"), new ItemStack[]{
				new ItemStack(Material.ENCHANTED_GOLDEN_APPLE, 64),
				new ItemStack(Material.ENDER_PEARL, 16),
				new ItemStack(Material.BLAZE_ROD, 16),
				new ItemStack(Material.END_PORTAL_FRAME, 12),
		});
	}

	@Test
	@DisplayName("Check if a players inventory get saved correctly")
	void inventorySaved() {
		init();
		PlayerMock player = server.addPlayer("CuzIm1Tigaaa");
		player.getPlayer().setOp(true);
		player.getInventory().setContents(inventories.get(server.getWorld("world")));
		Inventory expected = player.getInventory();

		assertTrue(player.performCommand("spectate"));

		assertTrue(player.getInventory().isEmpty(),
				printInventory(player.getInventory().getContents()));
		assertTrue(player.performCommand("spectate"));

		server.getScheduler().performOneTick();
		assertEquals(expected.getContents(), player.getInventory().getContents(),
				printInventory(player.getInventory().getContents()));
	}

	String printInventory(ItemStack[]... inventories) {
		StringBuilder result = new StringBuilder("\n");
		for(ItemStack[] inv : inventories) {
			result.append("\n");
			int col = 0;
			for(ItemStack i : inv) {
				if(i == null)
					result.append(String.format("%-20s", "null"));
				else
					result.append(String.format("%-20s", i.getType().name()));

				col++;
				if(col == 9) {
					col = 0;
					result.append("\n");
				}
			}
			result.append("\n\n");
		}
		result.append("\n");
		return result.toString();
	}
}