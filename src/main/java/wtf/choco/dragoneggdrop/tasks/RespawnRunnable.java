package wtf.choco.dragoneggdrop.tasks;

import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.boss.DragonBattle;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.EnderDragon;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import wtf.choco.dragoneggdrop.DragonEggDrop;
import wtf.choco.dragoneggdrop.api.BattleState;
import wtf.choco.dragoneggdrop.api.BattleStateChangeEvent;
import wtf.choco.dragoneggdrop.utils.ActionBarUtil;
import wtf.choco.dragoneggdrop.utils.DEDConstants;
import wtf.choco.dragoneggdrop.world.EndWorldWrapper;
import wtf.choco.dragoneggdrop.world.PortalCrystal;

/**
 * Represents a BukkitRunnable that handles the respawning of the Ender Dragon after it
 * has been slain.
 */
public class RespawnRunnable extends BukkitRunnable {

    private final DragonEggDrop plugin;
    private final EndWorldWrapper worldWrapper;

    private final DragonBattle dragonBattle;
    private final EnderDragon dragon;

    private final boolean announceRespawn;
    private final List<@NotNull String> announceMessages;
    private final boolean limitAnnounceToRadius;
    private final int announceMessageRadiusSquared;

    private int currentCrystal = 0, currentMessage = 0;
    private int secondsUntilRespawn;

    /**
     * Construct a new RespawnRunnable object.
     *
     * @param plugin an instance of the DragonEggDrop plugin
     * @param world the world to execute a respawn
     * @param respawnTime the time in seconds until the respawn is executed
     */
    public RespawnRunnable(@NotNull DragonEggDrop plugin, @NotNull World world, int respawnTime) {
        this.plugin = plugin;
        this.worldWrapper = EndWorldWrapper.of(world);
        this.secondsUntilRespawn = respawnTime;

        this.dragonBattle = world.getEnderDragonBattle();
        this.dragon = dragonBattle.getEnderDragon();

        this.announceMessages = plugin.getConfig().getStringList(DEDConstants.CONFIG_RESPAWN_MESSAGES_MESSAGES).stream().map(s -> ChatColor.translateAlternateColorCodes('&', s)).collect(Collectors.toList());
        this.announceRespawn = announceMessages.size() > 0;

        int announceMessageRadius = plugin.getConfig().getInt(DEDConstants.CONFIG_RESPAWN_MESSAGES_RADIUS, -1);
        this.limitAnnounceToRadius = (announceMessageRadius > 0);
        this.announceMessageRadiusSquared = (int) Math.pow(announceMessageRadius, 2);

        // Event call
        BattleStateChangeEvent bscEventCrystals = new BattleStateChangeEvent(dragonBattle, dragon, BattleState.DRAGON_DEAD, BattleState.CRYSTALS_SPAWNING);
        Bukkit.getPluginManager().callEvent(bscEventCrystals);
    }

    @Override
    public void run() {
        if (this.secondsUntilRespawn > 0) {
            if (announceRespawn) {
                // Show actionbar messages
                String message = announceMessages.get(currentMessage);

                if (limitAnnounceToRadius) {
                    Location endPortalLocation = dragonBattle.getEndPortalLocation();
                    if (endPortalLocation != null) {
                        ActionBarUtil.broadcastActionBar(message, endPortalLocation, announceMessageRadiusSquared, true);
                    }
                }
                else {
                    ActionBarUtil.broadcastActionBar(message, worldWrapper.getWorld(), true);
                }

                if (++currentMessage >= announceMessages.size()) {
                    this.currentMessage = 0;
                }
            }

            this.secondsUntilRespawn--;
            return;
        }

        // Only respawn if a Player is in the World
        World world = worldWrapper.getWorld();
        if (world.getPlayers().size() <= 0) {
            return;
        }

        // Start respawn process
        PortalCrystal crystalPos = PortalCrystal.values()[currentCrystal++];
        Location crystalLocation = crystalPos.getRelativeToPortal(world);
        assert crystalLocation != null;

        World crystalWorld = crystalLocation.getWorld();
        assert crystalWorld != null; // Impossible

        Chunk crystalChunk = crystalWorld.getChunkAt(crystalLocation);
        if (!crystalChunk.isLoaded()) {
            crystalChunk.load();
        }

        // Remove any existing crystal
        EnderCrystal existingCrystal = crystalPos.get(world);
        if (existingCrystal != null) {
            existingCrystal.remove();
        }

        crystalPos.spawn(world);
        crystalWorld.createExplosion(crystalLocation.getX(), crystalLocation.getY(), crystalLocation.getZ(), 0F, false, false);
        crystalWorld.spawnParticle(Particle.EXPLOSION_HUGE, crystalLocation, 0);

        // All crystals respawned
        if (currentCrystal >= 4) {
            // If dragon already exists, cancel the respawn process
            if (crystalWorld.getEntitiesByClass(EnderDragon.class).size() >= 1) {
                this.plugin.getLogger().warning("An EnderDragon is already present in world " + crystalWorld.getName() + ". Dragon respawn cancelled");

                ActionBarUtil.broadcastActionBar(ChatColor.RED + "Dragon respawn abandonned! Dragon already exists! Slay it!", crystalWorld, false);

                // Destroy all crystals
                for (PortalCrystal portalCrystal : PortalCrystal.values()) {
                    Location location = portalCrystal.getRelativeToPortal(world);
                    assert location != null; // Impossible

                    EnderCrystal enderCrystal = portalCrystal.get(world);
                    if (enderCrystal != null) {
                        enderCrystal.remove();
                    }

                    crystalWorld.getPlayers().forEach(p -> p.playSound(location, Sound.BLOCK_FIRE_EXTINGUISH, 1000, 1));
                    crystalWorld.createExplosion(location.getX(), location.getY(), location.getZ(), 0F, false, false);
                }

                this.cancel();
                return;
            }

            this.dragonBattle.initiateRespawn();
            RespawnSafeguardRunnable.newTimeout(plugin, worldWrapper.getWorld(), dragonBattle);

            BattleStateChangeEvent bscEventRespawning = new BattleStateChangeEvent(dragonBattle, dragon, BattleState.CRYSTALS_SPAWNING, BattleState.DRAGON_RESPAWNING);
            Bukkit.getPluginManager().callEvent(bscEventRespawning);

            this.worldWrapper.stopRespawn();
            this.cancel();
        }
    }

    /**
     * Get the amount of time remaining (in seconds) until the dragon respawns.
     *
     * @return the remaining time
     */
    public int getSecondsUntilRespawn() {
        return secondsUntilRespawn;
    }

}
