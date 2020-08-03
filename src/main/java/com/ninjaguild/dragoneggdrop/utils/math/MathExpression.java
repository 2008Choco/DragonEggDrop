package com.ninjaguild.dragoneggdrop.utils.math;

import com.ninjaguild.dragoneggdrop.particle.ParticleVariables;

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
    public double evaluate(ParticleVariables variables);

}
