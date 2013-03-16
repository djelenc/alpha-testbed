package testbed.test;

import java.util.ArrayList;
import java.util.Map;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import testbed.interfaces.Experience;
import testbed.interfaces.Opinion;
import testbed.trustmodel.Travos;

public class TravosTMTest {
    private Travos tm = null;

    @Before
    public void setUp() {
	tm = new Travos();
	tm.initialize(0.5, 10, 0.1, 0.95, 0.2);
    }

    @Test
    public void testFormulaeMethods() {
	final double integral = tm.integrate(11, 4, 0.2, 0.4);

	Assert.assertEquals(0.00489242, integral, 0.001);
	Assert.assertEquals(0.288675, tm.standardDeviation(0, 0), 0.001);
	Assert.assertEquals(0.00962886, tm.scaleM(0.498833, 0.287527), 0.001);
	Assert.assertEquals(0.0143514, tm.scaleN(0.498833, 0.287527), 0.001);
	Assert.assertEquals(0.498833, tm.adjustMean(16, 47, integral), 0.000001);
    }

    @Test
    public void basicTravos() {
	ArrayList<Experience> experiences = new ArrayList<Experience>();
	ArrayList<Opinion> opinions = new ArrayList<Opinion>();

	experiences.clear();
	opinions.clear();
	experiences.add(new Experience(1, 0, 0, 0.05));
	opinions.add(new Opinion(1, 1, 0, 0, 0.0));
	opinions.add(new Opinion(2, 1, 0, 0, 1.0));
	opinions.add(new Opinion(3, 1, 0, 0, 0.5));

	tm.processExperiences(experiences);
	tm.processOpinions(opinions);
	tm.calculateTrust();
	tm.getTrust(0);
    }

    @Test
    public void walkthroughScenario() {
	ArrayList<Experience> experiences = new ArrayList<Experience>();
	ArrayList<Opinion> opinions = new ArrayList<Opinion>();
	Travos.OPINION_SAMPLE_NUM = 1;

	experiences.clear();
	opinions.clear();

	for (int i = 0; i < 22; i++)
	    experiences.add(new Experience(2, 0, 0, (i < 17 ? 1 : 0)));

	for (int i = 0; i < 17; i++)
	    experiences.add(new Experience(3, 0, 0, (i < 2 ? 1 : 0)));

	for (int i = 0; i < 23; i++)
	    experiences.add(new Experience(4, 0, 0, (i < 18 ? 1 : 0)));

	for (int i = 0; i < 23; i++)
	    experiences.add(new Experience(5, 0, 0, (i < 9 ? 1 : 0)));

	for (int i = 0; i < 3; i++)
	    experiences.add(new Experience(6, 0, 0, (i < 3 ? 1 : 0)));

	for (int i = 0; i < 29; i++)
	    experiences.add(new Experience(7, 0, 0, (i < 18 ? 1 : 0)));

	tm.processExperiences(experiences);
	tm.processOpinions(opinions);
	tm.calculateTrust();
	Map<Integer, Double> trust = tm.getTrust(0);

	Assert.assertEquals(0.7500, trust.get(2), 0.001);
	Assert.assertEquals(0.1579, trust.get(3), 0.001);
	Assert.assertEquals(0.7600, trust.get(4), 0.001);
	Assert.assertEquals(0.4000, trust.get(5), 0.001);
	Assert.assertEquals(0.8000, trust.get(6), 0.001);
	Assert.assertEquals(0.6129, trust.get(7), 0.001);

	experiences.clear();
	opinions.clear();

	opinions.add(new Opinion(8, 6, 0, 0, 15 / 61d));
	opinions.add(new Opinion(9, 6, 0, 0, 4 / 5d));
	opinions.add(new Opinion(10, 6, 0, 0, 1d));

	tm.processExperiences(experiences);
	tm.processOpinions(opinions);
	tm.calculateTrust();

	tm.observations.get(8)[1].R = 11;
	tm.observations.get(8)[1].S = 4;
	tm.observations.get(9)[3].R = 22;
	tm.observations.get(9)[3].S = 10;
	tm.observations.get(10)[3].R = 18;
	tm.observations.get(10)[3].S = 8;

	Assert.assertEquals(0.8349, tm.getTrust(0).get(6), 0.001);
    }
}
