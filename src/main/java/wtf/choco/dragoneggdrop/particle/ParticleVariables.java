package wtf.choco.dragoneggdrop.particle;

import com.google.common.base.Preconditions;

import java.util.Random;

import org.jetbrains.annotations.NotNull;

/**
 * Represents a set of variables to be used when parsing and evaluating an expression
 * in a {@link ParticleShapeDefinition}.
 *
 * @author Parker Hawke - Choco
 */
public final class ParticleVariables {

    private final Random random = new Random();

    private double x, y, z;
    private double t;
    private double theta;

    /**
     * Construct a new set of variables.
     *
     * @param x the initial x value
     * @param y the initial y value
     * @param z the initial z value
     * @param t the initial t value
     * @param theta the initial theta value
     */
    public ParticleVariables(double x, double y, double z, double t, double theta) {
        this.update(x, y, z, t, theta);
    }

    /**
     * Construct a new set of variables where all values are initialized to 0.0
     */
    public ParticleVariables() {
        this(0.0, 0.0, 0.0, 0.0, 0.0);
    }

    /**
     * Get the x variable.
     *
     * @return x
     */
    public double getX() {
        return x;
    }

    /**
     * Get the y variable.
     *
     * @return y
     */
    public double getY() {
        return y;
    }

    /**
     * Get the z variable.
     *
     * @return z
     */
    public double getZ() {
        return z;
    }

    /**
     * Get the t variable.
     *
     * @return t
     */
    public double getT() {
        return t;
    }

    /**
     * Get the theta variable.
     *
     * @return theta
     */
    public double getTheta() {
        return theta;
    }

    void update(double x, double y, double z, double t, double theta) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.t = t;
        this.theta = theta;
    }

    /**
     * Get a variable by its name.
     *
     * @param name the variable name
     * @param defaultValue the default value to return if no variable with the given name
     * could be found
     *
     * @return the value of the variable
     */
    public double get(@NotNull String name, double defaultValue) {
        Preconditions.checkArgument(name != null, "name must not be null");

        switch (name) {
            case "x": return x;
            case "y": return y;
            case "z": return z;
            case "t": return t;
            case "theta": return theta;
            case "random": return random.nextDouble();
            default: return defaultValue;
        }
    }

}
