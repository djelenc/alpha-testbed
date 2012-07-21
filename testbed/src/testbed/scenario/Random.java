package testbed.scenario;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import testbed.common.LexiographicComparator;
import testbed.common.Utils;
import testbed.deceptionmodel.NegativeExaggeration;
import testbed.deceptionmodel.PositiveExaggeration;
import testbed.deceptionmodel.Silent;
import testbed.interfaces.Experience;
import testbed.interfaces.ICondition;
import testbed.interfaces.IDeceptionModel;
import testbed.interfaces.IParametersPanel;
import testbed.interfaces.IScenario;
import testbed.interfaces.Opinion;

/**
 * A very simple scenario implementation.
 * <ul>
 * <li>The scenario only has one type of service (service "0").
 * <li>The assignment of capabilities is (uniformly) random. Capabilities are
 * generated at start and they do not change.
 * <li>The assignment of deception models is random but in compliance with the
 * distribution that is given as the parameter. Deception models are generated
 * at start and they do not change. Also, agents use the assigned deception
 * models consistently.
 * <li>Alpha interacts with only one agent per time tick.
 * <li>Alpha obtains opinions from all agents in every tick.
 * </ul>
 * 
 * <p>
 * The initialization method requires an array of objects that represent
 * parameters with the following semantics (corresponding to the indexes):
 * <ul>
 * <li>0: (int) number of agents
 * <li>1: (double) standard deviation for generating experiences
 * <li>2: (double) standard deviation for generation opinions
 * <li>3: (Map<{@link IDeceptionModel}, Double>) distribution of deception
 * models
 * <li>5: (double) negative exaggeration coefficient
 * <li>4: (double) positive exaggeration coefficient
 * </ul>
 * 
 * @author David
 * 
 */
public class Random extends AbstractScenario implements IScenario {
    private static final String DM_EX = "Could not get deception model for agent %d (%d total agents) from %s";
    private static final String TOTAL_PROB_EX = "The sum of probabilities must be %.2f, but was %.2f.";
    private static final String EXAGG_EX = "The exaggeration parameter must be between 0 and 1, but was %.2f";
    private static final String ST_DEV_EX = "The standard deviation must be a non-negative double, but was %.2f";
    private static final String AGENT_NUM_EX = "The number of agents and services must be positive integer, but was %d";

    protected final static ICondition<Integer> VAL_SIZE;
    protected final static ICondition<Double> VAL_SD, VAL_EXAGG;
    protected final static ICondition<Map<IDeceptionModel, Double>> VAL_PROB;

    protected static final Set<Integer> SERVICES = new HashSet<Integer>();

    static {
	VAL_SIZE = new ICondition<Integer>() {
	    @Override
	    public void eval(Integer var) {
		if (var < 1)
		    throw new IllegalArgumentException(String.format(
			    AGENT_NUM_EX, var));
	    }
	};

	VAL_SD = new ICondition<Double>() {
	    @Override
	    public void eval(Double var) {
		if (var < 0)
		    throw new IllegalArgumentException(String.format(ST_DEV_EX,
			    var));
	    }
	};

	VAL_EXAGG = new ICondition<Double>() {
	    @Override
	    public void eval(Double var) {
		if (var < 0 || var > 1)
		    throw new IllegalArgumentException(String.format(EXAGG_EX,
			    var));
	    }
	};

	VAL_PROB = new ICondition<Map<IDeceptionModel, Double>>() {
	    @Override
	    public void eval(Map<IDeceptionModel, Double> var) {
		double sum = 0;

		for (Map.Entry<IDeceptionModel, Double> pair : var.entrySet()) {
		    sum += pair.getValue();
		}

		if (Math.abs(1d - sum) > 0.001)
		    throw new IllegalArgumentException(String.format(
			    TOTAL_PROB_EX, 1d, sum));
	    }
	};

	SERVICES.add(0);
    }

    protected int time;

    protected Map<Integer, Double> capabilities;
    protected Map<Integer, IDeceptionModel> deceptionModels;
    protected Set<Integer> agents;

    protected double sd_i, sd_o, posExCoef, negExCoef;

