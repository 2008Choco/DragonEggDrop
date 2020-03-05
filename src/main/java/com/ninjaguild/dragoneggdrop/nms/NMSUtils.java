package com.ninjaguild.dragoneggdrop.nms;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import net.minecraft.server.v1_15_R1.ChatMessageType;
import net.minecraft.server.v1_15_R1.EnderDragonBattle;
import net.minecraft.server.v1_15_R1.EntityEnderDragon;
import net.minecraft.server.v1_15_R1.IChatBaseComponent.ChatSerializer;
import net.minecraft.server.v1_15_R1.PacketPlayOutChat;
import net.minecraft.server.v1_15_R1.WorldProvider;
import net.minecraft.server.v1_15_R1.WorldProviderTheEnd;

import org.bukkit.World;
import org.bukkit.craftbukkit.v1_15_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftEnderDragon;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftPlayer;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Player;

public final class NMSUtils {

	private NMSUtils() { }

	public static DragonBattle getEnderDragonBattleFromWorld(World world) {
		if (world == null) return null;

		CraftWorld craftWorld = (CraftWorld) world;
		WorldProvider worldProvider = craftWorld.getHandle().worldProvider;

		if (!(worldProvider instanceof WorldProviderTheEnd)) return null;
		return new DragonBattle(((WorldProviderTheEnd) worldProvider).o());
	}

	public static DragonBattle getEnderDragonBattleFromDragon(EnderDragon dragon) {
		if (dragon == null) return null;

		EntityEnderDragon nmsDragon = ((CraftEnderDragon) dragon).getHandle();
		return new DragonBattle(nmsDragon.getEnderDragonBattle());
	}

	public static boolean hasBeenPreviouslyKilled(EnderDragon dragon) {
		if (dragon == null) return false;

		EnderDragonBattle battle = getEnderDragonBattleFromDragon(dragon).getHandle();
		return battle.d();
	}

	public static int getEnderDragonDeathAnimationTime(EnderDragon dragon) {
		if (dragon == null) return -1;

		EntityEnderDragon nmsDragon = ((CraftEnderDragon) dragon).getHandle();
		return nmsDragon.bA;
	}

	public static void sendActionBar(String message, Player... players) {
		if (players.length == 0) return;

		PacketPlayOutChat packet = new PacketPlayOutChat(ChatSerializer.a("{\"text\":\"" + message + "\"}"), ChatMessageType.GAME_INFO);
		Arrays.stream(players).filter(Objects::nonNull).forEach(p -> ((CraftPlayer) p).getHandle().playerConnection.sendPacket(packet));
	}

	public static void broadcastActionBar(String message, World world) {
		if (world == null) return;

		List<Player> players = world.getPlayers();
		if (players.isEmpty()) return;

		PacketPlayOutChat packet = new PacketPlayOutChat(ChatSerializer.a("{\"text\":\"" + message + "\"}"), ChatMessageType.GAME_INFO);
		players.forEach(p -> ((CraftPlayer) p).getHandle().playerConnection.sendPacket(packet));
	}

}