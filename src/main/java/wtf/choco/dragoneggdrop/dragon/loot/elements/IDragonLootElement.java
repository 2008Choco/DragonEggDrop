package wtf.choco.dragoneggdrop.dragon.loot.elements;

import java.util.Random;

import org.bukkit.block.Chest;
import org.bukkit.boss.DragonBattle;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import wtf.choco.dragoneggdrop.dragon.DragonTemplate;
import wtf.choco.dragoneggdrop.dragon.loot.pool.ILootPool;

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
     * @param battle the battle for which to generate the loot. May be null
     * @param template the template whose loot to generate
     * @param killer the player that killed the dragon. May be null
     * @param random a random instance
     * @param chest the chest generated on the portal if one was generated. May be null if
     * no chest was created
     */
    public void generate(@Nullable DragonBattle battle, @NotNull DragonTemplate template, @Nullable Player killer, @NotNull Random random, @Nullable Chest chest);

}
