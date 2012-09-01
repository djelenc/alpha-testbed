package testbed.interfaces;

/**
 * A parent interface for metrics. This interface provides a basis for the
 * {@link IRankingMetric} and the {@link IUtilityMetric}.
 * 
 * @author David
 * 
 */
public interface IMetric {

    /**
     * Initializes the metric with optional parameters.
     * 
     * @param params
     *            Optional parameters
     */
    public void initialize(Object... params);

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
