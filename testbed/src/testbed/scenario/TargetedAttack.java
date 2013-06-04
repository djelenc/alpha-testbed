package testbed.scenario;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import testbed.common.Utils;
import testbed.deceptionmodel.Complementary;
import testbed.deceptionmodel.Truthful;
import testbed.interfaces.DeceptionModel;
import testbed.interfaces.Experience;
import testbed.interfaces.Opinion;
import testbed.interfaces.ParameterCondition;
import testbed.interfaces.ParametersPanel;

/**
 * A scenario where attackers lie only about specific agents.
 * 
 * @author David
 * 
 */
public class TargetedAttack extends AbstractScenario {

    protected static final String UNKNOWN_DM = "Cannot determine deception model for reporter "
	    + "%d (c=%.2f) and agent %d (c=%.2f). "
	    + "Flags: neutralReporter(%s), neutralAgent(%s), "
	    + "attackerReporter(%s), attackerAgent(%s), "
	    + "targetReporter(%s), targetAgent(%s).";

    protected static final ParameterCondition<Integer> VAL_SIZE;
    protected static final ParameterCondition<Double> VAL_SD, VAL_DENS;

    // set of services -- only 1 service
    protected static final List<Integer> SERVICES = new ArrayList<Integer>();

    // deception models
    protected static final DeceptionModel TRUTHFUL = new Truthful();
    protected static final DeceptionModel COMPLEMENTARY = new Complementary();

    protected static final String INVALID_PARAMS = "Invalid parameters agents(%d), attackers(%d), "
	    + "targets(%d), partners(%d)";

    // time
    protected int time;

    // group of agents
    protected List<Integer> agents, neutral, attackers, targets,
	    interactionPartners;

    public static List<Integer> TARGETS = null;

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

	VAL_SIZE = new ParameterCondition<Integer>() {
	    @Override
	    public void eval(Integer var) {
		if (var < 1)
		    throw new IllegalArgumentException(
			    String.format(
				    "The number of agents must be non negative integer, but was %d",
				    var));
	    }
	};

	VAL_DENS = new ParameterCondition<Double>() {
	    @Override
	    public void eval(Double var) {
		if (var < 0 || var > 1)
		    throw new IllegalArgumentException(
			    String.format(
				    "The density must be between 0 and 1 inclusively, but was %.2f",
				    var));
	    }
	};

