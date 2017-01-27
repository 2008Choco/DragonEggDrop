/*
    DragonEggDrop
    Copyright (C) 2016  NinjaStix
    ninjastix84@gmail.com

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.ninjaguild.dragoneggdrop.utils;

import java.util.Collection;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_11_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

import net.minecraft.server.v1_11_R1.IChatBaseComponent.ChatSerializer;
import net.minecraft.server.v1_11_R1.PacketPlayOutChat;

// This will not yet be documented. I may or may not include this class in the final product
public final class ActionBar {

	private ActionBar() {}
	
	// FIXME: NMS-Dependent
    public static void sendToPlayer(Player player, String message) {
    	PacketPlayOutChat packet = new PacketPlayOutChat(ChatSerializer.a("{\"text\":\"" + message + "\"}"), (byte)2);
    	((CraftPlayer)player).getHandle().playerConnection.sendPacket(packet);
    }
    
    // FIXME: NMS-Dependent
    public static void sendToSome(Collection<Player> players, String message) {
    	PacketPlayOutChat packet = new PacketPlayOutChat(ChatSerializer.a("{\"text\":\"" + message + "\"}"), (byte)2);
    	for (Player player : players) {
    		((CraftPlayer)player).getHandle().playerConnection.sendPacket(packet);
    	}
    }
    
    // FIXME: NMS-Dependent
    public static void sendToAll(String message) {
    	PacketPlayOutChat packet = new PacketPlayOutChat(ChatSerializer.a("{\"text\":\"" + message + "\"}"), (byte)2);
    	for (Player player : Bukkit.getOnlinePlayers()) {
    		((CraftPlayer)player).getHandle().playerConnection.sendPacket(packet);
    	}
    }
    
}