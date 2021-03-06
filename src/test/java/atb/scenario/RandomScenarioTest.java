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
import atb.deceptionmodel.Truthful;
import atb.interfaces.DeceptionModel;
import atb.interfaces.Scenario;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class RandomScenarioTest {

    @Test
    public void checkSizesOfCollections() {
        Scenario scenario = new Random();
        scenario.setRandomGenerator(new DefaultRandomGenerator(0));
        Map<DeceptionModel, Double> deceptionModels = new HashMap<DeceptionModel, Double>();
        deceptionModels.put(new Truthful(), 1d);

        scenario.initialize(1, 0.1, 0.05, deceptionModels, 0.25, 0.25, 1d);

        assertEquals(1, scenario.getAgents().size());
        assertEquals(1, scenario.getServices().size());

        Map<Integer, Double> capabilities = scenario.getCapabilities(0);

        assertEquals(1, capabilities.size());
        assertEquals(1, scenario.generateExperiences().size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void failWhenAdditionalParametersMissing() {
        Scenario scenario = new Random();
        scenario.setRandomGenerator(new DefaultRandomGenerator(0));
        scenario.initialize(1, 1);
    }
}
