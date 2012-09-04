package testbed.interfaces;

/**
 * A parent interface for metrics. This interface provides a basis for the
 * {@link RankingMetric} and the {@link UtilityMetric}.
 * 
 * @author David
 * 
 */
public interface Metric {

    /**
     * Initializes the metric with optional parameters.
     * 
     * @param params
     *            Optional parameters
     */
    public void initialize(Object... params);

    /**
     * Returns {@link ParametersPanel} instance, which defines GUI for setting
     * parameters for this metric.
     * 
     * If the metric needs no parameters, this method should return null.
     * 
     * @return Instance of the {@link ParametersPanel} or null if parameters
     *         are not needed.
     */
    public ParametersPanel getParametersPanel();
}
