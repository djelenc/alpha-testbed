package testbed.test;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

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

	Set<Opinion> opinions = null;
	Set<Experience> experiences = new LinkedHashSet<Experience>();

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

	Map<Integer, Integer> ranks = tm.getRankings(0);

	Assert.assertEquals(1, (int) ranks.get(0));
	Assert.assertEquals(2, (int) ranks.get(1));
	Assert.assertEquals(3, (int) ranks.get(2));
	Assert.assertEquals(4, (int) ranks.get(3));
	Assert.assertEquals(5, (int) ranks.get(10));
    }
}
