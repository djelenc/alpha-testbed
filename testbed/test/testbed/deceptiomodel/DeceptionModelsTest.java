/*******************************************************************************
 * Copyright (c) 2013 David Jelenc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     David Jelenc - initial API and implementation
 ******************************************************************************/
package testbed.deceptiomodel;

import org.junit.Assert;
import org.junit.Test;

import testbed.deceptionmodel.NegativeExaggeration;
import testbed.deceptionmodel.PositiveExaggeration;
import testbed.deceptionmodel.Truthful;
import testbed.interfaces.DeceptionModel;

public class DeceptionModelsTest {

    @Test
    public void testValidCallFor1() {
	DeceptionModel m1 = new NegativeExaggeration();
	m1.initialize(1d);
	Assert.assertEquals(m1.calculate(0.5), 0d, 0.01);
    }

    @Test
    public void testValidCallFor0() {
	DeceptionModel m1 = new PositiveExaggeration();
	DeceptionModel m2 = new Truthful();
	m1.initialize(0d);
	m2.initialize();
	Assert.assertEquals(m1.calculate(0.5), m2.calculate(0.5), 0.01);
    }

    @Test
    public void testValidCall() {
	DeceptionModel model = new PositiveExaggeration();
	model.initialize(0.25);
	Assert.assertEquals(0.625, model.calculate(0.5), 0.01);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidParametersWrongType() {
	DeceptionModel model = new PositiveExaggeration();
	model.initialize("aaa");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidParametersTooBig() {
	DeceptionModel model = new PositiveExaggeration();
	model.initialize(2d);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidParametersTooSmall() {
	DeceptionModel model = new PositiveExaggeration();
	model.initialize(-5d);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidParametersToFew() {
	DeceptionModel model = new PositiveExaggeration();
	model.initialize();
    }
}
