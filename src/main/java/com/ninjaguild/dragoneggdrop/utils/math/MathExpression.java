package com.ninjaguild.dragoneggdrop.utils.math;

/**
 * Represents a mathematical expression capable of being evaluated.
 * 
 * @author Parker Hawke - 2008Choco
 */
@FunctionalInterface
public interface MathExpression {
	
	/**
	 * Evaluate the mathematical expression.
	 * 
	 * @return the evaluation result
	 */
	public double evaluate();
	
}