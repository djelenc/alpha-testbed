package testbed;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import testbed.interfaces.Accuracy;
import testbed.interfaces.Metric;
import testbed.interfaces.Scenario;
import testbed.interfaces.TrustModel;

public abstract class EvaluationProtocol {
    protected static final String CREATION_ERROR = "Could not instantiate metric '%s' using parameters %s.";
    protected static final String INCOMPATIBLE_EX = "Trust model '%s' cannot be tested with scenario '%s'.";
    protected static final String METRIC_QUERY_EX = "Invalid query for metric '%s' and service '%d'.";

    /**
     * Temporary variable that holds current results; keys represent metric and
     * service (obtained by XORing metric class with service number) and values
     * represent the metric value for that service
     */
    protected Map<Integer, Double> score;

    /** Subscribers to this evaluation run */
    protected List<MetricSubscriber> subscribers;

    public abstract void initialize(Object... params);

    public abstract void step(int time);

    /**
     * Returns the value of the given metric for the given service. This method
     * should be called by all instances of {@link MetricSubscriber} to pull the
     * data from the testbed.
     * 
     * @param service
     *            The type of service
     * @param metric
     *            The metric
     * @return The evaluation result
     */
    public double getMetric(int service, Metric metric) {
	final Double result = score.get(metric.getClass().hashCode() ^ service);

	if (null == result) {
	    throw new IllegalArgumentException(String.format(METRIC_QUERY_EX,
		    metric, service));
	} else {
	    return result;
	}
    }

    public abstract TrustModel<?> getModel();

    public abstract Scenario getScenario();

    public Class<? extends EvaluationProtocol> getEvaluationProtocol() {
	return this.getClass();
    }

    /**
     * Returns the set of interfaces that have to be implemented by the
     * evaluation scenario in this evaluation protocol.
     * 
     * <p>
     * It includes {@link Scenario} interface by default.
     * 
     * @return
     */
    protected Set<Class<?>> requiredScenarioImplementations() {
	final Set<Class<?>> classes = new HashSet<Class<?>>();
	classes.add(Scenario.class);

	return classes;
    }

    /**
     * Returns the set of interfaces that have to be implemented by the trust
     * model in this evaluation protocol.
     * 
     * <p>
     * It includes {@link TrustModel} interface by default.
     * 
     * @return
     */
    protected Set<Class<?>> requiredTrustModelImplementations() {
	final Set<Class<?>> classes = new HashSet<Class<?>>();
	classes.add(TrustModel.class);

	return classes;
    }

    /**
     * Returns the set of required metrics in this evaluation protocol.
     * 
     * <p>
     * It includes {@link Accuracy} metric by default.
     * 
     * @return
     */
    protected Set<Class<? extends Metric>> requiredMetrics() {
	final Set<Class<? extends Metric>> classes = new HashSet<Class<? extends Metric>>();
	classes.add(Accuracy.class);

	return classes;
    }

    /**
     * Subscribe to the notifications of the test-bed. The data has to be pulled
     * from the test-bed instance using {@link #getMetric(int, Metric)} method.
     * 
     * @param observer
     *            The instance of the subscriber.
     */
    public void subscribe(MetricSubscriber observer) {
	subscribers.add(observer);
    }

    /**
     * Removes the subscriber from the list of subscribers that the test-bed
     * notifies at the end of each run.
     * 
     * @param subscriber
     *            The instance to be removed.
     */
    public void remove(MetricSubscriber subscriber) {
	subscribers.remove(subscriber);
    }

    /**
     * Notifies the subscribers that the new data is ready to be pulled from the
     * test-bed.
     */
    protected void notifiySubscribers() {
	for (MetricSubscriber s : subscribers) {
	    s.update(null);
	    // FIXME: s.update(this);
	}
    }
}
