package testbed;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import testbed.interfaces.Accuracy;
import testbed.interfaces.Experience;
import testbed.interfaces.Opinion;
import testbed.interfaces.Scenario;
import testbed.interfaces.TrustModel;

public class TestbedNoDecisions extends EvaluationProtocol {

    /** Reference to the trust model */
    protected TrustModel<?> trustModel = null;

    /** Scenario */
    protected Scenario scenario = null;

    /** Class of accuracy instances */
    protected Class<? extends Accuracy> accuracyClass = null;

    /** Parameters for creating accuracy instances */
    protected Object[] accuracyParameters = null;

    /** All accuracy metrics */
    protected Map<Integer, Accuracy> accuracyMetrics = null;

    @Override
    public void initialize(Object... params) {
	trustModel = null;
	scenario = null;

	accuracyClass = null;// accuracy.getClass();
	accuracyParameters = null;// accParams;
	accuracyMetrics = new HashMap<Integer, Accuracy>();

	subscribers = new ArrayList<MetricSubscriber>();
	score = new HashMap<Integer, Double>();
    }

    @Override
    public void step(int time) {
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

	    score.put(accKey, accValue);
	}

	notifiySubscribers();
    }

    protected Accuracy getAccuracyInstance(int service) {
	Accuracy metric = accuracyMetrics.get(service);

	if (null == metric) {
	    try {
		metric = accuracyClass.newInstance();
		metric.initialize(accuracyParameters);
		accuracyMetrics.put(service, metric);
	    } catch (Exception e) {
		throw new Error(String.format(CREATION_ERROR, accuracyClass,
			Arrays.toString(accuracyParameters)));
	    }
	}

	return metric;
    }

    @Override
    public TrustModel<?> getModel() {
	return trustModel;
    }

    @Override
    public Scenario getScenario() {
	return scenario;
    }
}
