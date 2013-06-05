package testbed.test;

import java.util.ArrayList;
import java.util.List;

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
}
