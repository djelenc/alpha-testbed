package testbed.common;

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
