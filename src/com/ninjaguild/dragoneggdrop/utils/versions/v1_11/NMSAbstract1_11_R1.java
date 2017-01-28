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

package com.ninjaguild.dragoneggdrop.utils.versions.v1_11;

import java.lang.reflect.Field;
import java.util.Arrays;

import com.ninjaguild.dragoneggdrop.utils.versions.NMSAbstract;

import org.bukkit.World;
import org.bukkit.block.Chest;
import org.bukkit.craftbukkit.v1_11_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_11_R1.block.CraftChest;
import org.bukkit.craftbukkit.v1_11_R1.entity.CraftEnderDragon;
import org.bukkit.craftbukkit.v1_11_R1.entity.CraftPlayer;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Player;

import net.minecraft.server.v1_11_R1.BossBattleServer;
import net.minecraft.server.v1_11_R1.ChatMessage;
import net.minecraft.server.v1_11_R1.EnderDragonBattle;
import net.minecraft.server.v1_11_R1.EntityEnderDragon;
import net.minecraft.server.v1_11_R1.IChatBaseComponent.ChatSerializer;
import net.minecraft.server.v1_11_R1.PacketPlayOutBoss;
import net.minecraft.server.v1_11_R1.PacketPlayOutChat;
import net.minecraft.server.v1_11_R1.WorldProvider;
import net.minecraft.server.v1_11_R1.WorldProviderTheEnd;

/**
 * An abstract implementation of necessary net.minecraft.server and
 * org.bukkit.craftbukkit methods that vary between versions causing
 * version dependencies. Allows for version independency through
 * abstraction per Bukkit/Spigot release
 * <p>
 * <b><i>Supported Minecraft Versions:</i></b> 1.11.0, 1.11.1 and 1.11.2
 * 
 * @author Parker Hawke - 2008Choco
 */
public class NMSAbstract1_11_R1 implements NMSAbstract {
	
	@Override
	public void setDragonBossBarTitle(String title, Object battle) {
		if (!(battle instanceof EnderDragonBattle)) return;
		
		EnderDragonBattle dragonBattle = (EnderDragonBattle) battle;
		try {
			Field field = EnderDragonBattle.class.getDeclaredField("c");
			field.setAccessible(true);
			BossBattleServer battleServer = (BossBattleServer) field.get(dragonBattle);
			battleServer.title = new ChatMessage(title);
			battleServer.sendUpdate(PacketPlayOutBoss.Action.UPDATE_NAME);
			field.setAccessible(false);
		} catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
	}

	@Override
	public Object getEnderDragonBattleFromWorld(World world) {
		CraftWorld craftWorld = (CraftWorld) world;
		WorldProvider worldProvider = craftWorld.getHandle().worldProvider;
		
		if (!(worldProvider instanceof WorldProviderTheEnd)) return null;
		return ((WorldProviderTheEnd) worldProvider).t();
	}

	@Override
	public Object getEnderDragonBattleFromDragon(EnderDragon dragon) {
		EntityEnderDragon nmsDragon = ((CraftEnderDragon) dragon).getHandle();
		return nmsDragon.db();
	}

	@Override
	public void respawnEnderDragon(Object battle) {
		if (!(battle instanceof EnderDragonBattle)) return;
		
		EnderDragonBattle dragonBattle = (EnderDragonBattle) battle;
		dragonBattle.e();
	}

	@Override
	public boolean hasBeenPreviouslyKilled(EnderDragon dragon) {
		EnderDragonBattle battle = (EnderDragonBattle) this.getEnderDragonBattleFromDragon(dragon);
		return battle.d();
	}
	
	@Override
	public int getEnderDragonDeathAnimationTime(EnderDragon dragon) {
		EntityEnderDragon nmsDragon = ((CraftEnderDragon) dragon).getHandle();
		return nmsDragon.bG;
	}

	@Override
	public void setChestName(Chest chest, String name) {
		CraftChest craftChest = (CraftChest) chest;
		craftChest.getTileEntity().a(name);
	}

	@Override
	public void sendActionBar(String message, Player... players) {
		PacketPlayOutChat packet = new PacketPlayOutChat(ChatSerializer.a("{\"text\":\"" + message + "\"}"), (byte) 2);
		Arrays.stream(players).forEach(p -> ((CraftPlayer) p).getHandle().playerConnection.sendPacket(packet));
	}

	@Override
	public void broadcastActionBar(String message, World world) {
		PacketPlayOutChat packet = new PacketPlayOutChat(ChatSerializer.a("{\"text\":\"" + message + "\"}"), (byte) 2);
		world.getPlayers().forEach(p -> ((CraftPlayer) p).getHandle().playerConnection.sendPacket(packet));
	}
}