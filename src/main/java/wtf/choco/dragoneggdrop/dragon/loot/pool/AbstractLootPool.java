package wtf.choco.dragoneggdrop.dragon.loot.pool;

import com.google.common.base.Preconditions;

import java.util.Collection;
import java.util.Random;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import wtf.choco.commons.collection.RandomCollection;
import wtf.choco.dragoneggdrop.dragon.loot.elements.IDragonLootElement;

abstract class AbstractLootPool<T extends IDragonLootElement> implements ILootPool<T> {

    protected final String name;
    protected final double chance;
    protected final int minRolls, maxRolls;
    protected final RandomCollection<@Nullable T> elements;

    protected AbstractLootPool(@Nullable String name, double chance, int minRolls, int maxRolls, @NotNull Collection<@Nullable T> elements) {
        Preconditions.checkArgument(elements != null, "elements must not be null");

        this.name = name;
        this.chance = chance;
        this.minRolls = minRolls;
        this.maxRolls = maxRolls;

        this.elements = new RandomCollection<>();
        elements.forEach(e -> {
            this.elements.add(e != null ? e.getWeight() : 0.0, e);
        });
    }

    @Nullable
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

    @Nullable
    @Override
    public T roll(@NotNull Random random) {
        return elements.next(random);
    }

}
