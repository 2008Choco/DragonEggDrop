package com.ninjaguild.dragoneggdrop;

import java.util.Collection;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_10_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

import net.minecraft.server.v1_10_R1.IChatBaseComponent.ChatSerializer;
import net.minecraft.server.v1_10_R1.PacketPlayOutChat;

public final class ActionBar {

	private ActionBar() {}
	
    public static void sendToPlayer(Player player, String message) {
    	PacketPlayOutChat packet = new PacketPlayOutChat(ChatSerializer.a("{\"text\":\"" + message + "\"}"), (byte)2);
    	((CraftPlayer)player).getHandle().playerConnection.sendPacket(packet);
    }
    
    public static void sendToSome(Collection<Player> players, String message) {
    	PacketPlayOutChat packet = new PacketPlayOutChat(ChatSerializer.a("{\"text\":\"" + message + "\"}"), (byte)2);
    	for (Player player : players) {
    		((CraftPlayer)player).getHandle().playerConnection.sendPacket(packet);
    	}
    }
    
    public static void sendToAll(String message) {
    	PacketPlayOutChat packet = new PacketPlayOutChat(ChatSerializer.a("{\"text\":\"" + message + "\"}"), (byte)2);
    	for (Player player : Bukkit.getOnlinePlayers()) {
    		((CraftPlayer)player).getHandle().playerConnection.sendPacket(packet);
    	}
    }

}
