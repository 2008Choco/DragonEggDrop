package com.ninjaguild.dragoneggdrop.dragon.loot.pool;

import java.util.Random;

import com.google.gson.JsonObject;
import com.ninjaguild.dragoneggdrop.dragon.loot.elements.IDragonLootElement;

public interface ILootPool<T extends IDragonLootElement> {

    public String getName();

    public double getChance();

    public int getMinRolls();

    public int getMaxRolls();

    public T roll(Random random);

    public JsonObject toJson();

}
