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
package testbed.deceptionmodel;

import testbed.interfaces.DeceptionModel;

public class PositiveExaggeration extends ExaggerationModel implements
	DeceptionModel {

    @Override
    public double calculate(double value) {
	if (Double.isNaN(super.kappa))
	    super.kappaUnsetError();

	return value * (1 - super.kappa) + super.kappa;
    }

    @Override
    public String toString() {
	return "Positive exaggeration";
    }
}
