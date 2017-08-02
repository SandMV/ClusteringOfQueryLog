import java.util.HashSet;
import java.util.Set;

/**
 * This implementation does not allow to store null values as this breaks semantics
 * of the method getNotEqual:
 *
 * If it stores two different values one of which is null and another isn't then method call from non-null
 * argument stored in Pair will return null which is ambiguous because null on the one hand is the element in
 * pair and on other hand is the sign of absence of other elements in pair
 *
 * @param <T> is the type of stored elements
 */
public class UnorderedPairImpl<T> implements UnorderedPair<T> {
    private Set<T> pair;

    UnorderedPairImpl(T point1, T point2) {
        if (point1 == null || point2 == null) {
            throw new IllegalArgumentException("Points should be not null");
        }

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
            if (!e.equals(point)) {
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