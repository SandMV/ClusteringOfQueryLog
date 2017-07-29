/**
 * @param <T> is the type of stored elements
 */
public interface IUnorderedPair<T> {
    /**
     * @param other some element of compatible type
     * @return any element from pair which is not equal to 'other'
     * return null if there is no such element (in case when both elements
     * in pair are equal to each other)
     */
    T getNotEqualTo(T other);

    /**
     * @param e some element of compatible type
     * @return true if pair contains element e,
     * false otherwise
     */
    boolean inPair(T e);
}
