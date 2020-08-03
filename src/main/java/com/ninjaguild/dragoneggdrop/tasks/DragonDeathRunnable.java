package com.ninjaguild.dragoneggdrop.tasks;

import com.ninjaguild.dragoneggdrop.DragonEggDrop;
import com.ninjaguild.dragoneggdrop.api.BattleState;
import com.ninjaguild.dragoneggdrop.api.BattleStateChangeEvent;
import com.ninjaguild.dragoneggdrop.dragon.DragonTemplate;
import com.ninjaguild.dragoneggdrop.dragon.loot.DragonLootTable;
import com.ninjaguild.dragoneggdrop.particle.AnimatedParticleSession;
import com.ninjaguild.dragoneggdrop.particle.ParticleShapeDefinition;
import com.ninjaguild.dragoneggdrop.utils.DEDConstants;
import com.ninjaguild.dragoneggdrop.world.EndWorldWrapper;
import com.ninjaguild.dragoneggdrop.world.RespawnReason;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.boss.DragonBattle;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Represents a BukkitRunnable that handles the generation and particle display of the
 * loot after the Ender Dragon's death.
 */
public class DragonDeathRunnable extends BukkitRunnable {

    private final DragonEggDrop plugin;

    private AnimatedParticleSession particleSession;

    private final World world;
    private final EndWorldWrapper worldWrapper;
    private final DragonTemplate template;

    private int lightningAmount;

    private EnderDragon dragon;
    private boolean respawnDragon = false;

    /**
     * Construct a new DragonDeathRunnable object.
     *
     * @param plugin an instance of the DragonEggDrop plugin
     * @param worldWrapper the world in which the dragon death is taking place
     * @param dragon the dragon dying in this runnable
     */
    public DragonDeathRunnable(DragonEggDrop plugin, EndWorldWrapper worldWrapper, EnderDragon dragon) {
        this.plugin = plugin;
        this.worldWrapper = worldWrapper;
        this.world = worldWrapper.getWorld();
        this.dragon = dragon;
        this.template = worldWrapper.getActiveTemplate();

        FileConfiguration config = plugin.getConfig();
        ParticleShapeDefinition particleShapeDefinition = (template != null ? template.getParticleShapeDefinition() : null);
        this.lightningAmount = config.getInt(DEDConstants.CONFIG_LIGHTNING_AMOUNT);

        // Portal location
        DragonBattle dragonBattle = dragon.getDragonBattle();
        Location portalLocation = dragonBattle.getEndPortalLocation().add(0.5, 0.0, 0.5);

        this.respawnDragon = config.getBoolean(DEDConstants.CONFIG_RESPAWN_ON_DEATH, false);
        this.runTaskTimer(plugin, 0, 1);

        BattleStateChangeEvent bscEventCrystals = new BattleStateChangeEvent(dragonBattle, dragon, BattleState.BATTLE_END, BattleState.PARTICLES_START);
        Bukkit.getPluginManager().callEvent(bscEventCrystals);

        if (particleShapeDefinition != null) {
            this.particleSession = particleShapeDefinition.createSession(world, portalLocation.getX(), portalLocation.getZ());
        }
    }

    @Override
    public void run() {
        if (particleSession != null) {
            this.particleSession.tick();

            if (!particleSession.shouldStop()) {
                return;
            }
        }

        // Particles finished, place reward
        Location location = particleSession.getCurrentLocation().add(0, 1, 0);

        // Summon Zeus!
        for (int i = 0; i < lightningAmount; i++) {
            this.worldWrapper.getWorld().strikeLightning(location);
        }

        DragonBattle dragonBattle = dragon.getDragonBattle();
        this.worldWrapper.setActiveTemplate(null);

        if (template != null) {
            DragonLootTable lootTable = worldWrapper.hasLootTableOverride() ? worldWrapper.getLootTableOverride() : template.getLootTable();
            if (lootTable != null) {
                lootTable.generate(dragonBattle, template, findDragonKiller(dragon));
            }
            else {
                this.plugin.getLogger().warning("Could not generate loot for template " + template.getId() + ". Invalid loot table. Is \"loot\" defined in the template?");

                // Let's just generate an egg instead...
                location.getBlock().setType(Material.DRAGON_EGG);
            }

            this.worldWrapper.setLootTableOverride(null); // Reset the loot table override. Use the template's loot table next instead
        }

        if (respawnDragon && world.getPlayers().size() > 0) {
            this.worldWrapper.startRespawn(RespawnReason.DEATH);
        }

        BattleStateChangeEvent bscEventCrystals = new BattleStateChangeEvent(dragonBattle, dragon, BattleState.PARTICLES_START, BattleState.LOOT_SPAWN);
        Bukkit.getPluginManager().callEvent(bscEventCrystals);
        this.cancel();
    }

    private Player findDragonKiller(EnderDragon dragon) {
        EntityDamageEvent lastDamageCause = dragon.getLastDamageCause();
        if (!(lastDamageCause instanceof EntityDamageByEntityEvent)) {
            return null;
        }

        Entity damager = ((EntityDamageByEntityEvent) lastDamageCause).getDamager();
        if (damager instanceof Player) {
            return (Player) damager;
        }

        else if (damager instanceof Projectile) {
            ProjectileSource projectileSource = ((Projectile) damager).getShooter();
            if (!(projectileSource instanceof Player)) {
                return null; // Give up
            }

            return (Player) projectileSource;
        }

        return null;
    }

}
