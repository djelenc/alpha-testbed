package testbed.test;

import java.util.ArrayList;
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

	opinions.add(new Opinion(1, 0, 1, 0, 0.7, 0.05));
	opinions.add(new Opinion(2, 0, 1, 0, 0.6, 0.05));
	opinions.add(new Opinion(3, 0, 1, 0, 0.5, 0.05));
	opinions.add(new Opinion(2, 1, 1, 0, 0.4, 0.05));
	opinions.add(new Opinion(3, 1, 1, 0, 0.3, 0.05));
	opinions.add(new Opinion(6, 1, 1, 0, 0.2, 0.05));
	opinions.add(new Opinion(6, 5, 1, 0, 1.0, 0.05));

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
