/*******************************************************************************
 * Copyright (c) 2013 David Jelenc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     David Jelenc - initial API and implementation
 ******************************************************************************/
package testbed.scenario;

import testbed.interfaces.ParametersPanel;
import testbed.interfaces.RandomGenerator;
import testbed.interfaces.Scenario;

public abstract class AbstractScenario implements Scenario {

    protected RandomGenerator generator;

    @Override
    public String toString() {
	return getClass().getSimpleName();
    }

    @Override
    public ParametersPanel getParametersPanel() {
	return null;
    }

    @Override
    public void setRandomGenerator(RandomGenerator generator) {
	this.generator = generator;
    }
}
