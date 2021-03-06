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

public class BRSPair {
    public double R, S;

    public BRSPair(double r, double s) {
        this.R = r;
        this.S = s;
    }

    public BRSPair() {
        this.R = 0;
        this.S = 0;
    }

    @Override
    public String toString() {
        return String.format("(%.2f, %.2f)", R, S);
    }
}
