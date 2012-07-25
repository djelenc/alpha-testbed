package testbed.scenario;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import testbed.common.Utils;
import testbed.deceptionmodel.Complementary;
import testbed.deceptionmodel.Silent;
import testbed.deceptionmodel.Truthful;
import testbed.interfaces.Experience;
import testbed.interfaces.ICondition;
import testbed.interfaces.IDeceptionModel;
import testbed.interfaces.IParametersPanel;
import testbed.interfaces.IScenario;
import testbed.interfaces.Opinion;

/**
 * Scenario in which the probability of reporting an honest opinion is directly
 * correlated with the capability of the agent that provides the opinion. For
 * instance, an agent with capability 0.75 has 75% chance to report an honest
 * opinion. False opinions are computed with {@link Complementary} deception
 * model.
 * 
 * <p>
 * Capabilities (and by transitivity -- deception models) are assigned at
 * initialization and do not change during execution.
 * 
 * <p>
 * The initialization method requires an array of objects that represent
 * parameters with the following semantics:
 * <ul>
 * <li>0: (int) number of agents in the system
 * <li>1: (double) standard deviation for generating experiences
 * <li>2: (double) standard deviation for generation opinions
 * <li>3: (double) interaction density -- the percentage of agents with which
 * agent Alpha interacts
 * <li>4: (double) opinion density -- the ratio that determines the number of
 * opinions that each agents provides. Or
 * "how many agents does a particular agent knows".
 * <li>5: (double) change density -- the ratio that determines the number of
 * changes in the system.
 * <li>6: (int) the length of interval for changes
 * </ul>
 * 
 * @author David
 * 
 */
