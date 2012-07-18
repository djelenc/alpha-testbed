package testbed.interfaces;

import java.util.Map;

/**
 * Interface for metrics that evaluate the correctness of rankings.
 * 
 * @author David
 * 
 */
public interface IRankingMetric {

    /**
     * Initializes the metric with optional parameters.
     * 
     * @param params
     *            Optional parameters
     */
    public void initialize(Object... params);

    /**
     * Evaluates given rankings against given capabilities.
     * 
     * @param rankings
     *            A map of rankings, where agents' ID numbers are used for keys
     *            and their respective ranks as values
     * @param capabilities
     *            A map of capabilities, where agents' ID numbers are used for
     *            keys and their respective capabilities as values
     * 
     * @return An evaluation score between 0 and 1, inclusively.
     */
    public double evaluate(Map<Integer, Integer> rankings,
	    Map<Integer, Double> capabilities);

    /**
     * Returns a friendly name for the metric
     * 
     * @return
     */
    public String getName();

    /**
     * Returns {@link IParametersPanel} instance, which defines GUI for setting
     * parameters for this metric.
     * 
     * If the metric needs no parameters, this method should return null.
     * 
     * @return Instance of the {@link IParametersPanel} or null if parameters
     *         are not needed.
     */
    public IParametersPanel getParametersPanel();
}
