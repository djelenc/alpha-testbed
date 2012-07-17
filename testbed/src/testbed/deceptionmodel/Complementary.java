package testbed.deceptionmodel;

import testbed.interfaces.IDeceptionModel;

public class Complementary extends AbstractDeceptionModel implements
	IDeceptionModel {

    @Override
    public double calculate(double value) {
	return 1 - value;
    }
}
