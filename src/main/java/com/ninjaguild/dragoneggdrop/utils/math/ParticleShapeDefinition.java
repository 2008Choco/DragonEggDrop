package com.ninjaguild.dragoneggdrop.utils.math;

import java.util.HashMap;
import java.util.Map;

import com.google.common.base.Preconditions;

import org.bukkit.Location;
import org.bukkit.Particle;

/**
 * Represents a defined particle shape. Allows for easy shape-creation with two
 * expressions along the x and z axis, as well as an initial location to start the shape.
 *
 * @author Parker Hawke - Choco
 */
public class ParticleShapeDefinition {

    private final Map<String, Double> variables = new HashMap<>();

    private final Location initialLocation;
    private final MathExpression xExpression, zExpression;

    /**
     * Construct a new ParticleShapeDefinition with a given location, and mathmatical
     * equations for both the x and z axis.
     *
     * @param initialLocation the initial starting location
     * @param xExpression the expression for the x axis
     * @param zExpression the expression for the y axis
     */
    public ParticleShapeDefinition(Location initialLocation, String xExpression, String zExpression) {
        Preconditions.checkArgument(initialLocation != null, "null initial locations are not supported");
        Preconditions.checkArgument(xExpression != null && !xExpression.isEmpty(), "The x axis expression cannot be null or empty");
        Preconditions.checkArgument(zExpression != null && !zExpression.isEmpty(), "The z axis expression cannot be null or empty");

        this.variables.put("x", 0.0);
        this.variables.put("z", 0.0);
        this.variables.put("t", 0.0);
        this.variables.put("theta", 0.0);

        this.initialLocation = initialLocation.clone();
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
     * Execute the particle shape definition expressions with current values. To update
     * values, see {@link #updateVariables(double, double, double, double)}.
     *
     * @param particleType the type of particle to display
     * @param particleAmount the amount of particles to display
     * @param xOffset the x offset for each particle
     * @param yOffset the y offset for each particle
     * @param zOffset the z offset for each particle
     * @param particleExtra the extra value of the particle (generally speed, though this
     * is dependent on the type of particle used)
     */
    public void executeExpression(Particle particleType, int particleAmount, double xOffset, double yOffset, double zOffset, double particleExtra) {
        Preconditions.checkArgument(particleType != null, "Cannot spawn null particle");

        double x = xExpression.evaluate(), z = zExpression.evaluate();

        this.initialLocation.add(x, 0, z);
        this.initialLocation.getWorld().spawnParticle(particleType, initialLocation, particleAmount, xOffset, yOffset, zOffset, particleExtra, null, true);
        this.initialLocation.subtract(x, 0, z);
    }


    /**
     * Pre-fabricated particle shape definitions.
     *
     * @author Parker Hawke - Choco
     */
    public enum Prefab {

        /**
         * x = x
         * z = z
         */
        BALL("x", "z"),

        /**
         * x = cos(theta) * 1.2
         * z = sin(theta) * 1.2
         */
        HELIX("cos(theta) * 1.2", "sin(theta) * 1.2"),

        /**
         * x = cos(theta) * (100 / t)
         * z = sin(theta) * (100 / t)
         */
        OPEN_END_HELIX("cos(theta) * (100 / t)", "sin(theta) * (100 / t)");


        private final String xExpression, zExpression;

        private Prefab(String xExpression, String zExpression) {
            this.xExpression = xExpression;
            this.zExpression = zExpression;
        }

        /**
         * Create a new {@link ParticleShapeDefinition} from this prefab starting at the
         * given location.
         *
         * @param location the location at which the shape definition should start
         *
         * @return the created prefab
         */
        public ParticleShapeDefinition create(Location location) {
            return new ParticleShapeDefinition(location, xExpression, zExpression);
        }

    }

}
