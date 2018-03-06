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

/**
 * Enumeration class that represents trust degrees and grades of outcomes for
 * trust model of {@link AbdulRahmanHailes Abdul-Rahman and Hailes}.
 *
 * @author David
 */
public enum TD {
    VB(0.00), B(0.25), G(0.50), VG(0.75);

    public final double numeric;

    private TD(double numeric) {
        this.numeric = numeric;
    }

    public static TD fromDouble(double d) {
        if (d < 0d || d > 1d)
            throw new IllegalArgumentException(
                    String.format("TD must be within [0, 1], but was %.2f", d));

        if (d < B.numeric) {
            return VB;
        } else if (d < G.numeric) {
            return B;
        } else if (d < VG.numeric) {
            return G;
        } else {
            return VG;
        }
    }

    public static TD fromIndex(int i) {
        for (TD td : values())
            if (td.ordinal() == i)
                return td;

        throw new IllegalArgumentException(
                String.format("Cannot determine TD for rank %d", i));
    }
}
