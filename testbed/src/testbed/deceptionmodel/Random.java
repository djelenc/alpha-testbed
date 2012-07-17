package testbed.deceptionmodel;

import testbed.common.Utils;
import testbed.interfaces.IDeceptionModel;

public class Random extends AbstractDeceptionModel implements IDeceptionModel {

    @Override
    public double calculate(double value) {
	return Utils.randomUnif(0, 1);
    }

}
