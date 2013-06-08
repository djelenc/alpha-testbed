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

    private static final String TOO_MANY_IP = "Too many interaction partners. Should be at most %d, but was %d.";

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
    protected List<Integer> agents, neutrals, attackers, targets,
	    interactionPartners;

    protected static List<Integer> allTargets = null;
    protected static List<Integer> allNeutrals = null;
    protected static List<Integer> allAttackers = null;

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

	neutrals = new ArrayList<Integer>();
	attackers = new ArrayList<Integer>();
	targets = new ArrayList<Integer>();

	// assign agents to groups
	assignAgentsToGroups(agents, neutrals, attackers, targets,
		numAttackers, numTargets);

	// System.out.printf("Neutral = %s\nTarget = %s\nAttackers = %s\n",
	// neutrals, targets, attackers);

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
	assignDeceptionModelsSybil(agents, neutrals, attackers, targets, models);

	// list addition
	allTargets = targets;
	allNeutrals = neutrals;
	allAttackers = attackers;

	// determine interaction partners
	interactionPartners = determineInteractionPartners(numPartners,
		neutrals, attackers);

	// reset time
	time = 0;
    }

    /**
     * Returns a List of interaction partners that are chosen from the given
     * lists of neutrals and attackers. The methods first selects all neutrals,
     * and only then starts to assign partners from attackers.
     * 
     * @param numPartners
     *            Number of interaction partners to choose.
     * @param neutrals
     *            List of neutral agents
     * @param attackers
     *            List of attacking agents
     * @return List of selected interaction partners
     */
    public List<Integer> determineInteractionPartnersPreferNeutrals(
	    int numPartners, List<Integer> neutrals, List<Integer> attackers) {
	if (numPartners > neutrals.size() + attackers.size())
	    throw new IllegalArgumentException(String.format(TOO_MANY_IP,
		    neutrals.size() + attackers.size(), numPartners));

	final List<Integer> allNeutrals = new ArrayList<Integer>();
	allNeutrals.addAll(neutrals);
	final List<Integer> allAttackers = new ArrayList<Integer>();
	allAttackers.addAll(attackers);

	final List<Integer> selected = new ArrayList<Integer>();

	for (int i = 0; i < numPartners; i++) {
	    final int agent, rndIdx;

	    if (!allNeutrals.isEmpty()) {
		rndIdx = generator.nextIntFromTo(0, allNeutrals.size() - 1);
		agent = allNeutrals.remove(rndIdx);
	    } else if (!allAttackers.isEmpty()) {
		rndIdx = generator.nextIntFromTo(0, allAttackers.size() - 1);
		agent = allAttackers.remove(rndIdx);
	    } else {
		throw new Error("Unreachable code");
	    }

	    selected.add(agent);
	}

	return selected;
    }

    /**
     * Returns a List of interaction partners that are randomly chosen from the
     * given lists of neutrals and attackers.
     * 
     * @param numPartners
     *            Number of interaction partners to choose.
     * @param neutrals
     *            List of neutral agents
     * @param attackers
     *            List of attacking agents
     * @return List of selected interaction partners
     */
    public List<Integer> determineInteractionPartners(int numPartners,
	    List<Integer> neutrals, List<Integer> attackers) {
	final List<Integer> potential = new ArrayList<Integer>();
	potential.addAll(neutrals);
	potential.addAll(attackers);

	if (numPartners > potential.size())
	    throw new IllegalArgumentException(String.format(TOO_MANY_IP,
		    potential.size(), numPartners));

	final List<Integer> selected = new ArrayList<Integer>();

	for (int i = 0; i < numPartners; i++) {
	    final int max = potential.size() - 1;
	    final int agent = potential.remove(generator.nextIntFromTo(0, max));

	    selected.add(agent);
	}

	return selected;
    }

    /**
     * <p>
     * Neutrals
     * <ul>
     * <li>know other neutrals and the give honest opinions about them
     * <li>know targets and give honest opinions about them
     * <li>do not know attackers (have no opinions about them)
     * </ul>
     * 
     * <p>
     * Targets
     * <ul>
     * <li>know other targets and the give honest opinions about them
     * <li>know neutrals and give honest opinions about them
     * <li>do not know attackers (have no opinions about them)
     * </ul>
     * <p>
     * Attackers
     * <ul>
     * <li>know other attackers and give honest opinions about them
     * <li>lie about targets
     * <li>do not know neutrals
     * </ul>
     */
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
     * Neutrals
     * <ul>
     * <li>know other neutrals and the give honest opinions about them
     * <li>know targets and give honest opinions about them
     * <li>do not know attackers (have no opinions about them)
     * </ul>
     * 
     * <p>
     * Targets
     * <ul>
     * <li>know other targets and the give honest opinions about them
     * <li>know neutrals and give honest opinions about them
     * <li>know attackers and give honest opinions about them
     * </ul>
     * <p>
     * Attackers
     * <ul>
     * <li>know other attackers and give honest opinions about them
     * <li>lie about targets
     * <li>do not know neutrals
     * </ul>
     */
    public void assignDeceptionModelsSybilHard(List<Integer> agents,
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
		    else if (targetReporter && attackerAgent)
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
     * Neutrals
     * <ul>
     * <li>know other neutrals and the give honest opinions about them
     * <li>know targets and give honest opinions about them
     * <li>do not know attackers (have no opinions about them)
     * </ul>
     * 
     * <p>
     * Targets
     * <ul>
     * <li>know other targets and the give honest opinions about them
     * <li>know neutrals and give honest opinions about them
     * <li>do not know attackers (have no opinions about them)
     * </ul>
     * <p>
     * Attackers
     * <ul>
     * <li>only give false opinions about targets
     * <li>do not know neutrals nor other attackers
     * </ul>
     */
    public void assignDeceptionModelsSybilEasy(List<Integer> agents,
	    List<Integer> neutral, List<Integer> attackers,
	    List<Integer> targets, DeceptionModel[][] models) {
	boolean neutralReporter, neutralAgent, attackerReporter, targetReporter, targetAgent;

	for (int reporter : agents) {
	    for (int agent : agents) {
		if (reporter != agent) {
		    neutralReporter = neutral.contains(reporter);
		    neutralAgent = neutral.contains(agent);
		    attackerReporter = attackers.contains(reporter);
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
		    else if (attackerReporter && targetAgent)
			models[reporter][agent] = COMPLEMENTARY;
		    else
			models[reporter][agent] = null;
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

    public static List<Integer> getTargets() {
	if (allTargets == null) {
	    throw new IllegalArgumentException(String.format(
		    "Scenario %s was not initialized!",
		    TargetedAttack.class.getCanonicalName()));
	}

	return allTargets;
    }

    public static List<Integer> getNeutrals() {
	if (allTargets == null) {
	    throw new IllegalArgumentException(String.format(
		    "Scenario %s was not initialized!",
		    TargetedAttack.class.getCanonicalName()));
	}

	return allNeutrals;
    }

    public static List<Integer> getAttackers() {
	if (allTargets == null) {
	    throw new IllegalArgumentException(String.format(
		    "Scenario %s was not initialized!",
		    TargetedAttack.class.getCanonicalName()));
	}

	return allAttackers;
    }
}
