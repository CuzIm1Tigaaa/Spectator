package de.cuzim1tigaaa.spectate.files;

import de.cuzim1tigaaa.spectate.Main;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class Config {

    private static FileConfiguration message;

    public static boolean hideTab;
    public static boolean mirrorInventory;
    public static boolean saveLocation;
    public static boolean saveFlying;
    public static boolean kickOnCycle;

    public static String getMessage(String path, Object... replace) {
        String msg = message.getString(path);
        for(int i = 0; i < replace.length; i++) {
            String target = (String) replace[i];
            String replacement = replace[i += 1].toString();
            msg = msg.replace("[" + target + "]", replacement);
            if(target == null) {
                msg = null;
            }
            assert msg != null;
        }
        if(msg == null) {
            return "§cError: Message §7" + path + " §cdoes not exist!";
        }
        return ChatColor.translateAlternateColorCodes('&', msg);
    }

    public static void loadConfig() {
        File messagesfile = new File(Main.getInstance().getDataFolder(), "config.yml");
        try {
            if(!messagesfile.exists()) {
                message = new YamlConfiguration();
                message.save(messagesfile);
            }
            message = YamlConfiguration.loadConfiguration(messagesfile);
            message.set("Config.hideTab", hideTab = message.getBoolean("Config.hideTab", true));
            message.set("Config.mirrorInventory", mirrorInventory = message.getBoolean("Config.mirrorInventory", true));
            message.set("Config.saveLocation", saveLocation = message.getBoolean("Config.saveLocation", true));
            message.set("Config.saveFlying", saveFlying = message.getBoolean("Config.saveFlying", false));
            message.set("Config.kickOnCycle", kickOnCycle = message.getBoolean("Config.kickOnCycle", false));
            message.set("Config.Error.cannot", message.getString("Config.Error.cannot", "&7[player] &ccannot be spectated at the moment!"));
            message.set("Config.Error.dismount", message.getString("Config.Error.dismount", "&cYou cannot dismount while in Speccycle-Mode! Use &7/spectatecycle stop &cto leave Speccycle-Mode."));
            message.set("Config.Error.gm", message.getString("Config.Error.gm", "&cYou cannot change your GameMode while spectating! Use &7/spec &cto leave Spectator-Mode."));
            message.set("Config.Error.isNot", message.getString("Config.Error.isNot", "&cYou have to be a Player!"));
            message.set("Config.Error.nobody", message.getString("Config.Error.nobody", "&cThere are no spectators at the moment!"));
            message.set("Config.Error.noPlayers", message.getString("Config.Error.noPlayers", "&cThere are not enough Players online."));
            message.set("Config.Error.offline", message.getString("Config.Error.offline", "&7[player] &cis not Online!"));
            message.set("Config.Error.cycleOnly", message.getString("Config.Error.cycleOnly", "&cYou can only use &7/spectatecycle start <Interval>"));
            message.set("Config.Error.cycleStop", message.getString("Config.Error.cycleStop", "§cYou cannot leave Speccycle via &7/spectatecycle stop&c. Please use &7/spectate &cto unspectate."));
            message.set("Config.Error.same", message.getString("Config.Error.same", "&cYou are already spectating &7[player]&c!"));
            message.set("Config.Error.self", message.getString("Config.Error.self", "&cYou cannot spectate yourself!"));
            message.set("Config.Permission", message.getString("Config.Permission", "&cYou do not have permission to perform this Command!"));
            message.set("Config.Plugin.reload", message.getString("Config.Plugin.reload", "&7Plugin has been reloaded."));
            message.set("Config.Spectate.Cycle.start", message.getString("Config.Spectate.Cycle.start", "&7Speccycle start with interval &c[interval]&7."));
            message.set("Config.Spectate.Cycle.stop", message.getString("Config.Spectate.Cycle.stop", "&7Speccycle stopped."));
            message.set("Config.Spectate.Cycle.pause", message.getString("Config.Spectate.Cycle.pause", "&7Speccycle paused."));
            message.set("Config.Spectate.Cycle.resume", message.getString("Config.Spectate.Cycle.resume", "&7Speccycle resumed with interval &c[interval]&7."));
            message.set("Config.Spectate.Cycle.running", message.getString("Config.Spectate.Cycle.running", "&cYou are already in Speccycle-Mode!"));
            message.set("Config.Spectate.Cycle.notRunning", message.getString("Config.Spectate.Cycle.notRunning", "&cYou are not in Speccycle-Mode!"));
            message.set("Config.Spectate.Cycle.notPaused", message.getString("Config.Spectate.Cycle.notPaused", "&cSpeccycle-Mode is not paused!"));
            message.set("Config.Spectate.leave", message.getString("Config.Spectate.leave", "&7You are no longer in Spectator-Mode."));
            message.set("Config.Spectate.use", message.getString("Config.Spectate.use", "&7You are now in Spectator-Mode."));
            message.set("Config.Spectate.others", message.getString("Config.Spectate.others", "&7You are now spectating &c[player]."));
            message.set("Config.Spectate.give.leave", message.getString("Config.Spectate.give.leave", "[player] is no longer in Spectator-Mode."));
            message.set("Config.Spectate.give.use", message.getString("Config.Spectate.give.use", "[player] &7is now in Spectator-Mode."));
            message.set("Config.Spectate.give.others", message.getString("Config.Spectate.give.others", "[player] &7is now spectating [target]."));
            message.set("Config.Spectate.unSpectate.others", message.getString("Config.Spectate.unSpectate.others", "&7[player] is no longer spectating."));
            message.save(messagesfile);
        }catch (IOException exception) {
            exception.printStackTrace();
        }
    }
}