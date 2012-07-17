package testbed.deceptionmodel;

import testbed.common.Utils;
import testbed.interfaces.ICondition;
import testbed.interfaces.IDeceptionModel;

public abstract class ExaggerationModel extends AbstractDeceptionModel
	implements IDeceptionModel {

    protected double kappa = Double.NaN;

    @Override
    public void initialize(Object... params) {
	ICondition<Double> validator = new ICondition<Double>() {
	    @Override
	    public void eval(Double var) {
		if (var > 1 || var < 0) {
		    throw new IllegalArgumentException(String.format(
			    "Kappa must be within [0, 1], but was %.2f.", var));
		}
	    }
	};

	kappa = Utils.extractParameter(validator, 0, params);
    }

    protected void kappaUnsetError() throws IllegalArgumentException {
	throw new IllegalArgumentException(String.format(
		"Kappa was not set for the deception model %s", getName()));
    }
}
