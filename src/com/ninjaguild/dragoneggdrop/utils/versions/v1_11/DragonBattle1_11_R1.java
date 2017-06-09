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
import java.util.UUID;

import com.ninjaguild.dragoneggdrop.utils.versions.DragonBattle;

import org.apache.commons.lang3.EnumUtils;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.entity.EnderDragon;

import net.minecraft.server.v1_11_R1.BlockPosition;
import net.minecraft.server.v1_11_R1.BossBattle;
import net.minecraft.server.v1_11_R1.BossBattleServer;
import net.minecraft.server.v1_11_R1.ChatMessage;
import net.minecraft.server.v1_11_R1.EnderDragonBattle;
import net.minecraft.server.v1_11_R1.Entity;
import net.minecraft.server.v1_11_R1.PacketPlayOutBoss;
import net.minecraft.server.v1_11_R1.WorldServer;

/**
 * An abstract implementation for EnderDragonBattle of necessary 
 * net.minecraft.server and org.bukkit.craftbukkit methods that 
 * vary between versions causing version dependencies. Allows for
 * version independency through abstraction per Bukkit/Spigot release
 * <p>
 * <b><i>Supported Minecraft Versions:</i></b> 1.11.0, 1.11.1 and 1.11.2
 * 
 * @author Parker Hawke - 2008Choco
 */
public class DragonBattle1_11_R1 implements DragonBattle {
	
	private final EnderDragonBattle battle;
	
	protected DragonBattle1_11_R1(EnderDragonBattle battle) {
		this.battle = battle;
	}

	@Override
	public void setBossBarTitle(String title) {
		if (title == null) return;
		
		try {
			Field fieldBossBattleServer = EnderDragonBattle.class.getDeclaredField("c");
			fieldBossBattleServer.setAccessible(true);
			
			BossBattleServer battleServer = (BossBattleServer) fieldBossBattleServer.get(battle);
			if (battleServer == null) return;
			battleServer.title = new ChatMessage(title);
			battleServer.sendUpdate(PacketPlayOutBoss.Action.UPDATE_NAME);
			
			fieldBossBattleServer.setAccessible(false);
		} catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean setBossBarStyle(BarStyle style, BarColor colour) {
		if (style == null || colour == null) return false;
		
		try {
			Field fieldBossBattleServer = EnderDragonBattle.class.getDeclaredField("c");
			fieldBossBattleServer.setAccessible(true);
			
			BossBattleServer battleServer = (BossBattleServer) fieldBossBattleServer.get(battle);
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
			
			fieldBossBattleServer.setAccessible(false);
		} catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
			return false;
		}
		
		return true;
	}

	@Override
	public EnderDragon getEnderDragon() {
		EnderDragon dragon = null;
		
		try {
			Field fieldWorldServer = EnderDragonBattle.class.getDeclaredField("d");
			Field fieldDragonUUID = EnderDragonBattle.class.getDeclaredField("m");
			fieldWorldServer.setAccessible(true);
			fieldDragonUUID.setAccessible(true);
			
			WorldServer world = (WorldServer) fieldWorldServer.get(battle);
			UUID dragonUUID = (UUID) fieldDragonUUID.get(battle);
			
			if (world == null || dragonUUID == null) 
				return null;
			
			Entity dragonEntity = world.getEntity(dragonUUID);
			if (dragonEntity == null) return null;
			dragon = (EnderDragon) dragonEntity.getBukkitEntity();
			
			fieldWorldServer.setAccessible(false);
			fieldDragonUUID.setAccessible(false);
		} catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
		
		return dragon;
	}

	@Override
	public void respawnEnderDragon() {
		this.battle.e();
	}

	@Override
	public Location getEndPortalLocation() {
		Location portalLocation = null;
		
		try {
			Field fieldExitPortalLocation = EnderDragonBattle.class.getDeclaredField("o");
			Field fieldWorldServer = EnderDragonBattle.class.getDeclaredField("d");
			fieldExitPortalLocation.setAccessible(true);
			fieldWorldServer.setAccessible(true);
			
			WorldServer worldServer = (WorldServer) fieldWorldServer.get(battle);
			BlockPosition position = (BlockPosition) fieldExitPortalLocation.get(battle);
			if (worldServer != null && position != null) {
				World world = worldServer.getWorld();
				portalLocation = new Location(world, Math.floor(position.getX()) + 0.5, position.getY() + 4, Math.floor(position.getZ()) + 0.5);
			}
			
			fieldWorldServer.setAccessible(false);
			fieldExitPortalLocation.setAccessible(false);
		} catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
		
		return portalLocation;
	}
	
	/**
	 * Get the net.minecraft.server implementation of DragonBattle
	 * 
	 * @return the wrapped battle
	 */
	public EnderDragonBattle getHandle() {
		return this.battle;
	}

}