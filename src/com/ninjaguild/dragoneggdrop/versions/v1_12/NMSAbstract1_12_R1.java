package com.ninjaguild.dragoneggdrop.versions.v1_12;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

import com.ninjaguild.dragoneggdrop.versions.DragonBattle;
import com.ninjaguild.dragoneggdrop.versions.NMSAbstract;

import org.bukkit.World;
import org.bukkit.block.Chest;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_12_R1.block.CraftBlockEntityState;
import org.bukkit.craftbukkit.v1_12_R1.block.CraftChest;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftEnderDragon;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Player;

import net.minecraft.server.v1_12_R1.ChatMessageType;
import net.minecraft.server.v1_12_R1.EnderDragonBattle;
import net.minecraft.server.v1_12_R1.EntityEnderDragon;
import net.minecraft.server.v1_12_R1.IChatBaseComponent.ChatSerializer;
import net.minecraft.server.v1_12_R1.PacketPlayOutChat;
import net.minecraft.server.v1_12_R1.TileEntityChest;
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
	public boolean hasBeenPreviouslyKilled(EnderDragon dragon) {
		if (dragon == null) return false;
		
		EnderDragonBattle battle = ((DragonBattle1_12_R1) this.getEnderDragonBattleFromDragon(dragon)).getHandle();
		return battle.d();
	}
	
	@Override
	public int getEnderDragonDeathAnimationTime(EnderDragon dragon) {
		if (dragon == null) return -1;
		
		EntityEnderDragon nmsDragon = ((CraftEnderDragon) dragon).getHandle();
		return nmsDragon.bH;
	}

	@Override
	public void setChestName(Chest chest, String name) {
		if (chest == null || name == null) return;
		
		CraftChest craftChest = (CraftChest) chest;
		
		// CraftChest#getTileEntity() was moved up to CraftBlockEntityState#getTileEntity() and made protected
		// TODO Remove unnecessary checks when implementing this code for 1.13
		try {
			boolean entityStateExists = false;
			try {
				Class.forName("org.bukkit.craftbukkit.v1_12_R1.block.CraftBlockEntityState");
				entityStateExists = true;
			} catch (ClassNotFoundException e) { /* ignore */ }
			
			Method methodGetTileEntity;
			
			if (entityStateExists) {
				methodGetTileEntity = CraftBlockEntityState.class.getDeclaredMethod("getTileEntity");
			}
			else {
				methodGetTileEntity = CraftChest.class.getMethod("getTileEntity");
			}
			
			methodGetTileEntity.setAccessible(true);
			
			TileEntityChest chestEntity = (TileEntityChest) methodGetTileEntity.invoke(craftChest);
			chestEntity.setCustomName(name);
			
			methodGetTileEntity.setAccessible(false);
		} catch (NoSuchMethodException | IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
			e.printStackTrace();
		}
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