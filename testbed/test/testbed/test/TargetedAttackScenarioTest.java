package testbed.test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import testbed.common.DefaultRandomGenerator;
import testbed.scenario.TargetedAttack;

public class TargetedAttackScenarioTest {

    TargetedAttack scenario = null;

    @Before
    public void setUp() {
	scenario = new TargetedAttack();
	scenario.setRandomGenerator(new DefaultRandomGenerator(0));
	scenario.initialize(new Object[] { 100, 60, 20, 20, 0.1, 0.05 });
    }

    @Test
    public void assignAgentsToGroups() {
	List<Integer> all = new ArrayList<Integer>();
	List<Integer> neutral = new ArrayList<Integer>();
	List<Integer> attackers = new ArrayList<Integer>();
	List<Integer> targets = new ArrayList<Integer>();

	for (int i = 0; i < 21; i++)
	    all.add(i);

	scenario.assignAgentsToGroups(all, neutral, attackers, targets, 5, 2);

	Assert.assertEquals(5, attackers.size());
	Assert.assertEquals(2, targets.size());
	Assert.assertEquals(21, all.size());
    }

    @Test
    public void determineInteractionPartners() {
	final int numPartners = 80;
	scenario.initialize(new Object[] { 100, 60, 20, numPartners, 0.1, 0.05 });

	final Set<Integer> targets = new HashSet<Integer>();
	final Set<Integer> neutrals = new HashSet<Integer>();
	final Set<Integer> attackers = new HashSet<Integer>();
	final Set<Integer> ip = new HashSet<Integer>();

	targets.addAll(TargetedAttack.getTargets());
	neutrals.addAll(TargetedAttack.getNeutrals());
	attackers.addAll(TargetedAttack.getAttackers());

	ip.addAll(scenario.determineInteractionPartners(numPartners,
		TargetedAttack.getNeutrals(), TargetedAttack.getAttackers()));

	for (Integer agent : targets)
	    Assert.assertFalse(ip.contains(agent));

	for (Integer agent : attackers)
	    Assert.assertTrue(ip.contains(agent));

	for (Integer agent : neutrals)
	    Assert.assertTrue(ip.contains(agent));

	for (Integer agent : ip) {
	    Assert.assertFalse(targets.contains(agent));
	    Assert.assertTrue(attackers.contains(agent)
		    || neutrals.contains(agent));
	}
    }

    @Test
    public void determineInteractionPartnersBalanced() {
	final int numPartners = 5;
	scenario.initialize(new Object[] { 10, 6, 2, numPartners, 0.1, 0.05 });

	final Set<Integer> targets = new HashSet<Integer>();
	final Set<Integer> neutrals = new HashSet<Integer>();
	final Set<Integer> attackers = new HashSet<Integer>();
	final Set<Integer> ip = new HashSet<Integer>();

	targets.addAll(TargetedAttack.getTargets());
	neutrals.addAll(TargetedAttack.getNeutrals());
	attackers.addAll(TargetedAttack.getAttackers());

	ip.addAll(scenario.determineInteractionPartners(numPartners,
		TargetedAttack.getNeutrals(), TargetedAttack.getAttackers()));

	for (Integer agent : targets)
	    Assert.assertFalse(ip.contains(agent));

	for (Integer agent : neutrals)
	    Assert.assertTrue(ip.contains(agent));

	for (Integer agent : ip) {
	    Assert.assertFalse(targets.contains(agent));
	    Assert.assertTrue(attackers.contains(agent)
		    || neutrals.contains(agent));
	}
    }
}
