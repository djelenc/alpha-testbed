package testbed.test;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import junit.framework.Assert;

import org.junit.Test;

import testbed.interfaces.Experience;
import testbed.interfaces.Opinion;
import testbed.trustmodel.BRSWithFiltering;

public class BRSWithFilteringTMTest {

    @Test
    public void testCompute() {
	Set<Experience> experiences = new LinkedHashSet<Experience>();
	Set<Opinion> opinions = new LinkedHashSet<Opinion>();
	BRSWithFiltering tm = new BRSWithFiltering();
	int time = 0;
	tm.initialize(1d, 0d, 0.01, 10d);

	time = 1;
	experiences.clear();
	opinions.clear();
	experiences.add(new Experience(0, 0, time, 1.0));
	opinions.add(new Opinion(1, 0, 0, time, 1.0));
	opinions.add(new Opinion(2, 0, 0, time, 1.0));
	opinions.add(new Opinion(3, 0, 0, time, 1.0));
	opinions.add(new Opinion(4, 0, 0, time, 1.0));
	opinions.add(new Opinion(5, 0, 0, time, 1.0));
	opinions.add(new Opinion(6, 0, 0, time, 0.0));
	opinions.add(new Opinion(7, 0, 0, time, 0.0));
	opinions.add(new Opinion(8, 0, 0, time, 1.0));
	experiences.add(new Experience(2, 0, time, 0.75));

	opinions.add(new Opinion(1, 3, 0, time, .60));
	opinions.add(new Opinion(2, 3, 0, time, .50));
	opinions.add(new Opinion(3, 3, 0, time, .70));
	opinions.add(new Opinion(4, 3, 0, time, .40));
	opinions.add(new Opinion(5, 3, 0, time, .60));
	opinions.add(new Opinion(6, 3, 0, time, 0.30));
	opinions.add(new Opinion(7, 3, 0, time, 0.0));
	opinions.add(new Opinion(8, 3, 0, time, 1.0));

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
