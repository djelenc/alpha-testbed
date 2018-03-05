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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import atb.common.DefaultRandomGenerator;
import atb.deceptionmodel.NegativeExaggeration;
import atb.deceptionmodel.PositiveExaggeration;
import atb.deceptionmodel.Truthful;
import atb.interfaces.DeceptionModel;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class OscillationScenarioTest {

    Oscillation scenario = null;

    @Before
    public void setUp() {
        scenario = new Oscillation();
        scenario.setRandomGenerator(new DefaultRandomGenerator(0));
    }

    @Test
    public void assignDeceptionModels() {
        List<Integer> all = new ArrayList<Integer>();
        List<Integer> good = new ArrayList<Integer>();
        List<Integer> bad = new ArrayList<Integer>();
        Map<Integer, Double> capabilities = new LinkedHashMap<Integer, Double>();
        double fracGood = 1d / 3d;
        double fracBad = 1d / 3d;

        for (int i = 0; i < 6; i++)
            all.add(i);

        DeceptionModel[][] models = new DeceptionModel[all.size()][all.size()];

        scenario.assignAgentsToGroups(all, good, bad, fracGood, fracBad);
        scenario.assignCapabilities(all, good, bad, capabilities);
        scenario.assignDeceptionModels(all, good, bad, models);

        for (int i = 0; i < models.length; i++) {
            for (int j = 0; j < models.length; j++) {
                if (j != i) {
                    String comb;
                    if (good.contains(i)) {
                        comb = "G";
                    } else if (bad.contains(i)) {
                        comb = "B";
                    } else {
                        comb = "N";
                    }

                    if (good.contains(j)) {
                        comb += "G";
                    } else if (bad.contains(j)) {
                        comb += "B";
                    } else {
                        comb += "N";
                    }

                    if (comb.equals("GG")) {
                        Assert.assertTrue(
                                models[i][j] instanceof PositiveExaggeration);
                    } else if (comb.equals("GB")) {
                        Assert.assertTrue(
                                models[i][j] instanceof PositiveExaggeration);
                    } else if (comb.equals("GN")) {
                        Assert.assertTrue(
                                models[i][j] instanceof NegativeExaggeration);
                    } else if (comb.equals("BG")) {
                        Assert.assertTrue(
                                models[i][j] instanceof PositiveExaggeration);
                    } else if (comb.equals("BB")) {
                        Assert.assertTrue(
                                models[i][j] instanceof PositiveExaggeration);
                    } else if (comb.equals("BN")) {
                        Assert.assertTrue(
                                models[i][j] instanceof NegativeExaggeration);
                    } else if (comb.equals("NG")) {
                        Assert.assertTrue(models[i][j] == null);
                    } else if (comb.equals("NB")) {
                        Assert.assertTrue(models[i][j] == null);
                    } else if (comb.equals("NN")) {
                        Assert.assertTrue(models[i][j] instanceof Truthful);
                    } else {
                        Assert.fail("Unknown combination.");
                    }
                }
            }
        }
    }

    @Test
    public void switchCapabilities() {
        List<Integer> all = new ArrayList<Integer>();
        List<Integer> good = new ArrayList<Integer>();
        List<Integer> bad = new ArrayList<Integer>();

        double fracGood = 1d / 3d;
        double fracBad = 1d / 3d;

        for (int i = 0; i < 9; i++)
            all.add(i);

        scenario.assignAgentsToGroups(all, good, bad, fracGood, fracBad);

        Map<Integer, Double> cap = new LinkedHashMap<Integer, Double>();

        scenario.assignCapabilities(all, good, bad, cap);

        for (int agent : all) {
            if (bad.contains(agent)) {
                Assert.assertTrue(cap.get(agent) <= 0.5);
            } else if (good.contains(agent)) {
                Assert.assertTrue(cap.get(agent) >= 0.5);
            }
        }

        scenario.switchCapabilities(good, bad, cap);

        for (int agent : all) {
            if (bad.contains(agent)) {
                Assert.assertTrue(cap.get(agent) <= 0.5);
            } else if (good.contains(agent)) {
                Assert.assertTrue(cap.get(agent) >= 0.5);
            }
        }
    }

    @Test
    public void assignAgentsToGroups() {
        List<Integer> all = new ArrayList<Integer>();
        List<Integer> good = new ArrayList<Integer>();
        List<Integer> bad = new ArrayList<Integer>();

        double fracGood = 1d / 3d;
        double fracBad = 1d / 3d;

        for (int i = 0; i < 21; i++)
            all.add(i);

        scenario.assignAgentsToGroups(all, good, bad, fracGood, fracBad);

        for (int agent : good) {
            Assert.assertTrue(all.contains(agent));
            Assert.assertFalse(bad.contains(agent));
        }

        for (int agent : bad) {
            Assert.assertTrue(all.contains(agent));
            Assert.assertFalse(good.contains(agent));
        }

        Assert.assertEquals((int) (fracGood * all.size()), good.size());
        Assert.assertEquals((int) (fracBad * all.size()), bad.size());
    }

    @Test
    public void assignAgentsToGroupsExtremeCases() {
        List<Integer> all = new ArrayList<Integer>();
        List<Integer> good = new ArrayList<Integer>();
        List<Integer> bad = new ArrayList<Integer>();

        double fracGood = 1d;
        double fracBad = 0d;

        for (int i = 0; i < 10; i++)
            all.add(i);

        scenario.assignAgentsToGroups(all, good, bad, fracGood, fracBad);

        for (int agent : good) {
            Assert.assertTrue(all.contains(agent));
            Assert.assertFalse(bad.contains(agent));
        }

        for (int agent : bad) {
            Assert.assertTrue(all.contains(agent));
            Assert.assertFalse(good.contains(agent));
        }

        Assert.assertEquals((int) (fracGood * all.size()), good.size());
        Assert.assertEquals((int) (fracBad * all.size()), bad.size());

        fracGood = 0d;
        fracBad = 0d;

        scenario.assignAgentsToGroups(all, good, bad, fracGood, fracBad);

        for (int agent : good) {
            Assert.assertTrue(all.contains(agent));
            Assert.assertFalse(bad.contains(agent));
        }

        for (int agent : bad) {
            Assert.assertTrue(all.contains(agent));
            Assert.assertFalse(good.contains(agent));
        }

        Assert.assertEquals((int) (fracGood * all.size()), good.size());
        Assert.assertEquals((int) (fracBad * all.size()), bad.size());

        fracGood = 0.5;
        fracBad = 0.5;

        scenario.assignAgentsToGroups(all, good, bad, fracGood, fracBad);

        for (int agent : good) {
            Assert.assertTrue(all.contains(agent));
            Assert.assertFalse(bad.contains(agent));
        }

        for (int agent : bad) {
            Assert.assertTrue(all.contains(agent));
            Assert.assertFalse(good.contains(agent));
        }

        Assert.assertEquals(all.size(), good.size() + bad.size());
        Assert.assertEquals((int) (fracGood * all.size()), good.size());
        Assert.assertEquals((int) (fracBad * all.size()), bad.size());
    }
}
