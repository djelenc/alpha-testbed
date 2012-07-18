package testbed.interfaces;

import java.util.Map;

/**
 * Interface for metrics that evaluate the obtained utility during a test run.
 * 
 * @author David
 * 
 */
public interface IUtilityMetric {

    /**
     * Initializes the metric with optional parameters.
     * 
     * @param params
     *            Optional parameters
     */
    public void initialize(Object... params);

    /**
     * Returns the normalized cumulative utility that has been obtained so far.
     * 
     * @param capabilities
     *            A map of capabilities, where agents' ID numbers are used for
     *            keys and their respective capabilities as values
     * 
     * @param agent
     *            Agent with who Alpha made an interaction.
     * 
     * @return An evaluation score between 0 and 1, inclusively.
     */
    public double evaluate(Map<Integer, Double> capabilities, int agent);

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
