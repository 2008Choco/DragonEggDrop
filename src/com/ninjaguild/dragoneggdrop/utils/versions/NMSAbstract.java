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

package com.ninjaguild.dragoneggdrop.utils.versions;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.bukkit.World;
import org.bukkit.block.Chest;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Player;

/**
 * An abstract implementation of necessary net.minecraft.server and
 * org.bukkit.craftbukkit methods that vary between versions causing
 * version dependencies. Allows for version independency through
 * abstraction per Bukkit/Spigot release
 * 
 * @author Parker Hawke - 2008Choco
 */
public interface NMSAbstract {
	
	/**
	 * Initialize all required classes, methods and fields required for
	 * this version implementation
	 */
	public default void init(String version){}
	
	/**
	 * Set the title of the boss bar in a given ender dragon battle to 
	 * a specific name
	 * 
	 * @param title - The title to set
	 * @param battle - The battle to modify
	 */
	public void setDragonBossBarTitle(String title, Object battle);
	
	/**
	 * Get an EnderDragonBattle object based on the given world
	 * 
	 * @param world - The world to retrieve a battle from
	 * @return the resulting dragon battle
	 */
	public Object getEnderDragonBattleFromWorld(World world);
	
	/**
	 * Get an EnderDragonBattle object based on a specific Ender Dragon
	 * 
	 * @param dragon - The dragon to retrieve a battle from
	 * @return the resulting dragon battle
	 */
	public Object getEnderDragonBattleFromDragon(EnderDragon dragon);
	
	/**
	 * Set the state of EnderDragonBattle to its respawn state, and
	 * restart the battle once again
	 * 
	 * @param dragonBattle - The battle to modify
	 */
	public void respawnEnderDragon(Object dragonBattle);
	
	/**
	 * Check whether the dragon has been previously killed or not
	 * 
	 * @param dragon - The dragon to check
	 * @return true if the dragon has been previously killed
	 */
	public boolean hasBeenPreviouslyKilled(EnderDragon dragon);
	
	/**
	 * Get the Ender Dragon's current death animation time
	 * 
	 * @param dragon - The dragon to check
	 * @return the animation time
	 */
	public int getEnderDragonDeathAnimationTime(EnderDragon dragon);
	
	/**
	 * Set the custom name of a chest tile entity
	 * 
	 * @param chest - The chest to set the name of
	 * @param name - The name to set the chest to
	 */
	public void setChestName(Chest chest, String name);
	
	/**
	 * Send an action bar to a list of given players
	 * 
	 * @param message - The message to send
	 * @param players - The players to send the message to
	 */
	public void sendActionBar(String message, Player... players);
	
	/**
	 * Send an action bar to all players in a given world
	 * 
	 * @param message - The message to send
	 * @param world - The world to broadcast the message to
	 */
	public void broadcastActionBar(String message, World world);
	
	public class ReflectionUtils {
		
		public static Class<?> classCraftWorld;
		public static Method methodCraftWorldGetHandle;
		
		public static Class<?> classCraftChest;
		public static Method methodGetTileEntity;
		
		public static Class<?> classTileEntityChest;
		
		public static Class<?> classCraftPlayer;
		public static Method methodCraftPlayerGetHandle;
		
		public static Class<?> classEntityPlayer;
		public static Field fieldPlayerConnection;
		
		public static Class<?> classIChatBaseComponent;
		public static Class<?> classChatSerializer;
		public static Method methodFromJSON;
		
		public static Class<?> classPacket;
		public static Class<?> classPacketPlayOutChat;
		public static Constructor<?> constructorPacketPlayOutChat;
		
		public static Class<?> classPlayerConnection;
		public static Method methodSendPacket;
		
		public static Class<?> classCraftEnderDragon;
		public static Method methodCraftEnderDragonGetHandle;
		
		public static Class<?> classEntityEnderDragon;
		public static Class<?> classWorldProviderTheEnd;
		public static Class<?> classBossBattleServer;
		public static Class<?> classEnderDragonBattle;
		public static Class<?> enumPacketPlayOutBoss$Action;
		public static Class<?> classChatMessage;
		
		private static String version;
		
