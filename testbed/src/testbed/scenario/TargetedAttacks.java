package testbed.scenario;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import testbed.deceptionmodel.Complementary;
import testbed.deceptionmodel.Truthful;
import testbed.interfaces.DeceptionModel;
import testbed.interfaces.Experience;
import testbed.interfaces.Opinion;
import testbed.interfaces.ParametersPanel;

/**
 * A scenario where attackers lie only about specific agents.
 * 
 * @author David
 * 
 */
public class TargetedAttacks extends AbstractScenario {

    // set of services -- only 1 service
    protected static final List<Integer> SERVICES = new ArrayList<Integer>();

    // deception models
    protected static final DeceptionModel HONEST = new Truthful();
    protected static final DeceptionModel LIAR = new Complementary();

    // time
    protected int time;

    // agents
    protected List<Integer> agents, neutral, attackers, targets;

    // capabilities
    protected Map<Integer, Double> capabilities;

    // deception models
    protected DeceptionModel[][] models;

    // total number of agents
    protected int numAgents;

    // standard deviations for generating interaction and opinions
    protected double sd_i, sd_o;

    static {
	SERVICES.add(0);
	HONEST.initialize();
	LIAR.initialize();
    }

    @Override
    public void initialize(Object... parameters) {
	// extract parameters
	numAgents = 50;
	sd_i = 0.10;
	sd_o = 0.05;

	// create agents
	agents = new ArrayList<Integer>();

	for (int agent = 0; agent < numAgents; agent++) {
	    agents.add(agent);
	}

	neutral = new ArrayList<Integer>();
	attackers = new ArrayList<Integer>();
	targets = new ArrayList<Integer>();

	// assign agents to groups
	assignAgentsToGroups(agents, neutral, attackers, targets);

	// assign capabilities
	capabilities = new LinkedHashMap<Integer, Double>();

	for (int agent : agents) {
	    // assign capability
	    final double cap;

	    if (targets.contains(agent)) {
		cap = generator.nextDoubleFromTo(0.9, 1);
	    } else {
		cap = generator.nextDoubleFromTo(0, 1);
	    }

	    capabilities.put(agent, cap);
	}

	// assign deception models
	models = new DeceptionModel[agents.size()][agents.size()];
	assignDeceptionModels(agents, neutral, attackers, targets, models);

	// reset time
	time = 0;

	System.out.printf("N: %s\n", neutral);
	System.out.printf("T: %s\n", targets);
	System.out.printf("A: %s\n", attackers);
	System.out.printf("C: %s\n", capabilities);
    }

    public void assignDeceptionModels(List<Integer> agents,
	    List<Integer> neutral, List<Integer> attackers,
	    List<Integer> targets, DeceptionModel[][] models) {
	boolean neutralReporter, neutralAgent, attackerReporter, attackerAgent, targetReporter, targetAgent;

	for (int reporter : agents) {
	    for (int agent : agents) {
		if (reporter != agent) {
		    neutralReporter = neutral.contains(reporter);
		    neutralAgent = neutral.contains(agent);
		    attackerReporter = attackers.contains(reporter);
		    attackerAgent = attackers.contains(agent);
		    targetReporter = targets.contains(reporter);
		    targetAgent = targets.contains(agent);

		    if ((neutralReporter || targetReporter)
			    && (neutralAgent || targetAgent)) {
			models[reporter][agent] = HONEST;
			// with some P, this should be nulls
		    } else if ((neutralReporter || targetReporter)
			    && attackerAgent) {
			models[reporter][agent] = null;
		    } else if (attackerReporter
			    && (neutralAgent || attackerAgent)) {
			
			if (neutralAgent)
			    models[reporter][agent] = null;
			else 
			    models[reporter][agent] = HONEST;
		    } else if (attackerReporter && targetAgent) {
			models[reporter][agent] = LIAR;
		    } else {
			throw new IllegalArgumentException(String.format(
				"Cannot determine deception model for reporter "
					+ "%d (c=%.2f) and agent %d (c=%.2f)",
				reporter, capabilities.get(reporter), agent,
				capabilities.get(agent)));
		    }
		}
	    }
	}
    }

    public void assignAgentsToGroups(List<Integer> allAgents,
	    List<Integer> neutral, List<Integer> attackers,
	    List<Integer> targets) {

	neutral.clear();
	attackers.clear();
	targets.clear();

	for (int agent : allAgents) {
	    if (agent < 25) {
		neutral.add(agent);
	    } else if (agent < 40) {
		attackers.add(agent);
	    } else {
		targets.add(agent);
	    }
	}
    }

    @Override
    public void setCurrentTime(int time) {
	this.time = time;
    }

    @Override
    public Map<Integer, Double> getCapabilities(int service) {
	return capabilities;
    }

    @Override
    public List<Opinion> generateOpinions() {
	List<Opinion> opinions = new ArrayList<Opinion>();
	for (int reporter : agents) {
	    for (int agent : agents) {
		if (reporter != agent) {
		    final DeceptionModel dm = models[reporter][agent];

		    if (dm != null) {
			final double cap = capabilities.get(agent);
			double itd = generator.nextDoubleFromUnitTND(cap, sd_o);
			itd = dm.calculate(itd);

			Opinion o = new Opinion(reporter, agent, 0, time, itd);
			opinions.add(o);
		    }
		}
	    }
	}

	return opinions;
    }

    @Override
    public List<Experience> generateExperiences() {
	List<Experience> experiences = new ArrayList<Experience>();

	// FIXME: currently all agents from 20 - 29
	final int agent = (time % 10) + 20;

	// get its capability
	final double cap = capabilities.get(agent);

	// generate interaction outcome
	final double outcome = generator.nextDoubleFromUnitTND(cap, sd_i);

	// create experience tuple and add it to the set
	experiences.add(new Experience(agent, 0, time, outcome));

	return experiences;
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
	return null;
    }
}
