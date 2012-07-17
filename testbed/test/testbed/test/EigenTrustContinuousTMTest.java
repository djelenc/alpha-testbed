package testbed.test;

import java.util.LinkedHashSet;
import java.util.Set;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import testbed.interfaces.Experience;
import testbed.interfaces.Opinion;
import testbed.trustmodel.EigenTrustContinuous;

public class EigenTrustContinuousTMTest {

    private EigenTrustContinuous tm;
    private Set<Integer> agents, services;
    private Set<Opinion> opinions;
    private Set<Experience> experiences;

    @Before
    public void init() {
	tm = new EigenTrustContinuous();
	agents = new LinkedHashSet<Integer>();
	services = new LinkedHashSet<Integer>();
	agents.add(0);
	agents.add(1);
	agents.add(2);
	services.add(0);
	tm.initialize(agents, services);
    }

    @Test
    public void testHasConverged() {
	double[] t_new = new double[] { 0.26389, 0.625, 0.1111 };
	double[] t_old = new double[] { 1 / 3d, 1 / 3d, 1 / 3d };

	Assert.assertEquals(false, tm.hasConverged(t_new, t_old));
    }

    @Ignore
    @Test
    public void testCalculateOpinions() {
	// test data is deprecated since the algorithm was changed
	opinions = new LinkedHashSet<Opinion>();
	experiences = new LinkedHashSet<Experience>();

	opinions.add(new Opinion(0, 0, 0, 0, 0));
	opinions.add(new Opinion(1, 0, 0, 0, 2 / 3d));
	opinions.add(new Opinion(2, 0, 0, 0, 1 / 8d));
	opinions.add(new Opinion(0, 1, 0, 0, 1));
	opinions.add(new Opinion(1, 1, 0, 0, 0));
	opinions.add(new Opinion(2, 1, 0, 0, 7 / 8d));
	opinions.add(new Opinion(0, 2, 0, 0, 0));
	opinions.add(new Opinion(1, 2, 0, 0, 1 / 3d));
	opinions.add(new Opinion(2, 2, 0, 0, 0));

	tm.calculateTrust(experiences, opinions);

	Assert.assertEquals(new Integer(2), tm.getRankings(0).get(0));
	Assert.assertEquals(new Integer(1), tm.getRankings(0).get(1));
	Assert.assertEquals(new Integer(3), tm.getRankings(0).get(2));
    }
}
