import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by sandulmv on 28.07.17.
 */
public class DistanceMatrixImpl<T> implements DistanceMatrix<T> {
    private HashMap<UnorderedPair<T>, Double> distanceMatrix;

    public DistanceMatrixImpl() {
        distanceMatrix = new HashMap<>();
    }

    @Override
    public Double addDistance(T point1, T point2, double distance)
            throws IllegalArgumentException {
        if (distance < 0) {
            throw new IllegalArgumentException("Distance shouldn't be less than 0");
        }

        return distanceMatrix.put(new UnorderedPairImpl<>(point1, point2), distance);
    }

    @Override
    public Double deleteDistance(T point1, T point2) {
        return distanceMatrix.remove(new UnorderedPairImpl<>(point1, point2));
    }

    @Override
    public Map<UnorderedPair<T>, Double> deleteRow(T point) {
        Set<UnorderedPair<T>> rowKeys = distanceMatrix.keySet()
                .stream()
                .filter(i -> i.inPair(point))
                .collect(Collectors.toSet());
        Map<UnorderedPair<T>, Double> row = new HashMap<>();
        for (UnorderedPair<T> key : rowKeys) {
            row.put(key, distanceMatrix.remove(key));
        }
        return row;
    }

    @Override
    public Double getDistance(T point1, T point2) {
        return distanceMatrix.get(new UnorderedPairImpl<>(point1, point2));
    }

    @Override
    public boolean containsDistance(T point1, T point2) {
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
}
