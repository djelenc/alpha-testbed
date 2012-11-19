package testbed.test;

import static testbed.trustmodel.qad.Omega.D;
import static testbed.trustmodel.qad.Omega.PD;
import static testbed.trustmodel.qad.Omega.PT;
import static testbed.trustmodel.qad.Omega.T;
import static testbed.trustmodel.qad.Omega.U;

import java.util.ArrayList;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import testbed.interfaces.Experience;
import testbed.interfaces.Opinion;
import testbed.trustmodel.qad.Omega;
import testbed.trustmodel.qad.QTM;

public class QTMTest {

    private QTM tm = null;

    @Before
    public void setUp() {
	tm = new QTM();
	tm.initialize();
    }

    @Test
    public void testMedian() {
	Omega[] values = new Omega[] { U, U, PT, T, U, T, D, T, PD, T };
	Assert.assertEquals(PT, tm.median(values));

	values = new Omega[] {};
	Assert.assertNull(tm.median(values));

	values = new Omega[] { U, U, PT, T, U, T, D, T, null, null };
	Assert.assertEquals(PT, tm.median(values));

	values = new Omega[] { null, null };
	Assert.assertNull(tm.median(values));

	values = new Omega[] { U };
	Assert.assertEquals(U, tm.median(values));

	values = new Omega[] { T, D };
	Assert.assertEquals(T, tm.median(values));

	values = new Omega[] { PD, PT, U };
	Assert.assertEquals(U, tm.median(values));
    }

    @Test(expected = IllegalArgumentException.class)
    public void medianExceptionNullArray() {
	tm.median(null);
    }

    @Test
    public void testCredibilityWeights1() {
	ArrayList<Opinion> opinions = new ArrayList<Opinion>();
	ArrayList<Experience> experiences = new ArrayList<Experience>();

	experiences.add(new Experience(3, 0, 0, 1d));
	opinions.add(new Opinion(0, 3, 0, 0, 1d));
	opinions.add(new Opinion(1, 3, 0, 0, 1d));
	opinions.add(new Opinion(2, 3, 0, 0, 0d));

	tm.processExperiences(experiences);
	tm.processOpinions(opinions);
	tm.calculateTrust();

	experiences.clear();
	opinions.clear();
	double[] expected = new double[] { 1.2, 1.2, 0.6, 1.0d };

	for (int j = 0; j < expected.length; j++)
	    Assert.assertEquals(expected[j], tm.credibility[j], 0.001);
    }

    @Test
    public void testCredibilityWeights2() {
	ArrayList<Opinion> opinions = new ArrayList<Opinion>();
	ArrayList<Experience> experiences = new ArrayList<Experience>();

	experiences.add(new Experience(3, 0, 0, 1d));
	opinions.add(new Opinion(0, 3, 0, 0, 0d));
	opinions.add(new Opinion(1, 3, 0, 0, 0d));
	opinions.add(new Opinion(2, 3, 0, 0, 0d));

	tm.processExperiences(experiences);
	tm.processOpinions(opinions);
	tm.calculateTrust();

	experiences.clear();
	opinions.clear();
	double[] expected = new double[] { 1d, 1d, 1d, 1d };

	for (int j = 0; j < expected.length; j++)
	    Assert.assertEquals(expected[j], tm.credibility[j], 0.001);
    }
}