    @Override
    public void initialize(Object... parameters) {
	capabilities = new LinkedHashMap<Integer, Double>();
	deceptionModels = new LinkedHashMap<Integer, IDeceptionModel>();
	agents = new LinkedHashSet<Integer>();
	time = 0;

	int numAgents = Utils.extractParameter(VAL_SIZE, 0, parameters);

	sd_i = Utils.extractParameter(VAL_SD, 1, parameters);
	sd_o = Utils.extractParameter(VAL_SD, 2, parameters);

	// PMF for assigning deception models.
	// We must use TreeMap to ensure deterministic iteration over it
	TreeMap<IDeceptionModel, Double> dmPMF = new TreeMap<IDeceptionModel, Double>(
		new LexiographicComparator());

	dmPMF.putAll(Utils.extractParameter(VAL_PROB, 3, parameters));

	posExCoef = Utils.extractParameter(VAL_EXAGG, 4, parameters);
	negExCoef = Utils.extractParameter(VAL_EXAGG, 5, parameters);

	// initialize deception models
	for (Map.Entry<IDeceptionModel, Double> dm : dmPMF.entrySet()) {
	    if (dm.getKey() instanceof PositiveExaggeration) {
		dm.getKey().initialize(posExCoef);
	    } else if (dm.getKey() instanceof NegativeExaggeration) {
		dm.getKey().initialize(negExCoef);
	    } else {
		dm.getKey().initialize();
	    }
	}

	// generate agents
	for (int agent = 0; agent < numAgents; agent++) {
	    // add agent to set
	    agents.add(agent);

	    // assign capabilities
	    capabilities.put(agent, Utils.randomUnif(0, 1));

	    // assign deception model
	    deceptionModels.put(agent, getDM(agent, numAgents, dmPMF));
	}
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
    public IDeceptionModel getDM(int agent, int numAgents,
	    TreeMap<IDeceptionModel, Double> dmPMF) {
	TreeMap<IDeceptionModel, Integer> cumulative = new TreeMap<IDeceptionModel, Integer>(
		new LexiographicComparator());
	IDeceptionModel dm;
	float previous = 0;

	for (Entry<IDeceptionModel, Double> d : dmPMF.entrySet()) {
	    previous += d.getValue();
	    dm = d.getKey();
	    cumulative.put(dm, Math.round(previous * numAgents));
	}

	for (Entry<IDeceptionModel, Integer> d : cumulative.entrySet()) {
	    if (agent < d.getValue()) {
		return d.getKey();
	    }
	}

	throw new IllegalArgumentException(String.format(DM_EX, agent,
		numAgents, dmPMF));
    }

    @Override
    public Set<Opinion> generateOpinions() {
	Set<Opinion> opinions = new HashSet<Opinion>();

	Opinion opinion = null;
	IDeceptionModel deceptionModel = null;
	double cap, itd;

	for (int agent1 : agents) {
	    for (int agent2 : agents) {
		for (int service : SERVICES) {
		    // get deception model
		    deceptionModel = deceptionModels.get(agent1);

		    // if DM is not Silent, generate opinion
		    if (!(deceptionModel instanceof Silent)) {
			// get capability
			cap = capabilities.get(agent2);

			// generate internal trust degree
			itd = Utils.randomTND(cap, sd_o);
			itd = deceptionModel.calculate(itd);

			// create opinion tuple and add it to list
			opinion = new Opinion(agent1, agent2, service, time,
				itd);
			opinions.add(opinion);
		    }
		}
	    }
	}

	return opinions;
    }

    @Override
    public Set<Experience> generateExperiences() {
	Set<Experience> experiences = new HashSet<Experience>();

	Experience experience = null;
	int agent = -1;
	double cap, outcome;

	for (int service : SERVICES) {
	    // get agent to interact with
	    agent = time % agents.size();

	    // generate interaction outcome
	    cap = capabilities.get(agent);
	    outcome = Utils.randomTND(cap, sd_i);

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
    public Set<Integer> getAgents() {
	return agents;
    }

    @Override
    public Set<Integer> getServices() {
	return SERVICES;
    }

    @Override
    public IParametersPanel getParametersPanel() {
	return new RandomGUI();
    }

    @Override
    public void setCurrentTime(int time) {
	this.time = time;
    }
}
