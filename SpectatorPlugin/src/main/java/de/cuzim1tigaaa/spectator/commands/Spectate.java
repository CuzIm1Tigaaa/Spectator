package de.cuzim1tigaaa.spectator.commands;

import de.cuzim1tigaaa.spectator.SpectateAPI;
import de.cuzim1tigaaa.spectator.Spectator;
import de.cuzim1tigaaa.spectator.files.*;
import de.cuzim1tigaaa.spectator.spectate.SpectateInformation;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import java.util.*;

import static de.cuzim1tigaaa.spectator.files.Permissions.*;

public class Spectate implements CommandExecutor, TabCompleter {

	private final Spectator plugin;
	private final SpectateAPI spectateAPI;

	public Spectate(Spectator plugin) {
		Objects.requireNonNull(plugin.getCommand("spectate")).setExecutor(this);
		this.plugin = plugin;
		this.spectateAPI = plugin.getSpectateAPI();
	}

	@Override
	public boolean onCommand(@Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String label, @Nonnull String[] args) {
		if(!(sender instanceof Player)) {
			if(args.length == 0) {
				Messages.sendMessage(sender, Paths.MESSAGE_DEFAULT_SENDER);
				return true;
			}

			if(args.length == 1) {
				Player target = Bukkit.getPlayer(args[0]);
				if(target == null) {
					Messages.sendMessage(sender, Paths.MESSAGES_GENERAL_OFFLINEPLAYER, "TARGET", args[0]);
					return true;
				}

				if(spectateAPI.isSpectator(target)) {
					spectateAPI.getSpectateGeneral().unspectate(target, true);
					Messages.sendMessage(target, Paths.MESSAGES_COMMANDS_SPECTATE_LEAVE_OWN);
					Messages.sendMessage(sender, Paths.MESSAGES_COMMANDS_SPECTATE_LEAVE_OTHER, "TARGET", target.getName());
					return true;
				}

				spectateAPI.getSpectateGeneral().spectate(target, null);
				Messages.sendMessage(target, Paths.MESSAGES_COMMANDS_SPECTATE_JOIN_OWN);
				Messages.sendMessage(sender, Paths.MESSAGES_COMMANDS_SPECTATE_JOIN_OTHER, "TARGET", target.getName());
				return true;
			}
		}

		if(!(sender instanceof Player player) || (args.length > 1 && player.hasPermission(COMMAND_SPECTATE_CHANGE_OTHERS))) {
			Player spectator = Bukkit.getPlayer(args[0]);
			if(spectator == null) {
				Messages.sendMessage(sender, Paths.MESSAGES_GENERAL_OFFLINEPLAYER, "TARGET", args[0]);
				return true;
			}

			Player target = Bukkit.getPlayer(args[1]);
			if(target == null) {
				Messages.sendMessage(sender, Paths.MESSAGES_GENERAL_OFFLINEPLAYER, "TARGET", args[1]);
				return true;
			}

			if(spectateAPI.isSpectator(target)) {
				Messages.sendMessage(sender, Paths.MESSAGES_GENERAL_BYPASS_TELEPORT, "TARGET", target.getName());
				return true;
			}
			spectateAPI.getSpectateGeneral().spectate(spectator, target);
			Messages.sendMessage(spectator, Paths.MESSAGES_COMMANDS_SPECTATE_PLAYER, "TARGET", target.getName());
			Messages.sendMessage(sender, Paths.MESSAGES_COMMANDS_SPECTATE_JOIN_OTHER, "TARGET", spectator.getName());
			return true;
		}

		if(args.length == 0 || !hasPermission(player, COMMAND_SPECTATE_OTHERS)) {
			if(!hasPermission(player, COMMAND_SPECTATE_GENERAL)) {
				Messages.sendMessage(player, Paths.MESSAGE_DEFAULT_PERMISSION);
				return true;
			}

			if(spectateAPI.isSpectator(player)) {
				spectateAPI.getSpectateGeneral().unspectate(player, true);
				Messages.sendMessage(player, Paths.MESSAGES_COMMANDS_SPECTATE_LEAVE_OWN);
				return true;
			}
			spectateAPI.getSpectateGeneral().spectate(player, null);
			Messages.sendMessage(player, Paths.MESSAGES_COMMANDS_SPECTATE_JOIN_OWN);
			return true;
		}

		if(hasPermission(player, UTILS_HIDE_ARMORSTAND) && args[0].equalsIgnoreCase("-armorstand")) {
			if(!Config.getBoolean(Paths.CONFIG_HIDE_ARMOR_STANDS))
				return true;

			if(!spectateAPI.isSpectator(player)) {
				Messages.sendMessage(player, Paths.MESSAGES_GENERAL_NOTSPECTATOR);
				return true;
			}
			SpectateInformation info = spectateAPI.getSpectateInfo(player);
			info.setHideArmorStands(!info.isHideArmorStands());

			if(info.isHideArmorStands()) {
				spectateAPI.hideArmorstands(player);
				Messages.sendMessage(player, Paths.MESSAGES_COMMANDS_SPECTATE_ARMORSTANDS_OFF);
				return true;
			}
			spectateAPI.showArmorstands(player);
			Messages.sendMessage(player, Paths.MESSAGES_COMMANDS_SPECTATE_ARMORSTANDS_ON);
			return true;
		}

		Player target = Bukkit.getPlayer(args[0]);
		if(target == null || !target.isOnline()) {
			Messages.sendMessage(player, Paths.MESSAGES_GENERAL_OFFLINEPLAYER, "TARGET", args[0]);
			return true;
		}

		if(target.getUniqueId().equals(player.getUniqueId())) {
			Messages.sendMessage(player, Paths.MESSAGES_GENERAL_YOURSELF);
			return true;
		}

		if(spectateAPI.isSpectating(player, target)) {
			Messages.sendMessage(player, Paths.MESSAGES_GENERAL_SAMEPLAYER, "TARGET", target.getName());
			return true;
		}

		if(spectateAPI.isSpectating(target, player) || hasPermission(target, BYPASS_SPECTATED)) {
			if(!hasPermission(player, BYPASS_SPECTATEALL)) {
				Messages.sendMessage(player, Paths.MESSAGES_GENERAL_BYPASS_TELEPORT, "TARGET", target.getName());
				return true;
			}
		}

		if(plugin.getMultiverseCore() != null) {
			if(!player.getWorld().getUID().equals(target.getWorld().getUID())) {
				String world = plugin.getMultiverseCore().getMVWorldManager().getMVWorld(target.getWorld()).getPermissibleName();
				if(!player.hasPermission("multiverse.access." + world)) {
					Messages.sendMessage(player, Paths.MESSAGES_COMMANDS_SPECTATE_MULTIVERSE, "TARGET", target.getName());
					return true;
				}
			}
		}
		spectateAPI.getSpectateGeneral().spectate(player, target);
		Messages.sendMessage(player, Paths.MESSAGES_COMMANDS_SPECTATE_PLAYER, "TARGET", target.getName());
		return true;
	}

	@Override
	public List<String> onTabComplete(@Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String s, @Nonnull String[] args) {
		if(args.length == 1) {
			if(args[0].startsWith("-") && sender.hasPermission(UTILS_HIDE_ARMORSTAND))
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