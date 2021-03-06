package wtf.choco.dragoneggdrop.world;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.boss.DragonBattle;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents all possible locations in which an end crystal may be spawned in regards to
 * the End Portal found in The End.
 *
 * @author Parker Hawke - Choco
 */
public enum PortalCrystal {

    /**
     * The End Crystal located on the north side of the portal (negative Z axis).
     */
    NORTH_CRYSTAL(0, -3),

    /**
     * The End Crystal located on the east side of the portal (positive X axis).
     */
    EAST_CRYSTAL(3, 0),

    /**
     * The End Crystal located on the south side of the portal (positive Z axis).
     */
    SOUTH_CRYSTAL(0, 3),

    /**
     * The End Crystal located on the west side of the portal (negative X axis).
     */
    WEST_CRYSTAL(-3, 0);


    private final int xOffset, zOffset;

    private PortalCrystal(int xOffset, int zOffset) {
        this.xOffset = xOffset;
        this.zOffset = zOffset;
    }

    /**
     * Get the offset on the x axis to be applied for this crystal
     *
     * @return the x offset
     */
    public int getXOffset() {
        return xOffset;
    }

    /**
     * Get the offset on the z axis to be applied for this crystal
     *
     * @return the z offset
     */
    public int getZOffset() {
        return zOffset;
    }

    /**
     * Get a {@link Location} representing the crystal's expected location relative to a
     * given location. The passed location assumes the bottom of the portal (1 block below
     * the base of the portal).
     *
     * @param location the location starting point
     *
     * @return the relative crystal location
     */
    @NotNull
    public Location getRelativeTo(@NotNull Location location) {
        Preconditions.checkArgument(location != null, "location must not be null");
        return location.add(xOffset, 1, zOffset);
    }

    /**
     * Get a {@link Location} representing the crystal's expected location relative to the
     * provided world's portal.
     *
     * @param world the world containing the portal
     *
     * @return the relative crystal location. null if world is not (@link
     * Environment#THE_END)
     */
    @Nullable
    public Location getRelativeToPortal(@NotNull World world) {
        Preconditions.checkArgument(world != null, "world must not be null");

        if (world.getEnvironment() != Environment.THE_END) {
            return null;
        }

        DragonBattle dragonBattle = world.getEnderDragonBattle();
        if (dragonBattle == null) {
            return null;
        }

        dragonBattle.generateEndPortal(false);
        Location endPortalLocation = dragonBattle.getEndPortalLocation();
        assert endPortalLocation != null; // Impossible

        return getRelativeTo(endPortalLocation);
    }

    /**
     * Spawn a crystal on the portal in the given world and optionally set its
     * invulnerability state.
     *
     * @param world the world to spawn the crystal in
     * @param invulnerable the crystal's invulnerable state
     *
     * @return the spawned crystal. null if unsuccessfully spawned
     */
    @Nullable
    public EnderCrystal spawn(@NotNull World world, boolean invulnerable) {
        Preconditions.checkArgument(world != null, "world must not be null");

        if (world.getEnvironment() != Environment.THE_END) {
            return null;
        }

        // (Cloned from #isPresent() only because "location" is required)
        DragonBattle battle = world.getEnderDragonBattle();
        if (battle == null) {
            return null;
        }

        battle.generateEndPortal(false);
        Location endPortalLocation = battle.getEndPortalLocation();
        assert endPortalLocation != null; // Impossible

        Location location = getRelativeTo(endPortalLocation).add(0.5, 0, 0.5);

        // Check for existing crystal
        Collection<Entity> entities = world.getNearbyEntities(location, 1, 1, 1);
        return (EnderCrystal) Iterables.find(entities, e -> e instanceof EnderCrystal, world.spawn(location, EnderCrystal.class, e -> {
            e.setInvulnerable(invulnerable);
            e.setShowingBottom(false);
        }));
    }

    /**
     * Spawn a crystal on the portal in the given world and set it as invulnerable.
     *
     * @param world the world to spawn the crystal in
     *
     * @return the spawned crystal. null if unsuccessfully spawned
     *
     * @see #spawn(World, boolean)
     */
    @Nullable
    public EnderCrystal spawn(@NotNull World world) {
        return spawn(world, true);
    }

    /**
     * Get the current crystal relative to the world's portal location.
     *
     * @param world the world to reference
     *
     * @return the crystal positioned at the crystal location. null if none
     */
    @Nullable
    public EnderCrystal get(@NotNull World world) {
        Preconditions.checkArgument(world != null, "world must not be null");

        DragonBattle battle = world.getEnderDragonBattle();
        if (battle == null) {
            return null;
        }

        battle.generateEndPortal(false);
        Location endPortalLocation = battle.getEndPortalLocation();
        assert endPortalLocation != null; // Impossible

        Location location = getRelativeTo(endPortalLocation);
        Collection<Entity> entities = world.getNearbyEntities(location, 1, 1, 1);
        return (EnderCrystal) Iterables.find(entities, e -> e instanceof EnderCrystal, null);
    }

    /**
     * Check whether this crystal is spawned on the portal or not.
     *
     * @param world the world to check
     *
     * @return true if a crystal is spawned. false otherwise
     */
    public boolean isPresent(@NotNull World world) {
        Preconditions.checkArgument(world != null, "world must not be null");

        DragonBattle battle = world.getEnderDragonBattle();
        if (battle == null) {
            return false;
        }

        battle.generateEndPortal(false);
        Location endPortalLocation = battle.getEndPortalLocation();
        assert endPortalLocation != null; // Impossible

        Location location = getRelativeTo(endPortalLocation);

        // Check for existing crystal
        Collection<Entity> entities = world.getNearbyEntities(location, 1, 1, 1);
        return Iterables.tryFind(entities, e -> e instanceof EnderCrystal).isPresent();
    }

    /**
     * Get all crystals that have been spawned on the portal in the given world.
     *
     * @param world the world to check
     *
     * @return all spawned ender crystals
     */
    @NotNull
    public static List<@NotNull EnderCrystal> getAllSpawnedCrystals(@NotNull World world) {
        Preconditions.checkArgument(world != null, "world must not be null");

        List<EnderCrystal> crystals = new ArrayList<>();

        for (PortalCrystal portalCrystal : values()) {
            EnderCrystal crystal = portalCrystal.get(world);
            if (crystal == null) {
                continue;
            }

            crystals.add(crystal);
        }

        return crystals;
    }

}
