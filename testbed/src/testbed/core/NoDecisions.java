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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import testbed.interfaces.Accuracy;
import testbed.interfaces.Experience;
import testbed.interfaces.Metric;
import testbed.interfaces.Opinion;
import testbed.interfaces.OpinionObject;
import testbed.interfaces.Scenario;
import testbed.interfaces.TrustModel;

/**
 * An evaluation protocol, where a scenario determines both the opinion
 * providers and the interaction partners.
 * 
 * <p>
 * The evaluation protocol measures the accuracy of the computed trust values.
 * The execution flow of the evaluation protocol is the following:
 * <ol>
 * <li>The testbed sets time in the scenario.
 * <li>The testbed sets time in the trust model.
 * <li>The testbed instructs the scenario to list all available services.
 * <li>The testbed conveys the list of available services to the trust model.
 * <li>The testbed instructs the scenario to list all available agents.
 * <li>The testbed conveys the list of available agents to the trust model.
 * <li>The testbed instructs the scenario to generate opinions.
 * <li>The testbed conveys generated opinions to the trust model.
 * <li>The testbed instructs the scenario to generate experiences tuples for
 * agents that the trust model requested.
 * <li>The testbed conveys generated experiences to the trust model.
 * <li>The testbed instructs the trust model to evaluate trust.
 * <li>The testbed instructs the trust model to compute rankings of agents.
 * <li>The testbed conveys estimated trust to accuracy metric that then
 * evaluates its accuracy.
 * </ol>
 * 
 * @author David
 */
public class NoDecisions extends EvaluationProtocol {

    /** Error message for creating metrics */
    protected static final String CREATION_ERROR;

    static {
	CREATION_ERROR = "Could not instantiate metric '%s' using parameters %s.";
    }

    /** Trust model */
    private TrustModel<?, Opinion> trustModel = null;

    /** Scenario */
    private Scenario<Opinion> scenario = null;

    /** Class of accuracy instances */
    protected Class<? extends Accuracy> accuracyClass = null;

    /** Parameters for accuracy metrics */
    protected Object[] accuracyParameters = null;

    /** Map of service -> accuracy metrics */
    protected Map<Integer, Accuracy> serviceAccuracy = null;

    @SuppressWarnings("unchecked")
    @Override
    public void initialize(TrustModel<?, ?> tm,
	    Scenario<? extends OpinionObject> scn,
	    Map<? extends Metric, Object[]> metrics) {

	if (!validTrustModelClasses(tm.getClass())) {
	    throw new IllegalArgumentException("Invalid trust model.");
	}

	trustModel = (TrustModel<?, Opinion>) tm;

	if (!validScenarioClasses(scn.getClass())) {
	    throw new IllegalArgumentException("Invalid scenario.");
	}

	// TODO: Assure this does not fail
	scenario = (Scenario<Opinion>) scn;

	if (!validMetricClasses(metrics.keySet())) {
	    throw new IllegalArgumentException("Invalid metrics.");
	}

	for (Entry<? extends Metric, Object[]> e : metrics.entrySet()) {
	    accuracyClass = (Class<? extends Accuracy>) e.getKey().getClass();
	    accuracyParameters = e.getValue();
	    serviceAccuracy = new HashMap<Integer, Accuracy>();
	}

	subscribers = new ArrayList<MetricSubscriber>();
	results = new HashMap<Integer, Double>();
    }

    @Override
    protected void evaluationStep(int time) {
	// convey current time
	trustModel.setCurrentTime(time);
	scenario.setCurrentTime(time);

	// get all services
	final List<Integer> services = scenario.getServices();

	// convey services
	trustModel.setServices(services);

	// get all agents
	final List<Integer> agents = scenario.getAgents();

	// convey agents
	trustModel.setAgents(agents);

	// get opinions
	final List<Opinion> opinions = scenario.generateOpinions();

	// convey opinions
	trustModel.processOpinions(opinions);

	// generate experiences
	final List<Experience> experiences = scenario.generateExperiences();

	// convey experiences
	trustModel.processExperiences(experiences);

	// calculate trust
	trustModel.calculateTrust();

	// evaluation
	for (int service : services) {
	    final Map<Integer, Double> capabilities;
	    capabilities = scenario.getCapabilities(service);

	    // accuracy
	    final Accuracy accuracy = getAccuracyInstance(service);
	    final int accKey = accuracy.getClass().hashCode() ^ service;
	    final double accValue = accuracy
		    .evaluate(trustModel.getTrust(service), capabilities);

	    results.put(accKey, accValue);
	}
    }

    /**
     * Returns an instance of the ranking metric for the given service.
     * 
     * <p>
     * Method retrieves the instance from the the map of all ranking metric
     * instances. If for the given service, the map does not contain a metric
     * instance, the method creates a new instance, initializes it with
     * parameters, adds instance to the map and returns the instance.
     * 
     * @param service
     *            Type of service
     * @return An instance of the metric
     */
    protected Accuracy getAccuracyInstance(int service) {
	Accuracy metric = serviceAccuracy.get(service);

	if (null == metric) {
	    try {
		metric = (Accuracy) accuracyClass.newInstance();
		metric.initialize(accuracyParameters);
		serviceAccuracy.put(service, metric);
	    } catch (Exception e) {
		throw new Error(String.format(CREATION_ERROR, accuracyClass,
			Arrays.toString(accuracyParameters)));
	    }
	}

	return metric;
    }

    @Override
    public TrustModel<?, ?> getTrustModel() {
	return trustModel;
    }

    @Override
    public Scenario<? extends OpinionObject> getScenario() {
	return scenario;
    }

    @Override
    protected Set<Class<?>> requiredScenarioClasses() {
	final Set<Class<?>> required = new HashSet<Class<?>>();
	required.add(Scenario.class);
	return required;
    }

    @Override
    protected Set<Class<?>> requiredTrustModelClasses() {
	final Set<Class<?>> required = new HashSet<Class<?>>();
	required.add(TrustModel.class);
	return required;
    }

    @Override
    protected Set<Class<? extends Metric>> requiredMetricClasses() {
	final Set<Class<? extends Metric>> required = new HashSet<Class<? extends Metric>>();
	required.add(Accuracy.class);
	return required;
    }
}
