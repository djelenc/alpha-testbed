package testbed.test;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import junit.framework.Assert;

import org.junit.Test;

import testbed.interfaces.Experience;
import testbed.interfaces.Opinion;
import testbed.trustmodel.Simple;

public class SimpleTMTest {

    @Test
    public void testCombinedMixed() {
	Simple tm = new Simple();
	tm.initialize();

	Set<Opinion> opinions = new LinkedHashSet<Opinion>();
	Set<Experience> experiences = new LinkedHashSet<Experience>();

	experiences.add(new Experience(10, 1, 0, 0.9));
	experiences.add(new Experience(10, 1, 0, 0.9));
	opinions.add(new Opinion(1, 10, 1, 0, 0.3));

	experiences.add(new Experience(11, 1, 0, 0.9));
	opinions.add(new Opinion(1, 11, 1, 0, 0.3));

	tm.processExperiences(experiences);
	tm.processOpinions(opinions);
	tm.calculateTrust();

	Map<Integer, Integer> ranks = tm.getRankings(1);

	Assert.assertEquals(1, (int) ranks.get(10));
	Assert.assertEquals(2, (int) ranks.get(11));
    }

    @Test
    public void testCombinedSeparated() {
	Simple tm = new Simple();
	tm.initialize();

	Set<Opinion> opinions = new LinkedHashSet<Opinion>();
	Set<Experience> experiences = new LinkedHashSet<Experience>();

	opinions.add(new Opinion(1, 0, 1, 0, 0.7));
	opinions.add(new Opinion(2, 0, 1, 0, 0.6));
	opinions.add(new Opinion(3, 0, 1, 0, 0.5));
	opinions.add(new Opinion(2, 1, 1, 0, 0.4));
	opinions.add(new Opinion(3, 1, 1, 0, 0.3));
	opinions.add(new Opinion(6, 1, 1, 0, 0.2));
	opinions.add(new Opinion(6, 5, 1, 0, 0.1));

	tm.processExperiences(experiences);
	tm.processOpinions(opinions);
	tm.calculateTrust();

	Map<Integer, Integer> ranks = tm.getRankings(1);
	Assert.assertEquals(1, (int) ranks.get(0));
	Assert.assertEquals(2, (int) ranks.get(1));
	Assert.assertEquals(3, (int) ranks.get(5));

	experiences.add(new Experience(5, 1, 0, 0.8));
	experiences.add(new Experience(5, 1, 0, 0.7));
	experiences.add(new Experience(5, 1, 0, 0.6));

	tm.processExperiences(experiences);
	tm.processOpinions(opinions);
	tm.calculateTrust();

	ranks = tm.getRankings(1);
	Assert.assertEquals(1, (int) ranks.get(5));
	Assert.assertEquals(2, (int) ranks.get(0));
	Assert.assertEquals(3, (int) ranks.get(1));
    }

    @Test
    public void testOnlyExperience() {
	Simple tm = new Simple();
	tm.initialize();

	Set<Opinion> opinions = new LinkedHashSet<Opinion>();
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

    @Test
    public void testOnlyOpinions() {
	Simple tm = new Simple();
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

	tm.processExperiences(experiences);
	tm.processOpinions(opinions);
	tm.calculateTrust();

	Map<Integer, Integer> ranks = tm.getRankings(0);

	Assert.assertEquals(1, (int) ranks.get(5));
	Assert.assertEquals(2, (int) ranks.get(0));
	Assert.assertEquals(3, (int) ranks.get(1));

	opinions.clear();
	opinions.add(new Opinion(6, 4, 1, 0, 0.0));
	opinions.add(new Opinion(6, 5, 1, 0, 0.5));
	tm.processExperiences(experiences);
	tm.processOpinions(opinions);
	tm.calculateTrust();
	ranks = tm.getRankings(0);

	Assert.assertEquals(1, (int) ranks.get(0));
	Assert.assertEquals(2, (int) ranks.get(5));
	Assert.assertEquals(3, (int) ranks.get(1));
	Assert.assertEquals(4, (int) ranks.get(4));
    }
}
