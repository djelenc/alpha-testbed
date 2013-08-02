/*
 * Copyright (c) 2013 David Jelenc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     David Jelenc - initial API and implementation
 */
package testbed.common;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import testbed.interfaces.ParameterCondition;

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
    public static <T> T extractParameter(ParameterCondition<T> functor,
	    int index, Object[] params) throws IllegalArgumentException {
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
     * Converts the given map and to a {@link TreeMap} instance, thus enforcing
     * a deterministic iteration through it. If the given map is already a
     * {@link TreeMap} instance, the function returns the given map.
     * 
     * @param input
     *            Input map
     * @return Input map implemented as a {@link TreeMap}
     */
    public static <T, K> Map<T, K> ordered(Map<T, K> input) {
	if (input instanceof TreeMap) {
	    return (TreeMap<T, K>) input;
	} else {
	    final Map<T, K> output = new TreeMap<T, K>();
	    output.putAll(input);
	    return output;
	}
    }

    /**
     * Converts the set and to a {@link TreeSet} instance, thus enforcing a
     * deterministic iteration through it. If the given set is already a
     * {@link TreeSet} instance, the function returns the given set.
     * 
     * @param input
     *            Input set
     * @return Input set implemented as a {@link TreeSet}
     */
    public static <T> TreeSet<T> ordered(Set<T> input) {
	if (input instanceof TreeSet) {
	    return (TreeSet<T>) input;
	} else {
	    final TreeSet<T> output = new TreeSet<T>();
	    output.addAll(input);
	    return output;
	}
    }
}
