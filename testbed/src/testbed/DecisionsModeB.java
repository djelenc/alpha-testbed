package testbed;

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
    protected void evaluationlStep(int time) {
	// get agents
	final List<Integer> agents = scn.getAgents();

	// convey agents to TM
	tmOP.setAgents(agents);

	// get services
	final List<Integer> services = scn.getServices();

	// convey available services to TM
	tmOP.setServices(services);

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
