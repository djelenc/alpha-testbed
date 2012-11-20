package testbed;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import testbed.interfaces.Accuracy;
import testbed.interfaces.Experience;
import testbed.interfaces.Metric;
import testbed.interfaces.Opinion;
import testbed.interfaces.Scenario;
import testbed.interfaces.TrustModel;

public class NoDecisions extends EvaluationProtocol {

    /** Error message for creating metrics */
    protected static final String CREATION_ERROR;

    static {
	CREATION_ERROR = "Could not instantiate metric '%s' using parameters %s.";
    }

    /** Trust model */
    private TrustModel<?> trustModel = null;

    /** Scenario */
    private Scenario scenario = null;

    /** Class of accuracy instances */
    protected Class<? extends Accuracy> accuracyClass = null;

    /** Parameters for accuracy metrics */
    protected Object[] accuracyParameters = null;

    /** Map of service -> accuracy metrics */
    protected Map<Integer, Accuracy> serviceAccuracy = null;

    @SuppressWarnings("unchecked")
    @Override
    public void initialize(TrustModel<?> tm, Scenario scn,
	    Map<? extends Metric, Object[]> metrics) {

	if (!validTrustModelClasses(tm.getClass())) {
	    throw new IllegalArgumentException("Invalid trust model.");
	}

	trustModel = tm;

	if (!validScenarioClasses(scn.getClass())) {
	    throw new IllegalArgumentException("Invalid scenario.");
	}

	scenario = scn;

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
    protected void evaluationlStep(int time) {
	// convey current time
	trustModel.setCurrentTime(time);
	scenario.setCurrentTime(time);

	// get opinions
	final List<Opinion> opinions = scenario.generateOpinions();

	// convey opinions
	trustModel.processOpinions(opinions);

	// get services
	final List<Integer> services = scenario.getServices();

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
	    final double accValue = accuracy.evaluate(
		    trustModel.getTrust(service), capabilities);

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
    public TrustModel<?> getTrustModel() {
	return trustModel;
    }

    @Override
    public Scenario getScenario() {
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
