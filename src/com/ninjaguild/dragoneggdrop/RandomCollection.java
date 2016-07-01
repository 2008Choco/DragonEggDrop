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

package com.ninjaguild.dragoneggdrop;

import java.util.Collection;
import java.util.NavigableMap;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;

public class RandomCollection<E> {
	
    private final NavigableMap<Double, E> map = new TreeMap<Double, E>();
    private final Random random;
    private double total = 0;

    public RandomCollection() {
        this(new Random());
    }

    public RandomCollection(final Random random) {
        this.random = random;
    }

    protected boolean add(double weight, E result) {
        if (weight <= 0.0D) {
        	return false;
        }
        
        total += weight;
        map.put(total, result);
        
        return true;
    }
    
    protected void addAll(RandomCollection<E> collection) {
    	for (double d : collection.keySet()) {
    		add(d, collection.get(d));
    	}
    }
    
    private E get(Object key) {
		return map.get(key);
	}

	private Set<Double> keySet() {
    	return map.keySet();
    }
	
	protected Collection<E> values() {
		return map.values();
	}

    protected E next() {
        double value = random.nextDouble() * total;
        return map.ceilingEntry(value).getValue();
    }
    
}
