package testbed.deceptionmodel;

import testbed.interfaces.IDeceptionModel;

public class Truthful extends AbstractDeceptionModel implements IDeceptionModel {

    @Override
    public double calculate(double value) {
	return value;
    }

}
