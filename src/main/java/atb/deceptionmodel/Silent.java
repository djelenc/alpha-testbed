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
package atb.deceptionmodel;

import atb.interfaces.DeceptionModel;

/**
 * Silent deception model implies that either an agent does not know another
 * agent or it is unwilling to disclose its trust.
 * <p>
 * <p>
 * This class should only be used for notation -- in the implementation such
 * deception models should be replaced with null values.
 *
 * @author David
 */
public class Silent extends AbstractDeceptionModel implements DeceptionModel {

    private static final Error UP = new Error("Silent deception model "
            + "should not be used anywhere. It is only meant for notation");

    @Override
    public void initialize(Object... params) {
        throw UP;
    }

    @Override
    public double calculate(double value) {
        throw UP;
    }
}
