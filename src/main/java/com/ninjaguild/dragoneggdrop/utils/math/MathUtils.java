package com.ninjaguild.dragoneggdrop.utils.math;

import java.util.HashMap;
import java.util.Map;
import java.util.function.DoubleUnaryOperator;

/**
 * A powerful utility class to assist in the parsing and evaluation
 * of arithmetic functions with custom variables and operations through
 * a recursive algorithm.
 * 
 * @author Parker Hawke - 2008Choco
 */
public class MathUtils {
	
	/*
	 * This code is loosely based off of and modified from an original thread
	 * on Stackoverflow's forums:
	 * http://stackoverflow.com/questions/40975678/evaluating-a-math-expression-with-variables-java-8
	 * 
	 * Modifications include:
	 *   - Allow for custom arithmetic functions
	 *   - Variables included in the function
	 *   - Java 8 functionality & Object-Oriented format
	 */
	
	private static final Map<String, DoubleUnaryOperator> OPERATORS = new HashMap<>();
	
	static {
		// Basic arithmetics
		OPERATORS.put("sqrt", StrictMath::sqrt);
		OPERATORS.put("abs", Math::abs);
		OPERATORS.put("log", x -> x <= 0 ? Double.NaN : Math.log10(x));
		
		// Trigonometric
		OPERATORS.put("sin", x -> StrictMath.sin(Math.toRadians(x)));
		OPERATORS.put("cos", x -> StrictMath.cos(Math.toRadians(x)));
		OPERATORS.put("tan", x -> StrictMath.tan(Math.toRadians(x)));
		
		OPERATORS.put("csc", x -> 1 / StrictMath.sin(Math.toRadians(x)));
		OPERATORS.put("sec", x -> 1 / StrictMath.cos(Math.toRadians(x)));
		OPERATORS.put("cot", x -> 1 / StrictMath.tan(Math.toRadians(x)));
		
		// Conversion
		OPERATORS.put("rad", Math::toRadians);
		OPERATORS.put("deg", Math::toDegrees);
	}
	
	private MathUtils(){}
	
	/**
	 * Evaluate a mathematical expression with given variables.
	 * 
	 * @param expression the string to parse
	 * @param variables the map containing necessary variables for this expression
	 * (To modify variables at any given time between evaluations, replace values in the map)
	 * 
	 * @return The mathematical expression
	 */
	public static MathExpression parseExpression(String expression, Map<String, Double> variables) {
		return new ExpressionEvaluator(expression, variables).parse();
	}
	
	/**
	 * Evaluate a basic mathematical expression. Variables are not permitted in
	 * expressions parsed by this method. For variable-based functions, see
	 * {@link #parseExpression(String, Map)}.
	 * 
	 * @param expression the string to parse
	 * @return The mathematical expression
	 */
	public static MathExpression parseExpression(String expression) {
		return new ExpressionEvaluator(expression).parse();
	}
	
	/**
	 * Inject a custom mathematical operation into the expression parser.
	 * 
	 * @param functionName the name of the function to inject (i.e. "sqrt")
	 * @param operator the operation to perform when parsing this function
	 * 
	 * @return true if successful. false if operator already exists
	 */
	public static boolean injectMathematicalOperator(String functionName, DoubleUnaryOperator operator) {
		if (OPERATORS.containsKey(functionName)) return false;
		
		OPERATORS.put(functionName, operator);
		return true;
	}
	
	/**
	 * The logic behind the parsing of {@link MathExpression} objects.
	 * 
	 * @author Parker Hawke - 2008Choco
	 */
	private static class ExpressionEvaluator {
		
		private int pos = -1, ch;
		
		private final String expression;
		private final Map<String, Double> variables;
		
		public ExpressionEvaluator(String expression, Map<String, Double> variables) {
			this.expression = expression;
			this.variables = variables;
		}
		
		public ExpressionEvaluator(String expression) {
			this(expression, new HashMap<>());
		}
        
