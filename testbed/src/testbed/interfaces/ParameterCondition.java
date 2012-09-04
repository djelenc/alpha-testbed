package testbed.interfaces;

/**
 * Interface for functor instances to perform validation on the varargs
 * 
 * @author David
 * 
 * @param <T>
 *            Parameter type
 */
public interface ParameterCondition<T> {
    /**
     * If the parameter is valid the method does NOT throw an
     * {@link IllegalArgumentException}. If the parameter is invalid the method
     * throws an {@link IllegalArgumentException}.
     * 
     * @param var
     */
    public void eval(T var) throws IllegalArgumentException;
}
