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
package atb.trustmodel.qad;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import atb.interfaces.Experience;
import atb.interfaces.Opinion;

import java.util.ArrayList;

import static atb.trustmodel.qad.Omega.*;

public class QTMTest {

    private QTM tm = null;

    @Before
    public void setUp() {
        tm = new QTM();
        tm.initialize();
    }

    @Test
    public void testQualitativeAverage() {
        double[] freq = new double[]{0, 0.1, 0.2, 0.3, 0.4};
        Assert.assertEquals(Omega.PT, tm.qualtitativeAverage(freq));
    }

    @Test
    public void testMedian() {
        Omega[] values = new Omega[]{U, U, PT, T, U, T, D, T, PD, T};
        Assert.assertEquals(PT, Omega.median(values));

        values = new Omega[]{};
        Assert.assertNull(Omega.median(values));

        values = new Omega[]{U, U, PT, T, U, T, D, T, null, null};
        Assert.assertEquals(PT, Omega.median(values));

        values = new Omega[]{null, null};
        Assert.assertNull(Omega.median(values));

        values = new Omega[]{U};
        Assert.assertEquals(U, Omega.median(values));

        values = new Omega[]{T, D};
        Assert.assertEquals(T, Omega.median(values));

        values = new Omega[]{PD, PT, U};
        Assert.assertEquals(U, Omega.median(values));
    }

    @Test(expected = IllegalArgumentException.class)
    public void medianExceptionNullArray() {
        Omega.median(null);
    }

    @Test
    public void testCredibilityWeights() {
        ArrayList<Opinion> opinions = new ArrayList<Opinion>();
        ArrayList<Experience> experiences = new ArrayList<Experience>();

        experiences.add(new Experience(3, 0, 0, 1d));
        opinions.add(new Opinion(0, 3, 0, 0, 1d, 0.05));
        opinions.add(new Opinion(1, 3, 0, 0, 1d, 0.05));
        opinions.add(new Opinion(2, 3, 0, 0, 0d, 0.05));

        tm.processOpinions(opinions);
        tm.processExperiences(experiences);
        tm.calculateTrust();

        experiences.clear();
        opinions.clear();
        double[] expected = new double[]{1.2, 1.2, 0.6, 1.0d};

        for (int j = 0; j < expected.length; j++)
            Assert.assertEquals(expected[j], tm.credibility[j], 0.001);
    }
}