		public static void init(String version) {
			if (version != null) return;
			
			ReflectionUtils.version = version;
			
			classCraftWorld = ReflectionUtils.getCBClass("world.CraftWorld");
			methodCraftWorldGetHandle = ReflectionUtils.getMethod("getHandle", classCraftWorld);
			
			classCraftChest = ReflectionUtils.getCBClass("block.CraftChest");
			methodGetTileEntity = ReflectionUtils.getMethod("getTileEntity", classCraftChest);
			
			classTileEntityChest = ReflectionUtils.getNMSClass("TileEntityChest");
			
			classCraftPlayer = ReflectionUtils.getCBClass("entity.CraftPlayer");
			methodCraftPlayerGetHandle = ReflectionUtils.getMethod("getHandle", classCraftPlayer);
			
			classEntityPlayer = ReflectionUtils.getNMSClass("EntityPlayer");
			fieldPlayerConnection = ReflectionUtils.getField("playerConnection", classEntityPlayer);
			
			classIChatBaseComponent = ReflectionUtils.getNMSClass("IChatBaseComponent");
			classChatSerializer = ReflectionUtils.getNMSClass("IChatBaseComponent$ChatSerializer");
			methodFromJSON = ReflectionUtils.getMethod("a", classChatSerializer, String.class);
			
			classPacket = ReflectionUtils.getNMSClass("Packet");
			classPacketPlayOutChat = ReflectionUtils.getNMSClass("PacketPlayOutChat");
			constructorPacketPlayOutChat = ReflectionUtils.getConstructor(classPacketPlayOutChat, classIChatBaseComponent, Byte.TYPE);
			
			classPlayerConnection = ReflectionUtils.getNMSClass("PlayerConnection");
			methodSendPacket = ReflectionUtils.getMethod("sendPacket", classPacket);
			
			classCraftEnderDragon = ReflectionUtils.getCBClass("entity.CraftEnderDragon");
			methodCraftEnderDragonGetHandle = ReflectionUtils.getMethod("getHandle", classCraftEnderDragon);
			
			classEntityEnderDragon = ReflectionUtils.getNMSClass("EntityEnderDragon");
			classWorldProviderTheEnd = ReflectionUtils.getNMSClass("WorldProviderTheEnd");
			classBossBattleServer = ReflectionUtils.getNMSClass("BossBattleServer");
			classEnderDragonBattle = ReflectionUtils.getNMSClass("EnderDragonBattle");
			enumPacketPlayOutBoss$Action = ReflectionUtils.getNMSClass("PacketPlayOutBoss$Action");
			classChatMessage = ReflectionUtils.getNMSClass("ChatMessage");
		}
		
		/**
		 * Get a field from a specific class
		 * 
		 * @param name - The name of the field
		 * @param clazz - The class in which the field is located
		 * @return an instance of the field, or null if not found
		 */
		public static Field getField(String name, Class<?> clazz) {
			try {
				return clazz.getDeclaredField(name);
			} catch (NoSuchFieldException | SecurityException e) {
				System.out.println("Could not find field " + name + " in " + clazz.getSimpleName());
			}
			return null;
		}
		
		/**
		 * Get a method from a specific class
		 * 
		 * @param name - The name of the method
		 * @param clazz - The class in which the method is located
		 * @param paramTypes - The parameter types accepted by this method
		 * @return an instance of the method, or null if not found
		 */
		public static Method getMethod(String name, Class<?> clazz, Class<?>... paramTypes) {
			try {
				return clazz.getMethod(name, paramTypes);
			} catch (NoSuchMethodException | SecurityException e) {
				System.out.println("Could not find method " + name + " in " + clazz.getSimpleName());
			}
			return null;
		}
		
		/**
		 * Get a constructor from a specific class
		 * 
		 * @param clazz - The class in which the constructor is located
		 * @param paramTypes - The parameter types accepted by this constructor
		 * @return an instance of the constructor, or null if not found
		 */
		public static Constructor<?> getConstructor(Class<?> clazz, Class<?>... paramTypes) {
			try {
				return clazz.getConstructor(paramTypes);
			} catch (NoSuchMethodException | SecurityException e) {
				System.out.println("Could not find constructor in class " + clazz.getName());
			}
			return null;
		}
		
		/**
		 * Get a class object from the net.minecraft.server package
		 * 
		 * @param className - The name of the class
		 * @return the NMS class object, or null if not found
		 */
	    public static Class<?> getNMSClass(String className) {
	        try {
	        	return Class.forName("net.minecraft.server." + version + className);
	        } catch (Exception e) {
				System.out.println("Could not find class " + className);
	        }
	        return null;
	    }
	    
		/**
		 * Get a class object from the org.bukkit.craftbukkit package
		 * 
		 * @param className - The name of the class
		 * @return the CraftBukkit class object, or null if not found
		 */
	    public static Class<?> getCBClass(String className) {
	        try {
	        	return Class.forName("org.bukkit.craftbukkit." + version + className);
	        } catch (Exception e) {
				System.out.println("Could not find class " + className);
	        }
	        return null;
	    }
	}
	
}