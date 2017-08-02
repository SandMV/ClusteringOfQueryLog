import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by sandulmv on 28.07.17.
 */

/**
 * This implementation does not allow to have null in coordinates
 *
 * @param <T> type of coordinates
 */
public class DistanceMatrixImpl<T> implements DistanceMatrix<T> {
    private HashMap<UnorderedPair<T>, Double> distanceMatrix;

    public DistanceMatrixImpl() {
        distanceMatrix = new HashMap<>();
    }

    /**
     * @param point1 is one of two coordinates with which distance will be associated
     * @param point2 is one of two coordinates with which distance will be associated
     * @param distance between point1 and point2
     * @return old distance between point1 and point2 or null if there wasn't such distance
     * @throws IllegalArgumentException when point1 or point2 is null or distance less then 0
     */
    @Override
    public Double addDistance(T point1, T point2, double distance) throws IllegalArgumentException {
        if (distance < 0) {
            throw new IllegalArgumentException("Distance shouldn't be less than 0");
        }

        if (point1 == null || point2 == null) {
            throw new IllegalArgumentException("Points should be not null");
        }

        return distanceMatrix.put(new UnorderedPairImpl<>(point1, point2), distance);
    }

    @Override
    public Double deleteDistance(T point1, T point2) {
        if (point1 == null || point2 == null) {
            return null;
        }

        return distanceMatrix.remove(new UnorderedPairImpl<>(point1, point2));
    }

    @Override
    public Map<T, Double> deleteRow(T point) {
        if (point == null) {
            return new HashMap<>();
        }

        Set<UnorderedPair<T>> rowKeys = distanceMatrix.keySet()
                .stream()
                .filter(i -> i.inPair(point))
                .collect(Collectors.toSet());
        Map<T, Double> row = new HashMap<>();
        for (UnorderedPair<T> key : rowKeys) {
            row.put(key.getNotEqualTo(point), distanceMatrix.remove(key));
        }
        return row;
    }

    @Override
    public Double getDistance(T point1, T point2) {
        if (point1 == null || point2 == null) {
            return null;
        }

        return distanceMatrix.get(new UnorderedPairImpl<>(point1, point2));
    }

    @Override
    public boolean containsDistance(T point1, T point2) {
        if(point1 == null || point2 == null) {
            return false;
        }

        return distanceMatrix.containsKey(new UnorderedPairImpl<>(point1, point2));
    }

    @Override
    public UnorderedPair<T> getPairWithMinDistance() {
        double minDistance = Double.MAX_VALUE;
        UnorderedPair<T> minPair = null;
        for (Map.Entry<UnorderedPair<T>, Double> entry : distanceMatrix.entrySet()) {
            double distance = entry.getValue();
            if (distance <= minDistance) {
                minDistance = distance;
                minPair = entry.getKey();
            }
        }
        return minPair;
    }

    @Override
    public UnorderedPair<T> getPairWithMaxDistance() {
        double maxDistance = Double.MIN_VALUE;
        UnorderedPair<T> minPair = null;
        for (Map.Entry<UnorderedPair<T>, Double> entry : distanceMatrix.entrySet()) {
            double distance = entry.getValue();
            if (distance >= maxDistance) {
                maxDistance = distance;
                minPair = entry.getKey();
            }
        }
        return minPair;
    }

    @Override
    public int size() {
        return distanceMatrix.size();
    }
}
