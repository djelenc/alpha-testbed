package testbed.deceptionmodel;

import testbed.interfaces.IDeceptionModel;

public class PositiveExaggeration extends ExaggerationModel implements
	IDeceptionModel {

    @Override
    public double calculate(double value) {
	if (Double.isNaN(super.kappa))
	    super.kappaUnsetError();

	return value * (1 - super.kappa) + super.kappa;
    }

    @Override
    public String getName() {
	return "Positive exaggeration";
    }
}
