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
package testbed.deceptionmodel;

import testbed.common.Utils;
import testbed.interfaces.DeceptionModel;
import testbed.interfaces.ParameterCondition;

public abstract class ExaggerationModel extends AbstractDeceptionModel
        implements DeceptionModel {

    protected double kappa = Double.NaN;

    @Override
    public void initialize(Object... params) {
        ParameterCondition<Double> validator = new ParameterCondition<Double>() {
            @Override
            public void eval(Double var) {
                if (var > 1 || var < 0) {
                    throw new IllegalArgumentException(String.format(
                            "Kappa must be within [0, 1], but was %.2f.", var));
                }
            }
        };

        kappa = Utils.extractParameter(validator, 0, params);
    }

    protected void kappaUnsetError() throws IllegalArgumentException {
        throw new IllegalArgumentException(String
                .format("Kappa was not set for the deception model %s", this));
    }
}
