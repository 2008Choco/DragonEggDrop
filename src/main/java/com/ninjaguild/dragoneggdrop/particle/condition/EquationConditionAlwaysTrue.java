package com.ninjaguild.dragoneggdrop.particle.condition;

/**
 * An {@link EquationCondition} implementation. Will always be met.
 *
 * @author Parker Hawke - Choco
 */
public final class EquationConditionAlwaysTrue implements EquationCondition {

    /**
     * The singleton instance for this condition.
     */
    public static final EquationConditionAlwaysTrue INSTANCE = new EquationConditionAlwaysTrue();

    private EquationConditionAlwaysTrue() { }

    @Override
    public boolean isMet(ConditionContext context) {
        return true;
    }

}