public class TransitiveWithChanges extends AbstractScenario implements
	IScenario {

    // Set of all agents
    protected Set<Integer> agents;

    // Set of Alpha's interaction partners (subset of agents)
    protected List<Integer> partners;

    // Assigned capabilities
    protected Map<Integer, Double> capabilities;

    // Deception models
    protected IDeceptionModel[][] dms;

    protected int time;
    protected double sd_i, sd_o;
    protected double interDens, opDens, changeDens;
    protected int changeInterval;

    @Override
    public void initialize(Object... params) {
	agents = new LinkedHashSet<Integer>();
	partners = new ArrayList<Integer>();
	capabilities = new LinkedHashMap<Integer, Double>();
	dms = null;
	time = 0;

	// extract number of agents and services
	final ICondition<Integer> validatorSize = new ICondition<Integer>() {
	    @Override
	    public void eval(Integer var) {
		if (var < 1)
		    throw new IllegalArgumentException(
			    String.format(
				    "The number of agents and services must be non negative integer, but was %d",
				    var));
	    }
	};

	int numAgents = Utils.extractParameter(validatorSize, 0, params);

	// extract SD for generating IOs and ITDs
	final ICondition<Double> validatorSD = new ICondition<Double>() {
	    @Override
	    public void eval(Double var) {
		if (var < 0)
		    throw new IllegalArgumentException(
			    String.format(
				    "The standard deviation must be a non-negative double, but was %.2f",
				    var));
	    }
	};

	sd_i = Utils.extractParameter(validatorSD, 1, params);
	sd_o = Utils.extractParameter(validatorSD, 2, params);

	for (int i = 0; i < numAgents; i++) {
	    agents.add(i); // generate agents
	    capabilities.put(i, generator.randomUnif(0, 1)); // assign
							     // capabilities
	}

	// extract opinion and interaction densities
	final ICondition<Double> validatorDensity = new ICondition<Double>() {
	    @Override
	    public void eval(Double var) {
		if (var < 0 || var > 1)
		    throw new IllegalArgumentException(
			    String.format(
				    "The density must be between 0 and 1 inclusively, but was %.2f",
				    var));
	    }
	};

	interDens = Utils.extractParameter(validatorDensity, 3, params);
	opDens = Utils.extractParameter(validatorDensity, 4, params);
	changeDens = Utils.extractParameter(validatorDensity, 5, params);

	// extract opinion and interaction densities
	final ICondition<Integer> validatorInterval = new ICondition<Integer>() {
	    @Override
	    public void eval(Integer var) {
		if (var < 0)
		    throw new IllegalArgumentException(
			    String.format(
				    "The change interval must be positive integer, but was %d.",
				    var));
	    }
	};

	changeInterval = Utils.extractParameter(validatorInterval, 6, params);

	// define interaction partners
	partners = assignInteractionPartners(agents, interDens);
	dms = assignDeceptionModels(agents, capabilities, opDens);
    }

    @Override
    public Set<Opinion> generateOpinions() {
	final Set<Opinion> opinions = new HashSet<Opinion>();

	Opinion opinion = null;
	double cap, itd;

	for (int a1 : agents) {
	    for (int a2 : agents) {
		if (!(dms[a1][a2] instanceof Silent)) {
		    // get capability
		    cap = capabilities.get(a2);

		    // generate internal trust degree
		    itd = generator.randomTND(cap, sd_o);
		    itd = dms[a1][a2].calculate(itd);

		    // create opinion tuple and add it to list
		    opinion = new Opinion(a1, a2, 0, time, itd);
		    opinions.add(opinion);
		}
	    }
	}

	return opinions;
    }

    @Override
    public Set<Experience> generateExperiences() {
	// get agent to interact with
	int agent = partners.get(time % partners.size());

	// generate interaction outcome
	final double cap = capabilities.get(agent);
	final double outcome = generator.randomTND(cap, sd_i);

	// create experience tuple and add it to list
	final Experience experience = new Experience(agent, 0, time, outcome);

	final Set<Experience> experiences = new HashSet<Experience>();
	experiences.add(experience);

	return experiences;
    }

    /**
     * Constructs a subset of agents from a given set of agents by random
     * selection. Agents in the newly constructed set are Alpha's interaction
     * partners.
     * 
     * @param agents
     *            The set of all agents to choose from
     * @param interactionDensity
     *            The percentage of agents to interact with.
     * @return
     */
    public List<Integer> assignInteractionPartners(Set<Integer> agents,
	    double interactionDensity) {
	final List<Integer> partners = new ArrayList<Integer>();
	final long numPartners = Math.round(agents.size() * interactionDensity);
	int counter = 0, agent;

	while (counter < numPartners) {
	    agent = generator.randomUnifIndex(0, agents.size() - 1);

	    if (!partners.contains(agent)) {
		partners.add(agent);
		counter += 1;
	    }
	}

	return partners;
    }

    /**
     * Assign deception models using transitivity: higher capability means a
     * higher chance that an agent is truthful.
     * 
     * @param agents
     *            A set of agents
     * @param capabilities
     *            A map of capabilities
     * @param opinionDensity
     *            Percentage of all possible opinions that will be generated
     * @return
     */
    public IDeceptionModel[][] assignDeceptionModels(Set<Integer> agents,
	    Map<Integer, Double> capabilities, double opinionDensity) {

	IDeceptionModel[][] dms = new IDeceptionModel[agents.size()][agents
		.size()];
	final IDeceptionModel truthful = new Truthful();
	final IDeceptionModel liar = new Complementary();
	final IDeceptionModel silent = new Silent();
	double cap, rnd;

	for (int i = 0; i < dms.length; i++) {
	    cap = capabilities.get(i);

	    for (int j = 0; j < dms[i].length; j++) {
		rnd = generator.randomUnif(0, 1);

		if (cap > rnd) {
		    dms[i][j] = truthful;
		} else {
		    dms[i][j] = liar;
		}
	    }
	}

	// make opinions sparse
	final int limit = (int) ((1 - opinionDensity) * dms.length * dms.length);
	int i, j, counter = 0;

	while (counter < limit) {
	    i = generator.randomUnifIndex(0, dms.length - 1);
	    j = generator.randomUnifIndex(0, dms.length - 1);

	    if (dms[i][j] != silent) {
		dms[i][j] = silent;
		counter += 1;
	    }
	}

	return dms;
    }

    /**
     * Another implementation of assigning deception models. Should yield more
     * consistent results for different random seeds.
     * 
     * @param agents
     * @param capabilities
     * @param opinionDensity
     * @return
     */
    public IDeceptionModel[][] assignDeceptionModelsTenative(
	    Set<Integer> agents, Map<Integer, Double> capabilities,
	    double opinionDensity) {

	IDeceptionModel[][] dms = new IDeceptionModel[agents.size()][agents
		.size()];
	final IDeceptionModel truthful = new Truthful();
	final IDeceptionModel liar = new Complementary();
	final IDeceptionModel silent = new Silent();
	double cap;
	long numTruthful;
	int assignedTruthful = 0;
	int idx;

	for (int i = 0; i < dms.length; i++) {
	    cap = capabilities.get(i);
	    numTruthful = Math.round(cap * dms.length);
	    assignedTruthful = 0;

	    // chose random agent to tell the truth about
	    while (assignedTruthful < numTruthful) {
		idx = generator.randomUnifIndex(0, dms.length - 1);

		if (dms[i][idx] == null) {
		    dms[i][idx] = truthful;
		    assignedTruthful += 1;
		}
	    }

	    // lie toward others
	    for (int j = 0; j < dms[i].length; j++)
		if (dms[i][j] == null) {
		    dms[i][j] = liar;
		}
	}

	// make opinions sparse
	final int limit = (int) ((1 - opinionDensity) * dms.length * dms.length);
	int i, j, counter = 0;

	while (counter < limit) {
	    i = generator.randomUnifIndex(0, dms.length - 1);
	    j = generator.randomUnifIndex(0, dms.length - 1);

	    if (dms[i][j] != silent) {
		dms[i][j] = silent;
		counter += 1;
	    }
	}

	return dms;
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
	final Set<Integer> services = new HashSet<Integer>();
	services.add(0);

	return services;
    }

    @Override
    public IParametersPanel getParametersPanel() {
	return new TransitiveWithChangesGUI();
    }

    @Override
    public void setCurrentTime(int time) {
	this.time = time;

	// reassign stuff
	if (0 == time % changeInterval) {
	    int counter = 0;
	    final int changes = (int) Math.round(agents.size() * changeDens);

	    while (counter < changes) {
		final int agent = generator.randomUnifIndex(0,
			agents.size() - 1);
		capabilities.put(agent, generator.randomUnif(0, 1));
		counter += 1;
	    }

	    partners = assignInteractionPartners(agents, interDens);
	    dms = assignDeceptionModels(agents, capabilities, opDens);
	}
    }

    @Override
    public String getName() {
	return "Transitive with changes";
    }
}
