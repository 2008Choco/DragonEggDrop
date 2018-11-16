package com.ninjaguild.dragoneggdrop.utils;

import java.util.Collection;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.lang.Validate;

/**
 * An implementation of a Collection based on a TreeMap. The goal of
 * the RandomCollection is to simplify the process of retrieving a
 * random object based on its mapped weight.
 * 
 * @param <E> - The type of object to be stored in the Collection
 */
public class RandomCollection<E> {
	
    private final NavigableMap<Double, E> map = new TreeMap<>();
    private final Random random;
    private double total = 0;

    /**
     * Construct a new RandomCollection with a new Random object.
     */
    public RandomCollection() {
        this(new Random());
    }

    /**
     * Construct a new RandomCollection with a given Random object.
     * 
     * @param random an instance of Random to use
     */
    public RandomCollection(Random random) {
    	Validate.notNull(random, "Random instance must not be null");
        this.random = random;
    }

    /**
     * Add a new object to the collection with a given weighted random.
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
     * Add all elements of another RandomCollection.
     * 
     * @param collection the collection to add
     */
    public void addAll(RandomCollection<E> collection) {
    	for (double d : collection.keySet()) {
    		add(d, collection.get(d));
    	}
    }
    
    /**
     * Get an object based on its total weight upon injection.
     * 
     * @param key the total weight at the time of injection
     * 
     * @return the object, or null if not found
     */
    private E get(double key) {
		return map.get(key);
	}
    
    /**
     * Check whether this collection contains a specific value.
     * 
     * @param value whether the value to check for
     * 
     * @return true if the collection contains the value. false otherwise
     */
    public boolean contains(E value) {
    	return map.containsValue(value);
    }

    /**
     * Get a Set of all keys in the underlying TreeMap.
     * 
     * @return all key values
     */
	private Set<Double> keySet() {
    	return map.keySet();
    }
	
	/**
	 * Get a Collection of all values in the underlying TreeMap.
	 * 
	 * @return get all values in the collection
	 */
	public Collection<E> values() {
		return map.values();
	}

	/**
	 * Retrieve the next object, being a random object in the collection
	 * based on its weighted value.
	 * 
	 * @return a random weighted object
	 */
    public E next() {
        double value = random.nextDouble() * total;
        Entry<Double, E> entry = map.ceilingEntry(value);
        
        return (entry != null ? entry.getValue() : null);
    }
    
    /**
     * Clear all data from the random collection.
     */
    public void clear() {
    	this.map.clear();
    	this.total = 0;
    }
    
    /**
     * Check if this collection is completely empty and contains no entries.
     * 
     * @return true if empty, false if elements exist
     */
    public boolean isEmpty() {
    	return map.isEmpty();
    }
    
    /**
     * Get the size of the random collection.
     * 
     * @return the collection size
     */
    public int size() {
    	return map.size();
    }
	
	/**
	 * Get the values of this random collection as an iterable java.util.Collection
	 * object for easy manoeuvrability.
	 * 
	 * @return the resulting collection
	 */
	public Collection<E> toCollection() {
		return map.values();
	}
    
    /**
     * Copy a RandomCollection with identical elements and Random instance.
     * 
     * @param toCopy the collection to copy
     * 
     * @param <E> - The type of object stored in the Collections
     * 
     * @return the collection copy
     */
    public static <E> RandomCollection<E> copyOf(RandomCollection<E> toCopy) {
    	Validate.notNull(toCopy, "Cannot copy a null collection");
    	
    	RandomCollection<E> result = new RandomCollection<>(toCopy.random);
    	result.addAll(toCopy);
    	return result;
    }
    
}