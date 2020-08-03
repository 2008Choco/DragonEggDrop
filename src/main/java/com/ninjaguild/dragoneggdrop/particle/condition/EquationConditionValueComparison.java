package com.ninjaguild.dragoneggdrop.particle.condition;

import java.util.function.BiPredicate;
import java.util.function.Function;

/**
 * An {@link EquationCondition} implementation. Compares a value fetched from a {@link ConditionContext}
 * against that of a hard-coded value. Whether or not this condition is met will depend on the result
 * of the predicate passed at this object's construction.
 *
 * @param <T> the type of value to compare
 *
 * @author Parker Hawke - Choco
 */
public class EquationConditionValueComparison<T> implements EquationCondition {

    private final Function<ConditionContext, T> query;
    private final T value;
    private final BiPredicate<T, T> predicate;

    /**
     * Construct a value comparison condition.
     *
     * @param query the function to query a value from a condition context
     * @param value the value against which the queried value should be compared
     * @param predicate the comparison predicate
     */
    public EquationConditionValueComparison(Function<ConditionContext, T> query, T value, BiPredicate<T, T> predicate) {
        this.query = query;
        this.value = value;
        this.predicate = predicate;
    }

    @Override
    public boolean isMet(ConditionContext context) {
        return predicate.test(query.apply(context), value);
    }

}
