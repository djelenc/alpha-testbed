package testbed.deceptionmodel;

import testbed.common.Utils;
import testbed.interfaces.ICondition;
import testbed.interfaces.IDeceptionModel;
import testbed.interfaces.IRandomGenerator;

/**
 * Random deception model returns a completely random value from (0, 1). The
 * initialization method requires a valid instance of the
 * {@link IRandomGenerator}.
 * 
 * @author David
 * 
 */
public class RandomDeception extends AbstractDeceptionModel implements
	IDeceptionModel {

    private IRandomGenerator generator;

    @Override
    public void initialize(Object... params) {
	generator = Utils.extractParameter(new ICondition<IRandomGenerator>() {
	    @Override
	    public void eval(IRandomGenerator var)
		    throws IllegalArgumentException {

		if (null == var) {
		    throw new IllegalArgumentException(
			    "Random generator not set.");
		}

	    }
	}, 0, params);
    }

    @Override
    public double calculate(double value) {
	return generator.nextDoubleFromTo(0, 1);
    }

    @Override
    public String toString() {
	return "Random opinion";
    }

}
