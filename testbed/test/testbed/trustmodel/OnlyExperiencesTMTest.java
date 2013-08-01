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
package testbed.trustmodel;

import java.util.ArrayList;
import java.util.Map;

import junit.framework.Assert;

import org.junit.Test;

import testbed.interfaces.Experience;
import testbed.interfaces.Opinion;
import testbed.trustmodel.OnlyExperiences;

public class OnlyExperiencesTMTest {

    @Test
    public void testDynamicResizing() {
	OnlyExperiences tm = new OnlyExperiences();
	tm.initialize();
	tm.setCurrentTime(0);

	ArrayList<Opinion> opinions = null;
	ArrayList<Experience> experiences = new ArrayList<Experience>();

	experiences.add(new Experience(0, 0, 0, 0.9));
	tm.processExperiences(experiences);
	tm.processOpinions(opinions);
	tm.calculateTrust();
	experiences.clear();

	experiences.add(new Experience(1, 1, 0, 0.8));
	tm.processExperiences(experiences);
	tm.processOpinions(opinions);
	tm.calculateTrust();
	experiences.clear();

	experiences.add(new Experience(2, 1, 0, 0.7));
	experiences.add(new Experience(3, 1, 0, 0.6));
	tm.processExperiences(experiences);
	tm.processOpinions(opinions);
	tm.calculateTrust();
	experiences.clear();

	experiences.add(new Experience(10, 1, 0, 0.1));
	tm.processExperiences(experiences);
	tm.processOpinions(opinions);
	tm.calculateTrust();
	experiences.clear();

	Map<Integer, Double> trust = tm.getTrust(0);

	Assert.assertEquals(0.9, trust.get(0), 0.001);
	Assert.assertEquals(0.8, trust.get(1), 0.001);
	Assert.assertEquals(0.7, trust.get(2), 0.001);
	Assert.assertEquals(0.6, trust.get(3), 0.001);
	Assert.assertEquals(0.1, trust.get(10), 0.001);
    }
}
