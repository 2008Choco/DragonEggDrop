package wtf.choco.dragoneggdrop.world;

import com.google.common.base.Preconditions;

import java.util.concurrent.TimeUnit;

import org.jetbrains.annotations.NotNull;

/**
 * Represents a start time and duration for a dragon respawn process.
 *
 * @author Parker Hawke - Choco
 */
public class DragonRespawnData {

    private final EndWorldWrapper world;
    private final long startTime, duration;

    /**
     * Construct a new {@link DragonRespawnData} with a start time and duration.
     *
     * @param world the world to which this respawn data belongs
     * @param startTime the start time
     * @param duration the duration
     */
    public DragonRespawnData(@NotNull EndWorldWrapper world, long startTime, long duration) {
        Preconditions.checkArgument(world != null, "world must not be null");
        Preconditions.checkArgument(startTime > 0, "startTime must be positive");
        Preconditions.checkArgument(duration >= 0, "duration must be positive");

        this.world = world;
        this.startTime = startTime;
        this.duration = duration;
    }

    /**
     * Get the world to which this respawn data belongs.
     *
     * @return the world
     */
    @NotNull
    public EndWorldWrapper getWorld() {
        return world;
    }

    /**
     * Get the time in milliseconds at which this respawn data was started.
     *
     * @return the start time
     */
    public long getStartTime() {
        return startTime;
    }

    /**
     * Get the duration in milliseconds of this respawn data.
     *
     * @return the duration
     */
    public long getDuration() {
        return duration;
    }

    /**
     * Get the time remaining in the given unit relative to the given timestamp.
     *
     * @param unit the unit in which to fetch the remaining time
     * @param timestamp the timestamp at which the remaining time should be calculated
     *
     * @return the remaining time
     *
     * @see #getRemainingTime(TimeUnit)
     * @see #getRemainingMilliseconds()
     */
    public long getRemainingTime(@NotNull TimeUnit unit, long timestamp) {
        Preconditions.checkArgument(unit != null, "unit must not be null");

        long remainingMilliseconds = getRemainingMilliseconds(timestamp);
        return unit.convert(remainingMilliseconds, TimeUnit.MILLISECONDS);
    }

    /**
     * Get the time remaining in the given unit relative to now.
     *
     * @param unit the unit in which to fetch the remaining time
     *
     * @return the remaining time
     *
     * @see #getRemainingMilliseconds()
     */
    public long getRemainingTime(@NotNull TimeUnit unit) {
        return getRemainingTime(unit, System.currentTimeMillis());
    }

    /**
     * Get the remaining time in milliseconds relative to the given timestamp
     *
     * @param timestamp the timestamp at which the remaining time should be calculated
     *
     * @return the remaining milliseconds
     */
    public long getRemainingMilliseconds(long timestamp) {
        return (startTime + duration) - timestamp;
    }

    /**
     * Get the remaining time in milliseconds relative to now.
     *
     * @return the remaining milliseconds
     */
    public long getRemainingMilliseconds() {
        return getRemainingMilliseconds(System.currentTimeMillis());
    }

    /**
     * Check whether or not this respawn data is ready relative to the given timestamp,
     * meaning that {@link #getRemainingMilliseconds()} is {@literal <=} 0 (the duration
     * has been surpassed).
     *
     * @param timestamp the timestamp at which to check
     *
     * @return true if ready, false otherwise
     */
    public boolean isReady(long timestamp) {
        return getRemainingMilliseconds(timestamp) <= 0;
    }

    /**
     * Check whether or not this respawn data is ready relative to now, meaning that
     * {@link #getRemainingMilliseconds()} is {@literal <=} 0 (the duration has been
     * surpassed).
     *
     * @return true if ready, false otherwise
     */
    public boolean isReady() {
        return isReady(System.currentTimeMillis());
    }

}
