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
package testbed.common;

public class TrustTuple<F, S> implements Comparable<TrustTuple<F, S>> {
    public final F first;
    public final S second;

    public TrustTuple(F first, S second) {
	this.first = first;
	this.second = second;
    }

    @SuppressWarnings("unchecked")
    @Override
    public int compareTo(TrustTuple<F, S> o) {
	// this is needed to properly sort agents by their trust degrees
	// potential issue: two tuples are considered equal if they have the
	// same second component
	return ((Comparable<S>) this.second).compareTo(o.second);
    }

    @Override
    public String toString() {
	return String.format("TT<%s, %s>",
		first == null ? "null" : first.toString(),
		second == null ? "null" : second.toString());
    }
}
