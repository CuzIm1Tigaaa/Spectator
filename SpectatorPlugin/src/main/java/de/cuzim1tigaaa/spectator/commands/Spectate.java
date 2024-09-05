package de.cuzim1tigaaa.spectator.commands;

import de.cuzim1tigaaa.spectator.Spectator;
import de.cuzim1tigaaa.spectator.files.Config;
import de.cuzim1tigaaa.spectator.files.Paths;
import de.cuzim1tigaaa.spectator.spectate.SpectateInformation;
import de.cuzim1tigaaa.spectator.spectate.SpectateUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import java.util.*;

import static de.cuzim1tigaaa.spectator.files.Messages.getMessage;
import static de.cuzim1tigaaa.spectator.files.Permissions.*;

public class Spectate implements CommandExecutor, TabCompleter {

	private final Spectator plugin;
	private final SpectateUtils spectateUtils;

	public Spectate(Spectator plugin) {
		Objects.requireNonNull(plugin.getCommand("spectate")).setExecutor(this);
		this.plugin = plugin;
		this.spectateUtils = plugin.getSpectateUtils();
	}

	@Override
	public boolean onCommand(@Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String label, @Nonnull String[] args) {
		if(!(sender instanceof Player)) {
			if(args.length == 0) {
				sender.sendMessage(getMessage(sender, Paths.MESSAGE_DEFAULT_SENDER));
				return true;
			}

			if(args.length == 1) {
				Player target = Bukkit.getPlayer(args[0]);
				if(target == null) {
					sender.sendMessage(getMessage(sender, Paths.MESSAGES_GENERAL_OFFLINEPLAYER, "TARGET", args[0]));
					return true;
				}

				if(spectateUtils.isSpectator(target)) {
					spectateUtils.unspectate(target, true);
					target.sendMessage(getMessage(target, Paths.MESSAGES_COMMANDS_SPECTATE_LEAVE_OWN));
					sender.sendMessage(getMessage(sender, Paths.MESSAGES_COMMANDS_SPECTATE_LEAVE_OTHER, "TARGET", target.getName()));
					return true;
				}

				spectateUtils.getSpectateStartLocation().put(target.getUniqueId(), target.getLocation());
				spectateUtils.spectate(target, null);
				target.sendMessage(getMessage(target, Paths.MESSAGES_COMMANDS_SPECTATE_JOIN_OWN));
				sender.sendMessage(getMessage(sender, Paths.MESSAGES_COMMANDS_SPECTATE_JOIN_OTHER, "TARGET", target.getName()));
				return true;
			}
		}

		if(!(sender instanceof Player player) || (args.length > 1 && player.hasPermission(COMMAND_SPECTATE_CHANGE_OTHERS))) {
			Player spectator = Bukkit.getPlayer(args[0]);
			if(spectator == null) {
				sender.sendMessage(getMessage(sender, Paths.MESSAGES_GENERAL_OFFLINEPLAYER, "TARGET", args[0]));
				return true;
			}

			Player target = Bukkit.getPlayer(args[1]);
			if(target == null) {
				sender.sendMessage(getMessage(sender, Paths.MESSAGES_GENERAL_OFFLINEPLAYER, "TARGET", args[1]));
				return true;
			}

			if(spectateUtils.isSpectator(target)) {
				sender.sendMessage(getMessage(sender, Paths.MESSAGES_GENERAL_BYPASS_TELEPORT, "TARGET", target.getName()));
				return true;
			}
			spectateUtils.getSpectateStartLocation().put(spectator.getUniqueId(), spectator.getLocation());
			spectateUtils.spectate(spectator, target);
			spectator.sendMessage(getMessage(spectator, Paths.MESSAGES_COMMANDS_SPECTATE_PLAYER, "TARGET", target.getName()));
			sender.sendMessage(getMessage(sender, Paths.MESSAGES_COMMANDS_SPECTATE_JOIN_OTHER, "TARGET", spectator.getName()));
			return true;
		}

		if(!hasPermission(player, COMMAND_SPECTATE_GENERAL)) {
			player.sendMessage(getMessage(player, Paths.MESSAGE_DEFAULT_PERMISSION));
			return true;
		}

		if(args.length == 0 || !hasPermission(player, COMMAND_SPECTATE_OTHERS)) {
			if(spectateUtils.isSpectator(player)) {
				spectateUtils.unspectate(player, true);
				player.sendMessage(getMessage(player, Paths.MESSAGES_COMMANDS_SPECTATE_LEAVE_OWN));
				return true;
			}
			spectateUtils.getSpectateStartLocation().put(player.getUniqueId(), player.getLocation());
			spectateUtils.spectate(player, null);
			player.sendMessage(getMessage(player, Paths.MESSAGES_COMMANDS_SPECTATE_JOIN_OWN));
			return true;
		}

		if(hasPermission(player, UTILS_HIDE_ARMORSTAND)) {
			if(args[0].equalsIgnoreCase("-armorstand") && Config.getBoolean(Paths.CONFIG_HIDE_ARMOR_STANDS)) {
				SpectateInformation info = spectateUtils.getSpectateInformation(player);
				if(info == null) {
					player.sendMessage(getMessage(player, Paths.MESSAGES_GENERAL_NOTSPECTATOR));
					return true;
				}

				info.setHideArmorStands(!info.isHideArmorStands());
				if(info.isHideArmorStands()) {
					info.hideArmorstands();
					player.sendMessage(getMessage(player, Paths.MESSAGES_COMMANDS_SPECTATE_ARMORSTANDS_OFF));
				}else {
					info.restoreArmorstands();
					player.sendMessage(getMessage(player, Paths.MESSAGES_COMMANDS_SPECTATE_ARMORSTANDS_ON));
				}
				return true;
			}
		}

		Player target = Bukkit.getPlayer(args[0]);
		if(target == null || !target.isOnline()) {
			player.sendMessage(getMessage(player, Paths.MESSAGES_GENERAL_OFFLINEPLAYER, "TARGET", args[0]));
			return true;
		}

		if(target.getUniqueId().equals(player.getUniqueId())) {
			player.sendMessage(getMessage(player, Paths.MESSAGES_GENERAL_YOURSELF));
			return true;
		}

		if(spectateUtils.isSpectating(player, target)) {
			player.sendMessage(getMessage(player, Paths.MESSAGES_GENERAL_SAMEPLAYER, "TARGET", target.getName()));
			return true;
		}

		if(spectateUtils.isSpectating(target, player) || hasPermission(target, BYPASS_SPECTATED)) {
			if(!hasPermission(player, BYPASS_SPECTATEALL)) {
				player.sendMessage(getMessage(player, Paths.MESSAGES_GENERAL_BYPASS_TELEPORT, "TARGET", target.getName()));
				return true;
			}
		}

		if(plugin.getMultiverseCore() != null) {
			if(!player.getWorld().getUID().equals(target.getWorld().getUID())) {
				String world = plugin.getMultiverseCore().getMVWorldManager().getMVWorld(target.getWorld()).getPermissibleName();
				if(!player.hasPermission("multiverse.access." + world)) {
					player.sendMessage(getMessage(player, Paths.MESSAGES_COMMANDS_SPECTATE_MULTIVERSE, "TARGET", target.getName()));
					return true;
				}
			}
		}
		spectateUtils.getSpectateStartLocation().put(player.getUniqueId(), player.getLocation());
		spectateUtils.spectate(player, target);
		player.sendMessage(getMessage(player, Paths.MESSAGES_COMMANDS_SPECTATE_PLAYER, "TARGET", target.getName()));
		return true;
	}

	@Override
	public List<String> onTabComplete(@Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String s, @Nonnull String[] args) {
		if(args.length == 1) {
			if(args[0].startsWith("-"))
				return List.of("-armorstand");
			if(!(sender instanceof Player player) || hasPermission(player, COMMAND_SPECTATE_OTHERS))
				return plugin.getOnlinePlayerNames();
		}
		if(args.length == 2) {
			if(!(sender instanceof Player player) || hasPermission(player, COMMAND_SPECTATE_CHANGE_OTHERS))
				return plugin.getOnlinePlayerNames();
		}
		return Collections.emptyList();
	}
}