package wtf.choco.dragoneggdrop.utils.math;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.DoubleUnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.math.NumberUtils;

/**
 * A powerful utility class to assist in the parsing and evaluation of arithmetic
 * functions with custom variables and operations through a recursive algorithm.
 *
 * @author Parker Hawke - Choco
 */
public final class MathUtils {

    /*
     * This code is loosely based off of and modified from an original thread on
     * Stackoverflow's forums:
     * http://stackoverflow.com/questions/40975678/evaluating-a-math-expression-
     * with-variables-java-8 Modifications include: - Allow for custom arithmetic
     * functions - Variables included in the function - Java 8 functionality &
     * Object-Oriented format
     */

    private static final Pattern TIME_PATTERN = Pattern.compile("(\\d+)([wdhms])");
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

    private MathUtils() {}

    /**
     * Evaluate a basic mathematical expression.
     *
     * @param expression the string to parse
     *
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
        if (OPERATORS.containsKey(functionName)) {
            return false;
        }

        OPERATORS.put(functionName, operator);
        return true;
    }

    /**
     * Parse a timestamp value (i.e. 1w2d3h4m5s) and return its value in seconds.
     *
     * @param value the value to parse
     * @param defaultSeconds the value to return if "value" is null (i.e. from a config)
     *
     * @return the amount of time in seconds represented by the supplied value
     */
    public static int parseRespawnSeconds(String value, int defaultSeconds) {
        return (value != null) ? parseRespawnSeconds(value) : defaultSeconds;
    }

    /**
     * Parse a timestamp value (i.e. 1w2d3h4m5s) and return its value in seconds.
     *
     * @param value the value to parse
     *
     * @return the amount of time in seconds represented by the supplied value
     */
    public static int parseRespawnSeconds(String value) {
        // Handle legacy (i.e. no timestamps... for example, just "600")
        int legacyTime = NumberUtils.toInt(value, -1);
        if (legacyTime != -1) {
            return legacyTime;
        }

        int seconds = 0;

        Matcher matcher = TIME_PATTERN.matcher(value);
        while (matcher.find()) {
            int amount = NumberUtils.toInt(matcher.group(1));

            switch (matcher.group(2)) {
                case "w":
                    seconds += TimeUnit.DAYS.toSeconds(amount * 7);
                    break;
                case "d":
                    seconds += TimeUnit.DAYS.toSeconds(amount);
                    break;
                case "h":
                    seconds += TimeUnit.HOURS.toSeconds(amount);
                    break;
                case "m":
                    seconds += TimeUnit.MINUTES.toSeconds(amount);
                    break;
                case "s":
                    seconds += amount;
                    break;
            }
        }

        return seconds;
    }

    /**
     * Get a formatted time String from a time in seconds. Formatted time should be in the
     * format, "x hours, y minutes, z seconds". Alternatively, if time is 0, "now", or
     * "invalid seconds" otherwise.
     *
     * @param timeInSeconds the time in seconds
     * @param condensed whether or not to condense time units (i.e. hours {@literal ->} h)
     * @param omitions the time units to omit
     *
     * @return the formatted time
     */
    public static String getFormattedTime(int timeInSeconds, boolean condensed, TimeUnit... omitions) {
        if (timeInSeconds <= 0) {
            return (timeInSeconds == 0) ? "now" : "invalid seconds";
        }

        Set<TimeUnit> omitionsSet = EnumSet.noneOf(TimeUnit.class);
        if (omitions != null) {
            for (TimeUnit unit : omitions) {
                omitionsSet.add(unit);
            }
        }

        StringBuilder resultBuilder = new StringBuilder();

        if (timeInSeconds >= 604800) { // Weeks
            MathUtils.appendAndSeparate(resultBuilder, (int) Math.floor(timeInSeconds / 604800), "week", condensed);
            timeInSeconds %= 604800;
        }

        if (timeInSeconds >= 86400) { // Days
            if (!omitionsSet.contains(TimeUnit.DAYS)) {
                MathUtils.appendAndSeparate(resultBuilder, (int) Math.floor(timeInSeconds / 86400), "day", condensed);
            }

            timeInSeconds %= 86400;
        }

        if (timeInSeconds >= 3600) { // Hours
            if (!omitionsSet.contains(TimeUnit.HOURS)) {
                MathUtils.appendAndSeparate(resultBuilder, (int) Math.floor(timeInSeconds / 3600), "hour", condensed);
            }

            timeInSeconds %= 3600;
        }

        if (timeInSeconds >= 60) { // Minutes
            if (!omitionsSet.contains(TimeUnit.MINUTES)) {
                MathUtils.appendAndSeparate(resultBuilder, (int) Math.floor(timeInSeconds / 60), "minute", condensed);
            }

            timeInSeconds %= 60;
        }

        if (!omitionsSet.contains(TimeUnit.SECONDS) && timeInSeconds > 0) { // Seconds
            MathUtils.appendAndSeparate(resultBuilder, timeInSeconds, "second", condensed);
        }

        String result = resultBuilder.toString().trim();
        if (result.endsWith(",")) {
            result = result.substring(0, result.length() - 1);
        }

        if (result.isEmpty()) {
            result = "soon";
        }

        return result;
    }

    private static void appendAndSeparate(StringBuilder builder, int value, String toAppend, boolean condensed) {
        builder.append(value).append(' ').append(condensed ? toAppend.charAt(0) : toAppend);

        if (!condensed && value > 1) {
            builder.append('s');
        }

        builder.append(", ");
    }

    /**
     * Clamp a value between a minimum and maximum value.
     *
     * @param value the value to clamp
     * @param min the minimum value (inclusive)
     * @param max the maximum value (inclusive)
     *
     * @return the clamped value
     */
    public static double clamp(double value, double min, double max) {
        return (value < min ? min : (value > max ? max : value));
    }

    /**
     * The logic behind the parsing of {@link MathExpression} objects.
     *
     * @author Parker Hawke - Choco
     */
    private static class ExpressionEvaluator {

        private int pos = -1, ch;

        private final String expression;

        public ExpressionEvaluator(String expression) {
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
