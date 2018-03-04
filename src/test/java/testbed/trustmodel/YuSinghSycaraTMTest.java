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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import testbed.interfaces.Experience;
import testbed.interfaces.Opinion;

import java.util.ArrayList;

public class YuSinghSycaraTMTest {

    private YuSinghSycara tm = null;

    @Before
    public void setUp() {
        tm = new YuSinghSycara();
        tm.initialize();
    }

    @Test
    public void testExpandArray() {
        ArrayList<Opinion> opinions = new ArrayList<Opinion>();
        ArrayList<Experience> experiences = new ArrayList<Experience>();

        experiences.add(new Experience(1, 0, 0, 0.5));
        opinions.add(new Opinion(0, 1, 0, 0, 0.7, 0.05));
        opinions.add(new Opinion(2, 1, 0, 0, 0.7, 0.05));
        opinions.add(new Opinion(3, 1, 0, 0, 0.7, 0.05));

        tm.processExperiences(experiences);
        tm.processOpinions(opinions);
        tm.calculateTrust();

        experiences.clear();
        opinions.clear();
        Assert.assertEquals(4, tm.credibility.length);

        experiences.add(new Experience(1, 0, 0, 0.1));
        opinions.add(new Opinion(5, 1, 0, 0, 1, 0.05));
        tm.processExperiences(experiences);
        tm.processOpinions(opinions);
        tm.calculateTrust();
        experiences.clear();
        opinions.clear();

        Assert.assertEquals(6, tm.credibility.length);
    }

    @Test
    public void testQueueMultipleExperiences() {
        ArrayList<Opinion> opinions = new ArrayList<Opinion>();
        ArrayList<Experience> experiences = new ArrayList<Experience>();

        experiences.add(new Experience(1, 0, 0, 0.5));
        experiences.add(new Experience(1, 0, 0, 0.6));
        experiences.add(new Experience(1, 0, 0, 0.7));
        experiences.add(new Experience(1, 0, 0, 0.8));
        experiences.add(new Experience(1, 0, 0, 0.9));
        tm.processExperiences(experiences);
        tm.processOpinions(opinions);
        tm.calculateTrust();
        experiences.clear();
        opinions.clear();

        double[] expected = new double[]{0.9, 0.8, 0.7, 0.6, 0.5};

        for (int i = 0; i < expected.length; i++)
            Assert.assertEquals(expected[i], tm.local.get(1)[i], 0.0001);
    }

    @Test
    public void testQueue() {
        ArrayList<Opinion> opinions = new ArrayList<Opinion>();
        ArrayList<Experience> experiences = new ArrayList<Experience>();

        experiences.add(new Experience(1, 0, 0, 0.5));
        tm.processExperiences(experiences);
        tm.processOpinions(opinions);
        tm.calculateTrust();
        experiences.clear();
        opinions.clear();

        experiences.add(new Experience(1, 0, 0, 0.6));
        tm.processExperiences(experiences);
        tm.processOpinions(opinions);
        tm.calculateTrust();
        experiences.clear();
        opinions.clear();

        experiences.add(new Experience(1, 0, 0, 0.7));
        tm.processExperiences(experiences);
        tm.processOpinions(opinions);
        tm.calculateTrust();
        experiences.clear();
        opinions.clear();

        experiences.add(new Experience(1, 0, 0, 0.8));
        tm.processExperiences(experiences);
        tm.processOpinions(opinions);
        tm.calculateTrust();
        experiences.clear();
        opinions.clear();

        experiences.add(new Experience(1, 0, 0, 0.9));
        tm.processExperiences(experiences);
        tm.processOpinions(opinions);
        tm.calculateTrust();
        experiences.clear();
        opinions.clear();

        double[] expected = new double[]{0.9, 0.8, 0.7, 0.6, 0.5};

        for (int i = 0; i < expected.length; i++)
            Assert.assertEquals(expected[i], tm.local.get(1)[i], 0.0001);

        experiences.add(new Experience(1, 0, 0, 1.0));
        tm.processExperiences(experiences);
        tm.processOpinions(opinions);
        tm.calculateTrust();
        experiences.clear();
        opinions.clear();

        expected = new double[]{1.0, 0.9, 0.8, 0.7, 0.6};
        for (int i = 0; i < expected.length; i++)
            Assert.assertEquals(expected[i], tm.local.get(1)[i], 0.0001);
    }
}
