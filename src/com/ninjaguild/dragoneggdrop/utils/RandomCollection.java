/*
    DragonEggDrop
    Copyright (C) 2016  NinjaStix
    ninjastix84@gmail.com

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.ninjaguild.dragoneggdrop.utils;

import java.util.Collection;
import java.util.NavigableMap;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;

/**
 * An implementation of a Collection based on a TreeMap. The goal of
 * the RandomCollection is to simplify the process of retrieving a
 * random object based on its mapped weight
 * 
 * @param <E> - The type of object to be stored in the Collection
 */
public class RandomCollection<E> {
	
    private final NavigableMap<Double, E> map = new TreeMap<Double, E>();
    private final Random random;
    private double total = 0;

    /**
     * Construct a new RandomCollection with a new Random object
     */
    public RandomCollection() {
        this(new Random());
    }

    /**
     * Construct a new RandomCollection with a given Random object
     * 
     * @param random an instance of Random to use
     */
    public RandomCollection(Random random) {
        this.random = random;
    }

    /**
     * Add a new object to the collection with a given weighted random
     * 
     * @param weight the weight of the object
     * @param result the object to add
     * 
     * @return true if successfully added, false if an invalid weight was provided
     */
    public boolean add(double weight, E result) {
        if (weight <= 0.0D) {
        	return false;
        }
        
        total += weight;
        map.put(total, result);
        
        return true;
    }
    
    /**
     * Add all elements of another RandomCollection
     * 
     * @param collection the collection to add
     */
    public void addAll(RandomCollection<E> collection) {
    	for (double d : collection.keySet()) {
    		add(d, collection.get(d));
    	}
    }
    
    /**
     * Get an object based on its total weight upon injection
     * 
     * @param key the total weight at the time of injection
     * @return the object, or null if not found
     */
    private E get(Object key) {
		return map.get(key);
	}

    /**
     * Get a Set of all keys in the underlying TreeMap
     * 
     * @return all key values
     */
	private Set<Double> keySet() {
    	return map.keySet();
    }
	
	/**
	 * Get a Collection of all values in the underlying TreeMap
	 * 
	 * @return get all values in the collection
	 */
	public Collection<E> values() {
		return map.values();
	}

	/**
	 * Retrieve the next object, being a random object in the collection
	 * based on its weighted value
	 * 
	 * @return a random weighted object
	 */
    public E next() {
        double value = random.nextDouble() * total;
        return map.ceilingEntry(value).getValue();
    }
    
    /**
     * Clear all data from the random collection
     */
    public void clear() {
    	this.map.clear();
    	this.total = 0;
    }
    
}