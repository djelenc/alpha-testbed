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
package testbed.trustmodel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.junit.Ignore;
import org.junit.Test;

import testbed.interfaces.Experience;
import testbed.interfaces.Opinion;
import testbed.trustmodel.OnlyOpinions;
import cern.colt.Arrays;

public class OnlyOpinionsTMTest {

    @Ignore
    @Test
    public void test2DArrayCopy() {
	double[][] first = new double[][] { { 1, 2 }, { 3, 4 } };
	double[][] second = new double[3][3];

	System.out.printf("%s\n", Arrays.toString(first));

	for (int i = 0; i < first.length; i++) {
	    System.arraycopy(first[i], 0, second[i], 0, first.length);
	}

	System.out.printf("%s\n", Arrays.toString(second));
    }

    @Test
    public void testDynamicResizing() {
	OnlyOpinions tm = new OnlyOpinions();
	tm.initialize();

	ArrayList<Opinion> opinions = new ArrayList<Opinion>();
	ArrayList<Experience> experiences = new ArrayList<Experience>();

	final List<Integer> agents = new ArrayList<Integer>();
	agents.add(0);
	agents.add(1);
	agents.add(2);
	agents.add(3);
	agents.add(4);
	agents.add(5);
	agents.add(6);

	opinions.add(new Opinion(1, 0, 1, 0, 0.7, 0.05));
	opinions.add(new Opinion(2, 0, 1, 0, 0.6, 0.05));
	opinions.add(new Opinion(3, 0, 1, 0, 0.5, 0.05));
	opinions.add(new Opinion(2, 1, 1, 0, 0.4, 0.05));
	opinions.add(new Opinion(3, 1, 1, 0, 0.3, 0.05));
	opinions.add(new Opinion(6, 1, 1, 0, 0.2, 0.05));
	opinions.add(new Opinion(6, 5, 1, 0, 1.0, 0.05));

	tm.setAgents(agents);
	tm.processExperiences(experiences);
	tm.processOpinions(opinions);
	tm.calculateTrust();

	Map<Integer, Double> trust = tm.getTrust(0);

	Assert.assertEquals(1d, trust.get(5), 0.001);
	Assert.assertEquals(0.6, trust.get(0), 0.001);
	Assert.assertEquals(0.3, trust.get(1), 0.001);

	opinions.clear();
	opinions.add(new Opinion(6, 4, 1, 0, 0.0, 0.05));
	opinions.add(new Opinion(6, 5, 1, 0, 0.0, 0.05));

	tm.processExperiences(experiences);
	tm.processOpinions(opinions);
	tm.calculateTrust();

	trust = tm.getTrust(0);

	Assert.assertEquals(0d, trust.get(5), 0.001);
	Assert.assertEquals(0.6, trust.get(0), 0.001);
	Assert.assertEquals(0.3, trust.get(1), 0.001);
	Assert.assertEquals(0d, trust.get(4), 0.001);

    }
}
