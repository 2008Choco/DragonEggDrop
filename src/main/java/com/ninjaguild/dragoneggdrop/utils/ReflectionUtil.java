package com.ninjaguild.dragoneggdrop.utils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import com.google.common.base.Preconditions;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

public final class ReflectionUtil {

    private static Field fieldCraftPlayerPlayerConnection;

    private static Constructor<?> constructorPacketPlayOutChat;

    private static Method methodCraftPlayerGetHandle;
    private static Method methodChatSerializerFromString;
    private static Method methodPlayerConnectionSendPacket;

    private static Object enumConstantChatMessageTypeGameInfo;

    private static String version;

	private ReflectionUtil() { }

	// Here's hoping Bukkit makes proper API for this in the near future
	public static void sendActionBar(String message, Player player) {
	    Preconditions.checkArgument(message != null, "Message must not be null");
	    Preconditions.checkArgument(player != null, "Player must not be null");

	    try {
	        // Get the PlayerConnection instance to send a packet
	        Object entityPlayer = methodCraftPlayerGetHandle.invoke(player);
	        Object playerConnection = fieldCraftPlayerPlayerConnection.get(entityPlayer);

	        // Create the packet
	        Object messageComponent = methodChatSerializerFromString.invoke(null, "{\"text\":\"" + message + "\"}");
	        Object packetPlayOutChat = constructorPacketPlayOutChat.newInstance(messageComponent, enumConstantChatMessageTypeGameInfo);

	        // Send the packet
	        methodPlayerConnectionSendPacket.invoke(playerConnection, packetPlayOutChat);
	    } catch (ReflectiveOperationException e) {
	        e.printStackTrace();
	    }
	}

	public static void broadcastActionBar(String message, World world) {
	    Preconditions.checkArgument(world != null, "World must not be null");
		world.getPlayers().forEach(p -> ReflectionUtil.sendActionBar(message, p));
	}

    public static void broadcastActionBar(String message, Location location, int radiusSquared) {
        if (location == null || location.getWorld() == null || radiusSquared < 0) {
            return;
        }

        List<Player> players = location.getWorld().getPlayers();
        if (players.isEmpty()) {
            return;
        }

        for (Player player : players) {
            if (player.getLocation().distanceSquared(location) > radiusSquared) {
                continue;
            }

            ReflectionUtil.sendActionBar(message, player);
        }

    }

	public static void init(String version) {
	    if (ReflectionUtil.version != null) {
	        return;
	    }

	    ReflectionUtil.version = version;

	    Class<?> classPacket = getNMSClass("Packet");
	    Class<?> classCraftPlayer = getCBClass("entity.CraftPlayer");
	    Class<?> classEntityPlayer = getNMSClass("EntityPlayer");
	    Class<?> classChatMessageType = getNMSClass("ChatMessageType");
	    Class<?> classPlayerConnection = getNMSClass("PlayerConnection");
	    Class<?> classPacketPlayOutChat = getNMSClass("PacketPlayOutChat");
        Class<?> classIChatBaseComponent = getNMSClass("IChatBaseComponent");
        Class<?> classChatSerializer = getNMSClass("IChatBaseComponent$ChatSerializer");

	    fieldCraftPlayerPlayerConnection = getField(classEntityPlayer, "playerConnection");

	    constructorPacketPlayOutChat = getConstructor(classPacketPlayOutChat, classIChatBaseComponent, classChatMessageType);

	    methodCraftPlayerGetHandle = getMethod(classCraftPlayer, "getHandle");
	    methodChatSerializerFromString = getMethod(classChatSerializer, "a", String.class);
	    methodPlayerConnectionSendPacket = getMethod(classPlayerConnection, "sendPacket", classPacket);

	    enumConstantChatMessageTypeGameInfo = getEnumConstant(classChatMessageType, "GAME_INFO");
	}

    private static Method getMethod(Class<?> clazz, String name, Class<?>... paramTypes) {
        try {
            return clazz.getMethod(name, paramTypes);
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }

        return null;
    }

    private static Field getField(Class<?> clazz, String name) {
        try {
            Field field = clazz.getDeclaredField(name);
            field.setAccessible(true);
            return field;
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }

        return null;
    }

    private static Constructor<?> getConstructor(Class<?> clazz, Class<?>... parameters) {
        try {
            return clazz.getConstructor(parameters);
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }

        return null;
    }

    private static Class<?> getNMSClass(String className) {
        try {
            return Class.forName("net.minecraft.server." + version + "." + className);
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }

        return null;
    }

    private static Class<?> getCBClass(String className) {
        try {
            return Class.forName("org.bukkit.craftbukkit." + version + "." + className);
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }

        return null;
    }

    private static Object getEnumConstant(Class<?> clazz, String name) {
        try {
            Field field = getField(clazz, name);
            return (field != null) ? field.get(null) : null;
        } catch (IllegalArgumentException | ReflectiveOperationException e) {
            e.printStackTrace();
        }

        return null;
    }

}