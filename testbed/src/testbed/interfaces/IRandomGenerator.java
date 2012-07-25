package testbed.interfaces;

import java.util.TreeMap;

/**
 * Interface for random number generators.
 * 
 * @author David
 * 
 */
public interface IRandomGenerator {

    /**
     * Returns a random number generated from a Truncated normal distribution
     * with given mean and standard deviation. The value falls under [0, 1].
     * 
     * @see <a href=
     *      'http://en.wikipedia.org/wiki/Truncated_normal_distribution'>
     *      Truncated normal distribution</a>
     * 
     * @param mean
     *            Mean value
     * @param sd
     * 
     *            Standard deviation
     * @return random number
     */
    public double randomTND(double mean, double sd);

    /**
     * Returns a random number from an uniform distribution from (min, max).
     * 
     * @param min
     *            Minimum value (exclusively)
     * @param max
     *            Maximum value (exclusively)
     * @return Generated random number
     */
    public double randomUnif(double min, double max);

    /**
     * Returns a random integer from an uniform distribution between [min, max].
     * Useful for selecting a random index.
     * 
     * @param min
     *            Minimum value (inclusively)
     * @param max
     *            Maximum value (inclusively)
     * @return random index
     */
    public int randomUnifIndex(int min, int max);

    /**
     * Returns a random element from the set of keys in the provided Map. The
     * map thus represents a probability mass functions of elements (keys).
     * 
     * <p>
     * To warrant a deterministic behavior, the distribution must be given as a
     * {@link TreeMap}.
     * 
     * @param pmf
     *            Probability mass function of possible outcomes expressed as a
     *            {@link TreeMap}
     * @return A random element
     */
    public <T> T randomFromWeights(TreeMap<T, Double> pmf);

}