/*
 * Copyright (c) 2013 David Jelenc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors:
 *     David Jelenc - initial API and implementation
 */
package atb.scenario;

import atb.common.DefaultRandomGenerator;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class TargetedAttackScenarioTest {

    TargetedAttack scenario = null;

    @Before
    public void setUp() {
        scenario = new TargetedAttack();
        scenario.setRandomGenerator(new DefaultRandomGenerator(0));
        scenario.initialize(100, 60, 20, 20, 0.1, 0.05,
                TargetedAttackStrategy.LEVEL_1);
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

        assertEquals(5, attackers.size());
        assertEquals(2, targets.size());
        assertEquals(21, all.size());
    }

    @Test
    public void determineInteractionPartners() {
        final int numPartners = 80;
        scenario.initialize(100, 60, 20, numPartners, 0.1, 0.05,
                TargetedAttackStrategy.LEVEL_1);

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
            assertFalse(ip.contains(agent));

        for (Integer agent : attackers)
            assertTrue(ip.contains(agent));

        for (Integer agent : neutrals)
            assertTrue(ip.contains(agent));

        for (Integer agent : ip) {
            assertFalse(targets.contains(agent));
            assertTrue(
                    attackers.contains(agent) || neutrals.contains(agent));
        }
    }

    @Test
    public void determineInteractionPartnersBalanced() {
        final int numPartners = 5;
        scenario.initialize(10, 6, 2, numPartners, 0.1, 0.05,
                TargetedAttackStrategy.LEVEL_1);

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
            assertFalse(ip.contains(agent));

        for (Integer agent : neutrals)
            assertTrue(ip.contains(agent));

        for (Integer agent : ip) {
            assertFalse(targets.contains(agent));
            assertTrue(
                    attackers.contains(agent) || neutrals.contains(agent));
        }
    }
}
