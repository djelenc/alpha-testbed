package testbed.scenario;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import testbed.common.LexiographicComparator;
import testbed.common.Utils;
import testbed.deceptionmodel.NegativeExaggeration;
import testbed.deceptionmodel.PositiveExaggeration;
import testbed.deceptionmodel.RandomDeception;
import testbed.deceptionmodel.Silent;
import testbed.deceptionmodel.Truthful;
import testbed.interfaces.DeceptionModel;
import testbed.interfaces.Experience;
import testbed.interfaces.Opinion;
import testbed.interfaces.ParameterCondition;
import testbed.interfaces.ParametersPanel;
import testbed.interfaces.Scenario;

/**
 * A very simple scenario implementation.
 * <ul>
 * <li>The scenario only has one type of service (service "0").
 * <li>The assignment of capabilities is (uniformly) random. Capabilities are
 * generated at start and they do not change.
 * <li>The assignment of deception models is random but follows the distribution
 * that is given as the parameter. Deception models are generated at start and
 * they do not change. Also, agents use the assigned deception models
 * consistently -- for instance, if an agent is using a {@link Truthful}
 * deception model, then that agents reports a truthful opinion for all agents.
 * <li>Alpha interacts with only one agent per time tick.
 * <li>Alpha obtains opinions from all agents in every tick.
 * <li>Every agent in the system has an opinion about all other agents in the
 * system (i.e. everyone knows everyone). The only exception is if an agent is
 * using a {@link Silent} deception model. In that case, such agent does not
 * give any opinions.
 * </ul>
 * 
 * <p>
 * The initialization method requires an array of objects that represent
 * parameters with the following semantics (corresponding to the indexes):
 * <ul>
 * <li>0: (int) number of agents
 * <li>1: (double) standard deviation for generating experiences
 * <li>2: (double) standard deviation for generation opinions
 * <li>3: (Map<{@link DeceptionModel}, Double>) distribution of deception models
 * <li>4: (double) positive exaggeration coefficient
 * <li>5: (double) negative exaggeration coefficient
 * <li>6: (double) ratio between the number of distinct interaction partners and
 * all agents
 * </ul>
 * 
 * @author David
 * 
 */
public class Random extends AbstractScenario implements Scenario {
    protected static final String DENS_EX = "The density must be between 0 and 1 inclusively, but was %.2f";
    protected static final String DM_EX = "Could not get deception model for agent %d (%d total agents) from %s";
    protected static final String TOTAL_PROB_EX = "The sum of probabilities must be %.2f, but was %.2f.";
    protected static final String EXAGG_EX = "The exaggeration parameter must be between 0 and 1, but was %.2f";
    protected static final String ST_DEV_EX = "The standard deviation must be a non-negative double, but was %.2f";
    protected static final String AGENT_NUM_EX = "The number of agents and services must be positive integer, but was %d";

    protected final static ParameterCondition<Integer> VAL_SIZE;
    protected final static ParameterCondition<Double> VAL_SD, VAL_EXAGG,
	    VAL_DENS;
    protected final static ParameterCondition<Map<DeceptionModel, Double>> VAL_PROB;

    protected static final List<Integer> SERVICES;

    static {
	VAL_SIZE = new ParameterCondition<Integer>() {
	    @Override
	    public void eval(Integer var) {
		if (var < 1)
		    throw new IllegalArgumentException(String.format(
			    AGENT_NUM_EX, var));
	    }
	};

	VAL_SD = new ParameterCondition<Double>() {
	    @Override
	    public void eval(Double var) {
		if (var < 0)
		    throw new IllegalArgumentException(String.format(ST_DEV_EX,
			    var));
	    }
	};

	VAL_EXAGG = new ParameterCondition<Double>() {
	    @Override
	    public void eval(Double var) {
		if (var < 0 || var > 1)
		    throw new IllegalArgumentException(String.format(EXAGG_EX,
			    var));
	    }
	};

	VAL_PROB = new ParameterCondition<Map<DeceptionModel, Double>>() {
	    @Override
	    public void eval(Map<DeceptionModel, Double> var) {
		double sum = 0;

		for (Map.Entry<DeceptionModel, Double> pair : var.entrySet()) {
		    sum += pair.getValue();
		}

		if (Math.abs(1d - sum) > 0.001)
		    throw new IllegalArgumentException(String.format(
			    TOTAL_PROB_EX, 1d, sum));
	    }
	};

	VAL_DENS = new ParameterCondition<Double>() {
	    @Override
	    public void eval(Double var) {
		if (var < 0 || var > 1)
		    throw new IllegalArgumentException(String.format(DENS_EX,
			    var));
	    }
	};

	SERVICES = new ArrayList<Integer>();
	SERVICES.add(0);
    }

    protected int time;

    protected Map<Integer, Double> capabilities;
    protected Map<Integer, DeceptionModel> deceptionModels;
    protected List<Integer> agents;

    protected double sd_i, sd_o, posExCoef, negExCoef, interDens;

    // Set of Alpha's interaction partners (subset of agents)
    protected List<Integer> partners;

