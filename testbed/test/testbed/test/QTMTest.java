package testbed.test;

import static testbed.trustmodel.qad.Omega.D;
import static testbed.trustmodel.qad.Omega.PD;
import static testbed.trustmodel.qad.Omega.PT;
import static testbed.trustmodel.qad.Omega.T;
import static testbed.trustmodel.qad.Omega.U;

import java.util.ArrayList;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
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
    public void testQualitativeAverage() {
	double[] freq = new double[] { 0, 0.1, 0.2, 0.3, 0.4 };
	Assert.assertEquals(Omega.PT, tm.qualtitativeAverage(freq));
    }

    @Ignore
    @Test
    public void testVariance() {
	double[] freq;
	freq = new double[] { 0, 0.1, 0.2, 0.3, 0.4 };
	System.out.printf("%.4f\n", tm.variance(freq));
	// Assert.assertEquals(0.43478, tm.variance(freq), 0.001);

	freq = new double[] { 0, 0, 0.5, 0.5, 0 };
	Assert.assertEquals(0.6, tm.variance(freq), 0.001);

	freq = new double[] { 0, 0, 1, 0, 0 };
	Assert.assertEquals(1d, tm.variance(freq), 0.001);
    }

    @Test
    public void testMedian() {
	Omega[] values = new Omega[] { U, U, PT, T, U, T, D, T, PD, T };
	Assert.assertEquals(PT, Omega.median(values));

	values = new Omega[] {};
	Assert.assertNull(Omega.median(values));

	values = new Omega[] { U, U, PT, T, U, T, D, T, null, null };
	Assert.assertEquals(PT, Omega.median(values));

	values = new Omega[] { null, null };
	Assert.assertNull(Omega.median(values));

	values = new Omega[] { U };
	Assert.assertEquals(U, Omega.median(values));

	values = new Omega[] { T, D };
	Assert.assertEquals(T, Omega.median(values));

	values = new Omega[] { PD, PT, U };
	Assert.assertEquals(U, Omega.median(values));
    }

    @Test(expected = IllegalArgumentException.class)
    public void medianExceptionNullArray() {
	Omega.median(null);
    }

    @Test
    public void getTrust() {
	ArrayList<Opinion> opinions = new ArrayList<Opinion>();
	ArrayList<Experience> experiences = new ArrayList<Experience>();

	experiences.add(new Experience(3, 0, 0, 1d));
	opinions.add(new Opinion(0, 3, 0, 0, 1d));
	opinions.add(new Opinion(1, 3, 0, 0, 1d));
	opinions.add(new Opinion(2, 3, 0, 0, 0d));

	tm.processOpinions(opinions);
	tm.processExperiences(experiences);

	tm.getTrust(0);

	experiences.clear();
	opinions.clear();
    }
}
