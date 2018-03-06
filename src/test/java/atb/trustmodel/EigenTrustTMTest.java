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
package atb.trustmodel;

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import atb.interfaces.Experience;
import atb.interfaces.Opinion;

import java.util.ArrayList;

@Ignore
public class EigenTrustTMTest {

    private EigenTrust tm;
    private ArrayList<Integer> agents, services;
    private ArrayList<Opinion> opinions;
    private ArrayList<Experience> experiences;

    @Before
    public void init() {
        tm = new EigenTrust();
        agents = new ArrayList<Integer>();
        services = new ArrayList<Integer>();
        agents.add(0);
        agents.add(1);
        agents.add(2);
        services.add(0);
        tm.initialize(0.5, 1d, 1d);
    }

    @Test
    public void testHasConverged() {
        double[] t_new = new double[]{0.26389, 0.625, 0.1111};
        double[] t_old = new double[]{1 / 3d, 1 / 3d, 1 / 3d};

        Assert.assertEquals(false, tm.hasConverged(t_new, t_old));
    }

    @Test
    public void testCalculateOpinions() {
        opinions = new ArrayList<Opinion>();
        experiences = new ArrayList<Experience>();

        opinions.add(new Opinion(0, 0, 0, 0, 0, 0.05));
        opinions.add(new Opinion(1, 0, 0, 0, 2 / 3d, 0.05));
        opinions.add(new Opinion(2, 0, 0, 0, 1 / 8d, 0.05));
        opinions.add(new Opinion(0, 1, 0, 0, 1, 0.05));
        opinions.add(new Opinion(1, 1, 0, 0, 0, 0.05));
        opinions.add(new Opinion(2, 1, 0, 0, 7 / 8d, 0.05));
        opinions.add(new Opinion(0, 2, 0, 0, 0, 0.05));
        opinions.add(new Opinion(1, 2, 0, 0, 1 / 3d, 0.05));
        opinions.add(new Opinion(2, 2, 0, 0, 0, 0.05));

        tm.processExperiences(experiences);
        tm.processOpinions(opinions);
        tm.calculateTrust();

        Assert.assertEquals(new Integer(2), tm.getTrust(0).get(0));
        Assert.assertEquals(new Integer(1), tm.getTrust(0).get(1));
        Assert.assertEquals(new Integer(3), tm.getTrust(0).get(2));
    }
}
