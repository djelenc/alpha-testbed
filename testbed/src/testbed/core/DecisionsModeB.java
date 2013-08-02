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
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import testbed.common.Utils;
import testbed.interfaces.Accuracy;
import testbed.interfaces.Experience;
import testbed.interfaces.InteractionPartnerSelection;
import testbed.interfaces.Metric;
import testbed.interfaces.Opinion;
import testbed.interfaces.OpinionCost;
import testbed.interfaces.OpinionProviderSelection;
import testbed.interfaces.OpinionRequest;
import testbed.interfaces.Scenario;
import testbed.interfaces.SelectingInteractionPartners;
import testbed.interfaces.SelectingOpinionProviders;
import testbed.interfaces.TrustModel;
import testbed.interfaces.Utility;

/**
 * An evaluation protocol, where a trust model determines both, the opinion
 * providers and the interaction partners.
 * 
 * <p>
 * Besides measuring accuracy and utility, the protocol also measures the cost
 * that was endured in obtaining required opinions. The execution flow of the
 * evaluation protocol is the following:
 * <ol>
 * <li>The testbed sets time in the scenario.
 * <li>The testbed sets time in the trust model.
 * <li>The testbed instructs the scenario to list all available services.
 * <li>The testbed conveys the list of available services to the trust model.
 * <li>The testbed instructs the scenario to list all available agents.
 * <li>The testbed conveys the list of available agents to the trust model.
 * <li>The testbed instructs the trust model to generate opinion request (to
 * tell who to ask for opinions).
 * <li>The testbed instructs the scenario to generate requested opinions.
 * <li>The testbed conveys generated opinions to the trust model.
 * <li>The testbed instructs the trust model to tell, with whom does it want to
 * interact.
 * <li>The testbed instructs the scenario to generate experiences tuples for
 * agents that the trust model requested.
 * <li>The testbed conveys generated experiences to the trust model.
 * <li>The testbed instructs the trust model to estimate trust.
 * <li>The testbed instructs the trust model to compute rankings of agents.
 * <li>The testbed conveys estimated trust to accuracy metric that then
 * evaluates its accuracy.
 * <li>The testbed conveys selected interaction partner to the utility metric
 * that then evaluates the utility in the interaction.
 * <li>The testbed conveys the opinion requests to the opinion cost metric that
 * then evaluates their costs.
 * </ol>
 * <p>
 * 
 * @author David
 * 
 */
public class DecisionsModeB extends DecisionsModeA {

    private TrustModel<?> tm;
    private SelectingInteractionPartners tmIP;
    private SelectingOpinionProviders tmOP;

    private Scenario scn;
    private InteractionPartnerSelection scnIP;
    private OpinionProviderSelection scnOP;

    protected Class<? extends OpinionCost> ocClass;
    protected Object[] ocParameters;
    protected HashMap<Integer, OpinionCost> serviceOc;

    @SuppressWarnings("unchecked")
    @Override
    public void initialize(TrustModel<?> tm, Scenario scn,
	    Map<? extends Metric, Object[]> metrics) {
	if (!validTrustModelClasses(tm.getClass())) {
	    throw new IllegalArgumentException("Invalid trust model.");
	}

	this.tm = tm;
	this.tmIP = (SelectingInteractionPartners) tm;
	this.tmOP = (SelectingOpinionProviders) tm;

	if (!validScenarioClasses(scn.getClass())) {
	    throw new IllegalArgumentException("Invalid scenario.");
	}

	this.scn = scn;
	this.scnIP = (InteractionPartnerSelection) scn;
	this.scnOP = (OpinionProviderSelection) scn;

	if (!validMetricClasses(metrics.keySet())) {
	    throw new IllegalArgumentException("Invalid metrics.");
	}

	for (Entry<? extends Metric, Object[]> e : metrics.entrySet()) {
	    final Metric metric = e.getKey();
	    final Object[] parameters = e.getValue();

	    if (metric instanceof Utility) {
		utilityClass = (Class<? extends Utility>) metric.getClass();
		utilityParameters = parameters;
		serviceUtility = new HashMap<Integer, Utility>();
	    } else if (metric instanceof OpinionCost) {
		ocClass = (Class<? extends OpinionCost>) metric.getClass();
		ocParameters = parameters;
		serviceOc = new HashMap<Integer, OpinionCost>();
	    } else {
		accuracyClass = (Class<? extends Accuracy>) metric.getClass();
		accuracyParameters = parameters;
		serviceAccuracy = new HashMap<Integer, Accuracy>();
	    }
	}

	subscribers = new ArrayList<MetricSubscriber>();
	results = new HashMap<Integer, Double>();
    }

