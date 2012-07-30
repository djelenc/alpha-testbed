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
import testbed.deceptionmodel.Complementary;
import testbed.deceptionmodel.NegativeExaggeration;
import testbed.deceptionmodel.PositiveExaggeration;
import testbed.deceptionmodel.RandomDeception;
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
 * <li>The scenario only has one type of service.
 * <li>The assignment of capabilities is uniformly random. Capabilities are
 * generated at start and they do not change.
 * <li>The assignment of deception models is random. Deception models are
 * generated at start and they do not change. Also, agents use the assigned
 * deception models consistently.
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
 * <li>3: (Map<IDeceptionModel, Double>) distribution of deception models
 * <li>4: (double) positive exaggeration coefficient
 * <li>5: (double) negative exaggeration coefficient
 * </ul>
 * 
 * @author David
 * 
 */
public class RandomWithNewCommers extends AbstractScenario implements IScenario {
    protected int time;

    protected Map<Integer, Double> capabilities;
    protected Map<Integer, IDeceptionModel> deceptionModels;
    protected Set<Integer> agents, services;

    protected double sd_i, sd_o, posExCoef, negExCoef;

    @Override
    public void initialize(Object... parameters) {
	capabilities = new LinkedHashMap<Integer, Double>();
	deceptionModels = new LinkedHashMap<Integer, IDeceptionModel>();
	agents = new LinkedHashSet<Integer>();
	services = new HashSet<Integer>();
	services.add(0);
	time = 0;

	ICondition<Integer> validatorSize = new ICondition<Integer>() {
	    @Override
	    public void eval(Integer var) {
		if (var < 1)
		    throw new IllegalArgumentException(
			    String.format(
				    "The number of agents and services must be non negative integer, but was %d",
				    var));
	    }
	};

	int numAgents = Utils.extractParameter(validatorSize, 0, parameters);

	ICondition<Double> validatorSD = new ICondition<Double>() {
	    @Override
	    public void eval(Double var) {
		if (var < 0)
		    throw new IllegalArgumentException(
			    String.format(
				    "The standard deviation must be a non-negative double, but was %.2f",
				    var));
	    }
	};

	sd_i = Utils.extractParameter(validatorSD, 1, parameters);
	sd_o = Utils.extractParameter(validatorSD, 2, parameters);

	// PMF for assigning deception models.
	// We must use TreeMap to ensure deterministic iteration over map
	TreeMap<IDeceptionModel, Double> dmPMF = new TreeMap<IDeceptionModel, Double>(
		new LexiographicComparator());
	dmPMF.putAll(Utils.extractParameter(
		new ICondition<Map<IDeceptionModel, Double>>() {
		    @Override
		    public void eval(Map<IDeceptionModel, Double> var) {
			double sum = 0;

			for (Map.Entry<IDeceptionModel, Double> pair : var
				.entrySet()) {
			    sum += pair.getValue();
			}

			if (Math.abs(1d - sum) > 0.001)
			    throw new IllegalArgumentException(
				    String.format(
					    "The sum of probabilities must be %.2f, but was %.2f.",
					    1d, sum));
		    }
		}, 3, parameters));

	ICondition<Double> validatorExagg = new ICondition<Double>() {
	    @Override
	    public void eval(Double var) {
		if (var < 0 || var > 1)
		    throw new IllegalArgumentException(
			    String.format(
				    "The exaggeration parameter must be between 0 and 1, but was %.2f",
				    var));
	    }
	};

	posExCoef = Utils.extractParameter(validatorExagg, 4, parameters);
	negExCoef = Utils.extractParameter(validatorExagg, 5, parameters);

	// initialize deception models
	for (Map.Entry<IDeceptionModel, Double> dm : dmPMF.entrySet()) {
	    if (dm.getKey() instanceof PositiveExaggeration) {
		dm.getKey().initialize(posExCoef);
	    } else if (dm.getKey() instanceof NegativeExaggeration) {
		dm.getKey().initialize(negExCoef);
	    } else if (dm.getKey() instanceof RandomDeception) {
		dm.getKey().initialize(generator);
	    } else {
		dm.getKey().initialize();
	    }
	}

	// generate agents
	for (int i = 0; i < numAgents; i++) {
	    // add agent to set
	    agents.add(i);

	    // assign capabilities
	    capabilities.put(i, generator.nextDoubleFromTo(0, 1));

	    // assign deception model
	    deceptionModels.put(i, getDM(i, numAgents, dmPMF));
	}
    }

    /**
     * Gets an IDeceptionModel instance for an agent with given PMF
     * 
     * @param i
     *            Index of an agent
     * @param numAgents
     *            The total number of agents in the system
     * @param dmPMF
     *            The probability mass function of deception models
     * @return
     */
    public IDeceptionModel getDM(int i, int numAgents,
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
	    if (i < d.getValue()) {
		return d.getKey();
	    }
	}

	throw new IllegalArgumentException("Cound not get IDeceptionModel!");
    }

    @Override
    public Set<Opinion> generateOpinions() {
	Set<Opinion> opinions = new HashSet<Opinion>();

	Opinion opinion = null;
	IDeceptionModel deceptionModel = null;
	double cap, itd;

	for (int agent1 : agents) {
	    for (int agent2 : agents) {
		for (int service : services) {
		    // get deception model
		    deceptionModel = deceptionModels.get(agent1);

		    // if DM is not Silent, generate opinion
		    if (!(deceptionModel instanceof Silent)) {
			// get capability
			cap = capabilities.get(agent2);

			// generate internal trust degree
			itd = generator.nextDoubleFromUnitTND(cap, sd_o);
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

	for (int service : services) {
	    // get agent to interact with
	    agent = time % agents.size();

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
    public Set<Integer> getAgents() {
	return agents;
    }

    @Override
    public Set<Integer> getServices() {
	return services;
    }

    @Override
    public IParametersPanel getParametersPanel() {
	return new RandomGUI();
    }

    @Override
    public void setCurrentTime(int time) {
	this.time = time;

	if (0 == time % 100) {
	    final IDeceptionModel dm = new Complementary();
	    dm.initialize();
	    final int newSize = agents.size() + 50;

	    for (int agent = agents.size(); agent < newSize; agent++) {
		// add agent
		agents.add(agent);

		// assign capabilities
		capabilities.put(agent, generator.nextDoubleFromTo(0, 1));

		// assign deception model
		deceptionModels.put(agent, dm);
	    }
	}
    }
}
