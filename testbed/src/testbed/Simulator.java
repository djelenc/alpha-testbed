package testbed;

import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import testbed.common.LexiographicComparator;
import testbed.interfaces.Experience;
import testbed.interfaces.IMetric;
import testbed.interfaces.IScenario;
import testbed.interfaces.ITrustModel;
import testbed.interfaces.Opinion;

/**
 * The simulator that performs the evaluation.
 * 
 * <p>
 * The simulator stores a trust model, a scenario and a list of metrics. In
 * every tick, the simulator queries the scenario for experiences and opinions,
 * and then conveys them to the trust model. Afterwards the simulator calculates
 * the results with the given metrics. The results are stored in a temporary
 * variable, which is exposed via public method.
 * 
 * <p>
 * The Simulator class must be instantiated and run by a simulation platform,
 * such as Repast.
 * 
 * @author David
 * 
 */
public class Simulator {

    private final ITrustModel model;
    private final IScenario scenario;
    private final Set<IMetric> metrics;
    private final double[][] score;

    public Simulator(ITrustModel model, IScenario scenario, Set<IMetric> metrics) {
	this.model = model;
	this.scenario = scenario;
	this.metrics = new TreeSet<IMetric>(new LexiographicComparator());
	this.metrics.addAll(metrics);

	score = new double[scenario.getServices().size()][metrics.size()];
    }

    /**
     * Performs one step of evaluation.
     * 
     * <p>
     * The method queries the scenario for experiences and opinions and then
     * conveys them to the trust model. Finally, the rankings are evaluated with
     * provided metrics and the results are stored to a temporary variable.
     * 
     * @param time
     *            Current time
     */
    public void step(int time) {
	// notify components of the current time
	model.setCurrentTime(time);
	scenario.setCurrentTime(time);

	// get experiences and opinions from scenario

	// if (scenario instance of DecisionScenario &&
	// model instance of DecisionModel) {
	// Set<Integer> services;
	// Map<Integer, Integer> partners;
	// services = scenario.getServices();
	// partners = model.getInteractionPartners(services)
	// scenario.setInteractionPartners(partners);
	// XXX: ensure that the iteration through partners is deterministic!
	// }

	Set<Experience> experiences = scenario.generateExperiences();
	Set<Opinion> opinions = scenario.generateOpinions();

	// convey new experiences and opinions to trust model
	model.calculateTrust(experiences, opinions);

	// calculate metrics for all services
	Map<Integer, Integer> rankings;
	Map<Integer, Double> capabilities;

	int metric = 0;
	for (int service : scenario.getServices()) {
	    metric = 0;
	    rankings = model.getRankings(service);
	    capabilities = scenario.getCapabilities(service);

	    for (IMetric m : metrics) {
		score[service][metric] = m.evaluate(rankings, capabilities);
		metric += 1;
	    }

	    // for (UtilityMetri um : metricsUtility) {
	    // utilityResults[service][m
	    // }
	}
    }

    /**
     * Returns the value of the metric for the given service
     * 
     * @param service
     *            The service of the evaluation
     * @param metric
     *            The metric for the evaluation
     * @return The evaluation result
     */
    public double getMetric(int service, IMetric metric) {
	int idx = 0;

	for (IMetric m : metrics) {
	    if (m == metric) {
		break;
	    }

	    idx++;
	}

	if (service < score.length && service >= 0 && idx < score[0].length) {
	    return score[service][idx];
	} else {
	    throw new IllegalArgumentException(String.format(
		    "Invalid query to getMetric(%d, %s)", service, metric));
	}
    }

    public ITrustModel getModel() {
	return model;
    }

    public IScenario getScenario() {
	return scenario;
    }
}
