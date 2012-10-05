package testbed.deceptionmodel;

import testbed.interfaces.DeceptionModel;

public class Complementary extends AbstractDeceptionModel implements
	DeceptionModel {

    @Override
    public double calculate(double value) {
	return 1 - value;
    }
}
