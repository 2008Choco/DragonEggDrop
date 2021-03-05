package wtf.choco.dragoneggdrop.particle;

import com.google.common.base.Preconditions;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Particle;
import org.jetbrains.annotations.NotNull;

import wtf.choco.dragoneggdrop.particle.condition.ConditionContext;
import wtf.choco.dragoneggdrop.particle.condition.EquationCondition;
import wtf.choco.dragoneggdrop.utils.math.MathExpression;

/**
 * Represents a pair of equations along the x and z axis as well as a list of conditions
 * that must be met to be used by an {@link AnimatedParticleSession}. Includes a set of
 * data that will be used to spawn particles at any given point in an animation.
 *
 * @author Parker Hawke - Choco
 */
public class ConditionalEquationData {

    Particle particle;
    int particleAmount;
    double particleExtra;
    float particleOffsetX, particleOffsetY, particleOffsetZ;
    int particleStreams;

    double speedMultiplier;
    int frameIntervalTicks;
    double thetaIncrement;

    private List<@NotNull EquationCondition> conditions;

    private final MathExpression xExpression, zExpression;

    /**
     * Construct conditional equation data with a pair of equations.
     *
     * @param xExpression the x expression
     * @param zExpression the z expression
     */
    public ConditionalEquationData(@NotNull MathExpression xExpression, @NotNull MathExpression zExpression) {
        Preconditions.checkArgument(xExpression != null, "xExpression must not be null");
        Preconditions.checkArgument(zExpression != null, "zExpression must not be null");

        this.xExpression = xExpression;
        this.zExpression = zExpression;
    }

    /**
     * Get the mathematical expression to be used along the x axis.
     *
     * @return the x axis expression
     */
    @NotNull
    public MathExpression getXExpression() {
        return xExpression;
    }

    /**
     * Get the mathematical expression to be used along the z axis.
     *
     * @return the z axis expression
     */
    @NotNull
    public MathExpression getZExpression() {
        return zExpression;
    }

    /**
     * Add a condition that must be met for this equation data.
     *
     * @param condition the condition to add
     */
    public void addCondition(@NotNull EquationCondition condition) {
        Preconditions.checkArgument(condition != null, "condition must not be null");

        if (conditions == null) {
            this.conditions = new ArrayList<>();
        }

        this.conditions.add(condition);
    }

    /**
     * Check whether or not this equation data's conditions have all been met given the
     * provided context.
     *
     * @param context the context against which to check
     *
     * @return true if met, false otherwise
     */
    public boolean isMet(@NotNull ConditionContext context) {
        Preconditions.checkArgument(context != null, "context must not be null");

        if (conditions == null || conditions.isEmpty()) {
            return true;
        }

        for (EquationCondition condition : conditions) {
            if (!condition.isMet(context)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Get the particle to be spawned for this shape definition.
     *
     * @return the particle
     */
    @NotNull
    public Particle getParticle() {
        return particle;
    }

    /**
     * Get the amount of particles to spawn.
     *
     * @return the particle amount
     */
    public int getParticleAmount() {
        return particleAmount;
    }

    /**
     * Get the extra data for this particle definition. Most particles are not affected by
     * extra data.
     *
     * @return the particle extra data
     */
    public double getParticleExtra() {
        return particleExtra;
    }

    /**
     * Get the particle's x offset.
     *
     * @return the x offset
     */
    public float getParticleOffsetX() {
        return particleOffsetX;
    }

    /**
     * Get the particle's y offset.
     *
     * @return the y offset
     */
    public float getParticleOffsetY() {
        return particleOffsetY;
    }

    /**
     * Get the particle's z offset.
     *
     * @return the z offset
     */
    public float getParticleOffsetZ() {
        return particleOffsetZ;
    }

    /**
     * Get the amount of particle streams to generate.
     *
     * @return the stream count
     */
    public int getParticleStreams() {
        return particleStreams;
    }

    /**
     * Get the animation's speed multiplier.
     *
     * @return the speed multiplier
     */
    public double getSpeedMultiplier() {
        return speedMultiplier;
    }

    /**
     * Get the amount of ticks between each frame of the animation.
     *
     * @return the frame interval in ticks
     */
    public int getFrameIntervalTicks() {
        return frameIntervalTicks;
    }

    /**
     * Get the value by which theta will be incremented each tick in the animation.
     *
     * @return the theta increment
     */
    public double getThetaIncrement() {
        return thetaIncrement;
    }

}
