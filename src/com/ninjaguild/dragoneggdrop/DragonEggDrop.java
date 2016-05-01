package com.ninjaguild.dragoneggdrop;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;

import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import net.minecraft.server.v1_9_R1.BossBattleServer;
import net.minecraft.server.v1_9_R1.ChatMessage;
import net.minecraft.server.v1_9_R1.EnderDragonBattle;
import net.minecraft.server.v1_9_R1.EntityEnderDragon;
import net.minecraft.server.v1_9_R1.PacketPlayOutBoss;
import net.minecraft.server.v1_9_R1.WorldProviderTheEnd;

import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.v1_9_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_9_R1.entity.CraftEnderDragon;

public class DragonEggDrop extends JavaPlugin implements Listener {

	private PluginDescriptionFile pdf = null;
	private List<String> dragonNames = null;
	private Random rand = null;
	
	public void onEnable() {
		saveDefaultConfig();
		pdf = getDescription();
		
		//update config version to match plugin
		String configVersion = getConfig().getString("version");
		if (configVersion != pdf.getVersion()) {
			ConfigUtil cu = new ConfigUtil(this);
			cu.updateConfig(pdf.getVersion());
		}

		try {
			Particle.valueOf(getConfig().getString("particle-type", "FLAME").toUpperCase());
		} catch (IllegalArgumentException ex) {
			getLogger().log(Level.WARNING, "INVALID PARTICLE TYPE SPECIFIED! DISABLING...");
			getServer().getPluginManager().disablePlugin(this);
			getLogger().log(Level.INFO, "PLUGIN DISABLED");
			return;
		}
		
		getServer().getPluginManager().registerEvents(this, this);
		getCommand("dragoneggdrop").setExecutor(this);
		
		rand = new Random();
		dragonNames = getConfig().getStringList("dragon-names");
		
		String dragonCurrentName = getConfig().getString("current-dragon-name");
	    if (!dragonCurrentName.isEmpty()) {
	    	for (World world : getServer().getWorlds()) {
	    		if (world.getEnvironment() == Environment.THE_END) {
	    			setDragonBossBarTitle(dragonCurrentName, getEnderDragonBattleFromWorld(world));
	    		}
	    	}
	    }
	}
	
	public void onDisable() {
		//
	}

	@EventHandler
	public void onCreatureSpawn(CreatureSpawnEvent e) {
        if (e.getEntityType() == EntityType.ENDER_DRAGON) {
            if (!dragonNames.isEmpty()) {
            	String name = dragonNames.get(rand.nextInt(dragonNames.size()));
            	getConfig().set("current-dragon-name", name);
            	saveConfig();
                setDragonBossBarTitle(name, getEnderDragonBattleFromDragon((EnderDragon)e.getEntity()));
            }
        }
	}
	
	private void setDragonBossBarTitle(String title, EnderDragonBattle battle) {
		try {
			Field f = EnderDragonBattle.class.getDeclaredField("c");
			f.setAccessible(true);
			BossBattleServer battleServer = (BossBattleServer)f.get(battle);
			battleServer.title = new ChatMessage(ChatColor.translateAlternateColorCodes('&', title), new Object[0]);
			battleServer.sendUpdate(PacketPlayOutBoss.Action.UPDATE_NAME);
			f.setAccessible(false);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	protected EnderDragonBattle getEnderDragonBattleFromWorld(World world) {
		return ((WorldProviderTheEnd)((CraftWorld)world).getHandle().worldProvider).s();
	}
	
	protected EnderDragonBattle getEnderDragonBattleFromDragon(EnderDragon dragon) {
		return ((CraftEnderDragon)dragon).getHandle().cU();
	}
	
	@EventHandler
	private void onDragonDeath(EntityDeathEvent e) {
		if (e.getEntityType() == EntityType.ENDER_DRAGON) {
			EntityEnderDragon nmsDragon = ((CraftEnderDragon)e.getEntity()).getHandle();
			//get if the dragon has been previously killed
			boolean prevKilled = nmsDragon.cU().d();
			World world = e.getEntity().getWorld();
			
			DragonEggDrop instance = this;
			new BukkitRunnable() {
				@Override
				public void run() {
					if (nmsDragon.bF >= 185) {//dragon is dead at 200
						cancel();
					    getServer().getScheduler().runTask(instance, new DragonDeathRunnable(instance, world, prevKilled));
					}
				}
				
			}.runTaskTimer(this, 0L, 1L);
		}
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (cmd.getName().equalsIgnoreCase("dragoneggdrop")) {
			if (args.length == 0) {
    	        sender.sendMessage(ChatColor.GOLD + "-------------------");
    	        sender.sendMessage(ChatColor.GOLD + "    DragonEggDrop");
    	        sender.sendMessage(ChatColor.GOLD + "-------------------");
    	        sender.sendMessage(ChatColor.GOLD + "Author: " + pdf.getAuthors().get(0));
    	        sender.sendMessage(ChatColor.GOLD + "Version: " + pdf.getVersion());
    	        sender.sendMessage(ChatColor.GOLD + "-------------------");
        		
        		return false;
			}
			else if (args.length == 1) {
				if (args[0].equalsIgnoreCase("help")) {
					//
				}
				else if (args[0].equalsIgnoreCase("reload")) {
					if (sender.hasPermission("dragoneggdrop.reload")) {
						reloadConfig();
						sender.sendMessage(ChatColor.GREEN + "Reload Complete");
					}
					else {
						sender.sendMessage(ChatColor.RED + "Permission Denied!");
					}
					return true;
				}
				else if (args[0].equalsIgnoreCase("respawn")) {
					//
				}
			}
		}
		
		return false;
	}
	
}
