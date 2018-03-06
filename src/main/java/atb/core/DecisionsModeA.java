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
package atb.core;

import atb.common.Utils;
import atb.interfaces.*;

import java.util.*;
import java.util.Map.Entry;

/**
 * An evaluation protocol, where a scenario determines the opinion providers,
 * while a trust model selects interaction partners.
 * <p>
 * Besides measuring accuracy, the protocol also measures the utility that the
 * trust model obtains in interactions. The execution flow of the evaluation
 * protocol is the following:
 * <ol>
 * <li>The atb sets time in the scenario.
 * <li>The atb sets time in the trust model.
 * <li>The atb instructs the scenario to list all available services.
 * <li>The atb conveys the list of available services to the trust model.
 * <li>The atb instructs the scenario to list all available agents.
 * <li>The atb conveys the list of available agents to the trust model.
 * <li>The atb instructs the scenario to generate opinions.
 * <li>The atb conveys generated opinions to the trust model.
 * <li>The atb instructs the trust model to tell, with whom does agent Alpha
 * want to interact.
 * <li>The atb instructs the scenario to generate experiences tuples for
 * agents that the trust model requested.
 * <li>The atb conveys generated experiences to the trust model.
 * <li>The atb instructs the trust model to evaluate trust.
 * <li>The atb instructs the trust model to compute rankings of agents.
 * <li>The atb conveys estimated trust to accuracy metric that then
 * evaluates its accuracy.
 * <li>The atb conveys selected interaction partner to the utility metric
 * that then evaluates the utility in the interaction.
 * </ol>
 *
 * @author David
 */
public class DecisionsModeA extends NoDecisions {

    protected Class<? extends Utility> utilityClass;
    protected Object[] utilityParameters;
    protected HashMap<Integer, Utility> serviceUtility;
    private TrustModel<?> tm;
    private SelectingInteractionPartners tmSelect;
    private Scenario scn;
    private InteractionPartnerSelection scnSelect;

    @SuppressWarnings("unchecked")
    @Override
    public void initialize(TrustModel<?> tm, Scenario scn,
                           Map<? extends Metric, Object[]> metrics) {
        if (!validTrustModelClasses(tm.getClass())) {
            throw new IllegalArgumentException("Invalid trust model.");
        }

        this.tm = tm;
        this.tmSelect = (SelectingInteractionPartners) tm;

        if (!validScenarioClasses(scn.getClass())) {
            throw new IllegalArgumentException("Invalid scenario.");
        }

        this.scn = scn;
        this.scnSelect = (InteractionPartnerSelection) scn;

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

        // get opinions
        final List<Opinion> opinions = scn.generateOpinions();

        // convey opinions to the trust model
        tm.processOpinions(opinions);

        // Get interaction partners in a map
        // Convert Map to a TreeMap to ensure deterministic iteration
        final Map<Integer, Integer> partners = Utils
                .ordered(tmSelect.getInteractionPartners(services));

        // convey partner selection to the scenario
        scnSelect.setInteractionPartners(partners);

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
        }
    }

    /**
     * Returns an instance of the utility metric for the given service.
     * <p>
     * Method retrieves the instance from the the map of all utility metric
     * instances. If for the given service, the map does not contain a metric
     * instance, the method creates a new instance, initializes it with
     * parameters, adds instance to the map and returns the instance.
     *
     * @param service Type of service
     * @return An instance of the metric
     */
    protected Utility getUtilityInstance(int service) {
        Utility metric = serviceUtility.get(service);

        if (null == metric) {
            try {
                metric = utilityClass.newInstance();
                metric.initialize(utilityParameters);
                serviceUtility.put(service, metric);
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
        req.add(Utility.class);
        return req;
    }

    @Override
    protected Set<Class<?>> requiredScenarioClasses() {
        final Set<Class<?>> req = super.requiredScenarioClasses();
        req.add(InteractionPartnerSelection.class);
        return req;
    }

    @Override
    protected Set<Class<?>> requiredTrustModelClasses() {
        final Set<Class<?>> req = super.requiredTrustModelClasses();
        req.add(SelectingInteractionPartners.class);
        return req;
    }
}
