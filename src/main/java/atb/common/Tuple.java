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
package atb.common;

public class Tuple<F, S> implements Comparable<Tuple<F, S>> {
    public final F first;
    public final S second;

    public Tuple(F first, S second) {
        this.first = first;
        this.second = second;
    }

    @SuppressWarnings("unchecked")
    @Override
    public int compareTo(Tuple<F, S> o) {
        return ((Comparable<S>) this.second).compareTo(o.second);
    }

}
