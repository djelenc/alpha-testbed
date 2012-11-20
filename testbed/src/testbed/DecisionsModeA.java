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
import testbed.interfaces.Scenario;
import testbed.interfaces.SelectingInteractionPartners;
import testbed.interfaces.TrustModel;
import testbed.interfaces.Utility;

public class DecisionsModeA extends NoDecisions {

    private TrustModel<?> tm;
    private SelectingInteractionPartners tmSelect;

    private Scenario scn;
    private InteractionPartnerSelection scnSelect;

    protected Class<? extends Utility> utilityClass;
    protected Object[] utilityParameters;

    protected HashMap<Integer, Utility> serviceUtility;

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
    protected void evaluationlStep(int time) {
	// get opinions
	final List<Opinion> opinions = scn.generateOpinions();

	// convey opinions to the trust model
	tm.processOpinions(opinions);

	// get services
	final List<Integer> services = scn.getServices();

	// Get interaction partners in a map
	// Convert Map to a TreeMap to ensure deterministic iteration
	final Map<Integer, Integer> partners = Utils.ordered(tmSelect
		.getInteractionPartners(services));

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
     * 
     * <p>
     * Method retrieves the instance from the the map of all utility metric
     * instances. If for the given service, the map does not contain a metric
     * instance, the method creates a new instance, initializes it with
     * parameters, adds instance to the map and returns the instance.
     * 
     * @param service
     *            Type of service
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
