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

package com.ninjaguild.dragoneggdrop.utils.versions.v1_12;

import java.lang.reflect.Field;
import java.util.Arrays;

import com.ninjaguild.dragoneggdrop.utils.versions.DragonBattle;
import com.ninjaguild.dragoneggdrop.utils.versions.NMSAbstract;

import org.apache.commons.lang3.EnumUtils;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Chest;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_12_R1.block.CraftChest;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftEnderDragon;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Player;

import net.minecraft.server.v1_12_R1.BossBattle;
import net.minecraft.server.v1_12_R1.BossBattleServer;
import net.minecraft.server.v1_12_R1.ChatMessageType;
import net.minecraft.server.v1_12_R1.EnderDragonBattle;
import net.minecraft.server.v1_12_R1.EntityEnderDragon;
import net.minecraft.server.v1_12_R1.IChatBaseComponent.ChatSerializer;
import net.minecraft.server.v1_12_R1.PacketPlayOutBoss;
import net.minecraft.server.v1_12_R1.PacketPlayOutChat;
import net.minecraft.server.v1_12_R1.WorldProvider;
import net.minecraft.server.v1_12_R1.WorldProviderTheEnd;

/**
 * An abstract implementation of necessary net.minecraft.server and
 * org.bukkit.craftbukkit methods that vary between versions causing
 * version dependencies. Allows for version independency through
 * abstraction per Bukkit/Spigot release
 * <p>
 * <b><i>Supported Minecraft Versions:</i></b> 1.12.0
 * 
 * @author Parker Hawke - 2008Choco
 */
public class NMSAbstract1_12_R1 implements NMSAbstract {
	
	@Override
	public void setDragonBossBarTitle(String title, DragonBattle battle) {
		battle.setBossBarTitle(title);
	}

	@Override
	public DragonBattle getEnderDragonBattleFromWorld(World world) {
		if (world == null) return null;
		
		CraftWorld craftWorld = (CraftWorld) world;
		WorldProvider worldProvider = craftWorld.getHandle().worldProvider;
		
		if (!(worldProvider instanceof WorldProviderTheEnd)) return null;
		return new DragonBattle1_12_R1(((WorldProviderTheEnd) worldProvider).t());
	}

	@Override
	public DragonBattle getEnderDragonBattleFromDragon(EnderDragon dragon) {
		if (dragon == null) return null;
		
		EntityEnderDragon nmsDragon = ((CraftEnderDragon) dragon).getHandle();
		return new DragonBattle1_12_R1(nmsDragon.df());
	}
	
	@Override
	public boolean setBattleBossBarStyle(Object battle, BarStyle style, BarColor colour) {
		if ((battle == null || !(battle instanceof EnderDragonBattle))) return false;
		
		EnderDragonBattle dragonBattle = (EnderDragonBattle) battle;
		try {
			Field field = EnderDragonBattle.class.getDeclaredField("c");
			field.setAccessible(true);
			
			BossBattleServer battleServer = (BossBattleServer) field.get(dragonBattle);
			if (battleServer == null) return false;
			
			if (style != null) {
				String nmsStyle = style.name().contains("SEGMENTED") ? style.name().replace("SEGMENTED", "NOTCHED") : "SOLID";
				if (!EnumUtils.isValidEnum(BossBattle.BarStyle.class, nmsStyle)) {
					return false;
				}
				
				battleServer.style = BossBattle.BarStyle.valueOf(nmsStyle);
			}
			if (colour != null) battleServer.color = BossBattle.BarColor.valueOf(colour.name());
			battleServer.sendUpdate(PacketPlayOutBoss.Action.UPDATE_STYLE);
			
			field.setAccessible(false);
		} catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	@Override
	public EnderDragon getEnderDragonFromBattle(DragonBattle battle) {
		return battle.getEnderDragon();
	}

	@Override
	public void respawnEnderDragon(DragonBattle battle) {
		battle.respawnEnderDragon();
	}

	@Override
	public boolean hasBeenPreviouslyKilled(EnderDragon dragon) {
		if (dragon == null) return false;
		
		EnderDragonBattle battle = (EnderDragonBattle) this.getEnderDragonBattleFromDragon(dragon);
		return battle.d();
	}
	
	@Override
	public int getEnderDragonDeathAnimationTime(EnderDragon dragon) {
		if (dragon == null) return -1;
		
		EntityEnderDragon nmsDragon = ((CraftEnderDragon) dragon).getHandle();
		return nmsDragon.bH;
	}
	
	@Override
	public Location getEndPortalLocation(DragonBattle battle) {
		return battle.getEndPortalLocation();
	}

	@Override
	public void setChestName(Chest chest, String name) {
		if (chest == null || name == null) return;
		
		CraftChest craftChest = (CraftChest) chest;
		craftChest.getTileEntity().setCustomName(name);
	}

	@Override
	public void sendActionBar(String message, Player... players) {
		PacketPlayOutChat packet = new PacketPlayOutChat(ChatSerializer.a("{\"text\":\"" + message + "\"}"), ChatMessageType.GAME_INFO);
		Arrays.stream(players).forEach(p -> ((CraftPlayer) p).getHandle().playerConnection.sendPacket(packet));
	}

	@Override
	public void broadcastActionBar(String message, World world) {
		PacketPlayOutChat packet = new PacketPlayOutChat(ChatSerializer.a("{\"text\":\"" + message + "\"}"), ChatMessageType.GAME_INFO);
		world.getPlayers().forEach(p -> ((CraftPlayer) p).getHandle().playerConnection.sendPacket(packet));
	}
}