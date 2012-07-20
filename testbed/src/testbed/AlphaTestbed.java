package testbed;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import testbed.common.Utils;
import testbed.interfaces.Experience;
import testbed.interfaces.IDecisionMaking;
import testbed.interfaces.IMetric;
import testbed.interfaces.IPartnerSelection;
import testbed.interfaces.IRankingMetric;
import testbed.interfaces.IScenario;
import testbed.interfaces.ITrustModel;
import testbed.interfaces.IUtilityMetric;
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

    // Exception messages
    private static final String INCOMPATIBLE_EX = "Trust model %s cannot be tested with scenario %s.";
    private static final String UTILITY_VALUE_EX = "Unable to compute value for metric %s and service %d.";
    private static final String METRIC_QUERY_EX = "Invalid query to getMetric(%d, %s).";

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

    /** Instance of a ranking metric */
    private final IRankingMetric rankingMetric;

    /** Instance of an utility metric */
    private final IUtilityMetric utilityMetric;

    /** Temporary variable to hold the metric results */
    private final double[][] score;

    /** Flag for utility mode */
    private final boolean utilityMode;

    /** Map of Service => IUtilityMetric instances */
    private final Map<Integer, IUtilityMetric> allUtilityMetrics;

    public AlphaTestbed(ITrustModel model, IScenario scenario,
	    IRankingMetric rankingMetric, IUtilityMetric utilityMetric) {
	this.model = model;
	this.scenario = scenario;
	this.rankingMetric = rankingMetric;

	score = new double[scenario.getServices().size()][2];

	if (isValidUtilityMode(model, scenario)) {
	    this.decision = (IDecisionMaking) model;
	    this.selection = (IPartnerSelection) scenario;
	    this.utilityMetric = utilityMetric;
	    this.allUtilityMetrics = new HashMap<Integer, IUtilityMetric>();
	    this.utilityMode = true;
	} else if (isValidRankingsMode(model, scenario)) {
	    this.decision = null;
	    this.selection = null;
	    this.utilityMetric = null;
	    this.allUtilityMetrics = null;
	    this.utilityMode = false;
	} else {
	    throw new IllegalArgumentException(String.format(INCOMPATIBLE_EX,
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

	// get services
	final Set<Integer> services = scenario.getServices();

	Map<Integer, Integer> partners = null;

	if (isUtilityMode()) {
	    // query trust model for interaction partners
	    partners = decision.getNextInteractionPartners(services);

	    // Convert Map to a TreeMap to ensure deterministic iteration
	    partners = Utils.orderedMap(partners);

	    // give partner selection to the scenario
	    selection.setNextInteractionPartners(partners);
	}

	// generate experiences
	final Set<Experience> experiences = scenario.generateExperiences();

	// convey experiences
	model.processExperiences(experiences);

	// calculate trust
	model.calculateTrust();

	// calculate metrics for all services
	Map<Integer, Integer> rankings;
	Map<Integer, Double> capabilities;

	for (int service : services) {
	    rankings = model.getRankings(service);
	    capabilities = scenario.getCapabilities(service);

	    // evaluate rankings
	    score[service][0] = rankingMetric.evaluate(rankings, capabilities);

	    // evaluate utility
	    if (isUtilityMode()) {
		double value = -1d;

		if (!allUtilityMetrics.containsKey(service)) {
		    allUtilityMetrics.put(service, utilityMetric);
		}

		// In a particular time tick, agent Alpha can interact with
		// different agents, but for the same type of service.
		// Because
		// of this, we need to iterate through all partners. In such
		// case, the updated utility value (for that service) will
		// reflect the utility that was obtained after interacting
		// with
		// the last agent in the set of partners.
		for (Map.Entry<Integer, Integer> e : partners.entrySet()) {
		    final int agent = e.getKey();
		    final int partnerService = e.getValue();

		    final IUtilityMetric um = allUtilityMetrics.get(service);

		    if (partnerService == service) {
			value = um.evaluate(capabilities, agent);
		    }
		}

		// this should never be executed -- a sanity check
		if (Double.compare(value, 0) < 0) {
		    throw new IllegalArgumentException(String.format(
			    UTILITY_VALUE_EX, utilityMetric.getName(), service));
		}

		score[service][1] = value;
	    }
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
	final int metricIndex;

	if (metric instanceof IRankingMetric) {
	    metricIndex = 0;
	} else if (metric instanceof IUtilityMetric) {
	    metricIndex = 1;
	} else {
	    throw new IllegalArgumentException(String.format(METRIC_QUERY_EX,
		    service, metric));
	}

	return score[service][metricIndex];
    }

    public ITrustModel getModel() {
	return model;
    }

    public IScenario getScenario() {
	return scenario;
    }

    /**
     * Determines, if the combination of the trust model and the scenario
     * constitutes a valid mode that measures utility.
     * 
     * @param model
     *            Instance of a trust model
     * @param scenario
     *            Instance of a scenario
     * @return True, if and only if instance of the trust model implements the
     *         {@link IDecisionMaking} interface and the instance of a scenario
     *         implements the {@link IPartnerSelection} interface.
     */
    public boolean isValidUtilityMode(ITrustModel model, IScenario scenario) {
	return IDecisionMaking.class.isAssignableFrom(model.getClass())
		&& IPartnerSelection.class
			.isAssignableFrom(scenario.getClass());
    }

    /**
     * Determines, if the combination of the given trust model and the given
     * scenario constitutes a valid mode that measures rankings.
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
    public boolean isValidRankingsMode(ITrustModel model, IScenario scenario) {
	return !IDecisionMaking.class.isAssignableFrom(model.getClass())
		&& !IPartnerSelection.class.isAssignableFrom(scenario
			.getClass());
    }

    /**
     * Returns true if the testbed is in the utility mode.
     * 
     * @return
     */
    public boolean isUtilityMode() {
	return utilityMode;
    }
}
