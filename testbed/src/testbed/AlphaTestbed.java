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
 * <h1>The Alpha Testbed</h1>
 * <p>
 * The testbed that performs the evaluation. It has two modes of operation, the
 * ranking mode and the utility mode. The execution flow of the testbed and the
 * data it contains depend on the mode the testbed is in.
 * 
 * <h2>Ranking mode</h2>
 * <p>
 * In ranking mode, the testbed treats the trust model as a black box. This
 * means that the testbed generates the complete data that is given to the trust
 * model. The testbed then evaluates trust model's output. This is achieved by
 * evaluating rankings of agents that emerge from the computed trust degrees.
 * The general execution flow is the following:
 * <ol>
 * <li>The testbed sets time in the scenario.
 * <li>The testbed sets time in the trust model.
 * <li>The testbed instructs the scenario to generate opinions.
 * <li>The testbed conveys generated opinions to the trust model.
 * <li>The testbed instructs the scenario to generate experiences.
 * <li>The testbed conveys generated experiences to the trust model.
 * <li>The testbed instructs the trust model to evaluate trust.
 * <li>The testbed instruct the trust model to compute rankings of agents.
 * <li>The testbed evaluates received rankings.
 * </ol>
 * <p>
 * The last two steps are repeated for every type of service.
 * 
 * <p>
 * In ranking mode, the testbed holds a reference to instances of a
 * {@link ITrustModel}, a {@link IScenario}, and a {@link IRankingMetric}. Other
 * class members are set to null or ignored.
 * 
 * <h2>Utility mode</h2>
 * <p>
 * In utility mode the testbed treats the trust model as a cognitive entity.
 * This means that the trust model is required to select partners for
 * interactions, whereas in ranking mode the testbed (or the scenario) was
 * responsible for selecting interaction partners. Besides measuring the
 * correctness of rankings, the testbed in utility mode also measures the
 * utility that the trust model obtains in interactions during evaluation. The
 * general execution flow is the following:
 * <ol>
 * <li>The testbed sets time in the scenario.
 * <li>The testbed sets time in the trust model.
 * <li>The testbed instructs the scenario to generate opinions.
 * <li>The testbed conveys generated opinions to the trust model.
 * <li>The testbed instructs the trust model to tell, whom does agent Alpha want
 * to interact with.
 * <li>The testbed instructs the scenario to generate experiences tuples for
 * agents that the trust model requested.
 * <li>The testbed conveys generated experiences to the trust model.
 * <li>The testbed instructs the trust model to evaluate trust.
 * <li>The testbed instructs the trust model to compute rankings of agents.
 * <li>The testbed evaluates received rankings.
 * <li>The testbed evaluates the utility which was obtained from interactions.
 * </ol>
 * <p>
 * The last three steps are repeated for every type of service.
 * 
 * <p>
 * In utility mode, the testbed reference to the same instances as in ranking
 * mode (an instance of {@link ITrustModel}, {@link IScenario}, and
 * {@link IRankingMetric}). Besides those, the utility mode also references an
 * implementation of an {@link IDecisionMaking} interface (part of a trust model
 * that selects interaction partners), an {@link IPartnerSelection} interface
 * implementation (part of scenario that receives the partner selections and
 * prepares corresponding {@link Experience} tuples) and an instance of a
 * {@link IUtilityMetric} to evaluate the obtained utility. Because the
 * {@link IUtilityMetric} is stateful (and the state is different for every
 * service), we need to have an instance of such {@link IUtilityMetric} instance
 * for every possible type of service. The testbed stores all those instances in
 * the map of type {@link Map}<{@link Integer}, {@link IUtilityMetric}>, because
 * they track state.
 * 
 * <p>
 * The testbed must be instantiated and run by a simulation platform, such as
 * Repast.
 * 
 * @author David
 * 
 */
public class AlphaTestbed {
    private static final String CREATION_ERROR = "Could not instantiate metric '%s'.";
    private static final String INCOMPATIBLE_EX = "Trust model '%s' cannot be tested with scenario '%s'.";
    private static final String METRIC_QUERY_EX = "Invalid query for metric '%s' and service '%d'.";

    /** Reference to the trust model */
    private final ITrustModel model;

    /**
     * Reference to the decision making capabilities of a trust model -- null in
     * ranking mode
     */
    private final IDecisionMaking decision;

    /** Reference to the scenario */
    private final IScenario scenario;

    /**
     * Reference to the partner selection capability of the scenario -- null in
     * ranking mode
     */
    private final IPartnerSelection selection;

    /** Instance of a ranking metric */
    private final IRankingMetric rankingMetric;

    /** Instance of an utility metric -- null in ranking mode */
    private final IUtilityMetric utilityMetric;

    /** Temporary map that holds the metric results */
    private final Map<Integer, Double> score;

    /** Convenience flag to denote the utility mode */
    private final boolean utilityMode;

    /**
     * Mapping of services to {@link IUtilityMetric} instances -- null in
     * ranking mode
     */
    private final Map<Integer, IUtilityMetric> allUtilityMetrics;

    public AlphaTestbed(ITrustModel model, IScenario scenario,
	    IRankingMetric rankingMetric, IUtilityMetric utilityMetric) {
	this.model = model;
	this.scenario = scenario;
	this.rankingMetric = rankingMetric;

	score = new HashMap<Integer, Double>();

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

	// ------------------------------- //
	// -- Evaluate calculated trust -- //
	// ------------------------------- //

	for (int service : services) {
	    final Map<Integer, Integer> rankings;
	    final Map<Integer, Double> capabilities;

	    rankings = model.getRankings(service);
	    capabilities = scenario.getCapabilities(service);

	    // evaluate rankings
	    final int rankMetricKey;
	    final double rankMetricScore;

	    rankMetricKey = rankingMetric.getClass().hashCode() ^ service;
	    rankMetricScore = rankingMetric.evaluate(rankings, capabilities);

	    score.put(rankMetricKey, rankMetricScore);

	    // evaluate utility
	    if (isUtilityMode()) {
		if (!allUtilityMetrics.containsKey(service)) {
		    try {
			final IUtilityMetric um;
			um = utilityMetric.getClass().newInstance();
			um.initialize(); // TODO -- needs parameters
			allUtilityMetrics.put(service, um);
		    } catch (Exception e) {
			throw new Error(String.format(CREATION_ERROR,
				utilityMetric));
		    }
		}

		final Integer agent = partners.get(service);

		// if no partner for this service
		if (null != agent) {
		    final int utilityMetricKey;
		    final double utilityMetricScore;
		    final IUtilityMetric um = allUtilityMetrics.get(service);

		    utilityMetricKey = um.getClass().hashCode() ^ service;
		    utilityMetricScore = um.evaluate(capabilities, agent);
		    score.put(utilityMetricKey, utilityMetricScore);
		}
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
	final Double result = score.get(metric.getClass().hashCode() ^ service);

	if (null == result) {
	    throw new IllegalArgumentException(String.format(METRIC_QUERY_EX,
		    metric, service));
	} else {
	    return result;
	}
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
     * Returns true, if the testbed is in the utility mode.
     * 
     * @return
     */
    public boolean isUtilityMode() {
	return utilityMode;
    }
}
