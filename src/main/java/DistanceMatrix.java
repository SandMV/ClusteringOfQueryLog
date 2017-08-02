/**
 * Created by sandulmv on 29.07.17.
 */
import java.util.*;

/**
 * DistanceMatrixImpl stores distances between pairs of points of type T
 * distances must be greater or equal to 0
 * @param <T> determines the type of coordinates
 */
public interface DistanceMatrix<T> {

    /**
     * adds new distance to DistanceMatrixImpl associated with
     * (point1, point2) unordered pair
     * if such distance was already determined replaces it with the new value
     * @param point1 is one of two coordinates with which distance will be associated
     * @param point2 is one of two coordinates with which distance will be associated
     * @param distance between point1 and point2
     * @return old value associated with {point1, point2}, null otherwise
     * @throws IllegalArgumentException if distance < 0
     */
    Double addDistance(T point1, T point2, double distance);

    /**
     * removes distance from DistanceMatrixImpl which corresponding to
     * (point1, point2) unordered pair
     * @return distance between point1 and point2 if there was such, null otherwise
     */
    Double deleteDistance(T point1, T point2);

    /**
     * @return true if there is such distance associated with (point1, point2)
     * unordered pair, false otherwise
     */
    boolean containsDistance(T point1, T point2);

    /**
     * @return distance associated with (point1, point2) unordered pair
     * null otherwise
     */
    Double getDistance(T point1, T point2);

    /**
     * removes all values from DistanceMatrixImpl which coordinates include point
     * @param point
     * @return all deleted values: pairs and associated distances
     */
    Map<T, Double> deleteRow(T point);

    /**
     * @return pair with which smallest distance is associated null if no distances stored
     */
    UnorderedPair<T> getPairWithMinDistance();

    /**
     * @return pair with which largest distance is associated null if no distances stored
     */
    UnorderedPair<T> getPairWithMaxDistance();

    int size();
}