	VAL_SD = new ParameterCondition<Double>() {
	    @Override
	    public void eval(Double var) {
		if (var < 0)
		    throw new IllegalArgumentException(
			    String.format(
				    "The standard deviation must be a non-negative double, but was %.2f",
				    var));
	    }
	};
    }

    @Override
    public void initialize(Object... parameters) {
	// extract parameters
	numAgents = Utils.extractParameter(VAL_SIZE, 0, parameters);
	numAttackers = Utils.extractParameter(VAL_SIZE, 1, parameters);
	numTargets = Utils.extractParameter(VAL_SIZE, 2, parameters);
	numPartners = Utils.extractParameter(VAL_SIZE, 3, parameters);
	sd_i = Utils.extractParameter(VAL_SD, 4, parameters);
	sd_o = Utils.extractParameter(VAL_SD, 5, parameters);

	if (numAttackers >= numAgents || numPartners > numAgents - numTargets) {
	    throw new IllegalArgumentException(String.format(INVALID_PARAMS,
		    numAgents, numAttackers, numTargets, numPartners));
	}

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
		cap = generator.nextDoubleFromTo(0, 0.1);
	    } else {
		cap = generator.nextDoubleFromTo(0, 1);
	    }

	    capabilities.put(agent, cap);
	}

	// assign deception models
	models = new DeceptionModel[agents.size()][agents.size()];
	assignDeceptionModelsSybil(agents, neutral, attackers, targets, models);

	// XXX: debugging
	System.out.printf("neutrals = %s\ntargets = %s\nattackers = %s\n",
		neutral, targets, attackers);

	// list addition
	TARGETS = targets;

	// determine interaction partners
	// could be any agent, except attacked ones
	interactionPartners = new ArrayList<Integer>();

	for (int i = 0; i < numPartners; i++) {
	    int agent;

	    do {
		final int index = generator.nextIntFromTo(0, agents.size() - 1);
		agent = agents.get(index);
	    } while (targets.contains(agent)
		    || interactionPartners.contains(agent));

	    interactionPartners.add(agent);
	}

	// reset time
	time = 0;
    }

    public void assignDeceptionModelsSybil(List<Integer> agents,
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

		    if (neutralReporter && neutralAgent)
			models[reporter][agent] = TRUTHFUL;
		    else if (neutralReporter & targetAgent)
			models[reporter][agent] = TRUTHFUL;
		    else if (targetReporter & targetAgent)
			models[reporter][agent] = TRUTHFUL;
		    else if (targetReporter && neutralAgent)
			models[reporter][agent] = TRUTHFUL;
		    else if (attackerReporter && attackerAgent)
			models[reporter][agent] = TRUTHFUL;
		    else if (attackerReporter && targetAgent)
			models[reporter][agent] = COMPLEMENTARY;
		    else
			models[reporter][agent] = null;
		}
	    }
	}
    }

    /**
     * <p>
     * Neutrals and targets
     * <ul>
     * <li>know other targets and neutrals and the give honest opinions about
     * them
     * <li>do not know attackers
     * </ul>
     * <p>
     * Attackers
     * <ul>
     * <li>know other attackers and give honest opinions about them
     * <li>lie about targets
     * <li>do not know neutrals
     * </ul>
     */
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
		    } else if ((neutralReporter || targetReporter)
			    && attackerAgent) {
			models[reporter][agent] = null;
		    } else if (attackerReporter
			    && (neutralAgent || attackerAgent)) {
			if (attackerAgent)
			    models[reporter][agent] = TRUTHFUL;
		    } else if (attackerReporter && targetAgent) {
			models[reporter][agent] = COMPLEMENTARY;
		    } else {
			throw new IllegalArgumentException(String.format(
				UNKNOWN_DM, reporter,
				capabilities.get(reporter), agent,
				capabilities.get(agent), neutralReporter,
				neutralAgent, attackerReporter, attackerAgent,
				targetReporter, targetAgent));
		    }
		}
	    }
	}
    }

    /**
     * <p>
     * Neutrals and targets know everyone and tell truth about them.
     * <p>
     * Attackers
     * <ul>
     * <li>know other attackers and give honest opinions about them
     * <li>lie about targets
     * <li>do not know neutrals
     * </ul>
     */
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
			    && (neutralAgent || targetAgent || attackerAgent)) {
			models[reporter][agent] = TRUTHFUL;
		    } else if (attackerReporter
			    && (neutralAgent || attackerAgent)) {
			if (attackerAgent)
			    models[reporter][agent] = TRUTHFUL;
		    } else if (attackerReporter && targetAgent) {
			models[reporter][agent] = COMPLEMENTARY;
		    } else {
			throw new IllegalArgumentException(String.format(
				UNKNOWN_DM, reporter,
				capabilities.get(reporter), agent,
				capabilities.get(agent), neutralReporter,
				neutralAgent, attackerReporter, attackerAgent,
				targetReporter, targetAgent));
		    }
		}
	    }
	}
    }

    /**
     * <p>
     * Neutrals and targets know everyone and tell truth about them.
     * <p>
     * Attackers
     * <ul>
     * <li>do not know other attackers nor neutrals
     * <li>lie about targets
     * </ul>
     * 
     * <p>
     * Caveat: This works with QTM only when > 1.01
     */
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
			models[reporter][agent] = null;
		    } else if (attackerReporter && targetAgent) {
			models[reporter][agent] = COMPLEMENTARY;
		    } else {
			throw new IllegalArgumentException(String.format(
				UNKNOWN_DM, reporter,
				capabilities.get(reporter), agent,
				capabilities.get(agent), neutralReporter,
				neutralAgent, attackerReporter, attackerAgent,
				targetReporter, targetAgent));
		    }
		}
	    }
	}
    }

    /**
     * <p>
     * Neutrals and targets know everyone and they give honest opinions about
     * them.
     * 
     * <p>
     * Attackers know everyone and lie only about targets. For everyone else,
     * they give honest opinions.
     */
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
			throw new IllegalArgumentException(String.format(
				UNKNOWN_DM, reporter,
				capabilities.get(reporter), agent,
				capabilities.get(agent), neutralReporter,
				neutralAgent, attackerReporter, attackerAgent,
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
			throw new IllegalArgumentException(String.format(
				TargetedAttack.UNKNOWN_DM, reporter,
				capabilities.get(reporter), agent,
				capabilities.get(agent), neutralReporter,
				neutralAgent, attackerReporter, attackerAgent,
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
	return new TargetedAttackGUI();
    }

    @Override
    public String toString() {
	return "Targeted attack";
    }
}
