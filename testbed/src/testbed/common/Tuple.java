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

public class Tuple<F, S> {
    public final F first;
    public final S second;

    public Tuple(F first, S second) {
	this.first = first;
	this.second = second;
    }

    @Override
    public String toString() {
	return String.format("<%s, %s>",
		first == null ? "null" : first.toString(),
		second == null ? "null" : second.toString());
    }

    @Override
    public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + ((first == null) ? 0 : first.hashCode());
	result = prime * result + ((second == null) ? 0 : second.hashCode());
	return result;
    }

    @Override
    public boolean equals(Object obj) {
	if (this == obj)
	    return true;
	if (obj == null)
	    return false;
	if (getClass() != obj.getClass())
	    return false;
	@SuppressWarnings("unchecked")
	Tuple<F, S> other = (Tuple<F, S>) obj;
	if (first == null) {
	    if (other.first != null)
		return false;
	} else if (!first.equals(other.first))
	    return false;
	if (second == null) {
	    if (other.second != null)
		return false;
	} else if (!second.equals(other.second))
	    return false;
	return true;
    }
}
