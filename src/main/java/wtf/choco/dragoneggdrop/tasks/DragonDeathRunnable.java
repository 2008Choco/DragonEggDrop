package wtf.choco.dragoneggdrop.tasks;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.boss.DragonBattle;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import wtf.choco.dragoneggdrop.DragonEggDrop;
import wtf.choco.dragoneggdrop.api.BattleState;
import wtf.choco.dragoneggdrop.api.BattleStateChangeEvent;
import wtf.choco.dragoneggdrop.dragon.DragonTemplate;
import wtf.choco.dragoneggdrop.dragon.loot.DragonLootTable;
import wtf.choco.dragoneggdrop.particle.AnimatedParticleSession;
import wtf.choco.dragoneggdrop.particle.ParticleShapeDefinition;
import wtf.choco.dragoneggdrop.utils.DEDConstants;
import wtf.choco.dragoneggdrop.world.EndWorldWrapper;
import wtf.choco.dragoneggdrop.world.RespawnReason;

/**
 * Represents a BukkitRunnable that handles the generation and particle display of the
 * loot after the Ender Dragon's death.
 */
public class DragonDeathRunnable extends BukkitRunnable {

    private final DragonEggDrop plugin;

    private AnimatedParticleSession particleSession;

    private final EndWorldWrapper worldWrapper;
    private final DragonTemplate template;
    private final Location portalLocation;

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
    public DragonDeathRunnable(@NotNull DragonEggDrop plugin, @NotNull EndWorldWrapper worldWrapper, @NotNull EnderDragon dragon) {
        this.plugin = plugin;
        this.worldWrapper = worldWrapper;
        this.dragon = dragon;
        this.template = worldWrapper.getActiveTemplate();

        FileConfiguration config = plugin.getConfig();
        ParticleShapeDefinition particleShapeDefinition = (template != null ? template.getParticleShapeDefinition() : null);
        this.lightningAmount = config.getInt(DEDConstants.CONFIG_LIGHTNING_AMOUNT);

        // Portal location
        DragonBattle dragonBattle = dragon.getDragonBattle();
        Location endPortalLocation = dragonBattle != null ? dragonBattle.getEndPortalLocation() : null;
        this.portalLocation = endPortalLocation != null ? endPortalLocation.add(0.5, 0.0, 0.5) : new Location(worldWrapper.getWorld(), 0, 63, 0);

        this.respawnDragon = config.getBoolean(DEDConstants.CONFIG_RESPAWN_ON_DEATH, false);
        this.runTaskTimer(plugin, 0, 1);

        BattleStateChangeEvent bscEventCrystals = new BattleStateChangeEvent(dragonBattle, dragon, BattleState.BATTLE_END, BattleState.PARTICLES_START);
        Bukkit.getPluginManager().callEvent(bscEventCrystals);

        if (particleShapeDefinition != null) {
            this.particleSession = particleShapeDefinition.createSession(worldWrapper.getWorld(), portalLocation.getX() + 0.5, portalLocation.getZ() + 0.5);
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
        Location location = particleSession != null ? particleSession.getCurrentLocation().clone().add(0, 1, 0) : portalLocation.clone().add(0.0, 4.0, 0.0);

        // Summon Zeus!
        for (int i = 0; i < lightningAmount; i++) {
            this.worldWrapper.getWorld().strikeLightning(location).setMetadata(DEDConstants.METADATA_LOOT_LIGHTNING, new FixedMetadataValue(plugin, true));
        }

        DragonBattle dragonBattle = dragon.getDragonBattle();
        this.worldWrapper.setActiveTemplate(null);

        if (template != null) {
            DragonLootTable lootTable = worldWrapper.hasLootTableOverride() ? worldWrapper.getLootTableOverride() : template.getLootTable();
            if (lootTable != null && dragonBattle != null) {
                lootTable.generate(dragonBattle, template, findDragonKiller(dragon));
            }
            else {
                this.plugin.getLogger().warning("Could not generate loot for template " + template.getId() + ". Invalid loot table. Is \"loot\" defined in the template?");

                // Let's just generate an egg instead...
                location.getBlock().setType(Material.DRAGON_EGG);
            }

            this.worldWrapper.setLootTableOverride(null); // Reset the loot table override. Use the template's loot table next instead
        }

        if (respawnDragon && worldWrapper.getWorld().getPlayers().size() > 0) {
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
