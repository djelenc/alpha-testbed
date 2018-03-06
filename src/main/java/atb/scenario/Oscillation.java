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
package atb.scenario;

import atb.common.Utils;
import atb.deceptionmodel.NegativeExaggeration;
import atb.deceptionmodel.PositiveExaggeration;
import atb.deceptionmodel.Truthful;
import atb.interfaces.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Oscillation scenario
 *
 * @author David
 */
public class Oscillation extends AbstractScenario {

    // set of services -- only 1 service
    protected static final List<Integer> SERVICES = new ArrayList<Integer>();
    protected static final DeceptionModel TRUTHFUL = new Truthful();
    protected static final DeceptionModel POS_EXAGG = new PositiveExaggeration();
    protected static final DeceptionModel NEG_EXAGG = new NegativeExaggeration();

    // input parameter validators
    protected static final ParameterCondition<Integer> VAL_SIZE, VAL_CHANGE_INT;
    protected static final ParameterCondition<Double> VAL_SD, VAL_FRAC;

    static {
        SERVICES.add(0);
        TRUTHFUL.initialize();
        POS_EXAGG.initialize(0.5);
        NEG_EXAGG.initialize(0.5);

        VAL_SIZE = new ParameterCondition<Integer>() {
            @Override
            public void eval(Integer var) {
                if (var < 1)
                    throw new IllegalArgumentException(String
                            .format("The number of agents and services must be non negative "
                                    + "integer, but was %d", var));
            }
        };

        VAL_SD = new ParameterCondition<Double>() {
            @Override
            public void eval(Double var) {
                if (var < 0)
                    throw new IllegalArgumentException(String
                            .format("The standard deviation must be a non-negative "
                                    + "double, but was %.2f", var));
            }
        };

        VAL_CHANGE_INT = new ParameterCondition<Integer>() {
            @Override
            public void eval(Integer var) {
                if (var < 0 || var > 1000)
                    throw new IllegalArgumentException(String
                            .format("The change interval must be within [1, 1000],"
                                    + " but was %d", var));
            }
        };

        VAL_FRAC = new ParameterCondition<Double>() {
            @Override
            public void eval(Double var) {
                if (var < 0 || var > 1)
                    throw new IllegalArgumentException(
                            String.format(
                                    "The percentage must be wethin [%.2f,"
                                            + " %.2f], but was %.2f",
                                    0d, 1d, var));
            }
        };
    }

    // time
    protected int time;
    // agents: all and groups of bad, good and neutral
    protected List<Integer> agents, good, bad;
    // capabilities
    protected Map<Integer, Double> capabilities;
    // deception models
    protected DeceptionModel[][] models;
    // total number of agents
    protected int numAgents;
    // interval for changes
    protected int changeInterval;
    // fractions for good, bad and neutral agents
    protected double fracGood, fracBad, fracNeutral;
    // standard deviations for generating interaction and opinions
    protected double sd_i, sd_o;

    @Override
    public void initialize(Object... parameters) {
        // extract parameters
        numAgents = Utils.extractParameter(VAL_SIZE, 0, parameters);
        sd_i = Utils.extractParameter(VAL_SD, 1, parameters);
        sd_o = Utils.extractParameter(VAL_SD, 2, parameters);
        changeInterval = Utils.extractParameter(VAL_CHANGE_INT, 3, parameters);

        fracGood = Utils.extractParameter(VAL_FRAC, 4, parameters);
        fracBad = Utils.extractParameter(VAL_FRAC, 5, parameters);

        if (fracGood + fracBad > 1) {
            throw new IllegalArgumentException(String.format(
                    "The sum of percentages for Good and Bad agents is %.2f"
                            + ", but should be %.2f or less.",
                    fracGood + fracBad, 1d));
        }

        fracNeutral = 1 - fracGood - fracBad;

        // create all agents
        agents = new ArrayList<Integer>();
        good = new ArrayList<Integer>();
        bad = new ArrayList<Integer>();

        for (int agent = 0; agent < numAgents; agent++) {
            agents.add(agent);
        }

        // assign agents to groups
        assignAgentsToGroups(agents, good, bad, fracGood, fracBad);

        // assign capabilities
        capabilities = new LinkedHashMap<Integer, Double>();
        assignCapabilities(agents, good, bad, capabilities);

        // assign deception models
        models = new DeceptionModel[agents.size()][agents.size()];
        assignDeceptionModels(agents, good, bad, models);

        // reset time
        time = 0;
    }

