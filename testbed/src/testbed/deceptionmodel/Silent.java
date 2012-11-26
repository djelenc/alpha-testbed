package testbed.deceptionmodel;

import testbed.interfaces.DeceptionModel;

/**
 * Silent deception model implies that either an agent does not know another
 * agent or it is unwilling to disclose its trust.
 * 
 * <p>
 * This class should only be used for notation -- in the implementation such
 * deception models should be replaced with null values.
 * 
 * @author David
 * 
 */
public class Silent extends AbstractDeceptionModel implements DeceptionModel {

    private static final Error UP = new Error("Silent deception model "
	    + "should not be used anywhere. It is only meant for notation");

    @Override
    public void initialize(Object... params) {
	throw UP;
    }

    @Override
    public double calculate(double value) {
	throw UP;
    }
}
