package com.ninjaguild.dragoneggdrop.utils.function;

import java.util.function.BiPredicate;

/**
 * Represents a predicate (boolean-valued function) of two {@code double}-valued
 * arguments. This is the {@code double}-consuming primitive type specialization
 * of {@link BiPredicate}.
 *
 * @see BiPredicate
 *
 * @author Parker Hawke - Choco
 */
@FunctionalInterface
public interface DoubleBiPredicate {

    /**
     * Evaluates this predicate on the given arguments.
     *
     * @param first the first input argument
     * @param second the second input argument
     *
     * @return true if the input arguments match the predicate, false otherwise
     */
    public boolean test(double first, double second);

}
