package testbed.scenario;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import testbed.common.LexiographicComparator;
import testbed.common.Utils;
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
 * This scenario implementation differs from {@link Random} in the way that the
 * assignment of capabilities and deception models is different for every
 * service. All other aspects of the scenario are the same in comparison to
 * {@link Random}.
 * 
 * @author David
 * 
 */
public class RandomMultiService extends AbstractScenario implements IScenario {
    protected int time;

    protected Map<Integer, Double> capabilities;
    protected Map<Integer, IDeceptionModel> deceptionModels;
    protected Set<Integer> agents, services;

    protected double sd_i, sd_o, posExCoef, negExCoef;

    protected boolean numAgentsLarger = true;
    protected int pivot = 0;

    @Override
    public void initialize(Object... parameters) {
	capabilities = new LinkedHashMap<Integer, Double>();
	deceptionModels = new LinkedHashMap<Integer, IDeceptionModel>();
	agents = new LinkedHashSet<Integer>();
	services = new LinkedHashSet<Integer>();

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
	int numServices = Utils.extractParameter(validatorSize, 1, parameters);

	ICondition<Double> validator = new ICondition<Double>() {
	    @Override
	    public void eval(Double var) {
		if (var < 0)
		    throw new IllegalArgumentException(
			    String.format(
				    "The standard deviation must be a non-negative double, but was %.2f",
				    var));
	    }
	};

	sd_i = Utils.extractParameter(validator, 2, parameters);
	sd_o = Utils.extractParameter(validator, 3, parameters);

	// PMF for assigning deception models
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
		}, 4, parameters));

	// generate services
	for (int i = 0; i < numServices; i++) {
	    services.add(i);
	}

	final ICondition<Double> validatorExagg = new ICondition<Double>() {
	    @Override
	    public void eval(Double var) {
		if (var < 0 || var > 1)
		    throw new IllegalArgumentException(
			    String.format(
				    "The exaggeration parameter must be between 0 and 1, but was %.2f",
				    var));
	    }
	};

	posExCoef = Utils.extractParameter(validatorExagg, 5, parameters);
	negExCoef = Utils.extractParameter(validatorExagg, 6, parameters);

	// calculate combined map key for agents and services
	this.numAgentsLarger = numAgents >= numServices;
	this.pivot = (numAgentsLarger ? numAgents : numServices);

	int key = 0;

	// generate agents
	for (int i = 0; i < numAgents; i++) {
	    // add agent to set
	    agents.add(i);

	    for (int j = 0; j < numServices; j++) {
		// calculate key for Map
		key = (numAgentsLarger ? pivot * i + j : pivot * j + i);

		// assign capability
		capabilities.put(key, generator.nextDoubleFromTo(0, 1));

		final IDeceptionModel model = generator.fromWeights(dmPMF);

		if (model instanceof PositiveExaggeration) {
		    model.initialize(posExCoef);
		} else if (model instanceof NegativeExaggeration) {
		    model.initialize(negExCoef);
		} else if (model instanceof RandomDeception) {
		    model.initialize(generator);
		} else {
		    model.initialize();
		}

		// assign deception model
		deceptionModels.put(key, model);
	    }
	}
    }

    @Override
    public Set<Opinion> generateOpinions() {
	Set<Opinion> opinions = new HashSet<Opinion>();

	Opinion opinion = null;
	IDeceptionModel deceptionModel = null;
	double cap, itd;
	int key1, key2;

	for (int agent1 : agents) {
	    for (int agent2 : agents) {
		for (int service : services) {
		    // calculate key1
		    key1 = (numAgentsLarger ? pivot * agent1 + service : pivot
			    * service + agent1);

		    // get deception model
		    deceptionModel = deceptionModels.get(key1);

		    // if DM is not Silent, generate opinion
		    if (!(deceptionModel instanceof Silent)) {
			// calculate key2
			key2 = (numAgentsLarger ? pivot * agent2 + service
				: pivot * service + agent2);

			// get capability
			cap = capabilities.get(key2);

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
	int agent = -1, key = 0;
	double cap, outcome;

	for (int service : services) {
	    // get agent to interact with
	    agent = time % agents.size();

	    // calculate key
	    key = (numAgentsLarger ? pivot * agent + service : pivot * service
		    + agent);

	    // generate interaction outcome
	    cap = capabilities.get(key);
	    outcome = generator.nextDoubleFromUnitTND(cap, sd_i);

	    // create experience tuple and add it to list
	    experience = new Experience(agent, service, time, outcome);
	    experiences.add(experience);
	}

	return experiences;
    }

    @Override
    public Map<Integer, Double> getCapabilities(int service) {
	Map<Integer, Double> result = new LinkedHashMap<Integer, Double>();
	int key = 0;

	for (int agent : agents) {
	    key = (numAgentsLarger ? pivot * agent + service : pivot * service
		    + agent);
	    result.put(agent, capabilities.get(key));
	}

	return result;
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
    public void setCurrentTime(int time) {
	this.time = time;
    }

    @Override
    public IParametersPanel getParametersPanel() {
	return new RandomMultiServiceGUI();
    }

    @Override
    public String toString() {
	return "Random with multiple services";
    }
}
