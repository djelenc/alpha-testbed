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
public class TargetedAttack extends AbstractScenario {

    // set of services -- only 1 service
    protected static final List<Integer> SERVICES = new ArrayList<Integer>();

    // deception models
    protected static final DeceptionModel TRUTHFUL = new Truthful();
    protected static final DeceptionModel COMPLEMENTARY = new Complementary();

    // time
    protected int time;

    // group of agents
    protected List<Integer> agents, neutral, attackers, targets,
	    interactionPartners;

    // capabilities
    protected Map<Integer, Double> capabilities;

    // deception models
    protected DeceptionModel[][] models;

    // total number of agents
    protected int numAgents, numPartners, numAttackers, numTargets;

    // standard deviations for generating interaction and opinions
    protected double sd_i, sd_o;

    static {
	SERVICES.add(0);
	TRUTHFUL.initialize();
	COMPLEMENTARY.initialize();
    }

    @Override
    public void initialize(Object... parameters) {
	// extract parameters
	numAgents = 50;

	numAttackers = 20;
	numTargets = 10;
	numPartners = 10;

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
	assignAgentsToGroups(agents, neutral, attackers, targets, numAttackers,
		numTargets);

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

	// determine interaction partners
	// could be any agent, except attacked ones
	interactionPartners = new ArrayList<Integer>();

	for (int i = 0; i < numPartners; i++) {
	    int agent;

	    do {
		final int index = generator.nextIntFromTo(0, agents.size() - 1);
		agent = agents.get(index);
	    } while (targets.contains(agent));

	    interactionPartners.add(agent);
	}

	// reset time
	time = 0;
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
			models[reporter][agent] = TRUTHFUL;
		    } else if ((neutralReporter || targetReporter)
			    && attackerAgent) {
			models[reporter][agent] = TRUTHFUL;
		    } else if (attackerReporter
			    && (neutralAgent || attackerAgent)) {
			models[reporter][agent] = TRUTHFUL;
		    } else if (attackerReporter && targetAgent) {
			models[reporter][agent] = COMPLEMENTARY;
		    } else {
			throw new IllegalArgumentException(
				String.format(
					"Cannot determine deception model for reporter "
						+ "%d (c=%.2f) and agent %d (c=%.2f). "
						+ "Flags: neutralReporter(%s), neutralAgent(%s), "
						+ "attackerReporter(%s), attackerAgent(%s), "
						+ "targetReporter(%s), targetAgent(%s).",
					reporter, capabilities.get(reporter),
					agent, capabilities.get(agent),
					neutralReporter, neutralAgent,
					attackerReporter, attackerAgent,
					targetReporter, targetAgent));
		    }
		}
	    }
	}
    }
    
    // same as 1
    public void assignDeceptionModels5(List<Integer> agents,
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
			models[reporter][agent] = TRUTHFUL;
			// additionally: make it sparse?
		    } else if ((neutralReporter || targetReporter)
			    && attackerAgent) {
			models[reporter][agent] = null;
			// models[reporter][agent] = TRUTHFUL;
		    } else if (attackerReporter
			    && (neutralAgent || attackerAgent)) {
			// just skip

			if (attackerAgent)
			    models[reporter][agent] = TRUTHFUL;
			// rare?
		    } else if (attackerReporter && targetAgent) {
			models[reporter][agent] = COMPLEMENTARY;
		    } else {
			throw new IllegalArgumentException(
				String.format(
					"Cannot determine deception model for reporter "
						+ "%d (c=%.2f) and agent %d (c=%.2f). "
						+ "Flags: neutralReporter(%s), neutralAgent(%s), "
						+ "attackerReporter(%s), attackerAgent(%s), "
						+ "targetReporter(%s), targetAgent(%s).",
					reporter, capabilities.get(reporter),
					agent, capabilities.get(agent),
					neutralReporter, neutralAgent,
					attackerReporter, attackerAgent,
					targetReporter, targetAgent));
		    }
		}
	    }
	}
    }

    public void assignDeceptionModels4(List<Integer> agents,
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
			models[reporter][agent] = TRUTHFUL;
			// additionally: make it sparse?
		    } else if ((neutralReporter || targetReporter)
			    && attackerAgent) {
			models[reporter][agent] = null;
			// models[reporter][agent] = TRUTHFUL;
		    } else if (attackerReporter
			    && (neutralAgent || attackerAgent)) {
			// just skip

			// if (attackerAgent)
			// models[reporter][agent] = TRUTHFUL;
			// rare?
		    } else if (attackerReporter && targetAgent) {
			models[reporter][agent] = COMPLEMENTARY;
		    } else {
			throw new IllegalArgumentException(
				String.format(
					"Cannot determine deception model for reporter "
						+ "%d (c=%.2f) and agent %d (c=%.2f). "
						+ "Flags: neutralReporter(%s), neutralAgent(%s), "
						+ "attackerReporter(%s), attackerAgent(%s), "
						+ "targetReporter(%s), targetAgent(%s).",
					reporter, capabilities.get(reporter),
					agent, capabilities.get(agent),
					neutralReporter, neutralAgent,
					attackerReporter, attackerAgent,
					targetReporter, targetAgent));
		    }
		}
	    }
	}
    }

    public void assignDeceptionModels3(List<Integer> agents,
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
			models[reporter][agent] = TRUTHFUL;
			// additionally: make it sparse?
		    } else if ((neutralReporter || targetReporter)
			    && attackerAgent) {
			// models[reporter][agent] = null;
			models[reporter][agent] = TRUTHFUL;
		    } else if (attackerReporter
			    && (neutralAgent || attackerAgent)) {
			// just skip

			if (attackerAgent)
			    models[reporter][agent] = TRUTHFUL;
			// rare?
		    } else if (attackerReporter && targetAgent) {
			models[reporter][agent] = COMPLEMENTARY;
		    } else {
			throw new IllegalArgumentException(
				String.format(
					"Cannot determine deception model for reporter "
						+ "%d (c=%.2f) and agent %d (c=%.2f). "
						+ "Flags: neutralReporter(%s), neutralAgent(%s), "
						+ "attackerReporter(%s), attackerAgent(%s), "
						+ "targetReporter(%s), targetAgent(%s).",
					reporter, capabilities.get(reporter),
					agent, capabilities.get(agent),
					neutralReporter, neutralAgent,
					attackerReporter, attackerAgent,
					targetReporter, targetAgent));
		    }
		}
	    }
	}
    }

    public void assignDeceptionModels2(List<Integer> agents,
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
			models[reporter][agent] = TRUTHFUL;
		    } else if ((neutralReporter || targetReporter)
			    && attackerAgent) {
			models[reporter][agent] = TRUTHFUL;
		    } else if (attackerReporter
			    && (neutralAgent || attackerAgent)) {
			// if (attackerAgent)
			models[reporter][agent] = null;
		    } else if (attackerReporter && targetAgent) {
			models[reporter][agent] = COMPLEMENTARY;
		    } else {
			throw new IllegalArgumentException(
				String.format(
					"Cannot determine deception model for reporter "
						+ "%d (c=%.2f) and agent %d (c=%.2f). "
						+ "Flags: neutralReporter(%s), neutralAgent(%s), "
						+ "attackerReporter(%s), attackerAgent(%s), "
						+ "targetReporter(%s), targetAgent(%s).",
					reporter, capabilities.get(reporter),
					agent, capabilities.get(agent),
					neutralReporter, neutralAgent,
					attackerReporter, attackerAgent,
					targetReporter, targetAgent));
		    }
		}
	    }
	}
    }

    public void assignDeceptionModels1(List<Integer> agents,
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
			models[reporter][agent] = TRUTHFUL;
		    } else if ((neutralReporter || targetReporter)
			    && attackerAgent) {
			models[reporter][agent] = TRUTHFUL;
		    } else if (attackerReporter
			    && (neutralAgent || attackerAgent)) {
			models[reporter][agent] = TRUTHFUL;
		    } else if (attackerReporter && targetAgent) {
			models[reporter][agent] = COMPLEMENTARY;
		    } else {
			throw new IllegalArgumentException(
				String.format(
					"Cannot determine deception model for reporter "
						+ "%d (c=%.2f) and agent %d (c=%.2f). "
						+ "Flags: neutralReporter(%s), neutralAgent(%s), "
						+ "attackerReporter(%s), attackerAgent(%s), "
						+ "targetReporter(%s), targetAgent(%s).",
					reporter, capabilities.get(reporter),
					agent, capabilities.get(agent),
					neutralReporter, neutralAgent,
					attackerReporter, attackerAgent,
					targetReporter, targetAgent));
		    }
		}
	    }
	}
    }

    public void assignDeceptionModelsBkp(List<Integer> agents,
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
			models[reporter][agent] = TRUTHFUL;
			// additionally: make it sparse?
		    } else if ((neutralReporter || targetReporter)
			    && attackerAgent) {
			// models[reporter][agent] = null;
			models[reporter][agent] = TRUTHFUL;
		    } else if (attackerReporter
			    && (neutralAgent || attackerAgent)) {
			// just skip

			// if (attackerAgent)
			models[reporter][agent] = TRUTHFUL;
			// rare?
		    } else if (attackerReporter && targetAgent) {
			models[reporter][agent] = COMPLEMENTARY;
		    } else {
			throw new IllegalArgumentException(
				String.format(
					"Cannot determine deception model for reporter "
						+ "%d (c=%.2f) and agent %d (c=%.2f). "
						+ "Flags: neutralReporter(%s), neutralAgent(%s), "
						+ "attackerReporter(%s), attackerAgent(%s), "
						+ "targetReporter(%s), targetAgent(%s).",
					reporter, capabilities.get(reporter),
					agent, capabilities.get(agent),
					neutralReporter, neutralAgent,
					attackerReporter, attackerAgent,
					targetReporter, targetAgent));
		    }
		}
	    }
	}
    }

    public void assignAgentsToGroups(List<Integer> all, List<Integer> neutral,
	    List<Integer> attackers, List<Integer> targets, int numAttackers,
	    int numTargets) {
	neutral.clear();
	attackers.clear();

	final List<Integer> copiedAll = new ArrayList<Integer>(all);

	// create group of attackers
	int countAttackers = 0;
	do {
	    final int index = generator.nextIntFromTo(0, copiedAll.size() - 1);
	    final int agent = copiedAll.remove(index);
	    attackers.add(agent);
	    countAttackers++;
	} while (countAttackers < numAttackers);

	// create group of targets
	int countTargets = 0;
	do {
	    final int index = generator.nextIntFromTo(0, copiedAll.size() - 1);
	    final int agent = copiedAll.remove(index);
	    targets.add(agent);
	    countTargets++;
	} while (countTargets < numTargets);

	// put others in the neutral group
	neutral.addAll(copiedAll);
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
	final List<Opinion> opinions = new ArrayList<Opinion>();
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
	final List<Experience> experiences = new ArrayList<Experience>();

	final int agent = interactionPartners.get(time
		% interactionPartners.size());

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
