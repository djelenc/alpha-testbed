package testbed.scenario;

import testbed.common.Utils;
import testbed.interfaces.ICondition;
import testbed.interfaces.IParametersPanel;

/**
 * Transitive scenario where changes occur at given change intervals. At every
 * change a certain percentage of agents change behavior. Since this class
 * subclasses the {@link Transitive} scenario, it needs the parameters that are
 * required in the {@link Transitive}, plus two additional parameters:
 * <ul>
 * <li>5: (Double) The change density -- the percentage of agents that change at
 * every change interval
 * <li>6: (Double) The change interval -- time between to changes.
 * </ul>
 * 
 * 
 * @author David
 * 
 */
public class TransitiveWithChanges extends Transitive {

    /** Time between changes */
    protected int changeInterval;

    /** Percentage of agents that change at each change interval */
    protected double changeDens;

    @Override
    public void initialize(Object... params) {
	super.initialize(params);

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
    }

    @Override
    public IParametersPanel getParametersPanel() {
	return new TransitiveWithChangesGUI();
    }

    @Override
    public void setCurrentTime(int time) {
	this.time = time;

	// reassign capabilities and deception models
	if (0 == time % changeInterval) {
	    int counter = 0;
	    final int changes = (int) Math.round(agents.size() * changeDens);

	    while (counter < changes) {
		final int agent = generator.nextIntFromTo(0, agents.size() - 1);
		capabilities.put(agent, generator.nextDoubleFromTo(0, 1));
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
