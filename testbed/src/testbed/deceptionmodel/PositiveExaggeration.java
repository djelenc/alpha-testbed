package testbed.deceptionmodel;

import testbed.interfaces.DeceptionModel;

public class PositiveExaggeration extends ExaggerationModel implements
	DeceptionModel {

    @Override
    public double calculate(double value) {
	if (Double.isNaN(super.kappa))
	    super.kappaUnsetError();

	return value * (1 - super.kappa) + super.kappa;
    }

    @Override
    public String toString() {
	return "Positive exaggeration";
    }
}
