package testbed.common;

import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;

import testbed.interfaces.ICondition;

public class Utils {
    private static final String NL = System.getProperty("line.separator");

    /**
     * Extracts a value from a given array of objects at given index and casts
     * it to a desired type. Then the method semantically validates the value by
     * calling the functor given as the first argument.
     * 
     * @param functor
     *            The functor object to perform semantical validation
     * @param index
     *            The index of the desired parameter
     * @param params
     *            The array of parameters
     * @return
     * @throws IllegalArgumentException
     *             If any errors occur during execution or when the passed in
     *             parameters are invalid
     */
    public static <T> T extractParameter(ICondition<T> functor, int index,
	    Object[] params) throws IllegalArgumentException {
	try {
	    @SuppressWarnings("unchecked")
	    T var = (T) params[index];

	    // if incorrect an IllegalArgumentException is thrown
	    functor.eval(var);

	    return var;
	} catch (Exception ex) {
	    throw new IllegalArgumentException(String.format(
		    "Cannot get parameter at location %d from %s. Reason: %s",
		    index, Arrays.toString(params), ex));
	}
    }

    /**
     * An utility method to represent a matrix of doubles as a string
     * 
     * @param m
     *            Matrix of doubles
     * @return String representation of a matrix
     */
    public static String printMatrix(double[][] m) {
	StringBuffer s = new StringBuffer();

	for (int row = 0; row < m.length; row++) {
	    for (int col = 0; col < m[row].length; col++)
		s.append(String.format("%.2f\t", m[row][col]));

	    s.append(NL);
	}

	return s.toString();
    }

    /**
     * An utility method to represent a vector of doubles as a string
     * 
     * @param v
     *            Vector of doubles
     * @return String representation of a vector
     */
    public static String printVector(double[] v) {
	StringBuffer s = new StringBuffer();

	for (int col = 0; col < v.length; col++)
	    s.append(String.format("%.2f\t", v[col]));

	return s.toString();
    }

    /**
     * Takes a map and converts it to a tree map, thus enforcing a deterministic
     * iteration through it.
     * 
     * @param input
     *            Input map
     * @return Input map implemented as a tree map
     */
    public static <T, K> Map<T, K> orderedMap(Map<T, K> input) {
	final Map<T, K> output = new TreeMap<T, K>();
	output.putAll(input);

	return output;
    }
}
