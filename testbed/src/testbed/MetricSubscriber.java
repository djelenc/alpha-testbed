package testbed;

/**
 * Interface for metric subscribers.
 * 
 * <p>
 * The instance of the {@link AlphaTestbed} notifies all instances of classes
 * that implement this interface that the evaluation step is complete and that
 * they can retrieve data from the {@link AlphaTestbed} instance using
 * {@link AlphaTestbed#getMetric(int, testbed.interfaces.IMetric)} method.
 * 
 * @author David
 * 
 */
public interface MetricSubscriber {

    /**
     * This method is called at the end of each time step to notify the
     * subscriber that the test-bed is ready to serve new data.
     * 
     * <p>
     * The data has to be pulled from the instance (provided in the argument)
     * using {@link AlphaTestbed#getMetric(int, testbed.interfaces.IMetric)}
     * method.
     * 
     * @param instance
     *            A test-bed instance that performs the evaluation
     */
    public void update(AlphaTestbed instance);
}
