package de.cuzim1tigaaa.spectator.commands;

import de.cuzim1tigaaa.spectator.Spectator;
import de.cuzim1tigaaa.spectator.spectate.SpectateUtils;
import org.bukkit.command.*;
import org.bukkit.generator.WorldInfo;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import java.util.*;

public class SpectateInfo implements CommandExecutor, TabCompleter {

    private final SpectateUtils spectateUtils;

    public SpectateInfo(Spectator plugin) {
        Objects.requireNonNull(plugin.getCommand("spectateinfo")).setExecutor(this);
        this.spectateUtils = plugin.getSpectateUtils();
    }

    @Override
    public boolean onCommand(@Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String label, @Nonnull String[] args) {
        if(args.length > 0) {
            switch(args[0].toLowerCase()) {
                case "list" -> spectateUtils.getSpectateInfo().forEach((uuid, info) -> {
                    final String msg = " > " + info.getSpectator().getName() +
                            " " +
                            info.getState().name() +
                            " " +
                            "Target: " + (info.getTarget() == null ? "NONE" : info.getTarget().getName()) +
                            "\n" + String.join(", ", info.getAttributes().keySet().stream().map(WorldInfo::getName).toList());
                    sender.sendMessage(msg);
                });
                case "inv" -> spectateUtils.getSpectateInfo().forEach((uuid, info) -> info.getAttributes().forEach((w, attr) -> {
                    sender.sendMessage(w.getName() + ":");
                    StringBuilder msg = new StringBuilder("\t");
                    for(ItemStack item : attr.getInventory()) {
                        if(item == null) continue;
                        msg.append(item.getType().name()).append(" [").append(item.getAmount()).append("] ;");
                    }
                    sender.sendMessage(msg.toString());
                }));
            }
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(@Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String s, @Nonnull String[] args) {
        return Collections.emptyList();
    }
}