    /**
     * Assigns deception models to agents.
     *
     * @param agents List of all agents
     * @param good   List of good agents (subset of allAgents)
     * @param bad    List of bad agents (subset of allAgents)
     * @param models 2D array of deception models -- gets mutated
     */
    public void assignDeceptionModels(List<Integer> agents, List<Integer> good,
                                      List<Integer> bad, DeceptionModel[][] models) {
        boolean neutralReporter, neutralAgent, goodReporter, goodAgent,
                badReporter, badAgent;

        for (int reporter : agents) {
            for (int agent : agents) {
                if (reporter != agent) {
                    goodReporter = good.contains(reporter);
                    badReporter = bad.contains(reporter);
                    neutralReporter = !goodReporter && !badReporter;
                    goodAgent = good.contains(agent);
                    badAgent = bad.contains(agent);
                    neutralAgent = !badAgent && !goodAgent;

                    if (neutralReporter && neutralAgent) {
                        models[reporter][agent] = TRUTHFUL;
                    } else if (neutralReporter && !neutralAgent) {
                        models[reporter][agent] = null;
                    } else if (badReporter && !neutralAgent) {
                        models[reporter][agent] = POS_EXAGG;
                    } else if (badReporter && neutralAgent) {
                        models[reporter][agent] = NEG_EXAGG;
                    } else if (goodReporter && !neutralAgent) {
                        models[reporter][agent] = POS_EXAGG;
                    } else if (goodReporter && neutralAgent) {
                        models[reporter][agent] = NEG_EXAGG;
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

    /**
     * Switches the capabilities for good and bad agents. This method:
     * <ol>
     * <li>Puts all agents from list of good agents to the list of bad
     * agents</li>
     * <li>Puts all agents from the list of bad agents to the list of good
     * agents</li>
     * <li>Reassigns capabilities to good and bad agents.</li>
     * </ol>
     * <p>
     * Capabilities for neutral agents remain unchanged.
     *
     * @param good         The list of good agents -- gets mutated
     * @param bad          The list of bad agents -- gets mutated
     * @param capabilities The map of existing capabilities -- gets mutated
     */
    public void switchCapabilities(List<Integer> good, List<Integer> bad,
                                   Map<Integer, Double> capabilities) {

        List<Integer> newGood = new ArrayList<Integer>(bad);
        List<Integer> newBad = new ArrayList<Integer>(good);

        for (int agent : newGood) {
            capabilities.put(agent, generator.nextDoubleFromTo(0.5, 1));
        }

        for (int agent : newBad) {
            capabilities.put(agent, generator.nextDoubleFromTo(0, 0.5));
        }

        good.clear();
        good.addAll(newGood);
        bad.clear();
        bad.addAll(newBad);
    }

    /**
     * Assigns capabilities to the agents with respect the groups of agents.
     *
     * @param agents       The list of all agents
     * @param good         The list of good agents (subset of agents)
     * @param bad          The list of bad agents (subset of agents)
     * @param capabilities The map of capabilities -- gets mutated
     */
    public void assignCapabilities(List<Integer> agents, List<Integer> good,
                                   List<Integer> bad, Map<Integer, Double> capabilities) {
        for (int agent : agents) {
            if (good.contains(agent)) {
                capabilities.put(agent, generator.nextDoubleFromTo(0.5, 1));
            } else if (bad.contains(agent)) {
                capabilities.put(agent, generator.nextDoubleFromTo(0, 0.5));
            } else {
                capabilities.put(agent, generator.nextDoubleFromTo(0, 1));
            }
        }
    }

    /**
     * Assigns agents to groups with respect to the given fractions.
     *
     * @param allAgents List of all agents
     * @param good      List of good agents (subset of allAgents) -- gets mutated
     * @param bad       List of bad agents (subset of allAgents) -- gets mutated
     * @param fracGood  The fraction of good agents
     * @param fracBad   The fraction of bad agents
     */
    public void assignAgentsToGroups(List<Integer> allAgents,
                                     List<Integer> good, List<Integer> bad, double fracGood,
                                     double fracBad) {
        int numGood = (int) (fracGood * allAgents.size());
        int numBad = (int) (fracBad * allAgents.size());

        good.clear();
        bad.clear();

        int agent;

        // define good
        for (int i = 0; i < numGood; i++) {
            do {
                agent = generator.nextIntFromTo(0, allAgents.size() - 1);
            } while (good.contains(agent));

            good.add(agent);
        }

        // define bad
        for (int i = 0; i < numBad; i++) {
            do {
                agent = generator.nextIntFromTo(0, allAgents.size() - 1);
            } while (good.contains(agent) || bad.contains(agent));

            bad.add(agent);
        }
    }

    @Override
    public void setCurrentTime(int time) {
        this.time = time;

        if (0 == time % changeInterval) {
            // switch good and bad agents and reassign capabilities
            switchCapabilities(good, bad, capabilities);

            // reassign deception models
            assignDeceptionModels(agents, good, bad, models);
        }
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

                        final Opinion o = new Opinion(reporter, agent, 0, time,
                                itd, sd_o);
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
        // random agent
        final int agent = generator.nextIntFromTo(0, agents.size() - 1);

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
        return new OscillationGUI();
    }
}
