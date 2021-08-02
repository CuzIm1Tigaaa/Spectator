package de.cuzim1tigaaa.spectator.listener;

import com.comphenix.protocol.*;
import com.comphenix.protocol.events.*;
import com.comphenix.protocol.wrappers.EnumWrappers;
import de.cuzim1tigaaa.spectator.Spectator;
import de.cuzim1tigaaa.spectator.files.*;
import de.cuzim1tigaaa.spectator.player.Inventory;
import org.bukkit.GameMode;
import org.bukkit.entity.*;

public class PacketListener {

    private final Spectator instance;

    public PacketListener(Spectator plugin) {
        this.instance = plugin;
        this.register();
    }

    public void register() {
        ProtocolManager manager = ProtocolLibrary.getProtocolManager();
        manager.addPacketListener(new PacketAdapter(PacketAdapter.params().plugin(instance).types(PacketType.Play.Client.USE_ENTITY).optionAsync()) {
            @Override
            public void onPacketReceiving(PacketEvent event) {
                Player player = event.getPlayer();
                PacketContainer packet = event.getPacket();

                if(!player.getGameMode().equals(GameMode.SPECTATOR)) return;
                if(!instance.getSpectators().contains(player)) return;

                EnumWrappers.EntityUseAction action = packet.getEntityUseActions().read(0);
                if(!action.equals(EnumWrappers.EntityUseAction.ATTACK)) return;

                Entity entity = packet.getEntityModifier(player.getWorld()).read(0);
                if(!entity.getType().equals(EntityType.PLAYER)) return;

                Player target = (Player) entity;
                if(target.hasPermission(Permissions.BYPASS_SPECTATED) || !player.hasPermission(Permissions.COMMAND_SPECTATE_OTHERS)) {
                    if(!player.hasPermission(Permissions.BYPASS_SPECTATEALL)) {
                        player.sendMessage(Messages.getMessage(Paths.MESSAGES_GENERAL_BYPASS, "TARGET", target.getName()));
                        event.setCancelled(true);
                    }
                }else {
                    Inventory.getInventory(player, target);
                    instance.getRelation().put(player, target);
                }
            }
        });
    }
}