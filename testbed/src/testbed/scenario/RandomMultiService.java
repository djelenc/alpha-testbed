/*
 * Copyright (c) 2013 David Jelenc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     David Jelenc - initial API and implementation
 */
package testbed.scenario;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import testbed.common.LexiographicComparator;
import testbed.common.Utils;
import testbed.deceptionmodel.NegativeExaggeration;
import testbed.deceptionmodel.PositiveExaggeration;
import testbed.deceptionmodel.RandomDeception;
import testbed.deceptionmodel.Silent;
import testbed.interfaces.DeceptionModel;
import testbed.interfaces.Experience;
import testbed.interfaces.Opinion;
import testbed.interfaces.ParameterCondition;
import testbed.interfaces.ParametersPanel;
import testbed.interfaces.Scenario;

/**
 * This scenario implementation differs from {@link Random} in the way that the
 * assignment of capabilities and deception models is different for every
 * service. All other aspects of the scenario are the same in comparison to
 * {@link Random}.
 * 
 * @author David
 * 
 */
public class RandomMultiService extends AbstractScenario implements Scenario {
    protected int time;

    protected Map<Integer, Double> capabilities;
    protected Map<Integer, DeceptionModel> deceptionModels;
    protected List<Integer> agents, services;

    protected double sd_i, sd_o, posExCoef, negExCoef;

    protected boolean numAgentsLarger = true;
    protected int pivot = 0;

    @Override
    public void initialize(Object... parameters) {
	capabilities = new LinkedHashMap<Integer, Double>();
	deceptionModels = new LinkedHashMap<Integer, DeceptionModel>();
	agents = new ArrayList<Integer>();
	services = new ArrayList<Integer>();

	ParameterCondition<Integer> validatorSize = new ParameterCondition<Integer>() {
	    @Override
	    public void eval(Integer var) {
		if (var < 1)
		    throw new IllegalArgumentException(String.format(
			    "The number of agents and services must be non negative integer, but was %d",
			    var));
	    }
	};

	int numAgents = Utils.extractParameter(validatorSize, 0, parameters);
	int numServices = Utils.extractParameter(validatorSize, 1, parameters);

	ParameterCondition<Double> validator = new ParameterCondition<Double>() {
	    @Override
	    public void eval(Double var) {
		if (var < 0)
		    throw new IllegalArgumentException(String.format(
			    "The standard deviation must be a non-negative double, but was %.2f",
			    var));
	    }
	};

	sd_i = Utils.extractParameter(validator, 2, parameters);
	sd_o = Utils.extractParameter(validator, 3, parameters);

	// PMF for assigning deception models
	TreeMap<DeceptionModel, Double> dmPMF = new TreeMap<DeceptionModel, Double>(
		new LexiographicComparator());
	dmPMF.putAll(Utils.extractParameter(
		new ParameterCondition<Map<DeceptionModel, Double>>() {
		    @Override
		    public void eval(Map<DeceptionModel, Double> var) {
			double sum = 0;

			for (Map.Entry<DeceptionModel, Double> pair : var
				.entrySet()) {
			    sum += pair.getValue();
			}

			if (Math.abs(1d - sum) > 0.001)
			    throw new IllegalArgumentException(String.format(
				    "The sum of probabilities must be %.2f, but was %.2f.",
				    1d, sum));
		    }
		}, 4, parameters));

	// generate services
	for (int i = 0; i < numServices; i++) {
	    services.add(i);
	}

	final ParameterCondition<Double> validatorExagg = new ParameterCondition<Double>() {
	    @Override
	    public void eval(Double var) {
		if (var < 0 || var > 1)
		    throw new IllegalArgumentException(String.format(
			    "The exaggeration parameter must be between 0 and 1, but was %.2f",
			    var));
	    }
	};

	posExCoef = Utils.extractParameter(validatorExagg, 5, parameters);
	negExCoef = Utils.extractParameter(validatorExagg, 6, parameters);

	// calculate combined map key for agents and services
	this.numAgentsLarger = numAgents >= numServices;
	this.pivot = (numAgentsLarger ? numAgents : numServices);

	// generate agents
	for (int i = 0; i < numAgents; i++) {
	    // add agent to set
	    agents.add(i);

	    for (int j = 0; j < numServices; j++) {
		// calculate key for Map
		final int key = (numAgentsLarger ? pivot * i + j
			: pivot * j + i);

		// assign capability
		capabilities.put(key, generator.nextDoubleFromTo(0, 1));

		// assign deception models
		DeceptionModel model = generator.fromWeights(dmPMF);

		if (model instanceof PositiveExaggeration) {
		    model.initialize(posExCoef);
		} else if (model instanceof NegativeExaggeration) {
		    model.initialize(negExCoef);
		} else if (model instanceof RandomDeception) {
		    model.initialize(generator);
		} else if (model instanceof Silent) {
		    model = null;
		} else {
		    model.initialize();
		}

		deceptionModels.put(key, model);
	    }
	}
    }

    @Override
    public List<Opinion> generateOpinions() {
	List<Opinion> opinions = new ArrayList<Opinion>();

	for (int agent1 : agents) {
	    for (int agent2 : agents) {
		for (int service : services) {
		    // calculate key1
		    final int key1 = (numAgentsLarger ? pivot * agent1 + service
			    : pivot * service + agent1);

		    // get deception model
		    final DeceptionModel deceptionModel = deceptionModels
			    .get(key1);

		    // generate opinion if DM is not Silent
		    if (deceptionModel != null) {
			// calculate key2
			final int key2 = (numAgentsLarger
				? pivot * agent2 + service
				: pivot * service + agent2);

			// get capability
			final double cap = capabilities.get(key2);

			// generate internal trust degree
			double itd = generator.nextDoubleFromUnitTND(cap, sd_o);
			itd = deceptionModel.calculate(itd);

			// create opinion tuple and add it to list
			final Opinion opinion = new Opinion(agent1, agent2,
				service, time, itd, sd_o);
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
	int agent = -1, key = 0;
	double cap, outcome;

	for (int service : services) {
	    // get agent to interact with
	    agent = time % agents.size();

	    // calculate key
	    key = (numAgentsLarger ? pivot * agent + service
		    : pivot * service + agent);

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
	    key = (numAgentsLarger ? pivot * agent + service
		    : pivot * service + agent);
	    result.put(agent, capabilities.get(key));
	}

	return result;
    }

    @Override
    public List<Integer> getAgents() {
	return agents;
    }

    @Override
    public List<Integer> getServices() {
	return services;
    }

    @Override
    public void setCurrentTime(int time) {
	this.time = time;
    }

    @Override
    public ParametersPanel getParametersPanel() {
	return new RandomMultiServiceGUI();
    }

    @Override
    public String toString() {
	return "Random with multiple services";
    }
}
