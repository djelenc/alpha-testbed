package testbed.common;

import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;

import repast.simphony.random.RandomHelper;
import testbed.interfaces.ICondition;
import cern.jet.random.Normal;

public class Utils {
    private static final String NL = System.getProperty("line.separator");
    private static Normal normal = null;

    /**
     * Random generator with PDF of a Truncated normal distribution with given
     * mean and standard deviation. The value falls under [0, 1].
     * 
     * @see <a href=
     *      'http://en.wikipedia.org/wiki/Truncated_normal_distribution'>
     *      Truncated normal distribution</a>
     * 
     * @param mean
     *            Mean value
     * @param sd
     *            Standard deviation
     * @return random number
     */
    public static double randomTND(double mean, double sd) {
	normal = RandomHelper.getNormal();

	if (normal == null) { // check needed when re-initializing simulation
	    normal = RandomHelper.createNormal(0, 0);
	}

	double number;

	do {
	    number = normal.nextDouble(mean, sd);
	} while (number > 1 || number < 0);

	return number;
    }

    /**
     * Random generator with PDF of an uniform distribution on (min, max).
     * 
     * @param min
     *            Minimum value (exclusively)
     * @param max
     *            Maximum value (exclusively)
     * @return Generated random value
     */
    public static double randomUnif(double min, double max) {
	return RandomHelper.nextDoubleFromTo(min, max);
    }

    /**
     * Random generator of integers with uniform distribution between [min, max]
     * 
     * @param min
     * @param max
     * @return random index
     */
    public static int randomUnifIndex(int min, int max) {
	return RandomHelper.nextIntFromTo(min, max);
    }

    /**
     * Returns a random element from the provided probability mass function.
     * 
     * <p>
     * To warrant deterministic behavior, the given {@link Map} must be ordered,
     * by using {@link TreeMap} implementations.
     * 
     * @param pmf
     *            Probability mass function of possible outcomes expressed as a
     *            {@link Map}
     * @return A random element
     */
    public static <T> T randomFromWeights(Map<T, Double> pmf) {
	if (pmf == null || pmf.isEmpty()) {
	    return null;
	}

	double rnd = randomUnif(0, 1), weight = 0;

	for (Map.Entry<T, Double> e : pmf.entrySet()) {
	    weight += e.getValue();

	    if (weight > rnd) {
		return e.getKey();
	    }
	}

	throw new Error("This part of code should be unreachable!");
    }

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
    public static Map<Integer, Integer> orderedMap(Map<Integer, Integer> input) {
	final Map<Integer, Integer> output = new TreeMap<Integer, Integer>();
	output.putAll(input);

	return output;
    }
}