    @Override
    protected void evaluationStep(int time) {
	// convey current time
	tm.setCurrentTime(time);
	scn.setCurrentTime(time);

	// get all services
	final List<Integer> services = scn.getServices();

	// convey services
	tm.setServices(services);

	// get all agents
	final List<Integer> agents = scn.getAgents();

	// convey agents
	tm.setAgents(agents);

	// get opinion requests
	final List<OpinionRequest> opReqs;
	opReqs = tmOP.getOpinionRequests();

	// convey opinion requests to scenario
	scnOP.setOpinionRequests(opReqs);

	// get opinions
	final List<Opinion> opinions = scn.generateOpinions();

	// convey opinions to the trust model
	tm.processOpinions(opinions);

	// get interaction partners from TM
	// Convert Map to a TreeMap to ensure deterministic iteration
	final Map<Integer, Integer> partners;
	partners = Utils.ordered(tmIP.getInteractionPartners(services));

	// convey partner selections to scenario
	scnIP.setInteractionPartners(partners);

	// generate experiences
	final List<Experience> experiences = scn.generateExperiences();

	// convey experiences
	tm.processExperiences(experiences);

	// calculate trust
	tm.calculateTrust();

	// evaluation
	for (int service : services) {
	    final Map<Integer, Double> capabilities;
	    capabilities = scn.getCapabilities(service);

	    // accuracy
	    final Accuracy accuracy = getAccuracyInstance(service);
	    final int accKey = accuracy.getClass().hashCode() ^ service;
	    final double accValue = accuracy.evaluate(tm.getTrust(service),
		    capabilities);

	    results.put(accKey, accValue);

	    final Integer agent = partners.get(service);

	    // utility -- if partner for this service was selected
	    if (null != agent) {
		final Utility utility = getUtilityInstance(service);
		final int utilKey = utility.getClass().hashCode() ^ service;
		final double utilValue = utility.evaluate(capabilities, agent);

		results.put(utilKey, utilValue);
	    }

	    // opinion cost
	    final OpinionCost opinionCost = getOpinionCostInstance(service);
	    final int ocKey = opinionCost.getClass().hashCode() ^ service;
	    final double ocValue;
	    ocValue = opinionCost.evaluate(agents, services, opReqs);

	    results.put(ocKey, ocValue);
	}
    }

    /**
     * Returns an instance of the opinion cost metric for the given service.
     * 
     * <p>
     * Method retrieves the instance from the the map of all opinion cost metric
     * instances. If for the given service, the map does not contain an
     * instance, the method creates a new instance, initializes it with
     * parameters, adds instance to the map and returns the instance.
     * 
     * @param service
     *            Type of service
     * @return An instance of the metric
     */
    protected OpinionCost getOpinionCostInstance(int service) {
	OpinionCost metric = serviceOc.get(service);

	if (null == metric) {
	    try {
		metric = ocClass.newInstance();
		metric.initialize(utilityParameters);
		serviceOc.put(service, metric);
	    } catch (Exception e) {
		throw new Error(String.format(CREATION_ERROR, utilityClass,
			Arrays.toString(utilityParameters)));
	    }
	}

	return metric;
    }

    @Override
    public TrustModel<?> getTrustModel() {
	return tm;
    }

    @Override
    public Scenario getScenario() {
	return scn;
    }

    @Override
    protected Set<Class<? extends Metric>> requiredMetricClasses() {
	final Set<Class<? extends Metric>> req = super.requiredMetricClasses();
	req.add(OpinionCost.class);
	return req;
    }

    @Override
    protected Set<Class<?>> requiredScenarioClasses() {
	final Set<Class<?>> req = super.requiredScenarioClasses();
	req.add(OpinionProviderSelection.class);
	return req;
    }

    @Override
    protected Set<Class<?>> requiredTrustModelClasses() {
	final Set<Class<?>> req = super.requiredTrustModelClasses();
	req.add(SelectingOpinionProviders.class);
	return req;
    }
}
