package testbed;

import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import testbed.common.LexiographicComparator;
import testbed.interfaces.Experience;
import testbed.interfaces.IDecisionMaking;
import testbed.interfaces.IPartnerSelection;
import testbed.interfaces.IRankingMetric;
import testbed.interfaces.IScenario;
import testbed.interfaces.ITrustModel;
import testbed.interfaces.Opinion;

/**
 * The testbed that performs the evaluation.
 * 
 * <p>
 * The testbed holds a reference to a trust model, a scenario and a list of
 * metrics. In every tick, the testbed queries the scenario for experiences and
 * opinions, and then conveys them to the trust model. Afterwards it calculates
 * the results with the given metrics. The results are stored in a temporary
 * variable, which is exposed via public method.
 * 
 * <p>
 * The testbed must be instantiated and run by a simulation platform, such as
 * Repast.
 * 
 * @author David
 * 
 */
public class AlphaTestbed {

    private final ITrustModel model;
    private final IScenario scenario;
    private final Set<IRankingMetric> metrics;
    private final double[][] score;

    private final Mode mode;

    public AlphaTestbed(ITrustModel model, IScenario scenario,
	    Set<IRankingMetric> metrics) {
	this.model = model;
	this.scenario = scenario;
	this.metrics = new TreeSet<IRankingMetric>(new LexiographicComparator());
	this.metrics.addAll(metrics);

	score = new double[scenario.getServices().size()][metrics.size()];

	if (model instanceof ITrustModel && model instanceof IDecisionMaking
		&& scenario instanceof IScenario
		&& scenario instanceof IPartnerSelection) {
	    mode = Mode.Utility;
	} else if (model instanceof ITrustModel
		&& !(model instanceof IDecisionMaking)
		&& scenario instanceof IScenario
		&& !(scenario instanceof IPartnerSelection)) {
	    mode = Mode.Ranking;
	} else {
	    throw new IllegalArgumentException(String.format(
		    "Selected trust model (%s) "
			    + "cannot be tested with selected scenario (%s)",
		    model.getName(), scenario.getName()));
	}
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

	// if (scenario implements IPartnerSelection &&
	// model implements IDecisionMaking) {
	// Set<Integer> services;
	// Map<Integer, Integer> partners;
	// services = scenario.getServices();
	// partners = model.getNextInteractionPartners(services)
	// scenario.setNextInteractionPartners(partners);
	// XXX: ensure that the iteration through partners is deterministic!
	// }

	// get opinions
	Set<Opinion> opinions = scenario.generateOpinions();

	// convey opinions
	model.processOpinions(opinions);

	// get experiences

	if (mode == Mode.Utility) {
	    Map<Integer, Integer> partners = ((IDecisionMaking) model)
		    .getNextInteractionPartners(scenario.getServices());

	    ((IPartnerSelection) scenario).setNextInteractionPartners(partners);
	}

	Set<Experience> experiences = scenario.generateExperiences();

	// convey experiences
	model.processExperiences(experiences);

	model.calculateTrust();

	// calculate metrics for all services
	Map<Integer, Integer> rankings;
	Map<Integer, Double> capabilities;

	int metric = 0;
	for (int service : scenario.getServices()) {
	    metric = 0;
	    rankings = model.getRankings(service);
	    capabilities = scenario.getCapabilities(service);

	    for (IRankingMetric m : metrics) {
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
    public double getMetric(int service, IRankingMetric metric) {
	int idx = 0;

	for (IRankingMetric m : metrics) {
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
