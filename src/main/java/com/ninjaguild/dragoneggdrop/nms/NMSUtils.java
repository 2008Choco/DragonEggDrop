package com.ninjaguild.dragoneggdrop.nms;

import java.util.Arrays;

import net.minecraft.server.v1_13_R2.ChatMessageType;
import net.minecraft.server.v1_13_R2.EnderDragonBattle;
import net.minecraft.server.v1_13_R2.EntityEnderDragon;
import net.minecraft.server.v1_13_R2.IChatBaseComponent.ChatSerializer;
import net.minecraft.server.v1_13_R2.PacketPlayOutChat;
import net.minecraft.server.v1_13_R2.WorldProvider;
import net.minecraft.server.v1_13_R2.WorldProviderTheEnd;

import org.bukkit.World;
import org.bukkit.craftbukkit.v1_13_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_13_R2.entity.CraftEnderDragon;
import org.bukkit.craftbukkit.v1_13_R2.entity.CraftPlayer;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Player;

public final class NMSUtils {

	private NMSUtils() { }

	public static DragonBattle getEnderDragonBattleFromWorld(World world) {
		if (world == null) return null;

		CraftWorld craftWorld = (CraftWorld) world;
		WorldProvider worldProvider = craftWorld.getHandle().worldProvider;

		if (!(worldProvider instanceof WorldProviderTheEnd)) return null;
		return new DragonBattle(((WorldProviderTheEnd) worldProvider).r());
	}

	public static DragonBattle getEnderDragonBattleFromDragon(EnderDragon dragon) {
		if (dragon == null) return null;

		EntityEnderDragon nmsDragon = ((CraftEnderDragon) dragon).getHandle();
		return new DragonBattle(nmsDragon.ds());
	}

	public static boolean hasBeenPreviouslyKilled(EnderDragon dragon) {
		if (dragon == null) return false;

		EnderDragonBattle battle = getEnderDragonBattleFromDragon(dragon).getHandle();
		return battle.d();
	}

	public static int getEnderDragonDeathAnimationTime(EnderDragon dragon) {
		if (dragon == null) return -1;

		EntityEnderDragon nmsDragon = ((CraftEnderDragon) dragon).getHandle();
		return nmsDragon.bO;
	}

	public static void sendActionBar(String message, Player... players) {
		PacketPlayOutChat packet = new PacketPlayOutChat(ChatSerializer.a("{\"text\":\"" + message + "\"}"), ChatMessageType.GAME_INFO);
		Arrays.stream(players).forEach(p -> ((CraftPlayer) p).getHandle().playerConnection.sendPacket(packet));
	}

	public static void broadcastActionBar(String message, World world) {
		PacketPlayOutChat packet = new PacketPlayOutChat(ChatSerializer.a("{\"text\":\"" + message + "\"}"), ChatMessageType.GAME_INFO);
		world.getPlayers().forEach(p -> ((CraftPlayer) p).getHandle().playerConnection.sendPacket(packet));
	}

}