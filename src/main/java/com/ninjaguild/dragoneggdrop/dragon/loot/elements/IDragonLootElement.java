package com.ninjaguild.dragoneggdrop.dragon.loot.elements;

import java.util.Random;

import com.ninjaguild.dragoneggdrop.dragon.loot.pool.ILootPool;

import org.bukkit.block.Chest;
import org.bukkit.boss.DragonBattle;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Player;

/**
 * Represents a loot element as part of an {@link ILootPool}. These loot elements may be
 * generated in the world.
 *
 * @author Parker Hawke - Choco
 */
public interface IDragonLootElement {

    /**
     * Get this object's weight in the loot pool.
     *
     * @return this element's weight
     */
    public double getWeight();

    /**
     * Generate this loot element.
     *
     * @param battle the battle for which to generate the loot
     * @param dragon the dragon whose loot to generate
     * @param killer the player that killed the dragon. May be null
     * @param random a random instance
     * @param chest the chest generated on the portal if one was generated. May be null if
     * no chest was created
     */
    public void generate(DragonBattle battle, EnderDragon dragon, Player killer, Random random, Chest chest);

}
