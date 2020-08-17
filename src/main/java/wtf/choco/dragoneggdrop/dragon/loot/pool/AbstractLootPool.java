package wtf.choco.dragoneggdrop.dragon.loot.pool;

import java.util.Collection;
import java.util.Random;

import wtf.choco.dragoneggdrop.dragon.loot.elements.IDragonLootElement;
import wtf.choco.dragoneggdrop.utils.RandomCollection;

abstract class AbstractLootPool<T extends IDragonLootElement> implements ILootPool<T> {

    protected final String name;
    protected final double chance;
    protected final int minRolls, maxRolls;
    protected final RandomCollection<T> elements;

    protected AbstractLootPool(String name, double chance, int minRolls, int maxRolls, Collection<T> elements) {
        this.name = name;
        this.chance = chance;
        this.minRolls = minRolls;
        this.maxRolls = maxRolls;

        this.elements = new RandomCollection<>();
        elements.forEach(e -> this.elements.add(e.getWeight(), e));
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public double getChance() {
        return chance;
    }

    @Override
    public int getMinRolls() {
        return minRolls;
    }

    @Override
    public int getMaxRolls() {
        return maxRolls;
    }

    @Override
    public T roll(Random random) {
        return elements.next(random);
    }

}
