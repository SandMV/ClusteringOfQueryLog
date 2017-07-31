import java.util.HashSet;
import java.util.Set;

public class UnorderedPairImpl<T> implements UnorderedPair<T> {
    private Set<T> pair;

    UnorderedPairImpl(T point1, T point2) {
        pair = new HashSet<>();
        pair.add(point1);
        pair.add(point2);
    }

    @Override
    public boolean inPair(T point) {
        return pair.contains(point);
    }

    @Override
    public T getNotEqualTo(T point) {
        for (T e : pair) {
            if (!e.equals(pair)) {
                return e;
            }
        }
        return null;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof UnorderedPairImpl)) {
            return false;
        }
        if (other == this) {
            return true;
        }
        UnorderedPairImpl otherPair = (UnorderedPairImpl) other;
        return pair.equals(otherPair.pair);
    }

    @Override
    public int hashCode() {
        return pair.hashCode();
    }
}