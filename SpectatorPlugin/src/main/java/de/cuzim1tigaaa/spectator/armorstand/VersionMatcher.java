package de.cuzim1tigaaa.spectator.armorstand;

import org.bukkit.Bukkit;

import java.util.HashMap;
import java.util.Map;

public class VersionMatcher {

	private static final Map<String, String> VERSION_TO_REVISION = new HashMap<>() {
		{
			this.put("1.16", "1_16_R1");
			this.put("1.16.1", "1_16_R1");
			this.put("1.16.2", "1_16_R2");
			this.put("1.16.3", "1_16_R2");
			this.put("1.16.4", "1_16_R3");
			this.put("1.16.5", "1_16_R3");
			this.put("1.17", "1_17_R1");
			this.put("1.17.1", "1_17_R1");
		}
	};
	/* This needs to be updated to reflect the newest available version wrapper */
	private static final String FALLBACK_REVISION = "1_16_R1";

	public Armorstand match() {
		String craftBukkitPackage = Bukkit.getServer().getClass().getPackage().getName();

		String rVersion;
		if (!craftBukkitPackage.contains(".v")) { // cb package not relocated (i.e. paper 1.20.5+)
			final String version = Bukkit.getBukkitVersion().split("-")[0];
			rVersion = VERSION_TO_REVISION.getOrDefault(version, FALLBACK_REVISION);
		} else {
			rVersion = craftBukkitPackage.split("\\.")[3].substring(1);
		}

		try {
			System.out.println("Using version " + rVersion);
			System.out.println(getClass().getPackage().getName() + ".Armorstand_" + rVersion);
			return (Armorstand) Class.forName(getClass().getPackage().getName() + ".Armorstand_" + rVersion)
					.getDeclaredConstructor()
					.newInstance();
		} catch (ClassNotFoundException exception) {
			throw new IllegalStateException("Spectator does not support server version \"" + rVersion + "\"", exception);
		} catch (ReflectiveOperationException exception) {
			throw new IllegalStateException("Failed to instantiate armorstand for version " + rVersion, exception);
		}
	}
}