package testbed;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import testbed.interfaces.Accuracy;
import testbed.interfaces.InteractionPartnerSelection;
import testbed.interfaces.Metric;
import testbed.interfaces.OpinionCost;
import testbed.interfaces.OpinionProviderSelection;
import testbed.interfaces.Scenario;
import testbed.interfaces.SelectingInteractionPartners;
import testbed.interfaces.SelectingOpinionProviders;
import testbed.interfaces.TrustModel;
import testbed.interfaces.Utility;

public abstract class EvaluationProtocol {

    /** Set of available trust model interfaces */
    protected static final Set<Class<?>> TRUST_MODELS;

    /** Set of available scenario interfaces */
    protected static final Set<Class<?>> SCENARIOS;

    /** Set of available metric interfaces */
    protected static final Set<Class<?>> METRICS;

    /** Error message for invalid result query */
    protected static final String INVALID_QUERY;

    static {
	INVALID_QUERY = "Invalid query for metric '%s' and service '%d'.";

	TRUST_MODELS = new HashSet<Class<?>>();
	SCENARIOS = new HashSet<Class<?>>();
	METRICS = new HashSet<Class<?>>();

	// ALL AVAILABLE TRUST MODEL INTERFACES
	TRUST_MODELS.add(TrustModel.class);
	TRUST_MODELS.add(SelectingInteractionPartners.class);
	TRUST_MODELS.add(SelectingOpinionProviders.class);

	// ALL AVAILABLE SCENARIO INTERFACES
	SCENARIOS.add(Scenario.class);
	SCENARIOS.add(InteractionPartnerSelection.class);
	SCENARIOS.add(OpinionProviderSelection.class);

	// ALL AVAILABLE METRIC INTERFACES
	METRICS.add(Accuracy.class);
	METRICS.add(Utility.class);
	METRICS.add(OpinionCost.class);
    }

    /**
     * Temporary variable that holds current results; keys represent metric and
     * service (obtained by XORing metric class with service number) and values
     * represent the metric value for that service
     */
    protected Map<Integer, Double> results;

    /** Subscribers to this evaluation run */
    protected List<MetricSubscriber> subscribers;

    /**
     * Initializes the evaluation scenario.
     * 
     * @param tm
     *            Trust model insance
     * @param scn
     *            Scenario instance
     * @param metrics
     *            Map of metrics, where keys represent metric classes and values
     *            their parameters.
     */
    public abstract void initialize(TrustModel<?> tm, Scenario scn,
	    Map<? extends Metric, Object[]> metrics);

    /**
     * Implementation of the evaluation step.
     * 
     * @param time
     *            Current time
     */
    protected abstract void evaluationlStep(int time);

    /**
     * Returns the trust model instance.
     * 
     * @return
     */
    public abstract TrustModel<?> getModel();

    /**
     * Returns the scenario instance.
     * 
     * @return
     */
    public abstract Scenario getScenario();

    /**
     * Returns the set of interfaces that have to be implemented by the
     * scenario.
     * 
     * @return
     */
    protected abstract Set<Class<?>> requiredScenarioClasses();

    /**
     * Returns the set of interfaces that have to be implemented by the trust
     * model.
     * 
     * @return
     */
    protected abstract Set<Class<?>> requiredTrustModelClasses();

    /**
     * Returns the set of required metric classes.
     * 
     * @return
     */
    protected abstract Set<Class<? extends Metric>> requiredMetricClasses();

    /**
     * Performs one step of the evaluation protocol. At the end, all subscribers
     * are notified.
     * 
     * @param time
     *            Current time of the evaluation.
     */
    public void step(int time) {
	// perform protocol step
	evaluationlStep(time);

	// notify subscribers
	notifiySubscribers();
    }

    /**
     * Return true if the combination of given trust model, scenario and given
     * metrics is valid in this evaluation protocol.
     * 
     * @param tm
     *            Trust model instance
     * @param scn
     *            Scenario instance
     * @param metrics
     *            Map of metrics and their parameters
     * @return True if the combination is valid
     */
    public boolean validParameters(TrustModel<?> tm, Scenario scn,
	    Map<? extends Metric, Object[]> metrics) {
	return validTrustModelClasses(tm.getClass())
		&& validScenarioClasses(scn.getClass())
		&& validMetricClasses(metrics.keySet());
    }

    /**
     * Checks whether given trust model can be evaluated in this protocol.
     * 
     * @param tm
     *            Given trust model.
     * @return True on success.
     */
    protected boolean validTrustModelClasses(Class<?> tm) {
	final Set<Class<?>> required = requiredTrustModelClasses();

	for (Class<?> clazz : TRUST_MODELS) {
	    if (required.contains(clazz)) {
		if (!clazz.isAssignableFrom(tm))
		    return false;
	    } else {
		if (clazz.isAssignableFrom(tm))
		    return false;
	    }
	}

	return true;
    }

    /**
     * Checks whether given scenario can be used in this protocol.
     * 
     * @param scn
     *            Given scenario.
     * @return true on success.
     */
    protected boolean validScenarioClasses(Class<?> scn) {
	final Set<Class<?>> required = requiredScenarioClasses();

	for (Class<?> clazz : SCENARIOS) {
	    if (required.contains(clazz)) {
		if (!clazz.isAssignableFrom(scn))
		    return false;
	    } else {
		if (clazz.isAssignableFrom(scn))
		    return false;
	    }
	}

	return true;
    }

    /**
     * Checks whether set of given metric classes are compatible with the
     * evaluation protocol.
     * 
     * @param givenMetrics
     *            Set of given metrics.
     * @return True when metrics are compatible.
     */
    protected boolean validMetricClasses(Set<? extends Metric> givenMetrics) {
	final Set<Class<? extends Metric>> requiredMetrics = requiredMetricClasses();

	if (givenMetrics.size() != requiredMetrics.size()) {
	    return false;
	}

	for (Metric metric : givenMetrics) {
	    if (!containsASubInstance(metric.getClass(), requiredMetrics)) {
		return false;
	    }
	}

	return true;
    }

    /**
     * A helper method that checks whether given metric class is contained in
     * the set of given metric classes. Method is needed, because a metric that
     * is a sub-type of a required class is also valid.
     * 
     * @param metricClass
     *            Class of given metric.
     * @param requiredMetrics
     *            Set of required metric classes.
     * @return
     */
    protected boolean containsASubInstance(Class<? extends Metric> metricClass,
	    Set<Class<? extends Metric>> requiredMetrics) {
	for (Class<?> clazz : requiredMetrics) {
	    if (clazz.isAssignableFrom(metricClass)) {
		return true;
	    }
	}

	return false;
    }

    /**
     * Subscribe to the notifications of the test-bed. The data has to be pulled
     * from the test-bed instance using {@link #getResult(int, Metric)} method.
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
	    s.update(this);
	}
    }

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
    public double getResult(int service, Metric metric) {
	final Double result;
	result = results.get(metric.getClass().hashCode() ^ service);

	if (null == result) {
	    throw new IllegalArgumentException(String.format(INVALID_QUERY,
		    metric, service));
	} else {
	    return result;
	}
    }
}
