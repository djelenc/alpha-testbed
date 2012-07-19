package testbed;

import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import testbed.common.LexiographicComparator;
import testbed.common.Utils;
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

    /** Reference to the trust model */
    private final ITrustModel model;

    /**
     * Reference to the decision making capabilities of a trust model -- if it
     * does not have it, this is set to null
     */
    private final IDecisionMaking decision;

    /** Reference to the scenario */
    private final IScenario scenario;

    /**
     * Reference to the partner selection capability of the scenario -- if it
     * does not have it, this is set to null
     */
    private final IPartnerSelection selection;

    /** Set of ranking metrics */
    private final Set<IRankingMetric> metrics;

    /** Temporary variable to hold the metric results */
    private final double[][] score;

    public AlphaTestbed(ITrustModel model, IScenario scenario,
	    Set<IRankingMetric> metrics) {
	this.model = model;
	this.scenario = scenario;
	this.metrics = new TreeSet<IRankingMetric>(new LexiographicComparator());
	this.metrics.addAll(metrics);

	score = new double[scenario.getServices().size()][metrics.size()];

	if (isUtilityMode(model, scenario)) {
	    decision = (IDecisionMaking) model;
	    selection = (IPartnerSelection) scenario;
	} else if (isRankingsMode(model, scenario)) {
	    decision = null;
	    selection = null;
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

	// get opinions
	final Set<Opinion> opinions = scenario.generateOpinions();

	// convey opinions
	model.processOpinions(opinions);

	// get experiences
	final Set<Integer> services = scenario.getServices();

	if (null != selection && null != decision) {
	    Map<Integer, Integer> partners;

	    // query trust model for interaction partners
	    partners = decision.getNextInteractionPartners(services);

	    // Convert Map to a TreeMap to ensure deterministic iteration
	    partners = Utils.orderedMap(partners);

	    selection.setNextInteractionPartners(partners);
	}

	final Set<Experience> experiences = scenario.generateExperiences();

	// convey experiences
	model.processExperiences(experiences);

	// calculate trust
	model.calculateTrust();

	// calculate metrics for all services
	Map<Integer, Integer> rankings;
	Map<Integer, Double> capabilities;

	int metric = 0;
	for (int service : services) {
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

    /**
     * Determines if the combination of the trust model and the scenario
     * constitutes a testing model with measuring utility.
     * 
     * @param model
     *            Instance of a trust model
     * @param scenario
     *            Instance of a scenario
     * @return True, if and only if instance of the trust model implements the
     *         {@link IDecisionMaking} interface and the instance of a scenario
     *         implements the {@link IPartnerSelection} interface.
     */
    public boolean isUtilityMode(ITrustModel model, IScenario scenario) {
	return IDecisionMaking.class.isAssignableFrom(model.getClass())
		&& IPartnerSelection.class
			.isAssignableFrom(scenario.getClass());
    }

    /**
     * Determines if the combination of the given trust model and the given
     * scenario constitutes a testing mode with measuring rankings.
     * 
     * @param model
     *            Instance of a trust model
     * @param scenario
     *            Instance of a scenario
     * @return True, if and only if instance of the trust model does not
     *         implement the {@link IDecisionMaking} interface and the instance
     *         of a scenario does not implement the {@link IPartnerSelection}
     *         interface.
     */
    public boolean isRankingsMode(ITrustModel model, IScenario scenario) {
	return !IDecisionMaking.class.isAssignableFrom(model.getClass())
		&& !IPartnerSelection.class.isAssignableFrom(scenario
			.getClass());
    }
}