		/**
		 * Proceed to the next character in the expression
		 */
        public void nextChar() {
            ch = (++pos < expression.length()) ? expression.charAt(pos) : -1;
        }
        
        /**
         * Attempt to find a given character at the next position in
         * the expression (whilst ignoring whitespace characters).
         * 
         * @param charToEat the character to find
         * @return true if found. False otherwise
         */
        public boolean eat(int charToEat) {
            while (ch == ' ') nextChar();
            if (ch == charToEat) {
                this.nextChar();
                return true;
            }
            return false;
        }
        
        /**
         * Parse the provided function into a MathExpression using
         * recursive functions.
         * 
         * @return the parsed mathematical expression
         */
        public MathExpression parse() {
        	this.nextChar();
            MathExpression x = this.parseExpression();
            if (pos < expression.length()) throw new RuntimeException("Unexpected: " + (char)ch);
            return x;
        }
        
        /* Grammar:
         * expression = term | expression + term | expression - term
         * term = factor | term * factor | term / factor
         * factor = + factor | - factor | ( expression )
         *        | number | functionName factor | factor ^ factor
         */
        
        /**
         * Parse an entire sub-expression in the parent expression
         * (including addition and subtraction).
         * 
         * @return the parsed expression
         */
        public MathExpression parseExpression() {
            MathExpression x = this.parseTerm();
            while (true) {
                if (eat('+')) { // addition
                	MathExpression a = x, b = parseTerm();
                    x = (() -> a.evaluate() + b.evaluate());
                }
                else if (eat('-')) { // subtraction
                	MathExpression a = x, b = parseTerm();
                    x = (() -> a.evaluate() - b.evaluate());
                }
                else return x;
            }
        }
        
        /**
         * Parse a term in the parent expression (including
         * multiplication and division).
         * 
         * @return the parsed term
         */
        public MathExpression parseTerm() {
            MathExpression x = this.parseFactor();
            while (true) {
                if (eat('*')){ // multiplication
                	MathExpression a = x, b = parseFactor();
                    x = (() -> a.evaluate() * b.evaluate());
                }
                else if (eat('/')) { // division
                	MathExpression a = x, b = parseFactor();
                    x = (() -> a.evaluate() / b.evaluate());
                }
                else return x;
            }
        }
        
        /**
         * Parse a factor in the parent expression (including
         * addition, subtraction, parentheses and injected
         * operation functions).
         * 
         * @return the parsed factor
         */
        public MathExpression parseFactor() {
            if (eat('+')) return parseFactor(); // unary plus
            if (eat('-')){
            	double value = -parseFactor().evaluate();
            	return (() -> value); // unary minus
            }

            MathExpression x;
            int startPos = this.pos;
            if (eat('(')) { // parentheses
                x = this.parseExpression();
                eat(')');
            } else if ((ch >= '0' && ch <= '9') || ch == '.') { // numbers
                while ((ch >= '0' && ch <= '9') || ch == '.') this.nextChar();
                
                String value = expression.substring(startPos, this.pos);
                x = (() -> Double.parseDouble(value));
            } else if (ch >= 'a' && ch <= 'z') { // functions
                while (ch >= 'a' && ch <= 'z') this.nextChar();
                String func = expression.substring(startPos, this.pos);
                
                if (OPERATORS.containsKey(func)) {
                	DoubleUnaryOperator operand = OPERATORS.get(func);
                	MathExpression a = this.parseFactor();
                	x = (() -> operand.applyAsDouble(a.evaluate()));
                } else {
                	x = (() -> variables != null ? variables.getOrDefault(func, 0.0) : 0);
                }
            } else {
                throw new ArithmeticException("Unexpected: \"" + ch + "\"");
            }

            if (eat('^')) { // exponentiation
            	MathExpression a = x, b = parseFactor();
                x = (() -> Math.pow(a.evaluate(), b.evaluate()));
            }

            return x;
        }
	}
}