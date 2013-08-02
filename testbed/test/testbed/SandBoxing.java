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
package testbed;

import junit.framework.Assert;

import org.junit.Test;

import testbed.interfaces.Metric;
import testbed.metric.CumulativeNormalizedUtility;

public class SandBoxing {

    @Test
    public void objectCreation() {
	Metric m = new CumulativeNormalizedUtility();
	Metric copy;

	try {
	    copy = m.getClass().newInstance();
	} catch (Exception e) {
	    copy = null;
	}

	Assert.assertTrue(copy != m);
	Assert.assertEquals(copy.toString(), m.toString());
	Assert.assertNotSame("The same", copy, m);
    }

    @Test
    public void hashing() {
	Metric nu1 = new CumulativeNormalizedUtility();
	Metric nu2 = new CumulativeNormalizedUtility();
	nu1.initialize();
	nu2.initialize();

	int first = 1;
	int second = 2;

	int hash11 = nu1.getClass().hashCode() ^ first;
	int hash21 = nu2.getClass().hashCode() ^ first;
	int hash12 = nu1.getClass().hashCode() ^ second;
	int hash22 = nu2.getClass().hashCode() ^ second;

	Assert.assertTrue(hash11 == hash21);
	Assert.assertTrue(hash21 != hash12);
	Assert.assertTrue(hash12 == hash22);
    }
}
