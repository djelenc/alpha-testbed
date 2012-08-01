package testbed.test;

import java.util.LinkedHashSet;
import java.util.Set;

import junit.framework.Assert;

import org.junit.Test;

import testbed.interfaces.Experience;
import testbed.interfaces.Opinion;
import testbed.trustmodel.BetaReputation;

public class BetaReputationTMTest {

    @Test
    public void testCompute() {
	Set<Experience> experiences = new LinkedHashSet<Experience>();
	Set<Opinion> opinions = new LinkedHashSet<Opinion>();
	BetaReputation tm = new BetaReputation();
	int time = 0;
	tm.initialize(1d, 1d);

	time = 1;
	experiences.clear();
	opinions.clear();
	experiences.add(new Experience(0, 0, time, 0.70));

	tm.setCurrentTime(time);
	tm.processExperiences(experiences);
	tm.processOpinions(opinions);
	tm.calculateTrust();

	Assert.assertEquals(0.70, tm.computePairs().get(0).R, 0.001);

	time = 2;
	experiences.clear();
	opinions.clear();
	opinions.add(new Opinion(1, 0, 0, time, 1.0));
	opinions.add(new Opinion(2, 0, 0, time, 1.0));

	tm.setCurrentTime(time);
	tm.processExperiences(experiences);
	tm.processOpinions(opinions);
	tm.calculateTrust();

	Assert.assertEquals(0.70, tm.computePairs().get(0).R, 0.001);

	time = 3;
	experiences.clear();
	opinions.clear();
	experiences.add(new Experience(1, 0, time, 1.0));

	tm.setCurrentTime(time);
	tm.processExperiences(experiences);
	tm.processOpinions(opinions);
	tm.calculateTrust();

	Assert.assertEquals(0.95, tm.computePairs().get(0).R, 0.001);
	Assert.assertEquals(0.30, tm.computePairs().get(0).S, 0.001);

	time = 4;
	experiences.clear();
	opinions.clear();
	experiences.add(new Experience(2, 0, time, 0.50));
	opinions.add(new Opinion(2, 3, 0, time, 0.75));

	tm.setCurrentTime(time);
	tm.processExperiences(experiences);
	tm.processOpinions(opinions);
	tm.calculateTrust();

	Assert.assertEquals(0.50, tm.computePairs().get(2).R, 0.001);
	Assert.assertEquals(0.50, tm.computePairs().get(2).S, 0.001);
	Assert.assertEquals(0.08823, tm.computePairs().get(3).R, 0.001);
	Assert.assertEquals(0.02941, tm.computePairs().get(3).S, 0.001);

	// experiences.clear();
	// experiences.add(new Experience(0, 0, 4, 1.0));
	// tm.setCurrentTime(4);
	// tm.calculateTrust(experiences, opinions);
	// System.out.printf("%s\n", tm.compute());
	//
	// experiences.clear();
	// experiences.add(new Experience(1, 0, 0, 0.3));
	// experiences.add(new Experience(0, 0, 0, 0.5));
	// tm.calculateTrust(experiences, opinions);
    }

    @Test
    public void testOpinionsSet() {
	Set<Experience> experiences = new LinkedHashSet<Experience>();
	Set<Opinion> opinions = new LinkedHashSet<Opinion>();
	BetaReputation tm = new BetaReputation();
	tm.initialize(1d, 1d);

	opinions.add(new Opinion(0, 1, 0, 0, 0.50));
	opinions.add(new Opinion(1, 1, 0, 0, 0.75));
	opinions.add(new Opinion(2, 1, 0, 0, 1.00));

	tm.processExperiences(experiences);
	tm.processOpinions(opinions);
	tm.calculateTrust();

	Assert.assertEquals(0.50, tm.opinions[0][1].internalTrustDegree, 0.001);
	Assert.assertEquals(0.75, tm.opinions[1][1].internalTrustDegree, 0.001);
	Assert.assertEquals(1.00, tm.opinions[2][1].internalTrustDegree, 0.001);

	opinions.clear();
	opinions.add(new Opinion(2, 0, 0, 0, 0.50));
	opinions.add(new Opinion(2, 1, 0, 0, 0.50));
	opinions.add(new Opinion(5, 4, 0, 0, 0.50));

	tm.processExperiences(experiences);
	tm.processOpinions(opinions);
	tm.calculateTrust();

	Assert.assertEquals(0.50, tm.opinions[2][0].internalTrustDegree, 0.001);
	Assert.assertEquals(0.50, tm.opinions[2][1].internalTrustDegree, 0.001);
	Assert.assertEquals(0.50, tm.opinions[5][4].internalTrustDegree, 0.001);
    }

    @Test
    public void testExperiencesSet() {
	Set<Experience> experiences = new LinkedHashSet<Experience>();
	Set<Opinion> opinions = new LinkedHashSet<Opinion>();
	BetaReputation tm = new BetaReputation();
	tm.initialize(1d, 1d);

	experiences.add(new Experience(0, 0, 0, 1.0));
	tm.processExperiences(experiences);
	tm.processOpinions(opinions);
	tm.calculateTrust();
	experiences.clear();

	Assert.assertEquals(tm.experiences.get(0).get(0).outcome, 1, 0.001);

	experiences.add(new Experience(1, 0, 0, 0.3));
	experiences.add(new Experience(0, 0, 0, 0.5));
	tm.processExperiences(experiences);
	tm.processOpinions(opinions);
	tm.calculateTrust();
	experiences.clear();

	Assert.assertEquals(tm.experiences.get(0).get(0).outcome, 1, 0.001);
	Assert.assertEquals(tm.experiences.get(0).get(1).outcome, 0.5, 0.001);
	Assert.assertEquals(tm.experiences.get(1).get(0).outcome, 0.3, 0.001);
    }
}
