package com.ninjaguild.dragoneggdrop.utils;

import java.util.HashMap;
import java.util.Map;

import com.ninjaguild.dragoneggdrop.utils.math.MathExpression;
import com.ninjaguild.dragoneggdrop.utils.math.MathUtils;
import com.ninjaguild.dragoneggdrop.versions.NMSAbstract;

import org.apache.commons.lang.Validate;
import org.bukkit.Location;
import org.bukkit.Particle;

/**
 * Represents a defined particle shape. Allows for easy shape-creation with two
 * expressions along the x and z axis, as well as an initial location to start
 * the shape.
 * 
 * @author Parker Hawke - 2008Choco
 */
public class ParticleShapeDefinition {
	
	private final Map<String, Double> variables = new HashMap<>();
	
	private final NMSAbstract nmsAbstract;
	private final Location initialLocation;
	private final MathExpression xExpression, zExpression;
	
	/**
	 * Construct a new ParticleShapeDefinition with a given location, and mathmatical equations
	 * for both the x and z axis.
	 * 
	 * @param nmsAbstract the abstract implementation of NMS for DragonEggDrop
	 * @param initialLocation the initial starting location
	 * @param xExpression the expression for the x axis
	 * @param zExpression the expression for the y axis
	 */
	public ParticleShapeDefinition(NMSAbstract nmsAbstract, Location initialLocation, String xExpression, String zExpression) {
		Validate.notNull(nmsAbstract, "NMS abstract must not be null");
		Validate.notNull(initialLocation, "Null initial locations are not supported");
		Validate.notEmpty(xExpression, "The x axis expression cannot be null or empty");
		Validate.notEmpty(zExpression, "The z axis expression cannot be null or empty");
		
		this.variables.put("x", 0.0);
		this.variables.put("z", 0.0);
		this.variables.put("t", 0.0);
		this.variables.put("theta", 0.0);
		
		this.nmsAbstract = nmsAbstract;
		this.initialLocation = initialLocation;
		this.xExpression = MathUtils.parseExpression(xExpression, variables);
		this.zExpression = MathUtils.parseExpression(zExpression, variables);
	}
	
	/**
	 * Update the variables with new values.
	 * 
	 * @param x the new x value
	 * @param z the new y value
	 * @param t the new value of "t", time (or tick)
	 * @param theta the new theta value
	 */
	public void updateVariables(double x, double z, double t, double theta) {
		this.variables.put("x", x);
		this.variables.put("z", z);
		this.variables.put("t", t);
		this.variables.put("theta", theta);
	}
	
	/**
	 * Execute the particle shape definition expressions with current values. To update values,
	 * see {@link #updateVariables(double, double, double, double)}.
	 * 
	 * @param particleType the type of particle to display
	 * @param particleAmount the amount of particles to display
	 * @param xOffset the x offset for each particle
	 * @param yOffset the y offset for each particle
	 * @param zOffset the z offset for each particle
	 * @param particleExtra the extra value of the particle (generally speed, though this is
	 * dependent on the type of particle used)
	 */
	public void executeExpression(Particle particleType, int particleAmount, double xOffset, double yOffset, double zOffset, double particleExtra) {
		Validate.notNull(particleType, "Cannot spawn Particle of type null");
		
		double x = this.xExpression.evaluate(), z = this.zExpression.evaluate();
		
		this.initialLocation.add(x, 0, z);
		this.nmsAbstract.spawnParticle(particleType, initialLocation, particleAmount, xOffset, yOffset, zOffset, particleExtra);
		this.initialLocation.subtract(x, 0, z);
	}
}