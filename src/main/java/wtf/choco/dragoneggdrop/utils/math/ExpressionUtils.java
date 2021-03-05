package wtf.choco.dragoneggdrop.utils.math;

import com.google.common.base.Preconditions;

import java.util.HashMap;
import java.util.Map;
import java.util.function.DoubleUnaryOperator;

import org.jetbrains.annotations.NotNull;

/**
 * A utility class to parse and obtain instances of {@link MathExpression}
 *
 * @author Parker Hawke - Choco
 */
public final class ExpressionUtils {

    /*
     * This code is loosely based off of and modified from an original thread on
     * Stackoverflow's forums:
     * http://stackoverflow.com/questions/40975678/evaluating-a-math-expression-
     * with-variables-java-8 Modifications include: - Allow for custom arithmetic
     * functions - Variables included in the function - Java 8 functionality &
     * Object-Oriented format
     */

    private static final Map<@NotNull String, @NotNull DoubleUnaryOperator> OPERATORS = new HashMap<>();

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

    private ExpressionUtils() {}

    /**
     * Evaluate a basic mathematical expression.
     *
     * @param expression the string to parse
     *
     * @return The mathematical expression
     */
    @NotNull
    public static MathExpression parseExpression(@NotNull String expression) {
        Preconditions.checkArgument(expression != null, "expression must not be null");
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
    public static boolean injectMathematicalOperator(@NotNull String functionName, @NotNull DoubleUnaryOperator operator) {
        Preconditions.checkArgument(functionName != null, "functionName must not be null");
        Preconditions.checkArgument(operator != null, "operator must not be null");

        if (OPERATORS.containsKey(functionName)) {
            return false;
        }

        OPERATORS.put(functionName, operator);
        return true;
    }

    /**
     * The logic behind the parsing of {@link MathExpression} objects.
     *
     * @author Parker Hawke - Choco
     */
    private static class ExpressionEvaluator {

        private int pos = -1, ch;

        private final String expression;

        public ExpressionEvaluator(@NotNull String expression) {
            this.expression = expression;
        }

        /**
         * Proceed to the next character in the expression
         */
        public void nextChar() {
            this.ch = (++pos < expression.length()) ? expression.charAt(pos) : -1;
        }

        /**
         * Attempt to find a given character at the next position in the expression
         * (whilst ignoring whitespace characters).
         *
         * @param charToEat the character to find
         *
         * @return true if found. False otherwise
         */
        public boolean eat(int charToEat) {
            while (ch == ' ') {
                this.nextChar();
            }

            if (ch == charToEat) {
                this.nextChar();
                return true;
            }

            return false;
        }

        /**
         * Parse the provided function into a MathExpression using recursive functions.
         *
         * @return the parsed mathematical expression
         */
        @NotNull
        public MathExpression parse() {
            this.nextChar();

            MathExpression x = this.parseExpression();
            if (pos < expression.length()) {
                throw new RuntimeException("Unexpected: " + (char) ch);
            }

            return x;
        }

        /*
         * Grammar:
         *   expression = term | expression + term | expression - term
         *   term = factor | term * factor | term / factor
         *   factor = + factor | - factor | ( expression )
         *          | number | functionName factor | factor ^ factor
         */

        /**
         * Parse an entire sub-expression in the parent expression (including addition and
         * subtraction).
         *
         * @return the parsed expression
         */
        @NotNull
        public MathExpression parseExpression() {
            MathExpression x = parseTerm();

            while (true) {
                if (eat('+')) { // addition
                    MathExpression a = x, b = parseTerm();
                    x = (variables -> a.evaluate(variables) + b.evaluate(variables));
                }
                else if (eat('-')) { // subtraction
                    MathExpression a = x, b = parseTerm();
                    x = (variables -> a.evaluate(variables) - b.evaluate(variables));
                }
                else {
                    return x;
                }
            }
        }

        /**
         * Parse a term in the parent expression (including multiplication and division).
         *
         * @return the parsed term
         */
        @NotNull
        public MathExpression parseTerm() {
            MathExpression x = parseFactor();

            while (true) {
                if (eat('*')) { // multiplication
                    MathExpression a = x, b = parseFactor();
                    x = (variables -> a.evaluate(variables) * b.evaluate(variables));
                }
                else if (eat('/')) { // division
                    MathExpression a = x, b = parseFactor();
                    x = (variables -> a.evaluate(variables) / b.evaluate(variables));
                }
                else {
                    return x;
                }
            }
        }

        /**
         * Parse a factor in the parent expression (including addition, subtraction,
         * parentheses and injected operation functions).
         *
         * @return the parsed factor
         */
        @NotNull
        public MathExpression parseFactor() {
            if (eat('+')) {
                return parseFactor(); // unary plus
            }

            if (eat('-')) {
                return variables -> -parseFactor().evaluate(variables); // unary minus
            }

            MathExpression x;
            int startPos = pos;

            if (eat('(')) { // parentheses
                x = parseExpression();
                this.eat(')');
            }
            else if ((ch >= '0' && ch <= '9') || ch == '.') { // numbers
                while ((ch >= '0' && ch <= '9') || ch == '.') {
                    this.nextChar();
                }

                String value = expression.substring(startPos, this.pos);
                x = (variables -> Double.parseDouble(value));
            }
            else if (ch >= 'a' && ch <= 'z') { // functions
                while (ch >= 'a' && ch <= 'z') {
                    this.nextChar();
                }

                String function = expression.substring(startPos, this.pos);

                if (OPERATORS.containsKey(function)) {
                    DoubleUnaryOperator operand = OPERATORS.get(function);
                    MathExpression a = this.parseFactor();
                    x = (variables -> operand.applyAsDouble(a.evaluate(variables)));
                }
                else {
                    x = (variables -> variables.get(function, 0.0));
                }
            }
            else {
                throw new ArithmeticException("Unexpected: \"" + ch + "\"");
            }

            if (eat('^')) { // exponentiation
                MathExpression a = x, b = parseFactor();
                x = (variables -> Math.pow(a.evaluate(variables), b.evaluate(variables)));
            }

            return x;
        }
    }

}
