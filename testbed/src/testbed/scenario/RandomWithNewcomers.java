package testbed.scenario;

import testbed.common.Utils;
import testbed.deceptionmodel.Complementary;
import testbed.interfaces.ICondition;
import testbed.interfaces.IDeceptionModel;
import testbed.interfaces.IParametersPanel;

/**
 * Very similar to the {@link Random}, but this scenario features entering of
 * new agents to the system at given time intervals. New agents are all liars --
 * they all use {@link Complementary} deception model.
 * 
 * <p>
 * The scenario requires almost the same parameters as the {@link Random}
 * scenario, but there are some differences. Parameter 6 is ignored (all agents
 * are potential interaction counterparts), and two additional parameters are
 * required:
 * <ul>
 * <li>7 (int): changeInterval -- the interval at which new agents enter the
 * system
 * <li>8 (int): howManyNewAgents -- the number of new agents that enter the
 * system at every changeInterval
 * </ul>
 * 
 * @author David
 * 
 */
public class RandomWithNewcomers extends Random {

    protected static final String INTERVAL_EX = "The change interval must be a positive integer, but was %d.";
    protected static final String NEW_NUM_EX = "The number of newcomers must be a positive integer, but was %d.";
    protected static final IDeceptionModel LIAR = new Complementary();
    protected final static ICondition<Integer> VAL_NEW_NUM, VAL_INTERVAL;

    /** Time between changes */
    protected int changeInterval;

    /** The number of new agents that enter the system */
    protected int newcomersNumber;

    static {
	VAL_NEW_NUM = new ICondition<Integer>() {
	    @Override
	    public void eval(Integer var) {
		if (var < 1)
		    throw new IllegalArgumentException(String.format(
			    NEW_NUM_EX, var));
	    }
	};

	VAL_INTERVAL = new ICondition<Integer>() {
	    @Override
	    public void eval(Integer var) {
		if (var < 1)
		    throw new IllegalArgumentException(String.format(
			    INTERVAL_EX, var));
	    }
	};

	LIAR.initialize();
    }

    @Override
    public void initialize(Object... parameters) {
	super.initialize(parameters);

	changeInterval = Utils.extractParameter(VAL_INTERVAL, 7, parameters);
	newcomersNumber = Utils.extractParameter(VAL_NEW_NUM, 8, parameters);
    }

    @Override
    public IParametersPanel getParametersPanel() {
	return new RandomWithNewcomersGUI();
    }

    @Override
    public void setCurrentTime(int time) {
	this.time = time;

	if (0 == time % changeInterval) {
	    final int newSize = agents.size() + newcomersNumber;

	    for (int agent = agents.size(); agent < newSize; agent++) {
		// add agent
		agents.add(agent);

		// assign capabilities
		capabilities.put(agent, generator.nextDoubleFromTo(0, 1));

		// assign deception model
		deceptionModels.put(agent, LIAR);
	    }

	    partners.clear();
	    partners.addAll(agents);
	}
    }

    @Override
    public String toString() {
	return "Random with newcomers";
    }
}
