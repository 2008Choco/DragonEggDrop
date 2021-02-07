package wtf.choco.dragoneggdrop.dragon.loot.pool;

import com.google.gson.JsonObject;

import java.util.Random;

import wtf.choco.dragoneggdrop.dragon.loot.elements.IDragonLootElement;

/**
 * Represents a pool of loot elements.
 *
 * @author Parker Hawke - Choco
 *
 * @param <T> the type of element
 */
public interface ILootPool<T extends IDragonLootElement> {

    /**
     * Get this loot pool's name. Can return null if no name was specified.
     *
     * @return the loot pool name
     */
    public String getName();

    /**
     * Get the chance that this loot pool will generate when created.
     *
     * @return the chance (0.0 - 100.0) of generation
     */
    public double getChance();

    /**
     * Get the minimum amount of times this loot pool should be rolled (inclusive).
     *
     * @return the min rolls
     */
    public int getMinRolls();

    /**
     * Get the maximum amount of times this loot pool should be rolled (inclusive).
     *
     * @return the max rolls
     */
    public int getMaxRolls();

    /**
     * Roll this loot pool using the supplied Random instance. Each roll should result in
     * a different element from this pool.
     *
     * @param random the random instance with which random numbers should be generated
     *
     * @return the randomly generated loot from this pool
     */
    public T roll(Random random);

    /**
     * Write this loot pool as a JsonObject.
     *
     * @return the JSON representation
     */
    public JsonObject toJson();

}
