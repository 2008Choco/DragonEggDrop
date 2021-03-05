package wtf.choco.dragoneggdrop.utils.math;

import org.jetbrains.annotations.NotNull;

import wtf.choco.dragoneggdrop.particle.ParticleVariables;

/**
 * Represents a mathematical expression capable of being evaluated.
 *
 * @author Parker Hawke - Choco
 */
@FunctionalInterface
public interface MathExpression {

    /**
     * Evaluate the mathematical expression.
     *
     * @param variables a set of variables with which to replace special vars
     *
     * @return the evaluation result
     */
    public double evaluate(@NotNull ParticleVariables variables);

}
