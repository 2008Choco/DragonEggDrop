package wtf.choco.dragoneggdrop.utils.function;

/**
 * Represents a Provider capable of returning a primitive double.
 *
 * @param <T> the input type
 *
 * @author Parker Hawke - Choco
 */
@FunctionalInterface
public interface DoubleProvider<T> {

    /**
     * Applies this provider to supply a primitive double.
     *
     * @param t the input value
     *
     * @return the double
     */
    public double get(T t);

}
