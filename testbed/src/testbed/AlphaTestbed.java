package testbed;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import testbed.common.Utils;
import testbed.interfaces.Experience;
import testbed.interfaces.OpinionCost;
import testbed.interfaces.OpinionProviderSelection;
import testbed.interfaces.OpinionRequest;
import testbed.interfaces.SelectingInteractionPartners;
import testbed.interfaces.Metric;
import testbed.interfaces.InteractionPartnerSelection;
import testbed.interfaces.Accuracy;
import testbed.interfaces.Scenario;
import testbed.interfaces.SelectingOpinionProviders;
import testbed.interfaces.TrustModel;
import testbed.interfaces.Utility;
import testbed.interfaces.Opinion;

import static testbed.EvaluationProtocol.NO_DECISIONS;
import static testbed.EvaluationProtocol.SELECTING_INTERACTION_PARTNERS;
import static testbed.EvaluationProtocol.SELECTING_OPINIONS_PROVIDERS;

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
 * {@link TrustModel}, a {@link Scenario}, and a {@link Accuracy}. Other class
 * members are set to null or ignored.
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
 * implementation of an {@link SelectingInteractionPartners} interface (part of
 * a trust model that selects interaction partners), an
 * {@link InteractionPartnerSelection} interface implementation (part of
 * scenario that receives the partner selections and prepares corresponding
 * {@link Experience} tuples) and an instance of a {@link Utility} to evaluate
 * the obtained utility. Because the {@link Utility} is stateful (and the state
 * is different for every service), we need to have an instance of such
 * {@link Utility} instance for every possible type of service. The testbed
 * stores all those instances in the map of type {@link Map}<{@link Integer},
 * {@link Utility}>, because they track state.
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
    protected final TrustModel<?> trustModel;

    /** TM's interaction partner selection mechanism */
    protected final SelectingInteractionPartners tmSelectingInteractions;

    /** TM's opinion provider selection mechanism */
    protected final SelectingOpinionProviders tmSelectingOpinions;

    /** Scenario */
    protected final Scenario scenario;

    /** Scenario's interaction partner selection mechanism */
    protected final InteractionPartnerSelection scnInteractionSelection;

    /** Scenario's opinion provider selection mechanism */
    protected final OpinionProviderSelection scnOpinionSelection;

    /** Class of accuracy instances */
    protected final Class<? extends Accuracy> accuracyClass;

    /** Parameters for creating accuracy instances */
    protected final Object[] accuracyParameters;

    /** Class of utility instances */
    protected final Class<? extends Utility> utilityClass;

    /** Parameters for creating utility metric instances */
    protected final Object[] utilityParameters;

    /** Class of opinion cost instances */
    protected final Class<? extends OpinionCost> opinionCostClass;

    /** Parameters for creating opinion cost instances */
    protected final Object[] opinionCostParameters;

    /**
     * Temporary variable that holds current results; keys represent metric and
     * service (obtained by XORing metric class with service number) and values
     * represent the metric value for that service
     */
    protected final Map<Integer, Double> score;

    /** Type of evaluation protocol */
    protected final EvaluationProtocol evaluationProtocol;

    /** All accuracy metrics */
    protected final Map<Integer, Accuracy> accuracyMetrics;

    /** All utility metrics */
    protected final Map<Integer, Utility> utilityMetrics;

    /** All opinion cost metrics */
    protected final Map<Integer, OpinionCost> opinionCostMetrics;

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
     * {@link InteractionPartnerSelection} interface and the trust model
     * instance implements the {@link SelectingInteractionPartners}. This
     * combination constitutes the so called <b>utility mode</b>.
     * <li>In the second case, the scenario instance <b>does not</b> implement
     * the {@link InteractionPartnerSelection} interface, and the trust model
     * instance <b>does not</b> implement the
     * {@link SelectingInteractionPartners} interface. This combination
     * constitutes the so called <b>ranking mode</b>.
     * </ol>
     * If the given combination of scenario and trust model does not constitute
     * a valid combination, an {@link IllegalArgumentException} is thrown.
     * <li>An instance of the {@link Accuracy}. This instance is only used to
     * infer the type (i.e. class) for the ranking metric. The testbed will
     * create the actual instance of the {@link Accuracy} that will be used for
     * evaluation.
     * <li>The varargs parameter used to initialize a {@link Accuracy} instance.
     * The testbed uses these parameters when creates and initializes new
     * instances of the {@link Accuracy}.
     * <li>An instance of the {@link Utility}. This instance is only used to
     * infer the type (i.e. class) for the utility metric. The testbed will
     * create the actual instance of the {@link Utility} that will be used for
     * evaluation.
     * <li>The varargs parameter used to initialize a {@link Utility} instance.
     * The testbed uses these parameters when creates and initializes new
     * instances of the {@link Utility}.
     * </ol>
     * 
     * @param scn
     *            A scenario instance
     * @param tm
     *            A trust model instance
     * @param accuracy
     *            A ranking metric instance
     * @param accParams
     *            A set of varargs arguments to initialize the utility instance
     * @param utility
     *            A utility metric instance
     * @param utilParams
     *            A set of varargs arguments to initialize an utility instance
     */
    public AlphaTestbed(Scenario scn, TrustModel<?> tm, Accuracy accuracy,
	    Object[] accParams, Utility utility, Object[] utilParams,
	    OpinionCost opinionCost, Object[] ocParams) {
	trustModel = tm;
	scenario = scn;

	if (validModeSelectingOpinionProviders(tm, scn)) {
	    tmSelectingInteractions = (SelectingInteractionPartners) tm;
	    scnInteractionSelection = (InteractionPartnerSelection) scn;
	    tmSelectingOpinions = (SelectingOpinionProviders) tm;
	    scnOpinionSelection = (OpinionProviderSelection) scn;
	    accuracyClass = accuracy.getClass();
	    accuracyParameters = accParams;
	    accuracyMetrics = new HashMap<Integer, Accuracy>();
	    utilityClass = utility.getClass();
	    utilityParameters = utilParams;
	    utilityMetrics = new HashMap<Integer, Utility>();
	    opinionCostClass = opinionCost.getClass();
	    opinionCostParameters = ocParams;
	    opinionCostMetrics = new HashMap<Integer, OpinionCost>();
	    evaluationProtocol = SELECTING_OPINIONS_PROVIDERS;
	} else if (validModeSelectingInteractionPartners(tm, scn)) {
	    tmSelectingInteractions = (SelectingInteractionPartners) tm;
	    scnInteractionSelection = (InteractionPartnerSelection) scn;
	    tmSelectingOpinions = null;
	    scnOpinionSelection = null;
	    accuracyClass = accuracy.getClass();
	    accuracyParameters = accParams;
	    accuracyMetrics = new HashMap<Integer, Accuracy>();
	    utilityClass = utility.getClass();
	    utilityParameters = utilParams;
	    utilityMetrics = new HashMap<Integer, Utility>();
	    opinionCostClass = null;
	    opinionCostParameters = null;
	    opinionCostMetrics = null;
	    evaluationProtocol = SELECTING_INTERACTION_PARTNERS;
	} else if (validModeNoDecisions(tm, scn)) {
	    tmSelectingInteractions = null;
	    scnInteractionSelection = null;
	    tmSelectingOpinions = null;
	    scnOpinionSelection = null;
	    accuracyClass = accuracy.getClass();
	    accuracyParameters = accParams;
	    accuracyMetrics = new HashMap<Integer, Accuracy>();
	    utilityClass = null;
	    utilityParameters = null;
	    utilityMetrics = null;
	    opinionCostClass = null;
	    opinionCostParameters = null;
	    opinionCostMetrics = null;
	    evaluationProtocol = NO_DECISIONS;
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
	trustModel.setCurrentTime(time);
	scenario.setCurrentTime(time);

	switch (evaluationProtocol) {
	case NO_DECISIONS:
	    stepWithoutDecisions(time);
	    break;
	case SELECTING_INTERACTION_PARTNERS:
	    stepWithPartnerSelections(time);
	    break;
	case SELECTING_OPINIONS_PROVIDERS:
	    stepWithInteractionAndOpinionSelections(time);
	    break;
	default:
	    throw new Error("Unreachable code.");
	}

	// notify all subscribers
	notifiySubscribers();
    }

    /**
     * Performs one step through all phases of the evaluation protocol that
     * evaluates trust models by allowing them to select interaction partners
     * and opinion providers (mode B).
     * 
     * @param time
     *            Current time
     */
    public void stepWithInteractionAndOpinionSelections(int time) {
	// get agents
	final Set<Integer> agents = scenario.getAgents();

	// convey agents to TM
	tmSelectingOpinions.setAgents(agents);

	// get services
	final Set<Integer> services = scenario.getServices();

	// convey available services to TM
	tmSelectingOpinions.setServices(services);

	// get opinion requests
	final Set<OpinionRequest> opReqs;
	opReqs = Utils.ordered(tmSelectingOpinions.getOpinionRequests());

	// convey opinion requests to scenario
	scnOpinionSelection.setOpinionRequests(opReqs);

	// get opinions
	final Set<Opinion> opinions = scenario.generateOpinions();

	// convey opinions to the trust model
	trustModel.processOpinions(opinions);

	// get interaction partners from TM
	// Convert Map to a TreeMap to ensure deterministic iteration
	final Map<Integer, Integer> partners;
	partners = Utils.ordered(tmSelectingInteractions
		.getInteractionPartners(services));

	// convey partner selections to scenario
	scnInteractionSelection.setInteractionPartners(partners);

	// generate experiences
	final Set<Experience> experiences = scenario.generateExperiences();

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

	    final Integer agent = partners.get(service);

	    // utility -- if partner for this service was selected
	    if (null != agent) {
		final Utility utility = getUtilityInstance(service);
		final int utilKey = utility.getClass().hashCode() ^ service;
		final double utilValue = utility.evaluate(capabilities, agent);

		score.put(utilKey, utilValue);
	    }

	    // opinion cost
	    final OpinionCost opinionCost = getOpinionCostInstance(service);
	    final int ocKey = opinionCost.getClass().hashCode() ^ service;
	    final double ocValue;
	    ocValue = opinionCost.evaluate(agents, services, opReqs);

	    score.put(ocKey, ocValue);
	}
    }

    /**
     * Performs one step through all phases of the evaluation protocol that
     * evaluates trust models by allowing them to select interaction partners
     * (opinion providers are determined by the scenario -- mode A).
     * 
     * @param time
     *            Current time
     */
    public void stepWithPartnerSelections(int time) {
	// get opinions
	final Set<Opinion> opinions = scenario.generateOpinions();

	// convey opinions to the trust model
	trustModel.processOpinions(opinions);

	// get services
	final Set<Integer> services = scenario.getServices();

	// temporary var for interaction partners
	final Map<Integer, Integer> ipTemp;
	ipTemp = tmSelectingInteractions.getInteractionPartners(services);

	// Convert Map to a TreeMap to ensure deterministic iteration
	final Map<Integer, Integer> partners = Utils.ordered(ipTemp);

	// convey partner selection to the scenario
	scnInteractionSelection.setInteractionPartners(partners);

	// generate experiences
	final Set<Experience> experiences = scenario.generateExperiences();

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

	    final Integer agent = partners.get(service);

	    // utility -- if partner for this service was selected
	    if (null != agent) {
		final Utility utility = getUtilityInstance(service);
		final int utilKey = utility.getClass().hashCode() ^ service;
		final double utilValue = utility.evaluate(capabilities, agent);

		score.put(utilKey, utilValue);
	    }
	}
    }

    /**
     * Performs one step through all phases of the evaluation protocol that
     * evaluates trust models that have no decision making capabilities
     * (interaction partners and opinion providers are determined by the
     * scenario).
     * 
     * @param time
     *            Current time
     */
    public void stepWithoutDecisions(int time) {
	// get opinions
	final Set<Opinion> opinions = scenario.generateOpinions();

	// convey opinions
	trustModel.processOpinions(opinions);

	// get services
	final Set<Integer> services = scenario.getServices();

	// generate experiences
	final Set<Experience> experiences = scenario.generateExperiences();

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
    }

    /**
     * Returns the value of the given metric for the given service. This method
     * should be called by all instances of {@link MetricSubscriber} to pull the
     * data from the testbed.
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
	return trustModel;
    }

    public Scenario getScenario() {
	return scenario;
    }

    /**
     * Determines, if the combination of the trust model and the scenario
     * constitutes a valid mode where trust model selects interaction partners
     * while opinion providers are determined by the scenario (so called mode
     * A).
     * 
     * @param model
     *            Instance of a trust model
     * @param scenario
     *            Instance of a scenario
     * @return True, if and only if the instance of the trust model implements
     *         the {@link SelectingInteractionPartners} interface and the
     *         instance of a scenario implements the
     *         {@link InteractionPartnerSelection} interface.
     */
    protected boolean validModeSelectingInteractionPartners(
	    TrustModel<?> model, Scenario scenario) {
	return SelectingInteractionPartners.class.isAssignableFrom(model
		.getClass())
		&& InteractionPartnerSelection.class.isAssignableFrom(scenario
			.getClass());
    }

    /**
     * Determines, if the combination of the trust model and the scenario
     * constitutes a valid mode where trust model selects interaction partners
     * and opinion providers (so called mode B).
     * 
     * @param model
     *            Instance of a trust model
     * @param scenario
     *            Instance of a scenario
     * @return True, if and only if the instance of the trust model implements
     *         both the {@link SelectingInteractionPartners} and
     *         {@link SelectingOpinionProviders} interfaces while the scenario
     *         instance implements both the {@link InteractionPartnerSelection}
     *         and {@link OpinionProviderSelection} interfaces.
     */
    protected boolean validModeSelectingOpinionProviders(TrustModel<?> model,
	    Scenario scenario) {
	return SelectingOpinionProviders.class.isAssignableFrom(model
		.getClass())
		&& OpinionProviderSelection.class.isAssignableFrom(scenario
			.getClass())
		&& validModeSelectingInteractionPartners(model, scenario);
    }

    /**
     * Determines, if the combination of the trust model and the scenario
     * constitutes a valid mode where scenario determines interaction partners
     * and opinion providers.
     * 
     * @param model
     *            Instance of a trust model
     * @param scenario
     *            Instance of a scenario
     * @return True, if and only if the instance of the trust model does not
     *         implement the {@link SelectingInteractionPartners} interface and
     *         the instance of a scenario does not implement the
     *         {@link InteractionPartnerSelection} interface.
     */
    protected boolean validModeNoDecisions(TrustModel<?> model,
	    Scenario scenario) {
	return !SelectingInteractionPartners.class.isAssignableFrom(model
		.getClass())
		&& !InteractionPartnerSelection.class.isAssignableFrom(scenario
			.getClass());
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
	Utility metric = utilityMetrics.get(service);

	if (null == metric) {
	    try {
		metric = utilityClass.newInstance();
		metric.initialize(utilityParameters);
		utilityMetrics.put(service, metric);
	    } catch (Exception e) {
		throw new Error(String.format(CREATION_ERROR, utilityClass,
			Arrays.toString(utilityParameters)));
	    }
	}

	return metric;
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
	OpinionCost metric = opinionCostMetrics.get(service);

	if (null == metric) {
	    try {
		metric = opinionCostClass.newInstance();
		metric.initialize(utilityParameters);
		opinionCostMetrics.put(service, metric);
	    } catch (Exception e) {
		throw new Error(String.format(CREATION_ERROR, utilityClass,
			Arrays.toString(utilityParameters)));
	    }
	}

	return metric;
    }

    /**
     * Returns the evaluation protocol
     * 
     * @return
     */
    public EvaluationProtocol getEvaluationProtocol() {
	return evaluationProtocol;
    }
}
