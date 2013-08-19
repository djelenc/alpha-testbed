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
import java.util.Map;

import junit.framework.Assert;

import org.junit.Test;

import testbed.interfaces.ExampleGenerator;
import testbed.interfaces.Experience;
import testbed.interfaces.Opinion;
import testbed.interfaces.RandomGenerator;
import testbed.trustmodel.BRSWithFiltering;

public class BRSWithFilteringTMTest {

    @Test
    public void testCompute() {
	ArrayList<Experience> experiences = new ArrayList<Experience>();
	ArrayList<Opinion> opinions = new ArrayList<Opinion>();
	BRSWithFiltering tm = new BRSWithFiltering();
	int time = 0;
	RandomGenerator generator = new ExampleGenerator(0);
	tm.setRandomGenerator(generator);
	tm.initialize(1d, 0d, 0.01, 10d);

	time = 1;
	experiences.clear();
	opinions.clear();
	experiences.add(new Experience(0, 0, time, 1.0));
	opinions.add(new Opinion(1, 0, 0, time, 1.0, 0.05));
	opinions.add(new Opinion(2, 0, 0, time, 1.0, 0.05));
	opinions.add(new Opinion(3, 0, 0, time, 1.0, 0.05));
	opinions.add(new Opinion(4, 0, 0, time, 1.0, 0.05));
	opinions.add(new Opinion(5, 0, 0, time, 1.0, 0.05));
	opinions.add(new Opinion(6, 0, 0, time, 0.0, 0.05));
	opinions.add(new Opinion(7, 0, 0, time, 0.0, 0.05));
	opinions.add(new Opinion(8, 0, 0, time, 1.0, 0.05));
	experiences.add(new Experience(2, 0, time, 0.75));

	opinions.add(new Opinion(1, 3, 0, time, .60, 0.05));
	opinions.add(new Opinion(2, 3, 0, time, .50, 0.05));
	opinions.add(new Opinion(3, 3, 0, time, .70, 0.05));
	opinions.add(new Opinion(4, 3, 0, time, .40, 0.05));
	opinions.add(new Opinion(5, 3, 0, time, .60, 0.05));
	opinions.add(new Opinion(6, 3, 0, time, 0.30, 0.05));
	opinions.add(new Opinion(7, 3, 0, time, 0.0, 0.05));
	opinions.add(new Opinion(8, 3, 0, time, 1.0, 0.05));

	tm.setCurrentTime(time);
	tm.processExperiences(experiences);
	tm.processOpinions(opinions);
	tm.calculateTrust();
	Map<Integer, Double> score = tm.getTrust(0);

	Assert.assertEquals(0.9861111111111112, score.get(0), 0.00001);
	Assert.assertEquals(0.7500000000000000, score.get(2), 0.00001);
	Assert.assertEquals(0.5161290322580645, score.get(3), 0.00001);
    }
}
