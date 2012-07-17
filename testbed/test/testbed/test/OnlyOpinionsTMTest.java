package testbed.test;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import junit.framework.Assert;

import org.junit.Ignore;
import org.junit.Test;

import cern.colt.Arrays;

import testbed.interfaces.Experience;
import testbed.interfaces.Opinion;
import testbed.trustmodel.OnlyOpinions;

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

	Set<Opinion> opinions = new LinkedHashSet<Opinion>();
	Set<Experience> experiences = new LinkedHashSet<Experience>();

	opinions.add(new Opinion(1, 0, 1, 0, 0.7));
	opinions.add(new Opinion(2, 0, 1, 0, 0.6));
	opinions.add(new Opinion(3, 0, 1, 0, 0.5));
	opinions.add(new Opinion(2, 1, 1, 0, 0.4));
	opinions.add(new Opinion(3, 1, 1, 0, 0.3));
	opinions.add(new Opinion(6, 1, 1, 0, 0.2));
	opinions.add(new Opinion(6, 5, 1, 0, 1.0));

	tm.calculateTrust(experiences, opinions);

	Map<Integer, Integer> ranks = tm.getRankings(0);

	Assert.assertEquals(1, (int) ranks.get(5));
	Assert.assertEquals(2, (int) ranks.get(0));
	Assert.assertEquals(3, (int) ranks.get(1));

	opinions.clear();
	opinions.add(new Opinion(6, 4, 1, 0, 0.0));
	opinions.add(new Opinion(6, 5, 1, 0, 0.0));
	
	tm.calculateTrust(experiences, opinions);
	ranks = tm.getRankings(0);

	Assert.assertEquals(1, (int) ranks.get(0));
	Assert.assertEquals(2, (int) ranks.get(1));
	Assert.assertEquals(3, (int) ranks.get(4));
	Assert.assertEquals(3, (int) ranks.get(5));
    }
}
