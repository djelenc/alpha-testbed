package testbed.deceptionmodel;

import testbed.common.Utils;
import testbed.interfaces.ParameterCondition;
import testbed.interfaces.DeceptionModel;
import testbed.interfaces.RandomGenerator;

/**
 * Random deception model returns a completely random value from (0, 1). The
 * initialization method requires a valid instance of the
 * {@link RandomGenerator}.
 * 
 * @author David
 * 
 */
public class RandomDeception extends AbstractDeceptionModel implements
	DeceptionModel {

    private RandomGenerator generator;

    @Override
    public void initialize(Object... params) {
	generator = Utils.extractParameter(
		new ParameterCondition<RandomGenerator>() {
		    @Override
		    public void eval(RandomGenerator var)
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
