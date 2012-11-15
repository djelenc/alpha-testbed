package testbed;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import testbed.common.Utils;
import testbed.interfaces.Experience;
import testbed.interfaces.DecisionMaking;
import testbed.interfaces.Metric;
import testbed.interfaces.PartnerSelection;
import testbed.interfaces.Accuracy;
import testbed.interfaces.Scenario;
import testbed.interfaces.TrustModel;
import testbed.interfaces.Utility;
import testbed.interfaces.Opinion;

/**
 * <h1>The Alpha Testbed</h1>
 * <p>
 * The testbed that performs the evaluation. It has two modes of operation, the
 * <b>ranking mode</b> and the <b>utility mode</b>. The execution flow of the
 * testbed and the data it contains depend on the mode the testbed is in.
 * 
 * <p>
 * The testbed publishes the results of the evaluation by using subscribers.
 * Subscribers are instances of classes that implement the
 * {@link MetricSubscriber} interface.
 * 
 * <p>
 * The results are published in the following manner. First, the subscribers
 * have to subscribe to the testbed by using
 * {@link #subscribe(MetricSubscriber)} method. Second, at the end of each
 * evaluation step, the testbed notifies all subscribers by invoking their
 * {@link MetricSubscriber#update(AlphaTestbed)} methods. Finally, the
 * subscribers are expected to pull the results of the evaluation via the
 * {@link #getMetric(int, Metric)} method.
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
 * The last two steps are repeated for every type of service. Finally, the
 * subscribers are asked to retrieve the results.
 * 
 * <p>
 * In ranking mode, the testbed holds a reference to instances of a
 * {@link TrustModel}, a {@link Scenario}, and a {@link Accuracy}. Other
 * class members are set to null or ignored.
 * 
 * <h2>Utility mode</h2>
 * <p>
 * In utility mode, the testbed treats the trust model as a cognitive entity.
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
 * The last three steps are repeated for every type of service. Finally, the
 * subscribers are asked to retrieve the results.
 * 
 * <p>
 * In utility mode, the testbed reference to the same instances as in ranking
 * mode (an instance of {@link TrustModel}, {@link Scenario}, and
 * {@link Accuracy}). Besides those, the utility mode also references an
 * implementation of an {@link DecisionMaking} interface (part of a trust model
 * that selects interaction partners), an {@link PartnerSelection} interface
 * implementation (part of scenario that receives the partner selections and
 * prepares corresponding {@link Experience} tuples) and an instance of a
 * {@link Utility} to evaluate the obtained utility. Because the
 * {@link Utility} is stateful (and the state is different for every
 * service), we need to have an instance of such {@link Utility} instance
 * for every possible type of service. The testbed stores all those instances in
 * the map of type {@link Map}<{@link Integer}, {@link Utility}>, because
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
    protected static final String CREATION_ERROR = "Could not instantiate metric '%s' using parameters %s.";
    protected static final String INCOMPATIBLE_EX = "Trust model '%s' cannot be tested with scenario '%s'.";
    protected static final String METRIC_QUERY_EX = "Invalid query for metric '%s' and service '%d'.";

    /** Reference to the trust model */
    protected final TrustModel<?> model;

    /**
     * Reference to the decision making capabilities of a trust model -- null in
     * ranking mode
     */
    protected final DecisionMaking decision;

    /** Reference to the scenario */
    protected final Scenario scenario;

    /**
     * Reference to the partner selection capability of the scenario -- null in
     * ranking mode
     */
    protected final PartnerSelection selection;

    /** Class of ranking metric instances */
    protected final Class<? extends Accuracy> rankingMetricClass;

    /** Parameters for creating ranking metric instances */
    protected final Object[] rankingMetricParameters;

    /** Class of utility metric instances -- null in ranking mode */
    protected final Class<? extends Utility> utilityMetricClass;

    /** Parameters for creating utility metric instances */
    protected final Object[] utilityMetricParameters;

    /** Temporary map that holds the metric results */
    protected final Map<Integer, Double> score;

    /** Convenience flag to denote the utility mode */
    protected final boolean utilityMode;

    /** Mapping of services to {@link Accuracy} instance */
    protected final Map<Integer, Accuracy> allRankingMetrics;

    /**
     * Mapping of services to {@link Utility} instances -- null in ranking
     * mode
     */
    protected final Map<Integer, Utility> allUtilityMetrics;

    /** Subscribers to this evaluation run */
    protected final List<MetricSubscriber> subscribers;

    /**
     * Constructor for the AlphaTestbed class. Its parameters have the following
     * semantics:
     * <ol>
     * <li>An {@link Scenario} instance. The instance must be initialized (that
     * is, the method {@link Scenario#initialize(Object...)} must be called
     * before the instance is passed to this constructor).
     * <li>An {@link TrustModel} instance. Similar to scenario instance, the
     * instance of the trust model must also be initialized before it is passed
     * to this constructor.<br/>
     * <br/>
     * Additionally, the {@link TrustModel} instance must be compatible with the
     * {@link Scenario} instance. There are only two valid combinations.
     * <ol>
     * <li>In the first case, the scenario instance implements the
     * {@link PartnerSelection} interface and the trust model instance
     * implements the {@link DecisionMaking}. This combination constitutes the
     * so called <b>utility mode</b>.
     * <li>In the second case, the scenario instance <b>does not</b> implement
     * the {@link PartnerSelection} interface, and the trust model instance
     * <b>does not</b> implement the {@link DecisionMaking} interface. This
     * combination constitutes the so called <b>ranking mode</b>.
     * </ol>
     * If the given combination of scenario and trust model does not constitute
     * a valid combination, an {@link IllegalArgumentException} is thrown.
     * <li>An instance of the {@link Accuracy}. This instance is only used
     * to infer the type (i.e. class) for the ranking metric. The testbed will
     * create the actual instance of the {@link Accuracy} that will be used
     * for evaluation.
     * <li>The varargs parameter used to initialize a {@link Accuracy}
     * instance. The testbed uses these parameters when creates and initializes
     * new instances of the {@link Accuracy}.
     * <li>An instance of the {@link Utility}. This instance is only used
     * to infer the type (i.e. class) for the utility metric. The testbed will
     * create the actual instance of the {@link Utility} that will be used
     * for evaluation.
     * <li>The varargs parameter used to initialize a {@link Utility}
     * instance. The testbed uses these parameters when creates and initializes
     * new instances of the {@link Utility}.
     * </ol>
     * 
     * @param scn
     *            A scenario instance
     * @param tm
     *            A trust model instance
     * @param rankingMetric
     *            A ranking metric instance
     * @param rmParams
     *            A set of varargs arguments to initialize a ranking metric
     *            instance
     * @param utilityMetric
     *            A utility metric instance
     * @param umParams
     *            A set of varargs arguments to initialize an utility metric
     *            instance
     */
    public AlphaTestbed(Scenario scn, TrustModel<?> tm,
	    Accuracy rankingMetric, Object[] rmParams,
	    Utility utilityMetric, Object[] umParams) {
	model = tm;
	scenario = scn;

	if (isValidUtilityMode(tm, scn)) {
	    decision = (DecisionMaking) tm;
	    selection = (PartnerSelection) scn;
	    rankingMetricClass = rankingMetric.getClass();
	    rankingMetricParameters = rmParams;
	    allRankingMetrics = new HashMap<Integer, Accuracy>();
	    utilityMetricClass = utilityMetric.getClass();
	    utilityMetricParameters = umParams;
	    allUtilityMetrics = new HashMap<Integer, Utility>();
	    utilityMode = true;
	} else if (isValidRankingMode(tm, scn)) {
	    decision = null;
	    selection = null;
	    rankingMetricClass = rankingMetric.getClass();
	    rankingMetricParameters = rmParams;
	    allRankingMetrics = new HashMap<Integer, Accuracy>();
	    utilityMetricClass = null;
	    utilityMetricParameters = null;
	    allUtilityMetrics = null;
	    utilityMode = false;
	} else {
	    throw new IllegalArgumentException(String.format(INCOMPATIBLE_EX,
		    tm, scn));
	}

	subscribers = new ArrayList<MetricSubscriber>();
	score = new HashMap<Integer, Double>();
    }

    /**
     * Performs one step of evaluation.
     * 
     * <p>
     * This behavior of this method depends on the mode of the testbed.
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
	    final Map<Integer, Double> capabilities;

	    capabilities = scenario.getCapabilities(service);

	    // evaluate rankings
	    final Accuracy rm = getRankingMetricInstance(service);
	    final int rankMetricKey = rm.getClass().hashCode() ^ service;
	    final double rankMetricScore = rm.evaluate(model.getTrust(service),
		    capabilities);

	    score.put(rankMetricKey, rankMetricScore);

	    // evaluate utility
	    if (isUtilityMode()) {
		final Integer agent = partners.get(service);

		// if partner for this service was selected
		if (null != agent) {
		    final Utility um = getUtilityMetricInstance(service);
		    final int utilityMetricKey;
		    final double utilityMetricScore;
		    utilityMetricKey = um.getClass().hashCode() ^ service;
		    utilityMetricScore = um.evaluate(capabilities, agent);

		    score.put(utilityMetricKey, utilityMetricScore);
		}
	    }
	}

	// notify all subscribers
	notifiySubscribers();
    }

    /**
     * Returns the value of the metric for the given service. This method should
     * be called by all instances of {@link MetricSubscriber} to pull the data
     * from the test-bed.
     * 
     * @param service
     *            The type of service
     * @param metric
     *            The metric
     * @return The evaluation result
     */
    public double getMetric(int service, Metric metric) {
	final Double result = score.get(metric.getClass().hashCode() ^ service);

	if (null == result) {
	    throw new IllegalArgumentException(String.format(METRIC_QUERY_EX,
		    metric, service));
	} else {
	    return result;
	}
    }

    public TrustModel<?> getModel() {
	return model;
    }

    public Scenario getScenario() {
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
     * @return True, if and only if the instance of the trust model implements
     *         the {@link DecisionMaking} interface and the instance of a
     *         scenario implements the {@link PartnerSelection} interface.
     */
    protected boolean isValidUtilityMode(TrustModel<?> model, Scenario scenario) {
	return DecisionMaking.class.isAssignableFrom(model.getClass())
		&& PartnerSelection.class.isAssignableFrom(scenario.getClass());
    }

    /**
     * Determines, if the combination of the given trust model and the given
     * scenario constitutes a valid mode that measures rankings.
     * 
     * @param model
     *            Instance of a trust model
     * @param scenario
     *            Instance of a scenario
     * @return True, if and only if the instance of the trust model does not
     *         implement the {@link DecisionMaking} interface and the instance
     *         of a scenario does not implement the {@link PartnerSelection}
     *         interface.
     */
    protected boolean isValidRankingMode(TrustModel<?> model, Scenario scenario) {
	return !DecisionMaking.class.isAssignableFrom(model.getClass())
		&& !PartnerSelection.class
			.isAssignableFrom(scenario.getClass());
    }

    /**
     * Returns true, if the test-bed is in the utility mode.
     * 
     * @return
     */
    public boolean isUtilityMode() {
	return utilityMode;
    }

    /**
     * Subscribe to the notifications of the test-bed. The data has to be pulled
     * from the test-bed instance using {@link #getMetric(int, Metric)} method.
     * 
     * @param observer
     *            The instance of the subscriber.
     */
    public void subscribe(MetricSubscriber observer) {
	subscribers.add(observer);
    }

    /**
     * Removes the subscriber from the list of subscribers that the test-bed
     * notifies at the end of each run.
     * 
     * @param subscriber
     *            The instance to be removed.
     */
    public void remove(MetricSubscriber subscriber) {
	subscribers.remove(subscriber);
    }

    /**
     * Notifies the subscribers that the new data is ready to be pulled from the
     * test-bed.
     */
    protected void notifiySubscribers() {
	for (MetricSubscriber s : subscribers)
	    s.update(this);
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
    protected Accuracy getRankingMetricInstance(int service) {
	Accuracy metric = allRankingMetrics.get(service);

	if (null == metric) {
	    try {
		metric = rankingMetricClass.newInstance();
		metric.initialize(rankingMetricParameters);
		allRankingMetrics.put(service, metric);
	    } catch (Exception e) {
		throw new Error(String.format(CREATION_ERROR,
			rankingMetricClass,
			Arrays.toString(rankingMetricParameters)));
	    }
	}

	return metric;
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
    protected Utility getUtilityMetricInstance(int service) {
	Utility metric = allUtilityMetrics.get(service);

	if (null == metric) {
	    try {
		metric = utilityMetricClass.newInstance();
		metric.initialize(utilityMetricParameters);
		allUtilityMetrics.put(service, metric);
	    } catch (Exception e) {
		throw new Error(String.format(CREATION_ERROR,
			utilityMetricClass,
			Arrays.toString(utilityMetricParameters)));
	    }
	}

	return metric;
    }
}
