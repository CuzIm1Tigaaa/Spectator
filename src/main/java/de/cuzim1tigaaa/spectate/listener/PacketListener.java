package de.cuzim1tigaaa.spectate.listener;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers;
import de.cuzim1tigaaa.spectate.Main;
import de.cuzim1tigaaa.spectate.files.Config;
import de.cuzim1tigaaa.spectate.files.Permissions;
import de.cuzim1tigaaa.spectate.player.Inventory;
import org.bukkit.GameMode;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

public class PacketListener {

    private PacketListener() {
    }

    public static void register() {
        ProtocolManager manager = ProtocolLibrary.getProtocolManager();
        manager.addPacketListener(new PacketAdapter(PacketAdapter.params()
                .plugin(Main.getInstance())
                .types(PacketType.Play.Client.USE_ENTITY)
                .optionAsync()) {
            @Override
            public void onPacketReceiving(PacketEvent event) {
                Player player = event.getPlayer();
                if(player.getGameMode().equals(GameMode.SPECTATOR)) {
                    if(Main.getInstance().getSpectators().contains(player)) {
                        EnumWrappers.EntityUseAction action = event.getPacket().getEntityUseActions().read(0);
                        if(action.equals(EnumWrappers.EntityUseAction.ATTACK)) {
                            Entity entity = event.getPacket().getEntityModifier(player.getWorld()).read(0);
                            if(entity.getType().equals(EntityType.PLAYER)) {
                                Player target = (Player) entity;
                                if(target.hasPermission(Permissions.CANNOT)) {
                                    player.sendMessage(Config.getMessage("Config.Player.cannot", "player", target.getName()));
                                    event.setCancelled(true);
                                }else {
                                    if(player.hasPermission(Permissions.INVENTORY) && Config.mirrorInventory) {
                                        Inventory.getInventory(player, target);
                                    }
                                    Main.getInstance().getRelation().put(player, target);
                                }
                            }
                        }
                    }
                }
            }
        });
    }

}
