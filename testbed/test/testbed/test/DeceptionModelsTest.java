package testbed.test;

import org.junit.Assert;
import org.junit.Test;

import testbed.deceptionmodel.NegativeExaggeration;
import testbed.deceptionmodel.PositiveExaggeration;
import testbed.deceptionmodel.Truthful;
import testbed.interfaces.IDeceptionModel;

public class DeceptionModelsTest {

    @Test
    public void testValidCallFor1() {
	IDeceptionModel m1 = new NegativeExaggeration();
	m1.initialize(1d);
	Assert.assertEquals(m1.calculate(0.5), 0d, 0.01);
    }

    @Test
    public void testValidCallFor0() {
	IDeceptionModel m1 = new PositiveExaggeration();
	IDeceptionModel m2 = new Truthful();
	m1.initialize(0d);
	m2.initialize();
	Assert.assertEquals(m1.calculate(0.5), m2.calculate(0.5), 0.01);
    }

    @Test
    public void testValidCall() {
	IDeceptionModel model = new PositiveExaggeration();
	model.initialize(0.25);
	Assert.assertEquals(0.625, model.calculate(0.5), 0.01);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidParametersWrongType() {
	IDeceptionModel model = new PositiveExaggeration();
	model.initialize("aaa");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidParametersTooBig() {
	IDeceptionModel model = new PositiveExaggeration();
	model.initialize(2d);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidParametersTooSmall() {
	IDeceptionModel model = new PositiveExaggeration();
	model.initialize(-5d);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidParametersToFew() {
	IDeceptionModel model = new PositiveExaggeration();
	model.initialize();
    }
}
