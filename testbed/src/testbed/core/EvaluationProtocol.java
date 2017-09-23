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
package testbed.core;

import static testbed.core.AlphaTestbed.SCENARIOS;
import static testbed.core.AlphaTestbed.TRUST_MODELS;

import java.util.List;
import java.util.Map;
import java.util.Set;

import testbed.interfaces.Metric;
import testbed.interfaces.Scenario;
import testbed.interfaces.TrustModel;

/**
 * The base class for all evaluation protocols. Evaluation protocols have to be
 * implemented in classes that extend this class. They have to provide
 * implementations. that are specific to each evaluation protocol.
 * 
 * <p>
 * The protocol publishes results of the evaluation by using subscribers.
 * Subscribers are instances of classes that implement the
 * {@link MetricSubscriber} interface.
 * 
 * <p>
 * The results are published in the following manner. First, the subscribers
 * have to subscribe to the testbed by using
 * {@link #subscribe(MetricSubscriber)} method. Second, at the end of each
 * evaluation step, the testbed notifies all subscribers by invoking their
 * {@link MetricSubscriber#update(AlphaTestbed)} methods. Finally, the
 * subscribers are expected to pull the results of the evaluation via the
 * {@link #getMetric(int, Metric)} method.
 * 
 * @author David
 * 
 */
public abstract class EvaluationProtocol {

    /** Error message for invalid result query */
    protected static final String INVALID_QUERY;

    static {
	INVALID_QUERY = "Invalid query for metric '%s' and service '%d'.";
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
    protected abstract void evaluationStep(int time);

    /**
     * Returns the trust model instance.
     * 
     * @return
     */
    public abstract TrustModel<?> getTrustModel();

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
     * Returns the set of required metric classes that have to be given to the
     * evaluation protocol.
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
    public final void step(int time) {
	// perform protocol step
	evaluationStep(time);

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
    public final boolean validParameters(TrustModel<?> tm, Scenario scn,
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
    protected final boolean validTrustModelClasses(Class<?> tm) {
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
    protected final boolean validScenarioClasses(Class<?> scn) {
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
    protected final boolean validMetricClasses(
	    Set<? extends Metric> givenMetrics) {
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
    protected final boolean containsASubInstance(
	    Class<? extends Metric> metricClass,
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
    public final void subscribe(MetricSubscriber observer) {
	subscribers.add(observer);
    }

    /**
     * Removes the subscriber from the list of subscribers that the test-bed
     * notifies at the end of each run.
     * 
     * @param subscriber
     *            The instance to be removed.
     */
    public final void remove(MetricSubscriber subscriber) {
	subscribers.remove(subscriber);
    }

    /**
     * Notifies the subscribers that the new data is ready to be pulled from the
     * test-bed.
     */
    protected final void notifiySubscribers() {
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
    public final double getResult(int service, Metric metric) {
	final Double result;
	result = results.get(metric.getClass().hashCode() ^ service);

	if (null == result) {
	    throw new IllegalArgumentException(
		    String.format(INVALID_QUERY, metric, service));
	} else {
	    return result;
	}
    }
}