    @Override
    public void initialize(Object... parameters) {
	capabilities = new LinkedHashMap<Integer, Double>();
	deceptionModels = new LinkedHashMap<Integer, DeceptionModel>();
	agents = new ArrayList<Integer>();
	partners = new ArrayList<Integer>();
	time = 0;

	int numAgents = Utils.extractParameter(VAL_SIZE, 0, parameters);

	sd_i = Utils.extractParameter(VAL_SD, 1, parameters);
	sd_o = Utils.extractParameter(VAL_SD, 2, parameters);

	// PMF for assigning deception models.
	// We must use TreeMap to ensure deterministic iteration over it
	TreeMap<DeceptionModel, Double> dmPMF = new TreeMap<DeceptionModel, Double>(
		new LexiographicComparator());

	dmPMF.putAll(Utils.extractParameter(VAL_PROB, 3, parameters));

	posExCoef = Utils.extractParameter(VAL_EXAGG, 4, parameters);
	negExCoef = Utils.extractParameter(VAL_EXAGG, 5, parameters);

	// initialize deception models
	for (Map.Entry<DeceptionModel, Double> dm : dmPMF.entrySet()) {
	    final DeceptionModel model = dm.getKey();

	    if (model instanceof PositiveExaggeration) {
		model.initialize(posExCoef);
	    } else if (model instanceof NegativeExaggeration) {
		model.initialize(negExCoef);
	    } else if (model instanceof RandomDeception) {
		model.initialize(generator);
	    } else if (!(model instanceof Silent)) {
		model.initialize();
	    }
	}

	// generate agents
	for (int agent = 0; agent < numAgents; agent++) {
	    // add agent to set
	    agents.add(agent);

	    // assign capabilities
	    capabilities.put(agent, generator.nextDoubleFromTo(0, 1));

	    // assign deception model
	    deceptionModels.put(agent, getDM(agent, numAgents, dmPMF));
	}

	interDens = Utils.extractParameter(VAL_DENS, 6, parameters);

	partners.addAll(generator.chooseRandom(agents, interDens));
    }

    /**
     * Gets an IDeceptionModel instance for an agent with given PMF
     * 
     * @param agent
     *            Index of an agent
     * @param numAgents
     *            The total number of agents in the system
     * @param dmPMF
     *            The probability mass function of deception models
     * @return
     */
    public DeceptionModel getDM(int agent, int numAgents,
	    TreeMap<DeceptionModel, Double> dmPMF) {
	TreeMap<DeceptionModel, Integer> cumulative = new TreeMap<DeceptionModel, Integer>(
		new LexiographicComparator());
	float previous = 0;

	for (Entry<DeceptionModel, Double> d : dmPMF.entrySet()) {
	    previous += d.getValue();
	    final DeceptionModel dm = d.getKey();
	    cumulative.put(dm, Math.round(previous * numAgents));
	}

	for (Entry<DeceptionModel, Integer> d : cumulative.entrySet()) {
	    if (agent < d.getValue()) {
		if (d.getKey() instanceof Silent) {
		    return null;
		} else {
		    return d.getKey();
		}
	    }
	}

	throw new IllegalArgumentException(String.format(DM_EX, agent,
		numAgents, dmPMF));
    }

    @Override
    public List<Opinion> generateOpinions() {
	List<Opinion> opinions = new ArrayList<Opinion>();

	for (int agent1 : agents) {
	    for (int agent2 : agents) {
		for (int service : SERVICES) {
		    // get deception model
		    final DeceptionModel deceptionModel = deceptionModels
			    .get(agent1);

		    // if DM is not Silent, generate opinion
		    if (deceptionModel != null) {
			// get capability
			final double capability = capabilities.get(agent2);

			// generate internal trust degree
			final double internalTrustDegree = generator
				.nextDoubleFromUnitTND(capability, sd_o);
			final double communicatedInternalTrustDegree = deceptionModel
				.calculate(internalTrustDegree);

			// create opinion tuple and add it to list
			final Opinion opinion = new Opinion(agent1, agent2,
				service, time, communicatedInternalTrustDegree,
				sd_o);
			opinions.add(opinion);
		    }
		}
	    }
	}

	return opinions;
    }

    @Override
    public List<Experience> generateExperiences() {
	List<Experience> experiences = new ArrayList<Experience>();

	Experience experience = null;
	int agent = -1;
	double cap, outcome;

	for (int service : SERVICES) {
	    // get agent to interact with
	    agent = partners.get(time % partners.size());

	    // generate interaction outcome
	    cap = capabilities.get(agent);
	    outcome = generator.nextDoubleFromUnitTND(cap, sd_i);

	    // create experience tuple and add it to list
	    experience = new Experience(agent, service, time, outcome);
	    experiences.add(experience);
	}

	return experiences;
    }

    @Override
    public Map<Integer, Double> getCapabilities(int service) {
	return capabilities;
    }

    @Override
    public List<Integer> getAgents() {
	return agents;
    }

    @Override
    public List<Integer> getServices() {
	return SERVICES;
    }

    @Override
    public ParametersPanel getParametersPanel() {
	return new RandomGUI();
    }

    @Override
    public void setCurrentTime(int time) {
	this.time = time;
    }
}
