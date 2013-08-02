/*
 * Copyright (c) 2013 David Jelenc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     David Jelenc - initial API and implementation
 */
package testbed.common;

import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.Assert;
import org.junit.Test;

import testbed.common.Utils;
import testbed.interfaces.ParameterCondition;

public class UtilsTest {

    @Test
    public void orderedMapTest() {
	Map<Integer, Integer> map = new LinkedHashMap<Integer, Integer>();
	map.put(3, 10);
	map.put(4, 10);
	map.put(1, 30);
	map.put(2, 50);
	map.put(5, 30);

	Map<Integer, Integer> map2 = Utils.ordered(map);

	int prev = Integer.MIN_VALUE;

	for (Map.Entry<Integer, Integer> entry : map2.entrySet()) {
	    Assert.assertTrue(prev < entry.getKey());
	    prev = entry.getKey();
	}
    }

    @Test
    public void extractParameters() {
	ParameterCondition<Double> cond = new ParameterCondition<Double>() {
	    @Override
	    public void eval(Double var) {
		if (var < 1d)
		    throw new IllegalArgumentException(String.format(
			    "Var should be >=1, but was: %.2f", var));
	    }
	};

	Object[] params = new Object[] { 1d, "aa", 0d, 1, null };
	double var = Utils.extractParameter(cond, 0, params);

	Assert.assertEquals(1d, var, 0.01);

	try {
	    Utils.extractParameter(cond, 4, params);
	    Assert.fail();
	} catch (Exception e) {
	    Assert.assertTrue(true);
	}

	try {
	    Utils.extractParameter(cond, 10, params);
	    Assert.fail();
	} catch (Exception e) {
	    Assert.assertTrue(true);
	}

	try {
	    Utils.extractParameter(cond, -1, params);
	    Assert.fail();
	} catch (Exception e) {
	    Assert.assertTrue(true);
	}

	try {
	    Utils.extractParameter(cond, 1, params);
	    Assert.fail();
	} catch (Exception e) {
	    Assert.assertTrue(true);
	}

	try {
	    Utils.extractParameter(cond, 2, params);
	    Assert.fail();
	} catch (Exception e) {
	    Assert.assertTrue(true);
	}
    }

}
