package com.ninjaguild.dragoneggdrop;

import java.util.Collection;
import java.util.NavigableMap;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;

public class RandomCollection<E>
{
    private final NavigableMap<Double, E> map = new TreeMap<Double, E>();
    private final Random random;
    private double total = 0;

    public RandomCollection()
    {
        this(new Random());
    }

    public RandomCollection(Random random)
    {
        this.random = random;
    }

    public boolean add(double weight, E result)
    {
        if (weight <= 0.0D)
        {
        	return false;
        }
        
        total += weight;
        map.put(total, result);
        
        return true;
    }
    
    public void addAll(RandomCollection<E> collection)
    {
    	for (double d : collection.keySet())
    	{
    		add(d, collection.get(d));
    	}
    }
    
    private E get(Object key)
    {
		return map.get(key);
	}

	private Set<Double> keySet()
    {
    	return map.keySet();
    }
	
	public Collection<E> values()
	{
		return map.values();
	}

    public E next()
    {
        double value = random.nextDouble() * total;
        return map.ceilingEntry(value).getValue();
    }
}
